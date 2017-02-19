package com.verint.textanalytics.web.viewmodel;

import com.verint.textanalytics.model.trends.TrendChangeDirection;
import com.verint.textanalytics.model.trends.TrendType;

import lombok.Getter;
import lombok.Setter;

/**
 * Entity, Relations or Category Trend.
 * 
 * @author NShunewich
 *
 */
public class TextElementTrend {

	@Getter
	@Setter
	private String value;

	@Getter
	@Setter
	private String name;

	@Getter
	@Setter
	private int sentiment;

	@Getter
	@Setter
	private int prefix;

	// The number of docs the specified topi c appears in
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
	private double absChangePercentage;

	// Absolute Topic Volume Change (Percentage) / Background Topic Volume
	// Percentage * 100
	@Getter
	@Setter
	private double relChangePercentage;

	@Getter
	@Setter
	private TrendType trendType;

	@Getter
	@Setter
	private boolean leaf;
}
