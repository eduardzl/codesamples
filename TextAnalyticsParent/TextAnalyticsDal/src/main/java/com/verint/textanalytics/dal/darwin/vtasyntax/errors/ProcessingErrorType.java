package com.verint.textanalytics.dal.darwin.vtasyntax.errors;

/**
 * Created by EZlotnik on 3/20/2016.
 */
public enum ProcessingErrorType {

	// @formatter:off
	StopWordsFilterInitializationFailed(0),
	VTASyntaxAnalyzerInitializationFailed(1),
	WildCardAnalyzerInitializationFailed(2),
	TermTokensExtractionFailed(3),
	TermParsingFailed(4),
	PrefixLengthInWildCardSearchIsTooShort(5),
	WildCardIsNotAllowedInPhrasesOrProximityQuery(6),
	SearchIsEmptyOrIncludeStopWordOnly(7),
	WildCardPatternIsNotAllowedAsFirstCharacterOfTerm(8),
	LanguageDetectionError(9);

	// @formatter:on

	private int errorType;

	private ProcessingErrorType(int value) {
		this.errorType = value;
	}
}
