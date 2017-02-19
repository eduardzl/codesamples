package com.verint.textanalytics.model.facets;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * FacetRangeResultGroup.
 * 
 * @author imor
 *
 */
public class FacetRangeResultGroup extends FacetResultGroup {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String lowerValue;

	@Getter
	@Setter
	@Accessors(chain = true)
	private boolean isLowerInclusive;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String upperValue;

	@Getter
	@Setter
	@Accessors(chain = true)
	private boolean isUpperInclusive;
}
