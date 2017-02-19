package com.verint.textanalytics.dal.darwin;

import com.verint.textanalytics.common.collection.MultivaluedStringMap;
import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionErrorCode;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionException;
import com.verint.textanalytics.common.utils.DataUtils;
import com.verint.textanalytics.common.utils.JSONUtils;
import com.verint.textanalytics.common.utils.StringUtils;
import com.verint.textanalytics.dal.darwin.vtasyntax.QueryTerm;
import com.verint.textanalytics.dal.darwin.vtasyntax.VTASyntaxAnalyzer;
import com.verint.textanalytics.dal.rest.RestDataAccess;
import com.verint.textanalytics.model.analyze.FieldMetric;
import com.verint.textanalytics.model.documentSchema.DocumentHierarchyType;
import com.verint.textanalytics.model.documentSchema.FieldDataType;
import com.verint.textanalytics.model.documentSchema.TextSchemaField;
import com.verint.textanalytics.model.facets.SpeakerQueryType;
import com.verint.textanalytics.model.interactions.*;
import com.verint.textanalytics.model.trends.TrendType;
import lombok.val;
import org.json.JSONObject;
import org.json.JSONTokener;
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

import static org.junit.Assert.*;

/**
 * Solr Request Generator Test.
 *
 * @author imor
 */
@PowerMockIgnore({"javax.management.*", "javax.net.ssl.*"})
@RunWith(PowerMockRunner.class)
public class SolrRequestGeneratorTest extends BaseTest {

	protected String baseUrl = "http://10.165.140.102:8983/solr";
	protected String eaSearchBaseUrl = "http://10.165.140.102:9000/";
	protected String tenant = "tenant";
	protected String channel = "channel";
	protected String language = "en";
	protected int pageStart = 0;
	protected int pageSize = 50;

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

	private String interactionFieldsList;


	public SolrRequestGeneratorTest() {
		MockitoAnnotations.initMocks(this);

		Mockito.when(solrQueryParamsMock.getResponseFormat()).thenReturn("json");
		Mockito.when(solrQueryParamsMock.getResponseIdentation()).thenReturn("true");
		Mockito.when(solrQueryParamsMock.getFacetMinCount()).thenReturn(1);
		Mockito.when(solrQueryParamsMock.getSearchInteractionsResultSetSize()).thenReturn(10000);
		Mockito.when(solrQueryParamsMock.getChildDocumentsForParentLimit()).thenReturn(1000);
		Mockito.when(solrQueryParamsMock.getTopicsFacetLimit()).thenReturn(-1);
		Mockito.when(solrQueryParamsMock.getFacetLimit()).thenReturn(20);

		Mockito.when(appConfigMock.getDarwinRestRequestTimeout()).thenReturn(60);
		Mockito.when(appConfigMock.getAutoCompleteLimitForQuery()).thenReturn(40);
		Mockito.when(appConfigMock.getBypassTrendsThreshold()).thenReturn("false");
		Mockito.when(appConfigMock.getFacetThreadLimit()).thenReturn(-1);
		Mockito.when(appConfigMock.getHttpClientMaxConnectionsPerRoute()).thenReturn(100);
		Mockito.when(appConfigMock.getHttpClientMaxConnectionsTotal()).thenReturn(100);
		Mockito.when(appConfigMock.getDateRangeRound()).thenReturn(true);
		Mockito.when(appConfigMock.getDateRangeRoundUpTo()).thenReturn("MINUTE");
		Mockito.when(appConfigMock.getFacetThreadLimit()).thenReturn(10);
		Mockito.when(appConfigMock.getTermsSuggestionsFacetThreadsLimit()).thenReturn(10);



		Mockito.when(configurationManagerMock.getApplicationConfiguration()).thenReturn(appConfigMock);

		TextSchemaField topicsSchemaField = new TextSchemaField().setName("topic_f")
		                                                         .setDisplayFieldName("Entities")
		                                                         .setFieldDataType(FieldDataType.Text)
		                                                         .setDocumentHierarchyType(DocumentHierarchyType.Interaction);

		TextSchemaField relationsShchemaField = new TextSchemaField().setName("relations_f")
		                                                             .setDisplayFieldName("Rlations")
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

		this.interactionFieldsList= "id,channel,content_type,Meta_s_messageSourceType,date,Meta_dt_employeeStartTime,Meta_s_employeeTimeZone,Meta_ss_employeeNames,Meta_i_employeesMessages," +
				"Meta_l_avgEmployeeResponseTime,Meta_dt_customerStartTime,Meta_s_customerTimeZone,Meta_ss_customerNames,Meta_i_customerMessages,Meta_l_avgCustomerResponseTime," +
				"Meta_i_numberOfRobotMessages,Meta_i_messagesCount,Meta_l_handleTime,score,interaction_sentiment,categories,interaction_sentiment_ismixed";

		metricFields = this.getFieldsMetrics();
	}


	@Test
	public void getSearchInteractionsQueryTestFullContext() {

		SearchInteractionsContext searchContext = this.createFullSearchContext();

		// create new SolrRequestGenerator
		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();

		val searchDocumentsQuery = solrRequestGenerator.getSearchInteractionsQuery(tenant, channel, searchContext, language, pageStart, pageSize, sortProperty, sortDirection);

		assertNotNull(searchDocumentsQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, searchDocumentsQuery.getQueryPaths(), searchDocumentsQuery.getQueryParams(), false);

		//@formatter:off
		assertEquals(String.format("http://10.165.140.102:8983/solr/tenant/select?q=+(text_en_total:%1$s) +(text_en_total:%2$s) AND (topics_f_total:%3$s OR topics_f_total:%4$s) AND (topics_f_total:%5$s) AND (relations_f_total:%6$s)"
		               + "&fq=channel:channel"
		               + "&fq=content_type:PARENT"
		               + "&fq=date:[NOW\\-1MONTH\\/MINUTE TO NOW\\/MINUTE]"
		               + "&fq=(topics_f_total:%3$s OR topics_f_total:%4$s)"
					   + "&fq=(topics_f_total:%5$s)"
				       + "&fq=(relations_f_total:%6$s)"
					   + "&fq=(interaction_sentiment:\\-2)"
					   + "&fl=%7$s"
					   + "&sort=score desc&start=0&rows=50&wt=json&indent=true&json.nl=map",
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


	@Test
	public void getSearchInteractionsQueryFullContextAgentAndCustomerTerms() {

		SearchInteractionsContext searchContext = this.createFullSearchContext();
		List<String> lstTerms = new ArrayList<>();
		lstTerms.add("A:term1");
		lstTerms.add("C:term2");
		searchContext.setTerms(lstTerms);

		// create new SolrRequestGenerator
		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();

		val searchDocumentsQuery = solrRequestGenerator.getSearchInteractionsQuery(tenant, channel, searchContext, language, pageStart, pageSize, sortProperty, sortDirection);

		assertNotNull(searchDocumentsQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, searchDocumentsQuery.getQueryPaths(), searchDocumentsQuery.getQueryParams(), false);

		//@formatter:off
		assertEquals(String.format("http://10.165.140.102:8983/solr/tenant/select?q=+(text_en_agent:%1$s) +(text_en_customer:%2$s) AND (topics_f_total:%3$s OR topics_f_total:%4$s) AND (topics_f_total:%5$s) AND (relations_f_total:%6$s)"
		               + "&fq=channel:channel"
		               + "&fq=content_type:PARENT"
		               + "&fq=date:[NOW\\-1MONTH\\/MINUTE TO NOW\\/MINUTE]"
		               + "&fq=(topics_f_total:%3$s OR topics_f_total:%4$s)"
					   + "&fq=(topics_f_total:%5$s)"
				       + "&fq=(relations_f_total:%6$s)"
					   + "&fq=(interaction_sentiment:\\-2)"
					   + "&fl=%7$s"
					   + "&sort=score desc&start=0&rows=50&wt=json&indent=true&json.nl=map",
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

	@Test
	public void getSearchInteractionsQueryFullContextAgentAndUnknownTerms() {

		SearchInteractionsContext searchContext = this.createFullSearchContext();
		List<String> lstTerms = new ArrayList<>();
		lstTerms.add("A:term1");
		lstTerms.add("C:term2");
		lstTerms.add("at&t");
		searchContext.setTerms(lstTerms);

		// create new SolrRequestGenerator
		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();

		val searchDocumentsQuery = solrRequestGenerator.getSearchInteractionsQuery(tenant, channel, searchContext, language, pageStart, pageSize, sortProperty, sortDirection);

		assertNotNull(searchDocumentsQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, searchDocumentsQuery.getQueryPaths(), searchDocumentsQuery.getQueryParams(), false);

		//@formatter:off
		assertEquals(String.format("http://10.165.140.102:8983/solr/tenant/select?q=+(text_en_agent:%1$s) +(text_en_customer:%2$s) +(text_en_total:%3$s) AND (topics_f_total:%4$s OR topics_f_total:%5$s) AND (topics_f_total:%6$s) AND (relations_f_total:%7$s)"
		               + "&fq=channel:channel"
		               + "&fq=content_type:PARENT"
		               + "&fq=date:[NOW\\-1MONTH\\/MINUTE TO NOW\\/MINUTE]"
		               + "&fq=(topics_f_total:%4$s OR topics_f_total:%5$s)"
					   + "&fq=(topics_f_total:%6$s)"
				       + "&fq=(relations_f_total:%7$s)"
					   + "&fq=(interaction_sentiment:\\-2)"
					   + "&fl=%8$s"
					   + "&sort=score desc&start=0&rows=50&wt=json&indent=true&json.nl=map",
						 DataUtils.escapeCharsForSolrQuery("term1"),
						 DataUtils.escapeCharsForSolrQuery("term2"),
						 DataUtils.escapeCharsForSolrQuery("at&t"),
						 DataUtils.escapeCharsForSolrQuery("1/device"),
						 DataUtils.escapeCharsForSolrQuery("1/software"),
						 DataUtils.escapeCharsForSolrQuery("3/device/iphone/iphone 5"),
			             DataUtils.escapeCharsForSolrQuery("1/Service action->Account"),
		                 this.interactionFieldsList),
		             requestUrl);
		//@formatter:on
	}

	@Test
	@Ignore
	public void getSearchInteractionsQueryWith2GroupsTest() {

		SearchInteractionsContext searchContext = this.createSearchContextForGroupTagsTests();

		// Add topics_f fields
		// add entity filter
		FilterField filterField1 = new FilterField();
		filterField1.setName(TAConstants.SchemaFieldNames.topics_f);
		filterField1.setDataType(FieldDataType.Text);
		filterField1.setValues(new FilterFieldValue[] { new FilterFieldValue("1/device"), new FilterFieldValue("1/software") });
		filterField1.setGroupTag("GROUP_A");

		FilterField filterField2 = new FilterField();
		filterField2.setName(TAConstants.SchemaFieldNames.topics_f);
		filterField2.setDataType(FieldDataType.Text);
		filterField2.setValues(new FilterFieldValue[] { new FilterFieldValue("2/device/iphone") });
		filterField2.setGroupTag("GROUP_B");

		FilterField filterField3 = new FilterField();
		filterField3.setName(TAConstants.SchemaFieldNames.topics_f);
		filterField3.setDataType(FieldDataType.Text);
		filterField3.setValues(new FilterFieldValue[] { new FilterFieldValue("2/software/avg") });
		filterField3.setGroupTag("GROUP_A");

		FilterField filterField4 = new FilterField();
		filterField4.setName(TAConstants.SchemaFieldNames.topics_f);
		filterField4.setDataType(FieldDataType.Text);
		filterField4.setValues(new FilterFieldValue[] { new FilterFieldValue("3/device/iphone/iphone 5") });
		// No Group for this filter

		searchContext.setFilterFields(Arrays.asList(filterField1, filterField2, filterField3, filterField4));

		// create new SolrRequestGenerator
		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();

		val searchDocumentsQuery = solrRequestGenerator.getSearchInteractionsQuery(tenant, channel, searchContext, language, pageStart, pageSize, sortProperty, sortDirection);

		assertNotNull(searchDocumentsQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, searchDocumentsQuery.getQueryPaths(), searchDocumentsQuery.getQueryParams(), false);

		//@formatter:off
		assertEquals(String.format("http://10.165.140.102:8983/solr/tenant/select?"
		              +  "q={!parent which='content_type:PARENT' score=total}(text_en:%1$s) OR (text_en:%2$s) OR (topics_f:%3$s OR topics_f:%4$s) OR (topics_f:%5$s) OR (topics_f:%6$s) OR (topics_f:%7$s)"
				      + "&fq={!parent which='content_type:PARENT'}(text_en:%1$s)"
				      + "&fq={!parent which='content_type:PARENT'}(text_en:%2$s)"
				      + "&fq=channel:channel"
				      + "&fq=content_type:PARENT"
				      + "&fq=date:[NOW\\-1MONTH\\/DAY TO NOW]"
				      + "&fq={!parent which='content_type:PARENT'}(topics_f:%3$s OR topics_f:%4$s) AND (topics_f:%6$s)" 
				      + "&fq={!parent which='content_type:PARENT'}(topics_f:%5$s)" 
					  + "&fq={!parent which='content_type:PARENT'}(topics_f:%7$s)"
					  + "&fl=*,score,[child parentFilter=content_type:PARENT limit=1000]"
					  + "&sort=score desc&start=0&rows=50&wt=json&indent=true&json.nl=map",					 
					 DataUtils.escapeCharsForSolrQuery("term1"),
					 DataUtils.escapeCharsForSolrQuery("term2"),
					 DataUtils.escapeCharsForSolrQuery("1/device"),
					 DataUtils.escapeCharsForSolrQuery("1/software"),
					 DataUtils.escapeCharsForSolrQuery("2/device/iphone"),
					 DataUtils.escapeCharsForSolrQuery("2/software/avg"),
					 DataUtils.escapeCharsForSolrQuery("3/device/iphone/iphone 5")),					 				
		             requestUrl);
		//@formatter:on
	}

	@Test
	@Ignore
	public void getSearchInteractionsQueryWith2GroupsAndSpeakerTest() {

		SearchInteractionsContext searchContext = this.createSearchContextForGroupTagsTests();

		// Add topics_f fields
		// add entity filter
		FilterField filterField1 = new FilterField();
		filterField1.setName(TAConstants.SchemaFieldNames.topics_f);
		filterField1.setDataType(FieldDataType.Text);
		filterField1.setValues(new FilterFieldValue[] { new FilterFieldValue("1/device"), new FilterFieldValue("1/software") });
		filterField1.setGroupTag("GROUP_A");
		filterField1.setSpeaker(SpeakerQueryType.Agent);

		FilterField filterField2 = new FilterField();
		filterField2.setName(TAConstants.SchemaFieldNames.topics_f);
		filterField2.setDataType(FieldDataType.Text);
		filterField2.setValues(new FilterFieldValue[] { new FilterFieldValue("2/device/iphone") });
		filterField2.setGroupTag("GROUP_B");
		filterField2.setSpeaker(SpeakerQueryType.Customer);

		FilterField filterField3 = new FilterField();
		filterField3.setName(TAConstants.SchemaFieldNames.topics_f);
		filterField3.setDataType(FieldDataType.Text);
		filterField3.setValues(new FilterFieldValue[] { new FilterFieldValue("2/software/avg") });
		filterField3.setGroupTag("GROUP_A");
		filterField3.setSpeaker(SpeakerQueryType.Agent);

		FilterField filterField4 = new FilterField();
		filterField4.setName(TAConstants.SchemaFieldNames.topics_f);
		filterField4.setDataType(FieldDataType.Text);
		filterField4.setValues(new FilterFieldValue[] { new FilterFieldValue("3/device/iphone/iphone 5") });
		// No Group for this filter

		searchContext.setFilterFields(Arrays.asList(filterField1, filterField2, filterField3, filterField4));

		// create new SolrRequestGenerator
		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();

		val searchDocumentsQuery = solrRequestGenerator.getSearchInteractionsQuery(tenant, channel, searchContext, language, pageStart, pageSize, sortProperty, sortDirection);

		assertNotNull(searchDocumentsQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, searchDocumentsQuery.getQueryPaths(), searchDocumentsQuery.getQueryParams(), false);

		//@formatter:off
		assertEquals(String.format("http://10.165.140.102:8983/solr/tenant/select?q={!parent which='content_type:PARENT' score=total}(text_en:%1$s) OR (text_en:%2$s) OR (topics_f:%3$s OR topics_f:%4$s) OR (topics_f:%5$s) OR (topics_f:%6$s) OR (topics_f:%7$s)" 									
									+ "&fq={!parent which='content_type:PARENT'}(text_en:%1$s)"
									+ "&fq={!parent which='content_type:PARENT'}(text_en:%2$s)"
									+ "&fq=channel:channel"
									+ "&fq=content_type:PARENT"
									+ "&fq=date:[NOW\\-1MONTH\\/DAY TO NOW]"
									+ "&fq={!parent which=\'content_type:PARENT\'}(topics_f:%3$s OR topics_f:%4$s) AND speaker_type:agent AND (topics_f:%6$s) AND speaker_type:agent"
									+ "&fq={!parent which=\'content_type:PARENT\'}(topics_f:%5$s) AND speaker_type:customer"
									+ "&fq={!parent which=\'content_type:PARENT\'}(topics_f:%7$s)"
									+ "&fl=*,score,[child parentFilter=content_type:PARENT limit=1000]"
									+ "&sort=score desc&start=0&rows=50&wt=json&indent=true&json.nl=map",					 
					  DataUtils.escapeCharsForSolrQuery("term1"),
					  DataUtils.escapeCharsForSolrQuery("term2"),
					  DataUtils.escapeCharsForSolrQuery("1/device"),
					  DataUtils.escapeCharsForSolrQuery("1/software"),
					  DataUtils.escapeCharsForSolrQuery("2/device/iphone"),
					  DataUtils.escapeCharsForSolrQuery("2/software/avg"),
					  DataUtils.escapeCharsForSolrQuery("3/device/iphone/iphone 5")),					  					 					  
		             requestUrl);
		//@formatter:on
	}

	@Test
	@Ignore
	public void getSearchInteractionsQueryWithSpeakerOnFilterTest() {

		SearchInteractionsContext searchContext = this.createSearchContextForGroupTagsTests();

		// Add topics_f fields
		// add entity filter
		FilterField filterField1 = new FilterField();
		filterField1.setName(TAConstants.SchemaFieldNames.topics_f);
		filterField1.setDataType(FieldDataType.Text);
		filterField1.setValues(new FilterFieldValue[] { new FilterFieldValue("1/device"), new FilterFieldValue("1/software") });
		filterField1.setSpeaker(SpeakerQueryType.Agent);

		FilterField filterField2 = new FilterField();
		filterField2.setName(TAConstants.SchemaFieldNames.topics_f);
		filterField2.setDataType(FieldDataType.Text);
		filterField2.setValues(new FilterFieldValue[] { new FilterFieldValue("2/device/iphone"), new FilterFieldValue("2/software/avg") });
		filterField2.setSpeaker(SpeakerQueryType.Agent);

		FilterField filterField3 = new FilterField();
		filterField3.setName(TAConstants.SchemaFieldNames.relations_f);
		filterField3.setDataType(FieldDataType.Text);
		filterField3.setValues(new FilterFieldValue[] { new FilterFieldValue("2/ChangeAction->TelcoService/CHANGE->service"),
														new FilterFieldValue("2/ChangeAction->TelcoService/CHANGE->cellular service") });
		filterField3.setSpeaker(SpeakerQueryType.Customer);

		FilterField filterField4 = new FilterField();
		filterField4.setName(TAConstants.SchemaFieldNames.relations_f);
		filterField4.setDataType(FieldDataType.Text);
		filterField4.setValues(new FilterFieldValue[] { new FilterFieldValue("1/HaveAction->Program") });
		filterField4.setSpeaker(SpeakerQueryType.Automated);

		searchContext.setFilterFields(Arrays.asList(filterField1, filterField2, filterField3, filterField4));

		// create new SolrRequestGenerator
		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();

		val searchDocumentsQuery = solrRequestGenerator.getSearchInteractionsQuery(tenant, channel, searchContext, language, pageStart, pageSize, sortProperty, sortDirection);

		assertNotNull(searchDocumentsQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, searchDocumentsQuery.getQueryPaths(), searchDocumentsQuery.getQueryParams(), false);

		//@formatter:off
		assertEquals(String.format("http://10.165.140.102:8983/solr/tenant/select?q=(text_en:%1$s) OR (text_en:%2$s) OR (topics_f:%3$s OR topics_f:%4$s) OR (topics_f:%5$s OR topics_f:%6$s) OR (relations_f:%7$s OR relations_f:%8$s) OR (relations_f:%9$s)"
		                            + "&fq=(text_en:%1$s)"
		                            + "&fq=(text_en:%2$s)"
		                            + "&fq=channel:channel"
		                            + "&fq=content_type:PARENT"
		                            + "&fq=date:[NOW\\-1MONTH\\/DAY TO NOW]"
		                            + "&fq={!parent which=\'content_type:PARENT\'}(topics_f:%3$s OR topics_f:%4$s) AND speaker_type:agent"
		                            + "&fq={!parent which=\'content_type:PARENT\'}(topics_f:%5$s OR topics_f:%6$s) AND speaker_type:agent"
		                            + "&fq=(relations_f:%7$s OR relations_f:%8$s) AND speaker_type:customer"
		                            + "&fq=(relations_f:%9$s) AND speaker_type:automated"
		                            + "&fl=*,score,[child parentFilter=content_type:PARENT limit=1000]"
		                            + "&sort=score desc&start=0&rows=50&wt=json&indent=true&json.nl=map",		        					
		                           DataUtils.escapeCharsForSolrQuery("term1"), 
		                           DataUtils.escapeCharsForSolrQuery("term2"), 
		                           DataUtils.escapeCharsForSolrQuery("1/device"),
		                           DataUtils.escapeCharsForSolrQuery("1/software"), 
		                           DataUtils.escapeCharsForSolrQuery("2/device/iphone"),
		                           DataUtils.escapeCharsForSolrQuery("2/software/avg"), 
		                           DataUtils.escapeCharsForSolrQuery("2/ChangeAction->TelcoService/CHANGE->service"),
		                           DataUtils.escapeCharsForSolrQuery("2/ChangeAction->TelcoService/CHANGE->cellolar service"),
		                           DataUtils.escapeCharsForSolrQuery("1/HaveAction->Program") 
		                           ),
		             requestUrl);
		//@formatter:on
	}

	@Test
	public void getSuggestionsForAutocomplete() {
		SearchInteractionsContext searchContext = this.createFullSearchContext();

		// create new SolrRequestGenerator
		RequestGenerator solrRequestGenerator = this.createRequestGenerator();

		val suggestionsQuery = solrRequestGenerator.getTermsSuggestionsForAutocompleteQuery(tenant, channel, searchContext, "acc", language);

		assertNotNull(suggestionsQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, suggestionsQuery.getQueryPaths(), suggestionsQuery.getQueryParams(), false);

		//@formatter:off
		assertEquals("http://10.165.140.102:8983/solr/tenant/select?q=+(text_en_total:term1) +(text_en_total:term2) +(text_en_total:acc*)" +
					 "&fq=channel:channel&fq=content_type:PARENT" +
					 "&fq=date:[NOW\\-1MONTH\\/MINUTE TO NOW\\/MINUTE]" +
					 "&fq=(topics_f_total:1\\/device OR topics_f_total:1\\/software)" +
					 "&fq=(topics_f_total:3\\/device\\/iphone\\/iphone\\ 5)" +
				     "&fq=(relations_f_total:1\\/Service\\ action\\->Account)" +
				     "&fq=(interaction_sentiment:\\-2)" +
					 "&facet=true&facet.field=text_en_total&facet.method=enum&facet.prefix=acc&facet.limit=40&facet.mincount=1&facet.threads=10" +
				     "&rows=0&wt=json&indent=true&json.nl=map",
		             requestUrl);
		//@formatter:on
	}

	@Test
	public void getSuggestionsForAutocompleteTenantNullTesting() {

		// create new SolrRequestGenerator
		RequestGenerator solrRequestGenerator = this.createRequestGenerator();

		SearchInteractionsContext searchContext = this.createFullSearchContext();

		try {
			solrRequestGenerator.getTermsSuggestionsForAutocompleteQuery(null, channel, searchContext, "acc", language);
			fail("No exception then when tenant parameter was null");

		} catch (Exception e) {
			assertEquals(e.getClass(), TextQueryExecutionException.class);

			TextQueryExecutionException textQueryException = (TextQueryExecutionException) e;
			assertEquals(textQueryException.getAppExecutionErrorCode(), TextQueryExecutionErrorCode.TextQueryGenerationError);
		}

	}

	@Test
	public void getSuggestionsForAutocompleteChannelNullTesting() {
		// create new SolrRequestGenerator
		RequestGenerator solrRequestGenerator = this.createRequestGenerator();
		SearchInteractionsContext searchContext = this.createFullSearchContext();

		try {
			solrRequestGenerator.getTermsSuggestionsForAutocompleteQuery(tenant, null, searchContext, "acc", language);
			fail("No exception then when channel parameter was null");

		} catch (Exception e) {
			assertEquals(e.getClass(), TextQueryExecutionException.class);

			TextQueryExecutionException textQueryException = (TextQueryExecutionException) e;
			assertEquals(textQueryException.getAppExecutionErrorCode(), TextQueryExecutionErrorCode.TextQueryGenerationError);
		}
	}

	@Test
	public void getSuggestionsForAutocompletesearchContextNullTesting() {

		// create new SolrRequestGenerator
		RequestGenerator solrRequestGenerator = this.createRequestGenerator();

		try {
			solrRequestGenerator.getTermsSuggestionsForAutocompleteQuery(tenant, channel, null, "acc", language);
			fail("No exception then when search context parameter was null");

		} catch (Exception e) {
			assertEquals(e.getClass(), TextQueryExecutionException.class);

			TextQueryExecutionException textQueryException = (TextQueryExecutionException) e;
			assertEquals(textQueryException.getAppExecutionErrorCode(), TextQueryExecutionErrorCode.TextQueryGenerationError);
		}
	}

	@Test
	public void getSuggestionsForAutocompletePrefixNullTesting() {

		// create new SolrRequestGenerator
		RequestGenerator solrRequestGenerator = this.createRequestGenerator();

		SearchInteractionsContext searchContext = this.createFullSearchContext();

		try {
			solrRequestGenerator.getTermsSuggestionsForAutocompleteQuery(null, channel, searchContext, null, language);
			fail("No exception then when prefix parameter was null");

		} catch (Exception e) {
			assertEquals(e.getClass(), TextQueryExecutionException.class);

			TextQueryExecutionException textQueryException = (TextQueryExecutionException) e;
			assertEquals(textQueryException.getAppExecutionErrorCode(), TextQueryExecutionErrorCode.TextQueryGenerationError);
		}

	}

	@Test
	public void getSuggestionsForAutocompletelanguageNullTesting() {

		// create new SolrRequestGenerator
		RequestGenerator solrRequestGenerator = this.createRequestGenerator();

		SearchInteractionsContext searchContext = this.createFullSearchContext();

		try {
			solrRequestGenerator.getTermsSuggestionsForAutocompleteQuery(null, channel, searchContext, "acc", null);
			fail("No exception then when language parameter was null");

		} catch (Exception e) {
			assertEquals(e.getClass(), TextQueryExecutionException.class);

			TextQueryExecutionException textQueryException = (TextQueryExecutionException) e;
			assertEquals(textQueryException.getAppExecutionErrorCode(), TextQueryExecutionErrorCode.TextQueryGenerationError);
		}

	}

	@Test
	public void getTotalInteractionsQuantityQueryTestFullContext() {

		SearchInteractionsContext searchContext = this.createFullSearchContext();

		// create new SolrRequestGenerator
		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();

		val searchDocumentsQuery = solrRequestGenerator.getTotalInteractionsQuantityQuery(tenant, channel, searchContext);

		assertNotNull(searchDocumentsQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, searchDocumentsQuery.getQueryPaths(), searchDocumentsQuery.getQueryParams(), false);

		assertEquals("http://10.165.140.102:8983/solr/tenant/select?q=*:*" + "&fq=channel:channel&fq=content_type:PARENT" + "&rows=0&wt=json&indent=true&json.nl=map",
		             requestUrl);

		try {
			solrRequestGenerator.getTotalInteractionsQuantityQuery(null, channel, searchContext);
			fail("No exception then when tenant parameter was null");
		} catch (Exception e) {
			assertEquals(e.getClass(), TextQueryExecutionException.class);

			TextQueryExecutionException textQueryException = (TextQueryExecutionException) e;
			assertEquals(textQueryException.getAppExecutionErrorCode(), TextQueryExecutionErrorCode.TextQueryGenerationError);
		}
	}

	@Test
	public void getCurrentSearchInteractionsQuantityQueryTestFullContext() {

		SearchInteractionsContext searchContext = this.createFullSearchContext();

		// create new SolrRequestGenerator
		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();

		val searchDocumentsQuery = solrRequestGenerator.getCurrentSearchInteractionsQuantityQuery(tenant, channel, searchContext, language);

		assertNotNull(searchDocumentsQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, searchDocumentsQuery.getQueryPaths(), searchDocumentsQuery.getQueryParams(), false);

		//@formatter:off
		assertEquals("http://10.165.140.102:8983/solr/tenant/select?q=+(text_en_total:term1) +(text_en_total:term2)" +
					 "&fq=channel:channel&fq=content_type:PARENT" +
				     "&fq=date:[NOW\\-1MONTH\\/MINUTE TO NOW\\/MINUTE]" +
				     "&fq=(topics_f_total:1\\/device OR topics_f_total:1\\/software)" +
				     "&fq=(topics_f_total:3\\/device\\/iphone\\/iphone\\ 5)" +
				     "&fq=(relations_f_total:1\\/Service\\ action\\->Account)" +
				     "&fq=(interaction_sentiment:\\-2)&rows=0" +
				     "&wt=json&indent=true&json.nl=map",
		             requestUrl);
		//@formatter:on

		try {
			solrRequestGenerator.getCurrentSearchInteractionsQuantityQuery(null, channel, searchContext, language);
			fail("No exception then when tenant parameter was null");
		} catch (Exception e) {
			assertEquals(e.getClass(), TextQueryExecutionException.class);

			TextQueryExecutionException textQueryException = (TextQueryExecutionException) e;
			assertEquals(textQueryException.getAppExecutionErrorCode(), TextQueryExecutionErrorCode.TextQueryGenerationError);
		}
	}

	@Test
	public void getInteractionPreviewTest() {

		// create new SearchInteractionsContext
		String baseUrl = "http://10.165.140.102:8983/solr";
		String tenant = "tenant";
		String channel = "channel";
		String interactionId = "100";

		// create new SolrRequestGenerator
		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();
		val restDataAccess = this.getRestDataAccess();

		val interactionPreviewQueryParams = solrRequestGenerator.getInteractionPreviewQuery(tenant, channel, interactionId);

		String requestUrl = restDataAccess.getRequestUrl(baseUrl, interactionPreviewQueryParams.getQueryPaths(), interactionPreviewQueryParams.getQueryParams(), false);

		assertEquals(String.format("http://10.165.140.102:8983/solr/tenant/select?q=*:*"
				                           + "&fq=channel:channel"
				                           + "&fq=content_type:PARENT"
				                           + "&fq=(id:100)"
		                                   + "&fl=%s"
		                                   + "&rows=1"
				                           + "&wt=json"
				                           + "&indent=true"
				                           + "&json.nl=map", this.interactionFieldsList + ",[child parentFilter=content_type:PARENT limit=1000]"),
					 requestUrl);

		try {
			solrRequestGenerator.getInteractionPreviewQuery(null, channel, interactionId);
			fail("No exception then when tenant parameter was null");
		} catch (Exception e) {
			assertEquals(e.getClass(), TextQueryExecutionException.class);

			TextQueryExecutionException textQueryException = (TextQueryExecutionException) e;
			assertEquals(textQueryException.getAppExecutionErrorCode(), TextQueryExecutionErrorCode.TextQueryGenerationError);
		}
	}

	// Tests with Empty Context

	@Test
	public void getSearchInteractionsQueryTestEmptyContext() {

		SearchInteractionsContext searchContext = new SearchInteractionsContext();

		// create new SolrRequestGenerator
		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();

		val searchDocumentsQuery = solrRequestGenerator.getSearchInteractionsQuery(tenant, channel, searchContext, language, pageStart, pageSize, sortProperty, sortDirection);

		assertNotNull(searchDocumentsQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, searchDocumentsQuery.getQueryPaths(), searchDocumentsQuery.getQueryParams(), false);

		// @formatter:off
		assertEquals(String.format("http://10.165.140.102:8983/solr/tenant/select?q=*:*" +
		                            "&fq=channel:channel" +
				                    "&fq=content_type:PARENT" +
				                    "&fl=%s" +
				                    "&sort=score desc&start=0&rows=50&wt=json&indent=true&json.nl=map", this.interactionFieldsList + ",[child parentFilter=content_type:PARENT limit=1]"),
		                           requestUrl);
		// @formatter:on

		try {
			solrRequestGenerator.getSearchInteractionsQuery(null, channel, searchContext, language, pageStart, pageSize, sortProperty, sortDirection);
			fail("No exception then when tenant parameter was null");
		} catch (Exception e) {
			assertEquals(e.getClass(), TextQueryExecutionException.class);

			TextQueryExecutionException textQueryException = (TextQueryExecutionException) e;
			assertEquals(textQueryException.getAppExecutionErrorCode(), TextQueryExecutionErrorCode.TextQueryGenerationError);
		}
	}


	@Test
	public void getTotalInteractionsQuantityQueryTestEmptyContext() {

		SearchInteractionsContext searchContext = new SearchInteractionsContext();

		// create new SolrRequestGenerator
		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();

		val searchDocumentsQuery = solrRequestGenerator.getTotalInteractionsQuantityQuery(tenant, channel, searchContext);

		assertNotNull(searchDocumentsQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, searchDocumentsQuery.getQueryPaths(), searchDocumentsQuery.getQueryParams(), false);

		assertEquals("http://10.165.140.102:8983/solr/tenant/select?q=*:*" +
				             "&fq=channel:channel&fq=content_type:PARENT" +
				             "&rows=0" +
				             "&wt=json&indent=true&json.nl=map", requestUrl);

		try {
			solrRequestGenerator.getTotalInteractionsQuantityQuery(null, channel, searchContext);
			fail("No exception then when tenant parameter was null");
		} catch (Exception e) {
			assertEquals(e.getClass(), TextQueryExecutionException.class);

			TextQueryExecutionException textQueryException = (TextQueryExecutionException) e;
			assertEquals(textQueryException.getAppExecutionErrorCode(), TextQueryExecutionErrorCode.TextQueryGenerationError);
		}
	}

	@Test
	public void getCurrentSearchInteractionsQuantityQueryTestEmptyContext() {

		SearchInteractionsContext searchContext = new SearchInteractionsContext();

		// create new SolrRequestGenerator
		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();

		val searchDocumentsQuery = solrRequestGenerator.getCurrentSearchInteractionsQuantityQuery(tenant, channel, searchContext, language);

		assertNotNull(searchDocumentsQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, searchDocumentsQuery.getQueryPaths(), searchDocumentsQuery.getQueryParams(), false);

		assertEquals("http://10.165.140.102:8983/solr/tenant/select?q=*:*" +
				             "&fq=channel:channel&fq=content_type:PARENT&rows=0" +
				             "&wt=json&indent=true&json.nl=map", requestUrl);

		try {
			solrRequestGenerator.getCurrentSearchInteractionsQuantityQuery(null, channel, searchContext, language);

			fail("No exception then when tenant parameter was null");
		} catch (Exception e) {
			assertEquals(e.getClass(), TextQueryExecutionException.class);

			TextQueryExecutionException textQueryException = (TextQueryExecutionException) e;
			assertEquals(textQueryException.getAppExecutionErrorCode(), TextQueryExecutionErrorCode.TextQueryGenerationError);
		}
	}

	// Regular tests

	@Test
	public void getInteractionHighlightsQueryTest() {

		// create new SolrRequestGenerator
		RestDataAccess restDataAccess = this.getRestDataAccess();
		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();

		String tenant = "tenant1";
		String channel = "1271";
		String language = "en";
		SpeakerType speaker = SpeakerType.Agent;


		List<FilterFieldValue> interactionIds = new ArrayList<FilterFieldValue>();
		interactionIds.add(new FilterFieldValue("100", ""));

		List<QueryTerm> terms = new ArrayList<QueryTerm>();
		QueryTerm term = new QueryTerm("iphone", com.verint.textanalytics.dal.darwin.vtasyntax.TermType.Word,
		                               com.verint.textanalytics.dal.darwin.vtasyntax.SpeakerType.Agent, "iphone", Arrays.asList("iphone"));
		terms.add(term);
		val agentHighlightsQuery = solrRequestGenerator.getInteractionsHighlightsQuery(tenant, channel, interactionIds, SpeakerType.Agent, terms, language);

		String requestUrl = restDataAccess.getRequestUrl(baseUrl, agentHighlightsQuery.getQueryPaths(), agentHighlightsQuery.getQueryParams(), false);

		assertNotNull(agentHighlightsQuery);
		assertEquals("http://10.165.140.102:8983/solr/tenant1/select?q=*:*"
							 + "&fq=channel:1271"
							 + "&fq=content_type:CHILD"
				             + "&fq=(id:100)"
				             + "&fq=(speaker_type:agent)"
				             + "&hl=true&hl.fl=text_en&hl.q=(iphone)"
				             + "&rows=10000&wt=json&indent=true&json.nl=map",
				requestUrl);

		terms.clear();
		terms.add(new QueryTerm("iphone", com.verint.textanalytics.dal.darwin.vtasyntax.TermType.Word,
		                                  com.verint.textanalytics.dal.darwin.vtasyntax.SpeakerType.Customer, "iphone",  Arrays.asList("iphone")));
		val customerHighlightsQuery = solrRequestGenerator.getInteractionsHighlightsQuery(tenant, channel, interactionIds, SpeakerType.Customer, terms, language);
		requestUrl = restDataAccess.getRequestUrl(baseUrl, customerHighlightsQuery.getQueryPaths(), customerHighlightsQuery.getQueryParams(), false);

		assertNotNull(customerHighlightsQuery);
		assertEquals("http://10.165.140.102:8983/solr/tenant1/select?q=*:*"
				              + "&fq=channel:1271"
				              + "&fq=content_type:CHILD"
				              + "&fq=(id:100)"
				              + "&fq=(speaker_type:customer)"
				              + "&hl=true&hl.fl=text_en&hl.q=(iphone)&rows=10000"
				              + "&wt=json&indent=true&json.nl=map",
				requestUrl);

		terms.clear();
		terms.add(new QueryTerm("iphone", com.verint.textanalytics.dal.darwin.vtasyntax.TermType.Word,
		                                  com.verint.textanalytics.dal.darwin.vtasyntax.SpeakerType.NoSPS, "iphone",  Arrays.asList("iphone")));
		val noSPSHighlightsQuery = solrRequestGenerator.getInteractionsHighlightsQuery(tenant, channel, interactionIds, SpeakerType.Unknown, terms, language);
		requestUrl = restDataAccess.getRequestUrl(baseUrl, noSPSHighlightsQuery.getQueryPaths(), noSPSHighlightsQuery.getQueryParams(), false);

		assertNotNull(noSPSHighlightsQuery);
		assertEquals("http://10.165.140.102:8983/solr/tenant1/select?q=*:*" +
				             "&fq=channel:1271&fq=content_type:CHILD&fq=(id:100)" +
				             "&hl=true&hl.fl=text_en&hl.q=(iphone)&rows=10000" +
				             "&wt=json&indent=true&json.nl=map",
				requestUrl);

		try {
			solrRequestGenerator.getInteractionsHighlightsQuery(null, channel, interactionIds, speaker, terms, language);
			fail("No exception then when tenant parameter was null");
		} catch (Exception e) {
			assertEquals(e.getClass(), TextQueryExecutionException.class);

			TextQueryExecutionException textQueryException = (TextQueryExecutionException) e;
			assertEquals(textQueryException.getAppExecutionErrorCode(), TextQueryExecutionErrorCode.TextQueryGenerationError);
		}
	}

	@Test
	public void getInteractionsDailyVolumeSeriesTest() {
		String language = "en";
		SearchInteractionsContext searchContext = new SearchInteractionsContext();

		// create new SolrRequestGenerator
		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();

		val searchDocumentsQuery = solrRequestGenerator.getInteractionsDailyVolumeSeriesQuery(tenant, channel, searchContext, language);

		assertNotNull(searchDocumentsQuery);

		val restDataAccess = this.getRestDataAccess();

		MultivaluedStringMap queryParams = searchDocumentsQuery.getQueryParams();
		assertNotNull(queryParams);

		// find the json.facet parameter		
		assertTrue(queryParams.containsKey("json.facet"));

		List<String> lstJsonFacets = (List<String>) queryParams.get("json.facet");
		assertNotNull(lstJsonFacets);
		assertEquals(1, lstJsonFacets.size());

		String facetRequestJson = lstJsonFacets.get(0);

		// parse the json and validate
		if (!StringUtils.isNullOrBlank(facetRequestJson)) {
			JSONTokener tokener = new JSONTokener(facetRequestJson);
			JSONObject root = new JSONObject(tokener);

			JSONObject dailyVolumeEl = JSONUtils.getJSONObject(root, "interactionsDailyVolume");
			assertNotNull(dailyVolumeEl);

			assertEquals("terms", JSONUtils.getString("type", dailyVolumeEl, ""));
			assertEquals("Meta_l_date_day", JSONUtils.getString("field", dailyVolumeEl, ""));
			assertEquals(1, JSONUtils.getInt("mincount", dailyVolumeEl, -1));
			assertEquals(-1, JSONUtils.getInt("limit", dailyVolumeEl, 0));
		}

		try {
			solrRequestGenerator.getInteractionsDailyVolumeSeriesQuery(null, channel, searchContext, language);
			fail("No exception then when tenant parameter was null");
		} catch (Exception e) {
			assertEquals(e.getClass(), TextQueryExecutionException.class);

			TextQueryExecutionException textQueryException = (TextQueryExecutionException) e;
			assertEquals(textQueryException.getAppExecutionErrorCode(), TextQueryExecutionErrorCode.TextQueryGenerationError);
		}
	}

	@Test
	public void getResultSetMetricsTest() {
		String language = "en";
		SearchInteractionsContext searchContext = new SearchInteractionsContext();

		// create new SolrRequestGenerator
		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();

		List<FieldMetric> fieldsMetrics = this.getFieldsMetrics();

		val searchDocumentsQuery = solrRequestGenerator.getResultSetMetricsQuery(tenant, channel, searchContext, language, fieldsMetrics);

		assertNotNull(searchDocumentsQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, searchDocumentsQuery.getQueryPaths(), searchDocumentsQuery.getQueryParams(), false);

		assertEquals("http://10.165.140.102:8983/solr/tenant/select?q=*:*" +
				             "&fq=content_type:PARENT&fq=channel:channel" +
				             "&json.facet={AvgSentiment : \"avg(interaction_sentiment)\", AvgHandleTime : \"avg(Meta_l_handleTime)\", AvgEmployeeResponseTime : \"avg(Meta_l_avgEmployeeResponseTime)\", AvgCustomerResponseTime : \"avg(Meta_l_avgCustomerResponseTime)\", AvgMessagesCount : \"avg(Meta_i_messagesCount)\", AvgEmployeesMessages : \"avg(Meta_i_employeesMessages)\", AvgCustomerMessages : \"avg(Meta_i_customerMessages)\"}" +
				             "&rows=0" +
							 "&wt=json&indent=true&json.nl=map",
				requestUrl);
	}

	@Test
	public void getCreateTenantQueryTest() {

		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();

		val request = solrRequestGenerator.getCreateTenantQuery("UnitTestTenant");

		assertEquals(request, null);
	}

	@Test
	public void getDeleteTenantQueryTest() {

		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();

		val request = solrRequestGenerator.getDeleteTenantQuery("UnitTestTenant");

		assertEquals(request, null);
	}

	@Test
	public void getEntityTrendInteractionsDailyVolumeSeriesQueryTest() {
		// create new SolrRequestGenerator
		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();

		String entityValue = "1/Device";
		val searchDocumentsQuery = solrRequestGenerator.getTrendInteractionsDailyVolumeSeriesQueryByType(tenant, channel, TrendType.Entities, entityValue, SpeakerQueryType.Any);

		assertNotNull(searchDocumentsQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, searchDocumentsQuery.getQueryPaths(), searchDocumentsQuery.getQueryParams(), false);

		assertEquals(
				"http://10.165.140.102:8983/solr/tenant/select?q=*:*" +
						"&fq=content_type:PARENT&fq=channel:channel&fq=(topics_f_total:1\\/Device)" +
						"&json.facet={interactionsDailyVolume: {type : terms, field : Meta_l_date_day, mincount : 1, limit : -1 }}" +
						"&rows=0&wt=json&indent=true&json.nl=map",
				requestUrl);
	}

	@Test
	public void getCheckSourceTypeInChannelQueryTest() {
		// create new SolrRequestGenerator
		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();

		val CheckSourceTypeQuery = solrRequestGenerator.getCheckSourceTypeInChannelQuery(tenant, channel, SourceType.Chat.toString());

		assertNotNull(CheckSourceTypeQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, CheckSourceTypeQuery.getQueryPaths(), CheckSourceTypeQuery.getQueryParams(), false);

		assertEquals("http://10.165.140.102:8983/solr/tenant/select?q=*:*" +
				             "&fq=channel:channel" +
				             "&fq=Meta_s_interactionType:chat" +
				             "&fq=content_type:PARENT" +
				             "&rows=1" +
				             "&wt=json&indent=true&json.nl=map",
				requestUrl);
	}

	@Test
	public void getTextElementsTrendsQueryEntityTest() {
		// create new SolrRequestGenerator
		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();

		val CheckSourceTypeQuery = solrRequestGenerator.getTextElementsTrendsQuery(tenant, channel, TrendType.Entities, "periodName", null, "1/entityValue", "sortProp", "sortDir",
		                                                                           50, SpeakerQueryType.Any);

		assertNotNull(CheckSourceTypeQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, CheckSourceTypeQuery.getQueryPaths(), CheckSourceTypeQuery.getQueryParams(), false);

		assertEquals("http://10.165.140.102:8983/solr/tenant/channel/trends/topics?period=periodName&limit=50&baseDate=NOW/MINUTE&sort=sortProp sortDir&q=*:*&prefix=2/entityValue",
		             requestUrl);
	}

	@Test
	public void getTextElementsTrendsQueryEntityCustomerTest() {
		// create new SolrRequestGenerator
		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();

		val CheckSourceTypeQuery = solrRequestGenerator.getTextElementsTrendsQuery(tenant, channel, TrendType.Entities, "periodName", null, "1/entityValue", "sortProp", "sortDir",
		                                                                           50, SpeakerQueryType.Customer);

		assertNotNull(CheckSourceTypeQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, CheckSourceTypeQuery.getQueryPaths(), CheckSourceTypeQuery.getQueryParams(), false);

		assertEquals("http://10.165.140.102:8983/solr/tenant/channel/trends/topics?period=periodName&limit=50&speaker=customer&baseDate=NOW/MINUTE&sort=sortProp sortDir&q=*:*&prefix=2/entityValue",
		             requestUrl);
	}

	@Test
	public void getTextElementsTrendsQueryKeyTermsTest() {
		// create new SolrRequestGenerator
		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();

		val CheckSourceTypeQuery = solrRequestGenerator.getTextElementsTrendsQuery(tenant, channel, TrendType.Keyterms, "periodName", null, "1/entityValue", "sortProp", "sortDir",
		                                                                           50, SpeakerQueryType.Any);

		assertNotNull(CheckSourceTypeQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, CheckSourceTypeQuery.getQueryPaths(), CheckSourceTypeQuery.getQueryParams(), false);

		assertEquals("http://10.165.140.102:8983/solr/tenant/channel/trends/terms?period=periodName&limit=50&baseDate=NOW/MINUTE&sort=sortProp sortDir&q=*:*&prefix=2/entityValue",
		             requestUrl);
	}

	@Test
	public void getTextElementsTrendsQueryKeyTermsAgentTest() {
		// create new SolrRequestGenerator
		SolrRequestGenerator solrRequestGenerator = (SolrRequestGenerator) this.createRequestGenerator();

		val CheckSourceTypeQuery = solrRequestGenerator.getTextElementsTrendsQuery(tenant, channel, TrendType.Keyterms, "periodName", null, "1/entityValue", "sortProp", "sortDir",
		                                                                           50, SpeakerQueryType.Agent);

		assertNotNull(CheckSourceTypeQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, CheckSourceTypeQuery.getQueryPaths(), CheckSourceTypeQuery.getQueryParams(), false);

		assertEquals("http://10.165.140.102:8983/solr/tenant/channel/trends/terms?period=periodName&limit=50&speaker=agent&baseDate=NOW/MINUTE&sort=sortProp sortDir&q=*:*&prefix=2/entityValue",
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
