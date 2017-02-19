package com.verint.textanalytics.model.facets;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by TBaum on 8/10/2016.
 */
public class TextElementFacetResult {

	@Getter
	@Setter
	private List<TextElementsFacetNode> textElementsFacetNodeList;

	@Getter
	@Setter
	private boolean basedOnSample;


	/**
	 * Constructor.
	 */
	public TextElementFacetResult() {

		this.basedOnSample = false;
	}


}
