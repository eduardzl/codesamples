package com.verint.textanalytics.dal.darwin.vtasyntax;

import com.verint.textanalytics.dal.darwin.vtasyntax.errors.VTASyntaxRecognitionError;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Created by EZlotnik on 2/28/2016.
 */
public class VTASyntaxParsingResult {

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<QueryTerm> terms;

	@Setter
	@Getter
	@Accessors(chain = true)
	private String solrQuery;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<VTASyntaxRecognitionError> errors;

	@Getter
	@Setter
	@Accessors(chain = true)
	private Boolean isNOTExpression;

	/**
	 * Constructor.
	 */
	public VTASyntaxParsingResult() {

	}

	/**
	 * Get total count of terms in query.
	 * @return total count of terms
	 */
	public int getTermsCount() {
		return this.terms.size();
	}
}
