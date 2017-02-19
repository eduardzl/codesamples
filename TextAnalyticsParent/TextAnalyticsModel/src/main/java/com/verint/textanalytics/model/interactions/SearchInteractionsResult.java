package com.verint.textanalytics.model.interactions;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Result of Search interactions.
 * @author EZlotnik
 *
 */
public class SearchInteractionsResult {
	@Setter
	@Getter
	private List<Interaction> interactions;

	@Setter
	@Getter
	private int totalNumberFound;

	@Setter
	@Getter
	private Double maxScore;
}
