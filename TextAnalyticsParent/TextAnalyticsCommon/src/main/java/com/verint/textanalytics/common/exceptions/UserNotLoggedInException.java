package com.verint.textanalytics.common.exceptions;

/***
 * 
 * @author yzanis
 *
 */
public class UserNotLoggedInException extends AppRuntimeException {

	private static final long serialVersionUID = 1L;

	/***
	 * C'tor.
	 * @param errorCode
	 *            code of error
	 */
	public UserNotLoggedInException(UserNotLoggedInErrorCode errorCode) {
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
	public UserNotLoggedInException(Exception ex, UserNotLoggedInErrorCode errorCode) {
		super(ex, errorCode);
	}
}