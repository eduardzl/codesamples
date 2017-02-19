package com.verint.textanalytics.web.viewmodel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Represents View Model data for Analyze Daily Volume chart.
 * 
 * @author NShunewich
 *
 */
@AllArgsConstructor
public class EntityMetric {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String entity;

	@Getter
	@Setter
	@Accessors(chain = true)
	private double x;

	@Getter
	@Setter
	@Accessors(chain = true)
	private double y;
}
