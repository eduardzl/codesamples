package com.verint.textanalytics.dal.darwin;

import java.util.List;
import java.util.Map;

import com.verint.textanalytics.model.analyze.*;
import com.verint.textanalytics.model.facets.*;
import com.verint.textanalytics.model.interactions.*;
import com.verint.textanalytics.model.trends.*;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Interface for response converter component which converts application service
 * response to Portal data model.
 * 
 * @author EZlotnik
 *
 */
public interface ResponseConverter {

	/**
	 * Converts Search Interactions response to list of interactions models.
	 *
	 * @param searchDocumentsResponse textual response received as response to Search Documents
	 *                                request
	 * @param tenant                  tenant for which data is being requested
	 * @param channel                 channel for which data is being requested
	 * @return list of interactions matching the search
	 */
	SearchInteractionsResult getInteractions(String searchDocumentsResponse, String tenant, String channel);

	/**
	 * getFacets.
	 * 
	 * @param searchFacetsResponse
	 *            searchFacetsResponse
	 * @param tenant
	 *            tenant
	 * @param channel
	 *            channel
	 * @param facetField
	 *            field of faceting
	 * @param fieldsMetrics
	 *            list of metrics
	 * @return List<Facet>
	 */
	Facet getFacet(String searchFacetsResponse, String tenant, String channel, String facetField, List<FieldMetric> fieldsMetrics);

	/**
	 * @param utterancesJson utterances json
	 * @param tenant tenant
	 * @param channel channel
	 * @return pair : utterances and highlights in utterances
	 */
	HighlightResult getHighlights(String utterancesJson, String tenant, String channel);

	/**
	 * @param textElementFacetsResponse the string containing JSON response
	 * @param facetNames names of facet to extract
	 * @param withStats  should read stats from facet
	 * @param leavesOnly is the responose of Leaves only request
	 * @return The structured tree of entity facets
	 */
	List<TextElementsFacetNode> getTextElementsFacets(String textElementFacetsResponse, List<String> facetNames, Boolean withStats, Boolean leavesOnly);

	/**
	 * Converts text element metrics response.
	 * @param textElementMetricsResponse text element metrics response
	 * @param facetAliases aliases of inner facets
	 * @return text element metrics
	 */
	List<TextElementsFacetNode> getTextElementsMetrics(String textElementMetricsResponse, Map<String, String> facetAliases);

	/**
	 * @param responseJson
	 *            responseJson
	 * @return int
	 */
	int getQuantity(String responseJson);

	/**
	 * Extracts data objects from dates facet response.
	 * 
	 * @param interactonsDateFacetJson
	 *            json of facet date response.
	 * @param facetAlias
	 *            - facet alias in response json
	 * @return an interactions date facet.
	 */
	List<InteractionDailyVolumeDataPoint> getInteractionsDailyVolumeSeries(String interactonsDateFacetJson, String facetAlias);

	/**
	 * Extracts date range facet.
	 * @param interactonsDateFacetJson facet response json
	 * @return list of daily volumes point
	 */
	List<InteractionDailyVolumeDataPoint> getInteractionsDailyVolumeSeriesSimpleFacet(String interactonsDateFacetJson);

	/**
	 * Extracts data objects from entity entity trends response.
	 * 
	 * @param entityTrendsJson
	 *            entityTrendsJson
	 * @return List of entity trends
	 */
	List<TextElementTrend> getTextElementyTrends(String entityTrendsJson);

	/**
	 * Converts json of suggestion response to suggestions.
	 * @param suggestionsFacetResponse json of the suggestion response
	 * @param language language language
	 * @param facetAlias facet alias in response json
	 * @param interactionsCountStatAlias number of interaction stat alias
	 * @return autocomplete data
	 */
	List<SearchSuggestion> getTermsAutoCompleteSuggestions(String suggestionsFacetResponse, String language, String facetAlias, String interactionsCountStatAlias);

	/**
	 * Converts json of suggestion response to suggestions.
	 * @param suggestionsFacetResponse  response  json
	 * @param language language
	 * @return list of suggestions
	 */
	List<SearchSuggestion> getTermsAutoCompleteSuggestionsSimpleFacet(String suggestionsFacetResponse, String language);

	/**
	 * @param metricsJson   metricsJson
	 * @param fieldsMetrics metrics to calculate
	 * @param readInteractionsCount should interactions number be read
	 * @return List<MetricData>
	 */
	List<MetricData> getResultSetMetrics(String metricsJson, List<FieldMetric> fieldsMetrics, Boolean readInteractionsCount);

	/**
	 * @param metricsJson
	 *            metricsJson
	 * @return List<EntitySentimentsMetric>
	 */
	List<TextElementSentimentsMetric> getTextElementsSentimentsMetrics(String metricsJson);

	/***
	 * 
	 * @param json
	 *            response json
	 * @return checks that the number of rows found is more then 1
	 */
	boolean getIsSourceTypeInChannel(String json);

	/***
	 * 
	 * @param textElementFacetPathResponse
	 *            response json
	 * @param textElements
	 *            list of text elements in the path
	 * @param utteranceLevelMode
	 *            facet mode
	 * @return the result tree
	 */
	TextElementsFacetNode getTextElementsFacetPath(String textElementFacetPathResponse, List<TextElementType> textElements, boolean utteranceLevelMode);

	/**
	 * Converts to Solr Collection status.
	 * @param collectionStatusJson
	 *            collection status json.
	 * @param tenant
	 *            tenant
	 * @return collection status.
	 */
	CollectionStatus convertToCollectionStatus(String collectionStatusJson, String tenant);

	/**
	 * Parsers Solr suggestions response.
	 *
	 * @param suggestionsJson suggestions json.
	 * @param suggesterName   suggester
	 * @return list of suggestions.
	 */
	List<WeightedSuggestion> getFreeTextLookupSuggestions(String suggestionsJson, String suggesterName);
}
