/**
 * 
 */
package com.verint.textanalytics.dal.darwin;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.verint.textanalytics.model.facets.TextElementMetricType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.collection.*;
import com.verint.textanalytics.common.diagnostics.OperationType;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionException;
import com.verint.textanalytics.dal.rest.RestDataAccess;
import com.verint.textanalytics.model.documentSchema.ChannelSchema;
import com.verint.textanalytics.model.documentSchema.DocumentHierarchyType;
import com.verint.textanalytics.model.documentSchema.FieldDataType;
import com.verint.textanalytics.model.documentSchema.TenantSchema;
import com.verint.textanalytics.model.documentSchema.TextSchemaField;
import com.verint.textanalytics.model.facets.SpeakerQueryType;
import com.verint.textanalytics.model.facets.TextElementType;
import com.verint.textanalytics.model.interactions.SearchInteractionsContext;

/**
 * @author imor
 *
 */
public class DarwinTextAnalyticsProviderTest extends BaseTest {

	@InjectMocks
	private DarwinTextAnalyticsProvider darwinTextAnalyticsProvider;

	@Mock
	private RestDataAccess restDataAccess;

	@Mock
	private RequestGenerator requestGenerator;

	@Mock
	private ResponseConverter responseConverter;

	@Mock
	private ConfigurationManager configurationManager;

	private String channel;

	private String solrURL;

	private HashMap<String, String> postRawBodyHeaders;

	private int textElementsLimit = 30;

	public DarwinTextAnalyticsProviderTest() {

		channel = "1271";
		solrURL = "http://10.165.140.102:8983/solr";

		this.postRawBodyHeaders = new HashMap<>();
		this.postRawBodyHeaders.put("Content-Type", "application/x-www-form-urlencoded");

		MockitoAnnotations.initMocks(this);

		ApplicationConfiguration mockedApplicationConfiguration = mock(ApplicationConfiguration.class);
		when(mockedApplicationConfiguration.getDarwinTextEngineServiceBaseUrl()).thenReturn(solrURL);
		when(mockedApplicationConfiguration.getDarwinRestRequestTimeout()).thenReturn(3000);

		when(configurationManager.getApplicationConfiguration()).thenReturn(mockedApplicationConfiguration);

		TextEngineSchemaService mockedTextEngineConfiguration = mock(TextEngineSchemaService.class);
		when(mockedTextEngineConfiguration.getTenants()).thenReturn(new ArrayList<TenantSchema>());

		ChannelSchema channelobj = new ChannelSchema();
		channelobj.setName(channel);
		List<TextSchemaField> fields = new ArrayList<TextSchemaField>();
		fields.add(new TextSchemaField().setName(channel).setDisplayFieldName(channel).setFieldDataType(FieldDataType.Int).setDocumentHierarchyType(DocumentHierarchyType.Utterance));
		channelobj.setFields(fields);
		TenantSchema tenantObj = new TenantSchema();

		List<ChannelSchema> channels = new ArrayList<ChannelSchema>();
		channels.add(channelobj);
		tenantObj.setChannels(channels);

		darwinTextAnalyticsProvider.initialize();
	}

	// More EntitiesFacet tests

	/**
	 * The basic flow validates that all the methods are called
	 */
	@SuppressWarnings("unchecked")
	@Test(expected = TextQueryExecutionException.class)
	public void getEntitiesFacetTest_BasicFlowMethods() {

		TextElementMetricType volume = TextElementMetricType.Volume;
		darwinTextAnalyticsProvider.getTextElementsFacet(null, null, null, null, TextElementType.Entities, "1",  Arrays.asList(volume),volume,
		                                                 SpeakerQueryType.Any, false, false, textElementsLimit);

		Mockito.verify(requestGenerator, Mockito.times(1)).getFacetQuery(Mockito.anyString(), Mockito.anyString(), Mockito.any(SearchInteractionsContext.class),
		                                                                 Mockito.any(String.class), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyString());

		Mockito.verify(restDataAccess, Mockito.times(1)).executeGetRequest(Mockito.anyString(), Mockito.anyString(), Mockito.anyListOf(String.class),
		                                                                   (MultivaluedStringMap) Mockito.anyObject());

		Mockito.verify(responseConverter, Mockito.times(1)).getTextElementsFacets(null, Mockito.anyListOf(String.class), false, false);
	}

	/**
	 * The basic flow validates that all the methods are called
	 */
	@SuppressWarnings("unchecked")
	@Test(expected = TextQueryExecutionException.class)
	public void getRelationsFacetTest_BasicFlowMethods() {
		TextElementMetricType volume = TextElementMetricType.Volume;
		darwinTextAnalyticsProvider.getTextElementsFacet(null, null, null, null, TextElementType.Relations, "1",
	                                                    Arrays.asList(volume), volume,
	                                                    SpeakerQueryType.Any, false, false, textElementsLimit);

		Mockito.verify(requestGenerator, Mockito.times(1)).getFacetQuery(Mockito.anyString(), Mockito.anyString(), Mockito.any(SearchInteractionsContext.class),
		                                                                 Mockito.any(String.class), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyString());

		Mockito.verify(restDataAccess, Mockito.times(1)).executeGetRequest(Mockito.anyString(), Mockito.anyString(), Mockito.anyListOf(String.class),
		                                                                   (MultivaluedStringMap) Mockito.anyObject());

		Mockito.verify(responseConverter, Mockito.times(1)).getTextElementsFacets(null, Mockito.anyListOf(String.class), false, false);
	}

	/**
	 * Exception raised
	 */
	@Test(expected = TextQueryExecutionException.class)
	public void getEntitiesFacetTest_BasicFlowMethods_Exception() {

		Mockito.when(requestGenerator.getFacetQuery(Mockito.anyString(), Mockito.anyString(), Mockito.any(SearchInteractionsContext.class), Mockito.any(String.class),
		                                            Mockito.anyObject(), Mockito.anyObject(), Mockito.anyString())).thenReturn(null);

		TextElementMetricType volume = TextElementMetricType.Volume;
		darwinTextAnalyticsProvider.getTextElementsFacet(null, null, null, null, TextElementType.Entities, "1",
		                                                  Arrays.asList(volume), volume,
		                                                  SpeakerQueryType.Any, false, false, textElementsLimit);
	}

	/**
	 * Exception raised
	 */
	@Test(expected = TextQueryExecutionException.class)
	public void getRelationsFacetTest_BasicFlowMethods_Exception() {

		Mockito.when(requestGenerator.getFacetQuery(Mockito.anyString(), Mockito.anyString(), Mockito.any(SearchInteractionsContext.class), Mockito.any(String.class),
		                                            Mockito.anyObject(), Mockito.anyObject(), Mockito.anyString())).thenReturn(null);

		TextElementMetricType volume = TextElementMetricType.Volume;
		darwinTextAnalyticsProvider.getTextElementsFacet(null, null, null, null, TextElementType.Relations,"1",
		                                                 Arrays.asList(volume), volume,
		                                                 SpeakerQueryType.Any, false, false, textElementsLimit);
	}
}
