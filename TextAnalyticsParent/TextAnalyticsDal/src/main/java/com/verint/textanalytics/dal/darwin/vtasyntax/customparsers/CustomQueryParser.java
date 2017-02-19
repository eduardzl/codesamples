package com.verint.textanalytics.dal.darwin.vtasyntax.customparsers;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.*;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.IOUtils;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by EZlotnik on 3/27/2016.
 */
class CustomQueryParser extends QueryParser {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	/**
	 * Constructor.
	 * @param field field to be analyzed
	 * @param analyzer analyzer
	 */
	public CustomQueryParser(String field, Analyzer analyzer) {
		super(field, analyzer);
	}

	@Override
	protected final Query getWildcardQuery(String field, String termStr) throws ParseException {
		return super.getWildcardQuery(field, termStr);
	}

	@Override
	protected final Query getFuzzyQuery(String field, String term, float minSimilarity) throws ParseException {
		return super.getFuzzyQuery(field, term, minSimilarity);
	}

	@Override
	protected final Query getPrefixQuery(String field, String termStr) throws ParseException {
		return super.getPrefixQuery(field, termStr);
	}
}
