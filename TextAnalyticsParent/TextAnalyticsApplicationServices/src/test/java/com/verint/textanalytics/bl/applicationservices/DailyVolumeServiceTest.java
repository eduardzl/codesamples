package com.verint.textanalytics.bl.applicationservices;

import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionException;
import com.verint.textanalytics.dal.darwin.DarwinTextAnalyticsProvider;
import com.verint.textanalytics.dal.darwin.TextAnalyticsProvider;
import com.verint.textanalytics.model.interactions.RangeFilterField;
import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import com.verint.textanalytics.model.trends.InteractionDailyVolumeDataPoint;
import lombok.val;
import org.apache.commons.lang.NullArgumentException;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import propel.core.functional.tuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class DailyVolumeServiceTest {

	TextAnalyticsProvider textAnalyticsProvider;

	@Mock
	private ConfigurationManager configurationManager;

	@Mock
	private ApplicationConfiguration applicationConfiguration;

	@Mock
	private ConfigurationService configurationService;

	@InjectMocks
	private DailyVolumeService dailyVolumeService;

	String tenant;
	String channel;
	SearchInteractionsContext searchContext;
	SearchInteractionsContext backgroundContext;
	String language;

	@Before
	public void setUp() throws Exception {

		this.tenant = "test1";
		this.channel = "channel";
		this.searchContext = new SearchInteractionsContext();
		this.backgroundContext = new SearchInteractionsContext();
		this.language = "en";

		MockitoAnnotations.initMocks(this);

		Mockito.when(configurationManager.getApplicationConfiguration())
		       .thenReturn(applicationConfiguration);
		Mockito.when(applicationConfiguration.getDarwinRestRequestTimeout())
		       .thenReturn(30);
		Mockito.when(configurationService.getChannelLanguage(tenant, channel)).thenReturn("en");



	}

	@Test
	public void getDailyVolume_DataProperlyMerged() {

		searchContext.setRangeFilterFields(new ArrayList<RangeFilterField>());
		searchContext.getRangeFilterFields()
		             .add(new RangeFilterField());

		textAnalyticsProvider = new DarwinTextAnalyticsProvider() {
			@Override
			public java.util.List<InteractionDailyVolumeDataPoint> getInteractionsDailyVolumeSeries(String tenant, String channel, SearchInteractionsContext discoverTrendsContext,
			        String language) {

				SearchInteractionsContext context = discoverTrendsContext;

				List<InteractionDailyVolumeDataPoint> result = new ArrayList<InteractionDailyVolumeDataPoint>();

				int i = 0;

				if (context.getRangeFilterFields() != null && context.getRangeFilterFields()
				                                                     .size() > 0) {
					// search context
					result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
					                                                       .plusDays(++i), 10.0));
					result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
					                                                       .plusDays(++i), 15.0));
					result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
					                                                       .plusDays(++i), 20.0));
				} else {
					// background context
					result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
					                                                       .plusDays(++i), 100.0));
					result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
					                                                       .plusDays(++i), 150.0));
					result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
					                                                       .plusDays(++i), 200.0));

				}

				return result;
			};
		};

		dailyVolumeService.setTextAnalyticsProvider(textAnalyticsProvider);

		val result = this.dailyVolumeService.getInteractionsDailyVolume(tenant, channel, searchContext, backgroundContext);
		assertTrue(result != null);
		assertTrue(result.size() == 3);
		assertTrue(result.get(0)
		                 .getContextValue() == 10.0 && result.get(0)
		                                                     .getBackgroundValue() == 100.0);
		assertTrue(result.get(1)
		                 .getContextValue() == 15.0 && result.get(1)
		                                                     .getBackgroundValue() == 150.0);
		assertTrue(result.get(2)
		                 .getContextValue() == 20.0 && result.get(2)
		                                                     .getBackgroundValue() == 200.0);
	}

	@Test
	public void getDailyVolume_NotExistedValuesOfSearchContextDidNotIncluded() {

		searchContext.setRangeFilterFields(new ArrayList<RangeFilterField>());
		searchContext.getRangeFilterFields()
		             .add(new RangeFilterField());

		textAnalyticsProvider = new DarwinTextAnalyticsProvider() {
			@Override
			public java.util.List<InteractionDailyVolumeDataPoint> getInteractionsDailyVolumeSeries(String tenant, String channel, SearchInteractionsContext discoverTrendsContext,
			        String language) {

				SearchInteractionsContext context = discoverTrendsContext;

				List<InteractionDailyVolumeDataPoint> result = new ArrayList<InteractionDailyVolumeDataPoint>();

				int i = 0;

				if (context.getRangeFilterFields() != null && context.getRangeFilterFields()
				                                                     .size() > 0) {
					// search context
					result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
					                                                       .plusDays(++i), 10.0));
					result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
					                                                       .plusDays(++i), 15.0));
					result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
					                                                       .plusDays(++i), 20.0));
					result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
					                                                       .plusDays(++i), 25.0));
					result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
					                                                       .plusDays(++i), 30.0));
				} else {
					// background context
					result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
					                                                       .plusDays(++i), 100.0));
					result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
					                                                       .plusDays(++i), 150.0));
					result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
					                                                       .plusDays(++i), 200.0));

				}

				return result;
			};
		};

		dailyVolumeService.setTextAnalyticsProvider(textAnalyticsProvider);

		val result = this.dailyVolumeService.getInteractionsDailyVolume(tenant, channel, searchContext, backgroundContext);
		System.out.println(result.size());
		System.out.println("result==null?" + result == null);
		assertTrue(result != null);
		assertTrue(result.size() == 3);
		assertTrue(result.get(0)
		                 .getContextValue() == 10.0 && result.get(0)
		                                                     .getBackgroundValue() == 100.0);
		assertTrue(result.get(1)
		                 .getContextValue() == 15.0 && result.get(1)
		                                                     .getBackgroundValue() == 150.0);
		assertTrue(result.get(2)
		                 .getContextValue() == 20.0 && result.get(2)
		                                                     .getBackgroundValue() == 200.0);
	}

	@Test
	public void getDailyVolume_BackgroundValuesAreAllExist() {

		searchContext.setRangeFilterFields(new ArrayList<RangeFilterField>());
		searchContext.getRangeFilterFields()
		             .add(new RangeFilterField());

		textAnalyticsProvider = new DarwinTextAnalyticsProvider() {
			@Override
			public java.util.List<InteractionDailyVolumeDataPoint> getInteractionsDailyVolumeSeries(String tenant, String channel, SearchInteractionsContext discoverTrendsContext,
			        String language) {

				SearchInteractionsContext context = discoverTrendsContext;

				List<InteractionDailyVolumeDataPoint> result = new ArrayList<InteractionDailyVolumeDataPoint>();

				int i = 0;

				if (context.getRangeFilterFields() != null && context.getRangeFilterFields()
				                                                     .size() > 0) {
					// search context
					result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
					                                                       .plusDays(++i), 10.0));
					result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
					                                                       .plusDays(++i), 15.0));
					result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
					                                                       .plusDays(++i), 20.0));
				} else {
					// background context
					result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
					                                                       .plusDays(++i), 100.0));
					result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
					                                                       .plusDays(++i), 150.0));
					result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
					                                                       .plusDays(++i), 200.0));
					result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
					                                                       .plusDays(++i), 250.0));
					result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
					                                                       .plusDays(++i), 300.0));
				}

				return result;
			};
		};

		dailyVolumeService.setTextAnalyticsProvider(textAnalyticsProvider);

		val result = this.dailyVolumeService.getInteractionsDailyVolume(tenant, channel, searchContext, backgroundContext);
		assertTrue(result != null);
		assertTrue(result.size() == 5);
		assertTrue(result.get(0)
		                 .getContextValue() == 10.0 && result.get(0)
		                                                     .getBackgroundValue() == 100.0);
		assertTrue(result.get(1)
		                 .getContextValue() == 15.0 && result.get(1)
		                                                     .getBackgroundValue() == 150.0);
		assertTrue(result.get(2)
		                 .getContextValue() == 20.0 && result.get(2)
		                                                     .getBackgroundValue() == 200.0);
		assertTrue(result.get(3)
		                 .getContextValue() == 0.0 && result.get(3)
		                                                    .getBackgroundValue() == 250.0);
		assertTrue(result.get(4)
		                 .getContextValue() == 0.0 && result.get(4)
		                                                    .getBackgroundValue() == 300.0);
	}

	@Test
	public void getDailyVolume_EmptyResults() {

		searchContext.setRangeFilterFields(new ArrayList<RangeFilterField>());
		searchContext.getRangeFilterFields()
		             .add(new RangeFilterField());

		textAnalyticsProvider = new DarwinTextAnalyticsProvider() {
			@Override
			public java.util.List<InteractionDailyVolumeDataPoint> getInteractionsDailyVolumeSeries(String tenant, String channel, SearchInteractionsContext discoverTrendsContext,
			        String language) {

				List<InteractionDailyVolumeDataPoint> result = new ArrayList<InteractionDailyVolumeDataPoint>();

				return result;
			};
		};

		dailyVolumeService.setTextAnalyticsProvider(textAnalyticsProvider);

		val result = this.dailyVolumeService.getInteractionsDailyVolume(tenant, channel, searchContext, backgroundContext);
		assertTrue(result != null);
		assertTrue(result.size() == 0);
	}

	@Test
	public void getDailyVolume_Timeout() throws Exception {

		DailyVolumeService dailyVolumeService = new DailyVolumeService() {

			@Override
			protected Callable<Object> createFutureTask(DataRequestTaskType taskType, String tenant, String channel, SearchInteractionsContext context) {

				Callable<Object> futureTask = null;

				if (taskType == DataRequestTaskType.BACKGROUND_POINTS) {
					futureTask = () -> {
						TimeUnit.SECONDS.sleep(5);
						return new Pair<String, List<InteractionDailyVolumeDataPoint>>("backgroundPointsTask", null);
					};
				}

				if (taskType == DataRequestTaskType.SEARCH_POINTS) {
					futureTask = () -> {
						return new Pair<String, List<InteractionDailyVolumeDataPoint>>("searchPointsTask", null);
					};
				}

				return futureTask;
			}
		};
		dailyVolumeService.setConfigurationManager(this.configurationManager);

		try {
			dailyVolumeService.getInteractionsDailyVolume(tenant, channel, searchContext, backgroundContext);
			assertTrue(false);
		} catch (Exception ex) {
			assertTrue(true);
		}
	}

	@Test
	public void getDailyVolume_ThreadsRun1And2Seconds() throws Exception {

		DailyVolumeService dailyVolumeService = new DailyVolumeService() {

			@Override
			protected Callable<Object> createFutureTask(DataRequestTaskType taskType, String tenant, String channel, SearchInteractionsContext context) {

				Callable<Object> futureTask = null;
				List<InteractionDailyVolumeDataPoint> result = new ArrayList<InteractionDailyVolumeDataPoint>();

				if (taskType == DataRequestTaskType.BACKGROUND_POINTS) {
					futureTask = () -> {
						int i = 0;
						result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
						                                                       .plusDays(++i), 100.0));
						result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
						                                                       .plusDays(++i), 150.0));
						result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
						                                                       .plusDays(++i), 200.0));
						result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
						                                                       .plusDays(++i), 250.0));
						result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
						                                                       .plusDays(++i), 300.0));

						TimeUnit.SECONDS.sleep(2);
						return new Pair<String, List<InteractionDailyVolumeDataPoint>>("backgroundPointsTask", result);
					};

				}

				if (taskType == DataRequestTaskType.SEARCH_POINTS) {
					futureTask = () -> {
						int i = 0;
						result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
						                                                       .plusDays(++i), 10.0));
						result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
						                                                       .plusDays(++i), 15.0));
						result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
						                                                       .plusDays(++i), 20.0));

						TimeUnit.SECONDS.sleep(1);
						return new Pair<String, List<InteractionDailyVolumeDataPoint>>("searchPointsTask", result);
					};
				}

				return futureTask;
			}
		};
		dailyVolumeService.setConfigurationManager(this.configurationManager);

		try {
			val res = dailyVolumeService.getInteractionsDailyVolume(tenant, channel, searchContext, backgroundContext);
			assertTrue(res != null);
			assertTrue(res.size() == 5);
			assertTrue(res.get(0)
			              .getContextValue() == 10.0 && res.get(0)
			                                               .getBackgroundValue() == 100.0);
			assertTrue(res.get(1)
			              .getContextValue() == 15.0 && res.get(1)
			                                               .getBackgroundValue() == 150.0);
			assertTrue(res.get(2)
			              .getContextValue() == 20.0 && res.get(2)
			                                               .getBackgroundValue() == 200.0);
			assertTrue(res.get(3)
			              .getContextValue() == 0.0 && res.get(3)
			                                              .getBackgroundValue() == 250.0);
			assertTrue(res.get(4)
			              .getContextValue() == 0.0 && res.get(4)
			                                              .getBackgroundValue() == 300.0);

		} catch (Exception ex) {
			assertTrue(true);
		}
	}

	@Test
	public void getDailyVolume_DataProperlyMerged_New() {

		searchContext.setRangeFilterFields(new ArrayList<RangeFilterField>());
		searchContext.getRangeFilterFields()
		             .add(new RangeFilterField());

		textAnalyticsProvider = new DarwinTextAnalyticsProvider() {
			@Override
			public java.util.List<InteractionDailyVolumeDataPoint> getInteractionsDailyVolumeSeries(String tenant, String channel, SearchInteractionsContext discoverTrendsContext,
			        String language) {

				SearchInteractionsContext context = discoverTrendsContext;

				List<InteractionDailyVolumeDataPoint> result = new ArrayList<InteractionDailyVolumeDataPoint>();

				int i = 0;

				if (context.getRangeFilterFields() != null && context.getRangeFilterFields()
				                                                     .size() > 0) {
					// search context
					result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
					                                                       .plusDays(++i), 10.0));
					result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
					                                                       .plusDays(++i), 15.0));
					result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
					                                                       .plusDays(++i), 20.0));
				} else {
					// background context
					result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
					                                                       .plusDays(++i), 100.0));
					result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
					                                                       .plusDays(++i), 150.0));
					result.add(new InteractionDailyVolumeDataPoint(DateTime.now()
					                                                       .plusDays(++i), 200.0));

				}

				return result;
			}
		};
	}

	@Test(expected = TextQueryExecutionException.class)
	public void getDailyVolume_Exception() {

		searchContext.setRangeFilterFields(new ArrayList<RangeFilterField>());
		searchContext.getRangeFilterFields()
		             .add(new RangeFilterField());

		textAnalyticsProvider = new DarwinTextAnalyticsProvider() {
			@Override
			public java.util.List<InteractionDailyVolumeDataPoint> getInteractionsDailyVolumeSeries(String tenant, String channel, SearchInteractionsContext discoverTrendsContext,
			        String language) {

				throw new NullArgumentException("");
			};
		};

		//dailyVolumeService.setConfigurationService(configurationService);
		dailyVolumeService.setTextAnalyticsProvider(textAnalyticsProvider);

		this.dailyVolumeService.getInteractionsDailyVolume(tenant, channel, searchContext, backgroundContext);
	}
}
