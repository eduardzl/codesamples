package com.verint.textanalytics.web.viewmodel;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Created by yzanis on 29-Mar-16.
 */
public class SnippetPositionCIV {

	@Getter
	@Setter
	@Accessors(chain = true)
	protected int start;

	@Getter
	@Setter
	@Accessors(chain = true)
	protected int end;
}
