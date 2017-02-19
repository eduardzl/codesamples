package com.verint.textanalytics.common.exceptions;

/**
 * Configuration exception.
 * 
 * @author imor
 *
 */
public class ConfigurationException extends AppRuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Empty C'tor.
	 * 
	 * @param errorCode
	 *            code of error
	 */
	public ConfigurationException(ConfigurationErrorCode errorCode) {
		super(errorCode);
	}

	/**
	 * C'tor.
	 * 
	 * @param ex
	 *            inner exception.
	 * @param errorCode
	 *            code of error
	 */
	public ConfigurationException(Exception ex, ConfigurationErrorCode errorCode) {
		super(ex, errorCode);
	}

	/**
	 * C'tor.
	 * @param message
	 *            error message
	 * @param errorCode
	 *            error code
	 */
	public ConfigurationException(String message, ConfigurationErrorCode errorCode) {
		super(message, ConfigurationErrorCode.TextEngineSchemeNotValidError);
	}
}
