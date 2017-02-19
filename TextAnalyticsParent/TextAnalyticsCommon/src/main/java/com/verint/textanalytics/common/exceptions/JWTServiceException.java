package com.verint.textanalytics.common.exceptions;

/***
 * 
 * @author imor
 *
 */
public class JWTServiceException extends AppRuntimeException {

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
	public JWTServiceException(JWTServiceErrorCode errorCode) {
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
	public JWTServiceException(Exception ex, JWTServiceErrorCode errorCode) {
		super(ex, errorCode);
	}
}