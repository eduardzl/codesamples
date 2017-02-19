package com.verint.textanalytics.model.storedSearch;

import com.verint.textanalytics.common.utils.StringUtils;
import lombok.Getter;

/**
 * Created by EZlotnik on 4/7/2016.
 */
public enum CategoryReprocessingStatus {

	Unknown(0),
	Reprocessing(1),
	Reprocessed(2),
	Error(3);

	private static final String reprocessed = "reprocessed";
	private static final String reprocessing = "reprocessing";
	private static final String error = "error";

	private int status;

	CategoryReprocessingStatus(int status) {
		this.status = status;
	}

	/**
	 * Retrieves numeric value of enum.
	 * @return numeric value
	 */
	public int value() {
		return this.status;
	}

	/**
	 * Converts string value to enum.
	 * @param value string value
	 * @return enum value of specified string. Unknown if parsing failed
	 */
	public static CategoryReprocessingStatus fromString(String value) {
		CategoryReprocessingStatus enumValue = Unknown;

		if (!StringUtils.isNullOrBlank(value)) {
			switch (value.toLowerCase()) {
				case reprocessed:
					enumValue = Reprocessed;
					break;
				case reprocessing:
					enumValue = Reprocessing;
					break;
				case error:
					enumValue = Error;
					break;
				default:
					enumValue = Unknown;
					break;
			}
		}

		return enumValue;
	}
}
