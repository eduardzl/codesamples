package com.verint.textanalytics.common.exceptions;

/***
 * 
 * @author imor
 *
 */
public class ModelEditorException extends AppRuntimeException {

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
	public ModelEditorException(ModelEditorErrorCode errorCode) {
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
	public ModelEditorException(Exception ex, ModelEditorErrorCode errorCode) {
		super(ex, errorCode);
	}
}