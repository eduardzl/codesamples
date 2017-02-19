package com.verint.textanalytics.model.interactions;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Highlight.
 * 
 * @author imor
 *
 */

public class TermHighlight extends BaseHighlight {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String term;

	/**
	 * AllArgsConstructor.
	 * 
	 * @param term
	 *            term
	 * @param starts
	 *            starts
	 * @param ends
	 *            ends
	 */
	public TermHighlight(String term, int starts, int ends) {
		super();

		this.term = term;
		this.starts = starts;
		this.ends = ends;
	}

	/**
	 * TermHighlight.
	 */
	public TermHighlight() {
		super();
	}

	public String getKey() {
		return String.format("%d_%d", starts, ends);
	}

}
