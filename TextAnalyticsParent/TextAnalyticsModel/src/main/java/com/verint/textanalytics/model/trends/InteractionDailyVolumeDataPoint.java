package com.verint.textanalytics.model.trends;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.DateTime;

import com.verint.textanalytics.common.utils.DataUtils;

/**
 * @author EZlotnik Represents an Interactions volume for a single date. a value
 *         type parameter
 */
@NoArgsConstructor
public class InteractionDailyVolumeDataPoint {

	@Getter
	@Setter
	@Accessors(chain = true)
	private DateTime date;

	@Getter
	@Setter
	@Accessors(chain = true)
	private Double value;

	/**
	 * C'tor for InteractionDailyVolumeDataPoint.
	 * 
	 * @param strDate
	 *            date in string representation.
	 * @param volume
	 *            call interactions volume
	 */
	public InteractionDailyVolumeDataPoint(String strDate, Double volume) {
		this.date = DataUtils.getDateTimeFromDayOfDateString(strDate).withTimeAtStartOfDay();
		this.value = volume;
	}

	/**
	 * C'tor for InteractionDailyVolumeDataPoint.
	 * 
	 * @param date
	 *            date
	 * @param volume
	 *            call interactions volume
	 */
	public InteractionDailyVolumeDataPoint(DateTime date, Double volume) {
		this.date = date;
		this.value = volume;
	}
}
