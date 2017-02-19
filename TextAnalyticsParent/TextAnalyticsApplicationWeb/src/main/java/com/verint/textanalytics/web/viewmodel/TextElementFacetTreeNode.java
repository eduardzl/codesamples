package com.verint.textanalytics.web.viewmodel;

import lombok.Getter;
import lombok.Setter;

import lombok.experimental.Accessors;

/**
 * @author EZlotnik View Model class representing a node in Text Elements Facet Tree.
 */
public class TextElementFacetTreeNode extends TextElementFacetNode {

	@Getter
	@Setter
	@Accessors(chain = true)
	private boolean leaf;

	@Getter
	@Setter
	@Accessors(chain = true)
	private boolean expanded;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String iconCls;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String cls;

	@Getter
	@Setter
	private boolean basedOnSample;
}
