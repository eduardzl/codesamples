package com.verint.textanalytics.dal.darwin;

import com.codahale.metrics.Timer;
import com.google.common.base.Throwables;
import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.diagnostics.OperationType;
import com.verint.textanalytics.common.diagnostics.PerformanceMetrics;
import com.verint.textanalytics.common.exceptions.HttpExecutionException;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionErrorCode;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionException;
import com.verint.textanalytics.common.utils.CollectionUtils;
import com.verint.textanalytics.common.utils.JSONUtils;
import com.verint.textanalytics.common.utils.StringUtils;
import com.verint.textanalytics.common.utils.ThreadUtils;
import com.verint.textanalytics.dal.darwin.vtasyntax.QueryTerm;
import com.verint.textanalytics.dal.darwin.vtasyntax.TASQueryConfiguration;
import com.verint.textanalytics.dal.darwin.vtasyntax.errors.VTASyntaxProcessingException;
import com.verint.textanalytics.dal.darwin.vtasyntax.errors.VTASyntaxRecognitionException;
import com.verint.textanalytics.dal.rest.RestDataAccess;
import com.verint.textanalytics.model.analyze.FieldMetric;
import com.verint.textanalytics.model.analyze.MetricData;
import com.verint.textanalytics.model.analyze.TextElementSentimentsMetric;
import com.verint.textanalytics.model.facets.*;
import com.verint.textanalytics.model.interactions.*;
import com.verint.textanalytics.model.storedSearch.StoredSearchQuery;
import com.verint.textanalytics.model.trends.DiscoverTrendsContext;
import com.verint.textanalytics.model.trends.InteractionDailyVolumeDataPoint;
import com.verint.textanalytics.model.trends.TextElementTrend;
import com.verint.textanalytics.model.trends.TrendType;
import lombok.val;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author EZlotnik Provider for Text Analytics data access
 */
public class DarwinTextAnalyticsProvider implements TextAnalyticsProvider {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	private RestDataAccess restDataAccess;

	@Autowired
	private EASearchRequestGenerator requestGenerator;

	@Autowired
	private EASearchResponseConverter responseConverter;

	@Autowired
	private ConfigurationManager configurationManager;

	@Autowired
	private PerformanceMetrics performanceMetrics;

	private String textEngineServiceBaseUrl;

	private String textRepositoryServiceBaseUrl;

	private Map<String, String> postRawBodyHeaders;

	private String suggesterName = "mySuggester";

	private final Integer suggestionsLimit = 100;

	/**
	 * Constructor of Text Analytics provider.
	 */
	public DarwinTextAnalyticsProvider() {
		logger.debug("Allocating DarwinTextAnalyticsProvider");

		this.postRawBodyHeaders = new HashMap<>();
		this.postRawBodyHeaders.put("Content-Type", "application/x-www-form-urlencoded");
	}

	/**
	 * Method invoked by Spring after bean creation.
	 */
	public void initialize() {
		logger.debug("Initializing DarwinTextAnalyticsProvider provider");

		ApplicationConfiguration applicationConfig = configurationManager.getApplicationConfiguration();
		this.textEngineServiceBaseUrl = applicationConfig.getDarwinTextEngineServiceBaseUrl();
		this.textRepositoryServiceBaseUrl = applicationConfig.getDarwinTextRepositoryServiceBaseUrl();

		logger.debug("Text Analytics provider was initialized with following base urls , EASearch - {}, Solr - {}",
		             !StringUtils.isNullOrBlank(this.textEngineServiceBaseUrl) ? this.textEngineServiceBaseUrl : "",
		             !StringUtils.isNullOrBlank(this.textRepositoryServiceBaseUrl) ? this.textRepositoryServiceBaseUrl : "");

		logger.debug("Text Analytics provider was initialized");
	}

	@Override
	public SearchInteractionsResult searchInteractions(String tenant, String channel, SearchInteractionsContext searchContext, String language, int pageStart, int pageSize, String sortProperty, String sortDirection) {
		SearchInteractionsResult searchResult = null;

		try {
			Timer.Context context = this.performanceMetrics.startTimedOperation(OperationType.SearchInteractionsPage);

			this.logRequestInfo("Search Interactions", tenant, channel, searchContext, language);

			RestRequestPathsAndQueryParams restRequestPathsAndQueryParams = requestGenerator.getSearchInteractionsQuery(tenant, channel, searchContext, language, pageStart,
			                                                                                                            pageSize, sortProperty, sortDirection);

			// execute request for documents
			String documentsJson = this.restDataAccess.executeGetRequest(SolrTextQueryType.SearchInteractions.toString(), this.textEngineServiceBaseUrl,
			                                                             restRequestPathsAndQueryParams.getQueryPaths(), restRequestPathsAndQueryParams.getQueryParams());

			searchResult = this.responseConverter.getInteractions(documentsJson, tenant, channel);

			this.performanceMetrics.stopTimedOperation(context);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
		}

		return searchResult;
	}

	@Override
	public StoredSearchQuery getSearchInteractionsQueryForCategory(String tenant, String channel, SearchInteractionsContext searchContext, String language) {

		try {
			this.logRequestInfo("Search Interactions Query For Category", tenant, channel, searchContext, language);
			return requestGenerator.getSearchInteractionsQueryForCategory(tenant, channel, searchContext, language, true);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
		}

		return null;
	}

	@Override
	public Facet facetedSearch(String tenant, String channel, SearchInteractionsContext searchContext, String facetQueryField, List<FieldMetric> facetMetrics, Integer limit, String language) {
		return facetedSearch(tenant, channel, searchContext, facetQueryField, facetMetrics, limit, language, false);
	}

	@Override
	public Facet facetedSearch(String tenant, String channel, SearchInteractionsContext searchContext, String facetQueryField, List<FieldMetric> facetMetrics, Integer limit, String language, boolean preventExclude) {
		Facet facet = null;
		RestRequestPathsAndQueryParams restRequestPathsAndQueryParams = null;

		try {
			Timer.Context context = this.performanceMetrics.startTimedOperation(OperationType.FacetedSearch);

			this.logRequestInfo(String.format("Faceted Search on %s", facetQueryField), tenant, channel, searchContext, language);

			restRequestPathsAndQueryParams = requestGenerator.getFacetQuery(tenant, channel, searchContext, facetQueryField, facetMetrics, limit, language, preventExclude);

			String facetsJson = this.restDataAccess.executeGetRequest(SolrTextQueryType.Facets.toString(), this.textEngineServiceBaseUrl,
			                                                          restRequestPathsAndQueryParams.getQueryPaths(), restRequestPathsAndQueryParams.getQueryParams());

			facet = this.responseConverter.getFacet(facetsJson, tenant, channel, facetQueryField, facetMetrics);

			this.performanceMetrics.stopTimedOperation(context);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
		}

		return facet;
	}

	@Override
	public Facet getSentimentFacet(String tenant, String channel, SearchInteractionsContext searchContext, String sentimentFacetQueryField, String language) {
		Facet sentimentFacet = null;
		RestRequestPathsAndQueryParams restRequestPathsAndQueryParams = null;

		try {
			Timer.Context context = this.performanceMetrics.startTimedOperation(OperationType.SentimentFacet);

			this.logRequestInfo(String.format("Sentiment Faceted Search on %s", sentimentFacetQueryField), tenant, channel, searchContext, language);

			restRequestPathsAndQueryParams = requestGenerator.getFacetQuery(tenant, channel, searchContext, sentimentFacetQueryField, null, null, language);

			String facetsJson = this.restDataAccess.executeGetRequest(SolrTextQueryType.Facets.toString(), this.textEngineServiceBaseUrl,
			                                                          restRequestPathsAndQueryParams.getQueryPaths(), restRequestPathsAndQueryParams.getQueryParams());

			sentimentFacet = this.responseConverter.getFacet(facetsJson, tenant, channel, sentimentFacetQueryField, null);

			this.performanceMetrics.stopTimedOperation(context);

		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
		}

		return sentimentFacet;
	}

	@Override
	public List<TextElementsFacetNode> getTextElementsFacet(String tenant, String channel, SearchInteractionsContext searchContext, String language, TextElementType textElementType, String textElementPrefix, List<TextElementMetricType> metricsToCals, TextElementMetricType orderMetric, SpeakerQueryType speaker, boolean sameUtteranceMode, boolean leavesOnly, int elementsLimit) {
		List<TextElementsFacetNode> lstFacets = null;
		OperationType operationType;

		try {
			// generate entities facet
			Timer.Context context = null;

			this.logRequestInfo("Text Elements Facet ", tenant, channel, searchContext, language, textElementType);

			SolrTextQueryType solrTextQueryType;

			switch (textElementType) {
				case Entities:
					solrTextQueryType = SolrTextQueryType.EntitiesFacet;
					if (metricsToCals.size() > 1) {
						operationType = leavesOnly ? OperationType.EntitiesFacetLeavesOnlyWithStats : OperationType.EntitiesFacetWithStats;
					} else {
						operationType = leavesOnly ? OperationType.EntitiesFacetLeavesOnlyWithStats : OperationType.EntitiesFacet;
					}

					context = this.performanceMetrics.startTimedOperation(operationType);
					break;
				case Relations:
					solrTextQueryType = SolrTextQueryType.RelationsFacet;
					if (metricsToCals.size() > 1) {
						operationType = leavesOnly ? OperationType.RelationsFacetLeavesOnlyWithStats : OperationType.RelationsFacetWithStats;
					} else {
						operationType = leavesOnly ? OperationType.RelationsFacetLeavesOnlyWithStats : OperationType.RelationsFacet;
					}
					context = this.performanceMetrics.startTimedOperation(operationType);
					break;
				default:
					throw new Exception(TAConstants.ErrorMessages.textElementTypeInvalid);
			}


			RestRequestPathsAndQueryParams restRequestPathsAndQueryParams = null;

			restRequestPathsAndQueryParams = requestGenerator.getTextElementsFacetWithStatsQuery(tenant, channel, searchContext, language, textElementType, textElementPrefix,
			                                                                                     metricsToCals, orderMetric, speaker, leavesOnly, elementsLimit);


			String facetsJson = this.restDataAccess.executeGetRequest(solrTextQueryType.toString(), this.textEngineServiceBaseUrl, restRequestPathsAndQueryParams.getQueryPaths(),
			                                                          restRequestPathsAndQueryParams.getQueryParams());

			List<String> facetNames = new ArrayList<>();
			facetNames.add(TAConstants.FacetQuery.textElementFacetAlias);

			lstFacets = this.responseConverter.getTextElementsFacets(facetsJson, facetNames, true, leavesOnly);

			this.performanceMetrics.stopTimedOperation(context);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
		}

		return lstFacets;
	}

	@Override
	public List<TextElementsFacetNode> getTextElementsChildrenMetrics(String tenant, String channel, SearchInteractionsContext searchContext, String language, TextElementType textElementType, List<TextElementsFacetNode> textElements, List<TextElementMetricType> metricsToCals, TextElementMetricType orderMetric, SpeakerQueryType speaker, boolean sameUtteranceMode, boolean leavesOnly, int elementsLimit) {
		List<TextElementsFacetNode> lstFacets = null;
		OperationType operationType;

		try {
			// generate entities facet
			Timer.Context context = null;

			this.logRequestInfo("Text Elements Children Metrics ", tenant, channel, searchContext, language, textElementType);

			RestRequestPathsAndQueryParams restRequestPathsAndQueryParams = null;

			SolrTextQueryType solrTextQueryType;

			switch (textElementType) {
				case Entities:
					solrTextQueryType = SolrTextQueryType.EntitiesChildrenMetrics;
					if (metricsToCals.size() > 1) {
						operationType = leavesOnly ? OperationType.EntitiesChildrenFacetLeavesOnlyWithStats : OperationType.EntitiesChildrenFacetWithStats;
					} else {
						operationType = leavesOnly ? OperationType.EntitiesChildrenFacetLeavesOnlyWithStats : OperationType.EntitiesChildrenFacet;
					}
					context = this.performanceMetrics.startTimedOperation(operationType);
					break;
				case Relations:
					solrTextQueryType = SolrTextQueryType.RelationsChildrenMetrics;
					if (metricsToCals.size() > 1) {
						operationType = leavesOnly ? OperationType.RelationsChildrenFacetLeavesOnlyWithStats : OperationType.RelationsChildrenFacetWithStats;
					} else {
						operationType = leavesOnly ? OperationType.RelationsChildrenFacetLeavesOnlyWithStats : OperationType.RelationsChildrenFacet;
					}

					context = this.performanceMetrics.startTimedOperation(operationType);
					break;
				default:
					throw new Exception(TAConstants.ErrorMessages.textElementTypeInvalid);
			}

			restRequestPathsAndQueryParams = requestGenerator.getTextElementsChildrenFacetWithStatsQuery(tenant, channel, searchContext, language, textElementType, textElements,
			                                                                                             metricsToCals, orderMetric, speaker, leavesOnly, elementsLimit);


			String facetsJson = this.restDataAccess.executeGetRequest(solrTextQueryType.toString(), this.textEngineServiceBaseUrl, restRequestPathsAndQueryParams.getQueryPaths(),
			                                                          restRequestPathsAndQueryParams.getQueryParams());

			List<String> facetNames = new ArrayList<>();
			// generate names of facets to extract
			if (!CollectionUtils.isEmpty(textElements)) {
				for (int i = 0; i < textElements.size(); i++) {
					facetNames.add(TAConstants.FacetQuery.textElementFacetAlias + i);
				}
			}

			lstFacets = this.responseConverter.getTextElementsFacets(facetsJson, facetNames, true, leavesOnly);

			this.performanceMetrics.stopTimedOperation(context);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
		}

		return lstFacets;
	}

	@Override
	public List<TextElementsFacetNode> getTextElementsMetrics(String tenant, String channel, SearchInteractionsContext searchContext, String language, TextElementType textElementType, List<TextElementsFacetNode> textElements, List<TextElementMetricType> metricsToCalc, Boolean leavesOnly) {
		List<TextElementsFacetNode> textElementsNode = null;

		try {
			// generate entities facet
			Timer.Context context = null;

			RestRequestPathsAndQueryParams restRequestPathsAndQueryParams = null;

			this.logRequestInfo("Text Elements Metrics", tenant, channel, searchContext, language);

			Pair<RestRequestPathsAndQueryParams, Map<String, String>> queryResult = requestGenerator.getTextElementsMetricsQuery(tenant, channel, searchContext, language,
			                                                                                                                     textElementType, textElements, metricsToCalc,
			                                                                                                                     leavesOnly);

			restRequestPathsAndQueryParams = queryResult.getLeft();

			SolrTextQueryType solrTextQueryType;

			switch (textElementType) {
				case Entities:
					solrTextQueryType = SolrTextQueryType.EntitiesMetrics;
					context = leavesOnly ? this.performanceMetrics.startTimedOperation(OperationType.EntitiesFacetLeavesOnlyWithStats)
							             : this.performanceMetrics.startTimedOperation(OperationType.EntitiesFacetWithStats);

					break;
				case Relations:
					solrTextQueryType = SolrTextQueryType.RelationsMetrics;
					context = leavesOnly ?  this.performanceMetrics.startTimedOperation(OperationType.RelationsFacetLeavesOnlyWithStats)
										    : this.performanceMetrics.startTimedOperation(OperationType.RelationsFacetWithStats);
					break;
				default:
					throw new Exception("Text Element type is not defined");
			}

			String textElementMetricsJson = this.restDataAccess.executeGetRequest(solrTextQueryType.toString(), this.textEngineServiceBaseUrl,
			                                                                      restRequestPathsAndQueryParams.getQueryPaths(), restRequestPathsAndQueryParams.getQueryParams());

			textElementsNode = this.responseConverter.getTextElementsMetrics(textElementMetricsJson, queryResult.getRight());

			this.performanceMetrics.stopTimedOperation(context);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
		}

		return textElementsNode;
	}



	/**
	 * Retrieves number of contacts in result for current search.
	 *
	 * @param tenant        tenant
	 * @param channel       channel
	 * @param searchContext interaction search context
	 * @param language      language
	 * @return number of contacts in result
	 */
	@Override
	public int getResultSetInteractionsQuantity(String tenant, String channel, SearchInteractionsContext searchContext, String language) {
		int currentSearchInteractions = 0;

		try {
			this.logRequestInfo("Result Set Interactions Quantity", tenant, channel, searchContext, language);

			Timer.Context context = performanceMetrics.startTimedOperation(OperationType.CurrentResultSetInteractionsQuantity);

			RestRequestPathsAndQueryParams restRequestPathsAndQueryParams = requestGenerator.getCurrentSearchInteractionsQuantityQuery(tenant, channel, searchContext, language);

			String responseJson = restDataAccess.executeGetRequest(SolrTextQueryType.InteractionsQuantity.toString(), this.textEngineServiceBaseUrl,
			                                                       restRequestPathsAndQueryParams.getQueryPaths(), restRequestPathsAndQueryParams.getQueryParams());

			currentSearchInteractions = responseConverter.getQuantity(responseJson);

			performanceMetrics.stopTimedOperation(context);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
		}

		return currentSearchInteractions;
	}

	/**
	 * Number of contact for specific tenant/channel.
	 *
	 * @param tenant        tenant
	 * @param channel       channel
	 * @param searchContext interaction search context
	 * @param language      language
	 * @return number of contacts in result
	 */
	@Override
	public int getTotalInteractionsQuantity(String tenant, String channel, SearchInteractionsContext searchContext, String language) {

		int totalInteractions = 0;

		try {
			this.logRequestInfo("getTotalInteractionsQuantity", tenant, channel, searchContext, language);

			Timer.Context context = performanceMetrics.startTimedOperation(OperationType.InteractionsTotalQuantity);

			RestRequestPathsAndQueryParams restRequestPathsAndQueryParams = requestGenerator.getTotalInteractionsQuantityQuery(tenant, channel, searchContext);

			String responseJson = restDataAccess.executeGetRequest(SolrTextQueryType.InteractionsTotalQuantity.toString(), this.textEngineServiceBaseUrl,
			                                                       restRequestPathsAndQueryParams.getQueryPaths(), restRequestPathsAndQueryParams.getQueryParams());

			totalInteractions = responseConverter.getQuantity(responseJson);

			performanceMetrics.stopTimedOperation(context);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
		}

		return totalInteractions;
	}

	/**
	 * Validates Search Query.
	 *
	 * @param searchQuery channel
	 * @param language    language
	 */
	@Override
	public void validateSearchQuery(String searchQuery, String language) {

		try {
			logger.debug("Validation VTA Syntax Query for expression {}", searchQuery);

			this.requestGenerator.validateSearchQuery(searchQuery, language);

		} catch (VTASyntaxRecognitionException ex) {
			Throwables.propagate(ex);
		} catch (VTASyntaxProcessingException ex) {
			Throwables.propagate(ex);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
		}
	}

	/**
	 * Retrieves interaction review data.
	 *
	 * @param tenant        tenant
	 * @param channel       channel
	 * @param interactionId id of interaction document
	 * @param searchContext current search context
	 * @param language      language of the context
	 * @return interaction's data
	 */
	@Override
	public Interaction getInteractionPreview(String tenant, String channel, String interactionId, SearchInteractionsContext searchContext, String language) {
		Interaction interaction = null;

		try {
			this.logRequestInfo("getInteractionPreview", tenant, channel, searchContext, language);

			Timer.Context context = performanceMetrics.startTimedOperation(OperationType.InteractionPreview);

			RestRequestPathsAndQueryParams restRequestPathsAndQueryParams = requestGenerator.getInteractionPreviewQuery(tenant, channel, interactionId);

			String responseJson = restDataAccess.executeGetRequest(SolrTextQueryType.InteractionReview.toString(), this.textEngineServiceBaseUrl,
			                                                       restRequestPathsAndQueryParams.getQueryPaths(), restRequestPathsAndQueryParams.getQueryParams());

			SearchInteractionsResult searchInteractionsResult = responseConverter.getInteractions(responseJson, tenant, channel);
			if (searchInteractionsResult != null) {
				List<Interaction> interactions = searchInteractionsResult.getInteractions();

				if (interactions != null && interactions.size() > 0) {
					interaction = interactions.get(0);
				}
			}

			performanceMetrics.stopTimedOperation(context);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
		}

		return interaction;
	}

	/**
	 * Generates highlights in specified documents.
	 *
	 * @param tenant            tenant
	 * @param channel           channel
	 * @param parentDocumentIds id of interactions to generate highlights for
	 * @param speakerType       speaker type, for expresssions like A:"phrase", C:"phrase2"
	 *                          ...
	 * @param terms             list of terms to highlight
	 * @param language          language
	 * @return list of highlights
	 */
	public HighlightResult getInteractionsHighlightsForSpeaker(String tenant, String channel, List<FilterFieldValue> parentDocumentIds, SpeakerType speakerType, List<QueryTerm> terms, String language) {
		RestRequestPathsAndQueryParams requestParams = null;

		HighlightResult result = null;

		if (terms != null) {
			Timer.Context context = performanceMetrics.startTimedOperation(OperationType.InteractionHighlightsForSpeaker);

			requestParams = this.requestGenerator.getInteractionsHighlightsQuery(tenant, channel, parentDocumentIds, speakerType, terms, language);

			String requestType = "";
			switch (speakerType) {
				case Agent:
					requestType = SolrTextQueryType.SearchInteractionsHighlightsForAgent.toString();
					break;
				case Customer:
					requestType = SolrTextQueryType.SearchInteractionsHighlightsForCustomer.toString();
					break;
				case Unknown:
					requestType = SolrTextQueryType.SearchInteractionsHighlightsForNonSPS.toString();
					break;
				default:
					break;
			}

			String documentsJson = this.restDataAccess.executeGetRequest(requestType, configurationManager.getApplicationConfiguration().getDarwinTextEngineServiceBaseUrl(),
			                                                             requestParams.getQueryPaths(), requestParams.getQueryParams());

			result = this.responseConverter.getHighlights(documentsJson, tenant, channel);

			performanceMetrics.stopTimedOperation(context);

			logger.debug("Completed Get Highlighting For Speaker '{}',  {} highlights found.", speakerType.toString(),
			             result.getHighlights() != null ? result.getHighlights().size() : "0");
		}

		return result;
	}

	@Override
	public List<TextElementTrend> getTextElementsTrends(String tenant, String channel, DiscoverTrendsContext searchContext, TrendType trendType, String textValue, String sortProperty, String sortDirection, SpeakerQueryType speaker) {

		List<TextElementTrend> trendsList = null;

		this.logRequestInfo("getTextElementsTrends", tenant, channel, searchContext, "");

		try {
			Timer.Context context = null;

			// limit rows according to element type
			int textElementTrendsGridRowsLimit = 0;
			switch (trendType) {
				case Entities:
					textElementTrendsGridRowsLimit = configurationManager.getApplicationConfiguration().getEntitiesTrendsGridRowsLimit();
					context = performanceMetrics.startTimedOperation(OperationType.EntitiesTrends);
					break;
				case Relations:
					textElementTrendsGridRowsLimit = configurationManager.getApplicationConfiguration().getRelationsTrendsGridRowsLimit();
					context = performanceMetrics.startTimedOperation(OperationType.RelationsTrends);
					break;
				case Keyterms:
					textElementTrendsGridRowsLimit = configurationManager.getApplicationConfiguration().getKeytermsTrendsGridRowsLimit();
					context = performanceMetrics.startTimedOperation(OperationType.KeyTermsTrends);
					break;
				case Categories:
					textElementTrendsGridRowsLimit = configurationManager.getApplicationConfiguration().getCategoriesTrendsGridRowsLimit();
					context = performanceMetrics.startTimedOperation(OperationType.CategoriesTrends);
					break;
				default:
					break;
			}

			RestRequestPathsAndQueryParams restRequetPathsAndQueryParams = requestGenerator.getTextElementsTrendsQuery(tenant, channel, trendType, searchContext.getTrendsPeriod().name(), searchContext.getBaseDate(), textValue,
			                                                                                                           sortProperty, sortDirection, textElementTrendsGridRowsLimit,
			                                                                                                           speaker);

			String responseJson = restDataAccess.executeGetRequest(SolrTextQueryType.TextElementTrends.toString(), this.textEngineServiceBaseUrl,
			                                                       restRequetPathsAndQueryParams.getQueryPaths(), restRequetPathsAndQueryParams.getQueryParams());

			trendsList = responseConverter.getTextElementyTrends(responseJson);

			performanceMetrics.stopTimedOperation(context);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
		}

		return trendsList;
	}

	@Override
	public List<InteractionDailyVolumeDataPoint> getInteractionsDailyVolumeSeries(String tenant, String channel, SearchInteractionsContext discoverTrendsContext, String language) {
		List<InteractionDailyVolumeDataPoint> dailyVolumeSeries = null;


		try {
			Timer.Context context = performanceMetrics.startTimedOperation(OperationType.InteractionsDailyVolumeSeries);

			RestRequestPathsAndQueryParams restRequestPathsAndQueryParams = requestGenerator.getInteractionsDailyVolumeSeriesQuery(tenant, channel,
			                                                                                                                       (SearchInteractionsContext) discoverTrendsContext,
			                                                                                                                       language);

			String responseJson = restDataAccess.executeGetRequest(SolrTextQueryType.InteractionsDailyVolumeSeries.toString(), this.textEngineServiceBaseUrl,
			                                                       restRequestPathsAndQueryParams.getQueryPaths(), restRequestPathsAndQueryParams.getQueryParams());


			dailyVolumeSeries = responseConverter.getInteractionsDailyVolumeSeries(responseJson, TAConstants.FacetQuery.interactionsDailyVolumeAlias);

			performanceMetrics.stopTimedOperation(context);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
		}

		return dailyVolumeSeries;
	}

	@Override
	public List<InteractionDailyVolumeDataPoint> getTrendDailyVolumeSeriesByType(String tenant, String channel, TrendType trendType, String entityValue, SearchInteractionsContext discoverTrendsContext, String language, SpeakerQueryType speaker) {
		List<InteractionDailyVolumeDataPoint> trendPercentageDailyVolumeSeries = null;

		ExecutorService threadPool = null;

		try {
			threadPool = Executors.newFixedThreadPool(2);

			val tasks = new ArrayList<Callable<Object>>();
			val textAnalyticsProvider = this;

			String requestId = ThreadContext.get(TAConstants.requestId);
			List<InteractionDailyVolumeDataPoint> entityTrendDailyVolumeSeries = null;
			List<InteractionDailyVolumeDataPoint> totalDailyVolumeSeries = null;

			Callable<Object> getEntityDailyVolume = () -> {
				List<InteractionDailyVolumeDataPoint> dailyVolumeSeries = null;

				try {
					Timer.Context context = null;

					switch (trendType) {
						case Entities:
							context = performanceMetrics.startTimedOperation(OperationType.EntityInteractionsDailyVolumeSeries);
							break;
						case Relations:
							context = performanceMetrics.startTimedOperation(OperationType.RelationInteractionsDailyVolumeSeries);
							break;
						case Keyterms:
							context = performanceMetrics.startTimedOperation(OperationType.KeyTermInteractionsDailyVolumeSeries);
							break;
						case Themes:
							context = performanceMetrics.startTimedOperation(OperationType.ThemeInteractionsDailyVolumeSeries);
							break;
						case Categories:
							context = performanceMetrics.startTimedOperation(OperationType.CategoryInteractionsDailyVolumeSeries);
							break;
						default:
							context = performanceMetrics.startTimedOperation(OperationType.UnknownTrendInteractionsDailyVolumeSeries);
							break;
					}

					ThreadContext.put(TAConstants.requestId, requestId);

					RestRequestPathsAndQueryParams restRequestPathsAndQueryParams = requestGenerator.getTrendInteractionsDailyVolumeSeriesQueryByType(tenant, channel, trendType,
					                                                                                                                                  entityValue, speaker);

					String responseJson = restDataAccess.executeGetRequest(SolrTextQueryType.EntityInteractionsDailyVolumeSeries.toString(), this.textEngineServiceBaseUrl,
					                                                       restRequestPathsAndQueryParams.getQueryPaths(), restRequestPathsAndQueryParams.getQueryParams());

					dailyVolumeSeries = responseConverter.getInteractionsDailyVolumeSeries(responseJson, TAConstants.FacetQuery.interactionsDailyVolumeAlias);

					performanceMetrics.stopTimedOperation(context);

				} catch (Exception ex) {
					Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
					Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
				}

				return new ImmutablePair<>("entityTrendDailyVolume", dailyVolumeSeries);
			};

			Callable<Object> getTotalDailyVolume = () -> {
				ThreadContext.put(TAConstants.requestId, requestId);

				Timer.Context context = performanceMetrics.startTimedOperation(OperationType.InteractionsDailyVolumeSeries);
				List<InteractionDailyVolumeDataPoint> dailyVolumeSeries = textAnalyticsProvider.getInteractionsDailyVolumeSeries(tenant, channel, discoverTrendsContext, language);
				performanceMetrics.stopTimedOperation(context);

				return new ImmutablePair<String, List<InteractionDailyVolumeDataPoint>>("totalDailyVolume", dailyVolumeSeries);
			};

			tasks.add(getEntityDailyVolume);
			tasks.add(getTotalDailyVolume);

			ApplicationConfiguration appConfig = configurationManager.getApplicationConfiguration();

			// invokeAll() returns when all tasks are complete
			List<Future<Object>> lstFutures = threadPool.invokeAll(tasks, appConfig.getDarwinRestRequestTimeout(), TimeUnit.SECONDS);

			if (lstFutures != null) {
				for (Future<?> future : lstFutures) {
					if (future.isDone()) {

						// get tasks result of task is done
						Object taskResult = future.get();

						// result of Integer task
						val res = (Pair<String, List<InteractionDailyVolumeDataPoint>>) taskResult;

						if (taskResult != null) {

							switch (res.getLeft()) {
								case "entityTrendDailyVolume":
									entityTrendDailyVolumeSeries = res.getRight();
									break;
								case "totalDailyVolume":
									totalDailyVolumeSeries = res.getRight();
									break;
								default:
									break;
							}
						}
					}
				}
			}

			if (entityTrendDailyVolumeSeries != null && totalDailyVolumeSeries != null) {

				trendPercentageDailyVolumeSeries = new ArrayList<>();

				Map<DateTime, Double> entityTrendDailyVolumeHash = entityTrendDailyVolumeSeries.stream()
				                                                                               .collect(Collectors.toMap((InteractionDailyVolumeDataPoint v) -> v.getDate(),
				                                                                                                         (InteractionDailyVolumeDataPoint v) -> v.getValue()));
				for (val totalDailyVolumePoint : totalDailyVolumeSeries) {
					if (entityTrendDailyVolumeHash.containsKey(totalDailyVolumePoint.getDate())) {

						val entityTrendDataPoint = entityTrendDailyVolumeHash.get(totalDailyVolumePoint.getDate());

						if (entityTrendDataPoint != 0) {
							trendPercentageDailyVolumeSeries.add(new InteractionDailyVolumeDataPoint(totalDailyVolumePoint.getDate(),
							                                                                         (entityTrendDataPoint / totalDailyVolumePoint.getValue())
									                                                                         * TAConstants.percentage_100));
						}

					} else {
						trendPercentageDailyVolumeSeries.add(new InteractionDailyVolumeDataPoint(totalDailyVolumePoint.getDate(), 0.0));
					}
				}
			}
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
		} finally {
			ThreadUtils.shutdownExecutionThreadPool(threadPool);
		}

		return trendPercentageDailyVolumeSeries;
	}

	@Override
	public List<SearchSuggestion> getTermsAutoCompleteSuggestions(String tenant, String channel, SearchInteractionsContext searchContext, String prefix, String language) {
		List<SearchSuggestion> suggestionsForAutocomplete = null;

		if (this.configurationManager.getApplicationConfiguration().isInvokeAutoCompleteRequest()) {
			try {
				Timer.Context context = performanceMetrics.startTimedOperation(OperationType.TermsAutoCompleteSuggestions);

				RestRequestPathsAndQueryParams restRequestPathsAndQueryParams = requestGenerator.getTermsSuggestionsForAutocompleteQuery(tenant, channel, searchContext, prefix,
				                                                                                                                         language);

				String responseJson = this.restDataAccess.executeGetRequest(SolrTextQueryType.SearchSuggestions.toString(), this.textEngineServiceBaseUrl,
				                                                            restRequestPathsAndQueryParams.getQueryPaths(), restRequestPathsAndQueryParams.getQueryParams());

				suggestionsForAutocomplete = responseConverter.getTermsAutoCompleteSuggestionsSimpleFacet(responseJson, language);

				performanceMetrics.stopTimedOperation(context);
			} catch (Exception ex) {
				Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
				Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
			}
		}

		return suggestionsForAutocomplete;
	}

	@Override
	public List<MetricData> getResultSetMetrics(String tenant, String channel, SearchInteractionsContext searchContext, String language, List<FieldMetric> fieldsMetrics, Boolean readInteractionsNumber) {
		List<MetricData> metrics = new ArrayList<MetricData>();

		RestRequestPathsAndQueryParams restRequestPathsAndQueryParams = null;

		try {
			this.logRequestInfo("get Result Set Metrics on: ", tenant, channel, searchContext, language);

			Timer.Context context = performanceMetrics.startTimedOperation(OperationType.CurrentResultSetMetrics);

			restRequestPathsAndQueryParams = requestGenerator.getResultSetMetricsQuery(tenant, channel, searchContext, language, fieldsMetrics);

			String metricsJson = this.restDataAccess.executeGetRequest(SolrTextQueryType.CurrentResultSetMetrics.toString(), this.textEngineServiceBaseUrl,
			                                                           restRequestPathsAndQueryParams.getQueryPaths(), restRequestPathsAndQueryParams.getQueryParams());

			metrics = this.responseConverter.getResultSetMetrics(metricsJson, fieldsMetrics, readInteractionsNumber);

			performanceMetrics.stopTimedOperation(context);

		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
		}

		return metrics;
	}

	@Override
	public List<TextElementSentimentsMetric> getTextElementsChildrenSentiment(String tenant, String channel, SearchInteractionsContext searchContext, String language, TextElementType textElementType, List<TextElementsFacetNode> textElements, TextElementMetricType orderField, int elementsLimit) {
		List<TextElementSentimentsMetric> textElementSentimentsMetrics = null;

		RestRequestPathsAndQueryParams restRequestPathsAndQueryParams = null;

		long startTimeMillisecs, endTimeMillisecs;

		try {
			this.logRequestInfo("get TextElements Sentiments Metrics on: ", tenant, channel, searchContext, language, textElementType);

			Timer.Context context = null;

			restRequestPathsAndQueryParams = requestGenerator.getTextElementsChildrenSentimentQuery(tenant, channel, searchContext, language, textElementType, textElements,
			                                                                                        orderField, elementsLimit);

			SolrTextQueryType solrTextQueryType;
			OperationType operationType;
			switch (textElementType) {
				case Entities:
					solrTextQueryType = SolrTextQueryType.EntitiesSentiment;
					context = performanceMetrics.startTimedOperation(OperationType.EntitiesChildrenSentiment);
					break;
				case Relations:
					solrTextQueryType = SolrTextQueryType.RelationsSentiment;
					context = performanceMetrics.startTimedOperation(OperationType.RelationsChildrenSentiment);
					break;
				default:
					throw new IllegalArgumentException("textElement is not defined");
			}

			String metricsJson = this.restDataAccess.executeGetRequest(solrTextQueryType.toString(), this.textEngineServiceBaseUrl, restRequestPathsAndQueryParams.getQueryPaths(),
			                                                           restRequestPathsAndQueryParams.getQueryParams());

			textElementSentimentsMetrics = this.responseConverter.getTextElementsSentimentsMetrics(metricsJson);

			performanceMetrics.stopTimedOperation(context);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
		}
		return textElementSentimentsMetrics;
	}


	@Override
	public List<TextElementSentimentsMetric> getTextElementsChildrenSentiment(String tenant, String channel, SearchInteractionsContext searchContext, String language, TextElementType textElementType, String prefix, TextElementMetricType orderField, int elementsLimit) {
		List<TextElementSentimentsMetric> textElementSentimentsMetrics = null;

		RestRequestPathsAndQueryParams restRequestPathsAndQueryParams = null;

		long startTimeMillisecs, endTimeMillisecs;

		try {
			this.logRequestInfo("get TextElements Sentiments Metrics on: ", tenant, channel, searchContext, language, textElementType);

			Timer.Context context = null;

			restRequestPathsAndQueryParams = requestGenerator.getTextElementsChildrenSentimentQuery(tenant, channel, searchContext, language, textElementType, prefix, orderField, elementsLimit);

			SolrTextQueryType solrTextQueryType;
			OperationType operationType;
			switch (textElementType) {
				case Entities:
					solrTextQueryType = SolrTextQueryType.EntitiesSentiment;
					context = performanceMetrics.startTimedOperation(OperationType.EntitiesChildrenSentiment);
					break;
				case Relations:
					solrTextQueryType = SolrTextQueryType.RelationsSentiment;
					context = performanceMetrics.startTimedOperation(OperationType.RelationsChildrenSentiment);
					break;
				default:
					throw new IllegalArgumentException("textElement is not defined");
			}

			String metricsJson = this.restDataAccess.executeGetRequest(solrTextQueryType.toString(), this.textEngineServiceBaseUrl, restRequestPathsAndQueryParams.getQueryPaths(),
			                                                           restRequestPathsAndQueryParams.getQueryParams());

			textElementSentimentsMetrics = this.responseConverter.getTextElementsSentimentsMetrics(metricsJson);

			performanceMetrics.stopTimedOperation(context);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
		}
		return textElementSentimentsMetrics;
	}

	@Override
	public List<TextElementSentimentsMetric> getTextElementsSentiment(String tenant, String channel, SearchInteractionsContext searchContext, String language, TextElementType textElementType, List<TextElementsFacetNode> textElements, int textElementsLimit) {
		List<TextElementSentimentsMetric> textElementSentimentsMetrics = null;
		RestRequestPathsAndQueryParams restRequestPathsAndQueryParams = null;

		long startTimeMillisecs, endTimeMillisecs;

		try {
			this.logRequestInfo("get TextElements Sentiments Metrics on: ", tenant, channel, searchContext, language, textElementType);

			Timer.Context context = null;

			restRequestPathsAndQueryParams = requestGenerator.getTextElementsSentimentQuery(tenant, channel, searchContext, language, textElementType, textElements,
			                                                                                textElementsLimit);

			SolrTextQueryType solrTextQueryType;
			OperationType operationType;
			switch (textElementType) {
				case Entities:
					solrTextQueryType = SolrTextQueryType.EntitiesSentiment;
					operationType = OperationType.EntitiesSentiment;
					context = performanceMetrics.startTimedOperation(operationType);
					break;
				case Relations:
					solrTextQueryType = SolrTextQueryType.RelationsSentiment;
					operationType = OperationType.RelationsSentiments;
					context = performanceMetrics.startTimedOperation(operationType);
					break;
				default:
					throw new IllegalArgumentException("textElement is not defined");
			}

			String metricsJson = this.restDataAccess.executeGetRequest(solrTextQueryType.toString(), this.textEngineServiceBaseUrl, restRequestPathsAndQueryParams.getQueryPaths(),
			                                                           restRequestPathsAndQueryParams.getQueryParams());

			textElementSentimentsMetrics = this.responseConverter.getTextElementsSentimentsMetrics(metricsJson);

			performanceMetrics.stopTimedOperation(context);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
		}
		return textElementSentimentsMetrics;
	}

	/**
	 * Retrieve Solr the suggestions for search query.
	 *
	 * @param tenant        tenant
	 * @param channel       channel
	 * @param searchContext current search
	 * @param searchQuery   query to suggest for it
	 * @param language      language
	 * @return autocomplete text suggestions
	 */
	public List<WeightedSuggestion> getFreeTextLookupSuggestions(String tenant, String channel, SearchInteractionsContext searchContext, String searchQuery, String language) {
		List<WeightedSuggestion> suggestionsResult = null;

		try {
			RestRequestPathsAndQueryParams restRequestPathsAndQueryParams = null;

			this.logRequestInfo("getFreeTextLookupSuggestions", tenant, channel, searchContext, language);

			// get Collection (distributed) status including nodes statuses and shards
			CollectionStatus collectionStatus = this.getCollectionStatus(tenant);

			String joinedReplicasToQuery = this.getActiveReplicasList(collectionStatus, tenant);
			if (!StringUtils.isNullOrBlank(joinedReplicasToQuery)) {

				// generate suggestions query
				restRequestPathsAndQueryParams = requestGenerator.getFreeTextLookupSuggestionsQuery(tenant, searchQuery, this.suggesterName, this.suggestionsLimit,
				                                                                                    joinedReplicasToQuery);

				String suggestionsJson = this.restDataAccess.executeGetRequest(OperationType.SolrSuggestions.toString(), this.textRepositoryServiceBaseUrl,
				                                                               restRequestPathsAndQueryParams.getQueryPaths(), restRequestPathsAndQueryParams.getQueryParams());

				// convert suggestions json to suggestions
				suggestionsResult = this.responseConverter.getFreeTextLookupSuggestions(suggestionsJson, suggesterName);

			} else {
				logger.warn("No shards are available for collection {}", tenant);
			}
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
		}

		return suggestionsResult;
	}

	@Override
	public TASQueryConfiguration getVTASyntaxTASQueryConfiguration(String language) {
		return this.requestGenerator.getVTASyntaxTASQueryConfiguration(language);
	}

	private CollectionStatus getCollectionStatus(String tenant) {
		CollectionStatus collectionStatus = null;

		// generate query for Collection status using Solr admin page
		RestRequestPathsAndQueryParams restRequestPathsAndQueryParams = this.requestGenerator.getCollectionStatusRequest(tenant);

		String collectionStatusJson = this.restDataAccess.executeGetRequest(OperationType.SolrCollectionStatus.toString(), this.textRepositoryServiceBaseUrl,
		                                                                    restRequestPathsAndQueryParams.getQueryPaths(), restRequestPathsAndQueryParams.getQueryParams());

		collectionStatus = this.responseConverter.convertToCollectionStatus(collectionStatusJson, tenant);

		return collectionStatus;
	}

	private String getActiveReplicasList(CollectionStatus collectionStatus, String tenant) {
		String joinedReplicasToQuery = "";

		if (collectionStatus != null) {
			if (!CollectionUtils.isEmpty(collectionStatus.getShards())) {

				//@formatter:off
				
				// generate shards to include in the request
				List<Replica> replicasToQuery = collectionStatus.getShards().stream()
																			.filter(sh -> sh.getState() == ShardState.Active)
																			.map(sh ->  sh.getReplicas().stream().filter(r -> r.getState() == ReplicaState.Active).findFirst())
																			.map(or -> or.isPresent() ? or.get() : null)
																			.filter(r -> r != null)
																			.collect(Collectors.toList());
				if (!CollectionUtils.isEmpty(replicasToQuery)) {
					joinedReplicasToQuery =  replicasToQuery.stream()
								   							.map(r ->  String.format("%s/%s", r.getBaseUrl(), r.getCore()))
								   							.collect(Collectors.joining(","));
					
					if (StringUtils.isNullOrBlank(joinedReplicasToQuery)) {
						logger.warn("No active replicas are available for shards in collection {}", tenant);						
					}					 					
					 
				} else {
					logger.warn("No active replicas are available for shards in collection {}", tenant);
				}
			}
		}
		
		return joinedReplicasToQuery;
	}
	

	@Override
	public boolean createTenantIfNotExists(String tenant) {
		Boolean returnValue = false;

		RestRequestPathsAndQueryParams restRequestPathsAndQueryParams = null;

		try {
			logger.info("Executing createTenantIfNotExists for tenant - {}", tenant);

			Timer.Context context = performanceMetrics.startTimedOperation(OperationType.CreateTenant);

			restRequestPathsAndQueryParams = requestGenerator.getCreateTenantQuery(tenant);

			try {

				String responseJson = this.restDataAccess.executePutRequest(EASearchTextQueryType.CreateTenant.toString(),
				                                                            configurationManager.getApplicationConfiguration().getDarwinTextEngineServiceBaseUrl(),
				                                                            restRequestPathsAndQueryParams.getQueryPaths(), null, null, null, null);
				returnValue = true;
			} catch (HttpExecutionException e) {

				if (e.getResponseStatus() == TAConstants.httpCode400 && e.getResponseText().contains("already exists")) {
					returnValue = true;
				}
			}

			performanceMetrics.stopTimedOperation(context);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
		}
		return returnValue;

	}

	@Override
	public boolean deleteTenantAndTenantData(String tenant) {
		Boolean returnValue = false;

		RestRequestPathsAndQueryParams restRequestPathsAndQueryParams = null;

		try {
			logger.info("Executing deleteTenantAndTenantData for tenant - {}", tenant);

			Timer.Context timer = performanceMetrics.startTimedOperation(OperationType.DeleteTenant);

			restRequestPathsAndQueryParams = requestGenerator.getDeleteTenantQuery(tenant);

			try {
				String responseJson = this.restDataAccess.executeDeleteRequest(EASearchTextQueryType.DeleteTenant.toString(),
				                                                               configurationManager.getApplicationConfiguration().getDarwinTextEngineServiceBaseUrl(),
				                                                               restRequestPathsAndQueryParams.getQueryPaths(), null);
				returnValue = true;
			} catch (HttpExecutionException e) {

				if (e.getResponseStatus() == TAConstants.httpCode400 && e.getResponseText().contains("Could not find collection")) {
					returnValue = false;
				}
			}

			performanceMetrics.stopTimedOperation(timer);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
		}
		return returnValue;
	}

	@Override
	public boolean isSourceTypeExistInChannel(String tenant, String channel, SourceType type) {

		RestRequestPathsAndQueryParams restRequestPathsAndQueryParams = null;

		Boolean sourceTypeExists = false;

		try {
			logger.info("Checking That source -{} ,exsists in a channel -{} , for Tenant - {}", type, channel, tenant);

			Timer.Context timer = performanceMetrics.startTimedOperation(OperationType.CheckSourceTypeForChannel);

			restRequestPathsAndQueryParams = requestGenerator.getCheckSourceTypeInChannelQuery(tenant, channel, type.toString());

			String responseJson = restDataAccess.executeGetRequest(SolrTextQueryType.SourceTypeCheck.toString(), this.textEngineServiceBaseUrl,
			                                                       restRequestPathsAndQueryParams.getQueryPaths(), restRequestPathsAndQueryParams.getQueryParams());

			sourceTypeExists = responseConverter.getIsSourceTypeInChannel(responseJson);

			performanceMetrics.stopTimedOperation(timer);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
		}

		return sourceTypeExists;
	}

	private void logRequestInfo(String textAction, String tenant, String channel, SearchInteractionsContext searchContext, String language) {
		if (logger.isDebugEnabled()) {
			logger.debug("Executing {} for tenant - {}, channel - {}, searchContext - {}, language - {}", textAction, tenant, channel, JSONUtils.getObjectJSON(searchContext),
			            language);
		}
	}

	private void logRequestInfo(String textAction, String tenant, String channel, SearchInteractionsContext searchContext, String language, TextElementType textElement) {
		if (logger.isDebugEnabled()) {
			logger.debug("Executing {} for tenant - {}, channel - {}, searchContext - {}, language - {}, textElement {}", textAction, tenant, channel,
			            JSONUtils.getObjectJSON(searchContext), language, textElement.toString());
		}
	}

	/**
	 * @author EZlotnik
	 *
	 */
	enum SolrTextQueryType {
		// @formatter:off
		EntitiesFacet("Entities Facet"),
		EntitiesChildrenMetrics("Entitirs Chidlren Metrics"),
		EntityMetrics("Entity Metrics"),
		EntitiesMetrics("Entities Metrics"),
		RelationsFacet("Relations Facet"),
		RelationsChildrenMetrics("Relations Children Metrics"),
		RelationMetrics("Relation Metrics"),
		RelationsMetrics("Relations Metrics"),
		Facets("Facets"), 
		SearchInteractions("Search Interactions"), 
		SearchInteractionsHighlights("Search Interactions Highlights"),
		SearchInteractionsHighlightsForAgent("Search Interactions Highlights For Agent Terms"), 
		SearchInteractionsHighlightsForCustomer("Search Interactions Highlights For Customer Terms"), 
		SearchInteractionsHighlightsForNonSPS("Search Interactions Highlights For Non-SPS Terms"),

		InteractionsTotalQuantity("Interactions Total Quantity"), 
		InteractionsQuantity("Interactions Quantity"), 
		InteractionReview("Interaction Review"), 
		InteractionsDailyVolumeSeries("Interactions Daily Volume Series"), 
		EntityInteractionsDailyVolumeSeries("Entity Interactions Daily Volume Series"),
		
		CurrentResultSetMetrics("CurrentResultSetMetrics"),
		EntitiesSentiment("EntitiesSentiment"),
		RelationsSentiment("RelationsSentiment"),
		
		SearchSuggestions("Search suggestions for autocomplete"),

		TextElementTrends("Get Entity Trends"),
		SourceTypeCheck("Checking if source type exists in channel");
		// @formatter:on

		private String requestType;

		/**
		 * Constructor.
		 *
		 * @param requestType
		 */
		SolrTextQueryType(String requestType) {
			this.requestType = requestType;
		}
	}

	/***
	 * @author yzanis
	 */
	enum EASearchTextQueryType {

		// @formatter:off
		CreateTenant("Create Tenant"), 
		DeleteTenant("Delete Tenant");
		// @formatter:on

		private String requestType;

		/**
		 * Constructor.
		 *
		 * @param requestType
		 */
		EASearchTextQueryType(String requestType) {
			this.requestType = requestType;
		}
	}
}
