package com.verint.textanalytics.model.storedSearch;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
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
