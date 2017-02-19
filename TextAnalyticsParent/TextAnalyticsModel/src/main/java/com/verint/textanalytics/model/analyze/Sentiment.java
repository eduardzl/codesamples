package com.verint.textanalytics.model.analyze;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Sentiment.
 * 
 * @author imor
 *
 */
@AllArgsConstructor
@NoArgsConstructor
public class Sentiment {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String sentiment;

	@Getter
	@Setter
	@Accessors(chain = true)
	private double count;
}
