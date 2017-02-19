package com.verint.textanalytics.model.interactions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 
 * Represents an interaction's Category.
 * 
 * @author imor
 *
 */
@AllArgsConstructor
@NoArgsConstructor
public class CategoryTagging {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String id;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String name;
}
