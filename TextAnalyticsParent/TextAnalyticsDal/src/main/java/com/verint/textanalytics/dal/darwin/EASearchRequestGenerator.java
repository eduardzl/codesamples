package com.verint.textanalytics.dal.darwin;

import com.verint.textanalytics.common.collection.MultivaluedStringMap;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.utils.StringUtils;
import com.verint.textanalytics.dal.darwin.vtasyntax.QueryTerm;
import com.verint.textanalytics.dal.darwin.vtasyntax.TASQueryConfiguration;
import com.verint.textanalytics.model.analyze.FieldMetric;
import com.verint.textanalytics.model.facets.SpeakerQueryType;
import com.verint.textanalytics.model.facets.TextElementMetricType;
import com.verint.textanalytics.model.facets.TextElementType;
import com.verint.textanalytics.model.facets.TextElementsFacetNode;
import com.verint.textanalytics.model.interactions.FilterFieldValue;
import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import com.verint.textanalytics.model.interactions.SpeakerType;
import com.verint.textanalytics.model.storedSearch.StoredSearchQuery;
import com.verint.textanalytics.model.trends.TrendType;
import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * Generates URL for EASearch requests according to EASearch API definition.
 *
 * @author imor
 */
public final class EASearchRequestGenerator implements RequestGenerator {
	@Autowired
	@Setter
	private SolrRequestGenerator solrRequestGenerator;

	private final String fq = "fq";

	/**
	 * Sets Solr query parameters.
	 *
	 * @param queryParameters query parameters
	 */
	public void setQueryParams(SolrQueryParameters queryParameters) {
		solrRequestGenerator.setQueryParams(queryParameters);
	}

	/**
	 * Sets implementation of Text Engine Configuration.
	 *
	 * @param textEngineSchemeService text engine scheme configuration
	 */
	public void setTextEngineConfigurationService(TextEngineSchemaService textEngineSchemeService) {
		solrRequestGenerator.setTextEngineConfigurationService(textEngineSchemeService);
	}

	@Override
	public RestRequestPathsAndQueryParams getSearchInteractionsQuery(String tenant, String channel, SearchInteractionsContext searchContext, String language, int pageStart, int pageSize, String sortProperty, String sortDirection) {
		RestRequestPathsAndQueryParams restRequest = solrRequestGenerator.getSearchInteractionsQuery(tenant, channel, searchContext, language, pageStart, pageSize, sortProperty,
		                                                                                             sortDirection);
		updateRequestForEASearch(restRequest);

		return restRequest;
	}

	@Override
	public StoredSearchQuery getSearchInteractionsQueryForCategory(String tenant, String channel, SearchInteractionsContext searchContext, String language, boolean isEncoded) {
		return solrRequestGenerator.getSearchInteractionsQueryForCategory(tenant, channel, searchContext, language, isEncoded);
	}

	@Override
	public RestRequestPathsAndQueryParams getFacetQuery(String tenant, String channel, SearchInteractionsContext searchContext, String facetsQueryField, List<FieldMetric> facetMetrics, Integer limit, String language) {
		RestRequestPathsAndQueryParams restRequest = solrRequestGenerator.getFacetQuery(tenant, channel, searchContext, facetsQueryField, facetMetrics, limit, language);
		updateRequestForEASearch(restRequest);

		return restRequest;
	}

	@Override
	public RestRequestPathsAndQueryParams getFacetQuery(String tenant, String channel, SearchInteractionsContext searchContext, String facetsQueryField, List<FieldMetric> facetMetrics, Integer limit, String language, boolean preventExclude) {
		RestRequestPathsAndQueryParams restRequest = solrRequestGenerator.getFacetQuery(tenant, channel, searchContext, facetsQueryField, facetMetrics, limit, language,
		                                                                                preventExclude);
		updateRequestForEASearch(restRequest);

		return restRequest;
	}

	@Override
	public RestRequestPathsAndQueryParams getTotalInteractionsQuantityQuery(String tenant, String channel, SearchInteractionsContext searchContext) {
		RestRequestPathsAndQueryParams restRequest = solrRequestGenerator.getTotalInteractionsQuantityQuery(tenant, channel, searchContext);
		updateRequestForEASearch(restRequest);

		return restRequest;
	}

	@Override
	public RestRequestPathsAndQueryParams getCurrentSearchInteractionsQuantityQuery(String tenant, String channel, SearchInteractionsContext searchContext, String language) {
		RestRequestPathsAndQueryParams restRequest = solrRequestGenerator.getCurrentSearchInteractionsQuantityQuery(tenant, channel, searchContext, language);

		updateRequestForEASearch(restRequest);
		return restRequest;
	}

	@Override
	public void validateSearchQuery(String searchQuery, String language) {
		solrRequestGenerator.validateSearchQuery(searchQuery, language);
	}

	@Override
	public RestRequestPathsAndQueryParams getInteractionPreviewQuery(String tenant, String channel, String interactionId) {

		RestRequestPathsAndQueryParams restRequest = solrRequestGenerator.getInteractionPreviewQuery(tenant, channel, interactionId);
		updateRequestForEASearch(restRequest);
		return restRequest;
	}

	@Override
	public RestRequestPathsAndQueryParams getInteractionsHighlightsQuery(String tenant, String channel, List<FilterFieldValue> parentDocumentIds, SpeakerType speaker, List<QueryTerm> terms, String language) {
		RestRequestPathsAndQueryParams restRequest = solrRequestGenerator.getInteractionsHighlightsQuery(tenant, channel, parentDocumentIds, speaker, terms, language);
		updateRequestForEASearch(restRequest);
		return restRequest;
	}

	@Override
	public RestRequestPathsAndQueryParams getInteractionsDailyVolumeSeriesQuery(String tenant, String channel, SearchInteractionsContext searchContext, String language) {
		RestRequestPathsAndQueryParams restRequest = solrRequestGenerator.getInteractionsDailyVolumeSeriesQuery(tenant, channel, searchContext, language);

		updateRequestForEASearch(restRequest);
		return restRequest;
	}

	@Override
	public RestRequestPathsAndQueryParams getTrendInteractionsDailyVolumeSeriesQueryByType(String tenant, String channel, TrendType trendType, String entityValue, SpeakerQueryType speaker) {
		RestRequestPathsAndQueryParams restRequest = solrRequestGenerator.getTrendInteractionsDailyVolumeSeriesQueryByType(tenant, channel, trendType, entityValue, speaker);

		updateRequestForEASearch(restRequest);
		return restRequest;
	}

	@Override
	public RestRequestPathsAndQueryParams getResultSetMetricsQuery(String tenant, String channel, SearchInteractionsContext context, String language, List<FieldMetric> fieldsMetrics) {
		RestRequestPathsAndQueryParams restRequest = solrRequestGenerator.getResultSetMetricsQuery(tenant, channel, context, language, fieldsMetrics);

		updateRequestForEASearch(restRequest);
		return restRequest;
	}

	// @formatter:off

	@Override
	public RestRequestPathsAndQueryParams getTextElementsChildrenSentimentQuery(String tenant, String channel, SearchInteractionsContext context, String language, TextElementType textElementType,
																	    List<TextElementsFacetNode> textElements, TextElementMetricType orderField, int elementsLimit) {
	// @formatter:on

		RestRequestPathsAndQueryParams restRequest = solrRequestGenerator.getTextElementsChildrenSentimentQuery(tenant, channel, context, language, textElementType, textElements,
		                                                                                                        orderField, elementsLimit);

		updateRequestForEASearch(restRequest);

		// this is the EASearch Service suffix
		restRequest.getQueryPaths().add(TAConstants.SentimentAPI.ContextName);

		switch (textElementType) {
			case Entities:
				restRequest.getQueryPaths().add(TAConstants.SentimentAPI.Topics);
				break;
			case Relations:
				restRequest.getQueryPaths().add(TAConstants.SentimentAPI.Relations);
				break;
			default:
				throw new IllegalArgumentException("textElement is not defined");
		}

		MultivaluedStringMap queryParams = restRequest.getQueryParams();

		// we need no limit and no zero count entities
		queryParams.add("limit", String.valueOf(elementsLimit));
		queryParams.add(TAConstants.SentimentAPI.Leaves, "false");

		return restRequest;
	}

	// @formatter:off

	@Override
	public RestRequestPathsAndQueryParams getTextElementsChildrenSentimentQuery(String tenant, String channel, SearchInteractionsContext context, String language, TextElementType textElementType,
																	    String prefix, TextElementMetricType orderField, int elementsLimit) {
	// @formatter:on

		RestRequestPathsAndQueryParams restRequest = solrRequestGenerator.getTextElementsChildrenSentimentQuery(tenant, channel, context, language, textElementType, prefix,
		                                                                                                        orderField, elementsLimit);

		updateRequestForEASearch(restRequest);

		// this is the EASearch Service suffix
		restRequest.getQueryPaths().add(TAConstants.SentimentAPI.ContextName);

		switch (textElementType) {
			case Entities:
				restRequest.getQueryPaths().add(TAConstants.SentimentAPI.Topics);
				break;
			case Relations:
				restRequest.getQueryPaths().add(TAConstants.SentimentAPI.Relations);
				break;
			default:
				throw new IllegalArgumentException("textElement is not defined");
		}

		MultivaluedStringMap queryParams = restRequest.getQueryParams();

		// we need no limit and no zero count entities
		queryParams.add("limit", String.valueOf(elementsLimit));
		queryParams.add(TAConstants.SentimentAPI.Leaves, "false");

		return restRequest;
	}

	@Override
	public RestRequestPathsAndQueryParams getTextElementsSentimentQuery(String tenant, String channel, SearchInteractionsContext context, String language, TextElementType textElementType, List<TextElementsFacetNode> textElements, int elementsLimit) {
		RestRequestPathsAndQueryParams restRequest = solrRequestGenerator.getTextElementsSentimentQuery(tenant, channel, context, language, textElementType, textElements,
		                                                                                                elementsLimit);

		updateRequestForEASearch(restRequest);

		// this is the EASearch Service suffix
		restRequest.getQueryPaths().add(TAConstants.SentimentAPI.ContextName);

		switch (textElementType) {
			case Entities:
				restRequest.getQueryPaths().add(TAConstants.SentimentAPI.Topics);
				break;
			case Relations:
				restRequest.getQueryPaths().add(TAConstants.SentimentAPI.Relations);
				break;
			default:
				throw new IllegalArgumentException("textElement is not defined");
		}

		MultivaluedStringMap queryParams = restRequest.getQueryParams();

		// we need no limit and no zero count entities
		queryParams.add("limit", String.valueOf(elementsLimit));
		queryParams.add(TAConstants.SentimentAPI.Leaves, TAConstants.falseLowerCase);

		return restRequest;
	}

	@Override
	public RestRequestPathsAndQueryParams getTextElementsTrendsQuery(String tenant, String channel, TrendType trendType, String periodName, String baseDate, String textElementValue, String sortProperty, String sortDirection, int limitTo, SpeakerQueryType speaker) {
		RestRequestPathsAndQueryParams restRequest = solrRequestGenerator.getTextElementsTrendsQuery(tenant, channel, trendType, periodName, baseDate, textElementValue,
		                                                                                             sortProperty, sortDirection, limitTo, speaker);

		updateRequestForEASearch(restRequest);

		MultivaluedStringMap queryParams = restRequest.getQueryParams();

		switch (trendType) {
			case Categories:
				queryParams.add("facet.level", "Parent");
				break;
			default:
				queryParams.add("facet.byParent", "true");

				queryParams.add("facet.level", TAConstants.FacetQuery.trendsFacetbyParent);
				break;
		}

		return restRequest;
	}

	//@formatter:off
	@Override
	public RestRequestPathsAndQueryParams getTextElementsFacetWithStatsQuery(String tenant, String channel, SearchInteractionsContext searchContext, String language, TextElementType textElementType,
																			 String textElementPrefix, List<TextElementMetricType> metricsToCalc, TextElementMetricType orderMetric,
																			 SpeakerQueryType speaker, boolean leavesOnly, int elementsLimit) {


		RestRequestPathsAndQueryParams restRequest = solrRequestGenerator.getTextElementsFacetWithStatsQuery(tenant, channel, searchContext, language, textElementType,
		                                                                                                     textElementPrefix, metricsToCalc, orderMetric, speaker, leavesOnly, elementsLimit);

		updateRequestForEASearch(restRequest);
		return restRequest;
	}

	@Override
	public RestRequestPathsAndQueryParams getTextElementsChildrenFacetWithStatsQuery(String tenant, String channel, SearchInteractionsContext searchContext, String language, TextElementType textElementType,
																					 List<TextElementsFacetNode> textElements, List<TextElementMetricType> metricsToCalc, TextElementMetricType orderMetric,
																					 SpeakerQueryType speaker, boolean leavesOnly, int elementsLimit) {


		RestRequestPathsAndQueryParams restRequest = solrRequestGenerator.getTextElementsChildrenFacetWithStatsQuery(tenant, channel, searchContext, language, textElementType,
		                                                                                                             textElements, metricsToCalc, orderMetric, speaker, leavesOnly, elementsLimit);

		updateRequestForEASearch(restRequest);
		return restRequest;
	}
	//@formatter:on

	@Override
	public Pair<RestRequestPathsAndQueryParams, Map<String, String>> getTextElementsMetricsQuery(String tenant, String channel, SearchInteractionsContext searchContext, String language, TextElementType textElementType, List<TextElementsFacetNode> textElemens, List<TextElementMetricType> metricsToCalc, Boolean leavesOnly) {

		Pair<RestRequestPathsAndQueryParams, Map<String, String>> restRequest = solrRequestGenerator.getTextElementsMetricsQuery(tenant, channel, searchContext, language,
		                                                                                                                         textElementType, textElemens, metricsToCalc,
		                                                                                                                         leavesOnly);

		updateRequestForEASearch(restRequest.getLeft());
		return restRequest;
	}

	@Override
	//@formatter:off
	public RestRequestPathsAndQueryParams getTextElementsFacetWithStatsOnSameUtteranceQuery(String tenant, String channel, SearchInteractionsContext searchContext, String language,
																							TextElementType textElementType, int hierarchyLevelNumber, List<TextElementsFacetNode> textElements,
																							List<TextElementMetricType> metricsToCal, TextElementMetricType orderMetric, SpeakerQueryType speaker, int elementsLimit) {
	//@formatter:on

		RestRequestPathsAndQueryParams restRequest = solrRequestGenerator.getTextElementsFacetWithStatsOnSameUtteranceQuery(tenant, channel, searchContext, language,
		                                                                                                                    textElementType, hierarchyLevelNumber, textElements,
		                                                                                                                    metricsToCal, orderMetric, speaker, elementsLimit);

		updateRequestForEASearch(restRequest);
		return restRequest;
	}



	@Override
	public RestRequestPathsAndQueryParams getCreateTenantQuery(String tenant) {

		val restRequestPathsAndQueryParams = new RestRequestPathsAndQueryParams();
		removeSelect(restRequestPathsAndQueryParams);

		if (StringUtils.isNullOrBlank(tenant)) {
			throw new IllegalArgumentException("tenant parameter is null or empty");
		}

		addAdmin(restRequestPathsAndQueryParams);
		addTenantToPath(restRequestPathsAndQueryParams);
		addTenantName(tenant, restRequestPathsAndQueryParams);

		return restRequestPathsAndQueryParams;
	}

	@Override
	public RestRequestPathsAndQueryParams getDeleteTenantQuery(String tenant) {
		val restRequestPathsAndQueryParams = new RestRequestPathsAndQueryParams();
		removeSelect(restRequestPathsAndQueryParams);

		if (StringUtils.isNullOrBlank(tenant)) {
			throw new IllegalArgumentException("tenant parameter is null or empty");
		}

		addAdmin(restRequestPathsAndQueryParams);
		addTenantToPath(restRequestPathsAndQueryParams);
		addTenantName(tenant, restRequestPathsAndQueryParams);

		return restRequestPathsAndQueryParams;
	}

	@Override
	public RestRequestPathsAndQueryParams getCheckSourceTypeInChannelQuery(String tenant, String channel, String sourceType) {
		RestRequestPathsAndQueryParams restRequest = solrRequestGenerator.getCheckSourceTypeInChannelQuery(tenant, channel, sourceType);

		updateRequestForEASearch(restRequest);
		return restRequest;
	}

	@Override
	public RestRequestPathsAndQueryParams getTermsSuggestionsForAutocompleteQuery(String tenant, String channel, SearchInteractionsContext searchContext, String suggestionValue, String language) {
		RestRequestPathsAndQueryParams restRequest = solrRequestGenerator.getTermsSuggestionsForAutocompleteQuery(tenant, channel, searchContext, suggestionValue, language);
		updateRequestForEASearch(restRequest);

		return restRequest;
	}

	@Override
	public RestRequestPathsAndQueryParams getCollectionStatusRequest(String tenant) {
		return this.solrRequestGenerator.getCollectionStatusRequest(tenant);
	}

	@Override
	public RestRequestPathsAndQueryParams getFreeTextLookupSuggestionsQuery(String tenant, String searchQuery, String suggester, Integer suggestionsCount, String joinedReplicasToQuery) {
		return this.solrRequestGenerator.getFreeTextLookupSuggestionsQuery(tenant, searchQuery, suggester, suggestionsCount, joinedReplicasToQuery);
	}

	@Override
	public TASQueryConfiguration getVTASyntaxTASQueryConfiguration(String language) {
		return this.solrRequestGenerator.getVTASyntaxTASQueryConfiguration(language);
	}

	// private methods
	private void updateRequestForEASearch(RestRequestPathsAndQueryParams restRequest) {
		updateChannel(restRequest);
		removeSelect(restRequest);
		addSearch(restRequest);
	}

	private void updateChannel(RestRequestPathsAndQueryParams restRequest) {
		MultivaluedStringMap requestQueryParams = restRequest.getQueryParams();

		if (requestQueryParams.containsKey(fq)) {
			List<String> fqElements = (List<String>) requestQueryParams.get(fq);

			String channel = "";
			for (int i = 0; i < fqElements.size(); i++) {
				if (fqElements.get(i).startsWith("channel:")) {
					channel = fqElements.get(i).replaceFirst("channel:", "");
					fqElements.remove(i);
					break;
				}
			}

			List<String> requestPaths = restRequest.getQueryPaths();
			requestPaths.add(channel);
		}

	}

	private void removeSelect(RestRequestPathsAndQueryParams restRequest) {
		List<String> requestPaths = restRequest.getQueryPaths();
		requestPaths.remove("select");
	}

	protected void addSearch(final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		List<String> requestPaths = restRequestPathsAndQueryParams.getQueryPaths();
		requestPaths.add(0, "easearch");
		requestPaths.add(1, "search");
	}

	protected void addTenantName(String tenant, final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		List<String> requestPaths = restRequestPathsAndQueryParams.getQueryPaths();
		requestPaths.add(tenant);
	}

	protected void addTenantToPath(final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		List<String> requestPaths = restRequestPathsAndQueryParams.getQueryPaths();
		requestPaths.add("tenant");
	}

	protected void addAdmin(final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		List<String> requestPaths = restRequestPathsAndQueryParams.getQueryPaths();
		requestPaths.add(0, "easearch");
		requestPaths.add(1, "admin");
	}

	protected void addChannel(RestRequestPathsAndQueryParams restRequestParameters, String channel) {
		restRequestParameters.getQueryPaths().add(channel);
	}

}
