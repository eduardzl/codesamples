package com.verint.textanalytics.web.viewmodel;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Created by yzanis on 29-Mar-16.
 */
public class SnippetUtteranceCIV {

	@Getter
	@Setter
	@Accessors(chain = true)
	protected String utteranceId;

	@Getter
	@Setter
	@Accessors(chain = true)
	protected List<SnippetPositionCIV> snippets;
}
