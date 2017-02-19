package com.verint.textanalytics.common.exceptions;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * AppRuntimeException - Application common Runtime exception object.
 * 
 * @author imor
 *
 */
public class AppRuntimeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	@Accessors(chain = true)
	protected AppExecutionErrorCode appExecutionErrorCode;

	protected Map<String, Object> data = new HashMap<String, Object>();

	/**
	 * Empty C'tor code of error.
	 */
	public AppRuntimeException() {
		this.appExecutionErrorCode = GenericAppExecutionErrorCode.Error;
	}

	/**
	 * Empty C'tor.
	 * 
	 * @param errorCode
	 *            code of error
	 */
	public AppRuntimeException(AppExecutionErrorCode errorCode) {
		this.appExecutionErrorCode = errorCode;
	}

	/**
	 * C'tor.
	 * 
	 * @param ex
	 *            inner exception.
	 * @param errorCode
	 *            code of error
	 */
	public AppRuntimeException(Exception ex, AppExecutionErrorCode errorCode) {
		super(ex);

		this.appExecutionErrorCode = errorCode;
	}

	/**
	 * C'tor with inner exception parameter only.
	 * 
	 * @param ex
	 *            inner exception.
	 */
	public AppRuntimeException(Exception ex) {
		super(ex);
	}

	/**
	 * C'tor with inner exception parameter only.
	 * 
	 * @param message
	 *            exception message.
	 * @param errorCode
	 *            error code
	 */
	public AppRuntimeException(String message, AppExecutionErrorCode errorCode) {
		super(message);
		this.appExecutionErrorCode = errorCode;
	}

	/**
	 * Adds additional data to exception.
	 * 
	 * @param key
	 *            key of data to add
	 * @param value
	 *            value to add
	 * @return object itself for fluent api
	 */
	public AppRuntimeException put(String key, Object value) {
		this.data.put(key, value);
		return this;
	}

	/**
	 * Returns a data for specific key.
	 * 
	 * @param key
	 *            key
	 * @return data of key
	 */
	public Object get(String key) {
		if (this.data.containsKey(key)) {
			return this.data.get(key);
		}

		return null;
	}

	@Override
	public String toString() {
		return String.format("{Super: %s, ErrorCode: %s, data: %s }",  super.toString(), appExecutionErrorCode, mapToString(data));
	}

	private static String mapToString(Map<String, Object> map) {
		StringBuilder stringBuilder = new StringBuilder();

		if (map != null) {
			for (String key : map.keySet()) {
				if (stringBuilder.length() > 0) {
					stringBuilder.append(",");
				}
				Object value = map.get(key);
				stringBuilder.append((key != null ? key : ""));
				stringBuilder.append(":");
				stringBuilder.append(value != null ? value.toString() : "");
			}
		}

		return "[" + stringBuilder.toString() + "]";
	}

}
