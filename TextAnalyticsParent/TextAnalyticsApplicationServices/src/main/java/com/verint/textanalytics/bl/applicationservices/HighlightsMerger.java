package com.verint.textanalytics.bl.applicationservices;

import com.google.common.base.Throwables;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionErrorCode;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionException;
import com.verint.textanalytics.model.interactions.*;
import lombok.val;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Performs merge of highlights on Agent terms, Customer terms and NoSPS
 * terms.
 * @author EZlotnik
 *
 */
/**
 * @author EZlotnik
 *
 */
public class HighlightsMerger {

	/**
	 * C'tor.
	 */
	public HighlightsMerger() {

	}

	/**
	 * Merges highlists from agent, customer and no sps utterances.
	 *
	 * @param agentHighlights
	 *            list of highligts in agent utterances.
	 * @param customerHighlights
	 *            list of highligts in customer utterances.
	 * @param noSpeakerHighlights
	 *            list of highligts in no sps utterances.
	 * @return merged list of highlights.
	 */
	public List<UtteranceHighlights> mergeHighlights(List<UtteranceHighlights> agentHighlights, List<UtteranceHighlights> customerHighlights, List<List<UtteranceHighlights>> noSpeakerHighlights) {
		List<UtteranceHighlights> finalMergedHighlighting = null;

		try {
			finalMergedHighlighting = mergeUtteranceHighlighting(agentHighlights, customerHighlights);

			if (noSpeakerHighlights != null) {

				for (val arr : noSpeakerHighlights) {
					finalMergedHighlighting = mergeUtteranceHighlighting(finalMergedHighlighting, arr);
				}
			}

		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.UtteranceHighlightsMergeError));
		}

		return finalMergedHighlighting;
	}

	/**
	 *
	 * @param left left.
	 * @param right right.
	 * @return List<UtteranceHighlights>
	 */
	public List<UtteranceHighlights> mergeUtteranceHighlighting(List<UtteranceHighlights> left, List<UtteranceHighlights> right) {
		List<UtteranceHighlights> mergedUtterances = null;

		if (left != null && right != null) {

			mergedUtterances = new ArrayList<UtteranceHighlights>();
			HashMap<String, UtteranceHighlights> leftMap = this.getUtterancesHighlightingAsHashMap(left);
			HashMap<String, UtteranceHighlights> rightMap = this.getUtterancesHighlightingAsHashMap(right);

			// harvest the union of all document ids
			List<String> allDocumentIds = Stream.concat(leftMap.keySet().stream(), rightMap.keySet().stream()).distinct().collect(toList());

			UtteranceHighlights leftMapDocument, rightMapDocument;

			for (val documentId : allDocumentIds) {
				// if document is included in both sets, add document with
				// merged list of highlights
				if (leftMap.containsKey(documentId) && rightMap.containsKey(documentId)) {

					val mergeUtterance = new UtteranceHighlights();
					mergeUtterance.setDocumentId(documentId);

					leftMapDocument = leftMap.get(documentId);
					rightMapDocument = rightMap.get(documentId);

					if (leftMapDocument.getSpeakerType() == rightMapDocument.getSpeakerType())
						mergeUtterance.setSpeakerType(leftMapDocument.getSpeakerType());

					mergeUtterance.setTermHighlights(mergeTermHighlighting(leftMapDocument.getTermHighlights(), rightMapDocument.getTermHighlights()));
					mergeUtterance.setEntitiesHighlights(mergeTopicHighlighting(leftMapDocument.getEntitiesHighlights(), rightMapDocument.getEntitiesHighlights()));
					mergeUtterance.setRelationsHighlights(mergeRelationHighlighting(leftMapDocument.getRelationsHighlights(), rightMapDocument.getRelationsHighlights()));
					mergeUtterance.setKeyTermsHighlights(mergeKeyTermsHighlighting(leftMapDocument.getKeyTermsHighlights(), rightMapDocument.getKeyTermsHighlights()));

					mergeUtterance.setAllEntitiesHighlights(leftMapDocument.getAllEntitiesHighlights());
					mergeUtterance.setAllKeyTermsHighlights(leftMapDocument.getAllKeyTermsHighlights());
					mergeUtterance.setAllRelationsHighlights(leftMapDocument.getAllRelationsHighlights());

					mergedUtterances.add(mergeUtterance);

				} else if (leftMap.containsKey(documentId)) {
					mergedUtterances.add(leftMap.get(documentId));
				} else if (rightMap.containsKey(documentId)) {
					mergedUtterances.add(rightMap.get(documentId));
				}
			}
		} else if (left == null) {
			mergedUtterances = right;
		} else if (right == null) {
			mergedUtterances = left;
		}

		return mergedUtterances;
	}

	private List<TermHighlight> mergeTermHighlighting(List<TermHighlight> left, List<TermHighlight> right) {
		List<TermHighlight> mergedHighlights = null;

		if (left != null && right != null) {

			mergedHighlights = new ArrayList<TermHighlight>();
			HashMap<String, TermHighlight> leftMap = this.getTermHighlightsAsHashMap(left);
			HashMap<String, TermHighlight> rightMap = this.getTermHighlightsAsHashMap(right);

			// harvest the union of all document ids
			List<String> allHighlightsPositions = Stream.concat(leftMap.keySet().stream(), rightMap.keySet().stream()).distinct().collect(toList());

			for (val highlightsPosition : allHighlightsPositions) {

				// if highlight is included in both list
				// it doesn't matter from which list to take
				if (leftMap.containsKey(highlightsPosition) && rightMap.containsKey(highlightsPosition)) {

					mergedHighlights.add(leftMap.get(highlightsPosition));

				} else if (leftMap.containsKey(highlightsPosition)) {
					// highlight location found in left map only
					mergedHighlights.add(leftMap.get(highlightsPosition));
				} else if (rightMap.containsKey(highlightsPosition)) {
					// highlight location found in right map only
					mergedHighlights.add(rightMap.get(highlightsPosition));
				}
			}

		} else if (left == null) {
			mergedHighlights = right;
		} else if (right == null) {
			mergedHighlights = left;
		}

		return mergedHighlights;
	}

	private List<EntityHighlight> mergeTopicHighlighting(List<EntityHighlight> left, List<EntityHighlight> right) {
		List<EntityHighlight> mergedHighlights = null;

		if (left != null && right != null) {

			mergedHighlights = new ArrayList<EntityHighlight>();
			HashMap<String, EntityHighlight> leftMap = this.getTopicHighlightsAsHashMap(left);
			HashMap<String, EntityHighlight> rightMap = this.getTopicHighlightsAsHashMap(right);

			// harvest the union of all document ids
			List<String> allHighlightsPositions = Stream.concat(leftMap.keySet().stream(), rightMap.keySet().stream()).distinct().collect(toList());

			for (val highlightsPosition : allHighlightsPositions) {

				// if highlight is included in both list
				// it doesn't matter from which list to take
				if (leftMap.containsKey(highlightsPosition) && rightMap.containsKey(highlightsPosition)) {

					mergedHighlights.add(leftMap.get(highlightsPosition));

				} else if (leftMap.containsKey(highlightsPosition)) {
					// highlight location found in left map only
					mergedHighlights.add(leftMap.get(highlightsPosition));
				} else if (rightMap.containsKey(highlightsPosition)) {
					// highlight location found in right map only
					mergedHighlights.add(rightMap.get(highlightsPosition));
				}
			}

		} else if (left == null) {
			mergedHighlights = right;
		} else if (right == null) {
			mergedHighlights = left;
		}

		return mergedHighlights;
	}

	private List<RelationHighlight> mergeRelationHighlighting(List<RelationHighlight> left, List<RelationHighlight> right) {
		List<RelationHighlight> mergedHighlights = null;

		if (left != null && right != null) {

			mergedHighlights = new ArrayList<RelationHighlight>();
			HashMap<String, RelationHighlight> leftMap = this.getRelationHighlightsAsHashMap(left);
			HashMap<String, RelationHighlight> rightMap = this.getRelationHighlightsAsHashMap(right);

			// harvest the union of all document ids
			List<String> allHighlightsPositions = Stream.concat(leftMap.keySet().stream(), rightMap.keySet().stream()).distinct().collect(toList());

			for (val highlightsPosition : allHighlightsPositions) {

				// if highlight is included in both list
				// it doesn't matter from which list to take
				if (leftMap.containsKey(highlightsPosition) && rightMap.containsKey(highlightsPosition)) {

					mergedHighlights.add(leftMap.get(highlightsPosition));

				} else if (leftMap.containsKey(highlightsPosition)) {
					// highlight location found in left map only
					mergedHighlights.add(leftMap.get(highlightsPosition));
				} else if (rightMap.containsKey(highlightsPosition)) {
					// highlight location found in right map only
					mergedHighlights.add(rightMap.get(highlightsPosition));
				}
			}

		} else if (left == null) {
			mergedHighlights = right;
		} else if (right == null) {
			mergedHighlights = left;
		}

		return mergedHighlights;
	}

	private List<KeyTermHighlight> mergeKeyTermsHighlighting(List<KeyTermHighlight> left, List<KeyTermHighlight> right) {
		List<KeyTermHighlight> mergedHighlights = null;

		if (left != null && right != null) {

			mergedHighlights = new ArrayList<KeyTermHighlight>();
			HashMap<String, KeyTermHighlight> leftMap = this.getKeyTermHighlightsAsHashMap(left);
			HashMap<String, KeyTermHighlight> rightMap = this.getKeyTermHighlightsAsHashMap(right);

			// harvest the union of all document ids
			List<String> allHighlightsPositions = Stream.concat(leftMap.keySet().stream(), rightMap.keySet().stream()).distinct().collect(toList());

			for (val highlightsPosition : allHighlightsPositions) {

				// if highlight is included in both list
				// it doesn't matter from which list to take
				if (leftMap.containsKey(highlightsPosition) && rightMap.containsKey(highlightsPosition)) {

					mergedHighlights.add(leftMap.get(highlightsPosition));

				} else if (leftMap.containsKey(highlightsPosition)) {
					// highlight location found in left map only
					mergedHighlights.add(leftMap.get(highlightsPosition));
				} else if (rightMap.containsKey(highlightsPosition)) {
					// highlight location found in right map only
					mergedHighlights.add(rightMap.get(highlightsPosition));
				}
			}

		} else if (left == null) {
			mergedHighlights = right;
		} else if (right == null) {
			mergedHighlights = left;
		}

		return mergedHighlights;
	}

	private HashMap<String, TermHighlight> getTermHighlightsAsHashMap(List<TermHighlight> highlights) {
		val map = new HashMap<String, TermHighlight>();

		if (highlights != null) {
			for (val highlight : highlights) {
				map.put(highlight.getKey(), highlight);
			}
		}
		return map;
	}

	private HashMap<String, EntityHighlight> getTopicHighlightsAsHashMap(List<EntityHighlight> highlights) {
		val map = new HashMap<String, EntityHighlight>();

		if (highlights != null) {
			for (val highlight : highlights) {
				map.put(highlight.getKey(), highlight);
			}
		}
		return map;
	}

	private HashMap<String, RelationHighlight> getRelationHighlightsAsHashMap(List<RelationHighlight> highlights) {
		val map = new HashMap<String, RelationHighlight>();

		if (highlights != null) {
			for (val highlight : highlights) {
				map.put(highlight.getKey(), highlight);
			}
		}
		return map;
	}

	private HashMap<String, KeyTermHighlight> getKeyTermHighlightsAsHashMap(List<KeyTermHighlight> highlights) {
		val map = new HashMap<String, KeyTermHighlight>();

		if (highlights != null) {
			for (val highlight : highlights) {
				map.put(highlight.getKey(), highlight);
			}
		}
		return map;
	}

	private HashMap<String, UtteranceHighlights> getUtterancesHighlightingAsHashMap(List<UtteranceHighlights> utterancesHighlighting) {
		val map = new HashMap<String, UtteranceHighlights>();

		if (utterancesHighlighting != null) {
			for (val utteranceHighlighting : utterancesHighlighting) {
				map.put(utteranceHighlighting.getDocumentId(), utteranceHighlighting);
			}
		}
		return map;
	}
}