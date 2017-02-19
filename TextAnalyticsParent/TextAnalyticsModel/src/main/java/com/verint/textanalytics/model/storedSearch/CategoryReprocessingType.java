package com.verint.textanalytics.model.storedSearch;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 
 * DocumentFieldDataType enum.
 * 
 * @author imor
 *
 */
public enum CategoryReprocessingType {
	//@formatter:off
	Add("Add"),
	Update("Update"),
	Delete("Delete");

	//@formatter:on

	private String type;

	CategoryReprocessingType(String type) {
		this.type = type;
	}

	public String getType() {
		return this.type;
	}

	@Override
	public String toString() {
		return this.type.toLowerCase();
	}
}
