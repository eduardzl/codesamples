package com.verint.textanalytics.web.viewmodel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Represents metric limits data.
 * 
 * @author imor
 *
 */
@AllArgsConstructor
public class MetricLimitsData {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String name;

	@Getter
	@Setter
	@Accessors(chain = true)
	private double max = Double.MIN_VALUE;

	@Getter
	@Setter
	@Accessors(chain = true)
	private double min = Double.MAX_VALUE;

	/**
	 * Constructor.
	 * @param metricName
	 *            metric name
	 */
	public MetricLimitsData(String metricName) {
		this.name = metricName;
	}
}
