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
public class AnalyzeDailyVolumeDataPoints {

	@Getter
	@Setter
	@Accessors(chain = true)
	private long date;

	@Getter
	@Setter
	@Accessors(chain = true)
	private Double backgroundValue;

	@Getter
	@Setter
	@Accessors(chain = true)
	private Double contextValue;

	@Getter
	@Setter
	@Accessors(chain = true)
	private Double contextPercentage;

}
