package com.verint.textanalytics.common.utils;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import lombok.val;

import org.joda.time.*;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;

public class DataUtilsTest {

	@Test
	public final void testGetDateFromISO8601StringTimestamp() throws ParseException {
		long date = 123456789;
		assertEquals(date, DataUtils.getDateFromISO8601StringTimestamp(new DateTime(date)));
	}

	@Test
	public final void testGetDateFromISO8601String() throws ParseException {
		String dateText = "2014-10-15T15:05:50Z";

		val dateTime = DataUtils.getDateFromISO8601String(dateText);

		assertEquals(2014, dateTime.getYearOfEra());
		assertEquals(10, dateTime.getMonthOfYear());
		assertEquals(15, dateTime.getDayOfMonth());
		assertEquals(15, dateTime.getHourOfDay());
		assertEquals(5, dateTime.getMinuteOfHour());
		assertEquals(50, dateTime.getSecondOfMinute());
	}
	
	
	@Test
	public final void testGetDateFromISO8601String2() throws ParseException {
		String dateText = "2015-01-01T00:00:00Z";

		val dateTime = DataUtils.getDateFromISO8601StringWithMilliseconds(dateText).withTimeAtStartOfDay();

		assertEquals(2015, dateTime.getYearOfEra());
		assertEquals(1, dateTime.getMonthOfYear());
		assertEquals(1, dateTime.getDayOfMonth());
		assertEquals(0, dateTime.getHourOfDay());
		assertEquals(0, dateTime.getMinuteOfHour());
		assertEquals(0, dateTime.getSecondOfMinute());
	}

}
