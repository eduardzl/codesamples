package com.verint.textanalytics.dal.darwin.vtasyntax.customparsers;

import com.verint.textanalytics.dal.darwin.vtasyntax.customparsers.TokensExtractionResult;
import com.verint.textanalytics.dal.darwin.vtasyntax.errors.ErrorMessages;
import com.verint.textanalytics.dal.darwin.vtasyntax.errors.ProcessingErrorType;
import com.verint.textanalytics.dal.darwin.vtasyntax.errors.VTASyntaxProcessingException;
import com.verint.textanalytics.dal.darwin.vtasyntax.utils.DataUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by EZlotnik on 3/16/2016.
 */
public class TermProximityData {
	private final static String comma = ",";
	private final static String space = " ";
	private final static String invCommasPattern = "\"%s\"";
	private final static String invCommas = "\"";
	private final static String proximityQueryPattern = "W(%s)";

	@Getter
	@Setter
	private String query;

	@Getter
	@Setter
	private String queryForHighlight;

	@Getter
	@Setter
	private List<String>  searchTokens;

	@Getter
	@Setter
	private List<String> highlightTokens;

	/**
	 * Builds data required for term proximity query.
	 * @param terms terms of expression
	 * @param termTokensExtractor term extraction
	 * @param escapeValues should values be extracted
	 * @return data proximity for terms
	 */
	public static TermProximityData build(List<String> terms, TermTokensExtractor termTokensExtractor, boolean escapeValues) {
		TermProximityData termProximityData = new TermProximityData();

		TokensExtractionResult extractedTerms = termTokensExtractor.getTokens(terms);

		if (extractedTerms.getSearchTokens() != null && extractedTerms.getSearchTokens().size() > 0) {

			String phraseForQuery = null;
			String phraseForHighlight = null;

			if (extractedTerms.getSearchTokens().size() > 1) {
				// if there are more then 1 token, create W(term1, term2...) expression
				phraseForQuery = String.format(proximityQueryPattern, extractedTerms.getSearchTokens()
				                                                                    .stream()
				                                                                    .map(t -> DataUtils.escapeCharsForSolrQuery(invCommas)
						                                                                    + (escapeValues ? DataUtils.escapeCharsForSolrQuery(t) : t)
						                                                                    + DataUtils.escapeCharsForSolrQuery(invCommas))
				                                                                    .collect(Collectors.joining(comma)));
			} else {
				phraseForQuery = DataUtils.escapeCharsForSolrQuery(invCommas)
						+  DataUtils.escapeCharsForSolrQuery(extractedTerms.getSearchTokens().get(0))
						+ DataUtils.escapeCharsForSolrQuery(invCommas);
			}

			phraseForHighlight = String.format(invCommasPattern, extractedTerms.getHighlightTokens()
			                                                                   .stream()
			                                                                   .map(t -> escapeValues ? DataUtils.escapeCharsForSolrQuery(t) : t)
			                                                                   .collect(Collectors.joining(DataUtils.escapeCharsForSolrQuery(space))));

			termProximityData.setQuery(phraseForQuery);
			termProximityData.setQueryForHighlight(phraseForHighlight);
			termProximityData.setSearchTokens(extractedTerms.getSearchTokens());
			termProximityData.setHighlightTokens(extractedTerms.getHighlightTokens());
		} else {
			throw new VTASyntaxProcessingException(ProcessingErrorType.SearchIsEmptyOrIncludeStopWordOnly, ErrorMessages.searchIsEmptyOrIncludeStopWordOnly);
		}

		return termProximityData;
	}
}
