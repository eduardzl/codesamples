package com.verint.textanalytics.dal.darwin;

import com.verint.textanalytics.common.exceptions.TextQueryExecutionException;
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
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

/**
 * Interface for query generator component. Being used to TextAnalytics provider
 * implementation to generate queries to application service
 *
 * @author EZlotnik
 */
public interface RequestGenerator {

	/**
	 * Generates query url for Search interactions.
	 *
	 * @param tenant        Tenant
	 * @param channel       Channel
	 * @param searchContext search parameters
	 * @param language      language
	 * @param pageStart     start of documents range
	 * @param pageSize      number of documents in page
	 * @param sortProperty  sort property
	 * @param sortDirection sort direction
	 * @return wrapper with url paths and query parameters
	 * @throws TextQueryExecutionException exception thrown during query generation
	 */
	RestRequestPathsAndQueryParams getSearchInteractionsQuery(String tenant, String channel, SearchInteractionsContext searchContext, String language, int pageStart, int pageSize, String sortProperty, String sortDirection);

	/**
	 * Generates query for Category Search Interactions request.
	 *
	 * @param tenant        tenant
	 * @param channel       channel
	 * @param searchContext Search Interactions Context of search to became category.
	 * @param language      language
	 * @param isEncoded     isEncoded
	 * @return category query
	 */
	 StoredSearchQuery getSearchInteractionsQueryForCategory(String tenant, String channel, SearchInteractionsContext searchContext, String language, boolean isEncoded);

	/**
	 * @param tenant           tenant
	 * @param channel          channel
	 * @param searchContext    searchContext
	 * @param facetsQueryField field to perform faceting on it
	 * @param language         language
	 * @param facetMetrics     additional metrics to be calculated
	 * @param limit            limit on number of facet values
	 * @return RestRequestPathsAndQueryParams
	 * @throws TextQueryExecutionException exception thrown during query generation
	 */
	RestRequestPathsAndQueryParams getFacetQuery(String tenant, String channel, SearchInteractionsContext searchContext, String facetsQueryField, List<FieldMetric> facetMetrics, Integer limit, String language);

	/**
	 * @param tenant           tenant
	 * @param channel          channel
	 * @param searchContext    searchContext
	 * @param facetsQueryField field to perform faceting on it
	 * @param language         language
	 * @param facetMetrics     additional metrics to be calculated
	 * @param limit            limit on number of terms in facet query
	 * @param preventExclude   if true, don't exclude facet values
	 * @return RestRequestPathsAndQueryParams
	 * @throws TextQueryExecutionException exception thrown during query generation
	 */
	RestRequestPathsAndQueryParams getFacetQuery(String tenant, String channel, SearchInteractionsContext searchContext, String facetsQueryField, List<FieldMetric> facetMetrics, Integer limit, String language, boolean preventExclude);

	/**
	 * Generates textElements facet request using Solr 5 Facet JSON API.
	 *
	 * @param tenant            tenant
	 * @param channel           channel
	 * @param searchContext     searchContext searchContext
	 * @param language          language
	 * @param textElementType   textElement
	 * @param textElementPrefix prefix of text elements
	 * @param metricsToCalc     metrics to calculate
	 * @param orderMetric       order metric
	 * @param speaker           facetOnSpeaker
	 * @param leavesOnly        leaves only should be retrieved
	 * @param elementsLimit     limit to number of elements to retrieve in facet
	 * @return request parameters
	 */
	RestRequestPathsAndQueryParams getTextElementsFacetWithStatsQuery(String tenant, String channel, SearchInteractionsContext searchContext, String language, TextElementType textElementType, String textElementPrefix, List<TextElementMetricType> metricsToCalc, TextElementMetricType orderMetric, SpeakerQueryType speaker, boolean leavesOnly, int elementsLimit);

	/**
	 * Retrieves metrics of children of specified text elements.
	 *
	 * @param tenant          tenant
	 * @param channel         channel
	 * @param searchContext   search context
	 * @param language        language
	 * @param textElementType type of text element
	 * @param textElements    text elements
	 * @param metricsToCalc   metrics to calculate
	 * @param orderMetric     sort metric
	 * @param speaker         speaker
	 * @param leavesOnly      leaves only
	 * @param elementsLimit   limit on number of elements
	 * @return metrics of children elements
	 */
	RestRequestPathsAndQueryParams getTextElementsChildrenFacetWithStatsQuery(String tenant, String channel, SearchInteractionsContext searchContext, String language, TextElementType textElementType, List<TextElementsFacetNode> textElements, List<TextElementMetricType> metricsToCalc, TextElementMetricType orderMetric, SpeakerQueryType speaker, boolean leavesOnly, int elementsLimit);

	/**
	 * Generates Text Element Metric Query.
	 *
	 * @param tenant          tenant
	 * @param channel         channel
	 * @param searchContext   search context
	 * @param language        language
	 * @param textElementType text element type : Entity, Relation ...
	 * @param textElemens     list of text elements to retrieve metrics for : 1/Device, ...
	 * @param metricsToCalc   metrics to calculate for those text elements
	 * @param leavesOnly      leaves only mode
	 * @return text element metrics
	 */
	Pair<RestRequestPathsAndQueryParams, Map<String, String>> getTextElementsMetricsQuery(String tenant, String channel, SearchInteractionsContext searchContext, String language, TextElementType textElementType, List<TextElementsFacetNode> textElemens, List<TextElementMetricType> metricsToCalc, Boolean leavesOnly);

	/**
	 * Generates textElements facet request using Solr 5 Facet JSON API.
	 *
	 * @param tenant               tenant
	 * @param channel              channel
	 * @param searchContext        searchContext searchContext
	 * @param language             language
	 * @param textElementType      text element type : Entities, Relations..
	 * @param hierarchyLevelNumber text elements level to retrieve
	 * @param textElements         text elements to extract data
	 * @param metricsToCalc        metrics to calculate for each bucket
	 * @param orderMetric          order metric
	 * @param facetOnSpeaker       facetOnSpeaker
	 * @param elementsLimit        limit on number of elements
	 * @return request parameters
	 */
	RestRequestPathsAndQueryParams getTextElementsFacetWithStatsOnSameUtteranceQuery(String tenant, String channel, SearchInteractionsContext searchContext, String language, TextElementType textElementType, int hierarchyLevelNumber, List<TextElementsFacetNode> textElements, List<TextElementMetricType> metricsToCalc, TextElementMetricType orderMetric, SpeakerQueryType facetOnSpeaker, int elementsLimit);


	/**
	 * @param tenant            tenant
	 * @param channel           channel
	 * @param parentDocumentIds list of interactions ids to request highlights for
	 * @param speaker           speaker type
	 * @param terms             terms to highlight
	 * @param language          language
	 * @return a generated query
	 */
	RestRequestPathsAndQueryParams getInteractionsHighlightsQuery(String tenant, String channel, List<FilterFieldValue> parentDocumentIds, SpeakerType speaker, List<QueryTerm> terms, String language);

	/**
	 * @param tenant        tenant
	 * @param channel       channel
	 * @param searchContext searchContext
	 * @return RestRequestPathsAndQueryParams
	 * @throws TextQueryExecutionException exception thrown during query generation
	 */
	RestRequestPathsAndQueryParams getTotalInteractionsQuantityQuery(String tenant, String channel, SearchInteractionsContext searchContext);

	/**
	 * @param tenant        tenant
	 * @param channel       channel
	 * @param searchContext searchContext
	 * @param language      language
	 * @return RestRequestPathsAndQueryParams
	 * @throws TextQueryExecutionException exception thrown during query generation
	 */
	RestRequestPathsAndQueryParams getCurrentSearchInteractionsQuantityQuery(String tenant, String channel, SearchInteractionsContext searchContext, String language);

	/**
	 * Validates Search Query expression.
	 * @param searchQuery search expression.
	 * @param language    lnguage
	 */
	void validateSearchQuery(String searchQuery, String language);

	/**
	 * Retrieves list of utterances data for interaction.
	 *
	 * @param tenant        tenant
	 * @param channel       channel
	 * @param interactionId interaction document id
	 * @return list of utterances data
	 */
	RestRequestPathsAndQueryParams getInteractionPreviewQuery(String tenant, String channel, String interactionId);

	/**
	 * Generates query url for Search entities trends.
	 *
	 * @param tenant        tenant
	 * @param channel       channel
	 * @param trendType     type of requested trend
	 * @param periodName    searchContext
	 * @param baseDate      base date in ticks for historical trend, if null then the base date is NOW
	 * @param value         value
	 * @param sortProperty  sort property
	 * @param sortDirection sort direction
	 * @param limitTo       limitTo
	 * @param speaker       trends according to speaker type
	 * @return query
	 */
	RestRequestPathsAndQueryParams getTextElementsTrendsQuery(String tenant, String channel, TrendType trendType, String periodName, String baseDate, String value, String sortProperty, String sortDirection, int limitTo, SpeakerQueryType speaker);

	/**
	 * Generates a query for interactions daily volume distribution.
	 *
	 * @param tenant        tenant
	 * @param channel       channel
	 * @param searchContext search context
	 * @param language      language
	 * @return a query for daily volume distribution series
	 */
	RestRequestPathsAndQueryParams getInteractionsDailyVolumeSeriesQuery(String tenant, String channel, SearchInteractionsContext searchContext, String language);

	/**
	 * Generates am interactions daily distribution volume series for entity.
	 *
	 * @param tenant    tenant
	 * @param channel   channel
	 * @param trendType trendType
	 * @param entity    entity
	 * @param speaker   speaker
	 * @return a query for entity's daily volume distribution series.
	 */
	RestRequestPathsAndQueryParams getTrendInteractionsDailyVolumeSeriesQueryByType(String tenant, String channel, TrendType trendType, String entity, SpeakerQueryType speaker);

	/**
	 * @param tenant          tenant
	 * @param channel         channel
	 * @param searchContext   current search
	 * @param language        language
	 * @param suggestionValue the prefix of the searchedText
	 * @return autocomplete text suggestions
	 */
	RestRequestPathsAndQueryParams getTermsSuggestionsForAutocompleteQuery(String tenant, String channel, SearchInteractionsContext searchContext, String suggestionValue, String language);

	/**
	 * @param tenant        tenant
	 * @param channel       channel
	 * @param context       context
	 * @param fieldsMetrics metrics to calculate
	 * @param language      language
	 * @return RestRequestPathsAndQueryParams
	 */
	RestRequestPathsAndQueryParams getResultSetMetricsQuery(String tenant, String channel, SearchInteractionsContext context, String language, List<FieldMetric> fieldsMetrics);

	/**
	 * Retrives sentiment of text elements when the query is being applied on interactions level.
	 *
	 * @param tenant               tenant
	 * @param channel              channel
	 * @param context              context
	 * @param language             language
	 * @param textElementType      type of text Element : topics_f, relations_f
	 * @param textElementsPrefixes prefixes of elements to retrieve
	 * @param orderField           field to order the facet groups
	 * @param elementsLimit        limit on number of elements
	 * @return generated query
	 */
	RestRequestPathsAndQueryParams getTextElementsChildrenSentimentQuery(String tenant, String channel, SearchInteractionsContext context, String language, TextElementType textElementType, List<TextElementsFacetNode> textElementsPrefixes, TextElementMetricType orderField, int elementsLimit);


	/**
	 * Retrives sentiment of text elements when the query is being applied on interactions level.
	 *
	 * @param tenant               tenant
	 * @param channel              channel
	 * @param context              context
	 * @param language             language
	 * @param textElementType      type of text Element : topics_f, relations_f
	 * @param prefix prefix of text elements to retrieve sentiment for
	 * @param orderField           field to order the facet groups
	 * @param elementsLimit        limit on number of elements
	 * @return generated query
	 */
	RestRequestPathsAndQueryParams getTextElementsChildrenSentimentQuery(String tenant, String channel, SearchInteractionsContext context, String language, TextElementType textElementType, String prefix, TextElementMetricType orderField, int elementsLimit);


	/**
	 * Retrives sentiment of text elements when the query is being applied on interactions level.
	 *
	 * @param tenant          tenant
	 * @param channel         channel
	 * @param context         context
	 * @param language        language
	 * @param textElementType type of text Element : topics_f, relations_f
	 * @param textElements    values of text elements to retrieve sentiment for
	 * @param elementsLimit   limit on number of elements
	 * @return generated query
	 */
	RestRequestPathsAndQueryParams getTextElementsSentimentQuery(String tenant, String channel, SearchInteractionsContext context, String language, TextElementType textElementType, List<TextElementsFacetNode> textElements, int elementsLimit);

	/**
	 * @param tenant tenant
	 * @return RestRequestPathsAndQueryParams
	 */
	RestRequestPathsAndQueryParams getCreateTenantQuery(String tenant);

	/**
	 * @param tenant tenant
	 * @return RestRequestPathsAndQueryParams
	 */
	RestRequestPathsAndQueryParams getDeleteTenantQuery(String tenant);

	/***
	 * @param tenant     tenant
	 * @param channel    channel
	 * @param sourceType the sourceTypeToCheck
	 * @return RestRequestPathsAndQueryParams
	 */
	RestRequestPathsAndQueryParams getCheckSourceTypeInChannelQuery(String tenant, String channel, String sourceType);

	/**
	 * Generates url for Collection status.
	 *
	 * @param tenant tenant
	 * @return query parameters
	 */
	RestRequestPathsAndQueryParams getCollectionStatusRequest(String tenant);

	/**
	 * Generated query for suggestions using the specified replicas.
	 *
	 * @param tenant                tenant
	 * @param searchQuery           search query
	 * @param suggester             suggester name
	 * @param suggestionsCount      number of suggestions
	 * @param joinedReplicasToQuery list of active replicas in collection : each replicate for
	 *                              shard
	 * @return query parameters
	 */
	RestRequestPathsAndQueryParams getFreeTextLookupSuggestionsQuery(String tenant, String searchQuery, String suggester, Integer suggestionsCount, String joinedReplicasToQuery);

	/**
	 * Retrives TAS Query configuration for specific language.
	 *
	 * @param language language
	 * @return TAS VTA syntax Query configuration
	 */
	TASQueryConfiguration getVTASyntaxTASQueryConfiguration(String language);
}
