package com.verint.textanalytics.web.viewmodel.requestparams;

import com.verint.textanalytics.web.viewmodel.Category;
import lombok.Getter;
import lombok.Setter;

/**
 * Wrapper for CategoryActionParams request.
 * @author imor
 */
public class CategoryActionParams {

	@Getter
	@Setter
	private String channel;

	@Getter
	@Setter
	private Category category;

}
