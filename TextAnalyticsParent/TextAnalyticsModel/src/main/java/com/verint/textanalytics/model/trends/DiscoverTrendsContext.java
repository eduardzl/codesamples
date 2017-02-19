package com.verint.textanalytics.model.trends;

import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import lombok.Getter;
import lombok.Setter;

/**
 * @author EZlotnik Context representing a current discover trends context.
 */
public class DiscoverTrendsContext extends SearchInteractionsContext {
	@Getter
	@Setter
	private TrendsPeriod trendsPeriod;

	@Getter
	@Setter
	private String baseDate;
}
