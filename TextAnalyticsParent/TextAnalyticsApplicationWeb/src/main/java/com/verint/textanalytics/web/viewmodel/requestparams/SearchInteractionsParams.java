package com.verint.textanalytics.web.viewmodel.requestparams;

import com.verint.textanalytics.model.facets.SpeakerQueryType;
import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Wrapper for Search Interactions request.
 * 
 * @author EZlotnik
 *
 */
public class SearchInteractionsParams {
	@Getter
	@Setter
	private String channel;

	@Getter
	@Setter
	private SearchInteractionsContext searchContext;

	@Getter
	@Setter
	private SearchInteractionsContext backgroundContext;
	
	@Getter
	@Setter
	private List<String> facetsQueries;

	@Getter
	@Setter
	private SpeakerQueryType querySpeakerType;

	@Getter
	@Setter
	private Boolean queryOnSameUtterances;

	/***
	 * C'tor.
	 */
	public SearchInteractionsParams() {
		this.queryOnSameUtterances = false;
	}
}
