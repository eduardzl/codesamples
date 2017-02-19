package com.verint.textanalytics.web.viewmodel;

import com.verint.textanalytics.model.analyze.MetricType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Represents View Model data Result Set metric.
 * @author NShunewich
 *
 */
@AllArgsConstructor
public class Metric {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String displayKey;

	@Getter
	@Setter
	@Accessors(chain = true)
	private double currentSearchValue;

	@Getter
	@Setter
	@Accessors(chain = true)
	private double backgroundValue;

	@Getter
	@Setter
	@Accessors(chain = true)
	private double percentage;

	@Getter
	@Setter
	@Accessors(chain = true)
	private MetricType metricType;
}
