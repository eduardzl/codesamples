package com.verint.textanalytics.dal.darwin.vtasyntax;


import com.google.common.base.Throwables;
import com.verint.textanalytics.dal.darwin.vtasyntax.customparsers.TermParser;
import com.verint.textanalytics.dal.darwin.vtasyntax.customparsers.TermTokensExtractor;
import com.verint.textanalytics.dal.darwin.vtasyntax.errors.UnderlineErrorListener;
import com.verint.textanalytics.dal.darwin.vtasyntax.errors.VTASyntaxProcessingException;
import com.verint.textanalytics.dal.darwin.vtasyntax.errors.VTASyntaxRecognitionError;
import com.verint.textanalytics.dal.darwin.vtasyntax.errors.VTASyntaxRecognitionException;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.lucene.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Analyzer responsible for generating query from Verint query search string.
 *
 * @author EZlotnik
 *
 */
public class VTASyntaxAnalyzer  {
	private Logger logger = LoggerFactory.getLogger(this.getClass());


	// map that holds TermParser by language
	private Map<String, TermParser> termParserMap;

	// map that holds termTokenExtractor by language
	private Map<String, TermTokensExtractor> termTokensExtractorMap;

	/**
	 * Costructor.
	 */
	public VTASyntaxAnalyzer() {

		this.termTokensExtractorMap = new HashMap<>();

		this.termParserMap = new HashMap<>();


	}

	private TermTokensExtractor getTermTokenExtractor(String language) {

		if (!this.termTokensExtractorMap.containsKey(language)) {
			synchronized (this.termTokensExtractorMap) {
				if (!this.termTokensExtractorMap.containsKey(language)) {
					TermTokensExtractor termTokensExtractor = new TermTokensExtractor();
					termTokensExtractor.initialize(language);

					this.termTokensExtractorMap.put(language, termTokensExtractor);
				}
			}
		}

		return this.termTokensExtractorMap.get(language);
	}

	private TermParser getTermParser(String language) {

		if (!this.termParserMap.containsKey(language)) {
			synchronized (this.termParserMap) {
				if (!this.termParserMap.containsKey(language)) {
					TermParser termParser = new TermParser();
					termParser.initialize(language);

					this.termParserMap.put(language, termParser);
				}
			}
		}

		return this.termParserMap.get(language);
	}


	/**
	 * Analyze query in VTA syntax.
	 *
	 * @param vtaSyntaxQuery                  query
	 * @param tasQueryGenerationConfiguration query generation configuration.
	 * @return query parsing result
	 */
	public VTASyntaxParsingResult parseQuery(String vtaSyntaxQuery, TASQueryConfiguration tasQueryGenerationConfiguration)  {
		VTASyntaxParsingResult parsingResult = new VTASyntaxParsingResult();
		ByteArrayInputStream inputStream = null;

		if (vtaSyntaxQuery != null && !"".equals(vtaSyntaxQuery)) {

			try {
				// load a search terms query into Antlr
				ANTLRInputStream antlrInputStream = new ANTLRInputStream(vtaSyntaxQuery);
				VTASyntaxLexer lexer = new VTASyntaxLexer(antlrInputStream);
				TermTokensExtractor termTokensExtractor = this.getTermTokenExtractor(tasQueryGenerationConfiguration.getLanguage());

				CommonTokenStream tokens = new CommonTokenStream(lexer);
				VTASyntaxParser parser = new VTASyntaxParser(tokens);

				// remove ConsoleErrorListener and add custom error listener
				UnderlineErrorListener errorListener =  new UnderlineErrorListener();
				errorListener.setReportAmbiquityErrors(false);
				errorListener.setReportContextSensitivityErrors(false);
				errorListener.setReportFullContextErrors(false);
				parser.removeErrorListeners();
				parser.addErrorListener(errorListener);

				// parse the tree
				VTASyntaxParser.VtaexprContext vtaExprContext = parser.vtaexpr();

				// Walk it and attach our listener
				ParseTreeWalker walker = new ParseTreeWalker();
				VTASyntaxToSolrSyntaxListener listener = new VTASyntaxToSolrSyntaxListener();
				listener.setQueryGenerationConfiguration(tasQueryGenerationConfiguration);
				listener.setTermTokensExtractor(termTokensExtractor);
				listener.setTermParser(this.getTermParser(tasQueryGenerationConfiguration.getLanguage()));

				walker.walk(listener, vtaExprContext);
				List<VTASyntaxRecognitionError> errors = errorListener.getErrors();
				if (errors != null && errors.size() > 0) {
					// recognition exceptions are pushed into wrapper exception
					throw new VTASyntaxRecognitionException(errors);
				} else {

					// get first significant child of vtaexpr node
					Pair<VTASyntaxToSolrSyntaxListener.ExprType, String> vtaExprChildData =  listener.getValue(vtaExprContext.exprOr());
					if (vtaExprChildData != null) {
						switch (vtaExprChildData.a) {
							case ExprBasicNot:
							case TermNot:
							case ExprWithNotOnly:
								// child is NOT expression
								parsingResult.setIsNOTExpression(true);
								break;
							default:
								// child is not NOT expression
								parsingResult.setIsNOTExpression(false);
								break;
						}
					}

					String solrQuery = listener.getValue(vtaExprContext).b;

					// @formatter:off
					parsingResult.setSolrQuery(solrQuery)
					             .setTerms(listener.getTerms());
					// @formatter:on
				}
			} catch (VTASyntaxRecognitionException parsingEx) {
				// Parsing exception should be propogated as Parsing error
				Throwables.propagate(parsingEx);
			} catch (VTASyntaxProcessingException processEx) {
				Throwables.propagate(processEx);
			} catch (Exception ex) {
				Throwables.propagate(ex);
			} finally {
				try {
					IOUtils.close(inputStream);
				} catch (Exception ex) {
					logger.error("Failed to close ANTLR input stream.", ex);
				}
			}
		}

		return parsingResult;
	}




	/**
	 * Dispose all resources used by VTASyntax Analyzer.
	 */
	public void dispose() {

		if (this.termTokensExtractorMap != null) {
			this.termTokensExtractorMap.forEach((key, value) -> {
				value.dispose();
			});
		}

		if (this.termParserMap != null) {
			this.termParserMap.forEach((key, value) -> {
				value.dispose();
			});
		}
	}
}

