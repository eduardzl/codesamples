package com.verint.textanalytics.model.facets;

/**
 * Created by EZlotnik on 5/31/2016.
 */
public enum  TextElementsFacetCalculationType {

	FacetOnChildren(0),
	FacetOnTopLeafs(1);

	private int calculationType;

	TextElementsFacetCalculationType(int calcType) {
		this.calculationType = calcType;
	}
}
