package com.verint.textanalytics.model.analyze;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Represents Metric.
 * 
 * @author imor
 *
 */
@AllArgsConstructor
@NoArgsConstructor
public class TextElementSentimentsMetric {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String textElement;

	@Getter
	@Setter
	@Accessors(chain = true)
	private double volume;

	@Getter
	@Setter
	@Accessors(chain = true)
	private double prVolume;

	@Getter
	@Setter
	@Accessors(chain = true)
	private double sentimentAvg;

	@Getter
	@Setter
	@Accessors(chain = true)
	private double sentimentAvgIncludingNuetral;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<Sentiment> sentimentCount;

}
