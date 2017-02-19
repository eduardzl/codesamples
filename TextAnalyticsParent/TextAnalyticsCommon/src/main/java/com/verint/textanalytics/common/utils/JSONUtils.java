package com.verint.textanalytics.common.utils;

import java.util.Iterator;

import org.json.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import org.apache.logging.log4j.*;

/**
 * JSON utils.
 * 
 * @author EZlotnik
 *
 */
public final class JSONUtils {
	private static Logger s_logger;

	static {
		s_logger = LogManager.getLogger(JSONUtils.class.getName());
	}

	/**
	 * Private C'tor.
	 */
	private JSONUtils() {

	}

	/**
	 * Searches for inner node in json object and if founds returns it as.
	 * JSONObject
	 * 
	 * @param elem
	 *            parent element
	 * @param childNodeName
	 *            node name to search
	 * @return inner json node
	 */
	public static JSONObject getJSONObject(JSONObject elem, String childNodeName) {
		try {
			if (elem != null && elem.has(childNodeName)) {
				Object childNode = elem.getJSONObject(childNodeName);
				return childNode != null ? (JSONObject) childNode : null;
			}
		} catch (Exception e) {
			s_logger.error("Exception in JSONUtils.getJSONObject:. Error {}", e);
		}

		return null;
	}

	/**
	 * Searches for inner node in json object and if founds returns it as.
	 * 
	 * @param elem
	 *            parent element
	 * @param childNodeName
	 *            node name to search
	 * @return inner json node
	 */
	public static JSONArray getJSONArray(JSONObject elem, String childNodeName) {
		try {
			if (elem != null && elem.has(childNodeName)) {
				Object childNode = elem.getJSONArray(childNodeName);
				return childNode != null ? (JSONArray) childNode : null;
			}
		} catch (Exception e) {
			s_logger.error("Exception in JSONUtils.getJSONArray: {}", e);
		}

		return null;
	}

	/**
	 * Convert JSON node to string.
	 * 
	 * @param childNodeName
	 *            -
	 * @param elem
	 *            -
	 * @param defaultValue
	 *            -
	 * @return string
	 */
	public static String getString(String childNodeName, JSONObject elem, String defaultValue) {
		try {
			if (elem != null && elem.has(childNodeName)) {
				String nodeValue = elem.getString(childNodeName);
				return nodeValue != null ? nodeValue : defaultValue;
			}
		} catch (Exception e) {
			s_logger.error("Exception in JSONUtils.getString: {}", e);
		}
		return defaultValue;
	}

	/**
	 * Get inner node as int.
	 * 
	 * @param childNodeName
	 *            node name
	 * @param elem
	 *            element
	 * @param defaultValue
	 *            default value
	 * @return int value of node
	 */
	public static int getInt(String childNodeName, JSONObject elem, int defaultValue) {
		try {
			if (elem != null && elem.has(childNodeName)) {
				int childNode = elem.getInt(childNodeName);
				return childNode;
			}
		} catch (Exception e) {
			s_logger.error("Exception in JSONUtils.getInt: {}", e);
		}

		return defaultValue;
	}

	/**
	 * Get node value as boolean.
	 * 
	 * @param childNodeName
	 *            node name
	 * @param elem
	 *            parent element
	 * @param defaultValue
	 *            default value
	 * @return node as boolean
	 */
	public static boolean getBoolean(String childNodeName, JSONObject elem, boolean defaultValue) {

		try {
			if (elem != null && elem.has(childNodeName)) {
				boolean childNode = elem.getBoolean(childNodeName);
				return childNode;
			}
		} catch (Exception e) {
			s_logger.error("Exception in JSONUtils.getBoolean: {}", e);
		}

		return defaultValue;
	}

	/**
	 * Get node value as double.
	 * 
	 * @param childNodeName
	 *            node name
	 * @param elem
	 *            parent element
	 * @param defaultValue
	 *            default value
	 * @return node as double
	 */
	public static double getDouble(String childNodeName, JSONObject elem, double defaultValue) {
		try {
			if (elem != null && elem.has(childNodeName)) {
				double childNode = elem.getDouble(childNodeName);
				return childNode;
			}
		} catch (Exception e) {
			s_logger.error("Exception in JSONUtils.getDouble: ", e);
		}

		return defaultValue;
	}

	/**
	 * Get node value as long.
	 * 
	 * @param childNodeName
	 *            node name
	 * @param elem
	 *            parent element
	 * @param defaultValue
	 *            default value
	 * @return node as long
	 */
	public static long getLong(String childNodeName, JSONObject elem, long defaultValue) {
		try {
			if (elem != null && elem.has(childNodeName)) {
				long childNode = elem.getLong(childNodeName);
				return childNode;
			}
		} catch (Exception e) {
			s_logger.error("Exception in JSONUtils.getLong: {}", e);
		}

		return defaultValue;
	}

	/**
	 * Converts object to JSON representation string.
	 * 
	 * @param obj
	 *            object to convert
	 * @return JSON string
	 */
	public static String getObjectJSON(Object obj) {

		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			return "";
		}
	}

	/**
	 * Returns keys of all child objects.
	 * @param jObject
	 *            json object
	 * @return all child object keys
	 */
	public static String[] getObjectKeys(JSONObject jObject) {
		if (jObject != null) {
			return JSONObject.getNames(jObject);
		}

		return null;
	}

	/**
	 * Convert JSONObject to JSONArray.
	 * 
	 * @param jObject
	 *            jObject
	 * @return JSONArray
	 */
	public static JSONArray convertToArray(JSONObject jObject) {

		Iterator<?> x = jObject.keys();
		JSONArray jsonArray = new JSONArray();

		while (x.hasNext()) {
			String key = (String) x.next();
			jsonArray.put(key);
			jsonArray.put(jObject.get(key));
		}
		return jsonArray;
	}
}
