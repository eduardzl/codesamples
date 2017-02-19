package com.verint.textanalytics.model.interactions;

import static com.verint.textanalytics.common.constants.TAConstants.*;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.verint.textanalytics.common.utils.StringUtils;
import lombok.Getter;

/**
 * Utterance Speaker type.
 * 
 * @author EZlotnik
 *
 */
public enum SpeakerType {

	// @formatter:off
	Agent("agent"), 
	Customer("customer"), 
	Automated("automated"), 
	Unknown("unknown");
	// @formatter:on

	@Getter
	private String speakerType;

	private SpeakerType(String value) {
		this.speakerType = value;
	}

	/**
	 * Converts string value to SpeakerType enum.
	 * @param value
	 *            value to be converted
	 * @return speaker type
	 */
	public static SpeakerType toSpeakerType(String value) {
		SpeakerType parsedSpeakerType = Unknown;

		if (!StringUtils.isNullOrBlank(value)) {
			switch (value.toLowerCase()) {
				case "agent":
				case "employee":
				case "e":
				case "a":
					parsedSpeakerType = Agent;
				break;
				case "customer":
				case "c":
					parsedSpeakerType = Customer;
					break;
				case "aut":
				case "automated":
					parsedSpeakerType = Automated;
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
		return this.speakerType.toLowerCase();
	}
}

