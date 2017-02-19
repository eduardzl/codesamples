package com.verint.textanalytics.model.modelEditor;

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
	private List<Language> languages;

}
