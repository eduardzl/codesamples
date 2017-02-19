package com.verint.textanalytics.web.uiservices;

import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.model.facets.TextElementType;
import com.verint.textanalytics.model.facets.TextElementMetricType;
import com.verint.textanalytics.model.facets.TextElementsFacetNode;
import com.verint.textanalytics.web.viewmodel.TextElementFacetNode;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.List;

/**
 * @author imor
 */
public class ViewModelFilter {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	@Setter
	@Getter
	private ConfigurationManager configurationManager;

	private Comparator<? super TextElementsFacetNode> volumeComparator = new Comparator<TextElementsFacetNode>() {
		@Override
		public int compare(TextElementsFacetNode o1, TextElementsFacetNode o2) {
			return o2.getNumberOfInteractions() - o1.getNumberOfInteractions();
		}
	};

	private Comparator<? super TextElementsFacetNode> messageCountComparator = new Comparator<TextElementsFacetNode>() {
		@Override
		public int compare(TextElementsFacetNode o1, TextElementsFacetNode o2) {
			double d1 = 0, d2 = 0;

			d1 = o1.getMetricValue(TextElementMetricType.AvgMessagesCount);
			d2 = o2.getMetricValue(TextElementMetricType.AvgMessagesCount);

			return Double.compare(d2, d1);
		}
	};

	private Comparator<? super TextElementsFacetNode> handleTimeComparator = new Comparator<TextElementsFacetNode>() {
		@Override
		public int compare(TextElementsFacetNode o1, TextElementsFacetNode o2) {
			double d1 = 0, d2 = 0;

			d1 = o1.getMetricValue(TextElementMetricType.AvgHandleTime);
			d2 = o2.getMetricValue(TextElementMetricType.AvgHandleTime);

			return Double.compare(d2, d1);
		}
	};

	private Comparator<? super TextElementsFacetNode> correlationPercentageComparator = new Comparator<TextElementsFacetNode>() {
		@Override
		public int compare(TextElementsFacetNode o1, TextElementsFacetNode o2) {
			double d1 = 0, d2 = 0;

			d1 = o1.getMetricValue(TextElementMetricType.CorrelationPercentage);
			d2 = o2.getMetricValue(TextElementMetricType.CorrelationPercentage);

			return Double.compare(d2, d1);
		}
	};

	/**
	 * ViewModelConverter.
	 */
	public ViewModelFilter() {
	}

	/**
	 * @param lstTextElements lstTextElements
	 * @param textElement     textElement
	 * @param leavesOnly      leavesOnly
	 */
	@Deprecated
	public void applyFilterByTopLimitOnTextElementFacetNode(List<? extends TextElementFacetNode> lstTextElements, TextElementType textElement, boolean leavesOnly) {

		Limits limits = getLimits(textElement, leavesOnly);

		// apply filter by TopLimit
		if (lstTextElements != null) {
			// cut the list
			lstTextElements.subList(Math.min(lstTextElements.size(), limits.getRootTopLimit()), lstTextElements.size()).clear();

			for (val textElementNode : lstTextElements) {
				if (textElementNode.getChildren() != null) {
					textElementNode.setChildren(applyTopLimitFilterOnTextElementFacetNodeDescendants(textElementNode.getChildren(), limits.getDescendantsTopLimit()));
				}
			}
		}
	}

	/**
	 * @param lstTextElements lstTextElements
	 * @param textElement     textElement
	 * @param leavesOnly      leavesOnly
	 */
	public void applyFilterByPercentageOnTextElementFacetNode(List<? extends TextElementFacetNode> lstTextElements, TextElementType textElement, boolean leavesOnly) {

		Limits limits = getLimits(textElement, leavesOnly);

		// apply filter by percentage
		if (lstTextElements != null) {
			// cut the list
			int i;
			for (i = 0; i < lstTextElements.size(); i++) {
				if (lstTextElements.get(i).getPercentage() < limits.getRootPercentageLimit()) {
					break;
				}
			}

			lstTextElements.subList(i, lstTextElements.size()).clear();

			for (val textElementNode : lstTextElements) {
				if (textElementNode.getChildren() != null) {
					textElementNode.setChildren(applyPercentageLimitFilterOnTextElementFacetNodeDescendants(textElementNode.getChildren(), limits.getDescendantsPercentageLimit()));
				}
			}
		}

	}

	private List<? extends TextElementFacetNode> applyTopLimitFilterOnTextElementFacetNodeDescendants(List<? extends TextElementFacetNode> lstTextElements, int descendantsTopLimit) {

		if (lstTextElements != null) {
			// cut the descendants list
			lstTextElements.subList(Math.min(lstTextElements.size(), descendantsTopLimit), lstTextElements.size()).clear();

			for (val textElementNode : lstTextElements) {
				if (textElementNode.getChildren() != null) {
					textElementNode.setChildren(applyTopLimitFilterOnTextElementFacetNodeDescendants(textElementNode.getChildren(), descendantsTopLimit));
				}
			}
		}
		return lstTextElements;
	}

	private List<? extends TextElementFacetNode> applyPercentageLimitFilterOnTextElementFacetNodeDescendants(List<? extends TextElementFacetNode> lstTextElements, double descendantsPercentageLimit) {

		if (lstTextElements != null) {
			// cut the descendants list
			int i;
			for (i = 0; i < lstTextElements.size(); i++) {
				if (lstTextElements.get(i).getPercentage() < descendantsPercentageLimit) {
					break;
				}
			}
			lstTextElements.subList(i, lstTextElements.size()).clear();

			for (val textElementNode : lstTextElements) {
				if (textElementNode.getChildren() != null) {
					textElementNode.setChildren(applyPercentageLimitFilterOnTextElementFacetNodeDescendants(textElementNode.getChildren(), descendantsPercentageLimit));
				}
			}
		}
		return lstTextElements;
	}

	/**
	 * @param lstTextElementsNodes lstTextElementsNodes
	 * @param textElement          textElement
	 * @param leavesOnly           leavesOnly
	 */
	@Deprecated
	public void applyFilterByTopLimitOnTextElementsFacetNode(List<TextElementsFacetNode> lstTextElementsNodes, TextElementType textElement, boolean leavesOnly) {

		Limits limits = getLimits(textElement, leavesOnly);

		// apply filter by TopLimit
		if (lstTextElementsNodes != null) {
			// cut the list
			lstTextElementsNodes.subList(Math.min(lstTextElementsNodes.size(), limits.getRootTopLimit()), lstTextElementsNodes.size()).clear();

			for (val textElementNode : lstTextElementsNodes) {
				if (textElementNode.getChildren() != null) {
					textElementNode.setChildren(applyTopLimitFilterOnTextElementsFacetNodeDescendants(textElementNode.getChildren(), limits.getDescendantsTopLimit()));
				}
			}
		}
	}

	/**
	 * @param lstTextElementsNodes lstTextElementsNodes
	 * @param textElement          textElement
	 * @param leavesOnly           leavesOnly
	 */
	@Deprecated
	public void applyFilterByPercentageOnTextElementsFacetNode(List<TextElementsFacetNode> lstTextElementsNodes, TextElementType textElement, boolean leavesOnly) {

		Limits limits = getLimits(textElement, leavesOnly);

		// apply filter by percentage		
		if (lstTextElementsNodes != null) {
			// cut the list
			int i;
			for (i = 0; i < lstTextElementsNodes.size(); i++) {
				if (lstTextElementsNodes.get(i).getPercentage() < limits.getRootPercentageLimit()) {
					break;
				}
			}

			lstTextElementsNodes.subList(i, lstTextElementsNodes.size()).clear();

			for (val textElementNode : lstTextElementsNodes) {
				if (textElementNode.getChildren() != null) {
					textElementNode.setChildren(
							applyPercentageLimitFilterOnTextElementsFacetNodeDescendants(textElementNode.getChildren(), limits.getDescendantsPercentageLimit()));
				}
			}
		}

	}

	private List<TextElementsFacetNode> applyTopLimitFilterOnTextElementsFacetNodeDescendants(List<TextElementsFacetNode> lstTextElementsNodes, int descendantsTopLimit) {
		if (lstTextElementsNodes != null) {
			// cut the descendants list
			lstTextElementsNodes.subList(Math.min(lstTextElementsNodes.size(), descendantsTopLimit), lstTextElementsNodes.size()).clear();

			for (val textElementNode : lstTextElementsNodes) {
				if (textElementNode.getChildren() != null) {
					textElementNode.setChildren(applyTopLimitFilterOnTextElementsFacetNodeDescendants(textElementNode.getChildren(), descendantsTopLimit));
				}
			}
		}
		return lstTextElementsNodes;
	}

	private List<TextElementsFacetNode> applyPercentageLimitFilterOnTextElementsFacetNodeDescendants(List<TextElementsFacetNode> lstTextElementsNodes, double descendantsPercentageLimit) {
		if (lstTextElementsNodes != null) {
			// cut the descendants list
			int i;
			for (i = 0; i < lstTextElementsNodes.size(); i++) {
				if (lstTextElementsNodes.get(i).getPercentage() < descendantsPercentageLimit) {
					break;
				}
			}
			lstTextElementsNodes.subList(i, lstTextElementsNodes.size()).clear();

			for (val textElementNode : lstTextElementsNodes) {
				if (textElementNode.getChildren() != null) {
					textElementNode.setChildren(applyPercentageLimitFilterOnTextElementsFacetNodeDescendants(textElementNode.getChildren(), descendantsPercentageLimit));
				}
			}
		}
		return lstTextElementsNodes;
	}

	/**
	 * @param textElementsFacetNodes textElementsFacetNodes
	 * @param size                   size
	 */
	@Deprecated
	public void applySortOnTextElementsFacetNode(List<TextElementsFacetNode> textElementsFacetNodes, TextElementMetricType size) {

		if (textElementsFacetNodes == null || size == null) {
			return;
		}

		Comparator<? super TextElementsFacetNode> comparator = null;
		switch (size) {
			case Volume:
				comparator = volumeComparator;
				break;
			case AvgHandleTime:
				comparator = handleTimeComparator;
				break;
			case AvgMessagesCount:
				comparator = messageCountComparator;
				break;
			case CorrelationPercentage:
				comparator = messageCountComparator;
				break;
			default:
				logger.error("No comperator to apply when tring applySortOnTextElementsFacetNode");
				break;
		}

		textElementsFacetNodes.sort(comparator);

		for (TextElementsFacetNode textElementsFacetNode : textElementsFacetNodes) {
			if (textElementsFacetNode.getChildren() != null)
				applySortOnTextElementsFacetNode(textElementsFacetNode.getChildren(), size);
		}
	}

	/**
	 * @author imor
	 */
	private class Limits {
		@Setter
		@Getter
		private int rootTopLimit = Integer.MAX_VALUE;
		@Setter
		@Getter
		private int descendantsTopLimit = Integer.MAX_VALUE;
		@Setter
		@Getter
		private double rootPercentageLimit = 0;
		@Setter
		@Getter
		private double descendantsPercentageLimit = 0;
	}

	/**
	 * @param textElement textElement
	 * @param leavesOnly  leavesOnly
	 * @return Limits
	 */
	private Limits getLimits(TextElementType textElement, boolean leavesOnly) {
		val limits = new Limits();

		val applicationConfiguration = configurationManager.getApplicationConfiguration();
		// find the filter
		switch (textElement) {
			case Entities:
				if (leavesOnly) {
					limits.setRootTopLimit(applicationConfiguration.getEntitiesFacetLeavesTopLimit());
					limits.setRootPercentageLimit(applicationConfiguration.getEntitiesFacetLeavesPercentageLimit());
					break;
				} else {
					limits.setRootTopLimit(applicationConfiguration.getEntitiesFacetRootTopLimit());
					limits.setDescendantsTopLimit(applicationConfiguration.getEntitiesFacetDescendantsTopLimit());
					limits.setRootPercentageLimit(applicationConfiguration.getEntitiesFacetRootPercentageLimit());
					limits.setDescendantsPercentageLimit(applicationConfiguration.getEntitiesFacetDescendantsPercentageLimit());
					break;
				}
			case Relations:
				if (leavesOnly) {
					limits.setRootTopLimit(applicationConfiguration.getRelationsFacetLeavesTopLimit());
					limits.setRootPercentageLimit(configurationManager.getApplicationConfiguration().getRelationsFacetLeavesPercentageLimit());
					break;
				} else {
					limits.setRootTopLimit(applicationConfiguration.getRelationsFacetRootTopLimit());
					limits.setDescendantsTopLimit(applicationConfiguration.getRelationsFacetDescendantsTopLimit());
					limits.setRootPercentageLimit(applicationConfiguration.getRelationsFacetRootPercentageLimit());
					limits.setDescendantsPercentageLimit(applicationConfiguration.getRelationsFacetDescendantsPercentageLimit());
					break;
				}
			default:
				throw new IllegalArgumentException("textElement is not defined");
		}
		return limits;
	}

}
