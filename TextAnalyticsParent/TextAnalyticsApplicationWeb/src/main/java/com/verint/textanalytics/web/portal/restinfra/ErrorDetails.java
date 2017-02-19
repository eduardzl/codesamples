package com.verint.textanalytics.web.portal.restinfra;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.verint.textanalytics.common.exceptions.AppRuntimeException;

/**
 * @author imor
 *
 */
@NoArgsConstructor
public class ErrorDetails {

	/** application specific error code. */
	@Getter
	@Setter
	@Accessors(chain = true)
	private String errorCode;

	/** application specific Severity. */
	@Getter
	@Setter
	@Accessors(chain = true)
	private ErrorSeverity severity;

	/** application specific localized Message Key. */
	@Getter
	@Setter
	@Accessors(chain = true)
	private String messageKey;

	/** application specific localized String Format Values. */
	@Getter
	@Setter
	@Accessors(chain = true)
	private String[] messageKeyFormatValues;

	/** message describing the error from the exception. */
	@Getter
	@Setter
	@Accessors(chain = true)
	private String exceptionMessage;

	/** exception Type. */
	@Getter
	@Setter
	@Accessors(chain = true)
	private String exceptionType;

	/** show Error - true or false. */
	@Getter
	@Setter
	@Accessors(chain = true)
	private boolean showError;

	/** Stack Trace. */
	@Getter
	@Setter
	@Accessors(chain = true)
	private String stackTrace;

	/**
	 * C'tor.
	 * @param ex exception
	 */
	public ErrorDetails(AppRuntimeException ex) {
		super();

		this.errorCode = ex.getAppExecutionErrorCode().toString();
		this.exceptionType = ex.getClass().getName();
		this.exceptionMessage = ex.getMessage();
		this.severity = ErrorSeverity.High;
		this.showError = true;
	}
}