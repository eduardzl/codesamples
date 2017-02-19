package com.verint.textanalytics.web.viewmodel;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author imor
 *
 */
public class ModelsTree {

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<Model> models;
}
