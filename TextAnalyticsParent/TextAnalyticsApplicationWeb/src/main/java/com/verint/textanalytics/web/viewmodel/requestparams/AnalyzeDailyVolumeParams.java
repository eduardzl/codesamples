package com.verint.textanalytics.web.viewmodel.requestparams;

import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents context for Analyze Daily Volume.
 * 
 * @author NShunewich
 *
 */
public class AnalyzeDailyVolumeParams {
	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	private String channel;

	@Getter
	@Setter
	private SearchInteractionsContext backgroundContext;

	@Getter
	@Setter
	private SearchInteractionsContext searchContext;

}
