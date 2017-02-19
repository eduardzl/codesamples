package com.verint.textanalytics.web.viewmodel;

import com.verint.textanalytics.common.utils.StringUtils;
import lombok.Getter;

/**
 * Document Sentiment type.
 *
 * @author EZlotnik
 */
public enum HighlightTypeCIV {

	// @formatter:off
	Entity("entity"),
	Relation("relation"),
	Unknown("unknown"),
	Sentiment("sentiment"),
	Search("search"),
	Category("category"),
	KeyTerm("keyterm");
	// @formatter:on

	@Getter
	private String highlightType;

	private HighlightTypeCIV(String value) {
		this.highlightType = value;
	}

	/**
	 * Converts string value to SpeakerType enum.
	 *
	 * @param value value to be converted
	 * @return speaker type
	 */
	public static HighlightTypeCIV toHighlightType(String value) {
		HighlightTypeCIV parsedHighlightType = Unknown;

		if (!StringUtils.isNullOrBlank(value)) {
			switch (value.toLowerCase()) {
				case "entity":
					parsedHighlightType = Entity;
					break;
				case "relation":
					parsedHighlightType = Relation;
					break;
				case "search":
					parsedHighlightType = Search;
					break;
				case "category":
					parsedHighlightType = Category;
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