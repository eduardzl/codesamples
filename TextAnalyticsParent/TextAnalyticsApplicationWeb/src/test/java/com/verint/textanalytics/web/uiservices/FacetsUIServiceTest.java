package com.verint.textanalytics.web.uiservices;

import com.verint.textanalytics.bl.applicationservices.FacetsService;
import com.verint.textanalytics.bl.applicationservices.facet.textelements.TextElementsFacetService;
import com.verint.textanalytics.bl.security.MembershipProvider;
import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.model.analyze.FieldMetric;
import com.verint.textanalytics.model.analyze.MetricData;
import com.verint.textanalytics.model.facets.*;
import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import com.verint.textanalytics.model.security.Channel;
import com.verint.textanalytics.model.security.Tenant;
import com.verint.textanalytics.model.security.User;
import lombok.val;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class FacetsUIServiceTest extends BaseUIServiceTest {

	@InjectMocks
	private FacetsUIService facetsUIService;

	// this mock will inject to the tested object
	@Mock
	private ConfigurationManager configurationManager;

	@Mock
	private MembershipProvider membershipProvider;

	@Mock
	protected ApplicationConfiguration applicationConfiguration;

	@Mock
	protected FacetsService facetsService;

	@Mock
	protected TextElementsFacetService textElementsFacetsService;

	@Spy
	protected ViewModelFilter viewModelFilter;

	@Spy
	private ViewModelConverter viewModelConverter;

	private String i360FoundationToken = "qwertgasdgffasfas";
	private String tenant = "tenant1";
	private String channel = "channel1";
	private SearchInteractionsContext searchContext = new SearchInteractionsContext();
	private List<String> sentimentFacetsQueries = new ArrayList<String>(Arrays.asList("-2", "-1", "0", "1", "2"));
	private String facetName = "interaction_sentiment";

	private Facet facet = new Facet();

	private TextElementsFacetNode textElementsFacetNode1 = TextElementsFacetNode.buildFromPathString("1/A", false);
	private TextElementsFacetNode textElementsFacetNode2 = TextElementsFacetNode.buildFromPathString("1/B", false);
	private TextElementsFacetNode textElementsFacetNode3 = TextElementsFacetNode.buildFromPathString("1/C", false);

	private TextElementsFacetNode textElementsFacetNode11 = TextElementsFacetNode.buildFromPathString("2/A/A", false);
	private TextElementsFacetNode textElementsFacetNode12 = TextElementsFacetNode.buildFromPathString("2/A/B", false);
	private TextElementsFacetNode textElementsFacetNode13 = TextElementsFacetNode.buildFromPathString("2/A/C", false);

	private TextElementsFacetNode textElementsFacetNode21 = TextElementsFacetNode.buildFromPathString("2/B/A", false);
	private TextElementsFacetNode textElementsFacetNode22 = TextElementsFacetNode.buildFromPathString("2/B/B", false);
	private TextElementsFacetNode textElementsFacetNode23 = TextElementsFacetNode.buildFromPathString("2/B/C", false);

	private TextElementsFacetNode textElementsFacetNode31 = TextElementsFacetNode.buildFromPathString("2/C/A", false);
	private TextElementsFacetNode textElementsFacetNode32 = TextElementsFacetNode.buildFromPathString("2/C/B", false);
	private TextElementsFacetNode textElementsFacetNode33 = TextElementsFacetNode.buildFromPathString("2/C/C", false);

	@Before
	public void setUp() throws Exception {

		// this will execute the injection
		MockitoAnnotations.initMocks(this);

		// @formatter:off

		// the user to return from Foundation membership provider
		User user = new User();
		user.setName("user");
		user.setUserID(123);

		List<Tenant> tenantsList = new ArrayList<Tenant>();
		Tenant tenant1 = new Tenant();
		tenant1.setId(tenant);
		tenant1.setDisplayName("t1Name");
		tenant1.setEmId(111111);

		Channel channel1=new Channel();
		channel1.setId(channel);
		channel1.setDisplayName(channel+"DN");

		List<Channel> channels=new ArrayList<>();
		channels.add(channel1);

		tenant1.setChannels(channels);

		tenantsList.add(tenant1);

		user.setTenantsList(tenantsList);

		Mockito.when(membershipProvider.getUser(this.i360FoundationToken)).thenReturn(user);

		facetsUIService.setMembershipProvider(membershipProvider);

		viewModelFilter.setConfigurationManager(configurationManager);
		
		List<FieldMetric> metricFields = this.getFieldsMetrics();
		viewModelConverter.setMetricFields(metricFields);

		Mockito.when(configurationManager.getApplicationConfiguration()).thenReturn(applicationConfiguration);
		Mockito.when(applicationConfiguration.getDebugTenant()).thenReturn(tenant);
		Mockito.when(applicationConfiguration.getRelationsFacetRootTopLimit()).thenReturn(2);
		Mockito.when(applicationConfiguration.getRelationsFacetRootTopLimit()).thenReturn(2);
		
		Mockito.when(applicationConfiguration.getEntitiesFacetLeavesTopLimit()).thenReturn(7);
		Mockito.when(applicationConfiguration.getRelationsFacetLeavesTopLimit()).thenReturn(7);		
		
		Mockito.when(applicationConfiguration.getRelationsFacetDescendantsTopLimit()).thenReturn(2);

		List<Facet> facetMock = new ArrayList<Facet>(
                         Arrays.asList(facet.setFieldName(facetName).setTotalCount(5).setType(FacetType.SingleValues).setValues(new ArrayList<FacetResultGroup>(
                            Arrays.asList(new FacetSentimentSingleValueResultGroup().setCount(2).setPercentage(10.0).setTitle("-2").setTitleKey("titleKey-2"),
                                          new FacetSentimentSingleValueResultGroup().setCount(2).setPercentage(20.0).setTitle("-1").setTitleKey("titleKey-1"),
                                          new FacetSentimentSingleValueResultGroup().setCount(3).setPercentage(20.0).setTitle("0").setTitleKey("titleKey-0"),
                                          new FacetSentimentSingleValueResultGroup().setCount(4).setPercentage(20.0).setTitle("1").setTitleKey("titleKey-1"),
                                          new FacetSentimentSingleValueResultGroup().setCount(5).setPercentage(20.0).setTitle("2").setTitleKey("titleKey-2"))))));

		Mockito.when(facetsService.getSentimentFacet(tenant, channel, searchContext, sentimentFacetsQueries)).thenReturn(facetMock);

		MetricData metricDataVolume1 = new MetricData().setName(TextElementMetricType.Volume.toString()).setValue(1);
		MetricData metricDataVolume2 = new MetricData().setName(TextElementMetricType.Volume.toString()).setValue(2);
		MetricData metricDataVolume3 = new MetricData().setName(TextElementMetricType.Volume.toString()).setValue(3);
		
		MetricData metricDataHandleTime1 = new MetricData().setName(TextElementMetricType.AvgHandleTime.toString()).setValue(1);
		MetricData metricDataHandleTime2 = new MetricData().setName(TextElementMetricType.AvgHandleTime.toString()).setValue(2);
		MetricData metricDataHandleTime3 = new MetricData().setName(TextElementMetricType.AvgHandleTime.toString()).setValue(3);
		
		MetricData metricDataAvgMessagesCount1 = new MetricData().setName(TextElementMetricType.AvgMessagesCount.toString()).setValue(1);
		MetricData metricDataAvgMessagesCount2 = new MetricData().setName(TextElementMetricType.AvgMessagesCount.toString()).setValue(2);
		MetricData metricDataAvgMessagesCount3 = new MetricData().setName(TextElementMetricType.AvgMessagesCount.toString()).setValue(3);
		
		ArrayList<MetricData> metrics1 =  new ArrayList<MetricData>(Arrays.asList(metricDataVolume1,metricDataHandleTime1,metricDataAvgMessagesCount1));
		ArrayList<MetricData> metrics2 =  new ArrayList<MetricData>(Arrays.asList(metricDataVolume2,metricDataHandleTime2,metricDataAvgMessagesCount2));
		ArrayList<MetricData> metrics3 =  new ArrayList<MetricData>(Arrays.asList(metricDataVolume3,metricDataHandleTime3,metricDataAvgMessagesCount3));
		
		
								
		textElementsFacetNode11.setNumberOfInteractions(3).setMetrics(metrics3);
		textElementsFacetNode12.setNumberOfInteractions(2).setMetrics(metrics2);
		textElementsFacetNode13.setNumberOfInteractions(1).setMetrics(metrics1);
		
		textElementsFacetNode21.setNumberOfInteractions(3).setMetrics(metrics3);
		textElementsFacetNode22.setNumberOfInteractions(1).setMetrics(metrics1);
		textElementsFacetNode23.setNumberOfInteractions(2).setMetrics(metrics2);
		
		textElementsFacetNode31.setNumberOfInteractions(2).setMetrics(metrics2);
		textElementsFacetNode32.setNumberOfInteractions(1).setMetrics(metrics1);
		textElementsFacetNode33.setNumberOfInteractions(3).setMetrics(metrics3);
		
		List<TextElementsFacetNode> children1 = new ArrayList<TextElementsFacetNode>(Arrays.asList(textElementsFacetNode11, textElementsFacetNode12 ,textElementsFacetNode13));
		List<TextElementsFacetNode> children2 = new ArrayList<TextElementsFacetNode>(Arrays.asList(textElementsFacetNode21, textElementsFacetNode22 ,textElementsFacetNode23));
		List<TextElementsFacetNode> children3 = new ArrayList<TextElementsFacetNode>(Arrays.asList(textElementsFacetNode31, textElementsFacetNode32 ,textElementsFacetNode33));
		
		textElementsFacetNode1.setNumberOfInteractions(1).setMetrics(metrics1).setChildren(children1);
		textElementsFacetNode2.setNumberOfInteractions(2).setMetrics(metrics2).setChildren(children2);
		textElementsFacetNode3.setNumberOfInteractions(3).setMetrics(metrics3).setChildren(children3);

		TextElementFacetResult getTextElementsFacetRes = new TextElementFacetResult();
		getTextElementsFacetRes.setTextElementsFacetNodeList(new ArrayList<TextElementsFacetNode>(Arrays.asList(textElementsFacetNode1,textElementsFacetNode2,textElementsFacetNode3)));

		Mockito.when(textElementsFacetsService.getTextElementsFacet(tenant, channel, searchContext, searchContext,  TextElementType.Relations, SpeakerQueryType.Any, false, Mockito.anyInt(), Mockito.anyInt(), TextElementsFacetCalculationType.FacetOnChildren))
		       .thenReturn(getTextElementsFacetRes);
		
		Mockito.when(textElementsFacetsService.getTextElementsFacet(tenant, channel, searchContext, searchContext,  TextElementType.Relations, SpeakerQueryType.Any, false, Mockito.anyInt(), Mockito.anyInt(), TextElementsFacetCalculationType.FacetOnChildren))
		       .thenReturn(getTextElementsFacetRes);
		
		// @formatter:on
	}

	@Test
	@Ignore
	public void getSentimentFacetSearchTest() {
		List<Facet> resFacets = facetsUIService.getSentimentFacetSearch(i360FoundationToken, channel, searchContext, sentimentFacetsQueries);

		assertNotNull(resFacets);
		assertEquals(5, resFacets.get(0).getValues().size());

		assertEquals(facetName, resFacets.get(0).getFieldName());

		Integer facetCount = 0;

		for (int i = 0; i < 5; i++) {
			facetCount = resFacets.get(0).getValues().get(i).getCount();
			assertTrue(facetCount >= 0);
		}
	}

	@Test
	@Ignore
	public void getTextElementsFacetTreeTest() {
		val resFacets = facetsUIService.getTextElementsFacetTree(i360FoundationToken, channel, searchContext, searchContext, TextElementType.Relations, SpeakerQueryType.Any);
		assertNotNull(resFacets);
		assertEquals(2, resFacets.getChildren().size());
		assertEquals(2, resFacets.getChildren().get(0).getChildren().size());
		assertEquals(2, resFacets.getChildren().get(1).getChildren().size());
	}
}