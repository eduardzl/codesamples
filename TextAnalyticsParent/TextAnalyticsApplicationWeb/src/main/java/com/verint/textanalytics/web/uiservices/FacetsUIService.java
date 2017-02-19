package com.verint.textanalytics.web.uiservices;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.verint.textanalytics.bl.applicationservices.FacetsService;
import com.verint.textanalytics.bl.applicationservices.facet.textelements.TextElementsFacetService;
import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionErrorCode;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionException;
import com.verint.textanalytics.common.utils.CollectionUtils;
import com.verint.textanalytics.common.utils.ThreadUtils;
import com.verint.textanalytics.model.facets.*;
import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import com.verint.textanalytics.web.viewmodel.*;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import propel.core.functional.tuples.Pair;

import java.util.*;
import java.util.concurrent.*;

/**
 * UI Facets service.
 *
 * @author EZlotnik
 */

/**
 * @author YHemi
 */
public class FacetsUIService extends BaseUIService {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	private ViewModelConverter viewModelConverter;

	@Autowired
	private FacetsService facetsService;

	@Autowired
	private TextElementsFacetService textElementsFacet;

	@Autowired
	private ViewModelFilter viewModelFilter;

	@Autowired
	private ConfigurationManager configurationManager;

	private Map<String, String> metricsToConvertAll;

	/**
	 * C'tor.
	 */
	public FacetsUIService() {
		//@formatter:off
		metricsToConvertAll = new HashMap<>();
		metricsToConvertAll.put(TextElementMetricType.AvgHandleTime.name(), TextElementMetricType.AvgHandleTime.name());
		metricsToConvertAll.put(TextElementMetricType.AvgMessagesCount.name(), TextElementMetricType.AvgMessagesCount.name());
		metricsToConvertAll.put(TextElementMetricType.AvgEmployeesMessages.name(), TextElementMetricType.AvgEmployeesMessages.name());
		metricsToConvertAll.put(TextElementMetricType.AvgCustomerMessages.name(), TextElementMetricType.AvgCustomerMessages.name());
		metricsToConvertAll.put(TextElementMetricType.AvgEmployeeResponseTime.name(), TextElementMetricType.AvgEmployeeResponseTime.name());
		metricsToConvertAll.put(TextElementMetricType.AvgCustomerResponseTime.name(), TextElementMetricType.AvgCustomerResponseTime.name());
		metricsToConvertAll.put(TextElementMetricType.CorrelationPercentage.name(), TextElementMetricType.CorrelationPercentage.name());
		metricsToConvertAll.put(TextElementMetricType.AvgSentiment.name(), TextElementMetricType.AvgSentiment.name());
		//@formatter:on
	}

	/**
	 * Generates entities facet from search result.
	 *
	 * @param i360FoundationToken   foundation token
	 * @param channel               channel of data to search in
	 * @param searchContext         search context : filter fields and search terms
	 * @param backgroundContext     backgroundContext
	 * @param textElementType       textElement
	 * @param hierarchyLevelNumber  level number to retrieve data for (1 - first level, 2 - seconds level ...)
	 * @param textElements          text elements to retrive data for
	 * @param sizeByMetric          size metric
	 * @param colorByMetric         color metric
	 * @param querySpeaker          speaker
	 * @param sameUtteranceMode     same utterance mode
	 * @param leavesOnly            should leaves only be produced
	 * @param topLevelLimit         limit on top level facet
	 * @param descendantsLevelLimit limit on descendand level facet
	 * @return text elements facet nodes
	 */
	public TextElementFacetTreeMapResult getTextElementsFacetTreeMap(String i360FoundationToken, String channel, SearchInteractionsContext searchContext, SearchInteractionsContext backgroundContext, TextElementType textElementType, int hierarchyLevelNumber, List<TextElementsFacetNode> textElements, TextElementMetricType sizeByMetric, TextElementMetricType colorByMetric, SpeakerQueryType querySpeaker, boolean sameUtteranceMode, boolean leavesOnly, Integer topLevelLimit, Integer descendantsLevelLimit) {

		TextElementFacetTreeMapResult res = new TextElementFacetTreeMapResult();
		List<TextElementFacetTreeMapNode> lstTextElementsNodes = null;
		TextElementFacetTreeMapNode textElementConverted;

		MetricsLimits metricsLimits = new MetricsLimits();

		val userTenant = this.getTenantFromChannel(channel, i360FoundationToken);

		if (topLevelLimit == null && descendantsLevelLimit == null) {
			ApplicationConfiguration appConfig = configurationManager.getApplicationConfiguration();
			switch (textElementType) {
				case Entities:
					topLevelLimit = leavesOnly ? appConfig.getEntitiesFacetLeavesTopLimit() : appConfig.getEntitiesFacetRootTopLimit();
					descendantsLevelLimit = appConfig.getEntitiesFacetDescendantsTopLimit();
					break;
				case Relations:
					topLevelLimit = leavesOnly ? appConfig.getRelationsFacetLeavesTopLimit() : appConfig.getRelationsFacetRootTopLimit();
					descendantsLevelLimit = appConfig.getRelationsFacetDescendantsTopLimit();
					break;
				default:
					topLevelLimit = appConfig.getTextElementsFacetWithStatsFirstLevelLimit();
					descendantsLevelLimit = appConfig.getTextElementsFacetWithStatsFirstLevelLimit();
					break;
			}
		}

		TextElementFacetResult textElementsFacetResult = null;
		List<TextElementsFacetNode> textElementsFacetNodes;

		textElementsFacetResult = textElementsFacet.getTextElementsFacetWithStats(userTenant, channel, searchContext, backgroundContext, textElementType, hierarchyLevelNumber,
		                                                                          textElements, sizeByMetric, colorByMetric, querySpeaker, sameUtteranceMode, leavesOnly,
		                                                                          topLevelLimit, descendantsLevelLimit, TextElementsFacetCalculationType.FacetOnTopLeafs);

		textElementsFacetNodes = textElementsFacetResult.getTextElementsFacetNodeList();

		if (textElementsFacetNodes != null) {
			lstTextElementsNodes = new ArrayList<TextElementFacetTreeMapNode>();

			// prepare metrics to convert map
			Map<String, String> metricsToConvert = new HashMap<>();
			metricsToConvert.put(sizeByMetric.name(), sizeByMetric.name());
			if (!colorByMetric.equals(sizeByMetric)) {
				metricsToConvert.put(colorByMetric.name(), colorByMetric.name());
			}

			for (TextElementsFacetNode textElementsFacetNode : textElementsFacetNodes) {
				// convert Model textElement Facet Node to ViewModel textElement Facet
				// Node and adds it to list
				textElementConverted = viewModelConverter.convertToViewModelTextElementTreeMapNode(textElementsFacetNode, textElementType, metricsToConvert, metricsLimits);
				lstTextElementsNodes.add(textElementConverted);
			}
		}

		res.setTextElementFacetTreeMapNodes(lstTextElementsNodes);
		res.setMetricsLimitsData(metricsLimits.getMetricsLimitsMapByLevel());

		return res;
	}

	/**
	 * Generates entities facet from search result.
	 *
	 * @param i360FoundationToken foundation token
	 * @param channel             channel of data to search in
	 * @param searchContext       search context : filter fields and search terms
	 * @param backgroundContext   backgroundContext
	 * @param sizeByMetric        sizeByMetric
	 * @param colorByMetric       colorByMetric
	 * @param querySpeaker        speaker of the query
	 * @param sameUtteranceMode   sameUtteranceMode
	 * @param leavesOnly          leavesOnly
	 * @return list of entities facet nodes
	 */
	public TextElementFacetTreeMapResult getThemesFacetTreeMap(String i360FoundationToken, String channel, SearchInteractionsContext searchContext, SearchInteractionsContext backgroundContext, TextElementMetricType sizeByMetric, TextElementMetricType colorByMetric, SpeakerQueryType querySpeaker, boolean sameUtteranceMode, boolean leavesOnly) {

		ExecutorService threadPool = Executors.newFixedThreadPool(2);
		TextElementFacetTreeMapResult entitiesFacet = null;
		TextElementFacetTreeMapResult relationsFacet = null;
		TextElementFacetTreeMapResult res = null;

		try {
			String requestId = ThreadContext.get(TAConstants.requestId);

			val tasks = new ArrayList<Callable<Object>>();

			//add tasks to get all data after filter and tree bulding
			ApplicationConfiguration appConfig = configurationManager.getApplicationConfiguration();
			// @formatter:off
			tasks.add(() -> {
				ThreadContext.put(TAConstants.requestId, requestId);
				return new Pair<String, TextElementFacetTreeMapResult>("EntitiesFacet", getTextElementsFacetTreeMap(i360FoundationToken, channel, searchContext, backgroundContext,
				                                                                                                    TextElementType.Entities, 1, null, sizeByMetric, colorByMetric,
				                                                                                                    querySpeaker, sameUtteranceMode, leavesOnly,
				                                                                                                    leavesOnly
						                                                                                                    ? appConfig.getEntitiesFacetLeavesTopLimit() / 2
						                                                                                                    :
						                                                                                                    appConfig.getEntitiesFacetRootTopLimit(), appConfig.getEntitiesFacetDescendantsTopLimit()));
			});

			tasks.add(() -> {
				ThreadContext.put(TAConstants.requestId, requestId);
				return new Pair<String, TextElementFacetTreeMapResult>("RelationsFacet", getTextElementsFacetTreeMap(i360FoundationToken, channel, searchContext, backgroundContext,
				                                                                                                     TextElementType.Relations, 1, null, sizeByMetric,
				                                                                                                     colorByMetric, querySpeaker, sameUtteranceMode, leavesOnly,
				                                                                                                     leavesOnly
						                                                                                                     ?
						                                                                                                     appConfig.getRelationsFacetLeavesTopLimit() / 2
						                                                                                                     :
						                                                                                                     appConfig.getRelationsFacetRootTopLimit(), appConfig.getRelationsFacetDescendantsTopLimit()));
			});
		// @formatter:on
			// invokeAll() returns when all tasks are complete
			List<Future<Object>> lstFutures = threadPool.invokeAll(tasks, appConfig.getDarwinRestRequestTimeout() * 2, TimeUnit.SECONDS);

			if (lstFutures != null) {

				for (Future<?> future : lstFutures) {
					if (future.isDone()) {
						// get tasks result of task is done
						val taskResult = (Pair<String, Object>) future.get();
						if (taskResult != null) {
							switch (taskResult.getFirst()) {
								case "EntitiesFacet":
									if (taskResult.getSecond() != null) {
										entitiesFacet = (TextElementFacetTreeMapResult) taskResult.getSecond();
									}
									break;
								case "RelationsFacet":
									if (taskResult.getSecond() != null) {
										relationsFacet = (TextElementFacetTreeMapResult) taskResult.getSecond();
									}
									break;
								default:
									break;
							}
						}
					}
				}
			}

			// merge relations and entities facet
			res = this.mergeEntitiesAndRelationsFacetIntoThemesFacet(entitiesFacet, relationsFacet, sizeByMetric);

		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
		} finally {
			ThreadUtils.shutdownExecutionThreadPool(threadPool);
		}

		return res;
	}

	private TextElementFacetTreeMapResult mergeEntitiesAndRelationsFacetIntoThemesFacet(TextElementFacetTreeMapResult entitiesFacet, TextElementFacetTreeMapResult relationsFacet, TextElementMetricType orderMetric) {
		TextElementFacetTreeMapResult res;

		List<TextElementFacetTreeMapNode> mergedTextElements = null;
		Map<Integer, Collection<MetricLimitsData>> mergedMetricsLimits = new HashMap<>();

		if (relationsFacet != null && entitiesFacet != null) {

			List<TextElementFacetTreeMapNode> relations = relationsFacet.getTextElementFacetTreeMapNodes();
			List<TextElementFacetTreeMapNode> entities = entitiesFacet.getTextElementFacetTreeMapNodes();

			// check if only one type of text elements is available
			if (CollectionUtils.isEmpty(relations) && !CollectionUtils.isEmpty(entities)) {
				mergedTextElements = entities;
			} else if (!CollectionUtils.isEmpty(relations) && CollectionUtils.isEmpty(entities)) {
				mergedTextElements = relations;
			} else {
				mergedTextElements = this.mergeSortedTextElements(entities, relations, orderMetric);
			}

			val relationsMetrics = relationsFacet.getMetricsLimitsData();
			val entitiesMetrics = entitiesFacet.getMetricsLimitsData();

			if (relationsMetrics != null && entitiesMetrics != null) {
				MetricLimitsData metricLimitsDataRelations, metricLimitsDataEntities, curMetricLimit;
				Collection<MetricLimitsData> metricDataCollection;

				// we go over relations levels and metric keys becouse we assume that entitiy and relations metric is the same
				for (int i : relationsMetrics.keySet()) {

					val metricCollectionRelations = relationsMetrics.get(i).toArray();
					val metricCollectionEntities = entitiesMetrics.get(i).toArray();
					metricDataCollection = new ArrayList<>();

					for (int j = 0; j < metricCollectionRelations.length; j++) {
						metricLimitsDataRelations = (MetricLimitsData) metricCollectionRelations[j];
						metricLimitsDataEntities = (MetricLimitsData) metricCollectionEntities[j];

						curMetricLimit = new MetricLimitsData(metricLimitsDataRelations.getName());
						curMetricLimit.setMax(Math.max(metricLimitsDataEntities.getMax(), metricLimitsDataRelations.getMax()));
						curMetricLimit.setMin(Math.min(metricLimitsDataEntities.getMin(), metricLimitsDataRelations.getMin()));

						metricDataCollection.add(curMetricLimit);
					}

					mergedMetricsLimits.put(i, metricDataCollection);
				}
			}
		}

		res = new TextElementFacetTreeMapResult();
		res.setTextElementFacetTreeMapNodes(mergedTextElements);
		res.setMetricsLimitsData(mergedMetricsLimits);

		return res;
	}

	private List<TextElementFacetTreeMapNode> mergeSortedTextElements(List<TextElementFacetTreeMapNode> a, List<TextElementFacetTreeMapNode> b, TextElementMetricType orderMetric) {
		List<TextElementFacetTreeMapNode> answer = new ArrayList<>(a.size() + b.size());

		int i = 0, j = 0, k = 0;

		while (i < a.size() && j < b.size()) {
			if (a.get(i).getMetricValue(orderMetric.name()) < b.get(j).getMetricValue(orderMetric.name())) {
				answer.add(a.get(i++));
			} else {
				answer.add(b.get(j++));
			}
		}

		while (i < a.size()) {
			answer.add(a.get(i++));
		}

		while (j < b.size()) {
			answer.add(b.get(j++));
		}

		return answer;
	}

	/**
	 * Generates Text Elements tree (Left panel Topics/Relations).
	 *
	 * @param i360FoundationToken foundation token
	 * @param channel             channel of data to search in
	 * @param searchContext       search context : filter fields and search terms
	 * @param backgroundContext   backgroundContext
	 * @param textElementType     textElement type
	 * @param facetOnSpeaker      facetOnSpeaker
	 * @return list of entities facet nodes
	 */
	@SuppressWarnings("unchecked")
	public TextElementFacetTreeNode getTextElementsFacetTree(String i360FoundationToken, String channel, SearchInteractionsContext searchContext, SearchInteractionsContext backgroundContext, TextElementType textElementType, SpeakerQueryType facetOnSpeaker) {
		List<TextElementFacetTreeNode> lstTextElementsNodes = null;
		double topLevelPercentageLimit = 0, descendantsPercentageLimit = 0;

		val userTenant = this.getTenantFromChannel(channel, i360FoundationToken);

		ApplicationConfiguration appConfig = configurationManager.getApplicationConfiguration();
		int topLevelLimit = 0, descendantsLimit = 0;
		switch (textElementType) {
			case Entities:
				topLevelLimit = appConfig.getEntitiesFacetRootTopLimit();
				descendantsLimit = appConfig.getEntitiesFacetDescendantsTopLimit();
				topLevelPercentageLimit = appConfig.getEntitiesFacetRootPercentageLimit();
				descendantsPercentageLimit = appConfig.getEntitiesFacetDescendantsPercentageLimit();
				break;
			case Relations:
				topLevelLimit = appConfig.getRelationsFacetRootTopLimit();
				descendantsLimit = appConfig.getRelationsFacetDescendantsTopLimit();
				topLevelPercentageLimit = appConfig.getRelationsFacetRootPercentageLimit();
				descendantsPercentageLimit = appConfig.getRelationsFacetDescendantsPercentageLimit();
				break;
			default:
				topLevelLimit = appConfig.getTextElementsFacetWithStatsFirstLevelLimit();
				descendantsLimit = appConfig.getTextElementsFacetWithStatsFirstLevelLimit();
				break;
		}

		// retrieve the hierarchy of entities facet
		TextElementFacetResult textElementsFacetResults = textElementsFacet.getTextElementsFacet(userTenant, channel, searchContext, backgroundContext, textElementType,
		                                                                                         facetOnSpeaker, false, topLevelLimit, descendantsLimit,
		                                                                                         TextElementsFacetCalculationType.FacetOnTopLeafs);

		List<TextElementsFacetNode> textElementsFacetNodes = textElementsFacetResults.getTextElementsFacetNodeList();

		if (textElementsFacetNodes != null) {

			lstTextElementsNodes = new ArrayList<TextElementFacetTreeNode>();

			for (val textElementsFacetNode : textElementsFacetNodes) {
				// convert Model Entity Facet Node to ViewModel Entity Facet
				// node and add it to list
				TextElementFacetTreeNode n = viewModelConverter.convertToViewModelTextElementFacet(textElementsFacetNode);
				n.setBasedOnSample(textElementsFacetResults.isBasedOnSample());
				lstTextElementsNodes.add(n);
			}

			// Text Elements are sorted due to Solr sort

			if (topLevelPercentageLimit != 0 || descendantsPercentageLimit != 0) {
				viewModelFilter.applyFilterByPercentageOnTextElementFacetNode(lstTextElementsNodes, textElementType, false);
			}
		}

		TextElementFacetTreeNode facetRootNode = null;

		// create tree root node - required by ExtJs
		facetRootNode = new TextElementFacetTreeNode();
		facetRootNode.setValue(".");
		facetRootNode.setPercentage(0);
		facetRootNode.setLeaf(false);
		facetRootNode.setExpanded(true);
		facetRootNode.setIconCls(TAConstants.treeNodeNoIconCls);
		facetRootNode.setChildren(lstTextElementsNodes);
		facetRootNode.setBasedOnSample(textElementsFacetResults.isBasedOnSample());

		return facetRootNode;
	}

	/**
	 * Retrieves Text Element Metrics data.
	 *
	 * @param i360FoundationToken authentication token
	 * @param channel             channel
	 * @param searchContext       search context
	 * @param backgroundContext   background context
	 * @param textElementType     text element type : Entity, Relation..
	 * @param textElementValue    text element value : 1/Device ...
	 * @return metrics of this text element
	 */
	public TextElementFacetTreeMapNode getTextElementMetrics(String i360FoundationToken, String channel, SearchInteractionsContext searchContext, SearchInteractionsContext backgroundContext, TextElementType textElementType, String textElementValue) {
		val userTenant = this.getTenantFromChannel(channel, i360FoundationToken);

		TextElementsFacetNode textElementsFacetNode = textElementsFacet.getTextElementMetrics(userTenant, channel, searchContext, backgroundContext, textElementType,
		                                                                                      textElementValue);

		return viewModelConverter.convertToViewModelTextElementTreeMapNode(textElementsFacetNode, textElementType, this.metricsToConvertAll, null);
	}

	private FacetWeightGraphNode convertFacetPathToWeightGraph(TextElementsFacetNode facetPathNode) {

		if (facetPathNode == null) {
			return null;
		}

		FacetWeightGraphNode result = new FacetWeightGraphNode();
		result.setSum(facetPathNode.getNumberOfInteractions());
		result.setKey(facetPathNode.getName());
		result.setValue(facetPathNode.getValue());
		result.setValues(new ArrayList<FacetWeightGraphNode>());

		val children = facetPathNode.getChildren();

		if (children != null && children.size() > 0) {

			for (val node : children) {
				result.getValues().add(convertFacetPathToWeightGraph(node));
			}

		}

		return result;
	}

	/**
	 * Perform faceted Search.
	 *
	 * @param i360FoundationToken i360FoundationToken
	 * @param channel             channel
	 * @param searchContext       searchContext
	 * @param facetsQueries       facetsQueries
	 * @return List<Facet>
	 */
	public List<Facet> facetedSearch(String i360FoundationToken, String channel, SearchInteractionsContext searchContext, List<String> facetsQueries) {
		val userTenant = this.getTenantFromChannel(channel, i360FoundationToken);

		return facetsService.facetedSearch(userTenant, channel, searchContext, facetsQueries, null, null);
	}

	/**
	 * Facet sentiment.
	 *
	 * @param i360FoundationToken    i360FoundationToken
	 * @param channel                channel
	 * @param searchContext          searchContext
	 * @param sentimentFacetsQueries sentimentFacetsQueries
	 * @return List<Facet>
	 */
	public List<Facet> getSentimentFacetSearch(String i360FoundationToken, String channel, SearchInteractionsContext searchContext, List<String> sentimentFacetsQueries) {
		val userTenant = this.getTenantFromChannel(channel, i360FoundationToken);

		val listSentimentFacets = facetsService.getSentimentFacet(userTenant, channel, searchContext, sentimentFacetsQueries);

		return createFacetSentimentElements(listSentimentFacets);
	}

	/**
	 * checks if not all sentiment have result, if not we need to fabricate it.
	 * and create a sentiment with 0 count
	 *
	 * @param listSentimentFacets sentimentFieldFacetEl
	 * @return
	 */
	protected List<Facet> createFacetSentimentElements(List<Facet> listSentimentFacets) {
		Facet facetResults = new Facet();
		final String veryNegative = "-2";
		final String negative = "-1";
		final String neutral = "0";
		final String positive = "1";
		final String veryPositive = "2";

		ArrayList<String> sentimentResultArrayList = Lists.newArrayList(veryNegative, negative, neutral, positive, veryPositive);

		if (listSentimentFacets != null) {
			val listSentimentFacet = listSentimentFacets.get(0);
			for (val sentimentFacet : listSentimentFacet.getValues()) {
				val facetSentimentSingleValueResultGroup = new FacetSentimentSingleValueResultGroup();
				facetSentimentSingleValueResultGroup.setTitle(sentimentFacet.getTitle());
				facetSentimentSingleValueResultGroup.setTitleKey(localizeSentimentFacet(sentimentFacet.getTitle()));
				facetSentimentSingleValueResultGroup.setCount(sentimentFacet.getCount());
				facetSentimentSingleValueResultGroup.setValue(sentimentFacet.getTitle());
				facetSentimentSingleValueResultGroup.setSentimentValue(Integer.parseInt(sentimentFacet.getTitle()));
				facetResults.addGroupValue(facetSentimentSingleValueResultGroup);
				sentimentResultArrayList.remove(sentimentFacet.getTitle());
			}

			if (sentimentResultArrayList.size() != 5 || sentimentResultArrayList.size() == 5) {

				for (int i = 0; i < sentimentResultArrayList.size(); i++) {
					val facetSentimentSingleValueResultGroup = new FacetSentimentSingleValueResultGroup();
					facetSentimentSingleValueResultGroup.setTitle(sentimentResultArrayList.get(i).toString());
					facetSentimentSingleValueResultGroup.setTitleKey(localizeSentimentFacet(sentimentResultArrayList.get(i).toString()));
					facetSentimentSingleValueResultGroup.setCount(0);
					facetSentimentSingleValueResultGroup.setValue(sentimentResultArrayList.get(i).toString());
					facetSentimentSingleValueResultGroup.setSentimentValue(Integer.parseInt(sentimentResultArrayList.get(i)));
					facetResults.addGroupValue(facetSentimentSingleValueResultGroup);
				}
			}

			facetResults.setFieldName(listSentimentFacet.getFieldName());
			facetResults.setType(listSentimentFacet.getType());
			facetResults.setValuesDataType(listSentimentFacet.getValuesDataType());
			facetResults.sortBySentimentValue();
			listSentimentFacets.remove(0);
			listSentimentFacets.add(facetResults);
		}

		return listSentimentFacets;
	}

	/**
	 * Replace the title key with a localization key.
	 *
	 * @param titleKey titleKey
	 * @return String key
	 */
	private String localizeSentimentFacet(String titleKey) {
		switch (titleKey) {
			case "-2":
				return "DocumentSentiment_VeryNegative";
			case "-1":
				return "DocumentSentiment_Negative";
			case "0":
				return "DocumentSentiment_Neutral";
			case "1":
				return "DocumentSentiment_Positive";
			case "2":
				return "DocumentSentiment_VeryPositive";
			default:
				break;
		}

		return titleKey;
	}
}
