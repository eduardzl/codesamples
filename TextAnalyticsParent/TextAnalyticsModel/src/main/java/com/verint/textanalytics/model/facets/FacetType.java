package com.verint.textanalytics.model.facets;

/**
 * FacetType.
 * 
 * @author imor
 *
 */
public enum FacetType {

	Ranges(0), SingleValues(1);

	private int sign;

	FacetType(int sign) {
		this.sign = sign;
	}

	public int getSign() {
		return this.sign;
	}
}
