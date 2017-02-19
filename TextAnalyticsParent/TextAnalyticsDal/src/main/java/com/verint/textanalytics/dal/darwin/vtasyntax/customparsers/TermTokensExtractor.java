package com.verint.textanalytics.dal.darwin.vtasyntax.customparsers;

import com.google.common.base.Throwables;
import com.verint.textanalytics.common.utils.CollectionUtils;
import com.verint.textanalytics.dal.darwin.vtasyntax.errors.ErrorMessages;
import com.verint.textanalytics.dal.darwin.vtasyntax.errors.ProcessingErrorType;
import com.verint.textanalytics.dal.darwin.vtasyntax.errors.VTASyntaxProcessingException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.commongrams.CommonGramsFilterFactory;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.de.GermanNormalizationFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.analysis.tokenattributes.*;
import org.apache.lucene.analysis.util.ClasspathResourceLoader;
import org.apache.lucene.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedHashMap;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by EZlotnik on 3/9/2016.
 */
public class TermTokensExtractor {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private Analyzer analyzer;
	private Boolean isInitialized = false;

	private final String stopWordsIgnoreCase = "true";
	private final String analyzerTextFieldNameFormat = "text_%s_total";

	private String analyzerTextFieldName = null;

	private final String tokenTypeAlpahNum = "<ALPHANUM>";
	private final String tokenTypeGram = "gram";
	private final String space = " ";
	private final String words = "words";
	private final String ignoreCase = "ignoreCase";


	/**
	 * Constructor.
	 */
	public TermTokensExtractor() {
		logger.debug("TermTokensExtractor C'tor");
	}

	/**
	 * Initialize Lucene Analyzer with lower casing, stop words concatenations and stop words removal.
	 * @param language the language for this analyzer instance
	 */
	public void initialize(String language) {

		try {

			logger.debug(String.format("Initializing TermTokensExtractor with language %s", language));

			this.analyzerTextFieldName = String.format(analyzerTextFieldNameFormat, language);

			// allocate Lucence Analzyer according to language
			switch (language) {
				case "en":
					this.analyzer = this.getEnAnalyzer();
					break;
				case "deu":
					this.analyzer = this.getDeAnalyzer();
					break;
				default:
					throw new VTASyntaxProcessingException(ProcessingErrorType.LanguageDetectionError, "Could not find analyzer for givven lanaguage : " + language);

			}


			this.isInitialized = true;

		} catch (VTASyntaxProcessingException ex) {
			throw ex;
		} catch (Exception ex) {
			logger.error("Failed to initialize TermTokensExtractor. Error - {}. {}", ex.getMessage(), ex.getStackTrace());

			throw new VTASyntaxProcessingException(ProcessingErrorType.VTASyntaxAnalyzerInitializationFailed, ErrorMessages.vtaSyntaxAnalyzerInitializationFailed, ex);
		}
	}

	private Analyzer getEnAnalyzer() {
		return new Analyzer() {
			@Override
			protected TokenStreamComponents createComponents(String fieldName) {

				String stopWordsPath = "lang/stopwords_en.txt";

				Map<String, String> factoryArgs = new HashMap<>();
				StandardTokenizerFactory tokinizerFactory = new StandardTokenizerFactory(factoryArgs);

				Map<String, String> lowerCaseFactoryArgs = new HashMap<>();
				LowerCaseFilterFactory lowerCaseFilterFactory = new LowerCaseFilterFactory(lowerCaseFactoryArgs);

				CommonGramsFilterFactory commonGramsFilterFactory = null;
				try {
					logger.debug("Creating CommonGramsFilterFactory with stop words file at {}", stopWordsPath);

					Map<String, String> commonTermsFactoryArgs = new HashMap<>();
					commonTermsFactoryArgs.put(words, stopWordsPath);
					commonTermsFactoryArgs.put(ignoreCase, stopWordsIgnoreCase);
					commonGramsFilterFactory = new CommonGramsFilterFactory(commonTermsFactoryArgs);
					ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader();
					commonGramsFilterFactory.inform(resourceLoader);
				} catch (Exception ex) {
					// failed to read stop words resource
					logger.error("Failed to initialize CommonGramsFilterFactory. Error - {}. {}", ex.getMessage(), ex.getStackTrace());

					throw new VTASyntaxProcessingException(ProcessingErrorType.StopWordsFilterInitializationFailed, String.format(ErrorMessages.stopWordsFilterInitializationFailed, stopWordsPath), ex);
				}

				StopFilterFactory stopWordsFilterFactory = null;
				try {
					Map<String, String> stopWordsFilterFactoryArgs = new HashMap<>();
					stopWordsFilterFactoryArgs.put(words, stopWordsPath);
					stopWordsFilterFactoryArgs.put(ignoreCase, stopWordsIgnoreCase);
					stopWordsFilterFactory = new StopFilterFactory(stopWordsFilterFactoryArgs);
					ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader();
					stopWordsFilterFactory.inform(resourceLoader);
				} catch (Exception ex) {
					// failed to read stop words resource
					logger.error("Failed to initialize StopFilterFactory. Error - {}. {}", ex.getMessage(), ex.getStackTrace());

					throw new VTASyntaxProcessingException(ProcessingErrorType.StopWordsFilterInitializationFailed, String.format(ErrorMessages.stopWordsFilterInitializationFailed, stopWordsPath), ex);
				}

				Tokenizer source = tokinizerFactory.create();
				TokenStream lowerCaseFilter = lowerCaseFilterFactory.create(source);
				TokenStream commonTermsFilter = commonGramsFilterFactory.create(lowerCaseFilter);
				TokenStream stopWordsFilter = stopWordsFilterFactory.create(commonTermsFilter);

				return new Analyzer.TokenStreamComponents(source, stopWordsFilter);
			}
		};
	}

	// German analyzer
	private Analyzer getDeAnalyzer() {
		return new Analyzer() {
			@Override
			protected TokenStreamComponents createComponents(String fieldName) {

				String stopWordsPath = "lang/stopwords_de.txt";

				Map<String, String> factoryArgs = new HashMap<>();
				StandardTokenizerFactory tokinizerFactory = new StandardTokenizerFactory(factoryArgs);

				Map<String, String> lowerCaseFactoryArgs = new HashMap<>();
				LowerCaseFilterFactory lowerCaseFilterFactory = new LowerCaseFilterFactory(lowerCaseFactoryArgs);

				CommonGramsFilterFactory commonGramsFilterFactory = null;
				try {
					logger.debug("Creating CommonGramsFilterFactory with stop words file at {}", stopWordsPath);

					Map<String, String> commonTermsFactoryArgs = new HashMap<>();
					commonTermsFactoryArgs.put(words, stopWordsPath);
					commonTermsFactoryArgs.put(ignoreCase, stopWordsIgnoreCase);
					commonTermsFactoryArgs.put("format", "snowball");
					commonGramsFilterFactory = new CommonGramsFilterFactory(commonTermsFactoryArgs);
					ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader();
					commonGramsFilterFactory.inform(resourceLoader);
				} catch (Exception ex) {
					// failed to read stop words resource
					logger.error("Failed to initialize CommonGramsFilterFactory. Error - {}. {}", ex.getMessage(), ex.getStackTrace());

					throw new VTASyntaxProcessingException(ProcessingErrorType.StopWordsFilterInitializationFailed, String.format(ErrorMessages.stopWordsFilterInitializationFailed, stopWordsPath), ex);
				}

				StopFilterFactory stopWordsFilterFactory = null;
				try {
					Map<String, String> stopWordsFilterFactoryArgs = new HashMap<>();
					stopWordsFilterFactoryArgs.put(words, stopWordsPath);
					stopWordsFilterFactoryArgs.put(ignoreCase, stopWordsIgnoreCase);
					stopWordsFilterFactoryArgs.put("format", "snowball");
					stopWordsFilterFactory = new StopFilterFactory(stopWordsFilterFactoryArgs);
					ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader();
					stopWordsFilterFactory.inform(resourceLoader);
				} catch (Exception ex) {
					// failed to read stop words resource
					logger.error("Failed to initialize StopFilterFactory. Error - {}. {}", ex.getMessage(), ex.getStackTrace());

					throw new VTASyntaxProcessingException(ProcessingErrorType.StopWordsFilterInitializationFailed, String.format(ErrorMessages.stopWordsFilterInitializationFailed, stopWordsPath), ex);
				}


				GermanNormalizationFilterFactory germanNormalizationFilterFactory = new GermanNormalizationFilterFactory(new HashMap<>());

				Tokenizer source = tokinizerFactory.create();
				TokenStream lowerCaseFilter = lowerCaseFilterFactory.create(source);
				TokenStream commonTermsFilter = commonGramsFilterFactory.create(lowerCaseFilter);
				TokenStream stopWordsFilter = stopWordsFilterFactory.create(commonTermsFilter);
				TokenStream germanNormalizationFilter = germanNormalizationFilterFactory.create(stopWordsFilter);

				return new Analyzer.TokenStreamComponents(source, germanNormalizationFilter);
			}
		};
	}

	/**
	 * Extracts tokens to be send to search from phrase.
	 * @param terms terms to be analysed
	 * @return list of tokens to be searched.
	 */
	public TokensExtractionResult getTokens(List<String> terms) {
		TokensExtractionResult result = new TokensExtractionResult();
		List<Token> tokens = null;
		String phrase = null;

		TokenStream tokenStream = null;
		try {
			if (this.isInitialized) {
				if (!CollectionUtils.isEmpty(terms)) {
					phrase = terms.stream().collect(Collectors.joining(space));

					tokenStream = analyzer.tokenStream(analyzerTextFieldName, phrase);
					tokenStream.reset();

					int position = 0;
					while (tokenStream.incrementToken()) {
						CharTermAttribute termAttribute = tokenStream.getAttribute(CharTermAttribute.class);
						PositionIncrementAttribute positionIncrementAttribute = tokenStream.getAttribute(PositionIncrementAttribute.class);
						PositionLengthAttribute positionLengthAttribute = tokenStream.getAttribute(PositionLengthAttribute.class);
						OffsetAttribute offsetAttribute = tokenStream.getAttribute(OffsetAttribute.class);
						TypeAttribute typeAttribute = tokenStream.getAttribute(TypeAttribute.class);

						if (tokens == null) {
							tokens = new ArrayList<>();
						}

						// add position increment to calculate the position
						// for <common gram> token, the position increment is 0
						position += positionIncrementAttribute.getPositionIncrement();
						tokens.add(new Token(termAttribute.toString(), typeAttribute.type(), position,  offsetAttribute.startOffset(), offsetAttribute.endOffset()));
					}


					List<String> termsForSearch = this.getTermsForSearch(tokens);
					result.setSearchTokens(termsForSearch);
					result.setHighlightTokens(termsForSearch);
				}
			} else {
				throw new VTASyntaxProcessingException(ProcessingErrorType.VTASyntaxAnalyzerInitializationFailed, ErrorMessages.vtaSyntaxAnalyzerInitializationFailed, null);
			}
		} catch (VTASyntaxProcessingException ex) {
			Throwables.propagate(ex);
		} catch (Exception ex) {
			logger.error("Failed to extract tokens from phrase - {}. Error - {}. {}", phrase != null ? phrase : phrase, ex.getMessage(), ex.getStackTrace());

			throw new VTASyntaxProcessingException(ProcessingErrorType.TermTokensExtractionFailed, String.format(ErrorMessages.termTokensExtractionFailed, phrase != null ? phrase : ""), ex);
		} finally {
			try {
				if (tokenStream != null) {
					IOUtils.close(tokenStream);
				}
			} catch (Exception ex) {
				// discard
				logger.error("Failed to close tokens stream. Error - {}. {}", ex.getMessage(), ex.getStackTrace());
			}
		}

		return result;
	}

	private List<String> getTermsForSearch(List<Token> tokens) {
		List<String> terms = null;

		if (!CollectionUtils.isEmpty(tokens)) {

			MultivaluedHashMap<Integer, Token> tokensMap = new MultivaluedHashMap<Integer, Token>();
			terms = new ArrayList<>();
			int maxPosition = -1;

			// create Tokens map by position
			// as 2 tokens migth have same position (ALPAHNUM and CommonGram)
			for (Token token : tokens) {
				tokensMap.add(token.getPosition(), token);
				maxPosition = Math.max(maxPosition, token.getPosition());
			}


			for (int i = 1; i <= maxPosition; i++) {
				List<Token> tokensByPosition = (List<Token>) tokensMap.get(i);
				if (tokensByPosition.size() > 1) {
					// if 2 tokens have same postion, then take the common gram token which is term with common gram
					Optional<Token> commonGramTokenOpt = tokensByPosition.stream().filter(t -> t.getType().equals(tokenTypeGram)).findFirst();
					if (commonGramTokenOpt.isPresent()) {
						terms.add(commonGramTokenOpt.get().getTerm());
					}
				} else {
					terms.add(tokensByPosition.get(0).getTerm());
				}
			}
		}

		return terms;
	}

	private Token findToken(List<Token> tokens, int offsetStart, int offsetEnd, String tokenType) {
		// @formatter:off
		Optional<Token> foundToken = tokens.stream()
		                                   .filter(t -> (t.getOffsetStart() == offsetStart || t.getOffsetEnd() == offsetEnd) && tokenType.equals(t.getType()))
		                                   .findFirst();
		//@formatter:on

		if (foundToken.isPresent()) {
			return foundToken.get();
		}

		return null;
	}

	/**
	 * Dispose.
	 */
	public void dispose() {
		try {
			if (this.analyzer != null) {
				this.analyzer.close();
			}
		} catch (Exception ex) {
			// discard
			logger.error("Failed to close analyzer. Error - {}. {}", ex.getMessage(), ex.getStackTrace());
		}
	}
}
