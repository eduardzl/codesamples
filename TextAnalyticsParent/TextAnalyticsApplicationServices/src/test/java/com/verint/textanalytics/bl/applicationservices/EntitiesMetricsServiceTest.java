//package com.verint.textanalytics.bl.applicationservices;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//import lombok.val;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.MockitoAnnotations;
//
//import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
//import com.verint.textanalytics.common.configuration.ConfigurationManager;
//import com.verint.textanalytics.dal.darwin.TextAnalyticsProvider;
//import com.verint.textanalytics.model.analyze.EntityMetric;
//import com.verint.textanalytics.model.analyze.EntityMetricType;
//import com.verint.textanalytics.model.analyze.EntitySentimentsMetric;
//import com.verint.textanalytics.model.interactions.RangeFilterField;
//import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
//
//public class EntitiesMetricsServiceTest {
//
//	private static final double DELTA = 0.001;
//
//	@Mock
//	private TextAnalyticsProvider textAnalyticsProvider;
//
//	@Mock
//	private ConfigurationManager configurationManager;
//
//	@Mock
//	private ApplicationConfiguration applicationConfiguration;
//
//	@InjectMocks
//	private EntitiesMetricsService metricsService;
//
//	String tenant;
//	String channel;
//	SearchInteractionsContext searchContext;
//	SearchInteractionsContext backgroundContext;
//	List<String> metricFields;
//	String language;
//	EntityMetricType x1 = EntityMetricType.Volume;
//	EntityMetricType y1 = EntityMetricType.Sentiment;
//	EntityMetricType x2 = EntityMetricType.Volume;
//	EntityMetricType y2 = EntityMetricType.HandleTime;
//	EntityMetricType x3 = EntityMetricType.Sentiment;
//	EntityMetricType y3 = EntityMetricType.Sentiment;
//
//	private String entity;
//
//	private double xVal1 = 1.1;
//	private double yVal1 = 1.2;
//	private double wrongVal = -100;
//
//	@Before
//	public void setUp() throws Exception {
//
//		this.tenant = "test1";
//		this.channel = "channel";
//		this.searchContext = new SearchInteractionsContext();
//		this.searchContext.setTerms(new ArrayList<String>(Arrays.asList("yeah", "iphone")));
//		this.language = "en";
//
//		MockitoAnnotations.initMocks(this);
//
//		Mockito.when(configurationManager.getApplicationConfiguration())
//		       .thenReturn(applicationConfiguration);
//		Mockito.when(applicationConfiguration.getDarwinRestRequestTimeout())
//		       .thenReturn(30);
//
//		Mockito.when(textAnalyticsProvider.getEntitiesMetrics(tenant, channel, searchContext, x1, y1, language))
//		       .thenReturn(new ArrayList<EntityMetric>(Arrays.asList(new EntityMetric().setEntity(entity)
//		                                                                               .setX(xVal1)
//		                                                                               .setY(wrongVal))));
//
//		Mockito.when(textAnalyticsProvider.getEntitiesMetrics(tenant, channel, searchContext, x2, y2, language))
//		       .thenReturn(new ArrayList<EntityMetric>(Arrays.asList(new EntityMetric().setEntity(entity)
//		                                                                               .setX(xVal1)
//		                                                                               .setY(yVal1))));
//
//		Mockito.when(textAnalyticsProvider.getEntitiesMetrics(tenant, channel, searchContext, x3, y3, language))
//		       .thenReturn(new ArrayList<EntityMetric>(Arrays.asList(new EntityMetric().setEntity(entity)
//		                                                                               .setX(wrongVal)
//		                                                                               .setY(wrongVal))));
//
//		Mockito.when(textAnalyticsProvider.getEntitiesSentimentsMetrics(tenant, channel, searchContext, language))
//		       .thenReturn(new ArrayList<EntitySentimentsMetric>(Arrays.asList(new EntitySentimentsMetric().setEntity(entity)
//		                                                                                                   .setSentimentAvg(yVal1))));
//	}
//
//	@Test
//	public void getMetricsTest_oneInnerField() {
//
//		searchContext.setRangeFilterFields(new ArrayList<RangeFilterField>());
//		searchContext.getRangeFilterFields()
//		             .add(new RangeFilterField());
//
//		val result = metricsService.getEntitiesMetrics(tenant, channel, searchContext, x1, y1, language);
//
//		assertNotNull(result);
//		assertEquals(1, result.size());
//
//		int i = 0;
//		assertEquals(entity, result.get(i)
//		                           .getEntity());
//		assertEquals(xVal1, result.get(i)
//		                          .getX(), DELTA);
//		assertEquals(yVal1, result.get(i)
//		                          .getY(), DELTA);
//	}
//
//	@Test
//	public void getMetricsTest_noInnerField() {
//
//		searchContext.setRangeFilterFields(new ArrayList<RangeFilterField>());
//		searchContext.getRangeFilterFields()
//		             .add(new RangeFilterField());
//
//		val result = metricsService.getEntitiesMetrics(tenant, channel, searchContext, x2, y2, language);
//
//		assertNotNull(result);
//		assertEquals(1, result.size());
//
//		int i = 0;
//		assertEquals(entity, result.get(i)
//		                           .getEntity());
//		assertEquals(xVal1, result.get(i)
//		                          .getX(), DELTA);
//		assertEquals(yVal1, result.get(i)
//		                          .getY(), DELTA);
//	}
//
//	@Test
//	public void getMetricsTest_twoInnerField() {
//
//		searchContext.setRangeFilterFields(new ArrayList<RangeFilterField>());
//		searchContext.getRangeFilterFields()
//		             .add(new RangeFilterField());
//
//		val result = metricsService.getEntitiesMetrics(tenant, channel, searchContext, x3, y3, language);
//
//		assertNotNull(result);
//		assertEquals(1, result.size());
//
//		int i = 0;
//		assertEquals(entity, result.get(i)
//		                           .getEntity());
//		assertEquals(yVal1, result.get(i)
//		                          .getX(), DELTA);
//		assertEquals(yVal1, result.get(i)
//		                          .getY(), DELTA);
//	}
// }
