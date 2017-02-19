package com.verint.textanalytics.bl.applicationservices.facet.textelements;

import com.google.common.base.Throwables;
import com.verint.textanalytics.bl.applicationservices.ConfigurationService;
import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
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
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by EZlotnik on 5/31/2016.
 */
public class FacetOnChildrenImpl extends TextElementsFacet {

	@Autowired
	protected ConfigurationManager configurationManager;

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
		List<TextElementSentimentsMetric> textElementsSentiment = null;
		Boolean sentimentRequested = false;

		String language = configurationService.getChannelLanguage(tenant, channel);

		// retrieve first level of text elements : with prefix "1"
		List<TextElementsFacetNode> facetNodes = textAnalyticsProvider.getTextElementsFacet(tenant, channel, searchContext, language, textElementType, String.valueOf(hierarchyLevelNumber),
		                                                                                    metricsToCalc, orderMetric, speakerType, sameUtteranceMode, leavesOnly,
		                                                                                    rootLevelLimit);

		if (!CollectionUtils.isEmpty(facetNodes)) {

			val tasks = new ArrayList<Callable<Object>>();
			ExecutorService threadPool = null;

			try {

				ApplicationConfiguration appConfig = configurationManager.getApplicationConfiguration();

				// Add request for second level for Non-Leaves mode : prefix "2/Device/"  for example ...
				if (!leavesOnly) {
					List<List<TextElementsFacetNode>> facetNodesGroups = CollectionUtils.partition(facetNodes, appConfig.getTextElementsFacetQueryGroupSize());

					// Generate Task for Second level of Text Elements
					for (List<TextElementsFacetNode> facetNodesGroup : facetNodesGroups) {
						tasks.add(() -> {
							return new ImmutablePair<>(TextElementsFacet.METRICS_NESTED_LEVEL_THREAD,
							                           textAnalyticsProvider.getTextElementsChildrenMetrics(tenant, channel, searchContext, language, textElementType, facetNodesGroup,
							                                                                                metricsToCalc, orderMetric, speakerType, sameUtteranceMode, leavesOnly, descendantsLimit));
						});
					}
				}

				// Create tasks for calculations for Correlation Percentage if correlation metric required
				List<Callable<Object>> correlationTasks = this.getTextElementsFacetCorrelationToBackgroundTasks(tenant, channel, backgroundSearchContext, textElementType,
				                                                                                                facetNodes, metricsToCalc,
				                                                                                                speakerType, sameUtteranceMode, leavesOnly);
				if (!CollectionUtils.isEmpty(correlationTasks)) {
					tasks.addAll(correlationTasks);
				}

				// Create tasks for calculation of Sentiment if sentiment metric if require
				List<Callable<Object>> sentimentTasks = this.getTextElementsSentimentTasks(tenant, channel, searchContext, textElementType, hierarchyLevelNumber, facetNodes,
				                                                                           metricsToCalc,
				                                                                           leavesOnly, rootLevelLimit, descendantsLimit);
				if (!CollectionUtils.isEmpty(sentimentTasks)) {
					tasks.addAll(sentimentTasks);
					sentimentRequested = true;
				}


				if (!CollectionUtils.isEmpty(tasks)) {
					threadPool = Executors.newFixedThreadPool(tasks.size());

					// invokeAll() returns when all tasks are complete
					List<Future<Object>> lstFutures = threadPool.invokeAll(tasks, appConfig.getDarwinRestRequestTimeout(), TimeUnit.SECONDS);

					if (lstFutures != null) {

						for (Future<?> future : lstFutures) {
							if (future.isDone()) {
								// get tasks result of task is done
								val taskResult = (Pair<String, List<?>>) future.get();
								if (taskResult != null) {
									switch (taskResult.getLeft()) {
										case TextElementsFacet.METRICS_NESTED_LEVEL_THREAD:
											// add second level Text Elements nodes into the list
											if (!CollectionUtils.isEmpty(taskResult.getRight())) {
												// add second level nodes to first level
												facetNodes.addAll((List<TextElementsFacetNode>) taskResult.getRight());
											}
											break;

										case TextElementsFacet.METRICS_BACKGROUND_THREAD:
											if (!CollectionUtils.isEmpty(taskResult.getRight())) {
												// add second level nodes to first level
												if (textElementsBackgroundNodes == null) {
													textElementsBackgroundNodes = new ArrayList<>();
												}

												textElementsBackgroundNodes.addAll((List<TextElementsFacetNode>) taskResult.getRight());
											}
											break;
										case TextElementsFacet.SENTIMENT1_THREAD:
											if (!CollectionUtils.isEmpty(taskResult.getRight())) {
												if (textElementsSentiment == null) {
													textElementsSentiment = new ArrayList<>();
												}

												// add Sentiment for Text Elements from first level
												textElementsSentiment.addAll((List<TextElementSentimentsMetric>) taskResult.getRight());
											}
											break;
										case TextElementsFacet.SENTIMENT2_THREAD:
											if (!CollectionUtils.isEmpty(taskResult.getRight())) {
												if (textElementsSentiment == null) {
													textElementsSentiment = new ArrayList<>();
												}

												// add Sentiment of Text Elements from seond level
												textElementsSentiment.addAll((List<TextElementSentimentsMetric>) taskResult.getRight());
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
				Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
			} finally {
				ThreadUtils.shutdownExecutionThreadPool(threadPool);
			}
		}

		// generate tree of Text Elements for Non-Leaves mode
		if (!leavesOnly) {
			textElementsNodesTree = this.generateTexElementsFacetTree(facetNodes);
		} else {
			// just use the nodes for Leaves only mode
			textElementsNodesTree = facetNodes;
		}

		// update Sentiment if was requested
		if (sentimentRequested) {
			this.updateTextElementsSentiment(facetNodes, textElementsSentiment);
		}


		return new ImmutablePair<>(textElementsNodesTree, textElementsBackgroundNodes);
	}

	private List<Callable<Object>> getTextElementsSentimentTasks(String tenant, String channel, SearchInteractionsContext searchContext,
																TextElementType textElementType, int hierarchyLevelNumber,  List<TextElementsFacetNode> textElements, List<TextElementMetricType> metricsToCalc,
																Boolean leavesOnly, int topLevelElementsLimit, int descendentsElementsLimit) {

		val tasks = new ArrayList<Callable<Object>>();

		// check if Correlation Percentage metric was requested both for Regular metrics and Backgound correlation
		Optional<TextElementMetricType> sentimentMetric = metricsToCalc.stream().filter(m -> m.equals(TextElementMetricType.AvgSentiment)).findFirst();
		if (sentimentMetric.isPresent()) {

			if (!CollectionUtils.isEmpty(textElements)) {

				// Calcute metric to order by
				TextElementMetricType orderField = null;
				Optional<TextElementMetricType> orderMetricOpt = metricsToCalc.stream().filter(m -> !m.equals(TextElementMetricType.AvgSentiment)).findFirst();
				if (orderMetricOpt.isPresent()) {
					TextElementMetricType orderMetric = orderMetricOpt.get();
					if (!orderMetric.equals(TextElementMetricType.Volume) && !orderMetric.equals(TextElementMetricType.CorrelationPercentage)) {
						orderField = orderMetric;
					}
				}

				String language = configurationService.getChannelLanguage(tenant, channel);

				if (!leavesOnly) {
					try {
						final TextElementMetricType orderSentimentBy = orderField;

						tasks.add(() -> {
							// Request Sentiment of first level text elements
							return new ImmutablePair<>(TextElementsFacet.SENTIMENT1_THREAD,
							                           textAnalyticsProvider.getTextElementsChildrenSentiment(tenant, channel, searchContext, language, textElementType,
							                                                                                    "", orderSentimentBy, topLevelElementsLimit));
						});


						// Request for second level of elements
						tasks.add(() -> {
							// Retrive seconds level Text Element of First level
							return new ImmutablePair<>(TextElementsFacet.SENTIMENT1_THREAD,
							                           textAnalyticsProvider.getTextElementsChildrenSentiment(tenant, channel, searchContext, language, textElementType,
							                                                                                     textElements, orderSentimentBy, descendentsElementsLimit));
						});

					} catch (Exception ex) {
						Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
					}
				} else {

					// for Leaves only mode, retrieve sentiment for all text elements
					tasks.add(() -> {
						// Retrive First level Text Elements by invoking Query Facets
						return new ImmutablePair<>(TextElementsFacet.SENTIMENT1_THREAD,
						                           textAnalyticsProvider.getTextElementsSentiment(tenant, channel, searchContext, language, textElementType, textElements, topLevelElementsLimit));
					});

				}
			}
		}

		return tasks;
	}


	/**
	 * Calculate Background Context Metrics of text elements.
	 */
	private List<Callable<Object>> getTextElementsFacetCorrelationToBackgroundTasks(String tenant, String channel, SearchInteractionsContext backgroundSearchContext,
																				    TextElementType textElementType, List<TextElementsFacetNode> textElements, List<TextElementMetricType> metricsToCalc,
																					SpeakerQueryType speakerType, Boolean sameUtteranceMode, Boolean leavesOnly) {

		ApplicationConfiguration appConfig = this.configurationManager.getApplicationConfiguration();
		val tasks = new ArrayList<Callable<Object>>();

		// check if Correlation Percentage metric was requested both for Regular metrics and Backgound correlation
		Optional<TextElementMetricType> correlationMetric = metricsToCalc.stream().filter(m -> m == TextElementMetricType.CorrelationPercentage).findFirst();
		if (correlationMetric.isPresent()) {

			// Calculate volume only for Background
			List<TextElementMetricType> backgroundMetricsToCalc = Arrays.asList(TextElementMetricType.Volume);

			String language = configurationService.getChannelLanguage(tenant, channel);

			if (!CollectionUtils.isEmpty(textElements)) {
				if (!leavesOnly) {
					// for non-Leaves only mode, retrieve backgound volume for first and second level
					// of text elements
					List<List<TextElementsFacetNode>> facetNodesGroups = CollectionUtils.partition(textElements, appConfig.getTextElementsFacetQueryGroupSize());

					try {

						for (List<TextElementsFacetNode> facetNodesGroup : facetNodesGroups) {
							tasks.add(() -> {
								// Retrive First level Text Elements by invoking Query Facets
								return new ImmutablePair<>(TextElementsFacet.METRICS_BACKGROUND_THREAD,
								                           textAnalyticsProvider.getTextElementsMetrics(tenant, channel, backgroundSearchContext, language, textElementType,
								                                                                        facetNodesGroup, backgroundMetricsToCalc, leavesOnly));
							});

							tasks.add(() -> {
								// Retrive seconds level Text Element of First level
								return new ImmutablePair<>(TextElementsFacet.METRICS_BACKGROUND_THREAD,
								                           textAnalyticsProvider.getTextElementsChildrenMetrics(tenant, channel, backgroundSearchContext, language, textElementType,
								                                                                                facetNodesGroup, backgroundMetricsToCalc, null, speakerType, sameUtteranceMode, false, -1));
							});
						}
					} catch (Exception ex) {
						Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
					}
				} else {

					// for Leaves only mode, retrieve backgound volume all leaves text elements
					// by invoking requests for chunk all leaves
					List<List<TextElementsFacetNode>> leavesGroups = CollectionUtils.partition(textElements, appConfig.getTextElementsFacetQueryGroupSize());

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
		}

		return tasks;
	}

	/**
	 * Updates Text Elements Sentiment.
	 * @param textElements text elements
	 * @param textElementsSentiment text elements facet.
	 */
	private void updateTextElementsSentiment(List<TextElementsFacetNode> textElements, List<TextElementSentimentsMetric> textElementsSentiment) {
		Map<String, Double> sentimentMap = new HashMap<>();

		if (!CollectionUtils.isEmpty(textElementsSentiment) && !CollectionUtils.isEmpty(textElements)) {
			textElementsSentiment.stream().forEach(s -> sentimentMap.put(s.getTextElement(), s.getSentimentAvg()));

			textElements.stream().forEach(te -> {

				// if Sentiment for this Text Element is in Map
				if (sentimentMap.containsKey(te.getValue())) {
					te.addMetric(new MetricData(TextElementMetricType.AvgSentiment.name(), sentimentMap.get(te.getValue())));
				} else {
					logger.warn("Sentiment for Text Element with value {} was not found", te.getValue());
				}
			});
		}
	}
}
