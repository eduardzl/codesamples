package com.verint.textanalytics.web.viewmodel;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/***
 * Facet node.
 * @author TBaum
 *
 */
public class FacetWeightGraphNode {

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<FacetWeightGraphNode> values;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int sum;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String key;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String value;

}
