package com.verint.textanalytics.dal.darwin;

import com.verint.textanalytics.dal.darwin.vtasyntax.QueryTerm;
import com.verint.textanalytics.dal.darwin.vtasyntax.TASQueryConfiguration;
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

import java.util.List;

/**
 * Text Analytics provider interface.
 *
 * @author EZlotnik
 */
public interface TextAnalyticsProvider {

	/**
	 * Search for interactions.
	 *
	 * @param tenant        Tenant of request
	 * @param channel       Channel of request
	 * @param searchContext context with describes the search
	 * @param language      Language of request
	 * @param pageStart     start of the interactions range
	 * @param pageSize      page size
	 * @param sortProperty  sort property
	 * @param sortDirection sort direction
	 * @return list of interactions
	 */
	SearchInteractionsResult searchInteractions(String tenant, String channel, SearchInteractionsContext searchContext, String language, int pageStart, int pageSize, String sortProperty, String sortDirection);

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
	HighlightResult getInteractionsHighlightsForSpeaker(String tenant, String channel, List<FilterFieldValue> parentDocumentIds, SpeakerType speakerType, List<QueryTerm> terms, String language);

	/**
	 * Generates query for category search.
	 *
	 * @param tenant        tenant
	 * @param channel       channel
	 * @param searchContext search context.
	 * @param language      laguage
	 * @return search interactions query for category.
	 */
	StoredSearchQuery getSearchInteractionsQueryForCategory(String tenant, String channel, SearchInteractionsContext searchContext, String language);

	/**
	 * @param tenant           Tenant
	 * @param channel          Channel of the request
	 * @param searchContext    context describing current search
	 * @param facetsQueryField field to perform faceting on it
	 * @param facetMetrics     additional metrics to calculate
	 * @param limit            limit on number of terms
	 * @param language         language
	 * @return List<Facet>
	 */
	Facet facetedSearch(String tenant, String channel, SearchInteractionsContext searchContext, String facetsQueryField, List<FieldMetric> facetMetrics, Integer limit, String language);

	/**
	 * @param tenant           Tenant
	 * @param channel          Channel of the request
	 * @param searchContext    context describing current search
	 * @param facetsQueryField field to perform faceting on it
	 * @param facetMetrics     additional metrics to calculate
	 * @param limit            limit of number of terms for facet query
	 * @param language         language
	 * @param isPreventExclude isPreventExclude
	 * @return List<Facet>
	 */
	Facet facetedSearch(String tenant, String channel, SearchInteractionsContext searchContext, String facetsQueryField, List<FieldMetric> facetMetrics, Integer limit, String language, boolean isPreventExclude);

	/**
	 * @param tenant                    tenant
	 * @param channel                   channel
	 * @param searchContext             searchContext
	 * @param sentimentFacetsQueryField sentimentFacetsQueryField
	 * @param language                  language
	 * @return List<Facet>
	 */
	Facet getSentimentFacet(String tenant, String channel, SearchInteractionsContext searchContext, String sentimentFacetsQueryField, String language);

	/**
	 * Generates Text Elements facet.
	 *
	 * @param tenant            Tenant
	 * @param channel           Channel of the request
	 * @param searchContext     context describing current search
	 * @param language          language of the text field
	 * @param textElementType   textElementType : Entities, Relations
	 * @param textElementPrefix prefix for text elements
	 * @param metricsToCalc     metrics to calculate
	 * @param orderMetric       order metric
	 * @param speaker           speaker
	 * @param sameUtteranceMode sameUtteranceMode - all utterance level filters should apply
	 *                          on the same utterances
	 * @param leavesOnly should only text elements withouts children be displayed
	 * @param elementsLimit limit on facet elements
	 * @return list of nodes in text acet tree
	 */
	List<TextElementsFacetNode> getTextElementsFacet(String tenant, String channel, SearchInteractionsContext searchContext, String language, TextElementType textElementType,
													String textElementPrefix, List<TextElementMetricType> metricsToCalc, TextElementMetricType orderMetric,
													SpeakerQueryType speaker, boolean sameUtteranceMode, boolean leavesOnly, int elementsLimit);


	/**
	 * Generates Text Elements facet.
	 *
	 * @param tenant            Tenant
	 * @param channel           Channel of the request
	 * @param searchContext     context describing current search
	 * @param language          language of the text field
	 * @param textElementType   textElementType : Entities, Relations
	 * @param textElements      text elements
	 * @param metricsToCalc     metrics to calculate
	 * @param orderMetric       order metric
	 * @param speaker           speaker
	 * @param sameUtteranceMode sameUtteranceMode - all utterance level filters should apply
	 *                          on the same utterances
	 * @param leavesOnly should only text elements withouts children be displayed
	 * @param elementsLimit limit on facet elements
	 * @return list of nodes in text acet tree
	 */
	List<TextElementsFacetNode> getTextElementsChildrenMetrics(String tenant, String channel, SearchInteractionsContext searchContext, String language, TextElementType textElementType,
																 List<TextElementsFacetNode> textElements, List<TextElementMetricType> metricsToCalc, TextElementMetricType orderMetric,
															     SpeakerQueryType speaker, boolean sameUtteranceMode, boolean leavesOnly, int elementsLimit);

	/**
	 * Retrives Metrics for Text Element.
	 * @param tenant tenant
	 * @param channel channel
	 * @param searchContext  search context
	 * @param language language of request
	 * @param textElementType type of text elements (Entities, Relations ...)
	 * @param textElements  text elements
	 * @param metricsToCalc metrics to calculate
	 * @param  leavesOnly leaves only mode
	 * @return text elements (relations, topics) with calculated metrics metrics
	 */
	List<TextElementsFacetNode> getTextElementsMetrics(String tenant, String channel, SearchInteractionsContext searchContext, String language, TextElementType textElementType,
											            List<TextElementsFacetNode> textElements, List<TextElementMetricType> metricsToCalc, Boolean leavesOnly);


	/**
	 * Retrives number of interactions in result set of current search.
	 *
	 * @param tenant        tenant
	 * @param channel       channel
	 * @param searchContext search context
	 * @param language      language
	 * @return number of interactions
	 */
	int getResultSetInteractionsQuantity(String tenant, String channel, SearchInteractionsContext searchContext, String language);

	/**
	 * Retrieves number of total number of interactions.
	 *
	 * @param tenant        tenanat
	 * @param channel       channel
	 * @param searchContext search context
	 * @param language      language
	 * @return number of interactions.
	 */
	int getTotalInteractionsQuantity(String tenant, String channel, SearchInteractionsContext searchContext, String language);

	/**
	 * Validates search query.
	 * @param searchQuery search terms query
	 * @param language language of request
	 */
	void validateSearchQuery(String searchQuery, String language);

	/**
	 * Retrieves interaction's data for review.
	 *
	 * @param tenant        Tenant
	 * @param channel       Channel
	 * @param interactionId interaction's data
	 * @param searchContext context describing current search
	 * @param language      language of the text field
	 * @return interaction's data
	 */
	Interaction getInteractionPreview(String tenant, String channel, String interactionId, SearchInteractionsContext searchContext, String language);

	/**
	 * Generates an interactions daily volume series.
	 *
	 * @param tenant                tenant
	 * @param channel               channel
	 * @param discoverTrendsContext discover trends context
	 * @param language              language
	 * @return a series of interaction daily volume
	 */
	List<InteractionDailyVolumeDataPoint> getInteractionsDailyVolumeSeries(String tenant, String channel, SearchInteractionsContext discoverTrendsContext, String language);

	/**
	 * Generated an interactions daily volume series a specific entity.
	 *
	 * @param tenant                tenant
	 * @param channel               channel
	 * @param trendType             trendType
	 * @param entityValue           the value of entity
	 * @param discoverTrendsContext context of Discover Trends workspace
	 * @param language              the language
	 * @param speaker               speaker
	 * @return a list of data point for interaction volume.
	 */
	List<InteractionDailyVolumeDataPoint> getTrendDailyVolumeSeriesByType(String tenant, String channel, TrendType trendType, String entityValue, SearchInteractionsContext discoverTrendsContext, String language, SpeakerQueryType speaker);

	/**
	 * Retrieves a entity trends.
	 *
	 * @param tenant        tenant
	 * @param channel       channel
	 * @param searchContext searchContext
	 * @param trendType     type of requested Trend
	 * @param value         value
	 * @param sortProperty  sort property
	 * @param sortDirection sort direction
	 * @param speaker       trends according to speaker type
	 * @return List of entity trends
	 */
	List<TextElementTrend> getTextElementsTrends(String tenant, String channel, DiscoverTrendsContext searchContext, TrendType trendType, String value, String sortProperty, String sortDirection, SpeakerQueryType speaker);

	/**
	 * Retrieve the autocomplete suggestions for the prefix.
	 *
	 * @param tenant        tenant
	 * @param channel       channel
	 * @param searchContext current search
	 * @param prefix        the text to search in order to create the suggestion
	 * @param language      language
	 * @return autocomplete text suggestions
	 */
	List<SearchSuggestion> getTermsAutoCompleteSuggestions(String tenant, String channel, SearchInteractionsContext searchContext, String prefix, String language);

	/**
	 * Retrieve Metrics.
	 *
	 * @param tenant       tenant
	 * @param channel      channel
	 * @param context      context
	 * @param language     language
	 * @param metricFields context
	 * @param readInteractionsNumber should interactions number be read
	 * @return metrics for result set
	 */
	List<MetricData> getResultSetMetrics(String tenant, String channel, SearchInteractionsContext context, String language, List<FieldMetric> metricFields, Boolean readInteractionsNumber);

	/**
	 * Retrieves Text Elements (Relations, Topics) sentiment for elemenets with specified prefix.
	 *
	 * @param tenant               tenant
	 * @param channel              channel
	 * @param searchContext currentSearchContext
	 * @param language             language
	 * @param textElementType      type of textElement : Topics/Relation
	 * @param textElements  text elements to get thise children sentiment
	 * @param orderField field in schema to sort by it
	 * @param elementsLimit limit on number of elements
	 * @return sentiment for text elements.
	 */
	List<TextElementSentimentsMetric> getTextElementsChildrenSentiment(String tenant, String channel, SearchInteractionsContext searchContext, String language, TextElementType textElementType, List<TextElementsFacetNode> textElements, TextElementMetricType orderField, int elementsLimit);


	/**
	 * Retrieves Text Elements (Relations, Topics) sentiment for elemenets with specified prefix.
	 *
	 * @param tenant               tenant
	 * @param channel              channel
	 * @param searchContext currentSearchContext
	 * @param language             language
	 * @param textElementType      type of textElement : Topics/Relation
	 * @param prefix  prefix of text elements to retrive : prefix "1"  ->  "1/Hotel Service", "1/Tourism Attractions"
	 * @param orderField field in schema to sort by it
	 * @param elementsLimit limit on number of elements
	 * @return sentiment for text elements.
	 */
	List<TextElementSentimentsMetric> getTextElementsChildrenSentiment(String tenant, String channel, SearchInteractionsContext searchContext, String language, TextElementType textElementType, String prefix, TextElementMetricType orderField, int elementsLimit);

	/**
	 * Retrieves Sentiment for Text Elements (Relations, Topics) for supplied elements .
	 * @param tenant tenant of request
	 * @param channel channel of request
	 * @param searchContext search context
	 * @param language langugae
	 * @param textElementType type of text elements
	 * @param textElements values to retrieve sentiment for
	 * @param textElementsLimit limit on number of elements
	 * @return sentiment for text elements.
	 */
	List<TextElementSentimentsMetric> getTextElementsSentiment(String tenant, String channel, SearchInteractionsContext searchContext, String language, TextElementType textElementType, List<TextElementsFacetNode> textElements, int textElementsLimit);

	/***
	 * createTenantIfNotExists.
	 *
	 * @param tenant tenant
	 * @return true if the tenant was created or if the tenant existed
	 */
	boolean createTenantIfNotExists(String tenant);

	/***
	 * deleteTenantAndTenantData.
	 *
	 * @param tenant tenant to delete
	 * @return this deletes the tenant and the tenant data, return true if was
	 * deleted false if the tenant does not exists or error in any other
	 * case.
	 */
	boolean deleteTenantAndTenantData(String tenant);

	/***
	 * @param tenant  tenant
	 * @param channel channel
	 * @param type    sourceType to check if exists
	 * @return indication is this channel has items from this source type
	 */
	boolean isSourceTypeExistInChannel(String tenant, String channel, SourceType type);


	/**
	 * Retrieve the suggestions for search query.
	 *
	 * @param tenant        tenant
	 * @param channel       channel
	 * @param searchContext current search
	 * @param prefix        the text to search in order to create the suggestion
	 * @param language      language
	 * @return autocomplete text suggestions
	 */
	List<WeightedSuggestion> getFreeTextLookupSuggestions(String tenant, String channel, SearchInteractionsContext searchContext, String prefix, String language);

	/**
	 * Retrieves VTA Syntax query configuration.
	 * @param language language
	 * @return query configuration
	 */
	TASQueryConfiguration getVTASyntaxTASQueryConfiguration(String language);
}