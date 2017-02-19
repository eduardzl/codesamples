package com.verint.textanalytics.common.exceptions;

/***
 * 
 * @author yzanis
 *
 */
public class StoredSearchesException extends AppRuntimeException {

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
	public StoredSearchesException(StoredSearchesErrorCode errorCode) {
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
	public StoredSearchesException(Exception ex, StoredSearchesErrorCode errorCode) {
		super(ex, errorCode);
	}
}