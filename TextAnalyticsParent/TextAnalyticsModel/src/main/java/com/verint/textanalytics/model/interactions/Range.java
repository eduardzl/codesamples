package com.verint.textanalytics.model.interactions;

import lombok.Getter;
import lombok.Setter;

/**
 * @author EZlotnik Represents a range for values.
 */
public class Range {

	@Getter
	@Setter
	private String lowerValue;

	@Getter
	@Setter
	private String isLowerInclusive;

	@Getter
	@Setter
	private String upperValue;

	@Getter
	@Setter
	private String isUpperInclusive;

	@Getter
	@Setter
	private String key;

	@Getter
	@Setter
	private String titleKey;
}
