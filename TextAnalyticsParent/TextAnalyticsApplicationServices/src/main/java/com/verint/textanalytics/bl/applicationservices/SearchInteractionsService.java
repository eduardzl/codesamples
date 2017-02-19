package com.verint.textanalytics.bl.applicationservices;

import com.google.common.base.Throwables;
import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.exceptions.StoredSearchesErrorCode;
import com.verint.textanalytics.common.exceptions.StoredSearchesException;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionErrorCode;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionException;
import com.verint.textanalytics.common.utils.CollectionUtils;
import com.verint.textanalytics.common.utils.ThreadUtils;
import com.verint.textanalytics.dal.darwin.TextAnalyticsProvider;
import com.verint.textanalytics.model.interactions.*;
import com.verint.textanalytics.model.storedSearch.Category;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Search Documents application service.
 *
 * @author EZlotnik
 */
public class SearchInteractionsService extends ApplicationService {
	private Logger logger = LogManager.getLogger(this.getClass());

	/**
	 * Describes the type of local position.
	 *
	 * @author NShunewich
	 */
	public enum PositionType {
		START,
		END
	}

	@Autowired
	private TextAnalyticsProvider textAnalyticsProvider;

	@Getter
	@Setter
	@Autowired
	private ConfigurationManager configurationManager;

	@Autowired
	private HighlightOverlapManager highlightOverlapManager;

	@Getter
	@Setter
	@Autowired
	private SnippetsGenerator snippetsGenerator;

	@Autowired
	private HighlightsGenerator highlightsGenerator;

	@Autowired
	private StoredSearchService storedSearchService;

	@Autowired
	@Setter
	private ConfigurationService configurationService;

	/**
	 * Constructor.
	 */
	public SearchInteractionsService() {
	}

	/**
	 * Invokes request to Darwin data access.
	 *
	 * @param tenant        Tenant of the search
	 * @param channel       Which channel should be searched
	 * @param searchContext search context (Filter fields and query terms)
	 * @param pageStart     start of interactions range
	 * @param pageSize      size of page
	 * @param sortProperty  sort property
	 * @param sortDirection sort direction
	 * @return list of interactions which match search criteria
	 */
	public SearchInteractionsResult searchInteractions(String tenant, String channel, SearchInteractionsContext searchContext, int pageStart, int pageSize, String sortProperty, String sortDirection) {

		SearchInteractionsResult searchInteractionsResult = null;
		Map<String, Category> categories = null;
		ExecutorService threadPool = null;

		try {

			threadPool = Executors.newFixedThreadPool(2);
			String requestId = ThreadContext.get(TAConstants.requestId);
			val tasks = new ArrayList<Callable<Object>>();

			tasks.add(() -> {
				ThreadContext.put(TAConstants.requestId, requestId);
				return new ImmutablePair<>("Categories", this.storedSearchService.getActiveCategoriesMap(tenant, channel));
			});

			tasks.add(() -> {
				ThreadContext.put(TAConstants.requestId, requestId);
				return new ImmutablePair<>("Interactions", this.searchInteractionsImpl(tenant, channel, searchContext, pageStart, pageSize, sortProperty, sortDirection));
			});

			ApplicationConfiguration appConfig = getConfigurationManager().getApplicationConfiguration();

			// invokeAll() returns when all tasks are complete
			List<Future<Object>> lstFutures = threadPool.invokeAll(tasks, appConfig.getDarwinRestRequestTimeout(), TimeUnit.SECONDS);

			if (lstFutures != null) {
				for (Future<?> future : lstFutures) {
					if (future.isDone()) {

						// get tasks result of task is done
						Object taskResult = future.get();
						if (taskResult != null) {
							ImmutablePair<String, ?> resultPair = (ImmutablePair<String, ?>) taskResult;

							switch (resultPair.getLeft()) {
								case "Interactions":
									searchInteractionsResult = (SearchInteractionsResult) resultPair.getRight();
									break;
								case "Categories":
									categories = (Map<String, Category>) resultPair.getRight();
									break;
								default:
									break;
							}
						}
					}
				}
			}
		} catch (ExecutionException ex) {
			Boolean propagateEx = true;

			List<Throwable> causeChain = Throwables.getCausalChain(ex);
			if (!CollectionUtils.isEmpty(causeChain)) {

				Optional<Throwable> storedSearchThOpt = causeChain.stream().filter(th -> th instanceof StoredSearchesException).findFirst();
				if (storedSearchThOpt.isPresent()) {
					StoredSearchesException stEx = (StoredSearchesException) storedSearchThOpt.get();

					// Categories Retrieval error should not be propagated
					// to allow Interactions list to be displayed
					if (stEx.getAppExecutionErrorCode().equals(StoredSearchesErrorCode.CategoriesRetriveError)) {
						propagateEx = false;

						logger.error("Exception while retrieving Categories for tenant - {}, channel - {}", tenant, channel, ex);
					}
				}
			}

			if (propagateEx) {
				Throwables.propagate(ex);
			}
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex.getCause(), TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
		} finally {
			try {
				ThreadUtils.shutdownExecutionThreadPool(threadPool);
			} catch (Exception ex) {
				logger.warn("Failed to shutdown thread execution pool.", ex);
			}

			if (searchInteractionsResult != null) {
				//update Interaction with categories name
				List<Interaction> interactions = searchInteractionsResult.getInteractions();
				Map<String, Category> activeCategories = categories;

				if (!CollectionUtils.isEmpty(interactions)) {

					// @formatter:off
					interactions.stream()
					            .filter(i -> i.getCategories() != null)
					            .forEach(i -> {

									// use only Active categories
									i.setCategories(i.getCategories().stream()
									                                 .filter(catTagging -> activeCategories.containsKey(catTagging.getId()))
									                                 .map(categoryTagging -> {
										                                 Category category = activeCategories.get(categoryTagging.getId());
										                                 categoryTagging.setName(category.getName());
										                                 return categoryTagging;
									                                 })
									                                 .sorted((a, b) -> a.getName().compareTo(b.getName()))
									                                 .collect(Collectors.toList()));
								});
					// @formatter:on
				}
			}
		}

		return searchInteractionsResult;
	}

	/**
	 * Invokes request to Darwin data access.
	 *
	 * @param tenant        Tenant of the search
	 * @param channel       Which channel should be searched
	 * @param searchContext search context (Filter fields and query terms)
	 * @param pageStart     start of interactions range
	 * @param pageSize      size of page
	 * @param sortProperty  sort property
	 * @param sortDirection sort direction
	 * @return list of interactions which match search criteria
	 */
	private SearchInteractionsResult searchInteractionsImpl(String tenant, String channel, SearchInteractionsContext searchContext, int pageStart, int pageSize, String sortProperty, String sortDirection) {

		String language = configurationService.getChannelLanguage(tenant, channel);

		SearchInteractionsResult searchInteractionsResult = this.textAnalyticsProvider.searchInteractions(tenant, channel, searchContext, language, pageStart, pageSize,
		                                                                                                  sortProperty, sortDirection);
		if (searchInteractionsResult != null && searchInteractionsResult.getInteractions() != null) {

			List<Interaction> interactions = searchInteractionsResult.getInteractions();

			this.highlightsGenerator.generateSPSHighlights(tenant, channel, interactions, searchContext,  true, false);

			// merge terms highlights and topics highlights into single collection
			// of mergedHighlights
			mergeHighlightsForInteractions(interactions);

			// solve overlapping
			highlightOverlapManager.solveOverlappingForInteractions(interactions);

			// build snippets
			snippetsGenerator.buildSnippets(searchContext, interactions, true);
		}

		return searchInteractionsResult;
	}

	/**
	 * Retrives number of interactions for search context and backgound search context.
	 *
	 * @param tenant            Tenant of the search
	 * @param channel           Which channel should be searched
	 * @param searchContext     search context (Filter fields and query terms)
	 * @param backgroundContext background context (filter)
	 * @return ResultsQuantity
	 */
	public ResultsQuantity getResultSetsInteractionsQuantity(String tenant, String channel, SearchInteractionsContext searchContext, SearchInteractionsContext backgroundContext) {
		ResultsQuantity resultsQuantity = new ResultsQuantity();
		ExecutorService threadPool = null;

		try {
			String requestId = ThreadContext.get(TAConstants.requestId);
			threadPool = Executors.newFixedThreadPool(2);

			String language = configurationService.getChannelLanguage(tenant, channel);

			val tasks = new ArrayList<Callable<Object>>();

			tasks.add(() -> {
				ThreadContext.put(TAConstants.requestId, requestId);

				Integer currentSearchResult = this.textAnalyticsProvider.getResultSetInteractionsQuantity(tenant, channel, searchContext, language);
				return new ImmutablePair<>("currentSearch", currentSearchResult);
			});

			tasks.add(() -> {
				ThreadContext.put(TAConstants.requestId, requestId);

				Integer totalSearchResult = this.textAnalyticsProvider.getResultSetInteractionsQuantity(tenant, channel, backgroundContext, language);

				return new ImmutablePair<String, Integer>("totalSearch", totalSearchResult);
			});

			ApplicationConfiguration appConfig = configurationManager.getApplicationConfiguration();

			// invokeAll() returns when all tasks are complete
			List<Future<Object>> lstFutures = threadPool.invokeAll(tasks, appConfig.getDarwinRestRequestTimeout(), TimeUnit.SECONDS);

			if (lstFutures != null) {
				for (Future<?> future : lstFutures) {
					if (future.isDone()) {

						// get tasks result of task is done
						Object taskResult = future.get();
						if (taskResult != null) {

							// result of Integer task
							val res = (Pair<String, Integer>) taskResult;

							switch (res.getLeft()) {
								case "currentSearch":
									resultsQuantity.setCurrentSearchInteractions(res.getRight());
									break;
								case "totalSearch":
									resultsQuantity.setTotalInteractions(res.getRight());
									break;
								default:
									break;
							}
						}
					}
				}
			}

		} catch (Exception ex) {
			// ex is concurrent exception - need to take the real exception
			Throwables.propagateIfInstanceOf(ex.getCause(), TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
		} finally {
			ThreadUtils.shutdownExecutionThreadPool(threadPool);
		}

		return resultsQuantity;
	}

	/**
	 * Retrieves number of interactiosn for search context.
	 *
	 * @param tenant        tenant
	 * @param channel       channel
	 * @param searchContext search context
	 * @return number of interactions
	 */
	public Integer getResultSetInteractionsQuantity(String tenant, String channel, SearchInteractionsContext searchContext) {
		String language = configurationService.getChannelLanguage(tenant, channel);
		return this.textAnalyticsProvider.getResultSetInteractionsQuantity(tenant, channel, searchContext, language);
	}

	/**
	 * Validates search query.
	 * @param searchQuery terms query to validate
	 */
	public void validateSearchTermsQuery(String searchQuery) {
		//TODO: YULIYA MUST FIX THIS
		this.textAnalyticsProvider.validateSearchQuery(searchQuery, "en");
	}

	/**
	 * Retrieves interaction's data for review.
	 *
	 * @param tenant        tenant
	 * @param channel       channel
	 * @param interactionId interaction's document id
	 * @param searchContext search context (Filter fields and query terms)
	 * @return data of interaction's review
	 */
	public Interaction getInteractionPreview(String tenant, String channel, String interactionId, SearchInteractionsContext searchContext) {

		String language = configurationService.getChannelLanguage(tenant, channel);

		Interaction interaction = this.textAnalyticsProvider.getInteractionPreview(tenant, channel, interactionId, searchContext, language);

		if (interaction != null) {

			val interactionList = new ArrayList<Interaction>();
			interactionList.add(interaction);

			this.highlightsGenerator.generateSPSHighlights(tenant, channel, interactionList, searchContext, false, true);

			// Merge Entities, Relations and Terms highlights into single collection
			// and remove those which are not in search context
			mergeHighlightsForUtterances(interaction.getUtterances());

			// solve overlapping
			HighlightOverlapManager om = new HighlightOverlapManager();
			om.solveOverlppingForUtterances(interaction.getUtterances());

			//Get Snippet data
			// build snippets
			snippetsGenerator.buildSnippets(searchContext, interactionList, false);
	}

		return interaction;
	}

	/**
	 * Merges TermHighlight and TopicHighlight into MergedHighlighting.
	 * collection
	 *
	 * @param interactions Interactions list to operate
	 */
	public void mergeHighlightsForInteractions(List<Interaction> interactions) {

		// merge highlights into interactions
		if (!CollectionUtils.isEmpty(interactions)) {

			interactions.parallelStream().forEach(interaction -> {
				mergeHighlightsForUtterances(interaction.getUtterances());
			});
		}
	}

	/**
	 * Merges TermHighlight and TopicHighlight into MergedHighlighting.
	 *
	 * @param utterances List of utterances
	 */
	public void mergeHighlightsForUtterances(List<Utterance> utterances) {
		if (!CollectionUtils.isEmpty(utterances)) {

			for (Utterance utterance : utterances) {
				List<BaseHighlight> mergedHighlight = new ArrayList<BaseHighlight>();

				if (utterance.getTermsHighlighting() != null) {
					mergedHighlight.addAll(utterance.getTermsHighlighting());
				}

				if (utterance.getEntitiesHighlighting() != null) {
					mergedHighlight.addAll(utterance.getEntitiesHighlightingForeachPosition());
				}

				// TODO: Check size for each if
				if (utterance.getKeytermsHighlighting() != null && utterance.getKeytermsHighlighting().size() > 0) {
					mergedHighlight.addAll(utterance.getKeytermsHighlightingForeachPosition());
				}

				// TODO add new RelationsHighlight for each position
				mergedHighlight.addAll(utterance.getRelationsHighlightingForeachPosition());

				utterance.setMergedHighlighting(mergedHighlight);
			}
		}
	}
}
