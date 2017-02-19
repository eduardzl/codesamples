package com.verint.textanalytics.model.interactions;

import lombok.Getter;
import lombok.Setter;

import com.verint.textanalytics.model.documentSchema.FieldDataType;
import com.verint.textanalytics.model.facets.SpeakerQueryType;

/**
 * @author EZlotnik Describes a filter on specific field
 */
public class FilterField {

	@Getter
	@Setter
	private String name;

	@Getter
	@Setter
	private FilterFieldValue[] values;

	@Getter
	@Setter
	private FieldDataType dataType;

	@Getter
	@Setter
	private String groupTag;

	@Getter
	@Setter
	private SpeakerQueryType speaker;

	private boolean locked;

	/***
	 * C'tor.
	 */
	public FilterField() {
		this.locked = false;
		this.speaker = SpeakerQueryType.Any;

	}

	public boolean getLocked() {
		return this.locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;

	}

	/**
	 * Retrieves value at specified index.
	 * 
	 * @param index
	 *            index of value
	 * @return value
	 */
	public FilterFieldValue getValueAt(int index) {
		if (this.values != null && index <= this.values.length - 1) {
			return this.values[index];
		}

		throw new IndexOutOfBoundsException(String.format("Index Value %d is out of bounds for values array. Field name %s", index, this.name));
	}
}
