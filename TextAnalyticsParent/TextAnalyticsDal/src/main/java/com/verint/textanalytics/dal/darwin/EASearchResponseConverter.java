package com.verint.textanalytics.dal.darwin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionErrorCode;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionException;
import com.verint.textanalytics.common.utils.CollectionUtils;
import com.verint.textanalytics.common.utils.StringUtils;
import com.verint.textanalytics.model.analyze.FieldMetric;
import com.verint.textanalytics.model.analyze.MetricData;
import com.verint.textanalytics.model.analyze.TextElementSentimentsMetric;
import com.verint.textanalytics.model.documentSchema.TextSchemaField;
import com.verint.textanalytics.model.facets.Facet;
import com.verint.textanalytics.model.facets.TextElementType;
import com.verint.textanalytics.model.facets.TextElementsFacetNode;
import com.verint.textanalytics.model.interactions.*;
import com.verint.textanalytics.model.trends.InteractionDailyVolumeDataPoint;
import com.verint.textanalytics.model.trends.TextElementTrend;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * EASearchResponseConverter.
 *
 * @author imor
 */
public class EASearchResponseConverter implements ResponseConverter {

	@Autowired
	@Setter
	private SolrResponseConverter solrResponseConverter;

	/**
	 * Sets Text Engine Scheme.
	 *
	 * @param textEngineScheme text engine scheme
	 */
	public void setTextEngineConfigurationService(TextEngineSchemaService textEngineScheme) {
		solrResponseConverter.setTextEngineConfigurationService(textEngineScheme);
	}

	@Override
	public HighlightResult getHighlights(String utterancesJson, String tenant, String channel) {
		HighlightResult result = null;

		try {
			if (!StringUtils.isNullOrBlank(utterancesJson)) {

				ObjectMapper mapper = new ObjectMapper();
				JsonNode rootNode = mapper.readTree(utterancesJson);

				solrResponseConverter.checkResponseStatus(rootNode);

				JsonNode docsElem = rootNode.path(TAConstants.InteractionsQuery.response).path(TAConstants.InteractionsQuery.docs);

				// get list of dynamic fields from configuration, those fields are specific for channel and tenant
				List<TextSchemaField> channelDynamicFields = solrResponseConverter.textEngineConfigurationService.getChannelDynamicFields(tenant, channel);

				// retrieve highlights
				result = new HighlightResult();
				result.setUtterances(solrResponseConverter.getInteractionUtterances(docsElem, tenant, channel, channelDynamicFields));
				result.setHighlights(this.getUtterancesHighlights(docsElem));
			}
		} catch (Exception ex) {
			throw new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.JsonResponseParsingError).put(TAConstants.ErrorDataKeys.solrJsonResponse, utterancesJson);
		}

		return result;
	}

	@Override
	public SearchInteractionsResult getInteractions(String searchDocumentsResponse, String tenant, String channel) {
		return solrResponseConverter.getInteractions(searchDocumentsResponse, tenant, channel);
	}

	@Override
	public Facet getFacet(String searchFacetsResponse, String tenant, String channel, String facetField, List<FieldMetric> metrics) {
		return solrResponseConverter.getFacet(searchFacetsResponse, tenant, channel, facetField, metrics);
	}

	@Override
	public List<TextElementsFacetNode> getTextElementsFacets(String textElementFacetsResponse, List<String> facetNames, Boolean withStats, Boolean leavesOnly) {
		return solrResponseConverter.getTextElementsFacets(textElementFacetsResponse, facetNames, withStats, leavesOnly);
	}

	@Override
	public List<TextElementsFacetNode> getTextElementsMetrics(String textElementMetricsResponse, Map<String, String> facetAliases) {
		return solrResponseConverter.getTextElementsMetrics(textElementMetricsResponse, facetAliases);
	}


	@Override
	public int getQuantity(String responseJson) {
		return solrResponseConverter.getQuantity(responseJson);
	}

	@Override
	public List<InteractionDailyVolumeDataPoint> getInteractionsDailyVolumeSeries(String interactonsDateFacetJson, String facetAlias) {
		return solrResponseConverter.getInteractionsDailyVolumeSeries(interactonsDateFacetJson, facetAlias);
	}


	@Override
	public List<InteractionDailyVolumeDataPoint> getInteractionsDailyVolumeSeriesSimpleFacet(String interactonsDateFacetJson) {
		return solrResponseConverter.getInteractionsDailyVolumeSeriesSimpleFacet(interactonsDateFacetJson);
	}


	@Override
	public List<MetricData> getResultSetMetrics(String metricsJson, List<FieldMetric> fieldsMetrics, Boolean readInteractionsCount) {
		return solrResponseConverter.getResultSetMetrics(metricsJson, fieldsMetrics, readInteractionsCount);
	}

	@Override
	public List<TextElementSentimentsMetric> getTextElementsSentimentsMetrics(String metricsJson) {
		return solrResponseConverter.getTextElementsSentimentsMetrics(metricsJson);
	}

	@Override
	public List<TextElementTrend> getTextElementyTrends(String entityTrendsJson) {
		return solrResponseConverter.getTextElementyTrends(entityTrendsJson);
	}

	@Override
	public List<SearchSuggestion> getTermsAutoCompleteSuggestions(String suggestionsFacetResponse, String language, String facetAlias, String numberOfInteractionsStatAlias) {
		return solrResponseConverter.getTermsAutoCompleteSuggestions(suggestionsFacetResponse, language, facetAlias, numberOfInteractionsStatAlias);
	}


	@Override
	public List<SearchSuggestion> getTermsAutoCompleteSuggestionsSimpleFacet(String suggestionsFacetResponse, String language) {
		return solrResponseConverter.getTermsAutoCompleteSuggestionsSimpleFacet(suggestionsFacetResponse, language);
	}

	@Override
	public boolean getIsSourceTypeInChannel(String json) {
		return solrResponseConverter.getIsSourceTypeInChannel(json);
	}

	@Override
	public TextElementsFacetNode getTextElementsFacetPath(String textElementFacetPathResponse, List<TextElementType> textElements, boolean utteranceLevelMode) {
		return solrResponseConverter.getTextElementsFacetPath(textElementFacetPathResponse, textElements, utteranceLevelMode);
	}

	@Override
	public CollectionStatus convertToCollectionStatus(String collectionStatusJson, String tenant) {
		return solrResponseConverter.convertToCollectionStatus(collectionStatusJson, tenant);
	}

	@Override
	public List<WeightedSuggestion> getFreeTextLookupSuggestions(String suggestionsJson, String suggesterName) {
		return this.solrResponseConverter.getFreeTextLookupSuggestions(suggestionsJson, suggesterName);
	}

	private List<UtteranceHighlights> getUtterancesHighlights(JsonNode docsElem) {
		UtteranceHighlights utteranceHighlights = null;
		List<UtteranceHighlights> utterancesHighlights = new ArrayList<>();

		if (!docsElem.isMissingNode()) {
			String utteranceId;

			for (JsonNode docNode : docsElem) {
				utteranceId = docNode.path(TAConstants.SchemaFieldNames.documentId).asText();

				JsonNode highlightsLayerNode = docNode.path(TAConstants.InteractionsQuery.highlightLayers);
				JsonNode speakerTypeNode = docNode.path(TAConstants.InteractionsQuery.speakerType);
				if (highlightsLayerNode != null && !highlightsLayerNode.isMissingNode()) {

					List<TermHighlight> termsHighlights = termsLayerHandler(highlightsLayerNode);
					List<EntityHighlight> topicsHighlights = topicsLayerHandler(highlightsLayerNode);
					List<RelationHighlight> relationsHighlights = relationsLayerHandler(highlightsLayerNode);
					List<KeyTermHighlight> keyTermsHighlights = keyTermsLayerHandler(highlightsLayerNode);
					List<SentimentHighlight> sentimentHighlights = sentimentHighlightsHandler(highlightsLayerNode);

					if (!CollectionUtils.isEmpty(termsHighlights) || !CollectionUtils.isEmpty(topicsHighlights) || !CollectionUtils.isEmpty(relationsHighlights) || !CollectionUtils.isEmpty(keyTermsHighlights) || !CollectionUtils
							.isEmpty(sentimentHighlights)) {
						utteranceHighlights = new UtteranceHighlights();
						utteranceHighlights.setDocumentId(utteranceId);
						utteranceHighlights.setTermHighlights(termsHighlights);
						utteranceHighlights.setEntitiesHighlights(topicsHighlights);
						utteranceHighlights.setRelationsHighlights(relationsHighlights);
						utteranceHighlights.setKeyTermsHighlights(keyTermsHighlights);
						utteranceHighlights.setAllEntitiesHighlights(topicsHighlights);
						utteranceHighlights.setAllRelationsHighlights(relationsHighlights);
						utteranceHighlights.setAllKeyTermsHighlights(keyTermsHighlights);
						utteranceHighlights.setSentimentHighlights(sentimentHighlights);
						utteranceHighlights.setSpeakerType(SpeakerType.toSpeakerType(speakerTypeNode.asText("")));
						utterancesHighlights.add(utteranceHighlights);
					}
				}
			}
		}

		return utterancesHighlights;
	}


	private List<RelationHighlight> relationsLayerHandler(JsonNode layersMarking) {
		List<RelationHighlight> relationHighlights = null;
		RelationHighlight relationHighlight;

		// query RelationsLayer handler
		JsonNode relationsLayer = layersMarking.path(TAConstants.InteractionsQuery.relationsHighlightsLayer);
		if (!relationsLayer.isMissingNode()) {

			for (JsonNode relationNode : relationsLayer) {

				if (relationHighlights == null) {
					relationHighlights = new ArrayList<RelationHighlight>();
				}

				// Relations Highlights
				relationHighlight = new RelationHighlight();
				relationHighlight.setRelation(relationNode.path(TAConstants.InteractionsQuery.relation).asText());

				// Relations Positions and Sentiment
				relationHighlight.setPositions(this.getHighlightPositions(relationNode));
				relationHighlight.setSentiments(this.getSentimentsHighlights(relationNode));

				relationHighlights.add(relationHighlight);
			}
		}

		return relationHighlights;
	}

	private List<KeyTermHighlight> keyTermsLayerHandler(JsonNode layersMarking) {
		List<KeyTermHighlight> keyTermsHighlights = null;
		KeyTermHighlight keyTermHighlight;

		// query RelationsLayer handler
		JsonNode keyTermsLayer = layersMarking.path(TAConstants.InteractionsQuery.keyTermsHighlightsLayer);
		if (!keyTermsLayer.isMissingNode()) {

			for (JsonNode keyTermNode : keyTermsLayer) {

				if (keyTermsHighlights == null) {
					keyTermsHighlights = new ArrayList<KeyTermHighlight>();
				}

				// Relations Highlights
				keyTermHighlight = new KeyTermHighlight();
				keyTermHighlight.setKeyterm(keyTermNode.path(TAConstants.InteractionsQuery.keyterm).asText());

				// Relations Positions and Sentiment
				keyTermHighlight.setSentiments(this.getSentimentsHighlights(keyTermNode));
				keyTermHighlight.setStarts(keyTermNode.path(TAConstants.InteractionsQuery.positionStart).asInt());
				keyTermHighlight.setEnds(keyTermNode.path(TAConstants.InteractionsQuery.positionEnds).asInt());

				keyTermsHighlights.add(keyTermHighlight);
			}
		}

		return keyTermsHighlights;
	}

	private List<EntityHighlight> topicsLayerHandler(JsonNode layersMarking) {
		List<EntityHighlight> topicHighlights = null;
		EntityHighlight topicHighlight;

		// query TopicLayer handler
		JsonNode topicsLayer = layersMarking.path(TAConstants.InteractionsQuery.topicsHighlightsLayer);
		if (!topicsLayer.isMissingNode()) {

			for (JsonNode topicNode : topicsLayer) {
				if (topicHighlights == null) {
					topicHighlights = new ArrayList<EntityHighlight>();
				}

				topicHighlight = new EntityHighlight();
				topicHighlight.setTopic(topicNode.path("topic").asText());
				topicHighlight.setStarts(topicNode.path(TAConstants.InteractionsQuery.positionStart).asInt());
				topicHighlight.setEnds(topicNode.path(TAConstants.InteractionsQuery.positionEnds).asInt());

				topicHighlight.setSentiments(this.getSentimentsHighlights(topicNode));
				topicHighlights.add(topicHighlight);
			}
		}

		return topicHighlights;
	}

	private List<SentimentHighlight> sentimentHighlightsHandler(JsonNode layersMarking) {

		List<SentimentHighlight> sentimentHighlights = null;
		SentimentHighlight highlight;
		List<Position> sentimentPositions = null;
		Position sentimentPosition;

		JsonNode highlightsNode = layersMarking.path(TAConstants.InteractionsQuery.sentimentHighlightsLayer);
		if (!highlightsNode.isMissingNode()) {
			for (JsonNode sentimentHighlightNode : highlightsNode) {

				if (sentimentHighlights == null) {
					sentimentHighlights = new ArrayList<>();
				}

				highlight = new SentimentHighlight();
				highlight.setValue(sentimentHighlightNode.path("value").asInt());

				JsonNode positionsLayer = sentimentHighlightNode.path(TAConstants.InteractionsQuery.positions);
				if (!positionsLayer.isMissingNode()) {

					sentimentPositions = new ArrayList<>();
					for (JsonNode positionNode : positionsLayer) {

						sentimentPosition = new Position();
						sentimentPosition.setStarts(positionNode.path(TAConstants.InteractionsQuery.positionStart).asInt());
						sentimentPosition.setEnds(positionNode.path(TAConstants.InteractionsQuery.positionEnds).asInt());
						sentimentPositions.add(sentimentPosition);
					}
				} else {
					sentimentPositions = null;
				}

				highlight.setPositions(sentimentPositions);
				sentimentHighlights.add(highlight);
			}
		}
		return sentimentHighlights;
	}

	private List<TermHighlight> termsLayerHandler(JsonNode layersMarking) {
		List<TermHighlight> termHighlights = null;
		TermHighlight highlight;

		// query TermsLayer handler
		JsonNode termsHighlights = layersMarking.path(TAConstants.InteractionsQuery.queryTermsHighlightsLayer);
		if (!termsHighlights.isMissingNode()) {

			for (JsonNode termHighlight : termsHighlights) {
				if (termHighlights == null) {
					termHighlights = new ArrayList<TermHighlight>();
				}

				highlight = new TermHighlight();
				highlight.setTerm(termHighlight.path(TAConstants.InteractionsQuery.term).asText());
				highlight.setStarts(termHighlight.path(TAConstants.InteractionsQuery.solrPositionStart).asInt());
				highlight.setEnds(termHighlight.path(TAConstants.InteractionsQuery.positionEnds).asInt());
				termHighlights.add(highlight);
			}
		}

		return termHighlights;
	}

	private List<SentimentHighlight> getSentimentsHighlights(JsonNode element) {
		List<SentimentHighlight> sentimentHighlights = null;
		List<Position> sentimentPositions = null;

		JsonNode sentimentsLayer = element.path(TAConstants.InteractionsQuery.sentiments);
		if (!sentimentsLayer.isMissingNode()) {
			for (JsonNode sentimnentLayerNode : sentimentsLayer) {

				// ensure allocation
				if (sentimentHighlights == null) {
					sentimentHighlights = new ArrayList<>();
				}

				SentimentHighlight sentimentHighlight = new SentimentHighlight();
				sentimentHighlight.setValue(sentimnentLayerNode.path("value").asInt());

				sentimentPositions = null;
				JsonNode positionsLayer = sentimnentLayerNode.path(TAConstants.InteractionsQuery.positions);

				if (!positionsLayer.isMissingNode()) {

					for (JsonNode positionNode : positionsLayer) {
						if (sentimentPositions == null) {
							sentimentPositions = new ArrayList<>();
						}

						Position sentimentPosition = new Position();
						sentimentPosition.setStarts(positionNode.path(TAConstants.InteractionsQuery.positionStart).asInt());
						sentimentPosition.setEnds(positionNode.path(TAConstants.InteractionsQuery.positionEnds).asInt());
						sentimentPositions.add(sentimentPosition);
					}
				}

				sentimentHighlight.setPositions(sentimentPositions);
				sentimentHighlights.add(sentimentHighlight);
			}
		}

		return sentimentHighlights;
	}

	private List<Position> getHighlightPositions(JsonNode element) {
		List<Position> positions = new ArrayList<>();

		JsonNode positionsEl = element.path(TAConstants.InteractionsQuery.positions);
		if (!positionsEl.isMissingNode()) {
			for (JsonNode position : positionsEl) {

				// @formatter:off
				positions.add(new Position().setStarts(position.path(TAConstants.InteractionsQuery.positionStart).asInt())
				                            .setEnds(position.path(TAConstants.InteractionsQuery.positionEnds).asInt())
				);
				// @formatter:on

			}
		}

		return positions;
	}


}
