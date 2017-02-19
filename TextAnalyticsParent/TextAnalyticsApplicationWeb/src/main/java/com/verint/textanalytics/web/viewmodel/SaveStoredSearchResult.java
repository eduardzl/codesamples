package com.verint.textanalytics.web.viewmodel;

import lombok.Getter;
import lombok.Setter;

/**
 * Stored Search result.
 */
public class SaveStoredSearchResult {
	@Getter
	@Setter
	private String lastChangedDateTimeGMT;

	@Getter
	@Setter
	private long lastChangedDateTimeGMTMillis;
}
