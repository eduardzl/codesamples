package com.verint.textanalytics.model.facets;


/** JSON Facet API query type.
 * @author EZlotnik
 *
 */
public enum FacetMethodType {
	uif("uif"),
	dv("dv"),
	stream("stream");

	private String methodType;

	FacetMethodType(String methodType) {
		this.methodType = methodType;
	}

	public String getMethodType() {
		return this.methodType;
	}
	
	@Override
    public String toString() {
        return this.methodType;
    }
}
