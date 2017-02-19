package com.verint.textanalytics.bl.applicationservices;

import com.google.common.base.Throwables;
import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionErrorCode;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionException;
import com.verint.textanalytics.common.utils.ThreadUtils;
import com.verint.textanalytics.dal.darwin.TextAnalyticsProvider;
import com.verint.textanalytics.model.analyze.AnalyzeInteractionsDailyVolumePoints;
import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import com.verint.textanalytics.model.trends.InteractionDailyVolumeDataPoint;
import lombok.Setter;
import lombok.val;
import org.apache.logging.log4j.ThreadContext;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import propel.core.functional.tuples.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Retrieves data Daily Volume.
 *
 * @author NShunewich
 */
public class DailyVolumeService {

	@Autowired
	@Setter
	private TextAnalyticsProvider textAnalyticsProvider;

	@Autowired
	@Setter
	private ConfigurationManager configurationManager;

	@Autowired
	@Setter
	private ConfigurationService configurationService;

	private Comparator<InteractionDailyVolumeDataPoint> dateComparator;

	/**
	 * Enumerator Represents the type of future task to be executed.
	 *
	 * @author NShunewich
	 */
	public enum DataRequestTaskType {
		BACKGROUND_POINTS, SEARCH_POINTS
	}


	/**
	 * Retrieves data for the graph.
	 *
	 * @param tenant            tenant
	 * @param channel           channel
	 * @param searchContext     searchContext
	 * @param backgroundContext backgroundContext
	 * @return collection of points
	 */
	public List<AnalyzeInteractionsDailyVolumePoints> getInteractionsDailyVolume(String tenant, String channel, SearchInteractionsContext searchContext, SearchInteractionsContext backgroundContext) {

		List<AnalyzeInteractionsDailyVolumePoints> dailyVolumePoints = new ArrayList<AnalyzeInteractionsDailyVolumePoints>();

		try {
			// Execute concurrently
			Pair<List<InteractionDailyVolumeDataPoint>, List<InteractionDailyVolumeDataPoint>> result = null;

			result = getDailyVolumeConcurrently(tenant, channel, backgroundContext, searchContext);

			List<InteractionDailyVolumeDataPoint> backgroundPoints = result.getFirst();
			List<InteractionDailyVolumeDataPoint> searchPoints = result.getSecond();

			// Create hashmap to improve merge performance
			HashMap<LocalDate, InteractionDailyVolumeDataPoint> searchPointsHash;
			searchPointsHash = createDailyVolumePointsHashmap(searchPoints);

			// Merge two points collections
			dailyVolumePoints = mergeAndSortDailyVolumePoints(backgroundPoints, searchPointsHash);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.DailyVolumeError));
		}

		return dailyVolumePoints;
	}

	private HashMap<LocalDate, InteractionDailyVolumeDataPoint> createDailyVolumePointsHashmap(List<InteractionDailyVolumeDataPoint> points) {
		HashMap<LocalDate, InteractionDailyVolumeDataPoint> ctPointsMap = new HashMap<LocalDate, InteractionDailyVolumeDataPoint>();
		if (points != null) {
			points.forEach((point) -> {
				ctPointsMap.put(point.getDate().toLocalDate(), point);
			});
		}

		return ctPointsMap;
	}

	private List<AnalyzeInteractionsDailyVolumePoints> mergeAndSortDailyVolumePoints(List<InteractionDailyVolumeDataPoint> backgroundPoints, HashMap<LocalDate, InteractionDailyVolumeDataPoint> searchPointsHash) {

		List<AnalyzeInteractionsDailyVolumePoints> dailyVolumePoints = null;

		// @formatter:off
		dailyVolumePoints = backgroundPoints.stream()
		                                    .map(bkPoints -> {
												Double contextYPoint = (double) 0;

												InteractionDailyVolumeDataPoint searchPoint = searchPointsHash.get(bkPoints.getDate().toLocalDate());

												if (null != searchPoint) {
													contextYPoint = searchPoint.getValue();
												}

												return new AnalyzeInteractionsDailyVolumePoints(bkPoints.getDate(), bkPoints.getValue(), contextYPoint);
											})
		                                    .sorted(Comparator.comparing(AnalyzeInteractionsDailyVolumePoints::getDate))
				                            .collect(Collectors.toList());
		//@formatter:on

		return dailyVolumePoints;
	}

	private Pair<List<InteractionDailyVolumeDataPoint>, List<InteractionDailyVolumeDataPoint>> getDailyVolumeConcurrently(String tenant, String channel, SearchInteractionsContext backgroundContext, SearchInteractionsContext searchContext) {

		ExecutorService threadPool = null;

		Pair<List<InteractionDailyVolumeDataPoint>, List<InteractionDailyVolumeDataPoint>> result = null;
		List<InteractionDailyVolumeDataPoint> resBackgroundPoints = new ArrayList<InteractionDailyVolumeDataPoint>();
		List<InteractionDailyVolumeDataPoint> resSearchPoints = new ArrayList<InteractionDailyVolumeDataPoint>();

		try {

			String language = configurationService.getChannelLanguage(tenant, channel);

			threadPool = Executors.newFixedThreadPool(2);

			val tasks = new ArrayList<Callable<Object>>();

			// get request id which presents in the ThreadContext of each request 
			String requestId = ThreadContext.get(TAConstants.requestId);

			// 1st Task
			tasks.add(() -> {
				// place request in logger context
				ThreadContext.put(TAConstants.requestId, requestId);

				List<InteractionDailyVolumeDataPoint> backgroundDataPoints = textAnalyticsProvider.getInteractionsDailyVolumeSeries(tenant, channel, backgroundContext, language);
				return new Pair<DataRequestTaskType, List<InteractionDailyVolumeDataPoint>>(DataRequestTaskType.BACKGROUND_POINTS, backgroundDataPoints);
			});

			// 2nd Task
			tasks.add(() -> {
				// place request in logger context
				ThreadContext.put(TAConstants.requestId, requestId);

				List<InteractionDailyVolumeDataPoint> dataPoints = textAnalyticsProvider.getInteractionsDailyVolumeSeries(tenant, channel, searchContext, language);
				return new Pair<DataRequestTaskType, List<InteractionDailyVolumeDataPoint>>(DataRequestTaskType.SEARCH_POINTS, dataPoints);
			});

			ApplicationConfiguration appConfig = configurationManager.getApplicationConfiguration();
			int requestTimeout = appConfig.getDarwinRestRequestTimeout();

			List<Future<Object>> taskResults = threadPool.invokeAll(tasks, requestTimeout, TimeUnit.SECONDS);

			if (taskResults != null) {
				for (Future<?> future : taskResults) {
					if (future.isDone()) {
						Object taskResult = future.get();
						if (taskResult != null) {
							@SuppressWarnings("unchecked")
							val res = (Pair<DataRequestTaskType, List<InteractionDailyVolumeDataPoint>>) taskResult;

							DataRequestTaskType type = res.getFirst();

							if (type == DataRequestTaskType.BACKGROUND_POINTS) {
								resBackgroundPoints = res.getSecond();
							}

							if (type == DataRequestTaskType.SEARCH_POINTS) {
								resSearchPoints = res.getSecond();
							}
						}
					}
				}
			}

			result = new Pair<List<InteractionDailyVolumeDataPoint>, List<InteractionDailyVolumeDataPoint>>(resBackgroundPoints, resSearchPoints);

		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.DailyVolumeError));
		} finally {
			if (threadPool != null) {
				ThreadUtils.shutdownExecutionThreadPool(threadPool);
			}
		}

		return result;
	}

	protected Callable<Object> createFutureTask(DataRequestTaskType taskType, String tenant, String channel, SearchInteractionsContext context) {

		String language = configurationService.getChannelLanguage(tenant, channel);

		return () -> {
			List<InteractionDailyVolumeDataPoint> bkList = textAnalyticsProvider.getInteractionsDailyVolumeSeries(tenant, channel, context, language);
			return new Pair<DataRequestTaskType, List<InteractionDailyVolumeDataPoint>>(taskType, bkList);
		};
	}

}
