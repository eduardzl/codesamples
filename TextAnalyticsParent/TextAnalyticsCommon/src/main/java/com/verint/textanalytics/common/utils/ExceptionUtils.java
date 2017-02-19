package com.verint.textanalytics.common.utils;

/**
 * Exception utils.
 * @author EZlotnik
 *
 */
public final class ExceptionUtils {

	private ExceptionUtils() {

	}

	/**
	 * Extracts an exception stack trace and generates a single string with
	 * separated frames.
	 * @param ex
	 *            exception
	 * @return concatenated string
	 */
	public static String getStackTrace(Throwable ex) {
		return org.apache.commons.lang.exception.ExceptionUtils.getFullStackTrace(ex);
	}
}
