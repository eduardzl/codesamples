package com.verint.textanalytics.model.facets;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.verint.textanalytics.common.utils.StringUtils;
import com.verint.textanalytics.model.analyze.MetricData;

/**
 * Represents Entity node in Entities facet.
 * @author EZlotnik
 *
 */
@Accessors(chain = true)
public class TextElementsFacetNode {
	private final static String slash = "/";
	
	@Getter
	@Setter
	private String value;

	@Getter
	@Setter
	private String leaveValue;

	@Getter
	@Setter
	private String name;

	@Getter
	@Setter
	private int level;

	@Getter
	@Setter
	private double percentage;

	@Getter
	@Setter
	private int numberOfInteractions;

	@Getter
	@Setter
	private List<MetricData> metrics;

	@Getter
	@Setter
	private List<TextElementsFacetNode> children;

	@Getter
	@Setter
	private double correlationPercentage;

	/**
	 * Constructor.
	 */
	private TextElementsFacetNode() {
		this.children = new ArrayList<TextElementsFacetNode>();
		this.metrics = new ArrayList<MetricData>();
	}

	/**
	 * Adds metric to list of text element metric.
	 * @param metricData  metric
	 */
	public void addMetric(MetricData metricData) {
		this.metrics.add(metricData);
	}

	/**
	 * Adds child node.
	 * @param childNode
	 *            node to add
	 */
	public void addChild(TextElementsFacetNode childNode) {
		this.children.add(childNode);
	}

	/**
	 * @param metricName
	 *            metricName
	 * @return double
	 */
	public double getMetricValue(String metricName) {
		for (MetricData metric : this.metrics) {
			if (metric.getName().equals(metricName)) {
				return metric.getValue();
			}
		}
		return Double.NaN;
	}

	/**
	 * @param metric
	 *            metric
	 * @return double
	 */
	public double getMetricValue(TextElementMetricType metric) {
		return getMetricValue(metric.toString());
	}

	/**
	 * Builder method.
	 * @param lineage text element path
	 * @param leavesOnly is leaves only text element
	 * @return generated entity node.
	 */
	public static TextElementsFacetNode buildFromPathString(String lineage, Boolean leavesOnly) {
		TextElementsFacetNode textElement = null;
		if (!StringUtils.isNullOrBlank(lineage)) {
			String[] pathTokens = lineage.split("/");

			textElement = new TextElementsFacetNode();

			if (!leavesOnly) {
				textElement.setValue(lineage);
				// the element name is the last token
				textElement.setName(pathTokens[pathTokens.length - 1]);
				textElement.setLevel(Integer.parseInt(pathTokens[0]));
			} else {
				// in "Leaves Only" mode the path doesn't include level number
				// and element is in last level
				textElement.setLeaveValue(lineage);
				textElement.setLevel(pathTokens.length - 1);

				textElement.setValue(textElement.getLevel() + lineage);
				// the element name is the last token
				textElement.setName(pathTokens[pathTokens.length - 1]);
			}
		}

		return textElement;
	}

	/**
	 * Generates prefix to query children of this node.
	 * @return prefix to query children
	 */
	public String getChildrenPrefix() {
		String childrenPrefix = "";

		if (!StringUtils.isNullOrBlank(this.value)) {
			String[] parts = StringUtils.split(this.value, slash);

			if (parts.length > 0) {
				parts[0] = Integer.toString(Integer.parseInt(parts[0]) + 1);
				childrenPrefix = StringUtils.concat(parts, slash);
			}
		}

		return childrenPrefix;
	}
}
