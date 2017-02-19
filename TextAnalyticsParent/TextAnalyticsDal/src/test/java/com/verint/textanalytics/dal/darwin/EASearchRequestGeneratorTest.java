package com.verint.textanalytics.dal.darwin;

import com.verint.textanalytics.common.exceptions.TextQueryExecutionErrorCode;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionException;
import com.verint.textanalytics.common.utils.DataUtils;
import com.verint.textanalytics.dal.darwin.vtasyntax.QueryTerm;
import com.verint.textanalytics.dal.darwin.vtasyntax.SpeakerType;
import com.verint.textanalytics.dal.darwin.vtasyntax.TermType;
import com.verint.textanalytics.dal.darwin.vtasyntax.VTASyntaxAnalyzer;
import com.verint.textanalytics.dal.rest.RestDataAccess;
import com.verint.textanalytics.model.facets.TextElementMetricType;
import com.verint.textanalytics.model.facets.TextElementType;
import com.verint.textanalytics.model.interactions.FilterFieldValue;
import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import com.verint.textanalytics.model.storedSearch.StoredSearchQuery;
import lombok.val;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PowerMockIgnore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@PowerMockIgnore("javax.net.ssl.*")
public class EASearchRequestGeneratorTest extends SolrRequestGeneratorTest {

	// Tests with Full Context

	private String sortProperty = "score";

	private String sortDirection = "desc";

	private final String interactionFieldsList= "id,channel,content_type,Meta_s_messageSourceType,date,Meta_dt_employeeStartTime,Meta_s_employeeTimeZone,Meta_ss_employeeNames,Meta_i_employeesMessages," +
			"Meta_l_avgEmployeeResponseTime,Meta_dt_customerStartTime,Meta_s_customerTimeZone,Meta_ss_customerNames,Meta_i_customerMessages,Meta_l_avgCustomerResponseTime," +
			"Meta_i_numberOfRobotMessages,Meta_i_messagesCount,Meta_l_handleTime,score,interaction_sentiment,categories,interaction_sentiment_ismixed";

	protected RequestGenerator createEARequestGenerator() {

		EASearchRequestGenerator eaSearchRequestGenerator = new EASearchRequestGenerator();
		SolrRequestGenerator sr = new SolrRequestGenerator();
		sr.setVtaSyntaxAnalyzer(new VTASyntaxAnalyzer());
		sr.setConfigurationManager(configurationManagerMock);
		sr.setTextEngineConfigurationService(textEngineConfigurationServiceMock);
		sr.setQueryParams(solrQueryParamsMock);
		sr.setTextElementsFacetMetricFields(this.getFieldsMetrics());

		sr.initialize();

		eaSearchRequestGenerator.setSolrRequestGenerator(sr);
		eaSearchRequestGenerator.setQueryParams(this.solrQueryParamsMock);
		eaSearchRequestGenerator.setTextEngineConfigurationService(textEngineConfigurationServiceMock);

		return eaSearchRequestGenerator;
	}

	@Override
	@Test
	public void getSearchInteractionsQueryTestFullContext() {
		SearchInteractionsContext searchContext = this.createFullSearchContext();

		// create new SolrRequestGenerator
		RequestGenerator requestGenerator = (RequestGenerator) this.createEARequestGenerator();

		val searchDocumentsQuery = requestGenerator.getSearchInteractionsQuery(tenant, channel, searchContext, language, pageStart, pageSize, sortProperty, sortDirection);

		assertNotNull(searchDocumentsQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, searchDocumentsQuery.getQueryPaths(), searchDocumentsQuery.getQueryParams(), false);

		//@formatter:off
		assertEquals(String.format("http://10.165.140.102:8983/solr/easearch/search/tenant/channel?"
				+ "q=+(text_en_total:%1$s) +(text_en_total:%2$s) AND (topics_f_total:%3$s OR topics_f_total:%4$s) AND (topics_f_total:%5$s) AND (relations_f_total:%6$s)"
				+ "&fq=content_type:PARENT"
		        + "&fq=date:[NOW\\-1MONTH\\/MINUTE TO NOW\\/MINUTE]"
		        + "&fq=(topics_f_total:%3$s OR topics_f_total:%4$s)"
		        + "&fq=(topics_f_total:%5$s)"
		        + "&fq=(relations_f_total:%6$s)"
				+ "&fq=(interaction_sentiment:\\-2)"
		        + "&fl=%7$s&sort=score desc&start=0&rows=50&wt=json"
		        + "&indent=true&json.nl=map",
		                                  DataUtils.escapeCharsForSolrQuery("term1"), 
		                                  DataUtils.escapeCharsForSolrQuery("term2"),
		                                  DataUtils.escapeCharsForSolrQuery("1/device"), 
		                                  DataUtils.escapeCharsForSolrQuery("1/software"),
		                                  DataUtils.escapeCharsForSolrQuery("3/device/iphone/iphone 5"),
		                                  DataUtils.escapeCharsForSolrQuery("1/Service action->Account"),
		                                  this.interactionFieldsList),
		             requestUrl);
		//@formatter:on
	}

	/*@Override
	@Test
	public void getTotalInteractionsQuantityQueryTestFullContext() {

		SearchInteractionsContext searchContext = this.createFullSearchContext();

		// create new SolrRequestGenerator
		RequestGenerator requestGenerator = (RequestGenerator) this.createEARequestGenerator();

		val searchDocumentsQuery = requestGenerator.getTotalInteractionsQuantityQuery(tenant, channel, searchContext, language);

		assertNotNull(searchDocumentsQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, searchDocumentsQuery.getQueryPaths(), searchDocumentsQuery.getQueryParams(), false);

		assertEquals("http://10.165.140.102:8983/solr/easearch/search/tenant/channel?q=*:*&fq=content_type:PARENT&rows=0&wt=json&indent=true&json.nl=map", requestUrl);
	}*/

	@Override
	@Test
	public void getCurrentSearchInteractionsQuantityQueryTestFullContext() {
		SearchInteractionsContext searchContext = this.createFullSearchContext();

		// create new SolrRequestGenerator
		RequestGenerator requestGenerator = (RequestGenerator) this.createEARequestGenerator();

		val searchDocumentsQuery = requestGenerator.getCurrentSearchInteractionsQuantityQuery(tenant, channel, searchContext, language);

		assertNotNull(searchDocumentsQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, searchDocumentsQuery.getQueryPaths(), searchDocumentsQuery.getQueryParams(), false);

		//@formatter:off
		assertEquals("http://10.165.140.102:8983/solr/easearch/search/tenant/channel?q=+(text_en_total:term1) +(text_en_total:term2)"
				             + "&fq=content_type:PARENT&fq=date:[NOW\\-1MONTH\\/MINUTE TO NOW\\/MINUTE]"
				             + "&fq=(topics_f_total:1\\/device OR topics_f_total:1\\/software)"
				             + "&fq=(topics_f_total:3\\/device\\/iphone\\/iphone\\ 5)"
				             + "&fq=(relations_f_total:1\\/Service\\ action\\->Account)"
				             + "&fq=(interaction_sentiment:\\-2)"
							 + "&rows=0"
				             + "&wt=json&indent=true&json.nl=map",
		             requestUrl);
		//@formatter:on
	}

	@Override
	@Test
	public void getInteractionPreviewTest() {

		// create new SearchInteractionsContext
		String baseUrl = "http://10.165.140.102:8983";
		String tenant = "tenant";
		String channel = "channel";
		String interactionId = "100";

		// create new SolrRequestGenerator
		RequestGenerator requestGenerator = (RequestGenerator) this.createEARequestGenerator();
		val restDataAccess = this.getRestDataAccess();

		val interactionPreviewQueryParams = requestGenerator.getInteractionPreviewQuery(tenant, channel, interactionId);

		String requestUrl = restDataAccess.getRequestUrl(baseUrl, interactionPreviewQueryParams.getQueryPaths(), interactionPreviewQueryParams.getQueryParams(), false);

		assertEquals(String.format("http://10.165.140.102:8983/easearch/search/tenant/channel?q=*:*" +
				                           "&fq=content_type:PARENT&fq=(id:100)" +
				                           "&fl=%s" +
				                           "&rows=1" +
				                           "&wt=json&indent=true&json.nl=map",
		                           this.interactionFieldsList + ",[child parentFilter=content_type:PARENT limit=1000]"),
					 requestUrl);

		try {
			requestGenerator.getInteractionPreviewQuery(null, channel, interactionId);
			fail("No exception then when tenant parameter was null");
		} catch (Exception e) {
			assertEquals(e.getClass(), TextQueryExecutionException.class);

			TextQueryExecutionException textQueryException = (TextQueryExecutionException) e;
			assertEquals(textQueryException.getAppExecutionErrorCode(), TextQueryExecutionErrorCode.TextQueryGenerationError);
		}
	}

	// Tests with Empty Context

	@Override
	@Test
	public void getSearchInteractionsQueryTestEmptyContext() {
		SearchInteractionsContext searchContext = new SearchInteractionsContext();

		// create new SolrRequestGenerator
		RequestGenerator requestGenerator = (RequestGenerator) this.createEARequestGenerator();

		val searchDocumentsQuery = requestGenerator.getSearchInteractionsQuery(tenant, channel, searchContext, language, pageStart, pageSize, sortProperty, sortDirection);

		assertNotNull(searchDocumentsQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, searchDocumentsQuery.getQueryPaths(), searchDocumentsQuery.getQueryParams(), false);

		assertEquals(String.format("http://10.165.140.102:8983/solr/easearch/search/tenant/channel?q=*:*&fq=content_type:PARENT"
				                           + "&fl=%s"
				                           + "&sort=score desc"
				                           + "&start=0"
				                           + "&rows=50"
				                           + "&wt=json"
				                           + "&indent=true"
				                           + "&json.nl=map",
		                           this.interactionFieldsList + ",[child parentFilter=content_type:PARENT limit=1]"),
				requestUrl);

		try {
			requestGenerator.getSearchInteractionsQuery(null, channel, searchContext, language, pageStart, pageSize, sortProperty, sortDirection);
			fail("No exception then when tenant parameter was null");
		} catch (Exception e) {
			assertEquals(e.getClass(), TextQueryExecutionException.class);

			TextQueryExecutionException textQueryException = (TextQueryExecutionException) e;
			assertEquals(textQueryException.getAppExecutionErrorCode(), TextQueryExecutionErrorCode.TextQueryGenerationError);
		}
	}

	/*@Override
	@Test
	public void getTotalInteractionsQuantityQueryTestEmptyContext() {

		SearchInteractionsContext searchContext = new SearchInteractionsContext();

		// create new SolrRequestGenerator
		RequestGenerator requestGenerator = (RequestGenerator) this.createEARequestGenerator();

		val searchDocumentsQuery = requestGenerator.getTotalInteractionsQuantityQuery(tenant, channel, searchContext, language);

		assertNotNull(searchDocumentsQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, searchDocumentsQuery.getQueryPaths(), searchDocumentsQuery.getQueryParams(), false);

		assertEquals("http://10.165.140.102:8983/solr/easearch/search/tenant/channel?q=*:*" +
				             "&fq=content_type:PARENT" +
				             "&rows=0" +
				             "&wt=json&indent=true&json.nl=map", requestUrl);

		try {
			requestGenerator.getTotalInteractionsQuantityQuery(null, channel, searchContext, language);
			fail("No exception then when tenant parameter was null");
		} catch (Exception e) {
			assertEquals(e.getClass(), TextQueryExecutionException.class);

			TextQueryExecutionException textQueryException = (TextQueryExecutionException) e;
			assertEquals(textQueryException.getAppExecutionErrorCode(), TextQueryExecutionErrorCode.TextQueryGenerationError);
		}
	}*/

	@Override
	@Test
	public void getCurrentSearchInteractionsQuantityQueryTestEmptyContext() {

		SearchInteractionsContext searchContext = new SearchInteractionsContext();

		// create new SolrRequestGenerator
		RequestGenerator requestGenerator = (RequestGenerator) this.createEARequestGenerator();

		val searchDocumentsQuery = requestGenerator.getCurrentSearchInteractionsQuantityQuery(tenant, channel, searchContext, language);

		assertNotNull(searchDocumentsQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, searchDocumentsQuery.getQueryPaths(), searchDocumentsQuery.getQueryParams(), false);

		assertEquals("http://10.165.140.102:8983/solr/easearch/search/tenant/channel?q=*:*" +
				             "&fq=content_type:PARENT" +
				             "&rows=0" +
				             "&wt=json&indent=true&json.nl=map", requestUrl);

		try {
			requestGenerator.getCurrentSearchInteractionsQuantityQuery(null, channel, searchContext, language);
			fail("No exception then when tenant parameter was null");
		} catch (Exception e) {
			assertEquals(e.getClass(), TextQueryExecutionException.class);

			TextQueryExecutionException textQueryException = (TextQueryExecutionException) e;
			assertEquals(textQueryException.getAppExecutionErrorCode(), TextQueryExecutionErrorCode.TextQueryGenerationError);
		}
	}

	// Regular tests

	@Override
	@Test
	public void getInteractionHighlightsQueryTest() {

		// create new SolrRequestGenerator
		RestDataAccess restDataAccess = this.getRestDataAccess();
		RequestGenerator requestGenerator = (RequestGenerator) this.createEARequestGenerator();

		String tenant = "tenant1";
		String channel = "1271";
		String language = "en";
		com.verint.textanalytics.model.interactions.SpeakerType speaker = com.verint.textanalytics.model.interactions.SpeakerType.Agent;

		List<QueryTerm> terms = new ArrayList<QueryTerm>();
		QueryTerm term = new QueryTerm("iphone", TermType.Word, SpeakerType.Agent, "iphone", Arrays.asList("iphone"));
		terms.add(term);

		List<FilterFieldValue> interactionIds = new ArrayList<FilterFieldValue>();
		interactionIds.add(new FilterFieldValue("100", ""));

		val agentHighlightsQuery = requestGenerator.getInteractionsHighlightsQuery(tenant, channel, interactionIds,
		                                                                             com.verint.textanalytics.model.interactions.SpeakerType.Agent, terms, language);
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, agentHighlightsQuery.getQueryPaths(), agentHighlightsQuery.getQueryParams(), false);

		assertNotNull(agentHighlightsQuery);
		assertEquals("http://10.165.140.102:8983/solr/easearch/search/tenant1/1271?q=*:*"
				             + "&fq=content_type:CHILD"
				             + "&fq=(id:100)"
				             + "&fq=(speaker_type:agent)"
				             + "&hl=true"
				             + "&hl.fl=text_en"
				             + "&hl.q=(iphone)"
				             + "&rows=10000"
				             + "&wt=json&indent=true&json.nl=map",
				requestUrl);

		terms.clear();
		terms.add(new QueryTerm("iphone", TermType.Word, SpeakerType.Customer, "iphone", Arrays.asList("iphone")));
		val customerHighlightsQuery = requestGenerator.getInteractionsHighlightsQuery(tenant, channel, interactionIds,
		                                                                                com.verint.textanalytics.model.interactions.SpeakerType.Customer, terms, language);
		requestUrl = restDataAccess.getRequestUrl(baseUrl, customerHighlightsQuery.getQueryPaths(), customerHighlightsQuery.getQueryParams(), false);

		assertNotNull(customerHighlightsQuery);
		assertEquals("http://10.165.140.102:8983/solr/easearch/search/tenant1/1271?q=*:*"
		             + "&fq=content_type:CHILD"
		             + "&fq=(id:100)"
				     + "&fq=(speaker_type:customer)"
				     + "&hl=true"
				     + "&hl.fl=text_en"
		             + "&hl.q=(iphone)"
				     + "&rows=10000&wt=json&indent=true&json.nl=map",
				requestUrl);

		terms.clear();
		terms.add(new QueryTerm("iphone", TermType.Word, SpeakerType.NoSPS, "iphone", Arrays.asList("iphone")));

		val noSPSHighlightsQuery = requestGenerator.getInteractionsHighlightsQuery(tenant, channel, interactionIds,
		                                                                             com.verint.textanalytics.model.interactions.SpeakerType.Unknown, terms, language);
		requestUrl = restDataAccess.getRequestUrl(baseUrl, noSPSHighlightsQuery.getQueryPaths(), noSPSHighlightsQuery.getQueryParams(), false);

		assertNotNull(noSPSHighlightsQuery);
		assertEquals("http://10.165.140.102:8983/solr/easearch/search/tenant1/1271?q=*:*"
				             + "&fq=content_type:CHILD"
				             + "&fq=(id:100)"
				             + "&hl=true"
				             + "&hl.fl=text_en"
				             + "&hl.q=(iphone)"
				             + "&rows=10000&wt=json&indent=true&json.nl=map", requestUrl);

		try {
			requestGenerator.getInteractionsHighlightsQuery(null, channel, interactionIds, speaker, terms, language);
			fail("No exception then when tenant parameter was null");
		} catch (Exception e) {
			assertEquals(e.getClass(), TextQueryExecutionException.class);

			TextQueryExecutionException textQueryException = (TextQueryExecutionException) e;
			assertEquals(textQueryException.getAppExecutionErrorCode(), TextQueryExecutionErrorCode.TextQueryGenerationError);
		}
	}

	@Test
	public void getEntitiesSentimentsQueryTest() {
		SearchInteractionsContext searchContext = this.createFullSearchContext();

		// create new SolrRequestGenerator
		RequestGenerator requestGenerator = (RequestGenerator) this.createEARequestGenerator();

		val searchDocumentsQuery = requestGenerator.getTextElementsChildrenSentimentQuery(tenant, channel, searchContext, language, TextElementType.Entities, "1/",
		                                                                                  TextElementMetricType.AvgHandleTime, 50);

		assertNotNull(searchDocumentsQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(eaSearchBaseUrl, searchDocumentsQuery.getQueryPaths(), searchDocumentsQuery.getQueryParams(), false);

		//@formatter:off
		assertEquals(String.format("http://10.165.140.102:9000/easearch/search/tenant/channel/InteractionSentimentChart/topics?q=+(text_en_total:%1$s) +(text_en_total:%2$s)" +
				                "&fq=content_type:PARENT" +
				                "&fq=date:[NOW\\-1MONTH\\/MINUTE TO NOW\\/MINUTE]" +
				                "&fq=(topics_f_total:%3$s OR topics_f_total:%4$s)" +
				                "&fq=(topics_f_total:%5$s)" +
				                "&fq=(relations_f_total:%6$s)&fq=(interaction_sentiment:\\-2)" +
		                        "&solrSort=avg(Meta_l_handleTime) desc&prefix=1/" +
				                "&wt=json&indent=true&json.nl=map&limit=50&leaves=false",
		                           DataUtils.escapeCharsForSolrQuery("term1"),
		                           DataUtils.escapeCharsForSolrQuery("term2"),
		                           DataUtils.escapeCharsForSolrQuery("1/device"),
		                           DataUtils.escapeCharsForSolrQuery("1/software"),
		                           DataUtils.escapeCharsForSolrQuery("3/device/iphone/iphone 5"),
		                           DataUtils.escapeCharsForSolrQuery("1/Service action->Account")),
		             requestUrl);
		//@formatter:on
	}

	@Test
	public void getEntitiesSentimentsOnSameUtteranceQueryTest() {
		SearchInteractionsContext searchContext = this.createFullSearchContext();

		// create new SolrRequestGenerator
		RequestGenerator requestGenerator = (RequestGenerator) this.createEARequestGenerator();

		val searchDocumentsQuery = requestGenerator.getTextElementsChildrenSentimentQuery(tenant, channel, searchContext, language, TextElementType.Entities, "1/",
		                                                                                  TextElementMetricType.AvgCustomerMessages, 50);

		assertNotNull(searchDocumentsQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(eaSearchBaseUrl, searchDocumentsQuery.getQueryPaths(), searchDocumentsQuery.getQueryParams(), false);

		//@formatter:off
		assertEquals(String.format("http://10.165.140.102:9000/easearch/search/tenant/channel/InteractionSentimentChart/topics?q=+(text_en_total:%1$s) +(text_en_total:%2$s)"
											+ "&fq=content_type:PARENT"
		                                    + "&fq=date:[NOW\\-1MONTH\\/MINUTE TO NOW\\/MINUTE]"
		                                    + "&fq=(topics_f_total:%3$s OR topics_f_total:%4$s)"
		                                    + "&fq=(topics_f_total:%5$s)"
		                                    + "&fq=(relations_f_total:%6$s)&fq=(interaction_sentiment:\\-2)"
		                                    + "&solrSort=avg(Meta_i_customerMessages) desc"
				                            + "&prefix=1/"
											+ "&wt=json&indent=true&json.nl=map&limit=50&leaves=false",
		                           DataUtils.escapeCharsForSolrQuery("term1"),
		                           DataUtils.escapeCharsForSolrQuery("term2"),
		                           DataUtils.escapeCharsForSolrQuery("1/device"),
		                           DataUtils.escapeCharsForSolrQuery("1/software"),
		                           DataUtils.escapeCharsForSolrQuery("3/device/iphone/iphone 5"),
		                           DataUtils.escapeCharsForSolrQuery("1/Service action->Account")),
		             requestUrl);
		//@formatter:on
	}

	@Test
	public void getRelationsSentimentsQueryTest() {
		SearchInteractionsContext searchContext = this.createFullSearchContext();

		// create new SolrRequestGenerator
		RequestGenerator requestGenerator = (RequestGenerator) this.createEARequestGenerator();

		val searchDocumentsQuery = requestGenerator.getTextElementsChildrenSentimentQuery(tenant, channel, searchContext, language, TextElementType.Relations, "1/",
		                                                                                  TextElementMetricType.AvgHandleTime, 50);

		assertNotNull(searchDocumentsQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(eaSearchBaseUrl, searchDocumentsQuery.getQueryPaths(), searchDocumentsQuery.getQueryParams(), false);

		//@formatter:off
		assertEquals(String.format("http://10.165.140.102:9000/easearch/search/tenant/channel/InteractionSentimentChart/relations?"
				                           + "q=+(text_en_total:%1$s) +(text_en_total:%2$s)"
				                           + "&fq=content_type:PARENT&fq=date:[NOW\\-1MONTH\\/MINUTE TO NOW\\/MINUTE]"
				                           + "&fq=(topics_f_total:%3$s OR topics_f_total:%4$s)"
				                           + "&fq=(topics_f_total:%5$s)"
				                           + "&fq=(relations_f_total:%6$s)"
				                           + "&fq=(interaction_sentiment:\\-2)"
				                           + "&solrSort=avg(Meta_l_handleTime) desc"
				                           + "&prefix=1/"
				                           + "&wt=json&indent=true&json.nl=map&limit=50&leaves=false",
		                           DataUtils.escapeCharsForSolrQuery("term1"),
		                           DataUtils.escapeCharsForSolrQuery("term2"),
		                           DataUtils.escapeCharsForSolrQuery("1/device"),
		                           DataUtils.escapeCharsForSolrQuery("1/software"),
		                           DataUtils.escapeCharsForSolrQuery("3/device/iphone/iphone 5"),
		                           DataUtils.escapeCharsForSolrQuery("1/Service action->Account")),
		             requestUrl);
		//@formatter:on
	}

	@Test
	public void getRelationsSentimentsOmSameUtteranceQueryTest() {
		SearchInteractionsContext searchContext = this.createFullSearchContext();

		// create new SolrRequestGenerator
		RequestGenerator requestGenerator = (RequestGenerator) this.createEARequestGenerator();

		val searchDocumentsQuery = requestGenerator.getTextElementsChildrenSentimentQuery(tenant, channel, searchContext, language, TextElementType.Relations, "1/",
		                                                                                  TextElementMetricType.AvgCustomerMessages, 50);

		assertNotNull(searchDocumentsQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(eaSearchBaseUrl, searchDocumentsQuery.getQueryPaths(), searchDocumentsQuery.getQueryParams(), false);

		//@formatter:off
		assertEquals(String.format("http://10.165.140.102:9000/easearch/search/tenant/channel/InteractionSentimentChart/relations?" +
				                           "q=+(text_en_total:%1$s) +(text_en_total:%2$s)" +
				                           "&fq=content_type:PARENT&fq=date:[NOW\\-1MONTH\\/MINUTE TO NOW\\/MINUTE]" +
				                           "&fq=(topics_f_total:%3$s OR topics_f_total:%4$s)" +
				                           "&fq=(topics_f_total:%5$s)" +
				                           "&fq=(relations_f_total:%6$s)&fq=(interaction_sentiment:\\-2)" +
		                                   "&solrSort=avg(Meta_i_customerMessages) desc&prefix=1/" +
				                           "&wt=json&indent=true&json.nl=map&limit=50&leaves=false",
		                           DataUtils.escapeCharsForSolrQuery("term1"),
		                           DataUtils.escapeCharsForSolrQuery("term2"),
		                           DataUtils.escapeCharsForSolrQuery("1/device"),
		                           DataUtils.escapeCharsForSolrQuery("1/software"),
		                           DataUtils.escapeCharsForSolrQuery("3/device/iphone/iphone 5"),
		                           DataUtils.escapeCharsForSolrQuery("1/Service action->Account")),
		             requestUrl);
		//@formatter:on
	}

	// Entity Trend tests
	/*@Test
	public void getEntityTrendsQuery() {

		String periodName = "WEEKLY";
		String entityValue = "";
		String sortProperty = "relativeChange";
		String sortDirection = "DESC";
		String baseDate = null;
		Integer limitTo = 10;
		RequestGenerator requestGenerator = (RequestGenerator) this.createEARequestGenerator();

		// http://10.165.140.102:9000/search/DataFiller/TelcoDefaultChannel/trends/topics_f
		// ?q=*:*&period=WEEKLY&prefix=2/Program&limit=50&sort=relativeChange
		// ASC
		val request = requestGenerator.getTextElementsTrendsQuery(tenant, channel, TrendType.Entities, periodName, baseDate, language, entityValue, sortProperty, sortDirection,
		                                                          limitTo, SpeakerQueryType.Any);

		assertEquals(request.getQueryPaths().get(0), "search");
		assertEquals(request.getQueryPaths().get(1), "tenant");
		assertEquals(request.getQueryPaths().get(2), "channel");
		assertEquals(request.getQueryPaths().get(3), "trends");
		assertEquals(request.getQueryPaths().get(4), "topics");

		assertTrue(request.getQueryParams().get("period").stream().findFirst().get().equals(periodName));
		assertTrue(request.getQueryParams().get("prefix").stream().findFirst().get().equals("1/"));
		assertTrue(request.getQueryParams().get("sort").stream().findFirst().get().equals("relativeChange DESC"));
		assertTrue(request.getQueryParams().get("limit").stream().findFirst().get().equals("10"));
	}*/

	// Entity Trend tests
	/*@Test
	public void getEntityTrendsQueryWithBaseDate() {

		String periodName = "WEEKLY";
		String entityValue = "";
		String sortProperty = "relativeChange";
		String sortDirection = "DESC";
		String baseDate = "2016-03-14T22:00:00Z";
		Integer limitTo = 10;
		RequestGenerator requestGenerator = (RequestGenerator) this.createEARequestGenerator();

		// http://10.165.140.102:9000/search/DataFiller/TelcoDefaultChannel/trends/topics_f
		// ?q=*:*&period=WEEKLY&prefix=2/Program&limit=50&sort=relativeChange
		// ASC
		val request = requestGenerator.getTextElementsTrendsQuery(tenant, channel, TrendType.Entities, periodName, baseDate, language, entityValue, sortProperty, sortDirection,
		                                                          limitTo, SpeakerQueryType.Any);

		assertEquals(request.getQueryPaths().get(0), "search");
		assertEquals(request.getQueryPaths().get(1), "tenant");
		assertEquals(request.getQueryPaths().get(2), "channel");
		assertEquals(request.getQueryPaths().get(3), "trends");
		assertEquals(request.getQueryPaths().get(4), "topics");

		assertEquals(request.getQueryParams().get("period").get(0), periodName);
		assertEquals(request.getQueryParams().get("prefix").get(0), "1/");
		assertEquals(request.getQueryParams().get("sort").get(0), "relativeChange DESC");
		assertEquals(request.getQueryParams().get("limit").get(0), "10");
		assertEquals(request.getQueryParams().get("baseDate").get(0), "2016-03-14T22:00:00Z/MINUTE");
	}*/

	// Entity Trend tests
	/*@Test
	public void getEntityTrendsQuery2ndLevel() {

		String periodName = "DAILY";
		String entityValue = "1/Manufacturer";
		String sortProperty = "name";
		String sortDirection = "ASC";
		String baseDate = null;
		Integer limitTo = 30;
		RequestGenerator requestGenerator = (RequestGenerator) this.createEARequestGenerator();

		// http://10.165.140.102:9000/search/DataFiller/TelcoDefaultChannel/trends/topics_f
		// ?q=*:*&period=WEEKLY&prefix=2/Progra&limit=50&sort=relativeChange ASC
		val request = requestGenerator.getTextElementsTrendsQuery(tenant, channel, TrendType.Entities, periodName, baseDate, language, entityValue, sortProperty, sortDirection,
		                                                          limitTo, SpeakerQueryType.Any);

		assertEquals(request.getQueryPaths().get(0), "search");
		assertEquals(request.getQueryPaths().get(1), "tenant");
		assertEquals(request.getQueryPaths().get(2), "channel");
		assertEquals(request.getQueryPaths().get(3), "trends");
		assertEquals(request.getQueryPaths().get(4), "topics");

		assertTrue(request.getQueryParams().get("period").contains(periodName));
		assertTrue(request.getQueryParams().get("prefix").contains("2/Manufacturer"));
		assertTrue(request.getQueryParams().get("sort").contains("name ASC"));
		assertTrue(request.getQueryParams().get("limit").contains("30"));
	}*/

	// Entity Trend tests
	/*@Test
	public void getEntityTrendsQueryMonthly() {

		String periodName = "MONTHLY";
		String entityValue = "1/Manufacturer";
		String sortProperty = "name";
		String sortDirection = "DESC";
		String baseDate = null;
		Integer limitTo = 100;
		RequestGenerator requestGenerator = (RequestGenerator) this.createEARequestGenerator();

		// http://10.165.140.102:9000/search/DataFiller/TelcoDefaultChannel/trends/topics_f
		// ?q=*:*&period=WEEKLY&prefix=2/Progra&limit=50&sort=relativeChange ASC
		val request = requestGenerator.getTextElementsTrendsQuery(tenant, channel, TrendType.Entities, periodName, baseDate, language, entityValue, sortProperty, sortDirection, limitTo, SpeakerQueryType.Any);

		assertEquals(request.getQueryPaths().get(0), "search");
		assertEquals(request.getQueryPaths().get(1), "tenant");
		assertEquals(request.getQueryPaths().get(2), "channel");
		assertEquals(request.getQueryPaths().get(3), "trends");
		assertEquals(request.getQueryPaths().get(4), "topics");

		assertTrue(request.getQueryParams().get("period").contains(periodName));
		assertTrue(request.getQueryParams().get("prefix").contains("2/Manufacturer"));
		assertTrue(request.getQueryParams().get("sort").contains("name DESC"));
		assertTrue(request.getQueryParams().get("limit").contains("100"));
	}*/

	@Test
	public void getCreateTenantQueryTest() {

		RequestGenerator requestGenerator = (RequestGenerator) this.createEARequestGenerator();

		val request = requestGenerator.getCreateTenantQuery("UnitTestTenant");

		assertEquals(request.getQueryPaths().size(), 4);

		assertEquals(request.getQueryPaths().get(0), "easearch");
		assertEquals(request.getQueryPaths().get(1), "admin");
		assertEquals(request.getQueryPaths().get(2), "tenant");
		assertEquals(request.getQueryPaths().get(3), "UnitTestTenant");

	}

	@Test
	public void getDeleteTenantQueryTest() {
		RequestGenerator requestGenerator = (RequestGenerator) this.createEARequestGenerator();

		val request = requestGenerator.getDeleteTenantQuery("UnitTestTenant");

		assertEquals(request.getQueryPaths().size(), 4);

		assertEquals(request.getQueryPaths().get(0), "easearch");
		assertEquals(request.getQueryPaths().get(1), "admin");
		assertEquals(request.getQueryPaths().get(2), "tenant");
		assertEquals(request.getQueryPaths().get(3), "UnitTestTenant");
	}

	@Test
	public void getSearchInteractionsQueryForCategoryTest() {

		SearchInteractionsContext searchContext = this.createFullSearchContext();
		searchContext.getTerms().add("at&t");

		// create new SolrRequestGenerator
		RequestGenerator requestGenerator = (RequestGenerator) this.createEARequestGenerator();

		StoredSearchQuery queryForCategory = requestGenerator.getSearchInteractionsQueryForCategory(tenant, channel, searchContext, language, false);

		assertNotNull(queryForCategory);

		//@formatter:off
		assertEquals(String.format("q=+(text_en_total:%1$s) +(text_en_total:%2$s) +(text_en_total:%3$s)"
				+ "&fq=content_type:PARENT"
		        + "&fq=date:[NOW\\-1MONTH\\/MINUTE TO NOW\\/MINUTE]"
		        + "&fq=(topics_f_total:%4$s OR topics_f_total:%5$s)"
		        + "&fq=(topics_f_total:%6$s)"
		        + "&fq=(relations_f_total:%7$s)&fq=(interaction_sentiment:\\-2)",
		                           DataUtils.escapeCharsForSolrQuery("term1"),
		                           DataUtils.escapeCharsForSolrQuery("term2"),
		                           DataUtils.escapeCharsForSolrQuery("at&t"),
		                           DataUtils.escapeCharsForSolrQuery("1/device"),
		                           DataUtils.escapeCharsForSolrQuery("1/software"),
		                           DataUtils.escapeCharsForSolrQuery("3/device/iphone/iphone 5"),
		                           DataUtils.escapeCharsForSolrQuery("1/Service action->Account")),
		                                  queryForCategory.getDebugQuery());
		//@formatter:on
	}
}
