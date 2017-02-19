package com.verint.textanalytics.common.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/***
 * 
 * @author imor
 *
 */
public class StringUtilsTest {

	private String newline0;
	private String newline1;
	private String newline2;
	private String newline3;
	private String newline4;

	private String newLineSeparator = System.getProperty("line.separator");
	private String line = "line";
	private String threeDots = "...";

	@Before
	public void setUp() throws Exception {
		newline0 = line;
		newline1 = line + newLineSeparator + line;
		newline2 = line + newLineSeparator + line + newLineSeparator + line;
		newline3 = line + newLineSeparator + line + newLineSeparator + line + newLineSeparator + line;
		newline4 = line + newLineSeparator + line + newLineSeparator + line + newLineSeparator + line + newLineSeparator + line;
	}

	@Test
	public void topNLines_newline0_select1() {
		assertEquals(newline0, StringUtils.topNLines(newline0, 1));
	}

	@Test
	public void topNLines_newline0_select3() {
		assertEquals(newline0, StringUtils.topNLines(newline0, 3));
	}

	@Test
	public void topNLines_newline1_select1() {
		assertEquals(newline0 + threeDots, StringUtils.topNLines(newline1, 1));
	}

	@Test
	public void topNLines_newline1_select3() {
		assertEquals(newline1, StringUtils.topNLines(newline1, 3));
	}

	@Test
	public void topNLines_newline2_select1() {
		assertEquals(newline0 + threeDots, StringUtils.topNLines(newline2, 1));
	}

	@Test
	public void topNLines_newline2_select3() {
		assertEquals(newline2, StringUtils.topNLines(newline2, 3));
	}

	@Test
	public void topNLines_newline3_select1() {
		assertEquals(newline0 + threeDots, StringUtils.topNLines(newline3, 1));
	}

	@Test
	public void topNLines_newline3_select3() {
		assertEquals(newline2 + threeDots, StringUtils.topNLines(newline3, 3));
	}

	@Test
	public void topNLines_newline4_select1() {
		assertEquals(newline0 + threeDots, StringUtils.topNLines(newline4, 1));
	}

	@Test
	public void topNLines_newline4_select3() {
		assertEquals(newline2 + threeDots, StringUtils.topNLines(newline4, 3));
	}
}