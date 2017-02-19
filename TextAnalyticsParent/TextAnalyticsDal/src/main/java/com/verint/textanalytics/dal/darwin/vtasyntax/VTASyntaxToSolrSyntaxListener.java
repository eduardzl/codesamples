package com.verint.textanalytics.dal.darwin.vtasyntax;

import com.google.common.base.Throwables;
import com.verint.textanalytics.common.utils.StringUtils;
import com.verint.textanalytics.dal.darwin.vtasyntax.customparsers.PhraseData;
import com.verint.textanalytics.dal.darwin.vtasyntax.customparsers.TermParser;
import com.verint.textanalytics.dal.darwin.vtasyntax.customparsers.TermProximityData;
import com.verint.textanalytics.dal.darwin.vtasyntax.customparsers.TermTokensExtractor;
import com.verint.textanalytics.dal.darwin.vtasyntax.errors.ErrorMessages;
import com.verint.textanalytics.dal.darwin.vtasyntax.errors.ProcessingErrorType;
import com.verint.textanalytics.dal.darwin.vtasyntax.errors.VTASyntaxProcessingException;
import com.verint.textanalytics.dal.darwin.vtasyntax.errors.VTASyntaxRecognitionException;
import com.verint.textanalytics.dal.darwin.vtasyntax.utils.CollectionUtils;
import com.verint.textanalytics.dal.darwin.vtasyntax.utils.DataUtils;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.misc.Triple;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by EZlotnik on 2/24/2016.
 */
public class VTASyntaxToSolrSyntaxListener extends VTASyntaxBaseListener {
	private Map<ParseTree, Pair<ExprType, String>> values; /** maps nodes to strings with.  */

	@Getter
	private List<QueryTerm> terms;

	// by default we allow 3 words between tokens in NEAR expression, so the slop is 4
	private final Integer defaultDistanceInNear = 4;

	private final String invCommas = "\"";
	private final String fieldWithValueFormat = "%s:%s";
	private final String fieldWithQueryFormat = "%s:(%s)";
	private final String fieldWithQueryNoParethFormat = "%s:%s";
	private final String phraseWithDistanceFormat = "%s:\"%s %s\"~%s";
	private final String invCommasPattern = "\"%s\"";

	private final String valueInParentsPattern = "(%s)";
	private final String mustValueInParentsPattern = "+(%s)";

	private final String nearBetweenWordsPattern = "\"%s %s\"~%s";
	private final String nearSurroundParserPattern = "_query_:\"{!surround}%s:%sW(%s,%s)\"";
	private final String complexPhraseQuery = "_query_:\"{!complexphrase inOrder=true}%s:\\\"%s\\\"\"";

	private final String space = " ";
	private final String comma = ",";


	@Setter
	private TASQueryConfiguration queryGenerationConfiguration;

	@Setter
	private TermTokensExtractor termTokensExtractor;

	@Setter
	private TermParser termParser;

	/**
	 * Constructor.
	 */
	public VTASyntaxToSolrSyntaxListener() {
		/** maps nodes to strings with  */
		this.values = new HashMap<>();
		this.terms = new ArrayList<>();
	}

	/**
	 * pushes node value into map.
	 *
	 * @param node  node
	 * @param value value of this node
	 */
	private void setValue(ParseTree node, Pair<ExprType, String> value) {
		values.put(node, value);
	}

	/**
	 * retrieves node value from map.
	 *
	 * @param node node
	 * @return it's value from map
	 */
	public Pair<ExprType, String> getValue(ParseTree node) {
		return values.get(node);
	}


	@Override
	public void exitWord(VTASyntaxParser.WordContext ctx) {
		if (ctx.WORD() != null) {
			setValue(ctx, new Pair<>(ExprType.BasicTerm, ctx.WORD().getText()));
		} else {
			setValue(ctx, new Pair<>(ExprType.BasicTerm, ctx.INT().getText()));
		}
	}

	// word   # NOSPSWord
	@Override
	public void exitNOSPSWord(VTASyntaxParser.NOSPSWordContext ctx) {
		String word = ctx.word().getText();

		// apply wild card limitations
		this.termParser.validateTermWildCardValidityForSingleTermQuery(word, this.queryGenerationConfiguration.getWildCardPrefixMinLength());

		// add word to no SPS terms
		List<String> searchTerms = Arrays.asList(word);
		this.terms.add(new QueryTerm(word, TermType.Word, SpeakerType.NoSPS, word, searchTerms));

		String term = this.queryGenerationConfiguration.isEscapeValuesEnabled() ? DataUtils.escapeCharsForSolrQuery(word) : word;
		setValue(ctx, new Pair<>(ExprType.BasicTerm,  String.format(fieldWithValueFormat, this.queryGenerationConfiguration.getTextNoSPSField(), term)));
	}

	@Override
	public void exitNoSPSPhrase(VTASyntaxParser.NoSPSPhraseContext ctx) {
		if (ctx.phrase() != null) {
			Triple<String, String, List<String>> phraseValue = getWordsListText(ctx.phrase().word(), space);

			// apply wild card limitations
			this.termParser.validateTermWildCardValidatityForPhraseOrProximityQuery(phraseValue.c);

			// build phrase queries for Search and Highlight
			PhraseData phraseData = PhraseData.build(phraseValue.a, phraseValue.b, phraseValue.c, this.queryGenerationConfiguration.isEscapeValuesEnabled(), this.termTokensExtractor);

			QueryTerm queryTerm = new QueryTerm(phraseValue.a, TermType.Phrase, SpeakerType.NoSPS, phraseData.getPhraseQuery(),
			                          phraseData.getSearchTokens());

			String phraseQuery = String.format(fieldWithValueFormat, this.queryGenerationConfiguration.getTextNoSPSField(), phraseData.getPhraseQuery());
			setValue(ctx, new Pair<>(ExprType.BasicTerm, phraseQuery));

			this.terms.add(queryTerm);
		}
	}

	@Override
	public void exitAgentWord(VTASyntaxParser.AgentWordContext ctx) {
		String word = ctx.agent_word().word().getText();

		// apply wild card limitations
		this.termParser.validateTermWildCardValidityForSingleTermQuery(word, this.queryGenerationConfiguration.getWildCardPrefixMinLength());

		// add word to Agent terms
		List<String> searchTerms = Arrays.asList(word);
		this.terms.add(new QueryTerm(word, TermType.Word, SpeakerType.Agent, word, searchTerms));

		String wordQuery = String.format(fieldWithValueFormat, this.queryGenerationConfiguration.getTextAgentField(), this.queryGenerationConfiguration.isEscapeValuesEnabled() ? DataUtils.escapeCharsForSolrQuery(word) : word);
		setValue(ctx, new Pair<>(ExprType.BasicTerm, wordQuery));
	}

	@Override
	public void exitAgentPhrase(VTASyntaxParser.AgentPhraseContext ctx) {
		if (ctx.agent_phrase() != null) {
			Triple<String, String, List<String>> phraseValue = getWordsListText(ctx.agent_phrase().phrase().word(), space);

			this.termParser.validateTermWildCardValidatityForPhraseOrProximityQuery(phraseValue.c);

			// build phrase queries for Search and Highlight
			PhraseData phraseData = PhraseData.build(phraseValue.a, phraseValue.b, phraseValue.c, this.queryGenerationConfiguration.isEscapeValuesEnabled(), this.termTokensExtractor);

			QueryTerm queryTerm = new QueryTerm(phraseValue.a, TermType.Phrase, SpeakerType.Agent, phraseData.getPhraseQuery(),
			                          phraseData.getSearchTokens());

			String phraseQuery = String.format(fieldWithValueFormat, this.queryGenerationConfiguration.getTextAgentField(), phraseData.getPhraseQuery());
			setValue(ctx, new Pair<>(ExprType.BasicTerm, phraseQuery));

			this.terms.add(queryTerm);
		}
	}

	@Override
	public void exitCustomerWord(VTASyntaxParser.CustomerWordContext ctx) {
		String word = ctx.customer_word().word().getText();

		// apply wild card limitations
		this.termParser.validateTermWildCardValidityForSingleTermQuery(word, this.queryGenerationConfiguration.getWildCardPrefixMinLength());

		// add word to Agent terms
		List<String> searchTerms = Arrays.asList(word);
		this.terms.add(new QueryTerm(word, TermType.Word, SpeakerType.Customer, word, searchTerms));

		String wordQuery = String.format(fieldWithValueFormat, this.queryGenerationConfiguration.getTextCustomerField(), this.queryGenerationConfiguration.isEscapeValuesEnabled() ? DataUtils.escapeCharsForSolrQuery(word) : word);
		setValue(ctx, new Pair<>(ExprType.BasicTerm, wordQuery));
	}

	@Override
	public void exitCustomerPhrase(VTASyntaxParser.CustomerPhraseContext ctx) {
		if (ctx.customer_phrase() != null) {
			Triple<String, String, List<String>> phraseValue = getWordsListText(ctx.customer_phrase().phrase().word(), space);

			// apply wild card limitations
			this.termParser.validateTermWildCardValidatityForPhraseOrProximityQuery(phraseValue.c);

			QueryTerm queryTerm = null;

			// build phrase queries for Search and Highlight
			PhraseData phraseData = PhraseData.build(phraseValue.a, phraseValue.b, phraseValue.c, this.queryGenerationConfiguration.isEscapeValuesEnabled(), this.termTokensExtractor);

			queryTerm = new QueryTerm(phraseValue.a, TermType.Phrase, SpeakerType.Customer, phraseData.getPhraseQuery(),
			                          phraseData.getSearchTokens());

			String phraseQuery = String.format(fieldWithValueFormat, this.queryGenerationConfiguration.getTextCustomerField(), phraseData.getPhraseQuery());
			setValue(ctx, new Pair<>(ExprType.BasicTerm, phraseQuery));

			this.terms.add(queryTerm);
		}
	}

	@Override
	public void exitExprBasicTerm(VTASyntaxParser.ExprBasicTermContext ctx) {
		setValue(ctx, getValue(ctx.term()));
	}


	@Override
	public void exitExprAnd(VTASyntaxParser.ExprAndContext ctx) {
		if (ctx.getChildCount() > 1) {

			// collect only epxressions to be place in query
			if (!CollectionUtils.isEmpty(ctx.exprBasic())) {

				int regularExprsCount = 0, notExprsCount = 0;
				StringJoiner andExprQuery = new StringJoiner(" ");

				for (ParseTree child : ctx.children) {

					if (child instanceof VTASyntaxParser.ExprBasicContext) {
						// get value of current epxression as it was previously stored when visiting in this child node
						Pair<ExprType, String> childTypeAndValue = getValue(child);

						ExprType childType = childTypeAndValue.a;
						String childQuery = childTypeAndValue.b;

						switch (childType) {
							case BasicTerm:
							case ExprBasicNear:
							case ExprBasicInParent:
								// add +expression
								andExprQuery.add(String.format("+%s", childQuery));

								// count number of expressions which are not NOT
								regularExprsCount++;
								break;

							case TermNot:
							case ExprBasicNot:
							case ExprWithNotOnly:

								// don't add () around NOT expressions
								// don't add + for MUST_NOT only expressions (single NOT and expression with NOT only)
								andExprQuery.add(childQuery);

								notExprsCount++;
								break;
							case ExprsOr:
							case ExprsAnd:
							default:
								// add () around complex expressions
								andExprQuery.add(String.format(mustValueInParentsPattern, childQuery));

								// count number of expressions which are not NOT
								regularExprsCount++;
								break;
						}
					}
				}

				// save type and query for AND expression
				if (notExprsCount > 0 && regularExprsCount == 0) {
					// the expression contains only NOT expressions
					setValue(ctx, new Pair<>(ExprType.ExprWithNotOnly, andExprQuery.toString()));
				} else {
					// expressions contains some non NOT expressions
					setValue(ctx, new Pair<>(ExprType.ExprsAnd, andExprQuery.toString()));
				}

			} else {
				throw new VTASyntaxProcessingException(ProcessingErrorType.SearchIsEmptyOrIncludeStopWordOnly, ErrorMessages.searchIsEmptyOrIncludeStopWordOnly);
			}
		} else {
			setValue(ctx, getValue(ctx.getChild(0)));
		}
	}

	@Override
	public void exitExprOr(VTASyntaxParser.ExprOrContext ctx) {
		if (ctx.getChildCount() > 1) {

			if (!CollectionUtils.isEmpty(ctx.exprAnd())) {

				int regularExprsCount = 0, notExprsCount = 0;
				StringJoiner orExprQuery = new StringJoiner(" ");

				// collect only epxressions to be place in query
				for (ParseTree child: ctx.children) {
					if (child instanceof   VTASyntaxParser.ExprAndContext) {
						// get value of current epxression as it was previously stored when visiting in this child node
						Pair<ExprType, String> childTypeAndValue = getValue(child);

						ExprType childType = childTypeAndValue.a;
						String childQuery = childTypeAndValue.b;
						String query = null;

						switch (childType) {
							case BasicTerm:
							case ExprBasicNear:
							case ExprBasicInParent:
								// don't wrap basic terms, phrase and expression in parentheses
								orExprQuery.add(childQuery);

								regularExprsCount++;
								break;
							case TermNot:
							case ExprBasicNot:
							case ExprWithNotOnly:
								// don't wrap negation of term or expression
								orExprQuery.add(childQuery);

								notExprsCount++;
								break;

							case ExprsOr:
							case ExprsAnd:
							default:
								orExprQuery.add(String.format(valueInParentsPattern, childQuery));

								regularExprsCount++;
								break;
						}
					}
				}

				// save type and query for AND expression
				if (notExprsCount > 0 && regularExprsCount == 0) {
					// save OR query
					setValue(ctx, new Pair<>(ExprType.ExprWithNotOnly, orExprQuery.toString()));
				} else {
					setValue(ctx, new Pair<>(ExprType.ExprsOr, orExprQuery.toString()));
				}

			} else {
				throw new VTASyntaxProcessingException(ProcessingErrorType.SearchIsEmptyOrIncludeStopWordOnly, ErrorMessages.searchIsEmptyOrIncludeStopWordOnly);
			}
		} else {
			setValue(ctx, getValue(ctx.getChild(0)));
		}
	}

	@Override
	public void exitExprBasicInParent(VTASyntaxParser.ExprBasicInParentContext ctx) {
		String exprInBrackets = null;
		Pair<ExprType, String> expressionData = getValue(ctx.exprOr());

		switch (expressionData.a) {
			case ExprBasicInParent:
			case ExprBasicNot:
			case TermNot:
			case ExprWithNotOnly:
				// (NOT close) (NOT account)  ->  NOT close NOT account
				// NOT operator and expression in parentheses should not be added with extra ()

				exprInBrackets = expressionData.b;
				// keep the expression type as no () were added
				setValue(ctx, new Pair<>(expressionData.a, exprInBrackets));
				break;

			case ExprsOr:
			case ExprsAnd:
				exprInBrackets = String.format(valueInParentsPattern, getValue(ctx.exprOr()).b);
				setValue(ctx, new Pair<>(ExprType.ExprBasicInParent, exprInBrackets));
				break;
			default:
				exprInBrackets = String.format(valueInParentsPattern, getValue(ctx.exprOr()).b);
				setValue(ctx, new Pair<>(ExprType.ExprBasicInParent, exprInBrackets));
				break;
		}
	}

	@Override
	public void exitExprBasicNear(VTASyntaxParser.ExprBasicNearContext ctx) {
		String nearBasicExpr = getValue(ctx.exprNear()).b;

		setValue(ctx, new Pair<>(ExprType.ExprBasicNear, nearBasicExpr));
	}

	@Override
	public void exitExprBasicNot(VTASyntaxParser.ExprBasicNotContext ctx) {
		String exprNot;

		Pair<ExprType, String> expressionData = getValue(ctx.exprBasic());

		// if child is NOT ,then NOT is already wrapper with (), so don't
		// add additional ()
		switch (expressionData.a) {
			case ExprBasicInParent:
				exprNot = String.format("-%s", getValue(ctx.exprBasic()).b);
				break;
			case ExprWithNotOnly:
				// don't double negate
				exprNot = String.format("%s", getValue(ctx.exprBasic()).b);
				break;
			default:
				exprNot = String.format("-(%s)", getValue(ctx.exprBasic()).b);
				break;
		}

		setValue(ctx, new Pair<>(ExprType.ExprBasicNot, exprNot));
	}

	@Override
	public void exitTermNot(VTASyntaxParser.TermNotContext ctx) {
		String exprNot = String.format("-(%s)", getValue(ctx.term()).b);

		setValue(ctx, new Pair<>(ExprType.TermNot, exprNot));
	}


	@Override
	public void exitNoSPSTermsNearClause(VTASyntaxParser.NoSPSTermsNearClauseContext ctx) {
		String nearQuery;

		try {
			if (ctx.termsNoSPSNear() != null && ctx.termsNoSPSNear().children != null) {
				List<ParseTree> nearChildren = ctx.termsNoSPSNear().children;
				List<String> proximityQueries = new ArrayList<>();
				String basicProximityQuery;

				// iterate children
				for (int i = 0; i < nearChildren.size() - 1; i++) {
					// get NEAR query for each pair of terms to allow syntax
					// a NEAR B NEAR c NEAR d
					basicProximityQuery = getBasicProximityQuery(nearChildren.get(i), nearChildren.get(i + 1), nearChildren.get(i + 2),
					                                             this.queryGenerationConfiguration.getTextNoSPSField(), this.queryGenerationConfiguration.isEscapeValuesEnabled());

					if (!StringUtils.isNullOrBlank(basicProximityQuery)) {
						proximityQueries.add(basicProximityQuery);
					}
					i++;
				}

				if (proximityQueries.size() > 1) {
					// @formatter:off
					nearQuery = proximityQueries.stream()
											    .map(q -> String.format("+%s", q))
							                    .collect(Collectors.joining(space));
                    // @formatter:on
					setValue(ctx, new Pair<>(ExprType.ExprsAnd, String.format(valueInParentsPattern, nearQuery)));
				} else {
					nearQuery = proximityQueries.get(0);
					setValue(ctx, new Pair<>(ExprType.ExprBasicNear, nearQuery));
				}
			}
		} catch (VTASyntaxProcessingException ex) {
			Throwables.propagate(ex);
		} catch (Exception ex) {
			throw new VTASyntaxRecognitionException(ErrorMessages.syntaxParsingFailed, ex);
		}
	}

	@Override
	public void exitCustomerTermsNearClause(VTASyntaxParser.CustomerTermsNearClauseContext ctx) {
		String nearQuery;
		Boolean nearExpression = false;

		try {
			if (ctx.termsCustomerNear() != null && ctx.termsCustomerNear().children != null) {
				List<ParseTree> nearChildren = ctx.termsCustomerNear().children;
				List<String> proximityQueries = new ArrayList<>();
				String basicProximityQuery;

				// iterate children
				for (int i = 0; i < nearChildren.size() - 1; i++) {
					// get NEAR query for each pair of terms to allow syntax
					// a NEAR B NEAR c NEAR d
					basicProximityQuery = getBasicProximityQuery(nearChildren.get(i), nearChildren.get(i + 1), nearChildren.get(i + 2),
					                                             this.queryGenerationConfiguration.getTextCustomerField(), this.queryGenerationConfiguration.isEscapeValuesEnabled());

					if (!StringUtils.isNullOrBlank(basicProximityQuery)) {
						proximityQueries.add(basicProximityQuery);
					}
					i++;
				}

				if (proximityQueries.size() > 1) {
					// @formatter:off
					nearQuery = proximityQueries.stream()
												.map(q -> String.format("+%s", q))
							                    .collect(Collectors.joining(space));
                    // @formatter:on
					setValue(ctx, new Pair<>(ExprType.ExprsAnd, String.format(valueInParentsPattern, nearQuery)));
				} else {
					nearQuery = proximityQueries.get(0);
					setValue(ctx, new Pair<>(ExprType.ExprBasicNear, nearQuery));
				}
			}
		} catch (VTASyntaxProcessingException ex) {
			Throwables.propagate(ex);
		} catch (Exception ex) {
			throw new VTASyntaxRecognitionException(ErrorMessages.syntaxParsingFailed, ex);
		}
	}

	@Override
	public void exitAgentTermsNearClause(VTASyntaxParser.AgentTermsNearClauseContext ctx) {
		String nearQuery;

		try {
			if (ctx.termsAgentNear() != null && ctx.termsAgentNear().children != null) {
				List<ParseTree> nearChildren = ctx.termsAgentNear().children;
				List<String> proximityQueries = new ArrayList<>();
				String basicProximityQuery;

				// iterate children
				for (int i = 0; i < nearChildren.size() - 1; i++) {
					// get NEAR query for each pair of terms to allow syntax
					// a NEAR B NEAR c NEAR d
					basicProximityQuery = getBasicProximityQuery(nearChildren.get(i), nearChildren.get(i + 1), nearChildren.get(i + 2),
					                                             this.queryGenerationConfiguration.getTextAgentField(), this.queryGenerationConfiguration.isEscapeValuesEnabled());

					if (!StringUtils.isNullOrBlank(basicProximityQuery)) {
						proximityQueries.add(basicProximityQuery);
					}
					i++;
				}

				if (proximityQueries.size() > 1) {
					// @formatter:off
					nearQuery = proximityQueries.stream()
												.map(q -> String.format("+%s", q))
							                    .collect(Collectors.joining(space));
                    // @formatter:on
					setValue(ctx, new Pair<>(ExprType.ExprsAnd, String.format(valueInParentsPattern, nearQuery)));
				} else {
					nearQuery = proximityQueries.get(0);
					setValue(ctx, new Pair<>(ExprType.ExprBasicNear, nearQuery));
				}
			}
		} catch (VTASyntaxProcessingException ex) {
			Throwables.propagate(ex);
		} catch (Exception ex) {
			throw new VTASyntaxRecognitionException(ErrorMessages.syntaxParsingFailed, ex);
		}
	}

	@Override
	public void exitVtaexpr(VTASyntaxParser.VtaexprContext ctx) {
		setValue(ctx, new Pair<>(ExprType.VTAExpr, getValue(ctx.exprOr()).b));
	}

	private Triple<String, String, List<String>> getWordsListText(List<VTASyntaxParser.WordContext> wordsCtx, String separator) {
		String wordsText = "", wordTextEncoded = "";
		String wordValue;
		List<String> words = null;
		Triple<String, String, List<?>> wordsList;

		if (wordsCtx != null) {
			words = new ArrayList<>();

			for (int i = 0; i < wordsCtx.size(); i++) {
				// get word in the list
				VTASyntaxParser.WordContext wordCtx = wordsCtx.get(i);
				// get word value stored in lower levels of tree
				wordValue = getValue(wordCtx).b;

				words.add(wordValue);
				if (i >= 1) {
					wordsText += String.format("%s%s", separator, wordValue);
					wordTextEncoded += String.format("%s%s", separator, DataUtils.escapeCharsForSolrQuery(wordValue));
				} else {
					wordsText += wordValue;
					wordTextEncoded += DataUtils.escapeCharsForSolrQuery(wordValue);
				}
			}
		}

		return new Triple<>(wordsText, wordTextEncoded, words);
	}

	private String getBasicProximityQuery(ParseTree term0, ParseTree near, ParseTree term1, String fieldName, boolean escapeValues) {
		String nearQuery = "";
		String term0ProximityExpr = null, term1ProximityExpr = null;
		Integer slop = defaultDistanceInNear;

		term0ProximityExpr = getProximityTerm(term0, escapeValues);
		term1ProximityExpr = getProximityTerm(term1, escapeValues);

		if (near instanceof  VTASyntaxParser.NearContext) {
			VTASyntaxParser.NearContext nearOperator = (VTASyntaxParser.NearContext) near;
			if (nearOperator.INT() != null) {
				slop = Integer.parseInt(nearOperator.INT().toString()) + 1;
			}
		}

		if (!StringUtils.isNullOrBlank(term0ProximityExpr) && !StringUtils.isNullOrBlank(term1ProximityExpr)) {
			// generate Surround Query
			nearQuery = String.format(nearSurroundParserPattern, fieldName, slop, term0ProximityExpr, term1ProximityExpr);
		}

		return nearQuery;
	}

	private String getProximityTerm(ParseTree term, boolean escapeValues) {
		String termProximityExpr = null;
		TermType termType = null;
		SpeakerType speakerType = null;
		List<VTASyntaxParser.WordContext> words = new ArrayList<>();

		// the left side of NEAR is word
		if (term instanceof VTASyntaxParser.WordContext) {
			VTASyntaxParser.WordContext wordCtx = (VTASyntaxParser.WordContext) term;

			termType = TermType.Word;
			speakerType = SpeakerType.NoSPS;
			words.add(wordCtx);

		} else if (term instanceof VTASyntaxParser.Agent_wordContext) {
			// the left side of NEAR is Agent word
			VTASyntaxParser.Agent_wordContext agentWordCtx = (VTASyntaxParser.Agent_wordContext) term;

			termType = TermType.Word;
			speakerType = SpeakerType.Agent;
			words.add(agentWordCtx.word());

		} else if (term instanceof VTASyntaxParser.Customer_wordContext) {
			// the left side of NEAR is Customer word
			VTASyntaxParser.Customer_wordContext customerWordCtx = (VTASyntaxParser.Customer_wordContext) term;

			termType = TermType.Word;
			speakerType = SpeakerType.Customer;
			words.add(customerWordCtx.word());
		} else if (term instanceof VTASyntaxParser.PhraseContext) {
			// the left side of NEAR is phrase
			VTASyntaxParser.PhraseContext phraseCtx = (VTASyntaxParser.PhraseContext) term;

			termType = TermType.Phrase;
			speakerType = SpeakerType.NoSPS;
			words.addAll(phraseCtx.word());
		} else if (term instanceof VTASyntaxParser.Agent_phraseContext) {
			// the left side of NEAR is Agent phrase
			VTASyntaxParser.Agent_phraseContext phraseCtx = (VTASyntaxParser.Agent_phraseContext) term;

			termType = TermType.Phrase;
			speakerType = SpeakerType.Agent;
			words.addAll(phraseCtx.phrase().word());
		} else if (term instanceof VTASyntaxParser.Customer_phraseContext) {
			// the left side of NEAR is Customer phrase
			VTASyntaxParser.Customer_phraseContext phraseCtx = (VTASyntaxParser.Customer_phraseContext) term;

			termType = TermType.Phrase;
			speakerType = SpeakerType.Customer;
			words.addAll(phraseCtx.phrase().word());
		}

		if (termType != null && speakerType != null) {
			// generate phrase proximity tokens and query
			Triple<String, String, List<String>> termTokens = getWordsListText(words, space);

			// apply wild card limitations
			this.termParser.validateTermWildCardValidatityForPhraseOrProximityQuery(termTokens.c);

			TermProximityData termData = TermProximityData.build(termTokens.c, this.termTokensExtractor, this.queryGenerationConfiguration.isEscapeValuesEnabled());

			termProximityExpr = termData.getQuery();

			// add Term to list of terms
			this.terms.add(new QueryTerm(termTokens.a, termType, speakerType, termData.getQuery(), termData.getSearchTokens()));
		}

		return termProximityExpr;
	}

	/**
	 * Expression type enum.
	 */
	enum ExprType {
		// @formatter:off
		BasicTerm(0),
		ExprBasicInParent(1),
		ExprBasicNear(2),
		ExprBasicNot(3),
		TermNot(4),
		ExprWithNotOnly(5),
		ExprsOr(6),
		ExprsAnd(7),
		VTAExpr(8);
	    // @formatter:on

		private int type;

		ExprType(int exprType) {
			this.type = exprType;
		}
	}
}
