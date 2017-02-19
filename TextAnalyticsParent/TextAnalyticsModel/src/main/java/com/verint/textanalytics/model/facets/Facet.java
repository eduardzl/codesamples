package com.verint.textanalytics.model.facets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.verint.textanalytics.model.documentSchema.DocumentHierarchyType;
import com.verint.textanalytics.model.documentSchema.FieldDataType;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Facet.
 * 
 * @author imor
 *
 */
public class Facet {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String fieldName;

	@Getter
	@Setter
	@Accessors(chain = true)
	private FacetType type;

	@Getter
	@Setter
	@Accessors(chain = true)
	private FieldDataType valuesDataType;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int totalCount;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<FacetResultGroup> values;

	/**
	 * Facet constructor.
	 */
	public Facet() {
		this.values = new ArrayList<FacetResultGroup>();
	}

	/**
	 * Add group to facet groups.
	 * @param groupValue
	 *            group to add
	 */
	public void addGroupValue(FacetResultGroup groupValue) {
		this.values.add(groupValue);
	}

	/**
	 * Sorts facet buckets by interactions count in descending order.
	 */
	public void sortByInteractionsCountDescending() {
		Collections.sort(this.values, Collections.reverseOrder());
	}

	/**
	 * Sorts sentiment facet buckets by title.
	 */

	public void sortBySentimentValue() {

		this.values.sort(new Comparator<FacetResultGroup>() {

			@Override
			public int compare(FacetResultGroup facetResultGroup1, FacetResultGroup facetResultGroup2) {

				if (facetResultGroup1 instanceof FacetSentimentSingleValueResultGroup && facetResultGroup2 instanceof FacetSentimentSingleValueResultGroup) {
					return ((FacetSentimentSingleValueResultGroup) facetResultGroup2).getSentimentValue()
		                    - ((FacetSentimentSingleValueResultGroup) facetResultGroup1).getSentimentValue();
				} else {
					return facetResultGroup1.getCount() - facetResultGroup2.getCount();
				}
			}
		});
	}
}
