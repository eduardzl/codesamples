package com.verint.textanalytics.dal.darwin.vtasyntax;


import static org.junit.Assert.*;

import com.verint.textanalytics.common.utils.DataUtils;
import com.verint.textanalytics.dal.darwin.vtasyntax.customparsers.TermParser;
import com.verint.textanalytics.dal.darwin.vtasyntax.customparsers.TermTokensExtractor;
import com.verint.textanalytics.dal.darwin.vtasyntax.customparsers.TokensExtractionResult;
import com.verint.textanalytics.dal.darwin.vtasyntax.errors.ProcessingErrorType;
import com.verint.textanalytics.dal.darwin.vtasyntax.errors.VTASyntaxProcessingException;
import com.verint.textanalytics.dal.darwin.vtasyntax.errors.VTASyntaxRecognitionException;
import org.antlr.v4.runtime.misc.Pair;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Created by EZlotnik on 2/24/2016.
 */
public class VTASyntaxAnalyzerTest {


	@Test
	public void NoSPSTermOperationsTest() {
		String query;
		VTASyntaxParsingResult parsingResult;
		QueryTerm term1, term2;
		Pair<VTASyntaxAnalyzer, TASQueryConfiguration> vtaSyntaxAnalyzerWithQueryConfig = this.generateConfiguredVTASyntaxAnalyzer();
		VTASyntaxAnalyzer vtaSyntaxAnalyzer = vtaSyntaxAnalyzerWithQueryConfig.a;
		TASQueryConfiguration queryConfiguration = vtaSyntaxAnalyzerWithQueryConfig.b;

		query = "close";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("text_en_total:close", parsingResult.getSolrQuery());
		assertEquals(parsingResult.getTerms().size(), 1);
		term1 = parsingResult.getTerms().get(0);
		assertNotNull(term1.getSearchTokens());
		assertEquals("close", term1.getTermForQuery());
		assertEquals("close", term1.getHighlightQuery());

		query = "(close)";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("(text_en_total:close)", parsingResult.getSolrQuery());

		query = "close NOT account";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("text_en_total:close -(text_en_total:account)", parsingResult.getSolrQuery());

		assertEquals(parsingResult.getTerms().size(), 2);

		term1 = parsingResult.getTerms().get(0);
		assertNotNull(term1.getSearchTokens());
		assertEquals("close", term1.getTermForQuery());
		assertEquals("close", term1.getHighlightQuery());

		term2 = parsingResult.getTerms().get(1);
		assertNotNull(term2.getSearchTokens());
		assertEquals("account", term2.getTermForQuery());
		assertEquals("account", term2.getHighlightQuery());

		query = "close my account";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("text_en_total:close text_en_total:my text_en_total:account", parsingResult.getSolrQuery());
		assertEquals(3, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());


		query = "\"close my account\"";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("text_en_total:\"close my account\"", parsingResult.getSolrQuery());
		assertEquals(1, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());
		assertNotNull(parsingResult.getTerms());
		term1 = parsingResult.getTerms().get(0);
		assertEquals(TermType.Phrase, term1.getType());
		assertEquals("\"close\\ my\\ account\"", term1.getHighlightQuery());

		query = "close AND account AND NOT \"new phone\"";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("+text_en_total:close +text_en_total:account -(text_en_total:\"new phone\")", parsingResult.getSolrQuery());
		assertEquals(3, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());


		vtaSyntaxAnalyzer.dispose();
	}

	@Test
	public void noSPSTermsOperators_AND_OR_InParentheses() {
		String query;
		VTASyntaxParsingResult parsingResult;

		Pair<VTASyntaxAnalyzer, TASQueryConfiguration> vtaSyntaxAnalyzerWithQueryConfig = this.generateConfiguredVTASyntaxAnalyzer();
		VTASyntaxAnalyzer vtaSyntaxAnalyzer = vtaSyntaxAnalyzerWithQueryConfig.a;
		TASQueryConfiguration queryConfiguration = vtaSyntaxAnalyzerWithQueryConfig.b;

		query = "(speak with manager) AND (close account)";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("+(text_en_total:speak text_en_total:with text_en_total:manager) +(text_en_total:close text_en_total:account)", parsingResult.getSolrQuery());
		assertEquals(5, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		query = "\"cancellation\" AND \"cancel account\" \"cancel\" AND  \"canceled\"";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("(+text_en_total:\"cancellation\" +text_en_total:\"cancel account\") (+text_en_total:\"cancel\" +text_en_total:\"canceled\")", parsingResult.getSolrQuery());

		query = "(speak with manager) AND (close account) OR (buy new phone)";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("(+(text_en_total:speak text_en_total:with text_en_total:manager) +(text_en_total:close text_en_total:account)) (text_en_total:buy text_en_total:new text_en_total:phone)", parsingResult.getSolrQuery());
		assertEquals(8, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		vtaSyntaxAnalyzer.dispose();
	}


	@Test
	public void agentTermOperationsTest() {
		String query;
		VTASyntaxParsingResult parsingResult;
		QueryTerm term1, term2;

		Pair<VTASyntaxAnalyzer, TASQueryConfiguration> vtaSyntaxAnalyzerWithQueryConfig = this.generateConfiguredVTASyntaxAnalyzer();
		VTASyntaxAnalyzer vtaSyntaxAnalyzer = vtaSyntaxAnalyzerWithQueryConfig.a;
		TASQueryConfiguration queryConfiguration = vtaSyntaxAnalyzerWithQueryConfig.b;

		query = "A:close";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("text_en_agent:close", parsingResult.getSolrQuery());
		assertEquals(1, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.Agent)).count());
		assertEquals(0, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.Customer)).count());
		assertEquals(0, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		term1 = parsingResult.getTerms().get(0);
		assertEquals(TermType.Word, term1.getType());
		assertEquals("close", term1.getHighlightQuery());

		query = "A:\"close account\"";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("text_en_agent:\"close account\"", parsingResult.getSolrQuery());
		assertEquals(1, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.Agent)).count());
		assertEquals(0, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		query = "A:\"want to speak with manager\"";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("text_en_agent:\"want to speak with manager\"", parsingResult.getSolrQuery());
		assertEquals(1, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.Agent)).count());

		query = "A:\"want to speak with manager\" AND A:\"close my account\"";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("+text_en_agent:\"want to speak with manager\" +text_en_agent:\"close my account\"", parsingResult.getSolrQuery());
		assertEquals(2, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.Agent)).count());

		query = "(A:\"want to speak with manager\" AND A:\"close my account\")";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("(+text_en_agent:\"want to speak with manager\" +text_en_agent:\"close my account\")", parsingResult.getSolrQuery());
		assertEquals(2, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.Agent)).count());


		query = "(A:\"want to speak with manager\") AND (A:\"close my account\")";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("+(text_en_agent:\"want to speak with manager\") +(text_en_agent:\"close my account\")", parsingResult.getSolrQuery());

		assertEquals(2, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.Agent)).count());
		assertEquals(0, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		vtaSyntaxAnalyzer.dispose();
	}

	@Test
	public void customerTermOperationTest() {
		String query;
		QueryTerm term1, term2;
		VTASyntaxParsingResult parsingResult;
		Pair<VTASyntaxAnalyzer, TASQueryConfiguration> vtaSyntaxAnalyzerWithQueryConfig = this.generateConfiguredVTASyntaxAnalyzer();
		VTASyntaxAnalyzer vtaSyntaxAnalyzer = vtaSyntaxAnalyzerWithQueryConfig.a;
		TASQueryConfiguration queryConfiguration = vtaSyntaxAnalyzerWithQueryConfig.b;

		query = "C:close";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("text_en_customer:close", parsingResult.getSolrQuery());
		assertEquals(1, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.Customer)).count());
		assertEquals(0, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.Agent)).count());

		query = "C:\"cheapest rental\"";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("text_en_customer:\"cheapest rental\"", parsingResult.getSolrQuery());
		assertEquals(1, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.Customer)).count());
		assertEquals(0, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());
		assertEquals(0, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.Agent)).count());

		query = "C:\"close account\"";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("text_en_customer:\"close account\"", parsingResult.getSolrQuery());
		assertEquals(1, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.Customer)).count());
		assertEquals(0, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		query = "C:\"want to speak with manager\"";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("text_en_customer:\"want to speak with manager\"", parsingResult.getSolrQuery());
		assertEquals(1, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.Customer)).count());

		query = "C:\"want to speak with manager\" AND C:\"close my account\"";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("+text_en_customer:\"want to speak with manager\" +text_en_customer:\"close my account\"", parsingResult.getSolrQuery());

		assertEquals(2, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.Customer)).count());

		query = "(C:\"want to speak with manager\" AND C:\"close my account\")";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("(+text_en_customer:\"want to speak with manager\" +text_en_customer:\"close my account\")", parsingResult.getSolrQuery());
		assertEquals(2, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.Customer)).count());

		query = "C:\"want to speak with manager\" AND C:\"close my account\"";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("+text_en_customer:\"want to speak with manager\" +text_en_customer:\"close my account\"", parsingResult.getSolrQuery());

		assertEquals(2, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.Customer)).count());
		assertEquals(0, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		vtaSyntaxAnalyzer.dispose();
	}

	@Test
	public void mixedCustomerAgentExpressionsTest() {
		String query;
		VTASyntaxParsingResult parsingResult;
		Pair<VTASyntaxAnalyzer, TASQueryConfiguration> vtaSyntaxAnalyzerWithQueryConfig = this.generateConfiguredVTASyntaxAnalyzer();
		VTASyntaxAnalyzer vtaSyntaxAnalyzer = vtaSyntaxAnalyzerWithQueryConfig.a;
		TASQueryConfiguration queryConfiguration = vtaSyntaxAnalyzerWithQueryConfig.b;

		query = "A:\"want to close account\" C:close";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("text_en_agent:\"want to close account\" text_en_customer:close", parsingResult.getSolrQuery());
		assertEquals(1, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.Customer)).count());
		assertEquals(1, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.Agent)).count());

		query = "A:\"want to close account\" AND C:close";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("+text_en_agent:\"want to close account\" +text_en_customer:close", parsingResult.getSolrQuery());
		assertEquals(1, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.Agent)).count());
		assertEquals(1, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.Customer)).count());

		query = "A:\"want to close account\" AND A:\"want to speak with manager\" AND NOT \"credit card\"";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("+text_en_agent:\"want to close account\" +text_en_agent:\"want to speak with manager\" -(text_en_total:\"credit card\")", parsingResult.getSolrQuery());
		assertEquals(2, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.Agent)).count());
		assertEquals(1, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		query = "A:\"how can i help\" AND C:\"my phone not working\" AND (C:iphone C:iphone5 C:iphone6) AND NOT C:galaxy";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("+text_en_agent:\"how can i help\" +text_en_customer:\"my phone not working\" +(text_en_customer:iphone text_en_customer:iphone5 text_en_customer:iphone6) -(text_en_customer:galaxy)", parsingResult.getSolrQuery());
		assertEquals(1, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.Agent)).count());
		assertEquals(5, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.Customer)).count());

		vtaSyntaxAnalyzer.dispose();
	}

	@Test
	public void roundBracketsTest(){
		String query;
		VTASyntaxParsingResult parsingResult;
		Pair<VTASyntaxAnalyzer, TASQueryConfiguration> vtaSyntaxAnalyzerWithQueryConfig = this.generateConfiguredVTASyntaxAnalyzer();
		VTASyntaxAnalyzer vtaSyntaxAnalyzer = vtaSyntaxAnalyzerWithQueryConfig.a;
		TASQueryConfiguration queryConfiguration = vtaSyntaxAnalyzerWithQueryConfig.b;

		query = "(close account)";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("(text_en_total:close text_en_total:account)", parsingResult.getSolrQuery());
		assertEquals(2, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		query = "close (account)";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("text_en_total:close (text_en_total:account)", parsingResult.getSolrQuery());
		assertEquals(2, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		query = "close ((account))";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("text_en_total:close (text_en_total:account)", parsingResult.getSolrQuery());
		assertEquals(2, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		query = "(\"close account\") (\"speak with manager\")";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("(text_en_total:\"close account\") (text_en_total:\"speak with manager\")", parsingResult.getSolrQuery());
		assertEquals(2, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		query = "(\"close account\" \"speak with manager\")";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("(text_en_total:\"close account\" text_en_total:\"speak with manager\")", parsingResult.getSolrQuery());
		assertEquals(2, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());


		query = "((\"close account\") (\"speak with manager\"))";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("((text_en_total:\"close account\") (text_en_total:\"speak with manager\"))", parsingResult.getSolrQuery());
		assertEquals(2, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());


		query = "((close account) AND (speak))  OR ((bill) AND (high))";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("(+(text_en_total:close text_en_total:account) +(text_en_total:speak)) (+(text_en_total:bill) +(text_en_total:high))", parsingResult.getSolrQuery());
		assertEquals(5, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		vtaSyntaxAnalyzer.dispose();
	}


	@Test
	public void operatorANDTest(){
		String query;
		VTASyntaxParsingResult parsingResult;
		Pair<VTASyntaxAnalyzer, TASQueryConfiguration> vtaSyntaxAnalyzerWithQueryConfig = this.generateConfiguredVTASyntaxAnalyzer();
		VTASyntaxAnalyzer vtaSyntaxAnalyzer = vtaSyntaxAnalyzerWithQueryConfig.a;
		TASQueryConfiguration queryConfiguration = vtaSyntaxAnalyzerWithQueryConfig.b;

		query = "close AND account";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("+text_en_total:close +text_en_total:account", parsingResult.getSolrQuery());
		assertEquals(2, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		query = "\"close my account\" AND \"speak with manager\"";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("+text_en_total:\"close my account\" +text_en_total:\"speak with manager\"", parsingResult.getSolrQuery());
		assertEquals(2, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		query = "\"close my account\" AND \"speak with manager\" OR bill OR high";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("(+text_en_total:\"close my account\" +text_en_total:\"speak with manager\") text_en_total:bill text_en_total:high", parsingResult.getSolrQuery());
		assertEquals(4, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		query = "close AND account OR quickly";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("(+text_en_total:close +text_en_total:account) text_en_total:quickly", parsingResult.getSolrQuery());
		assertEquals(3, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());


		query = "close account AND quickly bill high";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("text_en_total:close (+text_en_total:account +text_en_total:quickly) text_en_total:bill text_en_total:high", parsingResult.getSolrQuery());
		assertEquals(5, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());


		query = "close account AND NOT quickly NOT bill NOT high";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("text_en_total:close (+text_en_total:account -(text_en_total:quickly)) -(text_en_total:bill) -(text_en_total:high)", parsingResult.getSolrQuery());
		assertEquals(5, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());


		query = "close AND account AND NOT quickly AND NOT bill AND NOT high";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("+text_en_total:close +text_en_total:account -(text_en_total:quickly) -(text_en_total:bill) -(text_en_total:high)", parsingResult.getSolrQuery());
		assertEquals(5, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		query = "\"close account\" AND (NOT quickly NOT bill NOT high)";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("+text_en_total:\"close account\" -(text_en_total:quickly) -(text_en_total:bill) -(text_en_total:high)", parsingResult.getSolrQuery());
		assertEquals(4, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		vtaSyntaxAnalyzer.dispose();
	}


	@Test
	public void operatorORTest(){
		String query;
		VTASyntaxParsingResult parsingResult;
		Pair<VTASyntaxAnalyzer, TASQueryConfiguration> vtaSyntaxAnalyzerWithQueryConfig = this.generateConfiguredVTASyntaxAnalyzer();
		VTASyntaxAnalyzer vtaSyntaxAnalyzer = vtaSyntaxAnalyzerWithQueryConfig.a;
		TASQueryConfiguration queryConfiguration = vtaSyntaxAnalyzerWithQueryConfig.b;

		query = "close OR my OR account";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("text_en_total:close text_en_total:my text_en_total:account", parsingResult.getSolrQuery());
		assertEquals(3, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());


		query = "\"close my account\" OR \"speak with manager\" OR \"bill high\"";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("text_en_total:\"close my account\" text_en_total:\"speak with manager\" text_en_total:\"bill high\"", parsingResult.getSolrQuery());
		assertEquals(3, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		query = "\"close my account\" OR NOT \"speak with manager\" OR bill NEAR high";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("text_en_total:\"close my account\" -(text_en_total:\"speak with manager\") _query_:\"{!surround}text_en_total:4W(\\\"bill\\\",\\\"high\\\")\"", parsingResult.getSolrQuery());
		assertEquals(4, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());


		query = "\"close my account\" OR NOT (\"speak with manager\" AND bill NEAR high)";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("text_en_total:\"close my account\" -(+text_en_total:\"speak with manager\" +_query_:\"{!surround}text_en_total:4W(\\\"bill\\\",\\\"high\\\")\")", parsingResult.getSolrQuery());
		assertEquals(4, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		query = "close AND account OR speak AND with AND manager OR bill AND high";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("(+text_en_total:close +text_en_total:account) (+text_en_total:speak +text_en_total:with +text_en_total:manager) (+text_en_total:bill +text_en_total:high)", parsingResult.getSolrQuery());
		assertEquals(7, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		vtaSyntaxAnalyzer.dispose();
	}

	@Test
	public void operatorNOTTest(){
		String query;
		VTASyntaxParsingResult parsingResult;
		Pair<VTASyntaxAnalyzer, TASQueryConfiguration> vtaSyntaxAnalyzerWithQueryConfig = this.generateConfiguredVTASyntaxAnalyzer();
		VTASyntaxAnalyzer vtaSyntaxAnalyzer = vtaSyntaxAnalyzerWithQueryConfig.a;
		TASQueryConfiguration queryConfiguration = vtaSyntaxAnalyzerWithQueryConfig.b;

		query = "(NOT close) (NOT account)";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("-(text_en_total:close) -(text_en_total:account)", parsingResult.getSolrQuery());
		assertEquals(2, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		query = "NOT close AND NOT account";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("-(text_en_total:close) -(text_en_total:account)", parsingResult.getSolrQuery());
		assertEquals(2, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		query = "NOT (close AND NOT account)";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("-(+text_en_total:close -(text_en_total:account))", parsingResult.getSolrQuery());
		assertEquals(2, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		query = "NOT bill NEAR high close NEAR account";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("-(_query_:\"{!surround}text_en_total:4W(\\\"bill\\\",\\\"high\\\")\") _query_:\"{!surround}text_en_total:4W(\\\"close\\\",\\\"account\\\")\"", parsingResult.getSolrQuery());
		assertEquals(4, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		query = "(NOT bill NEAR high) close NEAR account";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("-(_query_:\"{!surround}text_en_total:4W(\\\"bill\\\",\\\"high\\\")\") _query_:\"{!surround}text_en_total:4W(\\\"close\\\",\\\"account\\\")\"", parsingResult.getSolrQuery());
		assertEquals(4, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		query = "NOT \"close my account\"";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("-(text_en_total:\"close my account\")", parsingResult.getSolrQuery());
		assertEquals(1, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		// multiple parentheses around NOT should not produce () in Solr query
		query = "(((NOT \"close my account\")))";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("-(text_en_total:\"close my account\")", parsingResult.getSolrQuery());
		assertEquals(1, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		// multiple parentheses around NOT should not produce () in Solr query
		query = "\"speak with manager\" AND (((NOT \"close my account\")))";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("+text_en_total:\"speak with manager\" -(text_en_total:\"close my account\")", parsingResult.getSolrQuery());

		query = "(NOT bill NEAR high) AND NOT close NEAR account";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("-(_query_:\"{!surround}text_en_total:4W(\\\"bill\\\",\\\"high\\\")\") -(_query_:\"{!surround}text_en_total:4W(\\\"close\\\",\\\"account\\\")\")", parsingResult.getSolrQuery());

		query = "NOT (bill NEAR high AND close NEAR account)";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("-(+_query_:\"{!surround}text_en_total:4W(\\\"bill\\\",\\\"high\\\")\" +_query_:\"{!surround}text_en_total:4W(\\\"close\\\",\\\"account\\\")\")", parsingResult.getSolrQuery());


		vtaSyntaxAnalyzer.dispose();
	}

	@Test
	public void simplePhraseTest() {
		String query;
		VTASyntaxParsingResult parsingResult;
		Pair<VTASyntaxAnalyzer, TASQueryConfiguration> vtaSyntaxAnalyzerWithQueryConfig = this.generateConfiguredVTASyntaxAnalyzer();
		VTASyntaxAnalyzer vtaSyntaxAnalyzer = vtaSyntaxAnalyzerWithQueryConfig.a;
		TASQueryConfiguration queryConfiguration = vtaSyntaxAnalyzerWithQueryConfig.b;

		query = "\"close my account\"";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("text_en_total:\"close my account\"", parsingResult.getSolrQuery());
		assertEquals(1, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		vtaSyntaxAnalyzer.dispose();
	}


	@Test
	public void wildCardSearchesTest() {
		String query;
		VTASyntaxParsingResult parsingResult;
		Pair<VTASyntaxAnalyzer, TASQueryConfiguration> vtaSyntaxAnalyzerWithQueryConfig = this.generateConfiguredVTASyntaxAnalyzer();
		VTASyntaxAnalyzer vtaSyntaxAnalyzer = vtaSyntaxAnalyzerWithQueryConfig.a;
		TASQueryConfiguration queryConfiguration = vtaSyntaxAnalyzerWithQueryConfig.b;

		query = "close my accou*";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("text_en_total:close text_en_total:my text_en_total:accou*", parsingResult.getSolrQuery());
		assertEquals(3, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		try {
			query = "speak~1 my? accou*";
			parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
			assertEquals("text_en_total:speak~1 text_en_total:my? text_en_total:accou*", parsingResult.getSolrQuery());
			assertEquals(3, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());
		} catch(Exception ex) {
			assertTrue(ex instanceof VTASyntaxProcessingException);
		}

		try {
			query = "sp?eak my accou*";
			parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
			assertEquals("text_en_total:speak~1 text_en_total:my? text_en_total:accou*", parsingResult.getSolrQuery());
			assertEquals(3, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());
		} catch(Exception ex) {
			assertTrue(ex instanceof VTASyntaxProcessingException);
		}

		try {
			query = "speak~1 my ac?count";
			parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
			assertEquals("text_en_total:speak~1 text_en_total:my? text_en_total:accou*", parsingResult.getSolrQuery());
			assertEquals(3, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());
		} catch(Exception ex) {
			assertTrue(ex instanceof VTASyntaxProcessingException);
		}

		query = "speak~1 mine? accou*";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("text_en_total:speak~1 text_en_total:mine? text_en_total:accou*", parsingResult.getSolrQuery());
		assertEquals(3, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		vtaSyntaxAnalyzer.dispose();
	}

	@Test
	public void stopWordsOnlyInProximityQueryTest() {
		String query;
		VTASyntaxParsingResult parsingResult;
		Pair<VTASyntaxAnalyzer, TASQueryConfiguration> vtaSyntaxAnalyzerWithQueryConfig = this.generateConfiguredVTASyntaxAnalyzer();
		VTASyntaxAnalyzer vtaSyntaxAnalyzer = vtaSyntaxAnalyzerWithQueryConfig.a;
		TASQueryConfiguration queryConfiguration = vtaSyntaxAnalyzerWithQueryConfig.b;

		try {
			query = "the NEAR:10 to";
			parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		} catch (Exception ex){
			assertTrue(ex instanceof VTASyntaxProcessingException);
		}


		vtaSyntaxAnalyzer.dispose();
	}


	@Test
	public void usageOfNumbersAndCurrenciesTest() {
		String query;
		VTASyntaxParsingResult parsingResult;
		Pair<VTASyntaxAnalyzer, TASQueryConfiguration> vtaSyntaxAnalyzerWithQueryConfig = this.generateConfiguredVTASyntaxAnalyzer();
		VTASyntaxAnalyzer vtaSyntaxAnalyzer = vtaSyntaxAnalyzerWithQueryConfig.a;
		TASQueryConfiguration queryConfiguration = vtaSyntaxAnalyzerWithQueryConfig.b;

		query = "\"6 months\"";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("text_en_total:\"6 months\"", parsingResult.getSolrQuery());
		assertEquals(1, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		query = "A:\"6 months\"";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("text_en_agent:\"6 months\"", parsingResult.getSolrQuery());
		assertEquals(1, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.Agent)).count());

		query = "C:\"6 months\"";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("text_en_customer:\"6 months\"", parsingResult.getSolrQuery());
		assertEquals(1, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.Customer)).count());

		query = "\"1 2 3 4 5 months\"";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("text_en_total:\"1 2 3 4 5 months\"", parsingResult.getSolrQuery());
		assertEquals(1, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());


		query = "1 2";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("text_en_total:1 text_en_total:2", parsingResult.getSolrQuery());
		assertEquals(2, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		query = "A:\"my order on 100$\"";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("text_en_agent:\"my order on 100$\"", parsingResult.getSolrQuery());
		assertEquals(1, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.Agent)).count());


		query = "A:\"my at&t order\"";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("text_en_agent:\"my at\\&t order\"", parsingResult.getSolrQuery());
		assertEquals(1, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.Agent)).count());


		query = "\"all out of\" \"back order\" \"out-of-stock\" \"short-stock\" \"sold-out\" \"sold out\" \"were out of\" \"did not have\" \"out of\"";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals("text_en_total:\"all out of\" text_en_total:\"back order\" text_en_total:\"out\\-of\\-stock\" text_en_total:\"short\\-stock\" text_en_total:\"sold\\-out\" text_en_total:\"sold out\" text_en_total:\"were out of\" text_en_total:\"did not have\" text_en_total:\"out of\"",
		             parsingResult.getSolrQuery());
		assertEquals(9, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		vtaSyntaxAnalyzer.dispose();
	}

	@Test
	public void nearBetweenWordsTest() {
		String w1Esc, w2Esc, w3Esc, w4Esc, w5Esc, w6Esc, w7Esc, w8Esc, w9Esc;

		String query;
		VTASyntaxParsingResult parsingResult;
		Pair<VTASyntaxAnalyzer, TASQueryConfiguration> vtaSyntaxAnalyzerWithQueryConfig = this.generateConfiguredVTASyntaxAnalyzer();
		VTASyntaxAnalyzer vtaSyntaxAnalyzer = vtaSyntaxAnalyzerWithQueryConfig.a;
		TASQueryConfiguration queryConfiguration = vtaSyntaxAnalyzerWithQueryConfig.b;

		query = "close NEAR:5 account";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);

		w1Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "close"));
		w2Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "account"));

		assertEquals(String.format("_query_:\"{!surround}text_en_total:6W(%s,%s)\"", w1Esc, w2Esc), parsingResult.getSolrQuery());
		assertEquals(2, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		query = "C:broken NEAR:5 C:phone";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		w1Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "broken"));
		w2Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "phone"));
		assertEquals(String.format("_query_:\"{!surround}text_en_customer:6W(%s,%s)\"", w1Esc, w2Esc), parsingResult.getSolrQuery());
		assertEquals(2, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.Customer)).count());

		query = "C:\"speak with manager\" NEAR:5 C:\"close account\"";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);

		w1Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "speak_with"));
		w2Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "with_manager"));
		w3Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "manager"));

		w4Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "close"));
		w5Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "account"));

		assertEquals(String.format("_query_:\"{!surround}text_en_customer:6W(W(%s,%s,%s),W(%s,%s))\"", w1Esc, w2Esc, w3Esc, w4Esc, w5Esc), parsingResult.getSolrQuery());
		assertEquals(2, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.Customer)).count());

		query = "A:help NEAR:6 A:you";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		w1Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "help"));
		w2Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "you"));
		assertEquals(String.format("_query_:\"{!surround}text_en_agent:7W(%s,%s)\"", w1Esc, w2Esc), parsingResult.getSolrQuery());
		assertEquals(2, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.Agent)).count());


		query = "(bill NEAR:10 high) OR (bill NEAR:20 ridiculous) OR \"way too high\" OR overcharged";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);

		w1Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "bill"));
		w2Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "high"));
		w3Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "bill"));
		w4Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "ridiculous"));

		assertEquals(String.format("(_query_:\"{!surround}text_en_total:11W(%s,%s)\") (_query_:\"{!surround}text_en_total:21W(%s,%s)\") text_en_total:\"way too high\" text_en_total:overcharged",
		                           w1Esc, w2Esc, w3Esc, w4Esc), parsingResult.getSolrQuery());
		assertEquals(6, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		query = "(not NEAR recieve) (didn't NEAR recieve) (never NEAR revieved) \"never recieved\" NEAR bill";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);

		w1Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "not"));
		w2Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "recieve"));
		w3Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "didn't"));
		w4Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "recieve"));
		w5Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "never"));
		w6Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "revieved"));
		w7Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "never"));
		w8Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "recieved"));
		w9Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "bill"));

		assertEquals(String.format("(_query_:\"{!surround}text_en_total:4W(%s,%s)\") (_query_:\"{!surround}text_en_total:4W(%s,%s)\") (_query_:\"{!surround}text_en_total:4W(%s,%s)\") _query_:\"{!surround}text_en_total:4W(W(%s,%s),%s)\"",
		             w1Esc, w2Esc, w3Esc, w4Esc, w5Esc, w6Esc, w7Esc, w8Esc, w9Esc),
		             parsingResult.getSolrQuery());
		assertEquals(8, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		query = "(not NEAR recieve) (didn't NEAR recieve) (never NEAR revieved) \"never recieved\" NEAR bill";
		w1Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "not"));
		w2Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "recieve"));
		w3Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "didn't"));
		w4Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "recieve"));
		w5Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "never"));
		w6Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "revieved"));
		w7Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "never"));
		w8Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "recieved"));
		w9Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "bill"));

		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals(String.format("(_query_:\"{!surround}text_en_total:4W(%s,%s)\") (_query_:\"{!surround}text_en_total:4W(%s,%s)\") (_query_:\"{!surround}text_en_total:4W(%s,%s)\") _query_:\"{!surround}text_en_total:4W(W(%s,%s),%s)\"",
		                           w1Esc, w2Esc, w3Esc, w4Esc, w5Esc, w6Esc, w7Esc, w8Esc, w9Esc),
		             parsingResult.getSolrQuery());
		assertEquals(8, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());



		query = "you're NEAR:3 ray";
		w1Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "you're"));
		w2Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "ray"));

		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals(String.format("_query_:\"{!surround}text_en_total:4W(%s,%s)\"",
		                           w1Esc, w2Esc, w3Esc, w4Esc), parsingResult.getSolrQuery());

		assertEquals(2, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());


		query = "You're NEAR:3 ray";
		w1Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "you're"));
		w2Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "ray"));

		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals(String.format("_query_:\"{!surround}text_en_total:4W(%s,%s)\"",
		                           w1Esc, w2Esc, w3Esc, w4Esc), parsingResult.getSolrQuery());

		assertEquals(2, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());


		query = "\"to speak\" NEAR \"with your manager\"";
		w1Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "to_speak"));
		w2Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "speak"));

		w3Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "with_your"));
		w4Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "your"));
		w5Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "manager"));

		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals(String.format("_query_:\"{!surround}text_en_total:4W(W(%s,%s),W(%s,%s,%s))\"",
		                           w1Esc, w2Esc, w3Esc, w4Esc, w5Esc), parsingResult.getSolrQuery());

		assertEquals(2, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());



		query = "\"TO SPEAK\" NEAR \"With Your Manager\"";
		w1Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "to_speak"));
		w2Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "speak"));

		w3Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "with_your"));
		w4Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "your"));
		w5Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "manager"));

		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);
		assertEquals(String.format("_query_:\"{!surround}text_en_total:4W(W(%s,%s),W(%s,%s,%s))\"",
		                           w1Esc, w2Esc, w3Esc, w4Esc, w5Esc), parsingResult.getSolrQuery());

		assertEquals(2, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());

		vtaSyntaxAnalyzer.dispose();
	}


	@Test
	public void nearBetweenWordsCustomerTest() {
		String w1Esc, w2Esc, w3Esc, w4Esc, w5Esc, w6Esc, w7Esc, w8Esc, w9Esc;

		String query;
		VTASyntaxParsingResult parsingResult;
		Pair<VTASyntaxAnalyzer, TASQueryConfiguration> vtaSyntaxAnalyzerWithQueryConfig = this.generateConfiguredVTASyntaxAnalyzer();
		VTASyntaxAnalyzer vtaSyntaxAnalyzer = vtaSyntaxAnalyzerWithQueryConfig.a;
		TASQueryConfiguration queryConfiguration = vtaSyntaxAnalyzerWithQueryConfig.b;

		query = "C:making NEAR C:payments";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);

		w1Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "making"));
		w2Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "payments"));

		assertEquals(String.format("_query_:\"{!surround}text_en_customer:4W(%s,%s)\"", w1Esc, w2Esc), parsingResult.getSolrQuery());
		assertEquals(2, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.Customer)).count());


		query = "C:\"have to make\" NEAR C:payment";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);

		w1Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "have_to"));
		w2Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "to_make"));
		w3Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "make"));
		w4Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "payment"));

		assertEquals(String.format("_query_:\"{!surround}text_en_customer:4W(W(%s,%s,%s),%s)\"", w1Esc, w2Esc, w3Esc, w4Esc), parsingResult.getSolrQuery());
		assertEquals(2, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.Customer)).count());


		query = "C:\"have to make\" NEAR C:payment";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);

		w1Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "have_to"));
		w2Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "to_make"));
		w3Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "make"));
		w4Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "payment"));

		assertEquals(String.format("_query_:\"{!surround}text_en_customer:4W(W(%s,%s,%s),%s)\"", w1Esc, w2Esc, w3Esc, w4Esc), parsingResult.getSolrQuery());
		assertEquals(2, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.Customer)).count());

		vtaSyntaxAnalyzer.dispose();
	}


	@Test
	public void nearWithMoreThen2ExpressionsTest() {
		String w1Esc, w2Esc, w3Esc, w4Esc, w5Esc, w6Esc, w7Esc, w8Esc, w9Esc;

		String query;
		VTASyntaxParsingResult parsingResult;
		Pair<VTASyntaxAnalyzer, TASQueryConfiguration> vtaSyntaxAnalyzerWithQueryConfig = this.generateConfiguredVTASyntaxAnalyzer();
		VTASyntaxAnalyzer vtaSyntaxAnalyzer = vtaSyntaxAnalyzerWithQueryConfig.a;
		TASQueryConfiguration queryConfiguration = vtaSyntaxAnalyzerWithQueryConfig.b;

		query = "please AND answer NEAR security NEAR question";
		parsingResult = vtaSyntaxAnalyzer.parseQuery(query, queryConfiguration);

		w1Esc = DataUtils.escapeCharsForSolrQuery("please");
		w2Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "answer"));
		w3Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "security"));
		w4Esc = DataUtils.escapeCharsForSolrQuery(String.format("\"%s\"", "question"));

		assertEquals(String.format("+text_en_total:%s +(+_query_:\"%s\" +_query_:\"%s\")", w1Esc,
		                           String.format("{!surround}text_en_total:4W(%s,%s)",  w2Esc, w3Esc),
		                           String.format("{!surround}text_en_total:4W(%s,%s)",  w3Esc, w4Esc)),
		                           parsingResult.getSolrQuery());
		assertEquals(5, parsingResult.getTerms().stream().filter(t -> t.getSpeakerType().equals(SpeakerType.NoSPS)).count());
	}


		@Test
	public void analyzerProcessingExtractTokensTest(){
		TermTokensExtractor tokensExtractor = new TermTokensExtractor();
		tokensExtractor.initialize("en");

		List<String> highlightTokens = null;
		List<String> searchTokens = null;

		TokensExtractionResult tokens = tokensExtractor.getTokens(Arrays.asList("close", "the", "account"));

		searchTokens = tokens.getSearchTokens();
		assertNotNull(searchTokens);

		assertEquals("close_the", searchTokens.get(0));
		assertEquals("the_account", searchTokens.get(1));
		assertEquals("account", searchTokens.get(2));

		highlightTokens = tokens.getHighlightTokens();
		assertEquals("close_the", highlightTokens.get(0));
		assertEquals("the_account", highlightTokens.get(1));
		assertEquals("account", highlightTokens.get(2));

		tokens = tokensExtractor.getTokens(Arrays.asList("talk", "to", "my", "manager"));
		highlightTokens = tokens.getHighlightTokens();

		assertEquals("talk_to", highlightTokens.get(0));
		assertEquals("to_my", highlightTokens.get(1));
		assertEquals("my", highlightTokens.get(2));
		assertEquals("manager", highlightTokens.get(3));

		searchTokens = tokens.getSearchTokens();
		assertEquals("talk_to", searchTokens.get(0));
		assertEquals("to_my", searchTokens.get(1));
		assertEquals("my", searchTokens.get(2));
		assertEquals("manager", searchTokens.get(3));

		tokensExtractor.dispose();
	}


	@Test
	public void analyzerProcessingUpperCaseExtractTokensTest(){
		TermTokensExtractor tokensExtractor = new TermTokensExtractor();
		tokensExtractor.initialize("en");

		List<String> highlightTokens = null;
		List<String> searchTokens = null;

		TokensExtractionResult tokens = tokensExtractor.getTokens(Arrays.asList("Close", "The", "Account"));

		searchTokens = tokens.getSearchTokens();
		assertNotNull(searchTokens);

		assertEquals("close_the", searchTokens.get(0));
		assertEquals("the_account", searchTokens.get(1));
		assertEquals("account", searchTokens.get(2));

		TokensExtractionResult result = tokensExtractor.getTokens(Arrays.asList("You're"));
		assertNotNull(result);

		searchTokens = result.getSearchTokens();
		assertEquals("you're", searchTokens.get(0));

		tokensExtractor.dispose();
	}


	@Test
	public void analyzerWildCardPhrasesTest(){
		TermTokensExtractor tokensExtractor = new TermTokensExtractor();
		tokensExtractor.initialize("en");

		List<String> highlightTokens = null;
		List<String> searchTokens = null;

		TokensExtractionResult tokens = tokensExtractor.getTokens(Arrays.asList("close*", "acco?unt?"));

		tokensExtractor.dispose();
	}


	@Test
	public void wildCardTermTest() {
		TermParser termParser = new TermParser();
		termParser.initialize("en");

		termParser.validateTermWildCardValidityForSingleTermQuery("close*", 3);
		termParser.validateTermWildCardValidityForSingleTermQuery("clo?se*", 3);
		termParser.validateTermWildCardValidityForSingleTermQuery("close~1", 3);

		try {
			termParser.validateTermWildCardValidityForSingleTermQuery("*close", 3);
		} catch(Exception ex) {
			assertTrue(ex instanceof VTASyntaxProcessingException);
			assertEquals(ProcessingErrorType.WildCardPatternIsNotAllowedAsFirstCharacterOfTerm, ((VTASyntaxProcessingException)ex).getErrorType());
		}

		try {
			termParser.validateTermWildCardValidityForSingleTermQuery("cl*ose", 3);
		} catch(Exception ex) {
			assertTrue(ex instanceof VTASyntaxProcessingException);
			assertEquals(ProcessingErrorType.PrefixLengthInWildCardSearchIsTooShort, ((VTASyntaxProcessingException)ex).getErrorType());
		}

		try {
			termParser.validateTermWildCardValidityForSingleTermQuery("cl?ose", 3);
		} catch(Exception ex) {
			assertTrue(ex instanceof VTASyntaxProcessingException);
			assertEquals(ProcessingErrorType.PrefixLengthInWildCardSearchIsTooShort, ((VTASyntaxProcessingException)ex).getErrorType());
		}

		termParser.validateTermWildCardValidityForSingleTermQuery("clo?se", 3);
		termParser.validateTermWildCardValidityForSingleTermQuery("clo?s*e", 3);

		try {
			termParser.validateTermWildCardValidityForSingleTermQuery("?close", 3);
		} catch(Exception ex) {
			assertTrue(ex instanceof VTASyntaxProcessingException);
			assertEquals(ProcessingErrorType.WildCardPatternIsNotAllowedAsFirstCharacterOfTerm, ((VTASyntaxProcessingException)ex).getErrorType());
		}

		termParser.validateTermWildCardValidatityForPhraseOrProximityQuery(Arrays.asList("close", "my", "account"));
		termParser.validateTermWildCardValidatityForPhraseOrProximityQuery(Arrays.asList("speak", "with", "manager"));
		termParser.validateTermWildCardValidatityForPhraseOrProximityQuery(Arrays.asList("high", "bill"));

		try {
			termParser.validateTermWildCardValidatityForPhraseOrProximityQuery(Arrays.asList("?close"));
		} catch(Exception ex) {
			assertTrue(ex instanceof VTASyntaxProcessingException);
			assertEquals(ProcessingErrorType.WildCardPatternIsNotAllowedAsFirstCharacterOfTerm, ((VTASyntaxProcessingException)ex).getErrorType());
		}

		try {
			termParser.validateTermWildCardValidatityForPhraseOrProximityQuery(Arrays.asList("clos*", "account"));
		} catch (Exception ex) {
			assertTrue(ex instanceof VTASyntaxProcessingException);
			assertEquals(ProcessingErrorType.WildCardIsNotAllowedInPhrasesOrProximityQuery, ((VTASyntaxProcessingException)ex).getErrorType());
		}

		try {
			termParser.validateTermWildCardValidatityForPhraseOrProximityQuery(Arrays.asList("close", "account~1"));
		} catch (Exception ex) {
			assertTrue(ex instanceof VTASyntaxProcessingException);
			assertEquals(ProcessingErrorType.WildCardIsNotAllowedInPhrasesOrProximityQuery, ((VTASyntaxProcessingException)ex).getErrorType());
		}

		try {
			termParser.validateTermWildCardValidatityForPhraseOrProximityQuery(Arrays.asList("close", "account?"));
		} catch (Exception ex) {
			assertTrue(ex instanceof VTASyntaxProcessingException);
			assertEquals(ProcessingErrorType.WildCardIsNotAllowedInPhrasesOrProximityQuery, ((VTASyntaxProcessingException)ex).getErrorType());
		}

		termParser.dispose();
	}



	public static String bytesToHex(String s) {
		if (s == null) return "null";
		byte[] bytes = s.getBytes();
		final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for (int j = 0; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return "x'" + new String(hexChars) + "'";
	}



	private Pair<VTASyntaxAnalyzer, TASQueryConfiguration> generateConfiguredVTASyntaxAnalyzer(){
		TASQueryConfiguration queryGenerationConfig = new TASQueryConfiguration();
		queryGenerationConfig.setTextNoSPSField("text_en_total");
		queryGenerationConfig.setTextAgentField("text_en_agent");
		queryGenerationConfig.setTextCustomerField("text_en_customer");
		queryGenerationConfig.setEscapeValuesEnabled(true);
		queryGenerationConfig.setCommonGramsFilterEnabled(true);
		queryGenerationConfig.setWildCardPrefixMinLength(3);
		queryGenerationConfig.setLanguage("en");

		VTASyntaxAnalyzer vtaSyntaxAnalyzer = new VTASyntaxAnalyzer();

		return  new Pair<>(vtaSyntaxAnalyzer, queryGenerationConfig);
	}
}
