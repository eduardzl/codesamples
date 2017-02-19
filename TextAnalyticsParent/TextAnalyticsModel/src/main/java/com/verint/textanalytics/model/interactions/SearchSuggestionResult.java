package com.verint.textanalytics.model.interactions;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author yzanis
 *
 */
public class SearchSuggestionResult {

	@Setter
	@Getter
	private List<SearchSuggestion> suggestions;

	@Setter
	@Getter
	private int totalNumberFound;

	@Setter
	@Getter
	private boolean basedOnSample;
}
