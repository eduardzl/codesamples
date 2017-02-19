package com.verint.textanalytics.model.facets;

/**
 * Created by EZlotnik on 6/14/2016.
 */


public enum SimpleFacetMethod {
	Enum("enum"),
	Fc("fc"),
	Fcs("fcs");

	private String facetMethod;

	SimpleFacetMethod(String method) {
		this.facetMethod = method;
	}

	public String getQueryType() {
		return this.facetMethod;
	}

	@Override
    public String toString() {
        return this.facetMethod;
    }
}
