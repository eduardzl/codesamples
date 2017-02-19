package com.verint.textanalytics.web.viewmodel;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Suggestions result.
 * @author EZlotnik
 * @param <T>
 *            Type of data to be stored in List
 */
@NoArgsConstructor
public class WeightedSuggestionsDataResult<T> extends ListDataResult<T> {
	@Getter
	@Setter
	private Double maxWeight;

}
