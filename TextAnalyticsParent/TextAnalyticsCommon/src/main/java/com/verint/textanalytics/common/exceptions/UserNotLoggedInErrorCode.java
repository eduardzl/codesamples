package com.verint.textanalytics.common.exceptions;

import lombok.Getter;

/***
 * 
 * @author yzanis
 *
 */
public enum UserNotLoggedInErrorCode implements AppExecutionErrorCode {

	UserNotLogedInError(-1);

	@Getter
	private final int errorCode;

	private UserNotLoggedInErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

}