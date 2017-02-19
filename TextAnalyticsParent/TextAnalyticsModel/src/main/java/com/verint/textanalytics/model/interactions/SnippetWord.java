package com.verint.textanalytics.model.interactions;

import lombok.Getter;
import lombok.Setter;

/**
 * Contains information about the single word.
 * 
 * @author NShunewich
 *
 */
public class SnippetWord {
	@Getter
	@Setter
	private int index;

	@Getter
	@Setter
	private String word;

	@Getter
	@Setter
	private int start;

	@Getter
	@Setter
	private int end;

	@Getter
	@Setter
	private Boolean isHighlight = false;

	@Getter
	@Setter
	private BaseHighlight highlight;

	@Getter
	@Setter
	private Boolean isLastHighlight = false;

	@Getter
	@Setter
	private Boolean isLastHighlightingWord = false;

}
