package com.verint.textanalytics.dal.darwin.vtasyntax.customparsers;

import com.google.common.base.Throwables;
import com.verint.textanalytics.dal.darwin.vtasyntax.errors.ProcessingErrorType;
import com.verint.textanalytics.dal.darwin.vtasyntax.errors.VTASyntaxProcessingException;
import com.verint.textanalytics.dal.darwin.vtasyntax.errors.VTASyntaxRecognitionException;
import com.verint.textanalytics.dal.darwin.vtasyntax.errors.ErrorMessages;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.de.GermanNormalizationFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by EZlotnik on 3/28/2016.
 */
public class TermParser {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private boolean isInitialized = false;

	private Analyzer wildCardDetectionAnalyzer;

	private final String analyzerTextFieldNameFormat = "text_%s_total";

	private String analyzerTextFieldName = null;

	private final String leadingWildCardErrorMessage =  "'*' or '?' not allowed as first character in WildcardQuery";

	/**
	 * Initialization of Terms parser.
	 * @param language the language for this analyzer instance
	 */
	public void initialize(String language) {
		try {

			logger.debug(String.format("Initializing TermParser with language %s", language));

			this.analyzerTextFieldName = String.format(analyzerTextFieldNameFormat, language);

			// allocate Lucence Analzyer according to language
			switch (language) {
				case "en":
					this.wildCardDetectionAnalyzer = this.getEnAnalyzer();
					break;
				case "deu":
					this.wildCardDetectionAnalyzer = this.getDeAnalyzer();
					break;
				default:
					throw new VTASyntaxProcessingException(ProcessingErrorType.LanguageDetectionError, "Could not find analyzer for givven lanaguage : " + language);

			}



			this.isInitialized = true;

		} catch (Exception ex) {
			logger.error("Failed to initialize Term Parser. Error - {}", ex.toString());

			throw new VTASyntaxProcessingException(ProcessingErrorType.WildCardAnalyzerInitializationFailed, ErrorMessages.vtaSyntaxAnalyzerInitializationFailed, ex);
		}
	}

	private Analyzer getEnAnalyzer() {
		return new Analyzer() {
			@Override
			protected Analyzer.TokenStreamComponents createComponents(String fieldName) {

				Map<String, String> factoryArgs = new HashMap<>();
				StandardTokenizerFactory tokinizerFactory = new StandardTokenizerFactory(factoryArgs);

				Map<String, String> lowerCaseFactoryArgs = new HashMap<>();
				LowerCaseFilterFactory lowerCaseFilterFactory = new LowerCaseFilterFactory(lowerCaseFactoryArgs);

				Tokenizer source = tokinizerFactory.create();
				TokenStream lowerCaseFilter = lowerCaseFilterFactory.create(source);

				return new Analyzer.TokenStreamComponents(source, lowerCaseFilter);
			}
		};
	}

	private Analyzer getDeAnalyzer() {

		return new Analyzer() {
			@Override
			protected Analyzer.TokenStreamComponents createComponents(String fieldName) {

				Map<String, String> factoryArgs = new HashMap<>();
				StandardTokenizerFactory tokinizerFactory = new StandardTokenizerFactory(factoryArgs);

				Map<String, String> lowerCaseFactoryArgs = new HashMap<>();
				LowerCaseFilterFactory lowerCaseFilterFactory = new LowerCaseFilterFactory(lowerCaseFactoryArgs);

				Map<String, String> germanNormalizationFilterFactoryArgs = new HashMap<>();
				GermanNormalizationFilterFactory germanNormalizationFilterFactory = new GermanNormalizationFilterFactory(germanNormalizationFilterFactoryArgs);

				Tokenizer source = tokinizerFactory.create();
				TokenStream lowerCaseFilter = lowerCaseFilterFactory.create(source);
				TokenStream germanNormalizationFilter = germanNormalizationFilterFactory.create(lowerCaseFilter);

				return new Analyzer.TokenStreamComponents(source, germanNormalizationFilter);
			}
		};

	}

	/**
	 * Validates list of terms.
	 * @param term list of terms
	 * @param minPrefixLength minimal prefix length
	 */
	public void validateTermWildCardValidityForSingleTermQuery(String term, int minPrefixLength) {
		CustomQueryParser queryParser = this.getCustomQueryParser();

		this.validateTermWildCardValidityForSingleTerm(term, minPrefixLength, queryParser);
	}

	/**
	 * Validates terms wild card rules for term in phrase/proximity query.
	 * @param terms list of terms to validate
	 */
	public void validateTermWildCardValidatityForPhraseOrProximityQuery(List<String> terms) {
		if (terms != null) {
			CustomQueryParser queryParser = this.getCustomQueryParser();

			for (String term: terms) {
				this.validateTermWildCardValidityForPhraseOfProximity(term, queryParser);
			}
		}
	}


	/**
	 * Validates that if term wild card (prefix, wild card or fuzzy) then it obeys to rules.
	 * @param term term to validate
	 * @param minPrefixLength number of characters to be required in prefix
	 * @param queryParser query parser to check wild card
	 */
	private void validateTermWildCardValidityForSingleTerm(String term, int minPrefixLength, CustomQueryParser queryParser) {
		String text = null;

		try {
			if (term != null && !"".equals(term)) {
				Query query = queryParser.parse(term);
				if (query != null) {

					if (query instanceof PrefixQuery) {
						PrefixQuery prefixQuery = (PrefixQuery) query;
						text = prefixQuery.getPrefix().text();

						if (text.length() < minPrefixLength) {
							throw new VTASyntaxProcessingException(ProcessingErrorType.PrefixLengthInWildCardSearchIsTooShort, String.format(ErrorMessages.prefixLengthInWildCardSearchIsTooShort, term, minPrefixLength));
						}

					} else if (query instanceof WildcardQuery) {
						WildcardQuery wildcardQuery = (WildcardQuery) query;
						text = wildcardQuery.getTerm().text();

						int singleCharacterWildCardIndex = text.indexOf("?", 0);
						int multipleCharacterWildcardIndex = text.indexOf("*", 0);
						int wildCardIndex = -1;

						if (singleCharacterWildCardIndex != -1 && multipleCharacterWildcardIndex != -1) {
							wildCardIndex = Math.min(singleCharacterWildCardIndex, multipleCharacterWildcardIndex);
						} else if (singleCharacterWildCardIndex != -1) {
							wildCardIndex = singleCharacterWildCardIndex;
						} else if (multipleCharacterWildcardIndex != -1) {
							wildCardIndex = multipleCharacterWildcardIndex;
						}

						if (wildCardIndex != -1) {
							if (wildCardIndex < minPrefixLength) {
								throw new VTASyntaxProcessingException(ProcessingErrorType.PrefixLengthInWildCardSearchIsTooShort, String.format(ErrorMessages.prefixLengthInWildCardSearchIsTooShort, term, minPrefixLength));
							}
						}

					} else if (query instanceof FuzzyQuery) {
						FuzzyQuery fuzzyQuery = (FuzzyQuery) query;
						text = fuzzyQuery.getTerm().text();
					}
				}
			}
		} catch (VTASyntaxProcessingException ex1) {
			logger.warn("Parsing validation error for term {}. Error - {}", term, ex1);

			Throwables.propagate(ex1);
		} catch (org.apache.lucene.queryparser.classic.ParseException ex1) {
			logger.info("Parsing validation error for term {}. Error - {}", term, ex1);

			if (ex1.getCause() != null && ex1.getCause().getMessage().equalsIgnoreCase(leadingWildCardErrorMessage)) {
				VTASyntaxProcessingException pex = new VTASyntaxProcessingException(ProcessingErrorType.WildCardPatternIsNotAllowedAsFirstCharacterOfTerm);
				Throwables.propagate(pex);
			} else {
				Throwables.propagate(ex1);
			}
		} catch (Exception ex) {
			logger.error("Exception when parsing {}. Error - {}", term, ex);

			throw new VTASyntaxProcessingException(ProcessingErrorType.TermParsingFailed, String.format(ErrorMessages.termParsingFailed, term), ex);
		}
	}

	/**
	 * Validates that if term wild card (prefix, wild card or fuzzy) then it obeys to rules.
	 * @param term term to validate
	 * @param minPrefixLength number of characters to be required in prefix
	 */
	private void validateTermWildCardValidityForPhraseOfProximity(String term, CustomQueryParser queryParser) {
		String text = null;

		try {
			if (term != null && !"".equals(term)) {
				Query query = queryParser.parse(term);

				if (query != null) {
					if ((query instanceof PrefixQuery) || (query instanceof WildcardQuery) || (query instanceof FuzzyQuery)) {
						throw new VTASyntaxProcessingException(ProcessingErrorType.WildCardIsNotAllowedInPhrasesOrProximityQuery, String.format(ErrorMessages.wildCardIsNotAllowedInPhrasesOrProximityQuery, term));
					}
				}
			}
		} catch (VTASyntaxProcessingException ex1) {
			logger.warn("Phrase or Proximity query validation failed. Wildcard is currently not allowed. Search term - {}. Error - {}", term, ex1);
			Throwables.propagate(ex1);
		} catch (org.apache.lucene.queryparser.classic.ParseException ex1) {
			logger.info("Parsing validation error for term {}. Error - {}", term, ex1);

			if (ex1.getCause() != null && ex1.getCause().getMessage().equalsIgnoreCase(leadingWildCardErrorMessage)) {
				VTASyntaxProcessingException pex = new VTASyntaxProcessingException(ProcessingErrorType.WildCardPatternIsNotAllowedAsFirstCharacterOfTerm);
				Throwables.propagate(pex);
			} else {
				Throwables.propagate(ex1);
			}
		} catch (Exception ex) {
			logger.error("Exception when parsing {}. Error - {}", term, ex);

			throw new VTASyntaxProcessingException(ProcessingErrorType.TermParsingFailed, String.format(ErrorMessages.termParsingFailed, term), ex);
		}
	}

	/**
	 * Check if query is wild card query : inlcudes * or ? patterns
	 * @param term term
	 * @return true if term is wild card query
	 */
	public boolean isWildCardQueryQuery(String term) {
		CustomQueryParser queryParser = null;

		try {
			queryParser = this.getCustomQueryParser();
			Query query = queryParser.parse(term);

			return query != null && (query instanceof  WildcardQuery || query instanceof  PrefixQuery);
		} catch (Exception ex) {
			logger.error("Error parsing term {}. Error {}", term, ex);

			throw new VTASyntaxProcessingException(ProcessingErrorType.TermParsingFailed, String.format(ErrorMessages.termParsingFailed, term), ex);
		}
	}

	/**
	 * Check if term is fuzzy term : includes pattern  term~ .
	 * @param term term
	 * @return true if term is fuzzy pattern
	 */
	public boolean isFuzzyQueryQuery(String term) {
		CustomQueryParser queryParser = null;

		try {
			queryParser = this.getCustomQueryParser();
			Query query = queryParser.parse(term);

			return query != null && query instanceof  FuzzyQuery;
		} catch (Exception ex) {
			throw new VTASyntaxProcessingException(ProcessingErrorType.TermParsingFailed, String.format(ErrorMessages.termParsingFailed, term), ex);
		}
	}


	private CustomQueryParser getCustomQueryParser() {
		// These are lightweight objects and the JVM handles object creation and garbage collection very well.
		// Definitely do not use an object pool.
		if (this.wildCardDetectionAnalyzer != null) {
			CustomQueryParser customQueryParser = new CustomQueryParser(analyzerTextFieldName, this.wildCardDetectionAnalyzer);
			customQueryParser.setAllowLeadingWildcard(false);

			return customQueryParser;
		} else {
			throw new VTASyntaxProcessingException(ProcessingErrorType.WildCardAnalyzerInitializationFailed, ErrorMessages.vtaSyntaxAnalyzerInitializationFailed, null);
		}
	}

	/**
	 * Disponse the analyzer.
	 */
	public void dispose() {
		try {
			if (this.wildCardDetectionAnalyzer != null) {
				IOUtils.close(this.wildCardDetectionAnalyzer);
			}
		} catch (Exception ex) {
			// discard
			logger.error("Failed to close analyzer. Error - {}. {}", ex.getMessage(), ex.getStackTrace());
		}
	}
}
