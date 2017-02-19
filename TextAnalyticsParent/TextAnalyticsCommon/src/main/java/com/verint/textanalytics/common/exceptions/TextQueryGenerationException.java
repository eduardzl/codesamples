package com.verint.textanalytics.common.exceptions;

/**
 * Created by EZlotnik on 1/24/2016.
 */
public class TextQueryGenerationException extends AppRuntimeException {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * C'tor.
	 *
	 * @param errorCode
	 *            code of error
	 */
	public TextQueryGenerationException(TextQueryGenerationErrorCode errorCode) {
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
	public TextQueryGenerationException(Exception ex, TextQueryGenerationErrorCode errorCode) {
		super(ex, errorCode);
	}
}
