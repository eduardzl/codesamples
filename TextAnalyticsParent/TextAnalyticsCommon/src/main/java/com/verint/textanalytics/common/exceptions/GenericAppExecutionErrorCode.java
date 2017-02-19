package com.verint.textanalytics.common.exceptions;

import com.verint.textanalytics.common.exceptions.AppExecutionErrorCode;

import lombok.Getter;

/**
 * GenericAppExecutionErrorCode - Generic Application Execution Error Code.
 * 
 * @author imor
 *
 */
public enum GenericAppExecutionErrorCode implements AppExecutionErrorCode {
	Error(-1);

	@Getter
	private final int errorCode;

	private GenericAppExecutionErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
}
