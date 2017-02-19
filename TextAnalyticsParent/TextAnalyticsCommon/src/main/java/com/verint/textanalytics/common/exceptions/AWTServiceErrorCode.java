package com.verint.textanalytics.common.exceptions;

import lombok.Getter;

/**
 * Created by TBaum on 6/23/2016.
 */
public enum  AWTServiceErrorCode implements AppExecutionErrorCode {


	// @formatter:off
	CreateHeaderTokenError(0);
	// @formatter:on

	@Getter
	private final int errorCode;

	private AWTServiceErrorCode(int errorCode) {

		this.errorCode = errorCode;
	}
}