package com.verint.textanalytics.common.exceptions;

/***
 * 
 * @author yzanis
 *
 */
public class AuthorizationException extends AppRuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * C'tor.
	 * 
	 * @param ex
	 *            inner exception.
	 * @param errorCode
	 *            code of error
	 */
	public AuthorizationException(Exception ex, AuthorizationErrorCode errorCode) {
		super(ex, errorCode);
	}

	/**
	 * C'tor.
	 * @param message
	 *            error message
	 * @param errorCode
	 *            error code
	 */
	public AuthorizationException(String message, AuthorizationErrorCode errorCode) {
		super(message, errorCode);
	}

}