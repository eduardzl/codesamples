package com.verint.textanalytics.common.exceptions;

import lombok.Getter;

/**
 * Error code for Solr query generation.
 * 
 * @author EZlotnik
 *
 */
public enum TextQueryGenerationErrorCode implements AppExecutionErrorCode {

	// @formatter:off
	FacetOnUtteranceLevelField(-1);
	// @formatter:on

	@Getter
	private final int errorCode;

	private TextQueryGenerationErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
}
