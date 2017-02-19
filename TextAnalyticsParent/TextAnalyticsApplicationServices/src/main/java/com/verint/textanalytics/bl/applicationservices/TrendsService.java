package com.verint.textanalytics.bl.applicationservices;

import com.google.common.base.Throwables;
import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionErrorCode;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionException;
import com.verint.textanalytics.common.utils.CollectionUtils;
import com.verint.textanalytics.common.utils.ThreadUtils;
import com.verint.textanalytics.dal.darwin.TextAnalyticsProvider;
import com.verint.textanalytics.model.facets.SpeakerQueryType;
import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import com.verint.textanalytics.model.storedSearch.Category;
import com.verint.textanalytics.model.trends.DiscoverTrendsContext;
import com.verint.textanalytics.model.trends.InteractionDailyVolumeDataPoint;
import com.verint.textanalytics.model.trends.TextElementTrend;
import com.verint.textanalytics.model.trends.TrendType;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Facets service.
 *
 * @author EZlotnik
 */
public class TrendsService extends ApplicationService {

	@Autowired
	private TextAnalyticsProvider textAnalyticsProvider;

	@Autowired
	@Getter
	@Setter
	private StoredSearchService storedSearchService;

	@Autowired
	@Getter
	@Setter
	private ConfigurationManager configurationManager;

	@Autowired
	@Setter
	private ConfigurationService configurationService;

	/**
	 * Retrieves date facet.
	 *
	 * @param tenant                Tenant of the search
	 * @param channel               Which channel should be searched
	 * @param discoverTrendsContext discover trends context (Trends Period, Filter fields and
	 *                              query terms)
	 * @return date facet nodes
	 * @throws TextQueryExecutionException
	 */
	public List<InteractionDailyVolumeDataPoint> getInteractionsDailyVolumeSeries(String tenant, String channel, SearchInteractionsContext discoverTrendsContext) {
		String language = configurationService.getChannelLanguage(tenant, channel);
		return this.textAnalyticsProvider.getInteractionsDailyVolumeSeries(tenant, channel, discoverTrendsContext, language);
	}

	/**
	 * Retrieves a date facet for a specific entity.
	 *
	 * @param tenant                tenant
	 * @param channel               channel
	 * @param trendType             trendType
	 * @param entityValue           the entity value : ("1/device")
	 * @param discoverTrendsContext discover trends context
	 * @param speaker               speaker
	 * @return a date facet for scpeficic entity.
	 */
	public List<InteractionDailyVolumeDataPoint> getEntityTrendDailyVolumeSeriesByType(String tenant, String channel, TrendType trendType, String entityValue, SearchInteractionsContext discoverTrendsContext, SpeakerQueryType speaker) {

		String language = configurationService.getChannelLanguage(tenant, channel);
		List<InteractionDailyVolumeDataPoint> result = this.textAnalyticsProvider.getTrendDailyVolumeSeriesByType(tenant, channel, trendType, entityValue, discoverTrendsContext,
		                                                                                                          language, speaker);

		// sort serie
		result.sort((a, b) -> {
			return a.getDate().compareTo(b.getDate());
		});

		return result;

	}

	/**
	 * Retrieve entity trends.
	 *
	 * @param tenant           tenant
	 * @param channel          channel
	 * @param searchContext    searchContext
	 * @param trendType        type of requested trend
	 * @param textElementValue text element(Entity, Relation, Keyword) value
	 * @param sortProperty     sort propert
	 * @param sortDirection    sort direction
	 * @param speaker          trends according to speaker type
	 * @return list of entity trends
	 */
	public List<TextElementTrend> getTextElementsTrends(String tenant, String channel, DiscoverTrendsContext searchContext, TrendType trendType, String textElementValue, String sortProperty, String sortDirection, SpeakerQueryType speaker) {
		List<TextElementTrend> textElementsTrends = this.textAnalyticsProvider.getTextElementsTrends(tenant, channel, searchContext, trendType, textElementValue, sortProperty,
		                                                                                             sortDirection, speaker);
		this.setTrendType(textElementsTrends, trendType);
		return textElementsTrends;
	}

	/**
	 * Retrieve entity trends.
	 *
	 * @param tenant           tenant
	 * @param channel          channel
	 * @param searchContext    searchContext
	 * @param textElementValue text element(Entity, Relation, Keyword) value
	 * @param sortProperty     sort propert
	 * @param sortDirection    sort direction
	 * @return list of entity trends
	 */
	public List<TextElementTrend> getCategoriesTrends(String tenant, String channel, DiscoverTrendsContext searchContext, String textElementValue, String sortProperty, String sortDirection) {

		ExecutorService threadPool = null;

		List<TextElementTrend> textElementsTrends = null;
		List<TextElementTrend> textElementsTrendsFiltered = null;

		HashMap<Integer, Category> categoriesMap = null;

		try {

			String requestId = ThreadContext.get(TAConstants.requestId);

			threadPool = Executors.newFixedThreadPool(2);

			val tasks = new ArrayList<Callable<Object>>();

			tasks.add(() -> {
				ThreadContext.put(TAConstants.requestId, requestId);
				return new ImmutablePair<>("CategoriesTrend", getTextElementsTrends(tenant, channel, searchContext, TrendType.Categories, textElementValue,
				                                                 sortProperty, sortDirection, SpeakerQueryType.Any));
			});

			tasks.add(() -> {
				ThreadContext.put(TAConstants.requestId, requestId);
				return new ImmutablePair<>("CategoryNames", getStoredSearchService().getCategoriesMap(tenant, channel));
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
								case "CategoriesTrend":
									textElementsTrends = (List<TextElementTrend>) resultPair.getRight();
									break;
								case "CategoryNames":
									categoriesMap = (HashMap<Integer, Category>) resultPair.getRight();
									break;
								default:
									break;
							}
						}
					}
				}
			}

			//@formatter:off
			if (!CollectionUtils.isEmpty(textElementsTrends)) {

				// filter IsActive categories only
				HashMap<Integer, Category> catMap = (HashMap) categoriesMap.entrySet()
				                                                 .stream()
				                                                 .filter(c -> c.getValue().isActive())
				                                                 .collect(Collectors.toMap(c -> c.getKey(), c -> c.getValue()));

				// Merge with Category Names
				textElementsTrendsFiltered = textElementsTrends.stream()
                    // get only categories with names
					.filter(f -> catMap.containsKey(Integer.parseInt(f.getName())))
	                .map(f -> {
						Integer catId = Integer.parseInt(f.getName());
			            f.setValue(f.getName());
		                f.setName(catMap.get(catId).getName());
		                f.setTrendType(TrendType.Categories);
						return f;
					})
	                .collect(Collectors.toList());

				// sorting categories by name is the only sort that not happen on SOLR.
				// handling this case
				if (sortProperty.toLowerCase().compareTo("name") == 0) {

					textElementsTrendsFiltered.sort(new Comparator<TextElementTrend>() {
						@Override
						public int compare(TextElementTrend t1, TextElementTrend t2) {
							if (sortDirection.compareToIgnoreCase("desc") == 0) {
								return t2.getName().compareToIgnoreCase(t1.getName());
							} else {
								return t1.getName().compareToIgnoreCase(t2.getName());
							}
						}
					});

				}
			}

		//@formatter:on
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
		} finally {
			ThreadUtils.shutdownExecutionThreadPool(threadPool);
		}

		return textElementsTrendsFiltered;
	}

	/**
	 * Retrieve entity trends.
	 *
	 * @param tenant           tenant
	 * @param channel          channel
	 * @param searchContext    searchContext
	 * @param textElementValue text element(Entity, Relation, Keyword) value
	 * @param sortProperty     sort propert
	 * @param sortDirection    sort direction
	 * @param speaker          trends according to speaker type
	 * @return list of entity trends
	 */
	@SuppressWarnings("unchecked")
	public List<TextElementTrend> getMergedElementsTrends(String tenant, String channel, DiscoverTrendsContext searchContext, String textElementValue, String sortProperty, String sortDirection, SpeakerQueryType speaker) {

		ExecutorService threadPool = null;
		List<TextElementTrend> entitityTrends = null;
		List<TextElementTrend> relationTrends = null;
		List<TextElementTrend> mergedTrends = null;

		try {
			threadPool = Executors.newFixedThreadPool(2);

			String requestId = ThreadContext.get(TAConstants.requestId);

			ArrayList<Callable<Object>> tasks = new ArrayList<Callable<Object>>();

			tasks.add(() -> {
				ThreadContext.put(TAConstants.requestId, requestId);
				return new ImmutablePair<>("EntitiesTrends",
				                           this.getTextElementsTrends(tenant, channel, searchContext, TrendType.Entities, textElementValue, sortProperty, sortDirection, speaker));
			});

			tasks.add(() -> {
				ThreadContext.put(TAConstants.requestId, requestId);
				return new ImmutablePair<>("RelationsTrends", this.getTextElementsTrends(tenant, channel, searchContext, TrendType.Relations, textElementValue, sortProperty,
				                                                      sortDirection, speaker));
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
								case "EntitiesTrends":
									entitityTrends = (List<TextElementTrend>) resultPair.getRight();
									break;
								case "RelationsTrends":
									relationTrends = (List<TextElementTrend>) resultPair.getRight();
									break;
								default:
									break;
							}
						}
					}
				}
			}

			mergedTrends = new ArrayList<>();

			if (entitityTrends != null)
				mergedTrends.addAll(entitityTrends);

			if (relationTrends != null)
				mergedTrends.addAll(relationTrends);

		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
		} finally {
			ThreadUtils.shutdownExecutionThreadPool(threadPool);
		}

		return mergedTrends;
	}

	private void setTrendType(List<TextElementTrend> trends, TrendType trendType) {
		trends.forEach(trend -> trend.setTrendType(trendType));
	}

}
