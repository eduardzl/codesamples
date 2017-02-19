package com.verint.textanalytics.dal.darwin.vtasyntax.errors;

import com.verint.textanalytics.common.exceptions.AppRuntimeException;
import lombok.Getter;

/**
 * Created by EZlotnik on 4/4/2016.
 */
public class VTASyntaxProcessingException extends VTASyntaxException {

	@Getter
	private ProcessingErrorType errorType;

	/**
	 * Constructor.
	 * @param errorType error type
	 */
	public VTASyntaxProcessingException(ProcessingErrorType errorType) {
		this.errorType = errorType;
	}

	/**
	 * Constructor.
	 * @param errorType error code
	 * @param message error message
	 * @param innerEx inner exception
	 */
	public VTASyntaxProcessingException(ProcessingErrorType errorType, String message, Exception innerEx) {
		super(message, innerEx);

		this.errorType = errorType;
	}

	/**
	 * Constructor.
	 * @param errorType error code
	 * @param message error message
	 */
	public VTASyntaxProcessingException(ProcessingErrorType errorType, String message) {
		super(message);

		this.errorType = errorType;
	}
}
