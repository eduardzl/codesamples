package com.verint.textanalytics.web.viewmodel;

import java.util.*;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Wrapper for REST service response.
 * @param <T> Type of data to be stored in List
 * @author EZlotnik
 */
public class InteractionsListDataResult<T> extends ListDataResult<T> {

	@Getter
	@Setter
	@Accessors(chain = true)
	private Double maxRelevancyScore;

	/**
	 * Empty constructor.
	 */
	public InteractionsListDataResult() {
		super();
	}
}
