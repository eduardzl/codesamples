package com.verint.textanalytics.model.analyze;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Represents metric data.
 * 
 * @author NShunewich
 *
 */
@AllArgsConstructor
@NoArgsConstructor
public class MetricData {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String name;

	@Getter
	@Setter
	@Accessors(chain = true)
	private double value;
}
