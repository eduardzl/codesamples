package com.verint.textanalytics.model.interactions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * represents autocomplete suggestion.
 * @author yzanis
 *
 */
@AllArgsConstructor
@NoArgsConstructor
public class WeightedSuggestion {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String text;

	@Getter
	@Setter
	@Accessors(chain = true)
	private double weight;

}