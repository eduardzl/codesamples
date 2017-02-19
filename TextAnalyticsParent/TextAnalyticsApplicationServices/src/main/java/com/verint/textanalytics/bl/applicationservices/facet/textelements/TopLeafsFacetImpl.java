package com.verint.textanalytics.bl.applicationservices.facet.textelements;

import com.google.common.base.Throwables;
import com.verint.textanalytics.bl.applicationservices.ConfigurationService;
import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionErrorCode;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionException;
import com.verint.textanalytics.common.utils.CollectionUtils;
import com.verint.textanalytics.common.utils.ThreadUtils;
import com.verint.textanalytics.model.analyze.MetricData;
import com.verint.textanalytics.model.analyze.TextElementSentimentsMetric;
import com.verint.textanalytics.model.facets.SpeakerQueryType;
import com.verint.textanalytics.model.facets.TextElementMetricType;
import com.verint.textanalytics.model.facets.TextElementType;
import com.verint.textanalytics.model.facets.TextElementsFacetNode;
import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Created by EZlotnik on 5/31/2016.
 */
public class TopLeafsFacetImpl extends TextElementsFacet {

	@Autowired
	@Setter
	private ConfigurationService configurationService;

	//@formatter:off
	@Override
	public Pair<List<TextElementsFacetNode>, List<TextElementsFacetNode>> getTextElementsFacetWithStats(String tenant, String channel, SearchInteractionsContext searchContext, SearchInteractionsContext backgroundSearchContext,
																					TextElementType textElementType, int hierarchyLevelNumber, List<TextElementsFacetNode> textElements,
																					List<TextElementMetricType> metricsToCalc, TextElementMetricType orderMetric,
																					SpeakerQueryType speakerType, boolean sameUtteranceMode, boolean leavesOnly, int rootLevelLimit, int descendantsLimit) {
	//@formatter:on

		List<TextElementsFacetNode> textElementsNodesTree = null;
		List<TextElementsFacetNode> textElementsBackgroundNodes = null;


		List<Callable<Object>> textElementsTasks = null;

		// check if Avg. Sentiment metric was requested
		// @formatter:off
		Optional<TextElementMetricType> sentimentMetricRequired = metricsToCalc.stream()
		                                                               .filter(m -> m.equals(TextElementMetricType.AvgSentiment))
		                                                               .findFirst();

		// check if Correlation Percentage metric was requested both for Regular metrics and Backgound correlation
		Optional<TextElementMetricType> correlationMetricRequited = metricsToCalc.stream()
		                                                                 .filter(m -> m == TextElementMetricType.CorrelationPercentage)
		                                                                 .findFirst();
		// @formatter:on

		String requestId = ThreadContext.get(TAConstants.requestId);
		String language = configurationService.getChannelLanguage(tenant, channel);

		// build tasks for Executor service to retrive first level text elements, second level elements and sentiment if required
		textElementsTasks = this.generateTextElementsStatsTasks(tenant, channel, searchContext, language, textElementType, hierarchyLevelNumber,
		                                                        metricsToCalc, orderMetric, speakerType, sameUtteranceMode, leavesOnly, rootLevelLimit,
		                                                        requestId, sentimentMetricRequired.isPresent());

		// invoke text elements stats retrieval tasks
		Pair<?, ?> textElementsStatsResult = this.runTextElementsStatsTask(textElementsTasks);

		List<TextElementsFacetNode> facetNodes = (List<TextElementsFacetNode>) textElementsStatsResult.getLeft();
		List<TextElementSentimentsMetric> textElementsSentiments = (List<TextElementSentimentsMetric>) textElementsStatsResult.getRight();

		// generate tree of Text Elements for Non-Leaves mode
		if (!leavesOnly) {
			textElementsNodesTree = this.generateTexElementsFacetTree(facetNodes);
		} else {
			// just use the nodes for Leaves only mode
			textElementsNodesTree = facetNodes;
		}

		if (sentimentMetricRequired.isPresent()) {
			this.updateTextElementsSentiment(textElementsNodesTree, textElementsSentiments);
		}

		if (correlationMetricRequited.isPresent()) {
			textElementsBackgroundNodes = this.getTextElementsFacetCorrelationToBackgound(tenant, channel, backgroundSearchContext, language, textElementType, textElementsNodesTree, speakerType,
			                                                                                sameUtteranceMode, leavesOnly);
		}


		return new ImmutablePair<>(textElementsNodesTree, textElementsBackgroundNodes);
	}

	/**
	 * Generates tasks for Hierararchy top level text elements and thier sentiment (if required), seconds level top M text elements and thier sentiment if required.
	 */
	private List<Callable<Object>> generateTextElementsStatsTasks(String tenant, String channel, SearchInteractionsContext searchContext, String language, TextElementType textElementType,
																int hierarchyLevelNumber, List<TextElementMetricType> metricsToCalc, TextElementMetricType orderMetric,
																SpeakerQueryType speakerType, boolean sameUtteranceMode, boolean leavesOnly, int rootLevelLimit, String requestId, boolean sentimentRequested) {
		int leafsNodesLimit;
		ApplicationConfiguration appConfig = this.configurationManager.getApplicationConfiguration();

		// create array of tasks
		List<Callable<Object>> tasks = new ArrayList<>();

		TextElementMetricType sentimentOrderField = null;
		if (sentimentRequested) {
			// Calcute metric to order by
			Optional<TextElementMetricType> orderMetricOpt = metricsToCalc.stream().filter(m -> !m.equals(TextElementMetricType.AvgSentiment)).findFirst();
			if (orderMetricOpt.isPresent()) {
				TextElementMetricType sentimentOderMetric = orderMetricOpt.get();
				if (!sentimentOderMetric.equals(TextElementMetricType.Volume) && !sentimentOderMetric.equals(TextElementMetricType.CorrelationPercentage)) {
					sentimentOrderField = sentimentOderMetric;
				}
			}
		}

		final TextElementMetricType orderSentimentBy = sentimentOrderField;

		if (!leavesOnly) {
				// Tasks to retrieve first N text elements from top level
				tasks.add(() -> {
					ThreadContext.put(TAConstants.requestId, requestId);

					return new ImmutablePair<>(TextElementsFacet.METRICS_THREAD,
					                           // retrieve first level of text elements : with prefix "1"
					                           textAnalyticsProvider.getTextElementsFacet(tenant, channel, searchContext, language, textElementType, String.valueOf(hierarchyLevelNumber),
					                                                                      metricsToCalc, orderMetric, speakerType, sameUtteranceMode, leavesOnly, rootLevelLimit));
				});

				leafsNodesLimit = appConfig.getTextElementsFacetTopLeafsJoinLimit();

				// Task to retrieve top M leafs from "topics"/"relations"
				tasks.add(() -> {
					ThreadContext.put(TAConstants.requestId, requestId);

					return new ImmutablePair<>(TextElementsFacet.LEAFS_THREAD,
					                           // retrieve top N leafs ordered by the same metric as parent
					                           textAnalyticsProvider.getTextElementsFacet(tenant, channel, searchContext, language, textElementType, null,
					                                                                      metricsToCalc, orderMetric, speakerType, sameUtteranceMode, true, leafsNodesLimit));

				});

				if (sentimentRequested) {
					// Tasks for Sentiment of first level Text Elements
					tasks.add(() -> {
						// Request Sentiment of top level text elements, those with "1/" prefix
						return new ImmutablePair<>(TextElementsFacet.SENTIMENT1_THREAD,
						                           textAnalyticsProvider.getTextElementsChildrenSentiment(tenant, channel, searchContext, language, textElementType, String.valueOf(hierarchyLevelNumber),
						                                                                                  orderSentimentBy, rootLevelLimit));
					});

					// Tasks for Sentiment of seconf level text elements ordered by the same metric as text elements in stats request
					tasks.add(() -> {
						// Request Sentiment of top level text elements, those with "1/" prefix
						return new ImmutablePair<>(TextElementsFacet.SENTIMENT2_THREAD,
						                           textAnalyticsProvider.getTextElementsChildrenSentiment(tenant, channel, searchContext, language, textElementType, String.valueOf(hierarchyLevelNumber + 1),
						                                                                                  orderSentimentBy, leafsNodesLimit));
					});
				}
			} else {
				leafsNodesLimit = rootLevelLimit;

				// Task to retrieve top M leafs from "topics"/"relations"
				tasks.add(() -> {
					ThreadContext.put(TAConstants.requestId, requestId);

					return new ImmutablePair<>(TextElementsFacet.METRICS_THREAD,
					                           // retrieve top N leafs ordered by the same metric as parent
					                           textAnalyticsProvider.getTextElementsFacet(tenant, channel, searchContext, language, textElementType, null,
					                                                                      metricsToCalc, orderMetric, speakerType, sameUtteranceMode, true, rootLevelLimit));

				});

				// if senntiment requested, invoke request for leaves
				if (sentimentRequested) {
					// Tasks for Sentiment of first level Text Elements
					tasks.add(() -> {
						// Request Sentiment of top level text elements, those with "1/" prefix
						return new ImmutablePair<>(TextElementsFacet.SENTIMENT1_THREAD,
						                           textAnalyticsProvider.getTextElementsChildrenSentiment(tenant, channel, searchContext, language, textElementType, String.valueOf(hierarchyLevelNumber + 1),
						                                                                                  orderSentimentBy, rootLevelLimit));
					});
				}
			}

		return tasks;
	}

	private Pair<?, ?> runTextElementsStatsTask(List<Callable<Object>> tasks) {
		ExecutorService threadPool = null;
		List<TextElementsFacetNode> topLevelNodes = null;
		List<TextElementsFacetNode> leafsNodes = null;
		List<TextElementsFacetNode> facetNodes = null;
		List<TextElementSentimentsMetric> textElementsSentiments = null;

		MutablePair<List<TextElementsFacetNode>, List<TextElementSentimentsMetric>> result = new MutablePair<>();

		try {
			ApplicationConfiguration appConfig = this.configurationManager.getApplicationConfiguration();

			if (!CollectionUtils.isEmpty(tasks)) {
				threadPool = Executors.newFixedThreadPool(tasks.size());

				// allocate facet nodes array
				facetNodes = new ArrayList<>();
				textElementsSentiments = new ArrayList<>();

				logger.debug("Invoking request using {} parallel tasks for Text Elements Metrics. Wait timeout - {} seconds", tasks.size(), appConfig.getDarwinRestRequestTimeout());

				// invokeAll() returns when all tasks are complete
				List<Future<Object>> lstFutures = threadPool.invokeAll(tasks, appConfig.getDarwinRestRequestTimeout(), TimeUnit.SECONDS);

				if (lstFutures != null) {
					for (Future<?> future : lstFutures) {
						if (future.isDone()) {
							// get tasks result of task is done
							val taskResult = (Pair<String, List<?>>) future.get();
							if (taskResult != null) {
								switch (taskResult.getLeft()) {
									case TextElementsFacet.METRICS_THREAD:
										// get top level facet nodes
										if (taskResult.getRight() != null) {
											topLevelNodes = (List<TextElementsFacetNode>) taskResult.getRight();

											facetNodes.addAll(topLevelNodes);
										}
										break;

									case TextElementsFacet.LEAFS_THREAD:
										// get leafs nodes
										if (taskResult.getRight() != null) {
											leafsNodes = (List<TextElementsFacetNode>) taskResult.getRight();

											facetNodes.addAll(leafsNodes);
										}
										break;
									case TextElementsFacet.SENTIMENT1_THREAD:
									case TextElementsFacet.SENTIMENT2_THREAD:
										// get top level facet nodes
										if (taskResult.getRight() != null) {
											List<TextElementSentimentsMetric> sentiments = (List<TextElementSentimentsMetric>) taskResult.getRight();

											textElementsSentiments.addAll(sentiments);
										}
										break;
									default:
										break;
								}
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex.getCause(), TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
		} finally {
			ThreadUtils.shutdownExecutionThreadPool(threadPool);
		}

		result.setLeft(facetNodes);
		result.setRight(textElementsSentiments);

		return result;
	}


	/**
	 * Updates Text Elements Sentiment.
	 *
	 * @param textElements          text elements
	 * @param textElementsSentiment text elements facet.
	 */
	private void updateTextElementsSentiment(List<TextElementsFacetNode> textElements, List<TextElementSentimentsMetric> textElementsSentiment) {
		Map<String, Double> sentimentMap = new HashMap<>();

		if (!CollectionUtils.isEmpty(textElementsSentiment) && !CollectionUtils.isEmpty(textElements)) {
			textElementsSentiment.stream().forEach(s -> sentimentMap.put(s.getTextElement(), s.getSentimentAvg()));

			// @formatter:off
			textElements.parallelStream()
			            .forEach(te -> {

							// if Sentiment for this Text Element is in Map
				            Double sentiment = sentimentMap.get(te.getValue());
							if (sentiment != null) {
								te.addMetric(new MetricData(TextElementMetricType.AvgSentiment.name(), sentiment));
							} else {
								logger.warn("Sentiment for Text Element with value {} was not found", te.getValue());
							}

				            if (!CollectionUtils.isEmpty(te.getChildren())) {

					            for (TextElementsFacetNode childNode : te.getChildren()) {
						            sentiment = sentimentMap.get(childNode.getValue());
						            if (sentiment != null) {
										childNode.addMetric(new MetricData(TextElementMetricType.AvgSentiment.name(), sentiment));
									} else {
										logger.warn("Sentiment for Text Element with value {} was not found", childNode.getValue());
									}
					            }
				            }
						});
			// @formatter:on
		}
	}


	private List<TextElementsFacetNode> getTextElementsFacetCorrelationToBackgound(String tenant, String channel, SearchInteractionsContext backgroundSearchContext, String language,
																					TextElementType textElementType, List<TextElementsFacetNode> textElementsTree, SpeakerQueryType speakerType,
																					Boolean sameUtteranceMode, Boolean leavesOnly) {

		List<TextElementsFacetNode> textElementsBackgroundNodes = null;
		ExecutorService correlationsTasksThreadPool = null;
		List<Callable<Object>> correlationsTasks = null;

		if (!CollectionUtils.isEmpty(textElementsTree)) {
			try {
				ApplicationConfiguration appConfig = this.configurationManager.getApplicationConfiguration();

				correlationsTasks = this.getTextElementsFacetCorrelationToBackgroundTasks(tenant, channel, backgroundSearchContext, language, textElementType, textElementsTree,
																							speakerType, sameUtteranceMode, leavesOnly);

				if (!CollectionUtils.isEmpty(correlationsTasks)) {
					correlationsTasksThreadPool = Executors.newFixedThreadPool(correlationsTasks.size());

					// invokeAll() returns when all tasks are complete
					List<Future<Object>> lstFutures = correlationsTasksThreadPool.invokeAll(correlationsTasks, appConfig.getDarwinRestRequestTimeout(), TimeUnit.SECONDS);

					if (lstFutures != null) {
						for (Future<?> future : lstFutures) {
							// get tasks result of task is done
							if (future.isDone()) {
								// get tasks result of task is done
								val taskResult = (Pair<String, List<?>>) future.get();
								if (taskResult != null) {
									switch (taskResult.getLeft()) {
										case TextElementsFacet.METRICS_BACKGROUND_THREAD:
											if (!CollectionUtils.isEmpty(taskResult.getRight())) {
												// add second level nodes to first level
												if (textElementsBackgroundNodes == null) {
													textElementsBackgroundNodes = new ArrayList<>();
												}

												textElementsBackgroundNodes.addAll((List<TextElementsFacetNode>) taskResult.getRight());
											}
											break;
										default:
											break;
									}
								}
							}
						}
					}
				}
			} catch (Exception ex) {
				Throwables.propagateIfInstanceOf(ex.getCause(), TextQueryExecutionException.class);
				Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
			} finally {
				ThreadUtils.shutdownExecutionThreadPool(correlationsTasksThreadPool);
			}
		}

		return textElementsBackgroundNodes;
	}

	/**
	 * Calculate Background Context Metrics of text elements.
	 */
	private List<Callable<Object>> getTextElementsFacetCorrelationToBackgroundTasks(String tenant, String channel, SearchInteractionsContext backgroundSearchContext, String language,
																				    TextElementType textElementType, List<TextElementsFacetNode> textElementsTree, SpeakerQueryType speakerType,
																					Boolean sameUtteranceMode, Boolean leavesOnly) {

		ApplicationConfiguration appConfig = this.configurationManager.getApplicationConfiguration();
		val tasks = new ArrayList<Callable<Object>>();

		// Calculate volume only for Background
		List<TextElementMetricType> backgroundMetricsToCalc = Arrays.asList(TextElementMetricType.Volume);

		if (!CollectionUtils.isEmpty(textElementsTree)) {
			if (!leavesOnly) {
				// for non-Leaves only mode, retrieve backgound volume for first and second level
				// of text elements
				List<List<TextElementsFacetNode>> topLevelNodesGroups = CollectionUtils.partition(textElementsTree, appConfig.getTextElementsFacetTopLeafsGroupSizeForSentimentQuery());

				for (List<TextElementsFacetNode> topLevelNodesGroup : topLevelNodesGroups) {
					tasks.add(() -> {
						// Retrive First level Text Elements by invoking Query Facets
						return new ImmutablePair<>(TextElementsFacet.METRICS_BACKGROUND_THREAD,
						                           textAnalyticsProvider.getTextElementsMetrics(tenant, channel, backgroundSearchContext, language, textElementType,
						                                                                        topLevelNodesGroup, backgroundMetricsToCalc, leavesOnly));
					});
				}

				// Collect children from second level of elements
				// @formatter:off
				List<TextElementsFacetNode> childNodes = textElementsTree.stream()
			                                                         .flatMap(te ->  te.getChildren() != null ? te.getChildren().stream() : Stream.empty())
			                                                         .collect(toList());
				// @formatter:on

				if (!CollectionUtils.isEmpty(childNodes)) {
					List<List<TextElementsFacetNode>> childNodesGroups = CollectionUtils.partition(childNodes, appConfig.getTextElementsFacetTopLeafsGroupSizeForSentimentQuery());

					for (List<TextElementsFacetNode> childNodesGroup : childNodesGroups) {
						tasks.add(() -> {
							// Retrive seconds level Text Element of First level
							return new ImmutablePair<>(TextElementsFacet.METRICS_BACKGROUND_THREAD,
							                           textAnalyticsProvider.getTextElementsMetrics(tenant, channel, backgroundSearchContext, language, textElementType,
							                                                                        childNodesGroup, backgroundMetricsToCalc, true));
						});
					}
				}

			} else {

				// for Leaves only mode, retrieve backgound volume all leaves text elements
				// by invoking requests for chunk all leaves
				List<List<TextElementsFacetNode>> leavesGroups = CollectionUtils.partition(textElementsTree, appConfig.getTextElementsFacetQueryGroupSize());

				for (List<TextElementsFacetNode> leavesGroup : leavesGroups) {
					tasks.add(() -> {
						// Retrive First level Text Elements by invoking Query Facets
						return new ImmutablePair<>(TextElementsFacet.METRICS_BACKGROUND_THREAD,
						                           textAnalyticsProvider.getTextElementsMetrics(tenant, channel, backgroundSearchContext, language, textElementType, leavesGroup,
						                                                                        backgroundMetricsToCalc, leavesOnly));
					});
				}
			}
		}

		return tasks;
	}
}
