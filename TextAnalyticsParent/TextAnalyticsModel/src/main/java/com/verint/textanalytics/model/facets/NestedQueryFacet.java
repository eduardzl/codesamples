package com.verint.textanalytics.model.facets;

import com.verint.textanalytics.common.utils.CollectionUtils;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by EZlotnik on 2/4/2016.
 */
public  class NestedQueryFacet extends NestedFacet {
	@Setter
	@Getter
	private String query;

	/**
	 * Constructor accepted type of facet.
	 */
	public NestedQueryFacet() {
		super(FacetQueryType.Query);
	}

	/**
	 * Generated Json of Range Facet.
	 * @param queryJson quert json
	 * @param prettyFormat should pretty format the json
	 * @param isLast is facet last one to be requested
	 */
	@Override
	public void toJsonStrig(StringBuilder queryJson, Boolean prettyFormat, Boolean isLast) {
		Boolean hasStats = !CollectionUtils.isEmpty(this.facetStats);

		this.addFacetCommon(queryJson, prettyFormat, true);
		this.addFacetSpecific(queryJson, prettyFormat, hasStats);
		this.addFacetStats(queryJson);

		if (isLast) {
			queryJson.append(tab + brace);
		} else {
			queryJson.append(tab + brace + ",");
		}
	}

	private void addFacetSpecific(StringBuilder queryJson, Boolean prettyFormat, Boolean hasStats) {
		if (hasStats) {
			queryJson.append(tab2 + String.format("q : \"%s\", %s", this.query, nl));
		} else {
			queryJson.append(tab2 + String.format("q : \"%s\" %s", this.query, nl));
		}
	}
}
