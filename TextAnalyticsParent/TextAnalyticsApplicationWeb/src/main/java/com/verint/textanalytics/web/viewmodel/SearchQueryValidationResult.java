package com.verint.textanalytics.web.viewmodel;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by EZlotnik on 3/28/2016.
 */
public class SearchQueryValidationResult {
	@Setter
	private boolean isValid;

	@Setter
	private String errorKey;
}
