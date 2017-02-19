package com.verint.textanalytics.common.exceptions;

/**
 * Darwin Query Execution exception.
 * 
 * @author EZlotnik
 *
 */
public class TextQueryExecutionException extends AppRuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * C'tor.
	 * 
	 * @param errorCode
	 *            code of error
	 */
	public TextQueryExecutionException(TextQueryExecutionErrorCode errorCode) {
		super(errorCode);
	}

	/**
	 * C'tor.
	 * 
	 * @param ex
	 *            inner exception.
	 * @param errorCode
	 *            code of error
	 */
	public TextQueryExecutionException(Exception ex, TextQueryExecutionErrorCode errorCode) {
		super(ex, errorCode);
	}
}
