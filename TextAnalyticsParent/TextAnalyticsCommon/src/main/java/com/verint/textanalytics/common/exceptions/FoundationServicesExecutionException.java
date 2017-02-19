package com.verint.textanalytics.common.exceptions;

/***
 * 
 * @author yzanis
 *
 */
public class FoundationServicesExecutionException extends AppRuntimeException {

	private static final long serialVersionUID = 1L;

	/***
	 * C'tor.
	 * @param errorCode
	 *            code of error
	 */
	public FoundationServicesExecutionException(FoundationServicesExecutionErrorCode errorCode) {
		super(errorCode);
	}

	/***
	 * Constructor which accepts inner exception.
	 * 
	 * @param ex
	 *            inner exception
	 * @param errorCode
	 *            error code
	 */
	public FoundationServicesExecutionException(Exception ex, FoundationServicesExecutionErrorCode errorCode) {
		super(ex, errorCode);
	}
}