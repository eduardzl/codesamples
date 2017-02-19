package com.verint.textanalytics.dal.darwin.vtasyntax.errors;

/**
 * Created by EZlotnik on 3/9/2016.
 */
public final class ErrorMessages {


	public final static String stopWordsFilterInitializationFailed = "Failed to initialize StopWords filter. Stop words file path used - %s";
	public final static String vtaSyntaxAnalyzerInitializationFailed = "Failed to initialize VTA Syntax Lucene analyzer.";
	public final static String termTokensExtractionFailed = "Failed to extract search tokens from term %s.";
	public final static String termParsingFailed = "Failed to parse term %s";

	public final static String syntaxParsingFailed = "Exception occured when parsing expression in VTA syntax";
	public final static String prefixLengthInWildCardSearchIsTooShort = "Prefix length in wild card search '%s' is too short. Must be at least %s characters length";
	public final static String wildCardIsNotAllowedInPhrasesOrProximityQuery = "Wildcard or Fuzzy search is currently not allowed in phrase or proximity queries. Term %s";
	public final static String searchIsEmptyOrIncludeStopWordOnly = "Search is empty or include stop words only.";

	/**
	 * Private constructor.
	 */
	private ErrorMessages() {

	}
}
