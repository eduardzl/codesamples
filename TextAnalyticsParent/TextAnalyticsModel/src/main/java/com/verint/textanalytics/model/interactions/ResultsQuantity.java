package com.verint.textanalytics.model.interactions;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * ResultsQuantity as view model.
 * 
 * @author imor
 *
 */
public class ResultsQuantity {

	@Getter
	@Setter
	@Accessors(chain = true)
	private int totalInteractions;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int currentSearchInteractions;
}
