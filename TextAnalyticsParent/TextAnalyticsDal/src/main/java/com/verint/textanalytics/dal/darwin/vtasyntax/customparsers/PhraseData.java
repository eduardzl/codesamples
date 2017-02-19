package com.verint.textanalytics.dal.darwin.vtasyntax.customparsers;

import com.verint.textanalytics.dal.darwin.vtasyntax.customparsers.TokensExtractionResult;
import com.verint.textanalytics.dal.darwin.vtasyntax.utils.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by EZlotnik on 3/16/2016.
 */
public class PhraseData {
	private final static String comma = ",";
	private final static String space = " ";
	private final static String invCommasPattern = "\"%s\"";
	private final static String wildCardPattern = "___wildcard_%s___";

	@Getter
	@Setter
	private String phraseQuery;

	@Getter
	@Setter
	private String phraseForHighlight;

	@Getter
	@Setter
	private List<String> searchTokens;

	@Getter
	@Setter
	private List<String> highlightTokens;


	/**
	 * Builder for Generates Phrase data.
	 * @param phrase phrase
	 * @param escapedPhrase escaped phrase
	 * @param phraseTerms terms of search
	 * @param termTokensExtractor extracting search tokens and highlight
	 * @param escapeValues should values be escaped
	 * @return generated phrase data
	 */
	public static PhraseData build(String phrase, String escapedPhrase, List<String> phraseTerms, boolean escapeValues, TermTokensExtractor termTokensExtractor) {
		PhraseData phraseData = new PhraseData();

		if (escapeValues) {
			phraseData.setPhraseQuery(String.format(invCommasPattern, escapedPhrase));
		} else {
			phraseData.setPhraseQuery(String.format(invCommasPattern, phrase));
		}

		TokensExtractionResult extractedTerms = termTokensExtractor.getTokens(phraseTerms);
		if (extractedTerms != null) {

			if (extractedTerms.getHighlightTokens() != null) {
				// @formatter:off
				if (escapeValues) {
					phraseData.setPhraseForHighlight(String.format(invCommasPattern, extractedTerms.getHighlightTokens()
					                                                                               .stream()
					                                                                               .map(t -> DataUtils.escapeCharsForSolrQuery(t))
					                                                                               .collect(Collectors.joining(DataUtils.escapeCharsForSolrQuery(space)))));
				} else {
					phraseData.setPhraseForHighlight(String.format(invCommasPattern, extractedTerms.getHighlightTokens()
					                                                                               .stream()
					                                                                               .map(t -> t)
					                                                                               .collect(Collectors.joining(DataUtils.escapeCharsForSolrQuery(space)))));
				}
				//@formatter:on
			}

			phraseData.setSearchTokens(extractedTerms.getSearchTokens());
			phraseData.setHighlightTokens(extractedTerms.getHighlightTokens());
		}

		return phraseData;
	}

	/**
	 * Builder for Generates Phrase data.
	 * @param phrase phrase
	 * @param escapedPhrase escaped phrase
	 * @param phraseTerms terms of search
	 * @param escapeValues should values be escaped
	 * @param termTokensExtractor terms token extractor
	 * @param termParser parser to check if term is wild card term
	 * @return generated phrase data
	 */
	public static PhraseData buildComplexQueryData(String phrase, String escapedPhrase, List<String> phraseTerms, boolean escapeValues, TermTokensExtractor termTokensExtractor, TermParser termParser) {
		PhraseData phraseData = new PhraseData();

		if (!CollectionUtils.isEmpty(phraseTerms)) {

			int i = 0;
			String termWithwildCardPattern = null;
			List<String> termsForAnalysis = new ArrayList<>();
			Map<String, String> wildCardTermsMap = new HashMap<>();

			for (String term: phraseTerms) {

				// if term is wild card term then replace it with pattern to allow stop words processing
				if (termParser.isWildCardQueryQuery(term)) {
					termWithwildCardPattern = String.format(wildCardPattern, i);
					termsForAnalysis.add(termWithwildCardPattern);
					wildCardTermsMap.put(termWithwildCardPattern, term);
					i++;
				} else if (termParser.isFuzzyQueryQuery(term)) {
					// if term is fuzzy term then replace it with pattern to allow stop words processing
					termWithwildCardPattern = String.format(wildCardPattern, i);
					termsForAnalysis.add(termWithwildCardPattern);
					wildCardTermsMap.put(termWithwildCardPattern, term);
					i++;
				} else {
					termsForAnalysis.add(term);
				}
			}

			TokensExtractionResult tokensResult = termTokensExtractor.getTokens(termsForAnalysis);
			if (tokensResult != null && tokensResult.getSearchTokens() != null) {

				boolean wildCardPatternFound = false;
				List<String> phraseTokens = new ArrayList<>();
				for (String token : tokensResult.getSearchTokens()) {

					for (Map.Entry<String, String> wildCardEntry : wildCardTermsMap.entrySet()) {
						// if token has the wild card pattern
						// then replace it back to original pattern
						if (token.indexOf(wildCardEntry.getKey()) >= 0) {
							wildCardPatternFound = true;

							token = token.replaceFirst(wildCardEntry.getKey(), wildCardEntry.getValue());
							phraseTokens.add(token);
						}
					}

					// the term doesn't include wild card pattern
					if (!wildCardPatternFound) {
						phraseTokens.add(token);
					}
				}

				phraseData.setSearchTokens(phraseTokens);

				// @formatter:off
				phraseData.setPhraseQuery(phraseTokens.stream()
				                                       .map(t -> escapeValues ? DataUtils.escapeCharsForSolrQuery(t) : t)
				                                       .collect(Collectors.joining(space))
				);

				phraseData.setPhraseForHighlight(String.format(invCommasPattern, tokensResult.getHighlightTokens()
					                                                                               .stream()
					                                                                               .map(t -> escapeValues ?  DataUtils.escapeCharsForSolrQuery(t) : t)
					                                                                               .collect(Collectors.joining(DataUtils.escapeCharsForSolrQuery(space)))));
				//@formatter:on
			}
		}

		return phraseData;
	}
}
