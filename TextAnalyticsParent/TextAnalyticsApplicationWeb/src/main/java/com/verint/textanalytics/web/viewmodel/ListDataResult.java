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
public class ListDataResult<T> {

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<T> data;

	@Getter
	@Setter
	@Accessors(chain = true)
	private Boolean success;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int totalCount;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String errorMessage;

	/**
	 * Empty constructor.
	 */
	public ListDataResult() {

	}
}
