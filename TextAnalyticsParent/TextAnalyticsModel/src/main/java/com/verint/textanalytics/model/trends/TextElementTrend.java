package com.verint.textanalytics.model.trends;

import lombok.Getter;
import lombok.Setter;

/**
 * Entity trend.
 * 
 * @author EZlotnik
 *
 */
public class TextElementTrend {

	@Getter
	@Setter
	private String name;

	// The number of docs the specified topic appears in
	// according to the specified date-range
	@Getter
	@Setter
	private double volume;

	// The volume percentage change
	// according to the specified date-range
	@Getter
	@Setter
	private double prVolume;

	// The number of docs (within the result set) in the specified trend period
	// date-range (monthly, weekly, etc.)
	@Getter
	@Setter
	private int periodVolume;

	@Getter
	@Setter
	// volume / periodVolume
	private double periodPercentage;

	// The Average number of docs (within the result set) the specified topic
	// appears in according to the "previous" (background) date-range.
	@Getter
	@Setter
	private double backgroundAverageVolume;

	@Getter
	@Setter
	// The Average number of docs (within the result set) in the specified
	// "previous" trend period (background) date-range.
	private double backgroundPeriodAverageVolume;

	// backgroundAverageVolume / backgroundPeriodAverageVolume
	@Getter
	@Setter
	private double backgroundVolumePercentage;

	// sign (Topic Volume Percentage - Background Topic Volume Percentage)
	@Getter
	@Setter
	private TrendChangeDirection changeDirection;

	// Topic Volume Percentage - Background Topic Volume Percentage
	@Getter
	@Setter
	private double absoluteVolumeChange;

	// Absolute Topic Volume Change (Percentage) / Background Topic Volume
	// Percentage * 100
	@Getter
	@Setter
	private double relativeVolumeChange;

	@Getter
	@Setter
	private double bgSentiment;

	@Getter
	@Setter
	private double vSentiment;
	
	@Getter
	@Setter
	private TrendType trendType;

	@Getter
	@Setter
	private String value;
}
