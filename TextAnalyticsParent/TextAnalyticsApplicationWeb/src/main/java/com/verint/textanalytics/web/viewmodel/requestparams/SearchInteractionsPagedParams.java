package com.verint.textanalytics.web.viewmodel.requestparams;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * Wrapper for Search Interactions paged request.
 * @author EZlotnik
 */
public class SearchInteractionsPagedParams extends SearchInteractionsParams implements Serializable {
	private static final long serialVersionUID = 1L;

	@Setter
	@Getter
	private int pageStart;

	@Setter
	@Getter
	private int pageSize;

	@Setter
	@Getter
	private String sortProperty;

	@Setter
	@Getter
	private String sortDirection;
}
