package com.verint.textanalytics.web.viewmodel.requestparams;

import com.verint.textanalytics.model.facets.SpeakerQueryType;
import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Parameters for Interactions daily volume request.
 * @author EZlotnik
 *
 */
@NoArgsConstructor
public class TrendSeriesParams {
	@Getter
	@Setter
	private String channel;

	@Getter
	@Setter
	private String value;

	@Getter
	@Setter
	private int clientTimeZoneOffset;

	@Getter
	@Setter
	private SearchInteractionsContext searchContext;

	@Getter
	@Setter
	private SpeakerQueryType speaker;
}
