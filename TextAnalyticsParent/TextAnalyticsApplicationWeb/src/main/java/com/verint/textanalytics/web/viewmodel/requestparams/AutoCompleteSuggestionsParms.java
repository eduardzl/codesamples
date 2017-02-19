package com.verint.textanalytics.web.viewmodel.requestparams;

import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import lombok.Getter;
import lombok.Setter;

/**
 * Terms AutoComplete request parameters.
 * @author yzanis
 *
 */
public class AutoCompleteSuggestionsParms {
	@Getter
	@Setter
	private String channel;

	@Getter
	@Setter
	private SearchInteractionsContext searchContext;

	@Getter
	@Setter
	private String textPrefix;
}
