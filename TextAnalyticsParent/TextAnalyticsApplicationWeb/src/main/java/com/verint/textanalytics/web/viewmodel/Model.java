package com.verint.textanalytics.web.viewmodel;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author imor
 *
 */
public class Model {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String language;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String domain;

}
