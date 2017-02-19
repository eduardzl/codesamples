package com.verint.textanalytics.common.exceptions;

import lombok.Getter;

/***
 * 
 * @author yzanis
 *
 */
public enum FoundationServicesExecutionErrorCode implements AppExecutionErrorCode {

	// @formatter:off
	UserContextExecutionError(1),
	JsonResponseParsingError(2),
	DataSourcesDirectoryNotExist(3),
	DataSourcesFileCorapted(4);

	// @formatter:on

	@Getter
	private final int errorCode;

	private FoundationServicesExecutionErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

}