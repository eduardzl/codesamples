package com.verint.textanalytics.bl.applicationservices;

import com.verint.textanalytics.bl.applicationservices.facet.textelements.TextElementsFacetService;
import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.dal.darwin.TextAnalyticsProvider;
import com.verint.textanalytics.model.facets.*;
import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FacetsServiceTest {

	@InjectMocks
	private TextElementsFacetService facetsService;

	private static final double DELTA = 0.001;

	@Mock
	private TextAnalyticsProvider textAnalyticsProvider;

	@Mock
	private ConfigurationManager configurationManager;

	@Mock
	private ApplicationConfiguration applicationConfiguration;

	String tenant;
	String channel;
	SearchInteractionsContext searchContext;
	SearchInteractionsContext backgroundContext;
	List<String> metricFields;
	String language;
	List<String> facetsQueries;
	String facet1;
	String facet2;

	private final int topLevelLimit = 30;
	private final int descendantsLimit = 30;

	/**
	 * 
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		this.tenant = "test1";
		this.channel = "channel";
		this.searchContext = new SearchInteractionsContext();
		this.searchContext.setTerms(Arrays.asList("yeah", "iphone"));
		this.backgroundContext = new SearchInteractionsContext();
		this.metricFields = Arrays.asList("interaction_sentiment", "Meta_l_handleTime", "Meta_l_avgEmployeeResponseTime");
		this.language = "en";

		MockitoAnnotations.initMocks(this);

		Mockito.when(configurationManager.getApplicationConfiguration()).thenReturn(applicationConfiguration);
		Mockito.when(applicationConfiguration.getDarwinRestRequestTimeout()).thenReturn(30);

		TextElementsFacetNode topLevelNode = TextElementsFacetNode.buildFromPathString("1/Fee", false).setNumberOfInteractions(200);

		// first level of facet mock
		Mockito.when(textAnalyticsProvider.getTextElementsFacet(tenant, channel, searchContext, language, TextElementType.Entities,
		                                                        "1", Arrays.asList(TextElementMetricType.Volume),
		                                                        TextElementMetricType.Volume,
		                                                        SpeakerQueryType.Any, false, false, topLevelLimit))
		                                    .thenReturn(new ArrayList<TextElementsFacetNode>(
		                                                                Arrays.asList(TextElementsFacetNode.buildFromPathString("1/Fee", false).setNumberOfInteractions(240))));


		Mockito.when(textAnalyticsProvider.getTextElementsChildrenMetrics(tenant, channel, searchContext, language, TextElementType.Entities,
		                                                                  Arrays.asList(topLevelNode), Arrays.asList(TextElementMetricType.Volume), TextElementMetricType.Volume,
		                                                                  SpeakerQueryType.Any,
		                                                                  false, false, descendantsLimit))
		       .thenReturn(new ArrayList<TextElementsFacetNode>(
				       Arrays.asList(TextElementsFacetNode.buildFromPathString("2/Fee/charge", false).setNumberOfInteractions(200),
				                     TextElementsFacetNode.buildFromPathString("2/Fee/price", false).setNumberOfInteractions(40))));


		Mockito.when(textAnalyticsProvider.getResultSetInteractionsQuantity(tenant, channel, searchContext, language)).thenReturn(400);
	}

	@Test
	@Ignore
	public void getEntitiesFacetTest() {

		TextElementMetricType volume = TextElementMetricType.Volume;
		TextElementFacetResult result = facetsService.getTextElementsFacet(tenant, channel, searchContext, searchContext, TextElementType.Entities, SpeakerQueryType.Any, false,
		                                                                   topLevelLimit, descendantsLimit, TextElementsFacetCalculationType.FacetOnChildren);

		List<TextElementsFacetNode> list = result.getTextElementsFacetNodeList();
		assertNotNull(result);
		assertEquals(2, list.size());

		int i = 0;
		assertEquals("2/Fee/charge", list.get(i).getValue());
		assertEquals("charge", list.get(i).getName());
		assertEquals(200, list.get(i).getNumberOfInteractions());
		assertEquals(50.0, list.get(i).getPercentage(), DELTA);

		i = 1;
		assertEquals("2/Fee/price", list.get(i).getValue());
		assertEquals("price", list.get(i).getName());
		assertEquals(40, list.get(i).getNumberOfInteractions());
		assertEquals(10.0, list.get(i).getPercentage(), DELTA);

	}

	@Test
	@Ignore
	public void getEntitiesFacetWithStatsTest() {

		TextElementFacetResult result = facetsService.getTextElementsFacet(tenant, channel, searchContext, searchContext, TextElementType.Entities, SpeakerQueryType.Any, false,
		                                                                   topLevelLimit, descendantsLimit, TextElementsFacetCalculationType.FacetOnChildren);

		List<TextElementsFacetNode> list = result.getTextElementsFacetNodeList();

		assertNotNull(result);
		assertEquals(2, list.size());

		int i = 0;
		assertEquals("2/Fee/charge", list.get(i).getValue());
		assertEquals("charge", list.get(i).getName());
		assertEquals(200, list.get(i).getNumberOfInteractions());
		assertEquals(50.0, list.get(i).getPercentage(), DELTA);
		assertEquals("AvgSentiment", list.get(i).getMetrics().get(0).getName());
		assertEquals(1.2, list.get(i).getMetrics().get(0).getValue(), DELTA);

		i = 1;
		assertEquals("2/Fee/price", list.get(i).getValue());
		assertEquals("price", list.get(i).getName());
		assertEquals(40, list.get(i).getNumberOfInteractions());
		assertEquals(10.0, list.get(i).getPercentage(), DELTA);
		assertEquals("AvgSentiment", list.get(i).getMetrics().get(0).getName());
		assertEquals(-0.33, list.get(i).getMetrics().get(0).getValue(), DELTA);

	}

	@Test
	@Ignore
	public void getRelationsFacetTest() {

		TextElementFacetResult result = facetsService.getTextElementsFacet(tenant, channel, searchContext, searchContext, TextElementType.Relations, SpeakerQueryType.Any, false,
		                                                                   topLevelLimit, descendantsLimit, TextElementsFacetCalculationType.FacetOnChildren);

		List<TextElementsFacetNode> list = result.getTextElementsFacetNodeList();

		assertNotNull(result);
		assertEquals(2, list.size());

		int i = 0;
		assertEquals("2/Fee/charge", list.get(i).getValue());
		assertEquals("charge", list.get(i).getName());
		assertEquals(200, list.get(i).getNumberOfInteractions());
		assertEquals(50.0, list.get(i).getPercentage(), DELTA);

		i = 1;
		assertEquals("2/Fee/price", list.get(i).getValue());
		assertEquals("price", list.get(i).getName());
		assertEquals(40, list.get(i).getNumberOfInteractions());
		assertEquals(10.0, list.get(i).getPercentage(), DELTA);

	}

	@Test
	@Ignore
	public void getRelationsFacetWithStatsTest() {

		TextElementFacetResult result = facetsService.getTextElementsFacet(tenant, channel, searchContext, searchContext, TextElementType.Relations, SpeakerQueryType.Any, false,
		                                                                   topLevelLimit, descendantsLimit, TextElementsFacetCalculationType.FacetOnChildren);

		List<TextElementsFacetNode> list = result.getTextElementsFacetNodeList();

		assertNotNull(result);
		assertEquals(2, list.size());

		int i = 0;
		assertEquals("2/Fee/charge", list.get(i).getValue());
		assertEquals("charge", list.get(i).getName());
		assertEquals(200, list.get(i).getNumberOfInteractions());
		assertEquals(50.0, list.get(i).getPercentage(), DELTA);
		assertEquals("AvgSentiment", list.get(i).getMetrics().get(0).getName());
		assertEquals(1.2, list.get(i).getMetrics().get(0).getValue(), DELTA);

		i = 1;
		assertEquals("2/Fee/price", list.get(i).getValue());
		assertEquals("price", list.get(i).getName());
		assertEquals(40, list.get(i).getNumberOfInteractions());
		assertEquals(10.0, list.get(i).getPercentage(), DELTA);
		assertEquals("AvgSentiment", list.get(i).getMetrics().get(0).getName());
		assertEquals(-0.33, list.get(i).getMetrics().get(0).getValue(), DELTA);

	}
}
