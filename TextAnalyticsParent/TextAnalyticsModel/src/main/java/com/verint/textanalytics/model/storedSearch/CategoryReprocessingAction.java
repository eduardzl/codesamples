package com.verint.textanalytics.model.storedSearch;

import com.verint.textanalytics.common.utils.StringUtils;

/**
 * Created by EZlotnik on 4/7/2016.
 */
public enum CategoryReprocessingAction {
	Delete("delete"),
	Unknown("unknown");

	private String action;

	/**
	 * Constructor.
	 * @param action
	 */
	CategoryReprocessingAction(String action) {
		this.action = action;
	}

	/**
	 * Converting string value to enum.
	 * @param value string value
	 * @return enum of status
	 */
	public static CategoryReprocessingAction fromString(String value) {
		CategoryReprocessingAction enumValue = Unknown;

		if (!StringUtils.isNullOrBlank(value)) {
			switch (value.toLowerCase()) {
				case "delete":
					enumValue = Delete;
					break;
				default:
					enumValue = Unknown;
					break;
			}
		}

		return enumValue;
	}
}
