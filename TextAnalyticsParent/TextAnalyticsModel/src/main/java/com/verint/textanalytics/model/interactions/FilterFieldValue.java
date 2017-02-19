package com.verint.textanalytics.model.interactions;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/***
 * 
 * @author yzanis
 *
 */
public class FilterFieldValue {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String value;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String valueTitleKey;

	@Getter
	@Setter
	@Accessors(chain = true)
	private SpeakerType speakerType;
	/**
	 * 
	 */
	public FilterFieldValue() {
	}

	/**
	 * @param value
	 *            value
	 * @param valueTitleKey
	 *            valueTitleKey
	 */
	public FilterFieldValue(String value, String valueTitleKey) {
		super();
		this.value = value;
		this.valueTitleKey = valueTitleKey;
	}

	/**
	 * @param value
	 *            value
	 */
	public FilterFieldValue(String value) {
		super();
		this.value = value;
		this.valueTitleKey = "";
	}

}