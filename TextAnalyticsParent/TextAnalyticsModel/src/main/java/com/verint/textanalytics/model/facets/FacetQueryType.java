package com.verint.textanalytics.model.facets;


/** JSON Facet API query type.
 * @author EZlotnik
 *
 */
public enum FacetQueryType {
	Terms("terms"),
	Query("query"),
	Range("range");

	private String queryType;

	FacetQueryType(String queryType) {
		this.queryType = queryType;
	}

	public String getQueryType() {
		return this.queryType;
	}
	
	@Override
    public String toString() {
        return this.queryType;
    }
}
