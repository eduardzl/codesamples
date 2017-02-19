package com.verint.textanalytics.web.viewmodel.requestparams;

import com.verint.textanalytics.web.viewmodel.SavedSearch;
import lombok.Getter;
import lombok.Setter;

/***
 * Saved Search actions.
 * @author yzanis
 *
 */
public class SavedSearchActionParams {
	@Getter
	@Setter
	private String channel;

	@Getter
	@Setter
	private SavedSearch savedSearch;

}