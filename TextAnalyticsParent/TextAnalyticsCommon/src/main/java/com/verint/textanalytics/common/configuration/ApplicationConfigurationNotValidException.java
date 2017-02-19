package com.verint.textanalytics.common.configuration;

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
public class ApplicationConfigurationNotValidException extends Exception {

	private static final long serialVersionUID = 5577503366052453228L;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<String> invalidFields;

	/**
	 * 
	 * @param message
	 *            Exception message
	 */
	public ApplicationConfigurationNotValidException(String message) {
		super(message);				
	}

	/**
	 * 
	 * @param message
	 *            Exception message
	 * @param invalidFields
	 *            List of invalid fields names
	 */
	public ApplicationConfigurationNotValidException(String message, List<String> invalidFields) {
		super(String.format("%s. [invalidFields = %s]", message, invalidFields.toString()));
		this.setInvalidFields(invalidFields);
	}

	/**
	 * @param invalidFields
	 *            List of invalid fields names
	 */
	public ApplicationConfigurationNotValidException(List<String> invalidFields) {
		super(String.format("Application Configuration Not Valid Exception. [invalidFields = %s]", invalidFields.toString()));
		this.setInvalidFields(invalidFields);
	}

}
