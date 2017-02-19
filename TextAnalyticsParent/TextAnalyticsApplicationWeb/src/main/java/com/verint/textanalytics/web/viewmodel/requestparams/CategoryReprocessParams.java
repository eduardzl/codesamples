package com.verint.textanalytics.web.viewmodel.requestparams;

import com.verint.textanalytics.web.viewmodel.Category;
import lombok.Getter;
import lombok.Setter;

/**
 * Wrapper for Category Reprocesing request.
 * @author imor
 *
 */
public class CategoryReprocessParams {

	@Getter
	@Setter
	private String channel;

	@Getter
	@Setter
	private int categoryId;
}
