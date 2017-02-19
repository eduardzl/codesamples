package com.verint.textanalytics.web.viewmodel;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author imor View Model class representing a node in Entities Facet Tree Map.
 */
public class EntityFacetTreeMapNode {
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
	private List<MetricData> metrics;

	private List<EntityFacetTreeMapNode> children;

	public List<? extends EntityFacetTreeMapNode> getChildren() {
		return this.children;
	}

	/**
	 * Sets a list of children.
	 * @param chld
	 *            list of children to set.
	 * @return an object itself
	 */
	public EntityFacetTreeMapNode setChildren(List<? extends EntityFacetTreeMapNode> chld) {
		@SuppressWarnings("unchecked")
		List<EntityFacetTreeMapNode> convertedChildren = (List<EntityFacetTreeMapNode>) chld;
		this.children = convertedChildren;

		return this;
	}

	/**
	 * Appends a child node.
	 * @param child
	 *            node to add
	 */
	public void addChild(EntityFacetTreeMapNode child) {
		this.children.add(child);
	}
}
