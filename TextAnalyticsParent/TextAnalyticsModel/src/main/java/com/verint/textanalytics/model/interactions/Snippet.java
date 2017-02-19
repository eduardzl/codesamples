package com.verint.textanalytics.model.interactions;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * Highlight.
 * 
 * @author Nshunewich
 *
 */
public class Snippet {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String text;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String utteranceId;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String fullText;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<BaseHighlight> snippetHighlights;

	@Getter
	@Setter
	@Accessors(chain = true)
	private boolean naturalStart;

	@Getter
	@Setter
	@Accessors(chain = true)
	private boolean naturalEnd;

	@Getter
	@Setter
	private String type;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int start;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int end;

	/**
	 * Constructor.
	 */
	public Snippet() {
		snippetHighlights = new ArrayList<BaseHighlight>();
	}
}
