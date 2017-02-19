package com.verint.textanalytics.model.interactions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.verint.textanalytics.common.constants.TAConstants;

import com.verint.textanalytics.common.utils.CollectionUtils;
import propel.core.functional.Actions.Action1;
import propel.core.utils.Linq;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

/**
 * @author EZlotnik Context representing a current interactions search
 */
public class SearchInteractionsContext {

	@Getter
	@Setter
	private List<String> terms;

	@Getter
	@Setter
	private List<FilterField> filterFields;

	@Getter
	@Setter
	private List<RangeFilterField> rangeFilterFields;

	/**
	 * Empty C'tor.
	 */
	public SearchInteractionsContext() {

	}

	/**
	 * Creates new Search Interactions context.
	 * @param terms terms
	 * @param filterFields filters
	 * @param rangeFilterFields range filter fields
	 */
	public SearchInteractionsContext(List<String> terms, List<FilterField> filterFields, List<RangeFilterField> rangeFilterFields) {

		// Deep clone terms
		if (terms != null) {
			this.terms = new ArrayList<>();
			for (String term : terms) {
				this.terms.add(term);
			}
		}

		// Deep clone list of fiels but not the filters
		if (filterFields != null) {
			this.filterFields = new ArrayList<>();
			for (FilterField filterField : filterFields) {
				this.filterFields.add(filterField);
			}
		}

		// Deep clone range filter fields, but no range filters
		if (rangeFilterFields != null) {
			this.rangeFilterFields = new ArrayList<>();
			for (RangeFilterField rangeFilterField : rangeFilterFields) {
				this.rangeFilterFields.add(rangeFilterField);
			}
		}
	}

	/**
	 * Creates clone of givven context.
	 * @return cloned object
	 */
	public SearchInteractionsContext cloneMe() {
		return new SearchInteractionsContext(this.getTerms(), this.getFilterFields(), this.getRangeFilterFields());
	}
}
