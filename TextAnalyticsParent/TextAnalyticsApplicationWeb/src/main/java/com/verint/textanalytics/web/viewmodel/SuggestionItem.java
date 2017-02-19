package com.verint.textanalytics.web.viewmodel;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Term Suggestion.
 * @author yzanis
 *
 */
public class SuggestionItem {
	@Getter
	@Setter
	@Accessors(chain = true)
	private String text;

	@Getter
	@Setter
	@Accessors(chain = true)
	private double precent;

	@Getter
	@Setter
	private boolean basedOnSample;
}