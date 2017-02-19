package com.verint.textanalytics.model.facets;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * ValuesRange.
 * 
 * @author imor
 *
 */
public class ValuesRange {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String queryKey;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String key;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String titleKey;

	// Lower Value
	@Getter
	@Setter
	@Accessors(chain = true)
	private String lowerValue;

	@Getter
	@Setter
	@Accessors(chain = true)
	private boolean isLowerInclusive;

	// Upper Value
	@Getter
	@Setter
	@Accessors(chain = true)
	private String upperValue;

	@Getter
	@Setter
	@Accessors(chain = true)
	private boolean isUpperInclusive;
}
