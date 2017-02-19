package com.verint.textanalytics.dal.darwin;

import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.dal.darwin.vtasyntax.VTASyntaxAnalyzer;
import com.verint.textanalytics.dal.rest.RestDataAccess;
import com.verint.textanalytics.model.analyze.FieldMetric;
import com.verint.textanalytics.model.documentSchema.DocumentHierarchyType;
import com.verint.textanalytics.model.documentSchema.FieldDataType;
import com.verint.textanalytics.model.documentSchema.TextSchemaField;
import com.verint.textanalytics.model.facets.SpeakerQueryType;
import com.verint.textanalytics.model.facets.TextElementMetricType;
import com.verint.textanalytics.model.facets.TextElementType;
import com.verint.textanalytics.model.interactions.*;
import com.verint.textanalytics.model.interactions.SearchTerm.TermType;
import lombok.val;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Solr Request Generator Test.
 *
 * @author imor
 */
@PowerMockIgnore({"javax.management.*", "javax.net.ssl.*"})
@RunWith(PowerMockRunner.class)
public class SolrRequestGeneratorFacetsTest extends BaseTest {

	protected String baseUrl = "http://10.165.140.102:8983/solr";
	protected String eaSearchBaseUrl = "http://10.165.140.102:9000/";
	protected String tenant = "tenant";
	protected String channel = "channel";
	protected String language = "en";
	protected int pageStart = 0;
	protected int pageSize = 50;
	protected int textElementsLimit = 30;

	@Mock
	protected ApplicationConfiguration appConfigMock;

	@Mock
	protected ConfigurationManager configurationManagerMock;

	@Mock
	protected TextEngineSchemaService textEngineConfigurationServiceMock;

	@Mock
	protected SolrQueryParameters solrQueryParamsMock;

	protected List<FieldMetric> metricFields;

	private String sortProperty = "score";

	private String sortDirection = "desc";

	public SolrRequestGeneratorFacetsTest() {
		MockitoAnnotations.initMocks(this);

		Mockito.when(solrQueryParamsMock.getResponseFormat()).thenReturn("json");
		Mockito.when(solrQueryParamsMock.getResponseIdentation()).thenReturn("true");
		Mockito.when(solrQueryParamsMock.getFacetMinCount()).thenReturn(1);
		Mockito.when(solrQueryParamsMock.getSearchInteractionsResultSetSize()).thenReturn(10000);
		Mockito.when(solrQueryParamsMock.getChildDocumentsForParentLimit()).thenReturn(1000);
		Mockito.when(solrQueryParamsMock.getTopicsFacetLimit()).thenReturn(-1);
		Mockito.when(solrQueryParamsMock.getFacetLimit()).thenReturn(20);

		SearchTerm[] term1Arr = new SearchTerm[] { new SearchTerm().setTerm("term1").setSpeakerType(SpeakerType.Unknown).setTermType(TermType.Word) };
		SearchTerm[] term2Arr = new SearchTerm[] { new SearchTerm().setTerm("term2").setSpeakerType(SpeakerType.Unknown).setTermType(TermType.Word) };
		SearchTerm[] term3Arr = new SearchTerm[] { new SearchTerm().setTerm("at&t").setSpeakerType(SpeakerType.Unknown).setTermType(TermType.Word) };

		Mockito.when(appConfigMock.getDarwinRestRequestTimeout()).thenReturn(60);
		Mockito.when(appConfigMock.getAutoCompleteLimitForQuery()).thenReturn(40);
		Mockito.when(appConfigMock.getBypassTrendsThreshold()).thenReturn("false");
		Mockito.when(appConfigMock.getFacetThreadLimit()).thenReturn(-1);
		Mockito.when(appConfigMock.getHttpClientMaxConnectionsPerRoute()).thenReturn(100);
		Mockito.when(appConfigMock.getHttpClientMaxConnectionsTotal()).thenReturn(100);
		Mockito.when(appConfigMock.getDateRangeRound()).thenReturn(true);
		Mockito.when(appConfigMock.getDateRangeRoundUpTo()).thenReturn("MINUTE");
		Mockito.when(configurationManagerMock.getApplicationConfiguration()).thenReturn(appConfigMock);

		TextSchemaField topicsSchemaField = new TextSchemaField().setName("topic_f")
		                                                         .setDisplayFieldName("Entities")
		                                                         .setFieldDataType(FieldDataType.Text)
		                                                         .setDocumentHierarchyType(DocumentHierarchyType.Interaction);

		TextSchemaField relationsShchemaField = new TextSchemaField().setName("relations_f")
		                                                             .setDisplayFieldName("Relations")
		                                                             .setFieldDataType(FieldDataType.Text)
		                                                             .setDocumentHierarchyType(DocumentHierarchyType.Interaction);

		Mockito.when(textEngineConfigurationServiceMock.getTextSchemaField(tenant, channel, "topic_f")).thenReturn(topicsSchemaField);
		Mockito.when(textEngineConfigurationServiceMock.isValid()).thenReturn(true);

		Mockito.when(textEngineConfigurationServiceMock.isParentDocumentField(tenant, channel, "topics_f")).thenReturn(true);
		Mockito.when(textEngineConfigurationServiceMock.isChildDocumentField(tenant, channel, "topics_f")).thenReturn(true);

		Mockito.when(textEngineConfigurationServiceMock.getTextSchemaField(tenant, channel, "topic_f")).thenReturn(relationsShchemaField);
		Mockito.when(textEngineConfigurationServiceMock.isParentDocumentField(tenant, channel, "relations_f")).thenReturn(true);
		Mockito.when(textEngineConfigurationServiceMock.isChildDocumentField(tenant, channel, "relations_f")).thenReturn(true);

		Mockito.when(textEngineConfigurationServiceMock.isParentDocumentField(tenant, channel, "interaction_sentiment")).thenReturn(true);
		Mockito.when(textEngineConfigurationServiceMock.isChildDocumentField(tenant, channel, "interaction_sentiment")).thenReturn(false);

		Mockito.when(textEngineConfigurationServiceMock.isParentDocumentField(tenant, channel, "speaker_type")).thenReturn(false);
		Mockito.when(textEngineConfigurationServiceMock.isChildDocumentField(tenant, channel, "speaker_type")).thenReturn(true);

		metricFields = this.getFieldsMetrics();
	}


	@Test
	public void getEntitiesFacetQueryTest() {

		SearchInteractionsContext searchContext = createFullSearchContext();

		// create new SolrRequestGenerator
		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();
		TextElementMetricType volumeMetric = TextElementMetricType.Volume;

		val entitiesFacetQuery = solrRequestGenerator.getTextElementsFacetWithStatsQuery(tenant, channel, searchContext, language, TextElementType.Entities, "1",
		                                                                                 Arrays.asList(volumeMetric), volumeMetric,
		                                                                                 SpeakerQueryType.Any, false, textElementsLimit);

		assertNotNull(entitiesFacetQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, entitiesFacetQuery.getQueryPaths(), entitiesFacetQuery.getQueryParams(), false);

		//@formatter:off
		assertEquals("http://10.165.140.102:8983/solr/tenant/select?q=+(text_en_total:term1) +(text_en_total:term2)" +
						"&fq=channel:channel&fq=content_type:PARENT" +
				        "&fq=date:[NOW\\-1MONTH\\/MINUTE TO NOW\\/MINUTE]&fq=(topics_f_total:1\\/device OR topics_f_total:1\\/software)" +
				        "&fq=(topics_f_total:3\\/device\\/iphone\\/iphone\\ 5)" +
				        "&fq=(relations_f_total:1\\/Service\\ action\\->Account)&fq=(interaction_sentiment:\\-2)" +
						"&json.facet={textElementFacet: {type : terms, method : dv, field : topics_f_total, prefix : \"1\", mincount : 1, limit : 30 }}" +
						"&rows=0&wt=json&indent=true&json.nl=map",
		             requestUrl);
		//@formatter:on
	}

	@Test
	@Ignore
	// Same utterance mode currently is not supported
	public void getEntitiesFacetQueryOnSameUtteranceTest() {

		SearchInteractionsContext searchContext = createFullSearchContext();

		// create new SolrRequestGenerator
		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();
		TextElementMetricType volume = TextElementMetricType.Volume;

		val entitiesFacetQuery = solrRequestGenerator.getTextElementsFacetWithStatsOnSameUtteranceQuery(tenant, channel, searchContext, language, TextElementType.Entities, 1, null,
		                                                                                                Arrays.asList(volume), volume,
		                                                                                                SpeakerQueryType.Any, textElementsLimit);

		assertNotNull(entitiesFacetQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, entitiesFacetQuery.getQueryPaths(), entitiesFacetQuery.getQueryParams(), false);

		//@formatter:off
		assertEquals("http://10.165.140.102:8983/solr/tenant/select?q=" +
				             "{!parent which='content_type:PARENT' score=total}(text_en:term1) AND (text_en:term2) AND (topics_f_total:1\\/device OR topics_f_total:1\\/software) AND (topics_f_total:3\\/device\\/iphone\\/iphone\\ 5) AND (relations_f_total:1\\/Service\\ action\\->Account)" +
				             "&json.facet={textElementFacet: {type : terms, method : dv, field : topics_f_total, mincount : 1, limit : -1 }}" +
						     "&json.nl=map&indent=true&fq=channel:channel" +
						     "&fq=content_type:PARENT&fq=date:[NOW\\-1MONTH\\/DAY TO NOW]&fq=(interaction_sentiment:\\-2)&rows=0&wt=json",
		             requestUrl);
		//@formatter:on
	}

	@Test
	@Ignore
	// Same utterance mode feature in not supported currently
	public void getEntitiesFacetQueryOnSameUtteranceNoTermsOnlyTopicsTest() {

		SearchInteractionsContext searchContext = createFullSearchContext();
		searchContext.getTerms().clear();

		// create new SolrRequestGenerator
		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();
		TextElementMetricType volume = TextElementMetricType.Volume;

		val entitiesFacetQuery = solrRequestGenerator.getTextElementsFacetWithStatsOnSameUtteranceQuery(tenant, channel, searchContext, language, TextElementType.Entities, 1, null,
		                                                                                                Arrays.asList(volume), volume,
		                                                                                                SpeakerQueryType.Any, textElementsLimit);

		assertNotNull(entitiesFacetQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, entitiesFacetQuery.getQueryPaths(), entitiesFacetQuery.getQueryParams(), false);

		//@formatter:off
		assertEquals("http://10.165.140.102:8983/solr/tenant/select?q=" +
				             "{!parent which='content_type:PARENT' score=total}(topics_f_total:1\\/device OR topics_f_total:1\\/software) AND (topics_f_total:3\\/device\\/iphone\\/iphone\\ 5) AND (relations_f_total:1\\/Service\\ action\\->Account)" +
				             "&json.facet={textElementFacet: {type : terms, method : dv, field : topics_f_total, mincount : 1, limit : -1 }}" +
						     "&json.nl=map&indent=true&fq=channel:channel" +
						     "&fq=content_type:PARENT&fq=date:[NOW\\-1MONTH\\/DAY TO NOW]&fq=(interaction_sentiment:\\-2)&rows=0&wt=json",
		             requestUrl);
		//@formatter:on
	}

	@Test
	@Ignore
	// Same utterance mode is not supported
	public void getEntitiesFacetQueryOnSameUtteranceTermsOnlyTest() {

		SearchInteractionsContext searchContext = createFullSearchContext();
		List<FilterField> filterFields =  searchContext.getFilterFields().stream().filter(f -> f.getName() != TAConstants.SchemaFieldNames.topics_f
		                                                    && f.getName() != TAConstants.SchemaFieldNames.relations_f
															&& f.getName() != TAConstants.SchemaFieldNames.keyterms_f)
												 .collect(toList());
		searchContext.setFilterFields(filterFields);

		// create new SolrRequestGenerator
		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();
		TextElementMetricType volume = TextElementMetricType.Volume;

		val entitiesFacetQuery = solrRequestGenerator.getTextElementsFacetWithStatsOnSameUtteranceQuery(tenant, channel, searchContext, language, TextElementType.Entities, 1, null,
		                                                                                                Arrays.asList(volume), volume,
		                                                                                                SpeakerQueryType.Any, textElementsLimit);

		assertNotNull(entitiesFacetQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, entitiesFacetQuery.getQueryPaths(), entitiesFacetQuery.getQueryParams(), false);

		//@formatter:off
		assertEquals("http://10.165.140.102:8983/solr/tenant/select?q=" +
				             "{!parent which='content_type:PARENT' score=total}(text_en:term1) AND (text_en:term2)" +
				             "&json.facet={textElementFacet: {type : terms, method : dv, field : topics_f_total, mincount : 1, limit : -1 }}" +
						     "&json.nl=map&indent=true&fq=channel:channel" +
						     "&fq=content_type:PARENT&fq=date:[NOW\\-1MONTH\\/DAY TO NOW]&fq=(interaction_sentiment:\\-2)&rows=0&wt=json",
		             requestUrl);
		//@formatter:on
	}


	@Test
	@Ignore
	// Same utterance mode is not supported
	public void getEntitiesFacetQueryOnSameUtteranceCustomerSpeakerTest() {

		SearchInteractionsContext searchContext = createFullSearchContext();

		// create new SolrRequestGenerator
		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();
		TextElementMetricType volume = TextElementMetricType.Volume;

		val entitiesFacetQuery = solrRequestGenerator.getTextElementsFacetWithStatsOnSameUtteranceQuery(tenant, channel, searchContext, language, TextElementType.Entities, 1, null,
		                                                                                                Arrays.asList(volume), volume,
		                                                                                                SpeakerQueryType.Customer, textElementsLimit);

		assertNotNull(entitiesFacetQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, entitiesFacetQuery.getQueryPaths(), entitiesFacetQuery.getQueryParams(), false);

		//@formatter:off
		assertEquals("http://10.165.140.102:8983/solr/tenant/select?q=" +
				             "{!parent which='content_type:PARENT' score=total}(text_en:term1) AND (text_en:term2) AND (topics_f_total:1\\/device OR topics_f_total:1\\/software) AND (topics_f_total:3\\/device\\/iphone\\/iphone\\ 5) AND (relations_f_total:1\\/Service\\ action\\->Account) AND (speaker_type:customer)" +
				             "&json.facet={textElementFacet: {type : terms, method : dv, field : topics_f_total, mincount : 1, limit : -1 }}" +
						     "&json.nl=map&indent=true&fq=channel:channel" +
						     "&fq=content_type:PARENT&fq=date:[NOW\\-1MONTH\\/DAY TO NOW]&fq=(interaction_sentiment:\\-2)&rows=0&wt=json",
		             requestUrl);
		//@formatter:on
	}

	@Test
	public void getEntitiesFacetQueryOnSameUtteranceNoTermsNoTaggedElementsAgentSpeakerTest() {

		SearchInteractionsContext searchContext = createFullSearchContext();
		searchContext.getTerms().clear();
		List<FilterField> filterFields =  searchContext.getFilterFields().stream().filter(f -> f.getName() != TAConstants.SchemaFieldNames.topics_f
				&& f.getName() != TAConstants.SchemaFieldNames.relations_f
				&& f.getName() != TAConstants.SchemaFieldNames.keyterms_f)
		                                               .collect(toList());
		searchContext.setFilterFields(filterFields);

		// create new SolrRequestGenerator
		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();
		TextElementMetricType volume = TextElementMetricType.Volume;

		val entitiesFacetQuery = solrRequestGenerator.getTextElementsFacetWithStatsOnSameUtteranceQuery(tenant, channel, searchContext, language, TextElementType.Entities, 1, null,
		                                                                                                Arrays.asList(volume), volume,
		                                                                                                SpeakerQueryType.Agent, textElementsLimit);

		assertNotNull(entitiesFacetQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, entitiesFacetQuery.getQueryPaths(), entitiesFacetQuery.getQueryParams(), false);

		//@formatter:off
		assertEquals("http://10.165.140.102:8983/solr/tenant/select?q=" +
				             "{!parent which='content_type:PARENT' score=total}(speaker_type:agent)" +
				             "&fq=channel:channel"+
						     "&fq=content_type:PARENT&fq=date:[NOW\\-1MONTH\\/MINUTE TO NOW\\/MINUTE]&fq=(interaction_sentiment:\\-2)" +
				             "&json.facet={textElementFacet: {type : terms, method : dv, field : topics_f_total, mincount : 1, limit : 0 }}" +
						     "&rows=0&wt=json&indent=true&json.nl=map",
		             requestUrl);
		//@formatter:on
	}

	@Test
	@Ignore
	// Same utterance mode is not supported
	public void getRelationsFacetQueryOnSameUtteranceForSpeakerAgentTest() {

		SearchInteractionsContext searchContext = createFullSearchContext();

		// create new SolrRequestGenerator
		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();
		TextElementMetricType volume = TextElementMetricType.Volume;

		val relationsFacetQuery = solrRequestGenerator.getTextElementsFacetWithStatsOnSameUtteranceQuery(tenant, channel, searchContext, language, TextElementType.Relations, 1, null,
		                                                                                                 Arrays.asList(volume), volume,
		                                                                                                 SpeakerQueryType.Agent, textElementsLimit);

		assertNotNull(relationsFacetQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, relationsFacetQuery.getQueryPaths(), relationsFacetQuery.getQueryParams(), false);

		//@formatter:off
		assertEquals("http://10.165.140.102:8983/solr/tenant/select?q=" +
				             "{!parent which='content_type:PARENT' score=total}(text_en:term1) AND (text_en:term2) AND (topics_f_total:1\\/device OR topics_f_total:1\\/software) AND (topics_f_total:3\\/device\\/iphone\\/iphone\\ 5) AND (relations_f_total:1\\/Service\\ action\\->Account) AND (speaker_type:agent)" +
				             "&json.facet={textElementFacet: {type : terms, method : dv, field : relations_f_total, mincount : 1, limit : -1 }}" +
						     "&json.nl=map&indent=true&fq=channel:channel" +
						     "&fq=content_type:PARENT&fq=date:[NOW\\-1MONTH\\/DAY TO NOW]&fq=(interaction_sentiment:\\-2)&rows=0&wt=json",
		             requestUrl);
	   //@formatter:on
	}

	@Test
	@Ignore
	public void getEntitiesFacetQueryForSpeakerAutomatedTest() {

		SearchInteractionsContext searchContext = createFullSearchContext();

		// create new SolrRequestGenerator
		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();
		TextElementMetricType volume = TextElementMetricType.Volume;

		val entitiesFacetQuery = solrRequestGenerator.getTextElementsFacetWithStatsQuery(tenant, channel, searchContext, language, TextElementType.Entities, "1",
		                                                                                 Arrays.asList(volume), volume,
		                                                                                 SpeakerQueryType.Automated, false, textElementsLimit);

		assertNotNull(entitiesFacetQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, entitiesFacetQuery.getQueryPaths(), entitiesFacetQuery.getQueryParams(), false);

		//@formatter:off
		assertEquals("http://10.165.140.102:8983/solr/tenant/select?q=*:*&json.facet={textElementFacet: {type : terms, field : topics_f, mincount : 1, facet :  {},limit : -1 }}&json.nl=map&indent=true"
				+ "&fq=(text_en:term1)"
				+ "&fq=(text_en:term2)"
				+ "&fq=channel:channel&fq=content_type:PARENT&fq=date:[NOW\\-1MONTH\\/DAY TO NOW]"
				+ "&fq=(topics_f:1\\/device OR topics_f:1\\/software)&fq=(topics_f:3\\/device\\/iphone\\/iphone\\ 5)"
				+ "&fq=(interaction_sentiment:\\-2)&fq=speaker_type:automated&rows=0&wt=json",
		             requestUrl);
		//@formatter:on

	}

	@Test
	public void getRelationsFacetQueryTest() {

		SearchInteractionsContext searchContext = createFullSearchContext();

		// create new SolrRequestGenerator
		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();
		TextElementMetricType volume = TextElementMetricType.Volume;

		val relationsFacetQuery = solrRequestGenerator.getTextElementsFacetWithStatsQuery(tenant, channel, searchContext, language, TextElementType.Relations, "1",
		                                                                                  Arrays.asList(volume), volume,
		                                                                                  SpeakerQueryType.Any, false, textElementsLimit);

		assertNotNull(relationsFacetQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, relationsFacetQuery.getQueryPaths(), relationsFacetQuery.getQueryParams(), false);

		//@formatter:off
		assertEquals("http://10.165.140.102:8983/solr/tenant/select?q=+(text_en_total:term1) +(text_en_total:term2)" +
							 "&fq=channel:channel&fq=content_type:PARENT" +
				             "&fq=date:[NOW\\-1MONTH\\/MINUTE TO NOW\\/MINUTE]&fq=(topics_f_total:1\\/device OR topics_f_total:1\\/software)" +
				             "&fq=(topics_f_total:3\\/device\\/iphone\\/iphone\\ 5)" +
				             "&fq=(relations_f_total:1\\/Service\\ action\\->Account)" +
				             "&fq=(interaction_sentiment:\\-2)" +
		                     "&json.facet={textElementFacet: {type : terms, method : dv, field : relations_f_total, prefix : \"1\", mincount : 1, limit : 30 }}" +
							 "&rows=0&wt=json&indent=true&json.nl=map",
		             requestUrl);
		//@formatter:on
	}

	@Test
	public void getEntitiesFacetWithStatsQueryTest() {

		SearchInteractionsContext searchContext = createFullSearchContext();

		// create new SolrRequestGenerator
		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();

		TextElementMetricType avgEmployeeResponseTime = TextElementMetricType.AvgEmployeeResponseTime;
		TextElementMetricType avgCustomerResponseTime = TextElementMetricType.AvgCustomerResponseTime;

		val entitiesFacetQuery = solrRequestGenerator.getTextElementsFacetWithStatsQuery(tenant, channel, searchContext, language, TextElementType.Entities, "1",
		                                                                                 Arrays.asList(avgEmployeeResponseTime, avgCustomerResponseTime), avgCustomerResponseTime,
		                                                                                 SpeakerQueryType.Any, false, textElementsLimit);

		assertNotNull(entitiesFacetQuery);
		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, entitiesFacetQuery.getQueryPaths(), entitiesFacetQuery.getQueryParams(), false);

		//@formatter:off
		assertEquals("http://10.165.140.102:8983/solr/tenant/select?q=+(text_en_total:term1) +(text_en_total:term2)" +
							  "&fq=channel:channel&fq=content_type:PARENT&fq=date:[NOW\\-1MONTH\\/MINUTE TO NOW\\/MINUTE]&fq=(topics_f_total:1\\/device OR topics_f_total:1\\/software)" +
							  "&fq=(topics_f_total:3\\/device\\/iphone\\/iphone\\ 5)&fq=(relations_f_total:1\\/Service\\ action\\->Account)&fq=(interaction_sentiment:\\-2)" +
				              "&json.facet={textElementFacet: {type : terms, method : dv, field : topics_f_total, sort : { AvgCustomerResponseTime : desc}, prefix : \"1\", mincount : 1, limit : 30, " +
				                "facet : { AvgEmployeeResponseTime : \"avg(Meta_l_avgEmployeeResponseTime)\", AvgCustomerResponseTime : \"avg(Meta_l_avgCustomerResponseTime)\" }}}" +
				             "&rows=0&wt=json&indent=true&json.nl=map",
		             requestUrl);
		//@formatter:on
	}

	@Test
	public void getRelationsFacetWithStatsQueryTest() {

		SearchInteractionsContext searchContext = createFullSearchContext();

		// create new SolrRequestGenerator
		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();

		TextElementMetricType avgHandleTime = TextElementMetricType.AvgHandleTime;
		TextElementMetricType volume = TextElementMetricType.Volume;

		val entitiesFacetQuery = solrRequestGenerator.getTextElementsFacetWithStatsQuery(tenant, channel, searchContext, language, TextElementType.Relations, "1",
		                                                                                 Arrays.asList(volume, avgHandleTime), avgHandleTime, SpeakerQueryType.Any, false, textElementsLimit);

		assertNotNull(entitiesFacetQuery);
		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, entitiesFacetQuery.getQueryPaths(), entitiesFacetQuery.getQueryParams(), false);

		assertEquals("http://10.165.140.102:8983/solr/tenant/select?q=+(text_en_total:term1) +(text_en_total:term2)" +
				             "&fq=channel:channel&fq=content_type:PARENT" +
				             "&fq=date:[NOW\\-1MONTH\\/MINUTE TO NOW\\/MINUTE]&fq=(topics_f_total:1\\/device OR topics_f_total:1\\/software)" +
				             "&fq=(topics_f_total:3\\/device\\/iphone\\/iphone\\ 5)&fq=(relations_f_total:1\\/Service\\ action\\->Account)" +
				             "&fq=(interaction_sentiment:\\-2)" +
				             "&json.facet={textElementFacet: {type : terms, method : dv, field : relations_f_total, sort : { AvgHandleTime : desc}, prefix : \"1\", mincount : 1, limit : 30, facet : { AvgHandleTime : \"avg(Meta_l_handleTime)\" }}}" +
				             "&rows=0&wt=json&indent=true&json.nl=map",
		             requestUrl);
	}




	@Test
	public void getInteractionSentimentMetadataFacetQueryTest() {

		SearchInteractionsContext searchContext = this.createFullSearchContext();

		// create new SolrRequestGenerator
		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();

		val facetQueryParams = solrRequestGenerator.getFacetQuery(tenant, channel, searchContext, TAConstants.SchemaFieldNames.interactionSentiment, null, null, language);

		assertNotNull(facetQueryParams);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, facetQueryParams.getQueryPaths(), facetQueryParams.getQueryParams(), false);

		//@formatter:off
		assertEquals("http://10.165.140.102:8983/solr/tenant/select?q=+(text_en_total:term1) +(text_en_total:term2)"
							 + "&fq=channel:channel&fq=content_type:PARENT"
							 + "&fq=date:[NOW\\-1MONTH\\/MINUTE TO NOW\\/MINUTE]"
							 + "&fq=(topics_f_total:1\\/device OR topics_f_total:1\\/software)"
		                     + "&fq=(topics_f_total:3\\/device\\/iphone\\/iphone\\ 5)"
				             + "&fq=(relations_f_total:1\\/Service\\ action\\->Account)"
		                     + "&fq={!tag=tagForinteraction_sentiment}(interaction_sentiment:\\-2)"
							 + "&json.facet={facetForinteraction_sentiment: {type : terms, method : dv, field : interaction_sentiment, excludeTags : \"tagForinteraction_sentiment\", mincount : 1, limit : 20 }}"
				             + "&rows=0&wt=json&indent=true&json.nl=map",
		             requestUrl);
		//@formatter:on
	}


	@Test
	public void getEntitiesFacetQueryTestEmptyContext() {

		SearchInteractionsContext searchContext = new SearchInteractionsContext();

		// create new SolrRequestGenerator
		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();

		val facetQueryParams = solrRequestGenerator.getFacetQuery(tenant, channel, searchContext, TAConstants.SchemaFieldNames.topics_f, null, null, language);

		assertNotNull(facetQueryParams);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, facetQueryParams.getQueryPaths(), facetQueryParams.getQueryParams(), false);

		assertEquals(
				"http://10.165.140.102:8983/solr/tenant/select?q=*:*"
						+ "&fq=channel:channel"
						+ "&fq=content_type:PARENT"
						+ "&json.facet={facetFortopics_f: {type : terms, method : dv, field : topics_f, mincount : 1, limit : 20 }}"
						+ "&rows=0&wt=json&indent=true&json.nl=map",
				requestUrl);
	}

	// Private Methods

	protected List<String> createFacetsQuery() {
		List<String> facetsQueries = new ArrayList<String>();
		facetsQueries.add("topic_f");
		return facetsQueries;
	}

	protected List<String> createFullFacetsQuery() {
		List<String> facetsQueries = new ArrayList<String>();

		facetsQueries.add("topics_f");
		facetsQueries.add("speaker_type");
		facetsQueries.add("interaction_sentiment");

		return facetsQueries;
	}

	protected final RequestGenerator createRequestGenerator() {
		SolrRequestGenerator solrRequestGenerator = new SolrRequestGenerator(metricFields);

		solrRequestGenerator.setQueryParams(this.solrQueryParamsMock);
		solrRequestGenerator.setVtaSyntaxAnalyzer(new VTASyntaxAnalyzer());
		solrRequestGenerator.setTextEngineConfigurationService(textEngineConfigurationServiceMock);
		solrRequestGenerator.setConfigurationManager(configurationManagerMock);

		solrRequestGenerator.initialize();

		return solrRequestGenerator;
	}

	protected RestDataAccess getRestDataAccess() {
		val restDataAccess = new RestDataAccess(configurationManagerMock);

		return restDataAccess;
	}

	protected SearchInteractionsContext createFullSearchContext() {
		SearchInteractionsContext searchContext = new SearchInteractionsContext();

		// add terms query
		// Note: the real terms are being added in the
		// createSolrRequestGenerator method
		List<String> lstTerms = new ArrayList<String>(Arrays.asList("term1", "term2"));
		searchContext.setTerms(lstTerms);

		// add date range filter
		val dateRangeFilter = this.createDateRangeFilter(TAConstants.DateRangeValues.dateRangeLast1Month, true, TAConstants.DateRangeValues.dateRangeNow, true);

		searchContext.setRangeFilterFields(Arrays.asList(dateRangeFilter));

		// add entity filter
		val topicFilterField1 = new FilterField();
		topicFilterField1.setName(TAConstants.SchemaFieldNames.topics_f);
		topicFilterField1.setDataType(FieldDataType.Text);
		topicFilterField1.setValues(new FilterFieldValue[] { new FilterFieldValue("1/device"), new FilterFieldValue("1/software") });

		val topicFilterField2 = new FilterField();
		topicFilterField2.setName(TAConstants.SchemaFieldNames.topics_f);
		topicFilterField2.setDataType(FieldDataType.Text);
		topicFilterField2.setValues(new FilterFieldValue[] { new FilterFieldValue("3/device/iphone/iphone 5") });

		FilterField relationFieldField = new FilterField();
		relationFieldField.setName(TAConstants.SchemaFieldNames.relations_f);
		relationFieldField.setDataType(FieldDataType.Text);
		relationFieldField.setValues(new FilterFieldValue[] { new FilterFieldValue("1/Service action->Account") });


		val sentimentFilterField = new FilterField();
		sentimentFilterField.setName("interaction_sentiment");
		sentimentFilterField.setDataType(FieldDataType.Int);
		sentimentFilterField.setValues(new FilterFieldValue[] { new FilterFieldValue("-2") });

		searchContext.setFilterFields(Arrays.asList(topicFilterField1, topicFilterField2, relationFieldField, sentimentFilterField));
		return searchContext;
	}

	protected RangeFilterField createDateRangeFilter(String lowerValue, Boolean lowerValueInclusive, String upperValue, Boolean upperValueInclusive) {
		// initialized date filter field
		val dateFilterField = new RangeFilterField();
		dateFilterField.setName(TAConstants.SchemaFieldNames.parentDate);
		dateFilterField.setDataType(FieldDataType.Constant);

		List<Range> ranges = new ArrayList<Range>();
		Range range = new Range();
		range.setIsLowerInclusive(lowerValueInclusive.toString());
		range.setLowerValue(lowerValue);

		range.setIsUpperInclusive(upperValueInclusive.toString());
		range.setUpperValue(upperValue);
		ranges.add(range);
		dateFilterField.setRanges(ranges);

		return dateFilterField;
	}

	protected SearchInteractionsContext createSearchContextForGroupTagsTests() {
		SearchInteractionsContext searchContext = new SearchInteractionsContext();

		// add terms query
		// Note: the real terms are being added in the
		// createSolrRequestGenerator method
		List<String> lstTerms = new ArrayList<String>(Arrays.asList("term1", "term2"));
		searchContext.setTerms(lstTerms);

		// add date range filter
		val dateRangeFilter = this.createDateRangeFilter(TAConstants.DateRangeValues.dateRangeLast1Month, true, TAConstants.DateRangeValues.dateRangeNow, true);

		searchContext.setRangeFilterFields(Arrays.asList(dateRangeFilter));

		return searchContext;
	}

}
