package com.verint.textanalytics.common.exceptions;

/**
 * Created by TBaum on 6/23/2016.
 */
public class AWTServiceException  extends AppRuntimeException {

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
	public AWTServiceException(AWTServiceErrorCode errorCode) {
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
	public AWTServiceException(Exception ex, AWTServiceErrorCode errorCode) {
		super(ex, errorCode);
	}
}