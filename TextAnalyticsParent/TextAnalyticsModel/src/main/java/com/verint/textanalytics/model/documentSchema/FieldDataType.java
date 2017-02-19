package com.verint.textanalytics.model.documentSchema;

import java.util.HashMap;
import java.util.Map;

/**
 * @author EZlotnik Enumeration for filter field data type
 */
public enum FieldDataType {

	// Don't forget to keep this enum in sync with ExtJs enum - TextAnalytics.searchInteractions.enumeration.FieldType
	// NOTE : you must keep the order !!! 0,1,2,3,4,5,6 and etc. !!! the reason is in the ConfigServiceREsponseConverter 

	//@formatter:off
	Text(0), 
	Int(1),	
	Date(2),
	Boolean(3),
	Constant(4),
	Long(5);
	//@formatter:on

	private static final Map<Integer, FieldDataType> typesByValue = new HashMap<Integer, FieldDataType>();
	private int sign;

	/**
	 * Constructor.
	 *
	 * @param sign
	 */
	FieldDataType(int sign) {
		this.sign = sign;
	}

	public int getSign() {
		return this.sign;
	}

	static {
		for (FieldDataType type : FieldDataType.values()) {
			typesByValue.put(type.getSign(), type);
		}
	}

	/**
	 * @param sign sign
	 * @return FieldDataType
	 */
	public static FieldDataType forSign(int sign) {
		return typesByValue.get(sign);
	}

}