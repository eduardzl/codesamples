package com.verint.textanalytics.model.trends;

/**
 * TrendType.
 * 
 * @author nshunewich
 *
 */
public enum TrendType {

	//@formatter:off
	Entities(0),
	Relations(1),
	Keyterms(2),
	Themes(3),
	Categories(4);
	//@formatter:on

	private int type;

	TrendType(int type) {
		this.type = type;
	}

	public int getType() {
		return this.type;
	}
}
