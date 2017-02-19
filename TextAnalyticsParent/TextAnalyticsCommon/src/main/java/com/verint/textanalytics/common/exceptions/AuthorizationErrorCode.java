package com.verint.textanalytics.common.exceptions;

import lombok.Getter;

/***
 * 
 * @author yzanis
 *
 */
public enum AuthorizationErrorCode implements AppExecutionErrorCode {

	UserNotLogedInError(0), InvalidAccessError(1);

	@Getter
	private final int errorCode;

	private AuthorizationErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
}