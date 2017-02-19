package com.verint.textanalytics.common.utils;

import java.util.Arrays;
import java.util.Spliterators;
import java.util.stream.*;

import propel.core.utils.Linq;
import propel.core.utils.StringComparison;
import propel.core.utils.StringSplitOptions;

/**
 * String Utils.
 * @author EZlotnik
 *
 */
public final class StringUtils {

	private StringUtils() {

	}

	/**
	 * Splits a string.
	 * @param value
	 *            to split
	 * @param delimiter
	 *            delimiter
	 * @return array of tokens
	 */
	public static String[] split(String value, String delimiter) {
		return org.apache.commons.lang.StringUtils.split(value, delimiter);
	}

	/**
	 * Splits a string.
	 * @param value
	 *            to split
	 * @param delimiter
	 *            delimiter
	 * @return array of tokens
	 */
	public static String[] splitAndRemoveEmpty(String value, String delimiter) {
		return propel.core.utils.StringUtils.split(value, delimiter, StringSplitOptions.RemoveEmptyEntries);
	}

	/**
	 * isNullOrBlank of jpropel-light.
	 * @param value
	 *            value to test
	 * @return indication
	 */
	public static Boolean isNullOrBlank(String value) {
		return propel.core.utils.StringUtils.isNullOrBlank(value);
	}

	/**
	 * Trim string.
	 * @param value
	 *            value to trim
	 * @param trimmed
	 *            trimmed string
	 * @return string after trim
	 */
	public static String trim(String value, String trimmed) {
		return propel.core.utils.StringUtils.trim(value, trimmed, StringComparison.InvariantLocale);
	}

	/**
	 * Trim string.
	 * @param value
	 *            value to trim
	 * @param trimmed
	 *            trimmed string
	 * @return string after trim
	 */
	public static String trimEnd(String value, String trimmed) {
		return propel.core.utils.StringUtils.trimEnd(value, trimmed, StringComparison.InvariantLocale);
	}

	/**
	 * Trim string.
	 * @param value
	 *            value to trim
	 * @param trimmed
	 *            trimmed string
	 * @return string after trim
	 */
	public static String trimStart(String value, String trimmed) {
		return propel.core.utils.StringUtils.trimStart(value, trimmed, StringComparison.InvariantLocale);
	}

	/**
	 * Top N lines from string.
	 * @param value
	 *            string to take top lines
	 * @param numberOfLines
	 *            number of lines to take
	 * @return modified string
	 */
	public static String topNLines(String value, int numberOfLines) {
		String topNLinesConcat = "";

		if (!StringUtils.isNullOrBlank(value)) {
			String lineSeparator = propel.core.utils.StringUtils.detectLineSeparator(value);
			String[] lines = propel.core.utils.StringUtils.split(value, lineSeparator);
			if (lines != null) {
				if (lines.length > numberOfLines) {
					String[] topLines = Linq.take(lines, numberOfLines);
					topNLinesConcat = propel.core.utils.StringUtils.delimit(topLines, lineSeparator) + "...";
				} else {
					topNLinesConcat = value;
				}

			}
		}

		return topNLinesConcat;
	}

	/**
	 * Generates a single string from array of strings by concatenation.
	 * @param values
	 *            array of values
	 * @param lineTerminator
	 *            line terminator
	 * @return a concatenated value
	 */
	public static String concat(String[] values, String lineTerminator) {
		if (values != null) {
			return Arrays.stream(values).reduce("", (x, y) -> x + y + lineTerminator);
		}

		return "";
	}
}
