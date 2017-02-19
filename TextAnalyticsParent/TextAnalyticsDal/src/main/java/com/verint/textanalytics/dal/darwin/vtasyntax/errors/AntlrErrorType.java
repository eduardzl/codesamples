package com.verint.textanalytics.dal.darwin.vtasyntax.errors;

/**
 * Created by EZlotnik on 3/20/2016.
 */
public enum AntlrErrorType {

	// @formatter:off
	SyntaxError("SyntaxError"),
	ParsingError("ParsingError"),
	AmbiguityError("AmbiguityError"),
	AttemptingFullContextError("AttemptingFullContextError"),
	ContextSensitivityError("ContextSensitivityError");
	// @formatter:on

	private String errorType;

	private AntlrErrorType(String value) {
		this.errorType = value;
	}
}
