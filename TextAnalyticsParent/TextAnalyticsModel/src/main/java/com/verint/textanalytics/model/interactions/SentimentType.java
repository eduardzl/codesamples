package com.verint.textanalytics.model.interactions;

import static com.verint.textanalytics.common.constants.TAConstants.*;

/**
 * Document Sentiment type.
 * 
 * @author EZlotnik
 *
 */
public enum SentimentType {

	VeryNegative(sentimentVeryNegative), Negative(sentimentNegative), Neutral(sentimentNeutral), Positive(sentimentPositive), VeryPositive(sentimentVeryPositive);

	private int value;

	private SentimentType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	/**
	 * Converts int to enum value.
	 * @param x
	 *            int value
	 * @return enum value
	 */
	public static SentimentType toSentimentType(int x) {

		switch (x) {
			case sentimentVeryNegative:
				return VeryNegative;
			case sentimentNegative:
				return Negative;
			case sentimentNeutral:
				return Neutral;
			case sentimentPositive:
				return Positive;
			case sentimentVeryPositive:
				return VeryPositive;
			default:
				return Neutral;
		}
	}
}
