package com.verint.textanalytics.model.analyze;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Represents Metric.
 * 
 * @author NShunewich
 *
 */
@AllArgsConstructor
@NoArgsConstructor
public class MetricDataChange {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String name;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String displayKey;

	@Getter
	@Setter
	@Accessors(chain = true)
	private double backgroundValue;

	@Getter
	@Setter
	@Accessors(chain = true)
	private double currentSearchValue;
}
