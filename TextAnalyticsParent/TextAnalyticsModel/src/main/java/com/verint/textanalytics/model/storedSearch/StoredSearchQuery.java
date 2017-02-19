package com.verint.textanalytics.model.storedSearch;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by EZlotnik on 3/31/2016.
 */
@NoArgsConstructor
@AllArgsConstructor
public class StoredSearchQuery {
	@Getter
	@Setter
	private String query;

	@Getter
	@Setter
	private String debugQuery;
}
