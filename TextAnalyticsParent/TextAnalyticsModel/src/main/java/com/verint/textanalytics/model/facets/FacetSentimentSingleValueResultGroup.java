package com.verint.textanalytics.model.facets;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * FacetSentimentSingleValueResultGroup.
 * 
 * @author Yhemi
 *
 */
public class FacetSentimentSingleValueResultGroup extends FacetResultGroup {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String value;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int sentimentValue;
}
