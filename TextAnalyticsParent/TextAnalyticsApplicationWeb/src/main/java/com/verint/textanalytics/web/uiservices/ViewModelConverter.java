package com.verint.textanalytics.web.uiservices;

import com.google.common.base.Throwables;
import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.constants.TAConstants.MetricsQuery;
import com.verint.textanalytics.common.exceptions.ViewModelConversionException;
import com.verint.textanalytics.common.utils.CollectionUtils;
import com.verint.textanalytics.common.utils.DataUtils;
import com.verint.textanalytics.common.utils.StringUtils;
import com.verint.textanalytics.model.analyze.FieldMetric;
import com.verint.textanalytics.model.analyze.MetricData;
import com.verint.textanalytics.model.analyze.MetricType;
import com.verint.textanalytics.model.facets.FacetResultGroup;
import com.verint.textanalytics.model.facets.TextElementMetricType;
import com.verint.textanalytics.model.facets.TextElementType;
import com.verint.textanalytics.model.facets.TextElementsFacetNode;
import com.verint.textanalytics.model.interactions.*;
import com.verint.textanalytics.model.modelEditor.Domain;
import com.verint.textanalytics.model.modelEditor.Language;
import com.verint.textanalytics.model.security.Channel;
import com.verint.textanalytics.model.security.User;
import com.verint.textanalytics.model.storedSearch.Category;
import com.verint.textanalytics.model.storedSearch.CategoryReprocessingStatus;
import com.verint.textanalytics.model.storedSearch.SavedSearch;
import com.verint.textanalytics.model.trends.TrendChangeDirection;
import com.verint.textanalytics.model.trends.TrendType;
import com.verint.textanalytics.web.viewmodel.*;
import com.verint.textanalytics.web.viewmodel.Interaction;
import com.verint.textanalytics.web.viewmodel.SpeakerType;
import com.verint.textanalytics.web.viewmodel.Utterance;
import lombok.Getter;
import lombok.val;
import org.joda.time.DateTime;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Converts a model instances to view-model instances.
 *
 * @author EZlotnik
 */

/**
 * @author NShunewich
 */
public class ViewModelConverter {
	private static final String METRIC_PREFIX = "METRIC_";

	@Getter
	protected List<FieldMetric> metricFields;

	private Map<String, FieldMetric> metricFieldsMap;

	/**
	 * Constructor.
	 */
	public ViewModelConverter() {

	}

	/**
	 * ViewModelConverter.
	 *
	 * @param metricFields metrics for facet
	 */
	public ViewModelConverter(List<FieldMetric> metricFields) {
		this.updateFieldsMetrics(metricFields);
	}

	/**
	 * Setter for fields metrics.
	 *
	 * @param metrics metrics to use
	 */
	public void setMetricFields(List<FieldMetric> metrics) {
		this.updateFieldsMetrics(metrics);
	}

	private void updateFieldsMetrics(List<FieldMetric> metrics) {
		this.metricFields = metrics;

		if (this.metricFields != null) {
			this.metricFieldsMap = this.metricFields.stream().collect(Collectors.toMap((FieldMetric e) -> e.getName(), e -> e));
		}
	}

	/**
	 * Converts Model document to View Model document.
	 *
	 * @param intr document
	 * @return view model document
	 */
	public Interaction convertToViewModelInteraction(com.verint.textanalytics.model.interactions.Interaction intr) {
		try {
			val interaction = new com.verint.textanalytics.web.viewmodel.Interaction();
			interaction.setId(intr.getId());
			interaction.setChannel(intr.getChannel());
			interaction.setTenant(intr.getTenant());
			interaction.setSourceType(intr.getSourceType().toString());
			interaction.setLanguage(intr.getLanguage());

			// dates			
			interaction.setStartTimeTicks(DataUtils.getDateFromISO8601StringTimestamp(intr.getStartTime()));

			// agent
			interaction.setAgentNames(intr.getAgentNames());
			interaction.setAgentLocalStartTimeTicks(DataUtils.getDateFromISO8601StringTimestamp(intr.getAgentLocalStartTime()));
			interaction.setAgentMessagesCount(intr.getAgentMessagesCount());
			interaction.setAgentAvgResponseTime(intr.getAgentAvgResponseTime());

			// customer
			interaction.setCustomerNames(intr.getCustomerNames());
			interaction.setCustomerLocalStartTimeTicks(DataUtils.getDateFromISO8601StringTimestamp(intr.getCustomerLocalStartTime()));
			interaction.setCustomerMessagesCount(intr.getCustomerMessagesCount());
			interaction.setCustomerAvgResponseTime(intr.getCustomerAvgResponseTime());

			interaction.setNumberOfRobotMessages(intr.getNumberOfRobotMessages());

			interaction.setMessagesCount(intr.getMessagesCount());
			interaction.setHandleTime(intr.getHandleTime());

			interaction.setRelevancyScore(intr.getRelevancyScore());

			interaction.setSentiment(intr.getSentiment().getValue());
			interaction.setIsSentimentMixed(intr.getIsSentimentMixed());

			interaction.setDynamicFields(intr.getDynamicFields());
			interaction.setSourceTypeSpecificFields(intr.getSourceSpecificFields());
			interaction.setCategories(intr.getCategories());
			interaction.setSnippets(intr.getSnippets());
			interaction.setSubject(intr.getSubject());

			return interaction;
		} catch (Exception ex) {
			Throwables.propagate(new ViewModelConversionException(ex));
		}

		return null;
	}

	/**
	 * Converts interaction's data to Interaction Preview view model.
	 *
	 * @param intr interaction
	 * @return preview of interaction
	 */
	public InteractionPreview convertToViewModelInteractionPreview(com.verint.textanalytics.model.interactions.Interaction intr) {
		try {
			val interaction = new com.verint.textanalytics.web.viewmodel.InteractionPreview();
			interaction.setId(intr.getId());
			interaction.setTenant(intr.getTenant());
			interaction.setChannel(intr.getChannel());
			interaction.setLanguage(intr.getLanguage());
			interaction.setSourceType(intr.getSourceType());
			interaction.setSentiment(intr.getSentiment().getValue());
			interaction.setDate(DataUtils.getDateFromISO8601StringTimestamp(intr.getStartTime()));
			interaction.setDynamicFields(intr.getDynamicFields());
			interaction.setCategories(intr.getCategories());
			interaction.setUtterances(convertToViewModelUtterances(intr.getUtterances()));
			return interaction;
		} catch (Exception ex) {
			Throwables.propagate(new ViewModelConversionException(ex));
		}

		return null;
	}

	/**
	 * Converts interaction's data to Interaction Preview view model (In text widget format).
	 *
	 * @param intr interaction
	 * @return preview of interaction
	 */
	public InteractionPreviewCIV convertToViewModelInteractionPreviewCIV(com.verint.textanalytics.model.interactions.Interaction intr) {
		try {
			val interaction = new com.verint.textanalytics.web.viewmodel.InteractionPreviewCIV();
			val interactionDetails = new com.verint.textanalytics.web.viewmodel.InteractionCIV();

			HighlightCIVObject highlightObject = new HighlightCIVObject();
			List<BaseHighlightCIV> highlightsArray = new ArrayList<BaseHighlightCIV>();

			SentimentsCIV sentimentsObject = new SentimentsCIV();
			List<SentimentUtteranceCIV> sentimentsArray = new ArrayList<SentimentUtteranceCIV>();

			//When creating utterance view model calculate end time + highlights and sentements
			interactionDetails.setUtterances(convertToViewModelUtterancesCIV(intr.getUtterances(), interactionDetails, highlightsArray, sentimentsArray));

			//Update Highlights
			highlightObject.setHighlights(highlightsArray);
			highlightObject.setInteractionId(intr.getId());
			interaction.setHighlightObject(highlightObject);

			//Update sentiment
			sentimentsObject.setInteractionId(intr.getId());
			if (intr.getSentiment().getValue() == 0 && !intr.getIsSentimentMixed()) {
				sentimentsObject.setSentiment(null);
			} else {
				sentimentsObject.setSentiment(intr.getSentiment().getValue());

			}

			sentimentsObject.setSentiments(sentimentsArray);
			interaction.setSentimentsObject(sentimentsObject);

			interactionDetails.setMeta_dt_interactionStartTime(DataUtils.getISO8601StringFromDate(intr.getStartTime()));

			if (intr.getLanguage() == null) {
				//Interaction level language is ignored but still needs to be set to empty string
				interactionDetails.setLanguage("");
			} else {
				interactionDetails.setLanguage(intr.getLanguage());
			}

			List<Integer> agentIds = new ArrayList<Integer>();
			List<String> agentTimeZone = new ArrayList<String>();

			interactionDetails.setMeta_ss_employeesNames(intr.getAgentNames());

			int index = 0;
			if (intr.getAgentNames() != null) {
				for (String name : intr.getAgentNames()) {
					agentIds.add(index);
					index++;
					//In CIV format time zone has GMT prefix
					agentTimeZone.add("GMT" + "+00:00");
				}
			}
			interactionDetails.setMeta_ss_employeeIDs(agentIds);
			interactionDetails.setMeta_ss_employeeTimeZone(agentTimeZone);

			List<Integer> customerIds = new ArrayList<Integer>();
			List<String> customerTimeZone = new ArrayList<String>();

			interactionDetails.setMeta_ss_customerNames(intr.getCustomerNames());

			index = 0;
			if (intr.getCustomerNames() != null) {
				for (String name : intr.getCustomerNames()) {
					customerIds.add(index);
					index++;
					//In CIV format time zone has GMT prefix
					customerTimeZone.add("GMT" + intr.getCustomerTimeZone());
				}
			}
			interactionDetails.setMeta_ss_customerIDs(customerIds);
			interactionDetails.setMeta_ss_customerTimeZone(customerTimeZone);

			interactionDetails.setMeta_l_handleTime(intr.getHandleTime());
			interactionDetails.setMeta_i_numberOfRobotMessages(intr.getNumberOfRobotMessages());
			interactionDetails.setMeta_i_employeesMessages(intr.getAgentMessagesCount());
			interactionDetails.setMeta_l_avgEmployeeResponseTime(intr.getAgentAvgResponseTime());
			interactionDetails.setMeta_i_customerMessages(intr.getCustomerMessagesCount());
			interactionDetails.setMeta_l_avgCustomerResponseTime(intr.getCustomerAvgResponseTime());
			interactionDetails.setMeta_i_numberOfAttachments(0);

			if (intr.getSourceType() == SourceType.Email) {
				interactionDetails.setSubject(intr.getSubject());
			}

			interaction.setInteraction(interactionDetails);

			interaction.setMessageIdentifier(intr.getId());
			interaction.setTenant(intr.getTenant());
			interaction.setChannel(intr.getChannel());
			interaction.setInteractionType(intr.getSourceType());

			//	interaction.setDate(DataUtils.getDateFromISO8601StringTimestamp(intr.getStartTime()));

			//handle snippets
			SnippetsCIV snippets = new SnippetsCIV();

			snippets.setInteractionId(intr.getId());
			snippets.setSnippets(convertToViewModelSnippetsCIV(intr.getSnippets()));

			interaction.setSnippetObject(snippets);

			return interaction;
		} catch (Exception ex) {
			Throwables.propagate(new ViewModelConversionException(ex));
		}

		return null;
	}

	private List<SnippetUtteranceCIV> convertToViewModelSnippetsCIV(List<Snippet> interactionSnippet) {

		val res = new ArrayList<SnippetUtteranceCIV>();
		SnippetUtteranceCIV curUtterance;
		SnippetPositionCIV curPosition;

		if (interactionSnippet != null) {
			Map<String, List<Snippet>> utteranceSnippets = interactionSnippet.stream()
			                                                                 .map(c -> c.getUtteranceId())
			                                                                 .distinct()
			                                                                 .collect(toMap(u -> u, u -> interactionSnippet.stream()
			                                                                                                               .filter(s -> s.getUtteranceId() == u)
			                                                                                                               .collect(toList())));

			if (!utteranceSnippets.isEmpty()) {
				for (val utteramceSnippet : utteranceSnippets.values()) {
					if (utteramceSnippet != null && utteramceSnippet.size() != 0) {

						curUtterance = new SnippetUtteranceCIV();
						curUtterance.setSnippets(new ArrayList<SnippetPositionCIV>());

						for (Snippet curSnippet : utteramceSnippet) {
							//Change to happen only once
							curUtterance.setUtteranceId(curSnippet.getUtteranceId());

							curPosition = new SnippetPositionCIV();
							curPosition.setStart(curSnippet.getStart());
							curPosition.setEnd(curSnippet.getEnd());
							curUtterance.getSnippets().add(curPosition);
						}
						res.add(curUtterance);

					}
				}
			}
		}
		return res;
	}

	private List<Utterance> convertToViewModelUtterances(List<com.verint.textanalytics.model.interactions.Utterance> utterances) {
		val res = new ArrayList<Utterance>();

		Utterance viewModelUtterance;
		List<BaseHighlight> highlights;

		try {
			for (com.verint.textanalytics.model.interactions.Utterance utterance : utterances) {
				viewModelUtterance = new Utterance();

				viewModelUtterance.setContentType(utterance.getContentType());
				viewModelUtterance.setDocumentDynamicFields(utterance.getDocumentDynamicFields());

				viewModelUtterance.setEntities(utterance.getEntities());
				viewModelUtterance.setRelations(utterance.getRelations());
				viewModelUtterance.setKeyterms(utterance.getKeyterms());

				viewModelUtterance.setId(utterance.getId());
				viewModelUtterance.setLanguage(utterance.getLanguage());
				viewModelUtterance.setParentId(utterance.getParentId());
				viewModelUtterance.setSpeakerType(SpeakerType.toSpeakerType(utterance.getSpeakerType().toString()));
				viewModelUtterance.setText(utterance.getText());
				viewModelUtterance.setDate(DataUtils.getDateFromISO8601StringTimestamp(utterance.getDate()));

				highlights = new ArrayList<BaseHighlight>();

				if (utterance.getMergedHighlighting() != null) {
					for (BaseHighlight highlight : utterance.getMergedHighlighting()) {
						highlights.add(highlight);
					}
				}

				List<BaseHighlight> sortedHighlights = highlights.stream().sorted((h1, h2) -> h1.getStarts() - h2.getStarts()).collect(toList());

				viewModelUtterance.setHighlights(sortedHighlights);
				res.add(viewModelUtterance);
			}
		} catch (Exception ex) {
			Throwables.propagate(new ViewModelConversionException(ex));
		}

		return res;
	}

	private List<UtteranceCIV> convertToViewModelUtterancesCIV(List<com.verint.textanalytics.model.interactions.Utterance> utterances, com.verint.textanalytics.web.viewmodel.InteractionCIV interactinCIV, List<BaseHighlightCIV> highlightsArray, List<SentimentUtteranceCIV> sentimentsArray) {
		val res = new ArrayList<UtteranceCIV>();

		UtteranceCIV viewModelUtterance;
		BaseHighlightCIV highlight;
		SentimentUtteranceCIV utternaceSentiment;

		try {
			if (utterances != null) {
				for (com.verint.textanalytics.model.interactions.Utterance utterance : utterances) {

					viewModelUtterance = new UtteranceCIV();

					viewModelUtterance.setId(utterance.getId());
					viewModelUtterance.setLanguage(utterance.getLanguage());
					viewModelUtterance.setMeta_s_speaker(SpeakerType.toSpeakerType(utterance.getSpeakerType().toString()));
					//we always assume that the first is the speaker in the utterance
					viewModelUtterance.setSpeakerId(0);
					viewModelUtterance.setText(utterance.getText());
					viewModelUtterance.setPlainText(utterance.getText());
					viewModelUtterance.setDate(DataUtils.getISO8601StringFromDate(utterance.getDate()));

					if (utterance.getUtteranceSentiment() != null) {
						utternaceSentiment = new SentimentUtteranceCIV();
						utternaceSentiment.setUtteranceId(utterance.getId());

						utternaceSentiment.setSentiment(utterance.getUtteranceSentiment().getValue());

						sentimentsArray.add(utternaceSentiment);
					}
					//Update end time untill we get to the last interaction
					interactinCIV.setMeta_dt_interactionEndTime(DataUtils.getISO8601StringFromDate(utterance.getDate()));

					updateUtteranceHighlightsCIV(highlightsArray, utterance);

					res.add(viewModelUtterance);

				}
			}
		} catch (Exception ex) {
			Throwables.propagate(new ViewModelConversionException(ex));
		}

		return res;
	}

	private String removeUnwantedCharFromHighligthData(String originalName) {
		if (originalName.startsWith("/")) {
			return originalName.substring(1);
		}
		return originalName;
	}

	private void updateUtteranceHighlightsCIV(List<BaseHighlightCIV> highlightsArray, com.verint.textanalytics.model.interactions.Utterance utterance) {

		BaseHighlightCIV highlight;

		//add terms highlighting
		if (utterance.getTermsHighlighting() != null) {
			for (TermHighlight curHighlight : utterance.getTermsHighlighting()) {
				highlight = new BaseHighlightCIV();
				highlight.setUtteranceId(utterance.getId());
				highlight.setEnd(curHighlight.getEnds());
				highlight.setStart(curHighlight.getStarts());
				highlight.setData(curHighlight.getTerm());
				highlight.setType(HighlightTypeCIV.Search);
				highlight.setOriginalType(HighlightTypeCIV.Search);
				highlight.setUtteranceSpeaker(SpeakerType.toSpeakerType(utterance.getSpeakerType().toString()));
				highlightsArray.add(highlight);
			}
		}

		//add searched topics highlighting
		if (utterance.getEntitiesHighlighting() != null) {
			for (EntityHighlight curHighlight : utterance.getEntitiesHighlighting()) {
				highlight = new BaseHighlightCIV();
				highlight.setUtteranceId(utterance.getId());
				highlight.setEnd(curHighlight.getEnds());
				highlight.setStart(curHighlight.getStarts());
				highlight.setData(removeUnwantedCharFromHighligthData(curHighlight.getTopic()));
				highlight.setType(HighlightTypeCIV.Search);
				highlight.setOriginalType(HighlightTypeCIV.Entity);
				highlight.setUtteranceSpeaker(SpeakerType.toSpeakerType(utterance.getSpeakerType().toString()));
				highlightsArray.add(highlight);

			}
		}


		//add searched keyterms highlighting
		if (utterance.getKeytermsHighlighting() != null) {
			for (KeyTermHighlight curHighlight : utterance.getKeytermsHighlighting()) {
				highlight = new BaseHighlightCIV();
				highlight.setUtteranceId(utterance.getId());
				highlight.setEnd(curHighlight.getEnds());
				highlight.setStart(curHighlight.getStarts());
				highlight.setData(curHighlight.getKeyterm());
				highlight.setType(HighlightTypeCIV.Search);
				highlight.setOriginalType(HighlightTypeCIV.KeyTerm);
				highlight.setUtteranceSpeaker(SpeakerType.toSpeakerType(utterance.getSpeakerType().toString()));
				highlightsArray.add(highlight);

			}
		}


		//add searched relations highlighting
		if (utterance.getRelationsHighlighting() != null) {
			for (RelationHighlight curHighlight : utterance.getRelationsHighlighting()) {
				if (curHighlight.getPositions() != null) {
					for (Position curPosition : curHighlight.getPositions()) {
						highlight = new BaseHighlightCIV();
						highlight.setUtteranceId(utterance.getId());
						highlight.setEnd(curPosition.getEnds());
						highlight.setStart(curPosition.getStarts());
						highlight.setData(removeUnwantedCharFromHighligthData(curHighlight.getRelation()));
						highlight.setType(HighlightTypeCIV.Search);
						highlight.setOriginalType(HighlightTypeCIV.Relation);
						highlight.setUtteranceSpeaker(SpeakerType.toSpeakerType(utterance.getSpeakerType().toString()));
						highlightsArray.add(highlight);

					}
				}
			}

		}

		//add sentiment highligthing
		if (utterance.getSentimentHighlighting() != null) {
			for (SentimentHighlight curSentiment : utterance.getSentimentHighlighting()) {
				if (curSentiment.getPositions() != null) {
					for (Position curPosition : curSentiment.getPositions()) {
						highlight = new BaseHighlightCIV();
						highlight.setUtteranceId(utterance.getId());
						highlight.setEnd(curPosition.getEnds());
						highlight.setStart(curPosition.getStarts());
						highlight.setData(Integer.toString(curSentiment.getValue()));
						highlight.setType(HighlightTypeCIV.Sentiment);
						highlightsArray.add(highlight);
					}
				}
			}
		}

		//add all topics highlighting
		if (utterance.getAllEntitiesHighlighting() != null) {
			for (EntityHighlight curHighlight : utterance.getAllEntitiesHighlighting()) {
				highlight = new BaseHighlightCIV();
				highlight.setUtteranceId(utterance.getId());
				highlight.setEnd(curHighlight.getEnds());
				highlight.setStart(curHighlight.getStarts());
				highlight.setData(removeUnwantedCharFromHighligthData(curHighlight.getTopic()));
				highlight.setType(HighlightTypeCIV.Entity);
				highlightsArray.add(highlight);

			}
		}

		//add searched relations highlighting
		if (utterance.getAllRelationsHighlighting() != null) {
			for (RelationHighlight curHighlight : utterance.getAllRelationsHighlighting()) {
				if (curHighlight.getPositions() != null) {
					for (Position curPosition : curHighlight.getPositions()) {
						highlight = new BaseHighlightCIV();
						highlight.setUtteranceId(utterance.getId());
						highlight.setEnd(curPosition.getEnds());
						highlight.setStart(curPosition.getStarts());
						highlight.setData(removeUnwantedCharFromHighligthData(curHighlight.getRelation()));
						highlight.setType(HighlightTypeCIV.Relation);
						highlightsArray.add(highlight);

					}
				}
			}

		}

		//add all topics highlighting
		if (utterance.getAllKeytermsHighlighting() != null) {
			for (KeyTermHighlight curHighlight : utterance.getAllKeytermsHighlighting()) {
				highlight = new BaseHighlightCIV();
				highlight.setUtteranceId(utterance.getId());
				highlight.setEnd(curHighlight.getEnds());
				highlight.setStart(curHighlight.getStarts());
				highlight.setData(curHighlight.getKeyterm());
				highlight.setType(HighlightTypeCIV.KeyTerm);
				highlightsArray.add(highlight);

			}
		}

	}

	/**
	 * Convert Model EntityFacetNode to View Model TextElementFacetNode.
	 *
	 * @param textElementTreeNode an entity
	 * @return a view model entity facet node
	 */
	public TextElementFacetTreeNode convertToViewModelTextElementFacet(com.verint.textanalytics.model.facets.TextElementsFacetNode textElementTreeNode) {
		val treeNode = new TextElementFacetTreeNode();

		treeNode.setValue(textElementTreeNode.getValue());
		treeNode.setText(textElementTreeNode.getName());
		treeNode.setPercentage(textElementTreeNode.getPercentage());
		treeNode.setExpanded(false);
		treeNode.setIconCls(TAConstants.treeNodeNoIconCls);
		treeNode.setCls(TAConstants.treeNodeCls);
		treeNode.setCorrelationPercentage(textElementTreeNode.getCorrelationPercentage());

		// update text according to value

		if (textElementTreeNode.getChildren() != null) {
			treeNode.setLeaf(false);
			treeNode.setChildren(new ArrayList<TextElementFacetNode>());

			for (val childNode : textElementTreeNode.getChildren()) {
				treeNode.addChild(convertToViewModelTextElementFacet(childNode));
			}
		} else {
			treeNode.setLeaf(true);
		}

		return treeNode;
	}

	/**
	 * Convert Model EntityFacetNode to View Model TextElementFacetNode.
	 *
	 * @param facetItem an entity
	 * @return a view model entity facet node
	 */
	public TextElementFacetTreeNode convertToViewModelTextElementFacet(FacetResultGroup facetItem) {
		val treeNode = new TextElementFacetTreeNode();

		treeNode.setValue(facetItem.getTitleKey());
		treeNode.setText(facetItem.getTitle());
		treeNode.setPercentage(facetItem.getPercentage());
		treeNode.setExpanded(false);
		treeNode.setLeaf(true);
		treeNode.setIconCls(TAConstants.treeNodeNoIconCls);
		treeNode.setCls(TAConstants.treeNodeCls);

		return treeNode;
	}

	/**
	 * Converts a entities facet node to entities tree map node.
	 *
	 * @param textElementTreeNode entityTreeNode
	 * @param type                type of the text element to use in the client
	 * @param metricsToConvert    metrics of facet node to convert
	 * @param metricsLimits       metrics limit to update
	 * @return a tree map node
	 */
	public TextElementFacetTreeMapNode convertToViewModelTextElementTreeMapNode(com.verint.textanalytics.model.facets.TextElementsFacetNode textElementTreeNode, TextElementType type,
																			    Map<String, String> metricsToConvert, MetricsLimits metricsLimits) {
		TextElementFacetTreeMapNode treeNode = new TextElementFacetTreeMapNode();

		if (textElementTreeNode != null) {
			treeNode.setValue(textElementTreeNode.getValue());
			treeNode.setText(textElementTreeNode.getName());
			treeNode.setLevel(textElementTreeNode.getLevel());
			treeNode.setType(type);

			//@formatter:off
			val volumeMetric = new com.verint.textanalytics.web.viewmodel.MetricData();
			volumeMetric.setMetricType(MetricType.NUMBER)
						.setName(MetricsQuery.volume)
						.setDisplayKey(METRIC_PREFIX + MetricsQuery.volume)
						.setValue(textElementTreeNode.getNumberOfInteractions());
			treeNode.addMetric(volumeMetric.getName(), volumeMetric);

			val volumePercentageMetric = new com.verint.textanalytics.web.viewmodel.MetricData();
			volumePercentageMetric.setName(MetricsQuery.volumePercentage)
								  .setDisplayKey(METRIC_PREFIX + MetricsQuery.volumePercentage)
								  .setMetricType(MetricType.NUMBER)
								  .setValue(textElementTreeNode.getPercentage());
			treeNode.addMetric(volumePercentageMetric.getName(), volumePercentageMetric);

			// Add Only required metrics
			if (metricsToConvert.containsKey(TextElementMetricType.CorrelationPercentage.name())) {
				val correlationPercentageMetric = new com.verint.textanalytics.web.viewmodel.MetricData();
				correlationPercentageMetric.setName(MetricsQuery.correlationPercentage)
									  .setDisplayKey(METRIC_PREFIX + MetricsQuery.correlationPercentage)
									  .setMetricType(MetricType.NUMBER)
									  .setValue(textElementTreeNode.getCorrelationPercentage());



				treeNode.addMetric(correlationPercentageMetric.getName(), correlationPercentageMetric);
			}

			// Add required metrics only
			List<MetricData> metrics = textElementTreeNode.getMetrics();
			if (metrics != null) {
				metrics.stream().filter(m -> !MetricsQuery.volume.equals(m.getName()) && !MetricsQuery.volumePercentage.equals(m.getName()))
	                            .filter(m -> this.metricFieldsMap.containsKey(m.getName()))
	                            .forEach(m -> {

		                            if (metricsToConvert.containsKey(m.getName())) {
		                                com.verint.textanalytics.web.viewmodel.MetricData newMetricData = new com.verint.textanalytics.web.viewmodel.MetricData();
		                                newMetricData.setName(m.getName());
		                                newMetricData.setValue(m.getValue());

		                                FieldMetric metricField = this.metricFieldsMap.get(m.getName());

		                                newMetricData.setDisplayKey(metricField.getDisplayKey());
		                                newMetricData.setMetricType(metricField.getType());
		                                treeNode.addMetric(newMetricData.getName(), newMetricData);
		                            }
	                            });
			}

			if (metricsLimits != null) {
				// update Metrics limits for current Text Element
				updateMetricsLimitsData(treeNode, metricsLimits);
			}

			if (textElementTreeNode.getChildren() != null) {
				// allocate childrens list
				treeNode.setChildren(new ArrayList<>());

				// convert children including update of Metrics Limits
				for (TextElementsFacetNode childNode : textElementTreeNode.getChildren()) {
					treeNode.addChild(convertToViewModelTextElementTreeMapNode(childNode, type, metricsToConvert, metricsLimits));
				}
			}

			//@formatter:on
		}

		return treeNode;
	}

	/**
	 * Converts a facet result node to text elements tree map node.
	 *
	 * @param categoryFacetGroup facet group
	 * @return a tree map node
	 */
	public TextElementFacetTreeMapNode convertCategoryFacetGroupToViewModelTextElementTreeMapNode(com.verint.textanalytics.model.facets.FacetSingleValueResultGroup categoryFacetGroup) {

		val textElementNode = new TextElementFacetTreeMapNode();
		textElementNode.setText(categoryFacetGroup.getTitle());
		textElementNode.setValue(categoryFacetGroup.getValue());
		textElementNode.setType(TextElementType.Categories);
		// default value
		textElementNode.setLevel(1);

		//@formatter:off
		val volumeMetric = new com.verint.textanalytics.web.viewmodel.MetricData();
		volumeMetric.setMetricType(MetricType.NUMBER)
					.setName(MetricsQuery.volume)
					.setDisplayKey(METRIC_PREFIX + MetricsQuery.volume)
					.setValue(categoryFacetGroup.getCount());

				
		val volumePercentageMetric = new com.verint.textanalytics.web.viewmodel.MetricData();
		volumePercentageMetric.setName(MetricsQuery.volumePercentage)
							  .setDisplayKey(METRIC_PREFIX + MetricsQuery.volumePercentage)
							  .setMetricType(MetricType.NUMBER)
							  .setValue(categoryFacetGroup.getPercentage());


		val correlationPercentageMetric = new com.verint.textanalytics.web.viewmodel.MetricData();
		correlationPercentageMetric.setName(MetricsQuery.correlationPercentage)
							  .setDisplayKey(METRIC_PREFIX + MetricsQuery.correlationPercentage)
							  .setMetricType(MetricType.NUMBER)
							  .setValue(categoryFacetGroup.getCorrelationPercentage());

		textElementNode.addMetric(volumeMetric.getName(), volumeMetric);
		textElementNode.addMetric(volumePercentageMetric.getName(), volumePercentageMetric);
		textElementNode.addMetric(correlationPercentageMetric.getName(), correlationPercentageMetric);
		

		List<MetricData> metrics = categoryFacetGroup.getMetrics();
		
		metrics.stream().filter(m -> !MetricsQuery.volume.equals(m.getName()) && !MetricsQuery.volumePercentage.equals(m.getName()))
					    .filter(m -> this.metricFieldsMap.containsKey(m.getName()))
					    .forEach(m -> {
					    	com.verint.textanalytics.web.viewmodel.MetricData newMetricData = new com.verint.textanalytics.web.viewmodel.MetricData();
							newMetricData.setName(m.getName());
							newMetricData.setValue(m.getValue());
							
							FieldMetric metricField = this.metricFieldsMap.get(m.getName());
							
							newMetricData.setDisplayKey(metricField.getDisplayKey());
							newMetricData.setMetricType(metricField.getType());
							textElementNode.addMetric(newMetricData.getName(), newMetricData);
					    });
		//@formatter:on		

		return textElementNode;
	}

	/**
	 * Converts a category metrics to text element treemap node.
	 * @param categoryId category id
	 * @param categoryName category name
	 * @param metrics category metrics
	 * @return text element node
	 */
	public TextElementFacetTreeMapNode convertToCategoryMetrics(Integer categoryId, String categoryName, List<MetricData> metrics) {

		val textElementNode = new TextElementFacetTreeMapNode();
		textElementNode.setText(categoryName);
		textElementNode.setValue(String.valueOf(categoryId));

		Double interactionsCount = metrics.stream().filter(m -> m.getName().equals(MetricsQuery.volume)).findFirst().get().getValue();
		val volumeMetric = new com.verint.textanalytics.web.viewmodel.MetricData();
		volumeMetric.setMetricType(MetricType.NUMBER)
		            .setName(MetricsQuery.volume)
		            .setDisplayKey(METRIC_PREFIX + MetricsQuery.volume)
		            .setValue(interactionsCount);


		Double volumePercentage = metrics.stream().filter(m -> m.getName().equals(MetricsQuery.volumePercentage)).findFirst().get().getValue();
		val volumePercentageMetric = new com.verint.textanalytics.web.viewmodel.MetricData();
		volumePercentageMetric.setName(MetricsQuery.volumePercentage)
		                      .setDisplayKey(METRIC_PREFIX + MetricsQuery.volumePercentage)
		                      .setMetricType(MetricType.NUMBER)
		                      .setValue(volumePercentage);


		Double correlationPercentage = metrics.stream().filter(m -> m.getName().equals(MetricsQuery.correlationPercentage)).findFirst().get().getValue();
		val correlationPercentageMetric = new com.verint.textanalytics.web.viewmodel.MetricData();
		correlationPercentageMetric.setName(MetricsQuery.correlationPercentage)
		                           .setDisplayKey(METRIC_PREFIX + MetricsQuery.correlationPercentage)
		                           .setMetricType(MetricType.NUMBER)
		                           .setValue(correlationPercentage);

		textElementNode.addMetric(volumeMetric.getName(), volumeMetric);
		textElementNode.addMetric(volumePercentageMetric.getName(), volumePercentageMetric);
		textElementNode.addMetric(correlationPercentageMetric.getName(), correlationPercentageMetric);

		//@formatter:off
		metrics.stream()
		       .filter(m -> !MetricsQuery.volume.equals(m.getName()) && !MetricsQuery.volumePercentage.equals(m.getName())
				      && !MetricsQuery.correlationPercentage.equals(m.getName()))
		       .filter(m -> this.metricFieldsMap.containsKey(m.getName()))
		       .forEach(m -> {
			       com.verint.textanalytics.web.viewmodel.MetricData newMetricData = new com.verint.textanalytics.web.viewmodel.MetricData();
			       newMetricData.setName(m.getName());
			       newMetricData.setValue(m.getValue());

			       FieldMetric metricField = this.metricFieldsMap.get(m.getName());

			       newMetricData.setDisplayKey(metricField.getDisplayKey());
			       newMetricData.setMetricType(metricField.getType());
			       textElementNode.addMetric(newMetricData.getName(), newMetricData);
		       });
		//@formatter:on

		return textElementNode;
	}

	/**
	 * @param customerUsageAnalyticsEnabled indication of customer usafe configuration
	 * @param tenantChannels                list of channel for the tenant.
	 * @param appConfig                     application configuration
	 * @return a view model configuration.
	 */
	public Configuration convertToViewModelConfiguration(Boolean customerUsageAnalyticsEnabled, List<Channel> tenantChannels, ApplicationConfiguration appConfig) {
		val configuration = new Configuration();

		configuration.setCustomerUsageAnalyticsEnabled(customerUsageAnalyticsEnabled);
		configuration.setTenantChannels(tenantChannels);
		configuration.setAutoCompletePrefixMinLengthConfiguration(appConfig.getAutoCompletePrefixMinLengthConfiguration());
		configuration.setSearchTermsSuggestionType(appConfig.getSearchTermsSuggestionType());
		configuration.setAjaxRequestTimeout(appConfig.getDarwinRestRequestTimeout());
		configuration.setStoredSearchNameMaxLength(appConfig.getStoredSearchNameMaxLength());
		configuration.setSearchTermsLimit(appConfig.getSearchTermsLimit());
		configuration.setStoredSearchDescriptionMaxLength(appConfig.getStoredSearchDescriptionMaxLength());
		configuration.setModelEditorServiceURL(appConfig.getModelEditorServiceURL());

		configuration.setQuickMenuMetadataAvailable(appConfig.isQuickMenuMetadataAvailable());
		configuration.setQuickMenuOrgAvailable(appConfig.isQuickMenuOrgAvailable());

		return configuration;
	}

	/**
	 * Converts a model interactions daily volume data point to view model data
	 * point.
	 *
	 * @param dataPoint            a daily volume data point to convert
	 * @param clientTimeZoneOffset a client time zone offset
	 * @return a view model object
	 */
	public InteractionDailyVolumeDataPoint convertToViewModelDailyVolumeDataPoint(com.verint.textanalytics.model.trends.InteractionDailyVolumeDataPoint dataPoint, int clientTimeZoneOffset) {
		InteractionDailyVolumeDataPoint vwDataPoint = null;

		try {
			// @formatter:off
			// remove time of the date and then add a client time zone offset
			vwDataPoint = new InteractionDailyVolumeDataPoint().setDate(DataUtils.getDateFromISO8601StringTimestamp(DataUtils.getDateTimeAccordingToClientTimeZone(dataPoint.getDate(), clientTimeZoneOffset)))
			                                                   .setValue(dataPoint.getValue());
			// @formatter:on
		} catch (Exception ex) {
			Throwables.propagate(new ViewModelConversionException(ex));
		}

		return vwDataPoint;
	}

	/**
	 * Converts Entity trends model to view model.
	 *
	 * @param entityTrends entityTrends collection
	 * @return view model collection
	 */
	public List<TextElementTrend> convertToViewModelMergedTrend(List<com.verint.textanalytics.model.trends.TextElementTrend> entityTrends) {
		return convertToViewModelMergedTrend(entityTrends, TrendType.Themes);
	}

	/**
	 * Converts Entity trends model to view model.
	 *
	 * @param entityTrends entityTrends collection
	 * @param trendType the type of converted trend
	 * @return view model collection
	 */
	public List<TextElementTrend> convertToViewModelMergedTrend(List<com.verint.textanalytics.model.trends.TextElementTrend> entityTrends, TrendType trendType) {

		List<TextElementTrend> vmEntityTrends = new ArrayList<TextElementTrend>();
		TextElementTrend vmEntityTrend = new TextElementTrend();
		final int trendWithNoBackgroundPercent = 999;
		final int fullPercentage = 100;
		int prefix = 1;

		if (entityTrends != null) {
			for (com.verint.textanalytics.model.trends.TextElementTrend entityTrend : entityTrends) {

				String name = entityTrend.getName();
				String value;

				switch (trendType) {
					case Categories:
						name = entityTrend.getName();
						value = entityTrend.getValue();
						break;
					default:
						// parse name (1/Device)
						String[] arrName = name.split("/");
						int len = arrName.length;
						prefix = Integer.parseInt(arrName[0]);
						name = arrName[len - 1];

						value = entityTrend.getName();
				}

				vmEntityTrend = new TextElementTrend();
				vmEntityTrend.setName(name);
				vmEntityTrend.setValue(value);
				vmEntityTrend.setPrefix(prefix);
				vmEntityTrend.setSentiment((int) Math.round(entityTrend.getVSentiment()));
				vmEntityTrend.setTrendType(entityTrend.getTrendType());

				TrendChangeDirection direction;
				if (entityTrend.getAbsoluteVolumeChange() < 0)
					direction = TrendChangeDirection.Decrease;
				else
					direction = TrendChangeDirection.Increase;

				vmEntityTrend.setChangeDirection(direction);

				double absoluteV = entityTrend.getAbsoluteVolumeChange() * fullPercentage;
				double relativeV = entityTrend.getRelativeVolumeChange() * fullPercentage;
				double prValueV = entityTrend.getPrVolume() * fullPercentage;

				if (Double.isInfinite(relativeV)) {
					relativeV = trendWithNoBackgroundPercent;
				}
				vmEntityTrend.setAbsChangePercentage(absoluteV);
				vmEntityTrend.setRelChangePercentage(relativeV);
				vmEntityTrend.setPrVolume(prValueV);

				vmEntityTrend.setVolume(entityTrend.getVolume());

				// setting leaf to true for 2nd level.
				vmEntityTrend.setLeaf(value.startsWith("2") ? true : false);

				vmEntityTrends.add(vmEntityTrend);
			}
		}

		return vmEntityTrends;
	}


	/**
	 * Converts Merged trends model to view model.
	 *
	 * @param mergedTrends  mergedTrends collection
	 * @param sortProperty  sortProperty
	 * @param sortDirection sortDirection
	 * @param trendType trendType
	 * @return view model collection
	 */
	public List<TextElementTrend> convertToViewModelMergedTrend(List<com.verint.textanalytics.model.trends.TextElementTrend> mergedTrends, String sortProperty, String sortDirection, TrendType trendType) {

		List<TextElementTrend> vmTrends = convertToViewModelMergedTrend(mergedTrends, trendType);

		vmTrends.sort((t1, t2) -> {
			int res = 0;

			switch (sortProperty) {
				case "Sentiment":
					res = Integer.compare(t1.getSentiment(), t2.getSentiment());
					break;
				case "Name":
					res = t1.getName().compareTo(t2.getName());
					break;
				case "PrVolume":
					res = Double.compare(Math.abs(t1.getVolume()), Math.abs(t2.getVolume()));
					break;
				case "AbsoluteChange":
					res = Double.compare(Math.abs(t1.getAbsChangePercentage()), Math.abs(t2.getAbsChangePercentage()));
					break;
				case "RelativeChange":
					res = Double.compare(Math.abs(t1.getRelChangePercentage()), Math.abs(t2.getRelChangePercentage()));
					break;
				default:
					Throwables.propagate(new ViewModelConversionException(new Exception("Discover Trends - Merged Trends Convertor Error: Unknowwn Sort Type Property")));
			}

			if ("desc".equals(sortDirection)) {
				res *= -1;
			}

			return res;
		});

		return vmTrends;
	}

	/**
	 * @param suggestionResult text suggestions collection
	 * @return text suggestions
	 */
	public List<SuggestionItem> convertToViewModelSuggestionItem(com.verint.textanalytics.model.interactions.SearchSuggestionResult suggestionResult) {
		List<SuggestionItem> vmSuggestion = new ArrayList<SuggestionItem>();
		SuggestionItem vmSuggestionItem;
		final double fullPercentage = 100.0;

		if (suggestionResult.getSuggestions() != null && suggestionResult.getTotalNumberFound() != 0) {

			for (com.verint.textanalytics.model.interactions.SearchSuggestion textSuggestion : suggestionResult.getSuggestions()) {
				if (textSuggestion.getNumberOfOccurrences() != 0) {

					// remove tokens which created as result of CommonGramsFilter
					if (!this.isTextWithStopWordToken(textSuggestion.getText())) {
						vmSuggestionItem = new SuggestionItem();

						vmSuggestionItem.setText(textSuggestion.getText());
						double precent = ((textSuggestion.getNumberOfOccurrences() * 1.0) / suggestionResult.getTotalNumberFound()) * fullPercentage;
						if (precent > fullPercentage) {
							precent = fullPercentage;
						}

						vmSuggestionItem.setPrecent(precent);
						vmSuggestionItem.setBasedOnSample(suggestionResult.isBasedOnSample());
						vmSuggestion.add(vmSuggestionItem);
					}
				}
			}

			vmSuggestion = vmSuggestion.stream().sorted(Collections.reverseOrder(comparing(SuggestionItem::getPrecent))).collect(toList());
		}
		return vmSuggestion;
	}

	/**
	 * Converts suggestion to view model suggestion.
	 *
	 * @param suggestionResult suggestions result
	 * @return list of view model suggestions.
	 */
	public List<SuggestionItem> convertToViewModelNgrammedSuggestionItem(com.verint.textanalytics.model.interactions.SearchSuggestionResult suggestionResult) {
		List<SuggestionItem> vmSuggestion = new ArrayList<SuggestionItem>();
		SuggestionItem vmSuggestionItem;

		if (suggestionResult.getSuggestions() != null) {
			for (com.verint.textanalytics.model.interactions.SearchSuggestion textSuggestion : suggestionResult.getSuggestions()) {

				vmSuggestionItem = new SuggestionItem();
				vmSuggestionItem.setText(textSuggestion.getText());

				vmSuggestion.add(vmSuggestionItem);
			}
		}

		return vmSuggestion;
	}

	/**
	 * Converts model object points to view model.
	 *
	 * @param datePoints points generated by BL
	 * @return view model points array
	 */
	public List<AnalyzeDailyVolumeDataPoints> convertToViewModelAnalyzeDailyVolume(List<com.verint.textanalytics.model.analyze.AnalyzeInteractionsDailyVolumePoints> datePoints) {
		List<AnalyzeDailyVolumeDataPoints> viewmodelPoints = null;

		// No sort required as data is being sort by Solr facet
		try {
			if (!CollectionUtils.isEmpty(datePoints)) {
				viewmodelPoints = datePoints.parallelStream().map(mp -> {

					double contextPercentage = 0;
					long longDate = DataUtils.getDateFromISO8601StringTimestamp(mp.getDate());
					if (mp.getBackgroundValue() > 0) {
						contextPercentage = mp.getContextValue() / mp.getBackgroundValue() * TAConstants.percentage_100;
					}

					return new AnalyzeDailyVolumeDataPoints(longDate, mp.getBackgroundValue(), mp.getContextValue(), contextPercentage);
				}).collect(Collectors.toList());
			}

		} catch (Exception ex) {
			Throwables.propagate(new ViewModelConversionException(ex));
		}

		return viewmodelPoints;
	}

	/***
	 * @param tenantsList list of user authorized tenants as entity model to convert
	 * @return list of tenants to display
	 */
	public List<Tenant> convertToViewModelTenants(List<com.verint.textanalytics.model.security.Tenant> tenantsList) {

		List<Tenant> viewModelTenant = new ArrayList<Tenant>();
		Tenant curTenant;

		for (com.verint.textanalytics.model.security.Tenant tenant : tenantsList) {
			curTenant = new Tenant().setDisplayName(tenant.getDisplayName()).setId(tenant.getId());

			curTenant.setChannels(convertToViewModelChannels(tenant.getChannels()));

			//TODO: calc status
			curTenant.setStatus(DataSourceStatus.configured);

			viewModelTenant.add(curTenant);
		}

		return viewModelTenant;
	}

	/***
	 * @param channelList list of user authorized channels as entity model to convert
	 * @return list of channels to display
	 */
	public List<com.verint.textanalytics.web.viewmodel.Channel> convertToViewModelChannels(List<Channel> channelList) {
		List<com.verint.textanalytics.web.viewmodel.Channel> viewModelChannel = new ArrayList<com.verint.textanalytics.web.viewmodel.Channel>();

		if (channelList != null) {
			com.verint.textanalytics.web.viewmodel.Channel curChannel;

			for (Channel channel : channelList) {
				// @formatter:off
				curChannel = new com.verint.textanalytics.web.viewmodel.Channel()
																			.setDisplayName(channel.getDisplayName())
																			.setId(channel.getId())
																			.setStatus(DataSourceStatus.configured);

				// @formatter:on
				viewModelChannel.add(curChannel);
			}
		}

		return viewModelChannel;
	}

	/**
	 * Map Interactions Grid column name to Solr metadata field name.
	 *
	 * @param columnName column bound field
	 * @return name of VTA Solr metadata field.
	 */
	public String mapMetadataFieldName(String columnName) {
		String metaDataFieldName = "";

		if (!StringUtils.isNullOrBlank(columnName)) {
			switch (columnName) {
				case TAConstants.InteractionsGridColumns.sourceType:
					metaDataFieldName = TAConstants.SchemaFieldNames.sourceType;
					break;
				case TAConstants.InteractionsGridColumns.relevancyScore:
					metaDataFieldName = TAConstants.SchemaFieldNames.relevancyScore;
					break;
				case TAConstants.InteractionsGridColumns.startTime:
					metaDataFieldName = TAConstants.SchemaFieldNames.parentDate;
					break;
				case TAConstants.InteractionsGridColumns.sentiment:
					metaDataFieldName = TAConstants.SchemaFieldNames.interactionSentiment;
					break;
				case TAConstants.InteractionsGridColumns.handleTime:
					metaDataFieldName = TAConstants.SchemaFieldNames.handleTime;
					break;

				case TAConstants.InteractionsGridColumns.messagesCount:
					metaDataFieldName = TAConstants.SchemaFieldNames.messagesCount;
					break;
				case TAConstants.InteractionsGridColumns.robotMessagesCount:
					metaDataFieldName = TAConstants.SchemaFieldNames.robotMessagesCount;
					break;

				// agent fields
				case TAConstants.InteractionsGridColumns.agentNames:
					metaDataFieldName = TAConstants.SchemaFieldNames.agentNames;
					break;
				case TAConstants.InteractionsGridColumns.agentLocalStartTime:
					metaDataFieldName = TAConstants.SchemaFieldNames.agentLocalStartTime;
					break;
				case TAConstants.InteractionsGridColumns.agentMessagesCount:
					metaDataFieldName = TAConstants.SchemaFieldNames.agentMessagesCount;
					break;
				case TAConstants.InteractionsGridColumns.agentAvgResponseTime:
					metaDataFieldName = TAConstants.SchemaFieldNames.agentAvgResponseTime;
					break;

				// customer fields
				case TAConstants.InteractionsGridColumns.customerNames:
					metaDataFieldName = TAConstants.SchemaFieldNames.customerNames;
					break;
				case TAConstants.InteractionsGridColumns.customerLocalStartTime:
					metaDataFieldName = TAConstants.SchemaFieldNames.customerLocalStartTime;
					break;
				case TAConstants.InteractionsGridColumns.customerMessagesCount:
					metaDataFieldName = TAConstants.SchemaFieldNames.customerMessagesCount;
					break;
				case TAConstants.InteractionsGridColumns.customerAvgResponseTime:
					metaDataFieldName = TAConstants.SchemaFieldNames.customerAvgResponseTime;
					break;

				default:
					//check if it is meta data filed pass it as it is and if not sort by date
					if (columnName.contains("Meta")) {
						metaDataFieldName = columnName;
					} else {
						metaDataFieldName = TAConstants.SchemaFieldNames.parentDate;
					}
					break;
			}
		}

		return metaDataFieldName;
	}

	/**
	 * Maps Text Elements Grid view model field names to model names for EA
	 * Search API.
	 *
	 * @param viewModeFieldName view model field name.
	 * @return model field name
	 */
	public String mapTextElementsTrendFieldForSort(String viewModeFieldName) {
		return Character.toLowerCase(viewModeFieldName.charAt(0)) + viewModeFieldName.substring(1);
	}

	/**
	 * @param metrics metrics
	 * @return List<Metric>
	 */
	public List<Metric> convertToViewModelMetrics(List<com.verint.textanalytics.model.analyze.MetricDataChange> metrics) {
		List<Metric> res = new ArrayList<Metric>();

		String displayKey;
		double backgroundValue;
		double contextValue;
		double percentage;
		MetricType metricType;
		Metric uiMetric;

		for (com.verint.textanalytics.model.analyze.MetricDataChange metric : metrics) {

			displayKey = metric.getDisplayKey();
			backgroundValue = metric.getBackgroundValue();
			contextValue = metric.getCurrentSearchValue();

			if (backgroundValue != 0 && contextValue != 0) {
				percentage = ((contextValue - backgroundValue) / backgroundValue) * TAConstants.percentage_100;
			} else {
				percentage = 0.0;
			}

			metricType = this.metricFieldsMap.get(displayKey).getType();
			uiMetric = new Metric(displayKey, contextValue, backgroundValue, percentage, metricType);

			res.add(uiMetric);
		}

		res.sort(new Comparator<Metric>() {
			@Override
			public int compare(Metric metric1, Metric metric2) {
				val m1 = metricFieldsMap.get(metric1.getDisplayKey());
				val m2 = metricFieldsMap.get(metric2.getDisplayKey());
				if (m1 != null && m2 != null) {
					return m1.getIndex() - m2.getIndex();
				} else {
					return Integer.MIN_VALUE;
				}
			}
		});

		return res;
	}

	/**
	 * @param category category
	 * @return Category
	 */
	public Category convertToCategoryModel(com.verint.textanalytics.web.viewmodel.Category category) {
		Category resCategory = new Category();

		resCategory.setActive(category.isActive());
		resCategory.setColor(category.getColor());
		resCategory.setDescription(category.getDescription());
		resCategory.setId(category.getId());
		resCategory.setImpact(category.getImpact());
		if (category.getLastChangeDateTimeGMT() != null) {
			resCategory.setLastChangeDateTimeGMT(DataUtils.getDateFromISO8601StringWithMilliseconds(category.getLastChangeDateTimeGMT()));
		}

		resCategory.setLastModifiedByUserId(category.getLastModifiedByUserId());

		resCategory.setName(category.getName());
		resCategory.setPublished(category.isPublished());
		resCategory.setSearchContext(category.getSearchContext());
		resCategory.setSearchContextVersion(category.getSearchContextVersion());

		return resCategory;
	}

	/**
	 * @param category category
	 * @return Category
	 */
	public com.verint.textanalytics.web.viewmodel.Category convertToCategoryViewModel(Category category) {
		com.verint.textanalytics.web.viewmodel.Category resCategory = new com.verint.textanalytics.web.viewmodel.Category();

		resCategory.setName(category.getName());
		resCategory.setId(category.getId());
		resCategory.setActive(category.isActive());
		resCategory.setColor(category.getColor());
		resCategory.setDescription(category.getDescription());

		resCategory.setImpact(category.getImpact());

		resCategory.setLastChangeDateTimeGMT(DataUtils.getISO8601StringFromDate(category.getLastChangeDateTimeGMT()));
		resCategory.setLastChangeDateTimeGMTMillis(category.getLastChangeDateTimeGMT().getMillis());

		com.verint.textanalytics.model.storedSearch.CategoryReprocessingState reprocessingState = category.getReprocessingState();
		if (reprocessingState != null) {

			resCategory.setReprocessingStatus(reprocessingState.getStatus().value());
			if (reprocessingState.getLastReprocessedTime() != null) {
				resCategory.setLastReprocessedDateTimeGMT(DataUtils.getISO8601StringFromDate(reprocessingState.getLastReprocessedTime()));
				resCategory.setLastReprocessedDateTimeGMTMillis(DataUtils.getDateFromISO8601StringTimestamp(reprocessingState.getLastReprocessedTime()));
			}

			if (reprocessingState.getLastErrorTime() != null) {
				resCategory.setLastErrorTimeGMT(DataUtils.getISO8601StringFromDate(reprocessingState.getLastErrorTime()));
				resCategory.setLastErrorTimeGMTMillis(DataUtils.getDateFromISO8601StringTimestamp(reprocessingState.getLastErrorTime()));
			}

			if (reprocessingState.getReprocessStartTime() != null) {
				resCategory.setReprocessStartDateTimeGMT(DataUtils.getISO8601StringFromDate(reprocessingState.getReprocessStartTime()));
				resCategory.setReprocessStartDateTimeGMTMillis(DataUtils.getDateFromISO8601StringTimestamp(reprocessingState.getReprocessStartTime()));
			}
		} else {
			// if no reprocessing state, then user can reprocess
			resCategory.setReprocessingStatus(CategoryReprocessingStatus.Unknown.value());
		}

		resCategory.setIsReprocessingAllowed(category.getIsReprocessingAllowed());
		resCategory.setShouldBeReprocessed(category.getShouldBeReprocessed());

		resCategory.setLastModifiedByUserId(category.getLastModifiedByUserId());
		// TODO get the ID from the Name - foundation 
		resCategory.setLastModifiedByUserName("USER NAME - TBD");

		resCategory.setPublished(category.isPublished());
		resCategory.setSearchContext(category.getSearchContext());
		resCategory.setSearchContextVersion(category.getSearchContextVersion());

		return resCategory;
	}

	private DateTime getTime(DateTime lastChangeByDateTimeGMT, User user) {
		DateTime res = lastChangeByDateTimeGMT;

		return res;
	}

	/**
	 * @param savedSearch savedSearch
	 * @return SavedSearch
	 */
	public SavedSearch savedSearchConverterToModel(com.verint.textanalytics.web.viewmodel.SavedSearch savedSearch) {
		SavedSearch resSavedSearch = new SavedSearch();

		resSavedSearch.setDescription(savedSearch.getDescription());
		resSavedSearch.setId(savedSearch.getId());

		if (savedSearch.getLastChangeDateTimeGMT() != null) {
			resSavedSearch.setLastChangeDateTimeGMT(DataUtils.getDateFromISO8601StringWithMilliseconds(savedSearch.getLastChangeDateTimeGMT()));
		}
		resSavedSearch.setLastModifiedByUserId(savedSearch.getLastModifiedByUserId());

		resSavedSearch.setName(savedSearch.getName());
		resSavedSearch.setPublic(savedSearch.isPublic());
		resSavedSearch.setSearchContext(savedSearch.getSearchContext());
		resSavedSearch.setSearchContextVersion(savedSearch.getSearchContextVersion());

		return resSavedSearch;
	}

	/**
	 * @param savedSearch savedSearch
	 * @return SavedSearch
	 */
	public com.verint.textanalytics.web.viewmodel.SavedSearch savedSearchConverterToViewModel(SavedSearch savedSearch) {
		com.verint.textanalytics.web.viewmodel.SavedSearch resSavedSearch = new com.verint.textanalytics.web.viewmodel.SavedSearch();

		resSavedSearch.setDescription(savedSearch.getDescription());
		resSavedSearch.setId(savedSearch.getId());

		resSavedSearch.setLastChangeDateTimeGMT(DataUtils.getISO8601StringFromDate(savedSearch.getLastChangeDateTimeGMT()));
		resSavedSearch.setLastChangeDateTimeGMTMillis(savedSearch.getLastChangeDateTimeGMT().getMillis());

		resSavedSearch.setLastModifiedByUserId(savedSearch.getLastModifiedByUserId());

		// TODO get the ID from the Name - foundation 
		resSavedSearch.setLastModifiedByUserName("USER NAME - TBD"/*savedSearch.getLastModifiedByUserId()*/);

		resSavedSearch.setName(savedSearch.getName());
		resSavedSearch.setPublic(savedSearch.isPublic());
		resSavedSearch.setSearchContext(savedSearch.getSearchContext());
		resSavedSearch.setSearchContextVersion(savedSearch.getSearchContextVersion());

		return resSavedSearch;
	}


	/** Convert Categories.
	 * @param categoriesList categoriesList
	 * @return List<com.verint.textanalytics.web.viewmodel.Category>
	 */
	public List<com.verint.textanalytics.web.viewmodel.Category> categoriesListConverterToViewModel(List<Category> categoriesList) {
		if (categoriesList != null) {

			return categoriesList.stream()
					             .map(c -> this.convertToCategoryViewModel(c))
			                     .collect(toList());
		}

		return null;
	}

	/**
	 * @param savedSearchesList savedSearchesList
	 * @return List<com.verint.textanalytics.web.viewmodel.SavedSearch>
	 */
	public List<com.verint.textanalytics.web.viewmodel.SavedSearch> savedSearchesListConverterToViewModel(List<SavedSearch> savedSearchesList) {
		val res = new ArrayList<com.verint.textanalytics.web.viewmodel.SavedSearch>();
		if (savedSearchesList != null) {
			for (SavedSearch savedSearch : savedSearchesList) {
				res.add(this.savedSearchConverterToViewModel(savedSearch));
			}
		}

		return res;
	}

	/**
	 * @param categoriesRepository categoriesRepository
	 * @return CategoriesRepository
	 */
	public CategoriesRepository categoriesListConverterToViewModel(com.verint.textanalytics.model.storedSearch.CategoriesRepository categoriesRepository) {
		val res = new CategoriesRepository();
		res.setMaxId(categoriesRepository.getMaxId());
		res.setTimeStamp(categoriesRepository.getTimeStamp());

		// convert categories
		res.setCategories(this.categoriesListConverterToViewModel(categoriesRepository.getCategories()));
		res.setCanNotParseCategories(categoriesRepository.getCanNotParseCategories());
		return res;
	}

	/**
	 * @param savedSearchesRepository savedSearchesRepository
	 * @return SavedSearchesRepository
	 */
	public SavedSearchesRepository savedSearchesListConverterToViewModel(com.verint.textanalytics.model.storedSearch.SavedSearchesRepository savedSearchesRepository) {
		val res = new SavedSearchesRepository();
		res.setMaxId(savedSearchesRepository.getMaxId());
		res.setTimeStamp(savedSearchesRepository.getTimeStamp());
		res.setSavedSearches(this.savedSearchesListConverterToViewModel(savedSearchesRepository.getSavedSearches()));
		res.setCanNotParseSavedSearches(savedSearchesRepository.getCanNotParseSavedSearches());
		return res;
	}

	/**
	 * @param retrieveModelsTree retrieveModelsTree
	 * @return ModelsTree
	 */
	public ModelsTree modelsTreeConverter(com.verint.textanalytics.model.modelEditor.ModelsTree retrieveModelsTree) {

		ModelsTree res = new ModelsTree();
		Model model;
		res.setModels(new ArrayList<Model>());

		for (Language language : retrieveModelsTree.getLanguages()) {

			for (Domain domain : language.getDomains()) {

				model = new Model();
				model.setLanguage(language.getName());
				model.setDomain(domain.getName());

				res.getModels().add(model);
			}
		}

		return res;
	}

	private void updateMetricsLimitsData(com.verint.textanalytics.web.viewmodel.TextElementFacetTreeMapNode textElementTreeNode, MetricsLimits metricsLimits) {

		if (textElementTreeNode.getMetrics() != null) {
			for (val metricData : textElementTreeNode.getMetrics().values()) {
				// updates metrics limit data structure
				metricsLimits.updateMetricLimit(textElementTreeNode.getLevel(), metricData);
			}
		}
	}

	private boolean isTextWithStopWordToken(String token) {
		return token.indexOf("_") >= 0;
	}
}
