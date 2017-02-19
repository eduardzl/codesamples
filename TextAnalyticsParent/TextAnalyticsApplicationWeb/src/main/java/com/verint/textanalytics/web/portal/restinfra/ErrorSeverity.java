package com.verint.textanalytics.web.portal.restinfra;

import lombok.Getter;

/**
 * ErrorSeverity.
 * @author imor
 *
 */
public enum ErrorSeverity {

	// @formatter:off
	High(1),
	Medium(2),
	Low(3);
	// @formatter:on

	@Getter
	private final int errorSeverity;

	private ErrorSeverity(int errorSeverity) {
		this.errorSeverity = errorSeverity;
	}
}