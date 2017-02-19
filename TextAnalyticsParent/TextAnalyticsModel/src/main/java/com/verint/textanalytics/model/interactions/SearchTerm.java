package com.verint.textanalytics.model.interactions;

import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.utils.DataUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Search term in Verint syntax.
 * 
 * @author EZlotnik
 *
 */
@AllArgsConstructor
@NoArgsConstructor
public class SearchTerm implements Comparable<SearchTerm> {

	@Setter
	@Getter
	@Accessors(chain = true)
	private String term;

	@Setter
	@Getter
	@Accessors(chain = true)
	private TermType termType;

	@Setter
	@Getter
	@Accessors(chain = true)
	private SpeakerType speakerType;

	/**
	 * Term type in Search Term.
	 * 
	 * @author EZlotnik
	 *
	 */
	public enum TermType {
		// @formatter:off
		Word(0), 
		Phrase(1);
		// @formatter:on

		private int termType;

		private TermType(int termType) {
			this.termType = termType;
		}
	}

	/**
	 * Escapes term to be used in Solr query.
	 * @return escaped term
	 */
	public String getEscapedTermForQuery() {
		String escapedTerm;

		// encode special characters
		// phrase already is quoted, thus causing search
		// of whole phrase
		switch (this.termType) {
			case Word:
				escapedTerm = DataUtils.escapeCharsForSolrQuery(this.term);
				break;
			case Phrase:
				escapedTerm = DataUtils.escapeCharsForSolrQuery(this.term);
				break;
			default:
				escapedTerm = DataUtils.escapeCharsForSolrQuery(this.term);
				break;
		}

		return escapedTerm;
	}

	/**
	 * Query on escaped term with speaker.
	 * @param language                      language
	 * @param queryInteractions the query should filter Interactions or Utterances
	 * @return term and speaker query
	 */
	public String getEscapedTermWithSpeakerForQuery(String language, Boolean queryInteractions) {
		String termWithSpeaker = "";

		switch (this.speakerType) {
			case Agent:
				if (queryInteractions) {
					termWithSpeaker = String.format("text_%s_%s:%s", language, TAConstants.SpeakerTypeValues.Agent, this.getEscapedTermForQuery());
				} else {
					termWithSpeaker = String.format("text_%s:%s AND %s:%s", language, this.getEscapedTermForQuery(), TAConstants.SchemaFieldNames.speakerType, TAConstants.SpeakerTypeValues.Agent);
				}
				break;
			case Customer:
				if (queryInteractions) {
					termWithSpeaker = String.format("text_%s_%s:%s", language, TAConstants.SpeakerTypeValues.Customer, this.getEscapedTermForQuery());
				} else {
					termWithSpeaker = String.format("text_%s:%s AND %s:%s", language, this.getEscapedTermForQuery(), TAConstants.SchemaFieldNames.speakerType, TAConstants.SpeakerTypeValues.Customer);
				}
				break;
			case Unknown:
				if (queryInteractions) {
					termWithSpeaker = String.format("text_%s_total:%s", language, this.getEscapedTermForQuery());
				} else {
					termWithSpeaker = String.format("text_%s:%s", language, this.getEscapedTermForQuery());
				}
				break;
			default:
				if (queryInteractions) {
					termWithSpeaker = String.format("text_%s_total:%s", language, this.getEscapedTermForQuery());
				} else {
					termWithSpeaker = String.format("text_%s:%s", language, this.getEscapedTermForQuery());
				}
				break;
		}

		return termWithSpeaker;
	}

	/**
	 * Escapes term for usage in highlighting.
	 * @return escaped term.
	 */
	public String getEscapedTermForHighlight() {
		String escapedTerm = "";
		// encode special characters
		// phrase already is quoted, thus causing search
		// of whole phrase
		switch (this.termType) {
			case Word:
				escapedTerm = DataUtils.escapeCharsForSolrQuery(this.term);
				break;
			case Phrase:
				escapedTerm = this.term;
				break;
			default:
				escapedTerm = DataUtils.escapeCharsForSolrQuery(this.term);
				break;
		}

		return escapedTerm;
	}

	/**
	 * Generates term query with text field.
	 * @param language
	 *            language
	 * @return term query
	 */
	public String getEscapedTermWithTextField(String language) {
		return String.format("text_%s:%s", language, this.getEscapedTermForQuery());
	}

	@Override
	public int compareTo(SearchTerm o) {
		if (o != null) {
			if (o.getTerm().equals(this.getTerm()) && o.getTermType() == this.getTermType() && o.getSpeakerType() == this.getSpeakerType()) {
				return 0;
			}
		}

		return 1;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (getClass() != obj.getClass()) {
			return false;
		}

		final SearchTerm objTerm = (SearchTerm) obj;

		return this.compareTo(objTerm) == 0;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		final int hashPrefix = 53;

		hash = hashPrefix * hash + (this.term != null ? this.term.hashCode() : 0);
		hash = hashPrefix * hash + this.termType.hashCode();
		hash = hashPrefix * hash + this.speakerType.hashCode();
		return hash;
	}
}
