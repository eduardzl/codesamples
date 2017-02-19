package com.verint.textanalytics.model.facets;

import com.verint.textanalytics.common.utils.CollectionUtils;

/**
 * Created by EZlotnik on 2/4/2016.
 */
public  class NestedTermsFacet extends NestedFacet {

	/**
	 * Constructor.
	 */
	public NestedTermsFacet() {
		super(FacetQueryType.Terms);
	}

	/**
	 * Generated JSON of Terms nested facet.
	 * @param queryJson StringBuilder to add json to
	 * @param prettyFormat should pretty format
	 * @param isLast is last facet
	 */
	@Override
	public void toJsonStrig(StringBuilder queryJson, Boolean prettyFormat, Boolean isLast) {
		Boolean statsToCal = !CollectionUtils.isEmpty(this.facetStats);

		this.addFacetCommon(queryJson, prettyFormat, statsToCal);
		this.addFacetStats(queryJson);

		if (isLast) {
			queryJson.append(tab + brace);
		} else {
			queryJson.append(tab + brace + ",");
		}
	}
}
