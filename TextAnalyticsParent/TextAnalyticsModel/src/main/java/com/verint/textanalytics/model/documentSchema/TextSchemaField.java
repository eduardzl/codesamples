package com.verint.textanalytics.model.documentSchema;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * TextEngineField.
 * 
 * @author imor
 *
 */
public class TextSchemaField {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String name;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String displayFieldName;

	@Getter
	@Setter
	@Accessors(chain = true)
	private FieldDataType fieldDataType;

	@Getter
	@Setter
	@Accessors(chain = true)
	private DocumentHierarchyType documentHierarchyType;

	@Getter
	@Setter
	@Accessors(chain = true)
	private boolean facetDisable;

	@Override
	public boolean equals(Object schemeFieldObj) {
		if (schemeFieldObj == null) {
			return false;
		}

		if (schemeFieldObj == this) {
			return true;
		}

		if (!(schemeFieldObj instanceof TextSchemaField)) {
			return false;
		}

		TextSchemaField schemeField = (TextSchemaField) schemeFieldObj;

		return this.getName().equals(schemeField.getName()) && this.getDisplayFieldName().equals(schemeField.getDisplayFieldName())
		        && this.getDocumentHierarchyType().equals(schemeField.getDocumentHierarchyType()) && this.getFieldDataType().equals(schemeField.getFieldDataType());
	}

	@Override
	public int hashCode() {
		int hash = 3;
		final int hashPrefix = 53;

		hash = hashPrefix * hash + (this.name != null ? this.name.hashCode() : 0);
		hash = hashPrefix * hash + (this.displayFieldName != null ? this.displayFieldName.hashCode() : 0);
		hash = hashPrefix * hash + (this.fieldDataType != null ? this.fieldDataType.hashCode() : 0);
		hash = hashPrefix * hash + (this.documentHierarchyType != null ? this.documentHierarchyType.hashCode() : 0);
		return hash;
	}
}
