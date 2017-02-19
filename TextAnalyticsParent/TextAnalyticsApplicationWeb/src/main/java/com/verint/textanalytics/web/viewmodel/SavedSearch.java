package com.verint.textanalytics.web.viewmodel;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/***
 * 
 * @author imor
 *
 */
public class SavedSearch extends StoredSearch {
	@Getter
	@Setter
	@Accessors(chain = true)
	private boolean isPublic;
}
