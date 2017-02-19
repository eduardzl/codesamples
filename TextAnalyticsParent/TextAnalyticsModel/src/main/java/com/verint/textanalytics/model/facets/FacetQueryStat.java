package com.verint.textanalytics.model.facets;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Statistic to calculate for single field.
 * @author EZlotnik
 *
 */
@AllArgsConstructor
public class FacetQueryStat {
	@Getter
	@Setter
	@Accessors(chain = true)
	private String alias;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String field;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String function;

	@Override
	public String toString() {

		return String.format("%s : \"%s(%s)\"", this.getAlias(), this.getFunction(), this.getField());
	}
}
