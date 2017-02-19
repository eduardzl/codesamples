package com.verint.textanalytics.model.interactions;

import com.sun.corba.se.impl.oa.poa.ActiveObjectMap;
import com.verint.textanalytics.common.utils.StringUtils;
import lombok.Getter;

/**
 * Document Sentiment type.
 * 
 * @author EZlotnik
 *
 */
public enum HighlightType {

	// @formatter:off
	Entity("entity"), 
	Relation("relation"), 
	Term("term"),
	Unknown("unknown"),
	Sentiment("sentiment"),
	KeyTerm("keyterm");
	// @formatter:on

	@Getter
	private String highlightType;

	private HighlightType(String value) {
		this.highlightType = value;
	}

	/**
	 * Converts string value to SpeakerType enum.
	 * @param value
	 *            value to be converted
	 * @return speaker type
	 */
	public static HighlightType toHighlightType(String value) {
		HighlightType parsedHighlightType = Unknown;

		if (!StringUtils.isNullOrBlank(value)) {
			switch (value.toLowerCase()) {
				case "entity":
					parsedHighlightType = Entity;
					break;
				case "sentiment":
					parsedHighlightType = Sentiment;
					break;
				case "relation":
					parsedHighlightType = Relation;
					break;
				case "term":
					parsedHighlightType = Term;
					break;
				case "keyterm":
					parsedHighlightType = KeyTerm;
					break;
				default:
					parsedHighlightType = Unknown;
					break;
			}
		}

		return parsedHighlightType;
	}

	@Override
	public String toString() {
		return this.highlightType.toLowerCase();
	}
}