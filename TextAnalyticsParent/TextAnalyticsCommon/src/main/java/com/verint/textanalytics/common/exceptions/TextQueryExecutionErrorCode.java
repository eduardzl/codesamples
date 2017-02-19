package com.verint.textanalytics.common.exceptions;

import lombok.Getter;

/**
 * Error code for Solr query generation.
 * 
 * @author EZlotnik
 *
 */
public enum TextQueryExecutionErrorCode implements AppExecutionErrorCode {

	// @formatter:off
	SolrQueryExecutionStatus(-1),
	TextQueryGenerationError(2), 
	TextQueryExecutionError(3), 
	JsonResponseParsingError(4), 
	RESTTextQueryExecutionError(5),
	InteractionSnippetsBuildError(6),
	UtteranceHighlightsBuildError(7),
	UtteranceHighlightsMergeError(8),
	MetricsError(9),
	DailyVolumeError(10),
	EmptyAutoCompletePrefix(11);
	// @formatter:on

	@Getter
	private final int errorCode;

	private TextQueryExecutionErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
}
