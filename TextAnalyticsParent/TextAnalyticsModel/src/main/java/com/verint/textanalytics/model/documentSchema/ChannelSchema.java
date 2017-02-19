package com.verint.textanalytics.model.documentSchema;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Channel.
 * 
 * @author imor
 *
 */
public class ChannelSchema {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String name;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<TextSchemaField> fields;

	/**
	 * C'tor.
	 */
	public ChannelSchema() {
		this.fields = new ArrayList<>();
	}

	/**
	 * Adds field to list of channel's fields .
	 * @param schemaField
	 *            field to add
	 */
	public void addSchemaField(TextSchemaField schemaField) {
		this.fields.add(schemaField);
	}

	/**
	 * getTextEngineField.
	 * 
	 * @param fieldName
	 *            fieldName
	 * @return TextEngineField
	 */
	public TextSchemaField getTextEngineField(String fieldName) {
		for (TextSchemaField field : fields) {
			if (field.getName()
			         .equals(fieldName)) {
				return field;
			}
		}
		return null;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof ChannelSchema)) {
			return false;
		}

		return this.getName()
		           .equals(((ChannelSchema) obj).getName());

	}

	@Override
	public int hashCode() {
		int hash = 3;
		final int hashPrefix = 53;

		hash = hashPrefix * hash + (this.name != null ? this.name.hashCode() : 0);
		return hash;
	}
}
