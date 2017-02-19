package com.verint.textanalytics.model.interactions;

import com.verint.textanalytics.common.utils.StringUtils;

import lombok.Getter;

/**
 * Document Sentiment type.
 * 
 * @author EZlotnik
 *
 */
public enum SourceType {

	// @formatter:off
	Chat("chat"), 
	Email("email"), 
	Unknown("Unknown");
	// @formatter:on

	@Getter
	private String sourceType;

	private SourceType(String value) {
		this.sourceType = value;
	}

	/**
	 * Converts string value to SpeakerType enum.
	 * @param value
	 *            value to be converted
	 * @return speaker type
	 */
	public static SourceType toSourceType(String value) {
		SourceType parsedSpeakerType = Unknown;

		if (!StringUtils.isNullOrBlank(value)) {
			switch (value.toLowerCase()) {
				case "chat":
					parsedSpeakerType = Chat;
					break;
				case "email":
					parsedSpeakerType = Email;
					break;
				default:
					parsedSpeakerType = Unknown;
					break;
			}
		}

		return parsedSpeakerType;
	}

	@Override
	public String toString() {
		return this.sourceType.toLowerCase();
	}
}