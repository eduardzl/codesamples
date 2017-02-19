package com.verint.textanalytics.model.interactions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Represents an utterance's Topic.
 * 
 * @author imor
 *
 */
@AllArgsConstructor
@NoArgsConstructor
public class Position {

	@Getter
	@Setter
	@Accessors(chain = true)
	protected int starts;

	@Getter
	@Setter
	@Accessors(chain = true)
	protected int ends;
}
