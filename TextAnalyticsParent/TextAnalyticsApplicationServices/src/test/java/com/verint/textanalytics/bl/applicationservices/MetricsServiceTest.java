package com.verint.textanalytics.bl.applicationservices;

import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionException;
import com.verint.textanalytics.dal.darwin.TextAnalyticsProvider;
import com.verint.textanalytics.model.analyze.FieldMetric;
import com.verint.textanalytics.model.analyze.MetricData;
import com.verint.textanalytics.model.analyze.MetricDataChange;
import com.verint.textanalytics.model.interactions.RangeFilterField;
import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import lombok.val;
import org.apache.commons.lang.NullArgumentException;
import org.junit.Before;
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

public class MetricsServiceTest extends BaseTest {

	private static final double DELTA = 0.001;

	@Mock
	private TextAnalyticsProvider textAnalyticsProvider;

	@Mock
	private ConfigurationManager configurationManager;

	@Mock
	private ApplicationConfiguration applicationConfiguration;

	@Mock
	private ConfigurationService configurationService;

	@InjectMocks
	private CurrentResultSetMetricsService metricsService;

	private String tenant;
	private String channel;
	private SearchInteractionsContext searchContext;
	private SearchInteractionsContext backgroundContext;
	private List<FieldMetric> metricFields;
	private String language;

	@Before
	public void setUp() throws Exception {

		this.tenant = "test1";
		this.channel = "channel";
		this.searchContext = new SearchInteractionsContext();
		this.searchContext.setTerms(new ArrayList<String>(Arrays.asList("yeah", "iphone")));
		this.backgroundContext = new SearchInteractionsContext();
		this.language = "en";
		this.metricFields = this.getFieldsMetrics();

		//@formatter:off
		MockitoAnnotations.initMocks(this);

		Mockito.when(configurationManager.getApplicationConfiguration()).thenReturn(applicationConfiguration);
		Mockito.when(applicationConfiguration.getDarwinRestRequestTimeout()).thenReturn(30);

		Mockito.when(textAnalyticsProvider.getResultSetMetrics(tenant, channel, searchContext, language, metricFields, false))
				.thenReturn(new ArrayList<MetricData>(
						Arrays.asList(new MetricData(TAConstants.MetricsQuery.averageSentiment, 0.1), 
						              new MetricData(TAConstants.MetricsQuery.averageHandleTime, 1.1), 
						              new MetricData(TAConstants.MetricsQuery.averageMessagesCount, 2.1))));

		Mockito.when(textAnalyticsProvider.getResultSetMetrics(tenant, channel, backgroundContext, language, metricFields, false))
				.thenReturn(new ArrayList<MetricData>(
						Arrays.asList(new MetricData(TAConstants.MetricsQuery.averageSentiment, 0.2), 
						              new MetricData(TAConstants.MetricsQuery.averageHandleTime, 1.2), 
						              new MetricData(TAConstants.MetricsQuery.averageMessagesCount, 2.2))));

		Mockito.when(configurationService.getChannelLanguage(tenant, channel)).thenReturn(language);
		//@formatter:on
	}

	@Test
	public void getMetricsTest() {

		searchContext.setRangeFilterFields(new ArrayList<RangeFilterField>());
		searchContext.getRangeFilterFields().add(new RangeFilterField());

		val result = metricsService.getCurrentResultSetMetrics(tenant, channel, searchContext, backgroundContext, metricFields);

		assertNotNull(result);
		assertEquals(3, result.size());

		MetricDataChange metricChange = result.stream().filter(m -> m.getDisplayKey().equals(TAConstants.MetricsQuery.averageSentiment)).findFirst().get();
		assertEquals(metricChange.getDisplayKey(), TAConstants.MetricsQuery.averageSentiment);
		assertEquals(metricChange.getCurrentSearchValue(), 0.1, DELTA);
		assertEquals(metricChange.getBackgroundValue(), 0.2, DELTA);

		metricChange = result.stream().filter(m -> m.getDisplayKey().equals(TAConstants.MetricsQuery.averageHandleTime)).findFirst().get();
		assertEquals(metricChange.getDisplayKey(), TAConstants.MetricsQuery.averageHandleTime);
		assertEquals(metricChange.getCurrentSearchValue(), 1.1, DELTA);
		assertEquals(metricChange.getBackgroundValue(), 1.2, DELTA);

		metricChange = result.stream().filter(m -> m.getDisplayKey().equals(TAConstants.MetricsQuery.averageMessagesCount)).findFirst().get();
		assertEquals(metricChange.getDisplayKey(), TAConstants.MetricsQuery.averageMessagesCount);
		assertEquals(metricChange.getCurrentSearchValue(), 2.1, DELTA);
		assertEquals(metricChange.getBackgroundValue(), 2.2, DELTA);
	}

	@Test(expected = TextQueryExecutionException.class)
	public void getMetricsExceptionTest() {

		searchContext.setRangeFilterFields(new ArrayList<RangeFilterField>());
		searchContext.getRangeFilterFields().add(new RangeFilterField());

		Mockito.when(textAnalyticsProvider.getResultSetMetrics(tenant, channel, searchContext, language, metricFields, false)).thenThrow(new NullArgumentException(""));

		metricsService.getCurrentResultSetMetrics(tenant, channel, searchContext, backgroundContext, metricFields);
	}
}
