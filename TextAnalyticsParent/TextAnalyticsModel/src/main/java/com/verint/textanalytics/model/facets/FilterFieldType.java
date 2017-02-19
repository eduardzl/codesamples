package com.verint.textanalytics.model.facets;

/**
 * FilterFieldType.
 * 
 * @author imor
 *
 */
public enum FilterFieldType {
	Text(0), Int(1), Date(2), Boolean(3), Constant(4);

	private int sign;

	FilterFieldType(int sign) {
		this.sign = sign;
	}

	public int getSign() {
		return this.sign;
	}
}
