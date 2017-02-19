package com.verint.textanalytics.dal.darwin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionErrorCode;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionException;
import com.verint.textanalytics.common.utils.CollectionUtils;
import com.verint.textanalytics.common.utils.DataUtils;
import com.verint.textanalytics.common.utils.JSONUtils;
import com.verint.textanalytics.common.utils.StringUtils;
import com.verint.textanalytics.model.analyze.FieldMetric;
import com.verint.textanalytics.model.analyze.MetricData;
import com.verint.textanalytics.model.analyze.TextElementSentimentsMetric;
import com.verint.textanalytics.model.documentSchema.DocumentHierarchyType;
import com.verint.textanalytics.model.documentSchema.TextSchemaField;
import com.verint.textanalytics.model.facets.*;
import com.verint.textanalytics.model.interactions.*;
import com.verint.textanalytics.model.modelEditor.Domain;
import com.verint.textanalytics.model.modelEditor.Language;
import com.verint.textanalytics.model.trends.InteractionDailyVolumeDataPoint;
import com.verint.textanalytics.model.trends.TextElementTrend;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/**
 * @author EZlotnik Converts response of Solr to application model.
 */

/**
 * @author YHemi
 *
 */

public class SolrResponseConverter implements ResponseConverter {

	private static final String SLASH = "/";

	private static final String value = "val";

	private static final String buckets_str = "buckets";

	@Getter
	@Setter
	@Accessors(chain = true)
	@Autowired
	protected TextEngineSchemaService textEngineConfigurationService;

	private Logger logger = LogManager.getLogger(this.getClass());

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<FieldMetric> metricFields;

	/**
	 * Constructor.
	 */
	public SolrResponseConverter() {
		logger.debug("Initializing {}", this.getClass());
	}

	/**
	 * C'tor.
	 * @param metricFields
	 *            list of metrics to read from facet.
	 */
	public SolrResponseConverter(List<FieldMetric> metricFields) {
		this.metricFields = metricFields;
	}

	/**
	 * Converts Solr response to Interaction.
	 * @param searchDocumentJson json of documents to be converted
	 * @param tenant tenant for which data is being requested
	 * @param channel channel for which data is being requested
	 * @return interaction
	 */
	@Override
	public SearchInteractionsResult getInteractions(String searchDocumentJson, String tenant, String channel) {
		Boolean exceptionWrittern = false;
		val searchResult = new SearchInteractionsResult();
		val lstDocuments = new ArrayList<Interaction>();

		try {

			if (!StringUtils.isNullOrBlank(searchDocumentJson)) {

				ObjectMapper mapper = new ObjectMapper();
				JsonNode rootNode = mapper.readTree(searchDocumentJson);

				this.checkResponseStatus(rootNode);

				JsonNode responseNode = rootNode.path("response");
				JsonNode docsNode = responseNode.path("docs");

				searchResult.setTotalNumberFound(responseNode.path("numFound").asInt(0));
				searchResult.setMaxScore(responseNode.path("maxScore").asDouble(0));

				if (!docsNode.isMissingNode() && docsNode.isArray()) {

					// iterate over doc
					Interaction doc = null;

					// get list of dynamic fields from configuration, those fields are specific for channel and tenant
					List<TextSchemaField> channelDynamicFields = this.textEngineConfigurationService.getChannelDynamicFields(tenant, channel);

					for (JsonNode docNode : docsNode) {

						try {
							doc = new Interaction();
							doc.setId(docNode.path(TAConstants.SchemaFieldNames.documentId).asText(""));
							doc.setTenant(docNode.path(TAConstants.SchemaFieldNames.tenant).asText(""));
							doc.setChannel(docNode.path(TAConstants.SchemaFieldNames.channel).asText(""));
							doc.setIsSentimentMixed(docNode.path(TAConstants.SchemaFieldNames.interactionSentimentIsMixed).asBoolean(false));
							doc.setContentType(DocumentContentType.valueOf(docNode.path(TAConstants.SchemaFieldNames.content_type).asText("").toUpperCase()));

							doc.setLanguage(docNode.path(TAConstants.SchemaFieldNames.language).asText(""));

							// source type : Chat, Email ...
							doc.setSourceType(SourceType.toSourceType(docNode.path(TAConstants.SchemaFieldNames.sourceType).asText(TAConstants.sourceTypeUnknown)));

							// interaction date, agent local start time, customer local start time
							doc.setStartTime(DataUtils.getDateFromISO8601String(docNode.path(TAConstants.SchemaFieldNames.parentDate).asText("")));

							doc.setAgentLocalStartTime(DataUtils.getDateFromISO8601String(docNode.path(TAConstants.SchemaFieldNames.agentLocalStartTime).asText("")));
							doc.setAgentTimeZone(docNode.path(TAConstants.SchemaFieldNames.agentTimeZone).asText(""));

							// read Agent Names
							JsonNode agentNamesNode = docNode.path(TAConstants.SchemaFieldNames.agentNames);
							if (!agentNamesNode.isMissingNode() && agentNamesNode.isArray() && agentNamesNode.size() > 0) {
								val agentNameArr = new ArrayList<String>();

								for (JsonNode agentNameNode : agentNamesNode) {
									agentNameArr.add(agentNameNode.asText(""));
								}
								doc.setAgentNames(agentNameArr);
							}

							doc.setAgentMessagesCount(docNode.path(TAConstants.SchemaFieldNames.agentMessagesCount).asInt(0));
							doc.setAgentAvgResponseTime(docNode.path(TAConstants.SchemaFieldNames.agentAvgResponseTime).asInt(0));

							doc.setCustomerLocalStartTime(DataUtils.getDateFromISO8601String(docNode.path(TAConstants.SchemaFieldNames.customerLocalStartTime).asText("")));

							doc.setCustomerTimeZone(docNode.path(TAConstants.SchemaFieldNames.customerTimeZone).asText(""));

							// Customer names
							JsonNode customerNamesNode = docNode.path(TAConstants.SchemaFieldNames.customerNames);
							if (!customerNamesNode.isMissingNode() && customerNamesNode.isArray() && customerNamesNode.size() > 0) {
								val customerNameArr = new ArrayList<String>();

								for (JsonNode customerName : customerNamesNode) {
									customerNameArr.add(customerName.asText(""));
								}
								doc.setCustomerNames(customerNameArr);
							}

							doc.setCustomerMessagesCount(docNode.path(TAConstants.SchemaFieldNames.customerMessagesCount).asInt(0));
							doc.setCustomerAvgResponseTime(docNode.path(TAConstants.SchemaFieldNames.customerAvgResponseTime).asInt(0));

							doc.setNumberOfRobotMessages(docNode.path(TAConstants.SchemaFieldNames.numberOfRobotMessages).asInt(0));

							doc.setMessagesCount(docNode.path(TAConstants.SchemaFieldNames.messagesCount).asInt(0));
							doc.setHandleTime(docNode.path(TAConstants.SchemaFieldNames.handleTime).asInt(0));

							// score
							doc.setRelevancyScore(docNode.path(TAConstants.SchemaFieldNames.relevancyScore).asDouble(1));

							// sentiment conversion
							doc.setSentiment(SentimentType.toSentimentType(docNode.path(TAConstants.SchemaFieldNames.interactionSentiment).asInt(0)));

							// read Categories element from json node
							JsonNode categotiesNode = docNode.path(TAConstants.SchemaFieldNames.categories);
							if (!categotiesNode.isMissingNode() && categotiesNode.isArray() && categotiesNode.size() > 0) {
								List<CategoryTagging> lstCategories = new ArrayList<>();

								for (JsonNode categoryNode : categotiesNode) {
									lstCategories.add(new CategoryTagging().setId(categoryNode.asText("")));
								}

								doc.setCategories(lstCategories);
							}

							doc.setSourceSpecificFields(this.getInteractionSourceTypeSpecificFields(docNode, doc.getSourceType()));
							doc.setSubject(docNode.path(TAConstants.SchemaFieldNames.subject).asText(""));

							doc.setDynamicFields(this.getInteractionDynamicFields(docNode, tenant, channel, channelDynamicFields));

							JsonNode childDocumentsNode = docNode.path("_childDocuments_");
							if (!childDocumentsNode.isMissingNode()) {
								doc.setUtterances(this.getInteractionUtterances(childDocumentsNode, tenant, channel, channelDynamicFields));
							}

							lstDocuments.add(doc);
						} catch (Exception ex) {
							// write exception only once, as writing multiple
							// exceptions slows down the response
							if (!exceptionWrittern) {
								logger.error("Failed to generate interaction from JSON for {}. Error - {}", docNode.toString(), ex);

								exceptionWrittern = true;
							}
						}
					}
				}
			}

			searchResult.setInteractions(lstDocuments);

		} catch (Exception ex) {
			throw new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.JsonResponseParsingError).put(TAConstants.ErrorDataKeys.solrJsonResponse, searchDocumentJson);
		}

		return searchResult;
	}


	private List<DynamicField> getInteractionDynamicFields(JsonNode docElem, String tenant, String channel, List<TextSchemaField> lstChannelDynamicFields) {
		List<DynamicField> documentDynamicFields = new ArrayList<DynamicField>();

		if (lstChannelDynamicFields != null) {
			lstChannelDynamicFields.stream().filter(f -> f.getDocumentHierarchyType() == DocumentHierarchyType.Interaction).forEach(channelDynamicField -> {

				String dynamicFieldName = channelDynamicField.getName();
				if (docElem.has(dynamicFieldName)) {
					DynamicField documentDynamicField = new DynamicField();
					documentDynamicField.setName(dynamicFieldName);
					documentDynamicField.setType(DynamicFieldDataType.fromElementName(dynamicFieldName));
					documentDynamicField.setValue(DynamicFieldDataType.getValue(docElem, dynamicFieldName));
					documentDynamicFields.add(documentDynamicField);
				}
			});
		}

		return documentDynamicFields;
	}

	private List<DynamicField> getUtteranceDynamicFields(JsonNode docElem, String tenant, String channel, List<TextSchemaField> lstChannelDynamicFields) {
		List<DynamicField> documentDynamicFields = new ArrayList<DynamicField>();

		if (lstChannelDynamicFields != null) {
			lstChannelDynamicFields.stream().filter(f -> f.getDocumentHierarchyType() == DocumentHierarchyType.Interaction).forEach(channelDynamicField -> {
				String dynamicFieldName = channelDynamicField.getName();

				if (docElem.has(dynamicFieldName)) {
					DynamicField documentDynamicField = new DynamicField();
					documentDynamicField.setName(dynamicFieldName);
					documentDynamicField.setType(DynamicFieldDataType.fromElementName(dynamicFieldName));
					documentDynamicField.setValue(DynamicFieldDataType.getValue(docElem, dynamicFieldName));
					documentDynamicFields.add(documentDynamicField);
				}
			});
		}

		return documentDynamicFields;
	}

	private List<DynamicField> getInteractionSourceTypeSpecificFields(JsonNode docElem, SourceType sourceType) {
		List<DynamicField> sourceSpecificFields = new ArrayList<DynamicField>();
		List<TextSchemaField> schemaSourceTypeFields = null;

		switch (sourceType) {
			case Chat:
				schemaSourceTypeFields = textEngineConfigurationService.getSourceTypeTextSchemaFields(sourceType);
				break;
			case Email:
				schemaSourceTypeFields = textEngineConfigurationService.getSourceTypeTextSchemaFields(sourceType);
				break;
			case Unknown:
				break;
			default:
				break;
		}

		if (schemaSourceTypeFields != null) {
			schemaSourceTypeFields.stream().filter(f -> f.getDocumentHierarchyType() == DocumentHierarchyType.Interaction).forEach(sourceTypeField -> {
				String fieldName = sourceTypeField.getName();

				if (docElem.has(fieldName)) {
					DynamicField documentDynamicField = new DynamicField();
					documentDynamicField.setName(fieldName);
					documentDynamicField.setType(DynamicFieldDataType.fromElementName(fieldName));
					documentDynamicField.setValue(DynamicFieldDataType.getValue(docElem, fieldName));
					sourceSpecificFields.add(documentDynamicField);
				}
			});
		}

		return sourceSpecificFields;
	}

	protected List<Utterance> getInteractionUtterances(JsonNode childDocumentsNode, String tenant, String channel, List<TextSchemaField> channelDynamicFields) {

		List<Utterance> utterances = new ArrayList<Utterance>();

		if (!childDocumentsNode.isMissingNode() &&  childDocumentsNode.isArray()) {

			Utterance utterance = null;

			for (JsonNode childDocumentNode : childDocumentsNode) {
				utterance = new Utterance();

				utterance.setId(childDocumentNode.path(TAConstants.SchemaFieldNames.documentId).asText(""));
				utterance.setLanguage(childDocumentNode.path(TAConstants.SchemaFieldNames.language).asText(""));
				utterance.setParentId(childDocumentNode.path(TAConstants.SchemaFieldNames.parentId).asText(""));
				utterance.setDocumentDynamicFields(this.getUtteranceDynamicFields(childDocumentNode, tenant, channel, channelDynamicFields));
				utterance.setContentType(DocumentContentType.valueOf(childDocumentNode.path(TAConstants.SchemaFieldNames.content_type).asText("").toUpperCase()));
				utterance.setEntities(this.getEntities(childDocumentNode.path(TAConstants.SchemaFieldNames.topics)));
				utterance.setRelations(this.getRelations(childDocumentNode.path(TAConstants.SchemaFieldNames.relations)));
				utterance.setKeyterms(this.getKeyTerms(childDocumentNode.path(TAConstants.SchemaFieldNames.keyterms)));

				JsonNode sentementNode = childDocumentNode.path(TAConstants.SchemaFieldNames.utteranceSentiment);
				if (!sentementNode.isMissingNode()) {
					SentimentType utteranceSentiment = SentimentType.toSentimentType(sentementNode.asInt(0));
					if (utteranceSentiment == SentimentType.Neutral) {
						//Check if utterance sentiment is mixed or not and if not we need to ignore sentiment
						if (childDocumentNode.path(TAConstants.SchemaFieldNames.utteranceSentimentIsMixed).asBoolean()) {
							utterance.setUtteranceSentiment(utteranceSentiment);
						}

					} else {
						utterance.setUtteranceSentiment(utteranceSentiment);
					}
				}

				JsonNode textNode = childDocumentNode.path("text_" + utterance.getLanguage());
				if (!textNode.isMissingNode() && textNode.isArray() && textNode.size() > 0) {
					utterance.setText(textNode.get(0).asText());
				}

				utterance.setDate(DataUtils.getDateFromISO8601String(childDocumentNode.path(TAConstants.SchemaFieldNames.childDate).asText("")));
				utterance.setSpeakerType(SpeakerType.toSpeakerType(childDocumentNode.path(TAConstants.SchemaFieldNames.speakerType).asText("")));

				utterances.add(utterance);
			}
		}

		return utterances;
	}

	private List<UtteranceHighlights> getUtterancesHighlights(JsonNode rootElem) {

		List<UtteranceHighlights> utterancesHighlights = new ArrayList<>();

		try {
			JsonNode highlightingElem = rootElem.path("highlighting");
			if (!highlightingElem.isMissingNode()) {

				Iterator<Map.Entry<String, JsonNode>> utterances = highlightingElem.fields();
				while (utterances.hasNext()) {
					Map.Entry<String, JsonNode> utteranceEntry = utterances.next();

					// create new SubDocument Highlighting
					UtteranceHighlights utteranceHighlight = new UtteranceHighlights();
					utteranceHighlight.setDocumentId(utteranceEntry.getKey());

					JsonNode utteranceNode = utteranceEntry.getValue();
					if (!utteranceNode.isMissingNode()) {
						Iterator<Map.Entry<String, JsonNode>> textFieldEntries = utteranceNode.fields();
						while (textFieldEntries.hasNext()) {
							Map.Entry<String, JsonNode> textFieldEntry = textFieldEntries.next();

							List<TermHighlight> highlights = null;
							JsonNode termsNode  = textFieldEntry.getValue();

							if (!termsNode.isMissingNode() && termsNode.isArray()) {
								highlights = new ArrayList<TermHighlight>();

								for (JsonNode termNode : termsNode) {
									TermHighlight highlight = new TermHighlight();
									highlight.setTerm(termNode.path(TAConstants.InteractionsQuery.term).asText());
									highlight.setStarts(termNode.path(TAConstants.InteractionsQuery.solrPositionStart).asInt());
									highlight.setEnds(termNode.path(TAConstants.InteractionsQuery.positionEnds).asInt());

									highlights.add(highlight);
								}
							}

							utteranceHighlight.setTermHighlights(highlights);
						}
					}

					utterancesHighlights.add(utteranceHighlight);
				}
			}
		} catch (Exception ex) {
			throw new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.JsonResponseParsingError)
					.put(TAConstants.ErrorDataKeys.solrJsonResponse, rootElem.toString());
		}

		return utterancesHighlights;
	}

	private List<Entity> getEntities(JsonNode topicsNode) {
		Entity entity = null;
		List<Entity> entities = new ArrayList<Entity>();

		if (topicsNode != null && !topicsNode.isMissingNode() && topicsNode.isArray()) {
			String topicValue;

			for (JsonNode topicNode : topicsNode) {
				topicValue = topicNode.asText();

				// remove the location payload, split on "/" and take the last token
				String[] entityHierarchy = StringUtils.splitAndRemoveEmpty(topicValue.replaceAll("(\\|.*)", ""), SLASH);
				if (entityHierarchy != null && entityHierarchy.length > 0) {
					entity = new Entity();
					entity.setValue(topicValue);
					entity.setLevelNumber(entityHierarchy.length);
					entity.setName(entityHierarchy[entityHierarchy.length - 1]);
					entities.add(entity);
				}
			}
		}

		return entities;
	}

	private List<Relation> getRelations(JsonNode relationsNode) {
		Relation relation = null;
		List<Relation> relationsToReturn = new ArrayList<Relation>();

		if (relationsNode != null && !relationsNode.isMissingNode() && relationsNode.isArray()) {
			String relationValue;

			for (JsonNode relationNode : relationsNode) {
				relationValue = relationNode.asText();

				if (!StringUtils.isNullOrBlank(relationValue)) {
					// remove the location payload, split on "/" and take the last token
					String[] entityHierarchy = StringUtils.splitAndRemoveEmpty(relationValue.replaceAll("(\\|.*)", ""), SLASH);
					if (entityHierarchy != null && entityHierarchy.length > 0) {
						relation = new Relation();
						relation.setValue(relationValue);
						relation.setLevelNumber(entityHierarchy.length);
						relation.setName(entityHierarchy[entityHierarchy.length - 1]);
						relationsToReturn.add(relation);
					}
				}
			}
		}

		return relationsToReturn;
	}

	private List<KeyTerm> getKeyTerms(JsonNode keyTermsNode) {
		KeyTerm keyTerm = null;
		List<KeyTerm> keyTermsToReturn = new ArrayList<KeyTerm>();

		if (keyTermsNode != null && !keyTermsNode.isMissingNode() && keyTermsNode.isArray()) {
			String keyTermValue;

			for (JsonNode relationNode : keyTermsNode) {
				keyTermValue = relationNode.asText();

				if (!StringUtils.isNullOrBlank(keyTermValue)) {
					// remove the location payload, split on "/" and take the last token
					String[] entityHierarchy = StringUtils.splitAndRemoveEmpty(keyTermValue.replaceAll("(\\|.*)", ""), SLASH);
					if (entityHierarchy != null && entityHierarchy.length > 0) {
						keyTerm = new KeyTerm();
						keyTerm.setValue(keyTermValue);
						keyTerm.setLevelNumber(entityHierarchy.length);
						keyTerm.setName(entityHierarchy[entityHierarchy.length - 1]);
						keyTermsToReturn.add(keyTerm);
					}
				}
			}
		}

		return keyTermsToReturn;
	}

	/**
	 * @param textElementFacetsResponse Responded JSON string to parse
	 * @param facetNames names of facet to read
	 * @param withStats should stats be extracted from response
	 * @param leavesOnly is the response of leaves only request
	 * @return List of TextElement facet nodes
	 */
	@Override
	public List<TextElementsFacetNode> getTextElementsFacets(String textElementFacetsResponse, List<String> facetNames, Boolean withStats, Boolean leavesOnly) {
		val lstTextElements = new ArrayList<TextElementsFacetNode>();

		try {
			if (!StringUtils.isNullOrBlank(textElementFacetsResponse)) {

				ObjectMapper mapper = new ObjectMapper();
				JsonNode rootNode = mapper.readTree(textElementFacetsResponse);

				this.checkResponseStatus(rootNode);

				JsonNode facetsNode = rootNode.path(TAConstants.FacetQuery.facets);

				for (String facetName : facetNames) {
					JsonNode bucketsNode = facetsNode.path(facetName).path(TAConstants.FacetQuery.bucketsAlias);
					if (!bucketsNode.isMissingNode() && bucketsNode.isArray()) {

						MetricData metricData;
						// iterate over facets element and add topics with
						// count and percentage data
						for (JsonNode facetNode : bucketsNode) {

							TextElementsFacetNode textElementNode = TextElementsFacetNode.buildFromPathString(facetNode.path(TAConstants.FacetQuery.bucketTitleAlias).asText(), leavesOnly);
							textElementNode.setNumberOfInteractions(facetNode.path(TAConstants.FacetQuery.interactionsCountAlias).asInt(0));

							if (withStats) {
								for (FieldMetric metricField : this.metricFields) {
									JsonNode metricNode = facetNode.path(metricField.getName());
									if (!metricNode.isMissingNode()) {
										metricData = new MetricData();
										// facet level
										metricData.setName(metricField.getName());
										metricData.setValue(metricNode.asDouble(0.0));
										textElementNode.getMetrics().add(metricData);
									}
								}
							}

							// add elements to lstTextElements list
							lstTextElements.add(textElementNode);
						}
					}
				}
			}
		} catch (Exception ex) {
			throw new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.JsonResponseParsingError).put(TAConstants.ErrorDataKeys.solrJsonResponse,
			                                                                                                    textElementFacetsResponse);
		}

		return lstTextElements;
	}

	@Override
	public List<TextElementsFacetNode> getTextElementsMetrics(String textElementMetricsResponse, Map<String, String> facetAliases) {
		List<TextElementsFacetNode> textElements = null;

		try {
			if (!StringUtils.isNullOrBlank(textElementMetricsResponse)) {
				ObjectMapper mapper = new ObjectMapper();
				JsonNode rootNode = mapper.readTree(textElementMetricsResponse);

				this.checkResponseStatus(rootNode);

				JsonNode facetsNode = rootNode.path(TAConstants.FacetQuery.facets);
				if (!facetsNode.isMissingNode()) {
					textElements = new ArrayList<>();

					Iterator<Map.Entry<String, JsonNode>> iter = facetsNode.fields();
					while (iter.hasNext()) {
						Map.Entry<String, JsonNode> currentEntry = iter.next();

						JsonNode facetNode = currentEntry.getValue();
						String  facetAlias = currentEntry.getKey();

						// search for facet alias in mapping
						if (facetAliases.containsKey(facetAlias)) {
							String textElementValue = facetAliases.get(facetAlias);
							TextElementsFacetNode textElementMetrics = TextElementsFacetNode.buildFromPathString(textElementValue, false);

							JsonNode countsNode = facetNode.path(TAConstants.FacetQuery.interactionsCountAlias);
							textElementMetrics.setNumberOfInteractions(countsNode.asInt());

							// read metrics
							for (FieldMetric metricField : this.metricFields) {
								JsonNode metricJsonNode = facetNode.path(metricField.getName());

								if (!metricJsonNode.isMissingNode()) {

									MetricData metricData = new MetricData();
									// facet level
									metricData.setName(metricField.getName());
									metricData.setValue(metricJsonNode.asDouble(0.0));
									textElementMetrics.addMetric(metricData);
								}
							}

							textElements.add(textElementMetrics);
						}
					}
				}
			}
		} catch (Exception ex) {
			throw new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.JsonResponseParsingError).put(TAConstants.ErrorDataKeys.solrJsonResponse,
			                                                                                                    textElementMetricsResponse);
		}

		return textElements;
	}

	@Override
	public TextElementsFacetNode getTextElementsFacetPath(String textElementFacetPathResponse, List<TextElementType> textElements, boolean utteranceLevelMode) {

		if (utteranceLevelMode) {
			return this.getTextElementsFacetPathUtteranceLevel(textElementFacetPathResponse, textElements);
		} else {
			return this.getTextElementsFacetPathInteractionLevel(textElementFacetPathResponse, textElements);
		}
	}

	private TextElementsFacetNode getTextElementsFacetPathInteractionLevel(String textElementFacetPathResponse, List<TextElementType> textElements) {

		List<TextElementsFacetNode> result = null;
		TextElementsFacetNode rootElement = TextElementsFacetNode.buildFromPathString("/root", false);

		if (!StringUtils.isNullOrBlank(textElementFacetPathResponse)) {
			JSONTokener tokener = new JSONTokener(textElementFacetPathResponse);
			JSONObject root = new JSONObject(tokener);

			this.checkResponseStatus(root);

			JSONObject facetsEl = JSONUtils.getJSONObject(root, TAConstants.FacetQuery.facets);

			// get total count
			int totalUtterances = facetsEl.getInt("interactions");

			rootElement.setNumberOfInteractions(totalUtterances);

			if (facetsEl != null) {

				// get the first 
				String textElementSchemaFieldName = "";
				switch (textElements.get(0)) {
					case Entities:
						textElementSchemaFieldName = TAConstants.SchemaFieldNames.topics_f;
						break;
					case Relations:
						textElementSchemaFieldName = TAConstants.SchemaFieldNames.relations_f;
						break;
					default:
						throw new IllegalArgumentException();
				}

				// get first relations_f or topics_f
				JSONObject firstTextElement = JSONUtils.getJSONObject(facetsEl, textElementSchemaFieldName);

				JSONArray buckets = JSONUtils.getJSONArray(firstTextElement, buckets_str);
				result = loadTextElementsFromBacketsInteractionLevelRecursive(buckets, textElements.subList(1, textElements.size()));
			}

		}

		rootElement.setChildren(result);
		return rootElement;

	}

	private List<TextElementsFacetNode> loadTextElementsFromBacketsInteractionLevelRecursive(JSONArray buckets, List<TextElementType> textElements) {

		List<TextElementsFacetNode> result = new ArrayList<TextElementsFacetNode>();

		try {

			if (buckets != null) {
				for (int i = 0; i < buckets.length(); i++) {

					JSONObject bucket = buckets.getJSONObject(i);
					TextElementsFacetNode elemenToAdd = TextElementsFacetNode.buildFromPathString(bucket.getString("val"), false);

					if (textElements.size() > 0) {

						JSONObject utterancesSection = JSONUtils.getJSONObject(bucket, "getInteractionParent").getJSONObject("getUtterances");
						elemenToAdd.setNumberOfInteractions(utterancesSection.getInt("interactions"));

						// get the first text element
						String textElementSchemaFieldName = "";
						switch (textElements.get(0)) {
							case Entities:
								textElementSchemaFieldName = TAConstants.SchemaFieldNames.topics_f;
								break;
							case Relations:
								textElementSchemaFieldName = TAConstants.SchemaFieldNames.relations_f;
								break;
							default:
								throw new IllegalArgumentException("textElement is not defined");
						}

						// get first relations_f or topics_f
						JSONObject firstTextElement = JSONUtils.getJSONObject(utterancesSection, textElementSchemaFieldName);

						JSONArray innerBuckets = JSONUtils.getJSONArray(firstTextElement, buckets_str);

						if (innerBuckets != null && innerBuckets.length() > 0) {

							List<TextElementType> textElementSubList = textElements.subList(1, textElements.size());
							List<TextElementsFacetNode> children = loadTextElementsFromBacketsInteractionLevelRecursive(innerBuckets, textElementSubList);

							elemenToAdd.setChildren(children);
						}
					} else {
						elemenToAdd.setNumberOfInteractions(bucket.getInt("interactions"));
					}
					result.add(elemenToAdd);
				}
			}

		} catch (Exception ex) {
			throw new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.JsonResponseParsingError);
		}

		return result;
	}

	private TextElementsFacetNode getTextElementsFacetPathUtteranceLevel(String textElementFacetPathResponse, List<TextElementType> textElements) {

		List<TextElementsFacetNode> result = null;
		TextElementsFacetNode rootElement = TextElementsFacetNode.buildFromPathString("/root", false);

		if (!StringUtils.isNullOrBlank(textElementFacetPathResponse)) {
			JSONTokener tokener = new JSONTokener(textElementFacetPathResponse);
			JSONObject root = new JSONObject(tokener);

			this.checkResponseStatus(root);

			JSONObject facetsEl = JSONUtils.getJSONObject(root, TAConstants.FacetQuery.facets);

			// get total count
			int totalUtterances = facetsEl.getInt("count");

			rootElement.setNumberOfInteractions(totalUtterances);

			if (facetsEl != null) {

				// get the first 
				String textElementSchemaFieldName = "";
				switch (textElements.get(0)) {
					case Entities:
						textElementSchemaFieldName = TAConstants.SchemaFieldNames.topics_f;
						break;
					case Relations:
						textElementSchemaFieldName = TAConstants.SchemaFieldNames.relations_f;
						break;
					default:
						throw new IllegalArgumentException("textElement is not defined");
				}

				// get first relations_f or topics_f
				JSONObject firstTextElement = JSONUtils.getJSONObject(facetsEl, textElementSchemaFieldName);

				JSONArray buckets = JSONUtils.getJSONArray(firstTextElement, buckets_str);
				result = loadTextElementsFromBacketsRecursive(buckets, textElements.subList(1, textElements.size()));
			}

		}

		rootElement.setChildren(result);
		return rootElement;

	}

	private List<TextElementsFacetNode> loadTextElementsFromBacketsRecursive(JSONArray buckets, List<TextElementType> textElements) {
		List<TextElementsFacetNode> result = new ArrayList<TextElementsFacetNode>();

		try {

			if (buckets != null) {
				for (int i = 0; i < buckets.length(); i++) {

					JSONObject bucket = buckets.getJSONObject(i);
					TextElementsFacetNode elemenToAdd = TextElementsFacetNode.buildFromPathString(bucket.getString("val"), false);
					elemenToAdd.setNumberOfInteractions(bucket.getInt("count"));

					if (textElements.size() > 0) {
						// get the first text element
						String textElementSchemaFieldName = "";
						switch (textElements.get(0)) {
							case Entities:
								textElementSchemaFieldName = TAConstants.SchemaFieldNames.topics_f;
								break;
							case Relations:
								textElementSchemaFieldName = TAConstants.SchemaFieldNames.relations_f;
								break;
							default:
								throw new IllegalArgumentException("textElement is not defined");
						}

						// get first relations_f or topics_f
						JSONObject firstTextElement = JSONUtils.getJSONObject(bucket, textElementSchemaFieldName);

						JSONArray innerBuckets = JSONUtils.getJSONArray(firstTextElement, buckets_str);

						if (innerBuckets != null && innerBuckets.length() > 0) {

							List<TextElementType> textElementSubList = textElements.subList(1, textElements.size());
							List<TextElementsFacetNode> children = loadTextElementsFromBacketsRecursive(innerBuckets, textElementSubList);

							elemenToAdd.setChildren(children);
						}
					}
					result.add(elemenToAdd);
				}
			}

		} catch (Exception ex) {
			throw new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.JsonResponseParsingError);
		}

		return result;
	}


	@Override
	public Facet getFacet(String searchFacetsResponse, String tenant, String channel, String facetField, List<FieldMetric> metricsToRead) {
		Facet facetResults = new Facet();

		try {
			TextSchemaField textSchemeField = this.textEngineConfigurationService.getTextSchemaField(tenant, channel, facetField);

			String countInteractionsElName = "";
			if (this.textEngineConfigurationService.isParentDocumentField(tenant, channel, facetField)) {
				// for facet which runs on interactions level the interactions count generated as "count" element
				countInteractionsElName = TAConstants.FacetQuery.interactionsCountAlias;
			} else {
				// for facet which runs on utterance level the interactions count generated as "interactions_count" element
				countInteractionsElName = TAConstants.FacetQuery.interactionsCountStatAlias;
			}

			facetResults.setValuesDataType(textSchemeField.getFieldDataType());
			facetResults.setFieldName(textSchemeField.getName());
			facetResults.setType(FacetType.SingleValues);

			Function<JsonNode, String> getValueFunc;

			switch (textSchemeField.getFieldDataType()) {
				case Text:
				case Constant:
				case Date:
				case Boolean:
					getValueFunc = (JsonNode elem) -> elem.path(TAConstants.FacetQuery.bucketTitleAlias).asText("");
					break;
				case Int:
					getValueFunc = (JsonNode elem) -> String.valueOf(elem.path(value).asInt(0));
					break;
				case Long:
					getValueFunc = (JsonNode elem) -> String.valueOf(elem.path(TAConstants.FacetQuery.bucketTitleAlias).asLong(0));
					break;
				default:
					getValueFunc = (JsonNode elem) -> elem.path(TAConstants.FacetQuery.bucketTitleAlias).asText("");
					break;
			}

			if (!StringUtils.isNullOrBlank(searchFacetsResponse)) {

				ObjectMapper mapper = new ObjectMapper();
				JsonNode rootNode = mapper.readTree(searchFacetsResponse);

				this.checkResponseStatus(rootNode);

				JsonNode facetsNode = rootNode.path(TAConstants.FacetQuery.facets);
				JsonNode countNode = facetsNode.path(TAConstants.FacetQuery.interactionsCountAlias);
				if (!countNode.isMissingNode()) {
					facetResults.setTotalCount(countNode.asInt(0));
				}

				JsonNode buckets = facetsNode.path(String.format("facetFor%s", facetField)).path(TAConstants.FacetQuery.bucketsAlias);
				if (!buckets.isMissingNode() && buckets.isArray()) {

					JsonNode metricsNodeArr;

					for (JsonNode bucket : buckets) {

						val facetSingleValueResultGroup = new FacetSingleValueResultGroup();
						facetSingleValueResultGroup.setValue(getValueFunc.apply(bucket));
						facetSingleValueResultGroup.setTitle(facetSingleValueResultGroup.getValue());
						facetSingleValueResultGroup.setCount(bucket.path(countInteractionsElName).asInt(0));

						JsonNode metricsNode = null;

						if (!CollectionUtils.isEmpty(metricsToRead)) {
							facetSingleValueResultGroup.setMetrics(new ArrayList<MetricData>());

							for (FieldMetric metricField : metricsToRead) {

								if (metricField.isInnerFacet()) {
									// navigate to "InnerFacetStats" - > "buckets" node
									metricsNodeArr = bucket.path(TAConstants.FacetQuery.innerFacetStats).path(TAConstants.FacetQuery.bucketsAlias);
									if (!metricsNodeArr.isMissingNode() && metricsNodeArr.isArray() && metricsNodeArr.has(0)) {
										metricsNode = metricsNodeArr.get(0);
									}

								} else {
									metricsNode = bucket;
								}

								// facet level
								MetricData metricData = new MetricData();
								metricData.setName(metricField.getName());
								metricData.setValue(metricsNode.path(metricField.getName()).asDouble(0));

								facetSingleValueResultGroup.addMetricData(metricData);
							}
						}

						facetResults.addGroupValue(facetSingleValueResultGroup);
					}
				}
			}
		} catch (Exception ex) {
			throw new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.JsonResponseParsingError).put(TAConstants.ErrorDataKeys.solrJsonResponse, searchFacetsResponse);

		}

		return facetResults;
	}

	@Override
	public HighlightResult getHighlights(String utterancesJson, String tenant, String channel) {
		HighlightResult result = null;

		try {
			if (!StringUtils.isNullOrBlank(utterancesJson)) {

				ObjectMapper mapper = new ObjectMapper();
				JsonNode rootNode = mapper.readTree(utterancesJson);

				this.checkResponseStatus(rootNode);

				JsonNode docsElem = rootNode.path(TAConstants.InteractionsQuery.response).path(TAConstants.InteractionsQuery.docs);

				// get list of dynamic fields from configuration, those fields are specific for channel and tenant
				List<TextSchemaField> channelDynamicFields = this.textEngineConfigurationService.getChannelDynamicFields(tenant, channel);

				// retrieve highlights
				result = new HighlightResult();
				result.setUtterances(this.getInteractionUtterances(docsElem, tenant, channel, channelDynamicFields));
				result.setHighlights(this.getUtterancesHighlights(rootNode));
			}
		} catch (Exception ex) {
			throw new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.JsonResponseParsingError).put(TAConstants.ErrorDataKeys.solrJsonResponse, utterancesJson);
		}

		return result;
	}


	@Override
	public int getQuantity(String responseJson) {

		int res = 0;

		if (!StringUtils.isNullOrBlank(responseJson)) {
			// load JSON response and parse it
			try {
				JSONTokener tokener = new JSONTokener(responseJson);
				JSONObject root = new JSONObject(tokener);

				this.checkResponseStatus(root);

				JSONObject responseElem = JSONUtils.getJSONObject(root, "response");
				res = JSONUtils.getInt("numFound", responseElem, 0);

			} catch (Exception ex) {
				throw new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.JsonResponseParsingError).put(TAConstants.ErrorDataKeys.solrJsonResponse, responseJson);
			}
		}

		return res;
	}

	/**
	 * Converts Solr response to Daily Series : number of interactions for each day.
	 *
	 * @param interactonsDateFacetJson json of facet date response.
	 * @param facetAlias               - facet alias in response json
	 * @return list of data points : point for each day
	 */
	@Override
	public List<InteractionDailyVolumeDataPoint> getInteractionsDailyVolumeSeries(String interactonsDateFacetJson, String facetAlias) {
		Boolean exceptionWrittern = false;
		double dataPointValue = 0;
		String dataPointDate = "";

		val dailyVolumeSeries = new ArrayList<InteractionDailyVolumeDataPoint>();

		try {

			if (!StringUtils.isNullOrBlank(interactonsDateFacetJson)) {
				// load JSON response and parse it
				ObjectMapper mapper = new ObjectMapper();
				JsonNode rootNode = mapper.readTree(interactonsDateFacetJson);

				this.checkResponseStatus(rootNode);

				JsonNode buckets = rootNode.path(TAConstants.FacetQuery.facets).path(facetAlias).path(TAConstants.FacetQuery.bucketsAlias);
				if (!buckets.isMissingNode()) {
					for (JsonNode dailyVolumeNode : buckets) {

						try {
							dataPointValue = dailyVolumeNode.path("count").asDouble(0);
							dataPointDate = dailyVolumeNode.path(value).asText();

							// add new daily volume
							// interaction data point
							dailyVolumeSeries.add(new InteractionDailyVolumeDataPoint(dataPointDate, dataPointValue));
						} catch (Exception ex) {
							if (!exceptionWrittern) {
								logger.error("Failed to convert json entry to Interactions Daily volume data point.Json {}, Exception - {}",
								             dailyVolumeNode != null ? dailyVolumeNode.toString() : "", ex);

								exceptionWrittern = true;
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			throw new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.JsonResponseParsingError).put(TAConstants.ErrorDataKeys.solrJsonResponse,
			                                                                                                    interactonsDateFacetJson);
		}

		return dailyVolumeSeries;
	}

	/**
	 * Converts Solr response to Daily Series : number of interactions for each day.
	 *
	 * @param dailyVolumeFacetJson json of facet date response.
	 * @return list of data points : point for each day
	 */
	@Override
	public List<InteractionDailyVolumeDataPoint> getInteractionsDailyVolumeSeriesSimpleFacet(String dailyVolumeFacetJson) {
		List<InteractionDailyVolumeDataPoint> dailyVolumeSeries = new ArrayList<>();

		try {

			if (!StringUtils.isNullOrBlank(dailyVolumeFacetJson)) {
				// load JSON response and parse it
				ObjectMapper mapper = new ObjectMapper();
				JsonNode rootNode = mapper.readTree(dailyVolumeFacetJson);

				this.checkResponseStatus(rootNode);

				// @formatter:off
				JsonNode datesCounts = rootNode.path(TAConstants.FacetQuery.facetCounts)
				                            .path(TAConstants.FacetQuery.facetRanges)
				                            .path(TAConstants.FacetQuery.facetDate)
						                     .path(TAConstants.FacetQuery.counts);
				// @formatter:on

				if (!datesCounts.isMissingNode()) {
					Iterator<Map.Entry<String, JsonNode>> dateIterator = datesCounts.fields();

					while (dateIterator.hasNext()) {
						Map.Entry<String, JsonNode> dateCountEntry = dateIterator.next();
						JsonNode dateCountNode = dateCountEntry.getValue();

						dailyVolumeSeries.add(new InteractionDailyVolumeDataPoint(dateCountEntry.getKey(), dateCountNode.asDouble()));
					}
				}
			}
		} catch (Exception ex) {
			throw new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.JsonResponseParsingError).put(TAConstants.ErrorDataKeys.solrJsonResponse,
			                                                                                                    dailyVolumeFacetJson);
		}

		return dailyVolumeSeries;
	}


	@Override
	public List<MetricData> getResultSetMetrics(String metricsJson, List<FieldMetric> fieldsMetrics, Boolean readInteractionsCount) {
		List<MetricData> metrics = new ArrayList<MetricData>();
		MetricData metricData;

		try {
			if (!StringUtils.isNullOrBlank(metricsJson)) {

				// load JSON response and parse it
				ObjectMapper mapper = new ObjectMapper();
				JsonNode rootNode = mapper.readTree(metricsJson);

				this.checkResponseStatus(rootNode);

				JsonNode facetsEl = rootNode.path(TAConstants.FacetQuery.facets);
				if (!facetsEl.isMissingNode()) {

					// should interactions number be read
					if (readInteractionsCount) {
						metrics.add(new MetricData(TAConstants.MetricsQuery.volume,
						                           facetsEl.path(TAConstants.FacetQuery.interactionsCountAlias).asInt()));
					}

					// read metrics
					for (FieldMetric fieldMetric : fieldsMetrics) {
						JsonNode metricNode = facetsEl.path(fieldMetric.getName());

						if (!metricNode.isMissingNode()) {
							metricData = new MetricData(fieldMetric.getName(), metricNode.asDouble(0.0));
							metrics.add(metricData);
						}
					}
				}
			}
		} catch (Exception ex) {
			throw new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.JsonResponseParsingError).put(TAConstants.ErrorDataKeys.solrJsonResponse,
			                                                                                                    metricsJson).put("fields", fieldsMetrics);
		}

		return metrics;
	}

	@Override
	public List<TextElementSentimentsMetric> getTextElementsSentimentsMetrics(String textElementsSentimentJson) {
		List<TextElementSentimentsMetric> textElementsSentiment = new ArrayList<TextElementSentimentsMetric>();

		try {
			if (!StringUtils.isNullOrBlank(textElementsSentimentJson)) {
				// load JSON response and parse it
				ObjectMapper mapper = new ObjectMapper();
				JsonNode rootNode = mapper.readTree(textElementsSentimentJson);

				this.checkResponseStatus(rootNode);


				if (rootNode != null && rootNode.isArray()) {

					TextElementSentimentsMetric textElementSentiment;
					for (JsonNode textElementNode : rootNode) {

						textElementSentiment = new TextElementSentimentsMetric();
						textElementSentiment.setTextElement(textElementNode.path("name").asText());
						textElementSentiment.setSentimentAvg(textElementNode.path("sentimentAvg").asDouble());
						textElementSentiment.setSentimentAvgIncludingNuetral(textElementNode.path("sentimentAvgIncludingNeutral").asDouble());

						textElementsSentiment.add(textElementSentiment);
					}
				}
			}
		} catch (Exception ex) {
			throw new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.JsonResponseParsingError).put(TAConstants.ErrorDataKeys.solrJsonResponse, textElementsSentimentJson);
		}

		return textElementsSentiment;
	}

	@Override
	public List<TextElementTrend> getTextElementyTrends(String textElementTrendsJson) {

		val textElementsTrends = new ArrayList<TextElementTrend>();

		try {

			if (!StringUtils.isNullOrBlank(textElementTrendsJson)) {

				TextElementTrend textElementTrend;

				// load JSON response and parse it
				JSONTokener tokener = new JSONTokener(textElementTrendsJson);
				JSONObject root = new JSONObject(tokener);

				this.checkResponseStatus(root);

				int numFound = root.getInt("numFounds");
				JSONObject trendEl;

				if (numFound != 0) {
					val trendsElem = JSONUtils.getJSONArray(root, "trends");
					for (int i = 0; i < trendsElem.length(); i++) {

						trendEl = trendsElem.getJSONObject(i);

						textElementTrend = new TextElementTrend();
						textElementTrend.setAbsoluteVolumeChange(JSONUtils.getDouble("absoluteChange", trendEl, 0));
						textElementTrend.setRelativeVolumeChange(JSONUtils.getDouble("relativeChange", trendEl, 0));
						textElementTrend.setName(JSONUtils.getString("name", trendEl, ""));
						textElementTrend.setVSentiment(JSONUtils.getDouble("vSentiment", trendEl, 0));
						textElementTrend.setBgSentiment(JSONUtils.getDouble("bgSentiment", trendEl, 0));
						textElementTrend.setVolume(JSONUtils.getDouble("volume", trendEl, 0));
						textElementTrend.setPrVolume(JSONUtils.getDouble("prVolume", trendEl, 0));

						textElementsTrends.add(textElementTrend);
					}
				}
			}
		} catch (Exception ex) {
			throw new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.JsonResponseParsingError).put(TAConstants.ErrorDataKeys.solrJsonResponse, textElementTrendsJson);
		}

		return textElementsTrends;
	}

	@Override
	public List<SearchSuggestion> getTermsAutoCompleteSuggestions(String suggestionsFacetResponse, String language, String facetAlias, String interactionsCountStatAlias) {
		List<SearchSuggestion> searchSuggestions = new ArrayList<SearchSuggestion>();

		try {

			if (!StringUtils.isNullOrBlank(suggestionsFacetResponse)) {
				SearchSuggestion searchSuggestion;

				// load JSON response and parse it
				ObjectMapper mapper = new ObjectMapper();
				JsonNode rootNode = mapper.readTree(suggestionsFacetResponse);

				this.checkResponseStatus(rootNode);

				JsonNode bucketsNode = rootNode.path(TAConstants.FacetQuery.facets).path(facetAlias).path(TAConstants.FacetQuery.bucketsAlias);
				if (bucketsNode != null && !bucketsNode.isMissingNode() && bucketsNode.isArray()) {
					// iterate over facets element and add topics with
					// count and percentage data
					for (JsonNode suggestionElem : bucketsNode) {

						searchSuggestion = new SearchSuggestion();
						searchSuggestion.setText(suggestionElem.path(TAConstants.FacetQuery.bucketTitleAlias).asText());
						searchSuggestion.setNumberOfOccurrences(suggestionElem.path(TAConstants.FacetQuery.interactionsCountAlias).asInt());

						searchSuggestions.add(searchSuggestion);
					}
				}
			}
		} catch (Exception ex) {
			throw new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.JsonResponseParsingError).put(TAConstants.ErrorDataKeys.solrJsonResponse,
			                                                                                                    suggestionsFacetResponse);
		}
		return searchSuggestions;
	}

	@Override
	public List<SearchSuggestion> getTermsAutoCompleteSuggestionsSimpleFacet(String suggestionsFacetResponse, String language) {
		List<SearchSuggestion> searchSuggestions = new ArrayList<SearchSuggestion>();

		try {

			if (!StringUtils.isNullOrBlank(suggestionsFacetResponse)) {
				SearchSuggestion searchSuggestion;

				// load JSON response and parse it
				ObjectMapper mapper = new ObjectMapper();
				JsonNode rootNode = mapper.readTree(suggestionsFacetResponse);

				this.checkResponseStatus(rootNode);

				JsonNode facetFieldsNode = rootNode.path(TAConstants.FacetQuery.facetCounts).path(TAConstants.FacetQuery.facetFields);
				if (facetFieldsNode != null && !facetFieldsNode.isMissingNode()) {

					Iterator<String> nodeFieldsNames = facetFieldsNode.fieldNames();
					if (nodeFieldsNames.hasNext()) {
						String suggestionsNodeName = nodeFieldsNames.next();
						JsonNode textNode = facetFieldsNode.path(suggestionsNodeName);

						if (!textNode.isMissingNode()) {

							Iterator<Map.Entry<String, JsonNode>> termsIterator = textNode.fields();
							Map.Entry<String, JsonNode> entry;

							// iterate over facets element and add topics with
							// count and percentage data
							while (termsIterator.hasNext()) {
								entry = (Map.Entry<String, JsonNode>) termsIterator.next();
								String suggestionTerm = entry.getKey();

								searchSuggestion = new SearchSuggestion();
								searchSuggestion.setText(suggestionTerm);
								searchSuggestion.setNumberOfOccurrences(Integer.valueOf(entry.getValue().toString()));

								searchSuggestions.add(searchSuggestion);
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			throw new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.JsonResponseParsingError).put(TAConstants.ErrorDataKeys.solrJsonResponse,
			                                                                                                    suggestionsFacetResponse);
		}
		return searchSuggestions;
	}

	@Override
	public boolean getIsSourceTypeInChannel(String json) {
		boolean retVal = false;

		try {
			if (!StringUtils.isNullOrBlank(json)) {

				// load JSON response and parse it
				JSONTokener tokener = new JSONTokener(json);
				JSONObject root = new JSONObject(tokener);

				this.checkResponseStatus(root);

				JSONObject responseElem = JSONUtils.getJSONObject(root, "response");
				int numFound = JSONUtils.getInt("numFound", responseElem, 0);

				if (numFound > 0) {
					retVal = true;
				}

			}
		} catch (Exception ex) {
			throw new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.JsonResponseParsingError).put(TAConstants.ErrorDataKeys.solrJsonResponse, json);
		}

		return retVal;
	}

	@Override
	public CollectionStatus convertToCollectionStatus(String collectionStatusJson, String tenant) {
		CollectionStatus collectionStatus = null;

		try {
			if (!StringUtils.isNullOrBlank(collectionStatusJson)) {
				// load JSON response and parse it
				JSONTokener tokener = new JSONTokener(collectionStatusJson);
				JSONObject root = new JSONObject(tokener);

				this.checkResponseStatus(root);

				JSONObject cluster = JSONUtils.getJSONObject(root, "cluster");
				if (cluster != null) {
					JSONObject collections = JSONUtils.getJSONObject(cluster, "collections");
					if (collections != null) {
						JSONObject collectionEl = JSONUtils.getJSONObject(collections, tenant);
						if (collectionEl != null) {
							collectionStatus = new CollectionStatus();

							JSONObject shardsEl = JSONUtils.getJSONObject(collectionEl, "shards");
							if (shardsEl != null) {

								String[] shardsNames = JSONUtils.getObjectKeys(shardsEl);

								if (shardsNames != null) {

									// for each shard
									for (String shardName : shardsNames) {
										JSONObject shardEl = JSONUtils.getJSONObject(shardsEl, shardName);

										// parse and add shard
										Shard shard = new Shard();
										shard.setName(shardName);
										shard.setState(ShardState.toShardState(JSONUtils.getString("state", shardEl, "")));

										JSONObject replicasEl = JSONUtils.getJSONObject(shardEl, "replicas");
										if (replicasEl != null) {

											String[] replicasNames = JSONUtils.getObjectKeys(replicasEl);
											if (replicasNames != null) {
												for (String replicaName : replicasNames) {
													JSONObject replicaEl = JSONUtils.getJSONObject(replicasEl, replicaName);

													// parse and add replica
													Replica replica = new Replica();
													replica.setCore(JSONUtils.getString("core", replicaEl, ""));
													replica.setBaseUrl(JSONUtils.getString("base_url", replicaEl, ""));
													replica.setNodeName(JSONUtils.getString("node_name", replicaEl, ""));
													replica.setState(ReplicaState.toReplicaState(JSONUtils.getString("state", replicaEl, "")));

													shard.addReplica(replica);
												}
											}
										}

										collectionStatus.addShard(shard);
									}
								}
							}
						}
					}
				}

			}
		} catch (Exception ex) {
			throw new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.JsonResponseParsingError).put(TAConstants.ErrorDataKeys.solrJsonResponse, collectionStatusJson);
		}

		return collectionStatus;
	}

	/**
	 * Converts suggester response to list of suggestions.
	 * @param suggestionsJson
	 *            suggestions json
	 * @param suggesterName
	 *            suggester name
	 * @return list of suggestions.
	 */
	@Override
	public List<WeightedSuggestion> getFreeTextLookupSuggestions(String suggestionsJson, String suggesterName) {
		List<WeightedSuggestion> lstSuggestions = null;

		try {
			if (!StringUtils.isNullOrBlank(suggestionsJson)) {

				ObjectMapper mapper = new ObjectMapper();
				JsonNode rootNode = mapper.readTree(suggestionsJson);

				JsonNode suggesterNode = rootNode.path("suggest").path(suggesterName);
				Iterator<String> suggestionsTextNodes = suggesterNode.fieldNames();
				if (suggestionsTextNodes.hasNext()) {
					String textOfSuggestions = suggestionsTextNodes.next();
					JsonNode suggestions = suggesterNode.path(textOfSuggestions).path("suggestions");
					if (suggestions.isArray()) {
						lstSuggestions = new ArrayList<>();

						for (JsonNode suggestion : suggestions) {
							lstSuggestions.add(new WeightedSuggestion(suggestion.get("term").asText(), suggestion.get("weight").asDouble()));
						}
					}
				}
			}
		} catch (Exception ex) {
			throw new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.JsonResponseParsingError).put(TAConstants.ErrorDataKeys.solrJsonResponse, suggestionsJson);
		}

		return lstSuggestions;
	}


	protected void checkResponseStatus(JSONObject root) {
		if (root != null) {
			JSONObject responseHeader = JSONUtils.getJSONObject(root, "responseHeader");
			if (responseHeader != null) {
				int queryExecutionStatus = JSONUtils.getInt("status", responseHeader, 0);
				if (queryExecutionStatus != 0) {
					throw new TextQueryExecutionException(TextQueryExecutionErrorCode.SolrQueryExecutionStatus).put(TAConstants.ErrorDataKeys.solrExecutionStatus,
					                                                                                                queryExecutionStatus).put(TAConstants.ErrorDataKeys.solrJsonResponse,
					                                                                                                                          root.toString());
				}
			}
		}
	}

	protected void checkResponseStatus(JsonNode root) {
		if (root != null) {
			JsonNode responseNode = root.path("responseHeader");
			if (!responseNode.isMissingNode()) {
				int queryExecutionStatus = responseNode.path("status").asInt(0);
				if (queryExecutionStatus != 0) {
					throw new TextQueryExecutionException(TextQueryExecutionErrorCode.SolrQueryExecutionStatus).put(TAConstants.ErrorDataKeys.solrExecutionStatus,
					                                                                                                queryExecutionStatus)
					                                                                                           .put(TAConstants.ErrorDataKeys.solrJsonResponse, root.toString());
				}
			}
		}
	}
}
