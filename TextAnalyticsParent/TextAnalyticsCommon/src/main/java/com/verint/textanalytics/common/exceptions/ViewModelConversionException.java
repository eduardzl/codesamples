package com.verint.textanalytics.common.exceptions;

/**
 * ViewModelConversionException.
 * 
 * @author EZlotnik
 *
 */
public class ViewModelConversionException extends AppRuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Empty C'tor.
	 * 
	 */
	public ViewModelConversionException() {
		super();
	}

	/**
	 * Constructor which accepts inner exception.
	 * @param ex
	 *            exception
	 */
	public ViewModelConversionException(Exception ex) {
		super(ex);
	}
}
