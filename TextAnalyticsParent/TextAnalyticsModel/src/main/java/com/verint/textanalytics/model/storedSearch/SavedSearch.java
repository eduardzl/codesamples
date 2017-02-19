package com.verint.textanalytics.model.storedSearch;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/***
 * 
 * @author yzanis
 *
 */
public class SavedSearch extends StoredSearch {
	@Getter
	@Setter
	@Accessors(chain = true)
	private boolean isPublic;
}
