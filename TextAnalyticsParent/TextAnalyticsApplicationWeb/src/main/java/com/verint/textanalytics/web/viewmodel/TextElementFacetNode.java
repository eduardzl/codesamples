package com.verint.textanalytics.web.viewmodel;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author EZlotnik View Model class representing a node in Text Element Facet Tree.
 */
public class TextElementFacetNode {
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
	private double correlationPercentage;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int numberOfInteractions;

	private List<TextElementFacetNode> children;

	public List<? extends TextElementFacetNode> getChildren() {
		return this.children;
	}

	/**
	 * Sets a list of children.
	 * @param chld
	 *            list of children to set.
	 * @return an object itself
	 */
	public TextElementFacetNode setChildren(List<? extends TextElementFacetNode> chld) {
		@SuppressWarnings("unchecked")
		List<TextElementFacetNode> convertedChildren = (List<TextElementFacetNode>) chld;
		this.children = convertedChildren;

		return this;
	}

	/**
	 * Appends a child node.
	 * @param child
	 *            node to add
	 */
	public void addChild(TextElementFacetNode child) {
		this.children.add(child);
	}
}
