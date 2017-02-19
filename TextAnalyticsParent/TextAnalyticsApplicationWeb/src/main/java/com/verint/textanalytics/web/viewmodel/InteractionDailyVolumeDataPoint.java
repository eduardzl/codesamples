package com.verint.textanalytics.web.viewmodel;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author EZlotnik Represents an Interactions volume for a specific date.
 */
@NoArgsConstructor
public class InteractionDailyVolumeDataPoint {

	@Getter
	@Setter
	@Accessors(chain = true)
	private long date;

	@Getter
	@Setter
	@Accessors(chain = true)
	private Double value;
}
