package com.verint.textanalytics.common.exceptions;

import lombok.Getter;

/**
 * Error code for Solr query generation.
 * 
 * @author imor
 *
 */
public enum ConfigurationErrorCode implements AppExecutionErrorCode {

	ApplicationConfigurationNotValidError(0), TextEngineSchemeNotValidError(1);

	@Getter
	private final int errorCode;

	private ConfigurationErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
}
