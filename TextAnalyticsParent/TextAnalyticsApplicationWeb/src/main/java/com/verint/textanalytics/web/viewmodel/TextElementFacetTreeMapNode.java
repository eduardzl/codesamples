package com.verint.textanalytics.web.viewmodel;

import java.util.*;

import com.verint.textanalytics.model.facets.TextElementType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author imor View Model class representing a node in Entities Facet Tree Map.
 */
public class TextElementFacetTreeMapNode {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String value;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String text;

	@Getter
	@Setter
	private int level;

	@Getter
	@Setter
	@Accessors(chain = true)
	private TextElementType type;

	@Getter
	@Setter
	private Map<String, com.verint.textanalytics.web.viewmodel.MetricData> metrics;

	private List<TextElementFacetTreeMapNode> children;

	/**
	 * C'tor.
	 */
	public TextElementFacetTreeMapNode() {
		this.metrics = new HashMap<>();
	}

	/**
	 * Add metric data to hash.
	 * @param metricName
	 *            metric name
	 * @param volumeMetric
	 *            metric data
	 */
	public void addMetric(String metricName, com.verint.textanalytics.web.viewmodel.MetricData volumeMetric) {
		if (!this.metrics.containsKey(metricName)) {
			this.metrics.put(metricName, volumeMetric);
		}
	}

	/**
	 * Retrieves metric value.
	 * @param metricName metric name to retrieve value
	 * @return metric value
	 */
	public double getMetricValue(String metricName) {
		if (this.metrics.containsKey(metricName)) {
			return this.metrics.get(metricName).getValue();
		}

		return 0;
	}

	public List<? extends TextElementFacetTreeMapNode> getChildren() {
		return this.children;
	}

	/**
	 * Sets a list of children.
	 * @param chld
	 *            list of children to set.
	 * @return an object itself
	 */
	public TextElementFacetTreeMapNode setChildren(List<? extends TextElementFacetTreeMapNode> chld) {
		@SuppressWarnings("unchecked")
		List<TextElementFacetTreeMapNode> convertedChildren = (List<TextElementFacetTreeMapNode>) chld;
		this.children = convertedChildren;

		return this;
	}

	/**
	 * Appends a child node.
	 * @param child
	 *            node to add
	 */
	public void addChild(TextElementFacetTreeMapNode child) {
		this.children.add(child);
	}
}
