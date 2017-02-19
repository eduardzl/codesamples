package com.verint.textanalytics.common.exceptions;

import lombok.Getter;
import lombok.Setter;

/***
 * 
 * @author yzanis
 *
 */
public class HttpExecutionException extends AppRuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	private int responseStatus;

	@Getter
	@Setter
	private String responseText;

	/**
	 * C'tor.
	 * 
	 * @param errorCode
	 *            code of error
	 */
	public HttpExecutionException(HttpExecutionErrorCode errorCode) {
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
	public HttpExecutionException(Exception ex, HttpExecutionErrorCode errorCode) {
		super(ex, errorCode);
	}

	/**
	 * C'tor.
	 * @param message
	 *            error message
	 * @param errorCode
	 *            error code
	 */
	public HttpExecutionException(String message, HttpExecutionErrorCode errorCode) {
		super(message, HttpExecutionErrorCode.HttpExecutionFailed);
	}
}