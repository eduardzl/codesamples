package com.verint.textanalytics.dal.darwin.vtasyntax.customparsers;

import java.util.List;

/**
 * Created by EZlotnik on 3/10/2016.
 */
public class TokensExtractionResult {
	private List<String> searchTokens;

	private List<String> highlightTokens;

	/**
	 * Empty C'tor.
	 */
	public TokensExtractionResult() {

	}

	/**
	 * Constructor.
	 * @param searchTokens search tokens
	 * @param highlightTokens highlight tokens
	 */
	public TokensExtractionResult(List<String> searchTokens, List<String> highlightTokens) {
		this.searchTokens = searchTokens;
		this.highlightTokens = highlightTokens;
	}

	public List<String> getSearchTokens() {
		return this.searchTokens;
	}

	public void setSearchTokens(List<String> searchTokens) {
		this.searchTokens = searchTokens;
	}

	public List<String> getHighlightTokens() {
		return  this.highlightTokens;
	}

	public void setHighlightTokens(List<String> highlightTokens) {
		this.highlightTokens = highlightTokens;
	}
}
