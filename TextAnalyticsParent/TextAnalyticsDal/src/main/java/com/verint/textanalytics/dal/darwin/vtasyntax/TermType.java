package com.verint.textanalytics.dal.darwin.vtasyntax;

/**
 * Created by EZlotnik on 2/29/2016.
 */

public enum TermType {

	// @formatter:off
	Word("word"),
	Phrase("phrase");
	// @formatter:on

	private String termType;

	private TermType(String value) {
		this.termType = value;
	}
}
