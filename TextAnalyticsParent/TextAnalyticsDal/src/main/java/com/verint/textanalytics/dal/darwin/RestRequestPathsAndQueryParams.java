package com.verint.textanalytics.dal.darwin;

import java.util.*;
import com.verint.textanalytics.common.collection.InsertionOrderedMultiValueStringMap;
import com.verint.textanalytics.common.collection.MultivaluedStringMap;
import propel.core.functional.tuples.Pair;
import lombok.Getter;
import lombok.Setter;

/**
 * Wrapper for URL paths and query parameters.
 * 
 * @author EZlotnik
 *
 */
public class RestRequestPathsAndQueryParams {
	@Getter
	private List<String> queryPaths;

	@Getter
	private MultivaluedStringMap queryParams;

	@Getter
	@Setter
	private List<Pair<String, String>> bodyParams;

	/**
	 * Constructor.
	 * @param keepParametersOrder
	 *            should parameters be kept in the same order as added
	 */
	public RestRequestPathsAndQueryParams(Boolean keepParametersOrder) {
		this.queryPaths = new ArrayList<String>();
		this.queryParams = new InsertionOrderedMultiValueStringMap();

	}

	/**
	 * C'tor.
	 */
	public RestRequestPathsAndQueryParams() {
		this.queryPaths = new ArrayList<>();
		this.queryParams = new InsertionOrderedMultiValueStringMap();
	}
}
