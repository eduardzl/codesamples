package com.verint.textanalytics.bl.applicationservices;

import com.google.common.base.Throwables;
import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionErrorCode;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionException;
import com.verint.textanalytics.common.utils.ThreadUtils;
import com.verint.textanalytics.dal.darwin.TextAnalyticsProvider;
import com.verint.textanalytics.model.analyze.FieldMetric;
import com.verint.textanalytics.model.analyze.MetricData;
import com.verint.textanalytics.model.analyze.MetricDataChange;
import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import lombok.Setter;
import lombok.val;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import propel.core.functional.tuples.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

/**
 * Metrics Service.
 * 
 * @author imor
 *
 */
public class CurrentResultSetMetricsService {

	@Autowired
	@Setter
	private TextAnalyticsProvider textAnalyticsProvider;

	@Autowired
	@Setter
	private ConfigurationManager configurationManager;

	@Autowired
	@Setter
	private ConfigurationService configurationService;

	/**
	 * @param tenant
	 *            userTenant
	 * @param channel
	 *            channel
	 * @param currentSearchContext
	 *            currentSearchContext
	 * @param backgroundContext
	 *            backgroundContext
	 * @param metricFields
	 *            metricFields
	 * @return List<Metric>
	 */
	public List<MetricDataChange> getCurrentResultSetMetrics(String tenant, String channel, SearchInteractionsContext currentSearchContext, SearchInteractionsContext backgroundContext, List<FieldMetric> metricFields) {

		List<MetricDataChange> metrics = new ArrayList<MetricDataChange>();

		// Execute concurrently
		Pair<List<MetricData>, List<MetricData>> result = null;

		result = getMetricsConcurrently(tenant, channel, currentSearchContext, backgroundContext, metricFields);
		List<MetricData> backgroundMetrics = result.getFirst();
		List<MetricData> searchMetrics = result.getSecond();

		//HashMap<String, MetricData> searchMetricsHash;
		//searchMetricsHash = createSearchMetricsHash(searchMetrics);

		// Update Background Metrics
		metrics = mergeMetricsResults(backgroundMetrics, searchMetrics);

		return metrics;
	}

	/**
	 * Retrieves metrics of result set defined by search context.
	 * @param tenant tenant
	 * @param channel channel
	 * @param searchInteractionsContext search context
	 * @param metricsToCalculate metrics to calculate
	 * @param readInteractionsCount should number of interactions be read
	 * @return list of metrics calculated on result set
	 */
	public List<MetricData> getResultSetMetrics(String tenant, String channel, SearchInteractionsContext searchInteractionsContext, List<FieldMetric> metricsToCalculate, Boolean readInteractionsCount) {

		String language = configurationService.getChannelLanguage(tenant, channel);
		return this.textAnalyticsProvider.getResultSetMetrics(tenant, channel, searchInteractionsContext, language, metricsToCalculate, readInteractionsCount);
	}


	private List<MetricDataChange> mergeMetricsResults(List<MetricData> backgroundMetrics, List<MetricData> searchMetrics) {
		List<MetricDataChange> metrics = new ArrayList<MetricDataChange>();

		MetricDataChange metric;
		MetricData foundSearchMetricData;
		for (MetricData backgroundMetricData : backgroundMetrics) {
			foundSearchMetricData = null;

			for (MetricData searchMetricData : searchMetrics) {
				if (backgroundMetricData.getName() == searchMetricData.getName()) {
					foundSearchMetricData = searchMetricData;
					break;
				}
			}

			if (foundSearchMetricData == null) {
				// metrics found on backround but not in Search - setting 0 value
				foundSearchMetricData = new MetricData(backgroundMetricData.getName(), 0.0);
			}

			metric = new MetricDataChange();
			metric.setDisplayKey(foundSearchMetricData.getName());
			metric.setBackgroundValue(backgroundMetricData.getValue());
			metric.setCurrentSearchValue(foundSearchMetricData.getValue());

			metrics.add(metric);
		}
		return metrics;
	}

	private HashMap<String, MetricData> createSearchMetricsHash(List<MetricData> searchMetrics) {
		HashMap<String, MetricData> hash = new HashMap<String, MetricData>();
		if (searchMetrics != null) {
			searchMetrics.forEach((metric) -> {
				hash.put(metric.getName(), metric);
			});
		}

		return hash;
	}

	private Pair<List<MetricData>, List<MetricData>> getMetricsConcurrently(String tenant, String channel, SearchInteractionsContext searchContext, SearchInteractionsContext backgroundContext, List<FieldMetric> metricFields) {

		ExecutorService threadPool = null;

		Pair<List<MetricData>, List<MetricData>> result = null;
		List<MetricData> resBackgroundMetrics = new ArrayList<MetricData>();
		List<MetricData> resSearchMetrics = new ArrayList<MetricData>();

		String language = configurationService.getChannelLanguage(tenant, channel);

		try {
			threadPool = Executors.newFixedThreadPool(2);

			String requestId = ThreadContext.get(TAConstants.requestId);

			val tasks = new ArrayList<Callable<Object>>();
			// 1st Task
			tasks.add(() -> {
				// place request Id into logger context
				ThreadContext.put(TAConstants.requestId, requestId);

				List<MetricData> bkList = textAnalyticsProvider.getResultSetMetrics(tenant, channel, backgroundContext, language, metricFields, false);
				return new Pair<String, List<MetricData>>("backgroundMetricsTask", bkList);
			});

			// 2nd Task
			tasks.add(() -> {
				// place request Id into logger context
				ThreadContext.put(TAConstants.requestId, requestId);

				List<MetricData> bkList = textAnalyticsProvider.getResultSetMetrics(tenant, channel, searchContext, language, metricFields, false);
				return new Pair<String, List<MetricData>>("searchMetricsTask", bkList);
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
							val res = (Pair<String, List<MetricData>>) taskResult;
							switch (res.getFirst()) {
								case "backgroundMetricsTask":
									resBackgroundMetrics = res.getSecond();
									break;
								case "searchMetricsTask":
									resSearchMetrics = res.getSecond();
									break;
								default:
									break;
							}
						}
					}
				}
			}

			result = new Pair<List<MetricData>, List<MetricData>>(resBackgroundMetrics, resSearchMetrics);

		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.MetricsError));
		} finally {
			if (threadPool != null) {
				ThreadUtils.shutdownExecutionThreadPool(threadPool);
			}
		}

		return result;
	}
}
