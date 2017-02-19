package com.verint.textanalytics.web.viewmodel.requestparams;

import com.verint.textanalytics.model.facets.SpeakerQueryType;
import com.verint.textanalytics.model.trends.DiscoverTrendsContext;
import lombok.Getter;
import lombok.Setter;

/**
 * Wrapper for Search Document request.
 * @author EZlotnik
 *
 */

public class DiscoverTrendsParams {
	@Getter
	@Setter
	private String channel;

	@Getter
	@Setter
	private String value;

	@Getter
	@Setter
	private int prefix;

	@Getter
	@Setter
	private String sortProperty;

	@Getter
	@Setter
	private String sortDirection;

	@Getter
	@Setter
	private SpeakerQueryType speaker;

	@Getter
	@Setter
	private DiscoverTrendsContext searchContext;
}
