package com.verint.textanalytics.bl.applicationservices.facet.textelements;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.utils.CollectionUtils;
import com.verint.textanalytics.dal.darwin.TextAnalyticsProvider;
import com.verint.textanalytics.model.facets.SpeakerQueryType;
import com.verint.textanalytics.model.facets.TextElementMetricType;
import com.verint.textanalytics.model.facets.TextElementType;
import com.verint.textanalytics.model.facets.TextElementsFacetNode;
import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by EZlotnik on 5/31/2016.
 */
public abstract class TextElementsFacet {
	private final String slash = "/";

	protected Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	protected TextAnalyticsProvider textAnalyticsProvider;

	@Autowired
	protected ConfigurationManager configurationManager;

	public static final String FACET_THREAD = "TextElementsFacet";
	public static final String METRICS_THREAD = "TextElementMetrics";
	public static final String METRICS1_THREAD = "TextElementMetrics1";
	public static final String METRICS2_THREAD = "TextElementMetrics2";
	public static final String METRICS_BACKGROUND_THREAD = "TextElementMetricsBackground";
	public static final String METRICS_NESTED_LEVEL_THREAD = "TextElementMetricsNestedLevel";

	public static final String SENTIMENT1_THREAD = "TextElementsSentiment1";
	public static final String SENTIMENT2_THREAD = "TextElementsSentiment2";
	public static final String SINGLE_SENTIMENT_THREAD = "TextElementSingleSentiment";
	public static final String CURRENT_SEARCH_INTERACTIONS_COUNT_THREAD = "CurrentSearchInteractionsCount";
	public static final String BACKGROUND_CONTEXT_THREAD = "BackgroundContext";
	public static final String LEAFS_THREAD = "LeafsThread";


	/**
	* Calculates Topics/Realtions facet.
	* @param tenant tenant
	* @param channel channel
	* @param searchContext search interactions context
	* @param backgroundSearchContext background search context
	* @param textElementType text element type : Topics/Relations
	* @param hierarchyLevelNumber the number of hierarchy
	* @param textElements text elements
	* @param metricsToCalc metrics to calculate
	* @param orderMetric metric to sort by
	* @param speakerType speaker type
	* @param sameUtteranceMode same utterance search mode
	* @param leavesOnly calculate metrics of leaves only
	* @param rootLevelLimit number elements for top level
	* @param descendantsLimit number of elements for descending levels
	* @return facets
	 */
	public abstract Pair<List<TextElementsFacetNode>, List<TextElementsFacetNode>> getTextElementsFacetWithStats(String tenant, String channel, SearchInteractionsContext searchContext, SearchInteractionsContext backgroundSearchContext,
																					TextElementType textElementType, int hierarchyLevelNumber, List<TextElementsFacetNode> textElements,
																					List<TextElementMetricType> metricsToCalc, TextElementMetricType orderMetric,
																					SpeakerQueryType speakerType, boolean sameUtteranceMode, boolean leavesOnly, int rootLevelLimit, int descendantsLimit);



	/**
	 * Generates hierarchical structure of text elemements nodes from flat list.
	 * @param textElementsNodes text elements flast list
	 * @return text elements tree
	 */
	protected List<TextElementsFacetNode> generateTexElementsFacetTree(List<TextElementsFacetNode> textElementsNodes) {
		List<TextElementsFacetNode> textElementsFacetTree = null;

		Integer maxLevel = 0;
		Integer parentLevel = 0;
		String parent;

		if (!CollectionUtils.isEmpty(textElementsNodes)) {

			Optional<Integer> maxOpt = textElementsNodes.parallelStream().map(n -> n.getLevel()).max((d1, d2) -> Integer.compare(d1, d2));
			if (maxOpt.isPresent()) {
				maxLevel = maxOpt.get();
			}

			val textElementsMultimapList = new ArrayList<Multimap<String, TextElementsFacetNode>>();
			if (maxLevel > 0) {
				// create all needed multimaps
				for (int j = 0; j <= maxLevel; j++) {
					textElementsMultimapList.add(ArrayListMultimap.create());
				}

				// add each element to his relevant level, and to his relevant parent
				for (TextElementsFacetNode textElementNode : textElementsNodes) {
					Integer level = textElementNode.getLevel();

					parentLevel = level - 1;
					parent = (textElementNode.getValue().replaceFirst(level.toString(), parentLevel.toString())).substring(0, textElementNode.getValue().lastIndexOf(slash));
					textElementsMultimapList.get(level).put(parent, textElementNode);
				}

				textElementsFacetTree = generateTexElementsFacetTree(textElementsMultimapList);
			}
		}

		return textElementsFacetTree;
	}

	/**
	 * Generates Text Elements Tree from mapping of levels and Text Elements.
	 * @param textElementsMultimapList mapping of levels and Text Elements Facet nodes.
	 * @return tree structure of Text Elements.
	 */
	private List<TextElementsFacetNode> generateTexElementsFacetTree(ArrayList<Multimap<String, TextElementsFacetNode>> textElementsMultimapList) {
		List<TextElementsFacetNode> hashMapAsList;
		Multimap<String, TextElementsFacetNode> multimap, childMultimap;

		// go over all the levels hashes
		for (int i = 1; i < textElementsMultimapList.size(); i++) {
			multimap = textElementsMultimapList.get(i);

			//if this is NOT the last hash
			if (i + 1 < textElementsMultimapList.size()) {

				// current hash to list - so we can iterate the list and find the children for each element
				hashMapAsList = new ArrayList<TextElementsFacetNode>(multimap.values());

				// the next level hash
				childMultimap = textElementsMultimapList.get(i + 1);

				// go over the list and find the children for each element
				for (TextElementsFacetNode textElementsFacetNode : hashMapAsList) {
					val children = childMultimap.get(textElementsFacetNode.getValue());
					textElementsFacetNode.getChildren().addAll(children);
				}
			}
		}

		return new ArrayList<TextElementsFacetNode>(textElementsMultimapList.get(1).values());
	}
}
