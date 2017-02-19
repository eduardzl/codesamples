package com.verint.textanalytics.model.facets;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * FacetSingleValueResultGroup.
 * 
 * @author imor
 *
 */
public class FacetSingleValueResultGroup extends FacetResultGroup {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String value;
}
