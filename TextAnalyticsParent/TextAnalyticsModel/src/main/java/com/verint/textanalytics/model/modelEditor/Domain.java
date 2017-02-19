package com.verint.textanalytics.model.modelEditor;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author imor
 *
 */
public class Domain {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String name;
}
