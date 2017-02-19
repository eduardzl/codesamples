package com.verint.textanalytics.web.viewmodel.requestparams;

import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Represents context for Analyze Metrics.
 * 
 * @author imor
 *
 */
public class EntitiesMetricsParams {

	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String channel;

	@Getter
	@Setter
	@Accessors(chain = true)
	private SearchInteractionsContext currentSearchContext;

}
