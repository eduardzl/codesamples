package com.verint.textanalytics.bl.applicationservices.facet.textelements;

import com.google.common.base.Throwables;
import com.verint.textanalytics.bl.applicationservices.ApplicationService;
import com.verint.textanalytics.bl.applicationservices.ConfigurationService;
import com.verint.textanalytics.bl.applicationservices.SampleFilterService;
import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionErrorCode;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionException;
import com.verint.textanalytics.common.utils.CollectionUtils;
import com.verint.textanalytics.common.utils.ThreadUtils;
import com.verint.textanalytics.dal.darwin.TextAnalyticsProvider;
import com.verint.textanalytics.model.analyze.MetricData;
import com.verint.textanalytics.model.analyze.TextElementSentimentsMetric;
import com.verint.textanalytics.model.facets.*;
import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by EZlotnik on 2/7/2016.
 */
public class TextElementsFacetService extends ApplicationService {
	private final String slash = "/";

	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	private TextAnalyticsProvider textAnalyticsProvider;

	@Autowired
	private ConfigurationManager configurationManager;

	@Autowired
	private FacetOnChildrenImpl facetOnChildren;

	@Autowired
	private TopLeafsFacetImpl topLeafsFacet;

	@Autowired
	private SampleFilterService sampleFilterService;

	@Autowired
	@Setter
	private ConfigurationService configurationService;

	private final int hierarchyTopLevel = 1;

	private final List<TextElementMetricType> volumeMetrics = Arrays.asList(TextElementMetricType.Volume);
	private final TextElementMetricType volumeMetric = TextElementMetricType.Volume;

	private List<TextElementMetricType> metricsToCalculateAll;

	/**
	 * Constructor.
	 */
	public TextElementsFacetService() {
		metricsToCalculateAll = new ArrayList<>();
		metricsToCalculateAll.addAll(Arrays.asList(TextElementMetricType.Volume, TextElementMetricType.AvgHandleTime, TextElementMetricType.AvgMessagesCount, TextElementMetricType.AvgEmployeesMessages,
		                                           TextElementMetricType.AvgCustomerMessages, TextElementMetricType.AvgEmployeeResponseTime, TextElementMetricType.AvgCustomerResponseTime));
	}

	/**
	 * Retrieves Text Elements facet with interactions counts only.
	 *
	 * @param tenant            Tenant of the search
	 * @param channel           Which channel should be searched
	 * @param searchContext     search context (Filter fields and query terms)
	 * @param backgroundContext background context (Filter fields and query terms)
	 * @param textElementType   textElement
	 * @param speakerType       facetOnSpeaker
	 * @param sameUtteranceMode sameUtteranceMode
	 * @param topLevelLimit     limit on top level facet
	 * @param descendantsLimit  linit on descendants facets
	 * @param calculationType   facet calculation type : FacetOnChildren or TopLeafsJoin
	 * @return text elements facet
	 * @throws TextQueryExecutionException
	 */
	public TextElementFacetResult getTextElementsFacet(String tenant, String channel, SearchInteractionsContext searchContext, SearchInteractionsContext backgroundContext, TextElementType textElementType, SpeakerQueryType speakerType, boolean sameUtteranceMode, int topLevelLimit, int descendantsLimit, TextElementsFacetCalculationType calculationType) {
		List<TextElementsFacetNode> textElements = null;
		TextElementFacetResult result = new TextElementFacetResult();
		List<TextElementSentimentsMetric> textElementsSentimentsMetrics = null;
		Integer interactionsTotalCount = 0;

		try {

			String requestId = ThreadContext.get(TAConstants.requestId);

			String language = configurationService.getChannelLanguage(tenant, channel);

			interactionsTotalCount = textAnalyticsProvider.getResultSetInteractionsQuantity(tenant, channel, searchContext, language);

			// creating clone of context before it is modified
			// considering other threads working on the same original context
			searchContext = searchContext.cloneMe();

			// adding sampling filter in case current result set is greater than configured treshold
			boolean sampleApplyed = this.sampleFilterService.addSampleFilter(searchContext, interactionsTotalCount);
			if (sampleApplyed) {
				// getting # of interactions with sample so percentage values will be good estimated
				interactionsTotalCount = textAnalyticsProvider.getResultSetInteractionsQuantity(tenant, channel, searchContext, language);

			}

			result.setBasedOnSample(sampleApplyed);

			Pair<List<TextElementsFacetNode>, List<TextElementsFacetNode>> textElementsResult = null;

			switch (calculationType) {
				case FacetOnChildren:
					// calculate Topics/Relations facet by calculating on parents and on thier child text elements
					textElementsResult = facetOnChildren.getTextElementsFacetWithStats(tenant, channel, searchContext, backgroundContext, textElementType, hierarchyTopLevel, null,
					                                                                   volumeMetrics, volumeMetric, speakerType, sameUtteranceMode, false, topLevelLimit, descendantsLimit);
					break;

				case FacetOnTopLeafs:
				default:
					// calculate Topics/Relations facet by calculating on parents and top leafes and then joining when possible
					textElementsResult = topLeafsFacet.getTextElementsFacetWithStats(tenant, channel, searchContext, backgroundContext,  textElementType, hierarchyTopLevel, null,
					                                                                 volumeMetrics, volumeMetric, speakerType, sameUtteranceMode, false, topLevelLimit, descendantsLimit);

					break;
			}
			textElements = textElementsResult.getLeft();


			if (textElements != null && interactionsTotalCount != 0) {
				for (val textElementGroup : textElements) {
					updateTextElementPercentage(textElementGroup, interactionsTotalCount);
				}
			}
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
		}
		result.setTextElementsFacetNodeList(textElements);

		return result;
	}


	/**
	 * Retrieves entities facet.
	 *
	 * @param tenant                Tenant of the search
	 * @param channel               Which channel should be searched
	 * @param searchContext         search context (Filter fields and query terms)
	 * @param backgroundContext     background context (Filter fields and query terms)
	 * @param textElementType       textElement
	 * @param hierarchyLevelNumber  level of hierarchy to retrieve
	 * @param textElements          data of text elements to retrieve
	 * @param sizeByMetric          size metric
	 * @param colorByMetric         color metric
	 * @param speakerType           facetOnSpeaker
	 * @param sameUtteranceMode     sameUtteranceMode
	 * @param leavesOnly            should leaves only data be retrieved
	 * @param rootLevelLimit        limit on top level facet
	 * @param descendantsLevelLimit limit of descendant facets
	 * @param calculationType       Topics/Relations facet calculation type : facet on children or join on top leafs
	 * @return text elements facet nodes
	 * @throws TextQueryExecutionException
	 */
	//@formatter:off
	public TextElementFacetResult  getTextElementsFacetWithStats(String tenant, String channel, SearchInteractionsContext searchContext, SearchInteractionsContext backgroundContext,
																	TextElementType textElementType,
																	int hierarchyLevelNumber, List<TextElementsFacetNode> textElements,
																	TextElementMetricType sizeByMetric, TextElementMetricType colorByMetric,
																	SpeakerQueryType speakerType, boolean sameUtteranceMode, boolean leavesOnly, int rootLevelLimit, int descendantsLevelLimit,
			                                                        TextElementsFacetCalculationType calculationType) {
	//@formatter:on

		List<TextElementsFacetNode> currentSearchContextTextElements = null;
		TextElementFacetResult result = new TextElementFacetResult();
		List<TextElementsFacetNode> backgoundSerchContextTextElements = null;
		List<TextElementSentimentsMetric> textElementsSentiment = null;
		Integer interactionsTotalCount = 0;
		Integer interactionsBackgroundCount = 0;


		try {

			String requestId = ThreadContext.get(TAConstants.requestId);

			// metrics to calculate
			List<TextElementMetricType> metricsToCalc = Arrays.asList(sizeByMetric, colorByMetric);

			String language = configurationService.getChannelLanguage(tenant, channel);

			interactionsTotalCount = textAnalyticsProvider.getResultSetInteractionsQuantity(tenant, channel, searchContext, language);


			// creating clone of context before it is modified
			// considering other threads working on the same original context
			searchContext = searchContext.cloneMe();
			backgroundContext = searchContext.cloneMe();

			// adding sampling filter in case current result set is greater than configured treshold
			boolean sampleFilterAdded = this.sampleFilterService.addSampleFilter(searchContext, interactionsTotalCount);
			result.setBasedOnSample(sampleFilterAdded);

			if (sizeByMetric == TextElementMetricType.CorrelationPercentage) {
				// only on Correlation feature need background
				interactionsBackgroundCount = textAnalyticsProvider.getResultSetInteractionsQuantity(tenant, channel, backgroundContext, language);
				this.sampleFilterService.addSampleFilter(backgroundContext, interactionsBackgroundCount);
			}


			Pair<?, ?> textElementsResult = null;

			switch (calculationType) {
				case FacetOnChildren:
					// retrieve Text Elemennt with Metrics and Backgound Data if requested
					textElementsResult = facetOnChildren.getTextElementsFacetWithStats(tenant, channel, searchContext, backgroundContext, textElementType,
					                                                                   hierarchyLevelNumber, null, metricsToCalc, sizeByMetric, speakerType,
					                                                                   sameUtteranceMode, leavesOnly, rootLevelLimit, descendantsLevelLimit);
					break;
				case FacetOnTopLeafs:
				default:
					textElementsResult = topLeafsFacet.getTextElementsFacetWithStats(tenant, channel, searchContext, backgroundContext,  textElementType,
					                                                                 hierarchyLevelNumber, null, metricsToCalc, sizeByMetric, speakerType,
					                                                                 sameUtteranceMode, leavesOnly, rootLevelLimit, descendantsLevelLimit);
					break;
			}

			if (textElementsResult.getLeft() != null) {
				currentSearchContextTextElements = (List<TextElementsFacetNode>) textElementsResult.getLeft();
			} else {
				// if got nothing - make sure to return empty list
				currentSearchContextTextElements = new ArrayList<>();
			}

			if (textElementsResult.getRight() != null) {
				backgoundSerchContextTextElements = (List<TextElementsFacetNode>) textElementsResult.getRight();
			} else {
				// if got nothing - make sure to return empty list
				backgoundSerchContextTextElements = new ArrayList<>();
			}


			// Calcute Percentage
			if (currentSearchContextTextElements != null && interactionsTotalCount != 0) {
				for (TextElementsFacetNode textElement : currentSearchContextTextElements) {
					updateTextElementPercentage(textElement, interactionsTotalCount);
				}
			}

			if (sizeByMetric == TextElementMetricType.CorrelationPercentage || colorByMetric == TextElementMetricType.CorrelationPercentage) {
				if (currentSearchContextTextElements != null && backgoundSerchContextTextElements != null) {
					this.updateFacetGroupCorrelativePercentage(currentSearchContextTextElements, backgoundSerchContextTextElements);
				}
			}
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
		}

		result.setTextElementsFacetNodeList(currentSearchContextTextElements);

		return result;
	}


	/**
	 * Retrieves Metrics for single Entity or single Relation.
	 *
	 * @param tenant            tenant
	 * @param channel           channel
	 * @param searchContext     searchContext of request
	 * @param backgroundContext background search context
	 * @param textElementType   text element type : Entity, Relation ...
	 * @param textElementValue  text element value
	 * @return text element metrics
	 */
	public TextElementsFacetNode getTextElementMetrics(String tenant, String channel, SearchInteractionsContext searchContext, SearchInteractionsContext backgroundContext, TextElementType textElementType, String textElementValue) {

		TextElementsFacetNode textElement = null, textElementBackground = null;
		ExecutorService threadPool = null;

		try {
			String language = configurationService.getChannelLanguage(tenant, channel);
			int interactionsTotalCount = 0;
			List<TextElementSentimentsMetric> textElementSentiment = null;

			val tasks = new ArrayList<Callable<Object>>();
			threadPool = Executors.newFixedThreadPool(4);

			String requestId = ThreadContext.get(TAConstants.requestId);

			ApplicationConfiguration appConfig = configurationManager.getApplicationConfiguration();

			TextElementsFacetNode textElementNode = TextElementsFacetNode.buildFromPathString(textElementValue, false);

			// Metrics for Text Element
			tasks.add(() -> {
				ThreadContext.put(TAConstants.requestId, requestId);

				// retrieve Metrics for text element
				List<TextElementsFacetNode> textElementsWithMetrics = this.textAnalyticsProvider.getTextElementsMetrics(tenant, channel, searchContext, language, textElementType,
				                                                                                                        Arrays.asList(textElementNode), metricsToCalculateAll, false);

				return new ImmutablePair<>(TextElementsFacet.METRICS_THREAD, !CollectionUtils.isEmpty(textElementsWithMetrics) ? textElementsWithMetrics.get(0) : null);

			});

			tasks.add(() -> {
				ThreadContext.put(TAConstants.requestId, requestId);

				List<TextElementsFacetNode> textElementsWithMetrics = this.textAnalyticsProvider.getTextElementsMetrics(tenant, channel, backgroundContext, language,
				                                                                                                        textElementType, Arrays.asList(textElementNode),
				                                                                                                        volumeMetrics, false);

				return new ImmutablePair<>(TextElementsFacet.METRICS_BACKGROUND_THREAD, !CollectionUtils.isEmpty(textElementsWithMetrics) ? textElementsWithMetrics.get(0) : null);

			});

			// Get counting for interactions
			tasks.add(() -> {
				ThreadContext.put(TAConstants.requestId, requestId);

				int resultSetInteractionsQuantity = textAnalyticsProvider.getResultSetInteractionsQuantity(tenant, channel, searchContext, language);

				return new ImmutablePair<>(TextElementsFacet.CURRENT_SEARCH_INTERACTIONS_COUNT_THREAD, resultSetInteractionsQuantity);
			});

			// Text Element Sentiment
			tasks.add(() -> {
				ThreadContext.put(TAConstants.requestId, requestId);

				// retrieve Text Element Sentiment
				return new ImmutablePair<>(TextElementsFacet.SINGLE_SENTIMENT_THREAD,
				                           textAnalyticsProvider.getTextElementsSentiment(tenant, channel, searchContext, language, textElementType, Arrays.asList(textElementNode), -1));
			});

			// invokeAll() returns when all tasks are complete
			List<Future<Object>> lstFutures = threadPool.invokeAll(tasks, appConfig.getDarwinRestRequestTimeout(), TimeUnit.SECONDS);

			if (lstFutures != null) {

				for (Future<?> future : lstFutures) {
					if (future.isDone()) {
						// get tasks result of task is done
						val taskResult = (Pair<String, Object>) future.get();
						if (taskResult != null) {
							switch (taskResult.getLeft()) {
								case TextElementsFacet.METRICS_THREAD:
									if (taskResult.getRight() != null) {
										textElement = (TextElementsFacetNode) taskResult.getRight();
									}
									break;
								case TextElementsFacet.METRICS_BACKGROUND_THREAD:
									if (taskResult.getRight() != null) {
										textElementBackground = (TextElementsFacetNode) taskResult.getRight();
									}
									break;
								case TextElementsFacet.CURRENT_SEARCH_INTERACTIONS_COUNT_THREAD:
									if (taskResult.getRight() != null) {
										interactionsTotalCount = ((Integer) taskResult.getRight());
									}
									break;
								case TextElementsFacet.SINGLE_SENTIMENT_THREAD:
									if (taskResult.getRight() != null) {
										textElementSentiment = (List<TextElementSentimentsMetric>) taskResult.getRight();
									}
									break;
								default:
									break;
							}
						}
					}
				}
			}

			// Volume Percentage
			if (interactionsTotalCount != 0) {
				textElement.setPercentage((double) textElement.getNumberOfInteractions() / interactionsTotalCount * TAConstants.percentage_100);
			}

			// Correlation Percentage
			if (textElementBackground != null) {
				double backgroundNumberOfInteractions = (double) textElementBackground.getNumberOfInteractions();
				if (backgroundNumberOfInteractions != 0) {
					textElement.setCorrelationPercentage((double) textElement.getNumberOfInteractions() / backgroundNumberOfInteractions * TAConstants.percentage_100);
				}
			}

			if (!CollectionUtils.isEmpty(textElementSentiment)) {
				textElement.getMetrics().add(new MetricData(TextElementMetricType.AvgSentiment.name(), textElementSentiment.get(0).getSentimentAvg()));
			}

		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
		} finally {
			ThreadUtils.shutdownExecutionThreadPool(threadPool);
		}

		return textElement;
	}

	/**
	 * Recursively update percentage for each node.
	 *
	 * @param facetNode
	 *            facet node
	 * @param totalInteractionsCount
	 *            total interactiosn in current result set
	 */
	private void updateTextElementPercentage(TextElementsFacetNode facetNode, Integer totalInteractionsCount) {

		if (totalInteractionsCount != 0) {
			facetNode.setPercentage((double) facetNode.getNumberOfInteractions() / totalInteractionsCount * TAConstants.percentage_100);
		}

		if (facetNode.getChildren() != null) {
			for (TextElementsFacetNode childFacetNode : facetNode.getChildren()) {
				updateTextElementPercentage(childFacetNode, totalInteractionsCount);
			}
		}
	}


	private void updateFacetGroupCorrelativePercentage(List<TextElementsFacetNode> searchContextFacet, List<TextElementsFacetNode> backgroundContextFacet) {
		HashMap<String, TextElementsFacetNode> bgFacetHash = new HashMap<>();

		// Fill HashTable
		this.createCorrelativeBackground(backgroundContextFacet, bgFacetHash);

		// Percentage
		this.updateFacetGroupCorrelativePercentageRecursive(searchContextFacet, bgFacetHash);
	}

	private void createCorrelativeBackground(List<TextElementsFacetNode> backgroundContextFacet, HashMap<String, TextElementsFacetNode> backgroundContextFacetHash) {
		backgroundContextFacet.forEach(f -> {
			backgroundContextFacetHash.put(f.getValue(), f);
			if (f.getChildren().size() > 0)
				createCorrelativeBackground(f.getChildren(), backgroundContextFacetHash);
		});
	}

	private void updateFacetGroupCorrelativePercentageRecursive(List<TextElementsFacetNode> searchContextFacet, HashMap<String, TextElementsFacetNode> bgFacetHash) {

		searchContextFacet.forEach(f -> {
			if (bgFacetHash.containsKey(f.getValue())) {
				float a = (float) f.getNumberOfInteractions();
				float b = (float) bgFacetHash.get(f.getValue()).getNumberOfInteractions();
				f.setCorrelationPercentage(a / b * TAConstants.percentage_100);
			} else {
				logger.debug("Correlation Percentage: the background value for " + f.getValue() + "was not found");
			}

			if (f.getChildren().size() > 0) {
				updateFacetGroupCorrelativePercentageRecursive(f.getChildren(), bgFacetHash);
			}
		});
	}

}
