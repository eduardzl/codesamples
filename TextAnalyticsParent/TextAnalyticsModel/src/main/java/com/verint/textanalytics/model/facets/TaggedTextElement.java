package com.verint.textanalytics.model.facets;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Created by EZlotnik on 2/4/2016.
 */
@Accessors(chain = true)
@AllArgsConstructor
public class TaggedTextElement {

	@Getter
	@Setter
	private TextElementType type;

	@Getter
	@Setter
	private String value;
}
