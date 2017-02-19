package com.verint.textanalytics.dal.darwin.vtasyntax;

import com.verint.textanalytics.common.utils.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by EZlotnik on 2/29/2016.
 */
@AllArgsConstructor
public class QueryTerm {
	@Getter
	@Setter
	private String term;

	@Getter
	@Setter
	private TermType type;

	@Getter
	@Setter
	private SpeakerType speakerType;

	@Getter
	@Setter
	private String termForQuery;

	@Getter
	@Setter
	private List<String> searchTokens;

	/**
	 * Terms for highlight.
	 * @return terms for highlight
	 */
	public String getHighlightQuery() {
		String termsForHighlight = "";

		switch (this.type) {
			case Phrase:
				if (!CollectionUtils.isEmpty(this.searchTokens)) {
					termsForHighlight = String.format("\"%s\"", this.searchTokens.stream().collect(Collectors.joining(
							com.verint.textanalytics.dal.darwin.vtasyntax.utils.DataUtils.escapeCharsForSolrQuery(" "))));
				}
				break;
			case Word:
				termsForHighlight = com.verint.textanalytics.dal.darwin.vtasyntax.utils.DataUtils.escapeCharsForSolrQuery(this.searchTokens.get(0));
				break;
			default:
				break;
		}

		return termsForHighlight;
	}
}
