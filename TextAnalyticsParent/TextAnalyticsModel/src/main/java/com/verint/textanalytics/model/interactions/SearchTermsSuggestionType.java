package com.verint.textanalytics.model.interactions;

import lombok.Getter;

/**
 * Suggestions type.
 * @author EZlotnik
 *
 */
public enum SearchTermsSuggestionType {
	//@formatter:off
	FacetOnTerms(0),
    SolrFreeTextSuggester(1);
	//@formatter:on

	@Getter
	private int suggestionsType;

	SearchTermsSuggestionType(int suggestionsType) {
		this.suggestionsType = suggestionsType;
	}
}
