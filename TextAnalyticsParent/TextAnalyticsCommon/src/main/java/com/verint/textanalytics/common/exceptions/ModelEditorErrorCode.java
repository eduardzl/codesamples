package com.verint.textanalytics.common.exceptions;

import lombok.Getter;

/***
 * 
 * @author imor
 *
 */
public enum ModelEditorErrorCode implements AppExecutionErrorCode {

	// @formatter:off
	RetrieveModelsTreeError(0), RetrieveModelsTreeParsingError(1), RetrieveModelsTreeTenantNotFoundError(2);
	// @formatter:on

	@Getter
	private final int errorCode;

	private ModelEditorErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
}