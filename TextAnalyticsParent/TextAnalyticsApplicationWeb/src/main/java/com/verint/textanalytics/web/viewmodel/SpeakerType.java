package com.verint.textanalytics.web.viewmodel;

import com.verint.textanalytics.common.utils.StringUtils;
import lombok.Getter;

/**
 * Created by EZlotnik on 1/12/2016.
 */
public enum SpeakerType {

	// @formatter:off
	agent("agent"),
	customer("customer"),
	automated("automated"),
	unknown("unknown");
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
		SpeakerType parsedSpeakerType = unknown;

		if (!StringUtils.isNullOrBlank(value)) {
			switch (value.toLowerCase()) {
				case "agent":
				case "employee":
				case "e":
				case "a":
					parsedSpeakerType = agent;
					break;
				case "customer":
				case "c":
					parsedSpeakerType = customer;
					break;
				case "aut":
				case "automated":
					parsedSpeakerType = automated;
					break;
				default:
					parsedSpeakerType = unknown;
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

