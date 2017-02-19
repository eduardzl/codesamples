package com.verint.textanalytics.model.facets;

import com.verint.textanalytics.common.constants.TAConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * SpeakerQueryType.
 * 
 * @author tbaum
 *
 */
public enum SpeakerQueryType {
	//@formatter:off
	Any(0),
	Agent(1),
	Customer(2),
	Automated(3);
	//@formatter:on

	private int speakerType;

	SpeakerQueryType(int speakerType) {
		this.speakerType = speakerType;
	}

	public int getSpeakerType() {
		return this.speakerType;
	}

	@Override
	public String toString() {
		switch (this) {
			case Agent:
				return TAConstants.SpeakerTypeValues.Agent;
			case Customer:
				return  TAConstants.SpeakerTypeValues.Customer;
			case Automated:
				return  TAConstants.SpeakerTypeValues.Automated;
			default:
				throw new IllegalArgumentException();
		}
	}
}
