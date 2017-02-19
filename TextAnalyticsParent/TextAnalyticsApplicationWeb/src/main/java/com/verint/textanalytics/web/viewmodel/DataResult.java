package com.verint.textanalytics.web.viewmodel;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

/**
 * Wrapper for REST service response.
 * @author EZlotnik
 *
 * @param <T>
 *            Type of data to be stored in List
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class DataResult<T> {

	@Getter
	@Setter
	@Accessors(chain = true)
	private T data;

	@Getter
	@Setter
	@Accessors(chain = true)
	private Boolean success;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int totalCount;

	/**
	 * Empty constructor.
	 */
	public DataResult() {

	}
}
