package com.verint.textanalytics.common.utils;

import lombok.NonNull;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Logger;

import static java.lang.Math.toIntExact;

/**
 * DataUtils class.
 *
 * @author EZlotnik
 */
public final class DataUtils {
	private static final DateTimeFormatter parser = ISODateTimeFormat.dateTimeParser();
	private static final DateTimeFormatter dayOfTheDateFormatter = DateTimeFormat.forPattern("yyyyMMdd");

	private static final String iso8601DateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	private static final String dateRangeFacetDateTimeFormat =  "yyyy-MM-dd";


	private DataUtils() {

	}


	/**
	 * Gets timestamp of date specified in ISO8601 format.
	 *
	 * @param date date to convert
	 * @return ticks of date
	 * @throws ParseException exception
	 */
	public static long getDateFromISO8601StringTimestamp(DateTime date) {
		return date.getMillis();
	}

	/**
	 * Convert string in ISO8601 format to Date.
	 *
	 * @param strDate date in string representation
	 * @return date.
	 */
	public static DateTime getDateFromISO8601String(String strDate) {
		return parser.parseDateTime(strDate).withZone(DateTimeZone.UTC);
	}

	/**
	 * Converts string in yyyyMMdd format to date.
	 * @param dayOfDate value to be converted
	 * @return datetime object
	 */
	public static DateTime getDateTimeFromDayOfDateString(String dayOfDate) {
		return dayOfTheDateFormatter.parseDateTime(dayOfDate).withZone(DateTimeZone.UTC);
	}

	/**
	 * Returns a date time with time zone offset according to browser time zone.
	 *
	 * @param dateTime             date time
	 * @param clientTimeZoneOffset number of milliseconds in client time zone offset
	 * @return an update date time
	 */
	public static DateTime getDateTimeAccordingToClientTimeZone(DateTime dateTime, int clientTimeZoneOffset) {
		return dateTime.minusMillis(dateTime.getMillisOfDay()).plusSeconds(clientTimeZoneOffset);
	}

	/**
	 * Returns a difference in days between two dates.
	 *
	 * @param dateStart start date
	 * @param dateEnd   end date
	 * @return diff in days
	 */
	public static int getDaysBetween(DateTime dateStart, DateTime dateEnd) {
		return Days.daysBetween(dateStart, dateEnd).getDays();
	}

	/**
	 * Converts a string in ISO8601 format with milliseconds to date.
	 *
	 * @param strDate string representing a date
	 * @return date
	 */
	public static DateTime getDateFromISO8601StringWithMilliseconds(String strDate) {
		return parser.parseDateTime(strDate).withZone(DateTimeZone.UTC);
	}


	/**
	 * Converts a string in ISO8601 format with milliseconds to date. Null returned for empty string as parameter.
	 * @param strDate date in ISO8601 format
	 * @return datetime of joda
	 */
	public static DateTime getDateFromISO8601StringWithMillisecondsSafe(String strDate) {
		DateTime date = null;

		if (!StringUtils.isNullOrBlank(strDate)) {
			date = getDateFromISO8601StringWithMilliseconds(strDate);
		}

		return  date;
	}

	/**
	 * Difference in milliseconds between 2 times.
	 *
	 * @param startTime start time
	 * @param endTime   end time
	 * @return a difference in milliseconds between
	 */
	public static long getTimeDiffMilliseconds(DateTime startTime, DateTime endTime) {
		return endTime.getMillis() - startTime.getMillis();
	}

	/**
	 * Get DateTime string represntation.
	 *
	 * @param date date
	 * @return string in ISO8601
	 */
	public static String getISO8601StringFromDate(DateTime date) {
		return date.toString(iso8601DateTimeFormat);
	}

	/**
	 * Get DateTime string represntation.
	 *
	 * @param date date
	 * @return string in ISO8601
	 */
	public static String getDateForDateRangeFacet(DateTime date) {
		return date.toString(dateRangeFacetDateTimeFormat);
	}

	/**
	 * Obtains a DateTime set to the current system millisecond time using
	 * ISOChronology in the default time zone.
	 *
	 * @return now millisecond
	 */
	public static long getNowMilliseconds() {
		return DateTime.now().getMillis();
	}

	/**
	 * Escaping special characters for Solr request.
	 *
	 * @param s string to be escaped
	 * @return an escaped string
	 */
	public static String escapeCharsForSolrQuery(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {

			char c = s.charAt(i);
			if (Character.isWhitespace(c)) {
				sb.append('\\');
				sb.append(c);
			} else {
				// These characters are part of the query syntax and must be escaped
				switch (c) {
					case '\\':
					case '+':
					case '-':
					case '!':
					case '(':
					case ')':
					case ':':
					case '^':
					case '[':
					case ']':
					case '\"':
					case '{':
					case '}':
						// case '~':  '~' '*' and '?' are not encoded thus allowing wild-card search
						// case '*':
						// case '?':
					case '|':
					case '&':
					case ';':
					case '/':
						sb.append('\\');
						sb.append(c);
						break;
					default:
						sb.append(c);
						break;
				}
			}
		}

		return sb.toString();
	}

	/**
	 * Gets date and returns ISO 8601 String without milisecond.
	 *
	 * @param date the date
	 * @return ISO 8601 String without milisecond
	 */

	public static String getISO8601StringForDateNoMiliSecond(Date date) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		return dateFormat.format(date);
	}

	/**
	 * Gets Date containing time zone and returns the UTC Time with the same time without the milisecond.
	 *
	 * @param date with timezone and milisecond
	 * @return date without milisecond in UTC format
	 */
	public static String getUTCTimeFromTimeZoneTimeInISO8601NoMiliSecond(Date date) {
		DateTime dt = new DateTime(date);
		// changing the date to UTC according to user TimeZone
		long instant = dt.getMillis();
		DateTimeZone tz = dt.getZone();
		long offset = tz.getOffset(instant);

		dt = dt.plusMillis(toIntExact(offset));
		return getISO8601StringForDateNoMiliSecond(dt.toDate());
	}

}
