package com.verint.textanalytics.dal.darwin.vtasyntax.customparsers;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by EZlotnik on 3/10/2016.
 */
@AllArgsConstructor
class Token {
	@Getter
	private String term;

	@Getter
	private String type;

	@Getter
	private int position;

	@Getter
	private int offsetStart;

	@Getter
	private int offsetEnd;
}
