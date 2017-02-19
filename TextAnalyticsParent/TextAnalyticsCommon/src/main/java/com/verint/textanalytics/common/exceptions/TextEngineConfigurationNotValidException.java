package com.verint.textanalytics.common.exceptions;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Application Configuration Not Valid Exception.
 * 
 * @author imor
 *
 */
public class TextEngineConfigurationNotValidException extends Exception {

	private static final long serialVersionUID = 5577503365464453228L;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<String> invalidFields;

	/**
	 * 
	 * @param message
	 *            Exception message
	 * @param invalidFields
	 *            List of invalid fields names
	 */
	public TextEngineConfigurationNotValidException(String message, List<String> invalidFields) {
		super(String.format("%s. [invalidFields = %s]", message, invalidFields.toString()));
		this.setInvalidFields(invalidFields);
	}

	/**
	 * @param invalidFields
	 *            List of invalid fields names
	 */
	public TextEngineConfigurationNotValidException(List<String> invalidFields) {
		super(String.format("Application Configuration Not Valid Exception. [invalidFields = %s]", invalidFields.toString()));
		this.setInvalidFields(invalidFields);
	}

}
