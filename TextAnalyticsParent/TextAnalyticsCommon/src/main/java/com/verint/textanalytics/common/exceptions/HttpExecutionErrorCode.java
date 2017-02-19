package com.verint.textanalytics.common.exceptions;

import lombok.Getter;

/***
 * 
 * @author yzanis
 *
 */
public enum HttpExecutionErrorCode implements AppExecutionErrorCode {

	HttpExecutionFailed(0);

	@Getter
	private final int errorCode;

	private HttpExecutionErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
}