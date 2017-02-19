package com.verint.textanalytics.dal.darwin.vtasyntax.errors;

import lombok.NoArgsConstructor;

/**
 * Created by EZlotnik on 4/11/2016.
 */
@NoArgsConstructor
public class VTASyntaxException extends RuntimeException {

	/**
	 * C'tor.
	 * @param message message
	 * @param innerEx inner exception
	 */
	public VTASyntaxException(String message, Exception innerEx) {
		super(message, innerEx);
	}

	/**
	 * C'tor.
	 * @param message message
	 */
	public VTASyntaxException(String message) {
		super(message);
	}
}
