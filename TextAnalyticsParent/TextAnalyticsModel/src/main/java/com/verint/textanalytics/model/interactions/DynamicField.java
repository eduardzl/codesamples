package com.verint.textanalytics.model.interactions;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Represents Document Dynamic Field.
 * 
 * @author imor
 *
 */
public class DynamicField {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String name;

	@Getter
	@Setter
	@Accessors(chain = true)
	private DynamicFieldDataType type;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String value;

	/**
	 * DocumentDynamicField constructor.
	 * 
	 * @param name
	 *            the DocumentDynamicField name
	 * @param type
	 *            the DocumentDynamicField type
	 * @param value
	 *            the DocumentDynamicField value
	 * 
	 */
	public DynamicField(String name, DynamicFieldDataType type, String value) {
		super();
		this.name = name;
		this.type = type;
		this.value = value;
	}

	/**
	 * DocumentDynamicField constructor.
	 */
	public DynamicField() {
		super();
	}

}
