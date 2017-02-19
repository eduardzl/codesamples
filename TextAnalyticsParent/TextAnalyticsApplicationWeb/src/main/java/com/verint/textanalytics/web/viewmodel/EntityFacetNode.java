package com.verint.textanalytics.web.viewmodel;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.experimental.Accessors;

import com.verint.textanalytics.common.utils.*;

/**
 * @author EZlotnik View Model class representing a node in Entities Facet Tree.
 */
public class EntityFacetNode {
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
	@Accessors(chain = true)
	private double percentage;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int numberOfInteractions;

	private List<EntityFacetNode> children;

	public List<? extends EntityFacetNode> getChildren() {
		return this.children;
	}

	/**
	 * Sets a list of children.
	 * @param chld
	 *            list of children to set.
	 * @return an object itself
	 */
	public EntityFacetNode setChildren(List<? extends EntityFacetNode> chld) {
		@SuppressWarnings("unchecked")
		List<EntityFacetNode> convertedChildren = (List<EntityFacetNode>) chld;
		this.children = convertedChildren;

		return this;
	}

	/**
	 * Appends a child node.
	 * @param child
	 *            node to add
	 */
	public void addChild(EntityFacetNode child) {
		this.children.add(child);
	}
}
