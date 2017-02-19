package com.verint.textanalytics.web.viewmodel;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.*;

/**
 * Represents metric limits data.
 * 
 * @author imor
 *
 */

public class MetricsLimits {

	private Table<Integer, String, MetricLimitsData> metricsLimits;

	/**
	 * C'tor.
	 */
	public MetricsLimits() {
		this.metricsLimits = HashBasedTable.create();
	}

	/**
	 * updates Metrics limit data for specific level and metric.
	 * @param level
	 *            level number
	 * @param metricData
	 *            metric data
	 */
	public void updateMetricLimit(int level, MetricData metricData) {
		this.ensureMetricLimitData(level, metricData.getName());

		MetricLimitsData metricLimit = this.metricsLimits.get(level, metricData.getName());
		if (metricData.getValue() > metricLimit.getMax()) {
			metricLimit.setMax(metricData.getValue());
		}

		if (metricData.getValue() < metricLimit.getMin()) {
			metricLimit.setMin(metricData.getValue());
		}
	}

	private void ensureMetricLimitData(int level, String metricName) {
		if (!metricsLimits.contains(level, metricName)) {
			metricsLimits.put(level, metricName, new MetricLimitsData(metricName));
		}
	}

	/**
	 * Generates hash of levels with list of metrics limits.
	 * @return hash of levels with list of metrics limits.
	 */
	public Map<Integer, Collection<MetricLimitsData>> getMetricsLimitsMapByLevel() {
		Map<Integer, Collection<MetricLimitsData>> result = new HashMap<>();

		Set<Integer> levels = metricsLimits.rowKeySet();
		for (Integer level : levels) {
			result.put(level, metricsLimits.row(level).values());
		}

		return result;
	}
}
