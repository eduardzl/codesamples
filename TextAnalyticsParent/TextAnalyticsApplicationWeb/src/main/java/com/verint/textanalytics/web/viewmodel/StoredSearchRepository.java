package com.verint.textanalytics.web.viewmodel;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Access layer to Stored Searches.
 * @author imor
 *
 */
public class StoredSearchRepository {

	@Getter
	@Setter
	@Accessors(chain = true)
	private int maxId;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String timeStamp;

}
