package com.verint.textanalytics.common.utils;

import static org.junit.Assert.*;
import lombok.val;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class JSONUtilsTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testGetJSONObject() {

		val jsonObj = new JSONObject();
		val obj = new JSONObject();

		jsonObj.put("obj", obj);

		val res = JSONUtils.getJSONObject(jsonObj, "obj");
		assertEquals(obj, res);
	}

	@Test
	public final void testGetJSONObjectNotJSONObject() {

		val jsonObj = new JSONObject();
		val obj = new Object();

		jsonObj.put("obj", obj);

		val res = JSONUtils.getJSONObject(jsonObj, "obj");
		assertEquals(null, res);
	}

	@Test
	public final void testGetJSONObjectNoKeyFound() {

		val jsonObj = new JSONObject();
		val obj = new Object();

		jsonObj.put("obj", obj);

		val res = JSONUtils.getJSONObject(jsonObj, "NotFound");
		assertEquals(null, res);
	}

	@Test
	public final void testGetJSONArray() {
		val jsonObj = new JSONObject();
		val jsonArray = new JSONArray();

		jsonObj.put("obj", jsonArray);

		val res = JSONUtils.getJSONArray(jsonObj, "obj");
		assertEquals(jsonArray, res);
	}

	@Test
	public final void testGetJSONArrayNotArray() {
		val jsonObj = new JSONObject();
		val obj = new Object();

		jsonObj.put("obj", obj);

		val res = JSONUtils.getJSONArray(jsonObj, "obj");
		assertEquals(null, res);
	}

	@Test
	public final void testGetJSONArrayNoKey() {
		val jsonObj = new JSONObject();
		val jsonArray = new JSONArray();

		jsonObj.put("obj", jsonArray);

		val res = JSONUtils.getJSONArray(jsonObj, "NotFound");
		assertEquals(null, res);
	}

	@Test
	public final void testGetString() {
		val jsonObj = new JSONObject();
		jsonObj.put("key", "value");
		String defaultString = "Error Response";

		val res = JSONUtils.getString("key", jsonObj, defaultString);
		assertEquals("value", res);
	}

	@Test
	public final void testGetStringWithObj() {
		val jsonObj = new JSONObject();
		val obj = new Object();
		jsonObj.put("key", obj);
		String defaultString = "NotFound";

		val res = JSONUtils.getString("key", jsonObj, defaultString);
		assertEquals(defaultString, res);
	}

	@Test
	public final void testGetStringGettingIncorrectString() {
		val jsonObj = new JSONObject();
		jsonObj.put("key", "value");
		String defaultString = "NotFound";

		val res = JSONUtils.getString("value", jsonObj, defaultString);
		assertEquals(defaultString, res);
	}

	@Test
	public final void testGetInt() {
		val jsonObj = new JSONObject();
		Integer objValue = 1256;
		jsonObj.put("key", objValue);
		Integer defaultInt = 999999;

		val res = JSONUtils.getInt("key", jsonObj, defaultInt);
		assertEquals(1256, res);
	}

	@Test
	public final void testGetIntGettingIncorrectInt() {
		val jsonObj = new JSONObject();
		Integer objValue = 1256;
		jsonObj.put("key", objValue);
		Integer defaultInt = 999999;

		val res = JSONUtils.getInt("incorrectKey", jsonObj, defaultInt);
		assertEquals(999999, res);
	}

	@Test
	public final void testGetIntWithString() {
		val jsonObj = new JSONObject();
		String objValue = "1256";
		jsonObj.put("key", objValue);
		Integer defaultInt = 999999;

		val res = JSONUtils.getInt("incorrectKey", jsonObj, defaultInt);
		assertEquals(999999, res);
	}

	@Test
	public final void testGetIntWithObj() {
		val jsonObj = new JSONObject();
		val obj = new Object();
		jsonObj.put("key", obj);
		Integer defaultInt = 99999;

		val res = JSONUtils.getInt("key", jsonObj, defaultInt);
		assertEquals(99999, res);
	}

	@Test
	public final void testGetBoolean() {
		val jsonObj = new JSONObject();
		Boolean objValue = true;
		jsonObj.put("key", objValue);
		Boolean defaultBo = false;

		val res = JSONUtils.getBoolean("key", jsonObj, defaultBo);
		assertEquals(objValue, res);
	}

	@Test
	public final void testGetBooleanGettingError() {
		val jsonObj = new JSONObject();
		Boolean objValue = true;
		jsonObj.put("key", objValue);
		Boolean defaultBo = false;

		val res = JSONUtils.getBoolean("incorrectKey", jsonObj, defaultBo);
		assertEquals(defaultBo, res);
	}

	@Test
	public final void testGetBooleanGettingErrorMessage() {
		val jsonObj = new JSONObject();
		val obj = new Object();
		jsonObj.put("key", obj);
		Boolean defaultBo = false;

		val res = JSONUtils.getBoolean("key", jsonObj, defaultBo);
		assertEquals(defaultBo, res);
	}

	@Test
	public final void testGetDouble() {
		val jsonObj = new JSONObject();
		Double objValue = 1.22;
		jsonObj.put("key", objValue);
		Double defaultDoubl = 9.999999;

		val res = JSONUtils.getDouble("key", jsonObj, defaultDoubl);
		assertTrue(res == objValue);
	}

	@Test
	public final void testGetDoubleWithDivided() {
		val jsonObj = new JSONObject();
		Double objValue = 4 / 2.2;
		jsonObj.put("key", objValue);
		Double defaultDoubl = 9.999999;

		val res = JSONUtils.getDouble("key", jsonObj, defaultDoubl);
		assertTrue(res == objValue);
	}

	@Test
	public final void testGetDoubleError() {
		val jsonObj = new JSONObject();
		Double objValue = 2.24;
		jsonObj.put("key", objValue);
		Double defaultDoubl = 9.999999;

		val res = JSONUtils.getDouble("incorrectKey", jsonObj, defaultDoubl);
		assertTrue(res == defaultDoubl);
	}

	@Test
	public final void testGetDoubleErrorObj() {
		val jsonObj = new JSONObject();
		val obj = new Object();
		jsonObj.put("key", obj);
		Double defaultDoubl = 9.999999;

		val res = JSONUtils.getDouble("key", jsonObj, defaultDoubl);
		assertTrue(res == defaultDoubl);
	}

	@Test
	public final void testGetLong() {
		val jsonObj = new JSONObject();
		Long objValue = 1111111111L;
		jsonObj.put("key", objValue);
		Long defaultLong = 9999999L;

		val res = JSONUtils.getLong("key", jsonObj, defaultLong);
		assertTrue(res == objValue);
	}

	@Test
	public final void testGetLongWithDivided() {
		val jsonObj = new JSONObject();
		Long objValue = 1111111111L / 222222L;
		jsonObj.put("key", objValue);
		Long defaultLong = 9999999L;

		val res = JSONUtils.getLong("key", jsonObj, defaultLong);
		assertTrue(res == objValue);
	}

	@Test
	public final void testGetLongError() {
		val jsonObj = new JSONObject();
		Long objValue = 1111111111L / 222222L;
		jsonObj.put("key", objValue);
		Long defaultLong = 9999999L;

		val res = JSONUtils.getLong("incorrectKey", jsonObj, defaultLong);
		assertTrue(res == defaultLong);
	}

	@Test
	public final void testGetLongErrorObj() {
		val jsonObj = new JSONObject();
		val obj = new Object();
		jsonObj.put("key", obj);
		Long defaultLong = 9999999L;

		val res = JSONUtils.getLong("key", jsonObj, defaultLong);
		assertTrue(res == defaultLong);
	}

	@Test
	public final void testGetObjectJSON() {
		val jsonObj = new JSONObject();
		val obj = new Object[] { 1 };
		jsonObj.put("key", obj);

		val res = JSONUtils.getObjectJSON(obj);
		assertEquals("[ 1 ]", res);
	}

	@Test
	public final void testGetObjectJSONObj() {
		val obj = new Object[] { 1, 2, 5, 6 };

		val res = JSONUtils.getObjectJSON(obj);
		assertEquals("[ 1, 2, 5, 6 ]", res);
	}

	@Test
	public final void testGetObjectJSONObjInsteadOfObj() {
		val obj = new JSONObject();

		val res = JSONUtils.getObjectJSON(obj);
		assertEquals("", res);
	}

	@Test
	public final void testConvertToArray() {

		val obj = new JSONObject("{x:1,y:2,z:3}");

		val res = JSONUtils.convertToArray(obj);
		assertEquals("[\"x\",1,\"y\",2,\"z\",3]", res.toString());
	}

	@Test
	public final void testConvertToArrayEmpty() {

		val obj = new JSONObject("{}");

		val res = JSONUtils.convertToArray(obj);
		assertEquals("[]", res.toString());
	}
}
