package com.verint.textanalytics.model.facets;

/**
 * FacetType.
 * 
 * @author imor
 *
 */
public enum TextElementType {

	Entities(0),
	Relations(1),
	Themes(2),
	// For Text Element Chart only
	Categories(3);

	private int type;

	TextElementType(int type) {
		this.type = type;
	}

	/**
	 * Returns text element type.
	 * @return type
	 */
	public int getType() {
		return this.type;
	}
}
