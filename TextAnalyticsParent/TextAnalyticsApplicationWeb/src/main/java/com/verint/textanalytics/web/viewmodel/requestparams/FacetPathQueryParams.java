package com.verint.textanalytics.web.viewmodel.requestparams;

import com.verint.textanalytics.model.facets.SpeakerQueryType;
import com.verint.textanalytics.model.facets.TextElementMetricType;
import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Wrapper for Facet Path request.
 * @author TBaum
 *
 */
public class FacetPathQueryParams {

	@Getter
	@Setter
	private String channel;

	@Getter
	@Setter
	private SearchInteractionsContext searchContext;

	@Getter
	@Setter
	private List<String> facetsQueries;

	@Getter
	@Setter
	private SpeakerQueryType facetOnSpeaker;

	@Getter
	@Setter
	private TextElementMetricType size;

	@Getter
	@Setter
	private Boolean queryOnSameUtterances;

	@Getter
	@Setter
	private Boolean facetOnSameUtterances;

	@Getter
	@Setter
	private int textElementType;

	/***
	 * C'tor.
	 */
	public FacetPathQueryParams() {
		this.queryOnSameUtterances = false;
		this.facetOnSameUtterances = false;
	}

}
