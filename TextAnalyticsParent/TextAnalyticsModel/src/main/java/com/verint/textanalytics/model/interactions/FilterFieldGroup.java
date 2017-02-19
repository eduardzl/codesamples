package com.verint.textanalytics.model.interactions;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * @author TBaum List of FilterFields under the same Group, to filter the same
 *         Utterance
 */
public class FilterFieldGroup {

	@Getter
	@Setter
	private List<FilterField> filters;

	@Getter
	@Setter
	private String groupTag;

	/***
	 * C'tor.
	 */
	public FilterFieldGroup() {
		super();
		this.filters = new ArrayList<FilterField>();
		this.groupTag = null;
	}

	/***
	 * Add new filter to the group.
	 * @param filter
	 *            - the filter to add
	 * @throws Exception
	 *             - if trying to add filter from diffrent group
	 */
	public void addFilter(FilterField filter) throws Exception {

		if (this.groupTag != null && !filter.getGroupTag().equals(this.groupTag)) {
			// filter with diffrent group tag is trying to be added
			throw new Exception("FilterFieldGroup can contain filters from diffrent group tag");
		}

		this.filters.add(filter);
		this.groupTag = filter.getGroupTag();

	}
}
