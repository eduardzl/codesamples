package com.verint.textanalytics.web.viewmodel.requestparams;

import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import lombok.Getter;
import lombok.Setter;

/**
 * Result Set metrics request parameters.
 * 
 * @author imor
 *
 */
public class CurrentResultSetMetricsParams {
	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	private String channel;

	@Getter
	@Setter
	private SearchInteractionsContext currentSearchContext;

	@Getter
	@Setter
	private SearchInteractionsContext backgroundContext;

}
