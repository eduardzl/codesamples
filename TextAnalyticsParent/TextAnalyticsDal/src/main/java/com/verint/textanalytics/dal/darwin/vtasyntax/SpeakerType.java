package com.verint.textanalytics.dal.darwin.vtasyntax;

/**
 * Created by EZlotnik on 2/29/2016.
 */
public enum SpeakerType {

	// @formatter:off
	Agent("agent"),
	Customer("customer"),
	NoSPS("unknown");
	// @formatter:on

	private String speakerType;

	private SpeakerType(String value) {
		this.speakerType = value;
	}
}


