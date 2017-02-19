package com.verint.textanalytics.dal.darwin.vtasyntax.errors;

import java.util.List;

/**
 * Created by EZlotnik on 2/28/2016.
 */
public class VTASyntaxRecognitionException extends VTASyntaxException {
	private AntlrErrorType errorType;

	private List<VTASyntaxRecognitionError>  recognitionErrors;

	/**
	 * Constructor.
	 * @param errorType error type
	 * @param  errorMessage error message
	 * @param errors errors
	 */
	public VTASyntaxRecognitionException(AntlrErrorType errorType, String errorMessage, List<VTASyntaxRecognitionError> errors) {
		super(errorMessage);

		this.errorType = errorType;
		this.recognitionErrors = errors;
	}

	/**
	 * C'tor.
	 * @param message exception message
	 * @param innerException inner exception
	 */
	public VTASyntaxRecognitionException(String message, Exception innerException) {
		super(message, innerException);
	}

	/**
	 * C'tor.
	 * @param message error message
	 */
	public VTASyntaxRecognitionException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * @param errors errors
	 */
	public VTASyntaxRecognitionException(List<VTASyntaxRecognitionError> errors) {
		this.recognitionErrors = errors;
	}

}
