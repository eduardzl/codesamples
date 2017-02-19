package com.verint.textanalytics.model.facets;

import java.util.List;

import com.verint.textanalytics.common.utils.CollectionUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * NestedRangeFacet.
 * 
 * @author imor
 *
 */
@Accessors(chain = true)
public class NestedRangeFacet extends NestedFacet {
	@Getter
	@Setter
	private String rangeStart;

	@Getter
	@Setter
	private String rangeEnd;

	@Getter
	@Setter
	private String rangeGap;

	/**
	 * Constructor.
	 */
	public NestedRangeFacet() {
		super(FacetQueryType.Range);
	}

	/**
	 * Generated Json of Range Facet.
	 * @param queryJson quert json
	 * @param prettyFormat should pretty format the json
	 * @param isLast is facet last one in the list
	 */
	@Override
	public void toJsonStrig(StringBuilder queryJson, Boolean prettyFormat, Boolean isLast) {
		Boolean appendCommaAtEnd = !CollectionUtils.isEmpty(this.facetStats);

		this.addFacetCommon(queryJson, prettyFormat, true);
		this.addFacetSpecific(queryJson, prettyFormat, appendCommaAtEnd);
		this.addFacetStats(queryJson);

		if (isLast) {
			queryJson.append(tab + brace);
		} else {
			queryJson.append(tab + brace + ",");
		}
	}

	private void addFacetSpecific(StringBuilder queryJson, Boolean prettyFormat, Boolean appendCommaAtEnd) {
		switch (this.fieldDataType) {
			case Date:
				queryJson.append(tab2 + String.format("start : \"%s\", %s", this.rangeStart, nl));
				queryJson.append(tab2 + String.format("end : \"%s\", %s", this.rangeEnd, nl));
				if (appendCommaAtEnd) {
					queryJson.append(tab2 + String.format("gap : \"%s\", %s", this.rangeGap, nl));
				} else {
					queryJson.append(tab2 + String.format("gap : \"%s\" %s", this.rangeGap, nl));
				}
				break;
			default:
				queryJson.append(tab2 + String.format("start : %s, %s", this.rangeStart, nl));
				queryJson.append(tab2 + String.format("end : %s, %s", this.rangeEnd, nl));
				if (appendCommaAtEnd) {
					queryJson.append(tab2 + String.format("gap : %s, %s", this.rangeGap, nl));
				} else {
					queryJson.append(tab2 + String.format("gap : %s %s", this.rangeGap, nl));
				}
				break;
		}
	}
}