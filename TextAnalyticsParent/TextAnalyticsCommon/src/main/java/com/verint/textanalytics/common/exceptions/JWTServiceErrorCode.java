package com.verint.textanalytics.common.exceptions;

import lombok.Getter;

/***
 * 
 * @author imor
 *
 */
public enum JWTServiceErrorCode implements AppExecutionErrorCode {

	// @formatter:off
	CreateHeaderTokenError(0);
	// @formatter:on

	@Getter
	private final int errorCode;

	private JWTServiceErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
}