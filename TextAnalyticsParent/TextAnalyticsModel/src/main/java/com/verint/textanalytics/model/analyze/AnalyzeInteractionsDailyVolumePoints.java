package com.verint.textanalytics.model.analyze;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.DateTime;

/**
 * Contains information about daily volume graph.
 * 
 * @author NShunewich
 *
 */
@AllArgsConstructor
public class AnalyzeInteractionsDailyVolumePoints {
	@Getter
	@Setter
	@Accessors(chain = true)
	private DateTime date;

	@Getter
	@Setter
	@Accessors(chain = true)
	private Double backgroundValue;

	@Getter
	@Setter
	@Accessors(chain = true)
	private Double contextValue;

}
