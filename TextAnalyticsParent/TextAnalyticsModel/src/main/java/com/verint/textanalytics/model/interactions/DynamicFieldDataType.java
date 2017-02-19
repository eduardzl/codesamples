package com.verint.textanalytics.model.interactions;

import com.fasterxml.jackson.databind.JsonNode;
import org.json.JSONObject;

import com.verint.textanalytics.common.utils.JSONUtils;

/**
 * 
 * DocumentFieldDataType enum.
 * 
 * @author imor
 *
 */
public enum DynamicFieldDataType {
	//@formatter:off
	Int("i"), 
	String("s"),
	StringArray("ss"),
	IntArray("si"),
	Double("d"), 
	Text("t"), 
	Boolean("b"),
	Long("l"),
	Date("dt");
	//@formatter:on

	private String sign;

	DynamicFieldDataType(String sign) {
		this.sign = sign;
	}

	public String getSign() {
		return this.sign;
	}

	/**
	 * Get DocumentFieldDataType from Element Name.
	 * 
	 * @param elmName
	 *            the Element Name
	 * @return DocumentFieldDataType
	 */
	public static DynamicFieldDataType fromElementName(String elmName) {
		return fromSign(elmName.split("_")[1]);
	}

	/**
	 * @param sign
	 *            the DocumentFieldDataType sign
	 * @return DocumentFieldDataType
	 */
	public static DynamicFieldDataType fromSign(String sign) {
		if (sign != null) {
			for (DynamicFieldDataType documentFieldDataType : DynamicFieldDataType.values()) {
				if (sign.equalsIgnoreCase(documentFieldDataType.sign)) {
					return documentFieldDataType;
				}
			}
		}
		return null;
	}

	/**
	 * @param docElem
	 *            the doc element
	 * @param elmName
	 *            the element name
	 * @return the element value
	 */
	public static String getValue(JsonNode docElem, String elmName) {

		DynamicFieldDataType documentFieldDataType = DynamicFieldDataType.fromElementName(elmName);

		switch (documentFieldDataType) {
			case Int:
				return Integer.toString(docElem.path(elmName).asInt(0));
			case Boolean:
				return java.lang.Boolean.toString(docElem.path(elmName).asBoolean(false));
			case Double:
				return java.lang.Double.toString(docElem.path(elmName).asDouble(0));
			case StringArray:
				return docElem.path(elmName).toString();
			case Long:
				return java.lang.Long.toString(docElem.path(elmName).asLong(0));
			case String:
			case Text:
			case Date:
			default:
				return docElem.path(elmName).asText("");
		}
	}
}
