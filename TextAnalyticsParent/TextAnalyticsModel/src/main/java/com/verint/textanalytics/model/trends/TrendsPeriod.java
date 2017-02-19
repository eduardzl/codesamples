package com.verint.textanalytics.model.trends;

import lombok.Getter;

/**
 * Trends period.
 *
 * @author EZlotnik
 */
public enum TrendsPeriod {

	// @formatter:off
	HALF_HOURLY(0), HOURLY(1), DAILY(2), WEEKLY(3), MONTHLY(4), QUARTERLY(5), YEARLY(6);
	// @formatter:on

	@Getter
	private int trendsPeriod;

	private TrendsPeriod(int value) {
		this.trendsPeriod = value;
	}
}
