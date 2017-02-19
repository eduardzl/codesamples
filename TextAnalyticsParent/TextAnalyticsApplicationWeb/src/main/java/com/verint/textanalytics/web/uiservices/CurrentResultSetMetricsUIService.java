package com.verint.textanalytics.web.uiservices;

import com.verint.textanalytics.bl.applicationservices.CurrentResultSetMetricsService;
import com.verint.textanalytics.model.analyze.FieldMetric;
import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import com.verint.textanalytics.web.viewmodel.Metric;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Represents Metrics Service Widget.
 * 
 * @author imor
 *
 */
public class CurrentResultSetMetricsUIService extends BaseUIService {

	@Autowired
	private CurrentResultSetMetricsService metricsService;

	@Autowired
	private ViewModelConverter viewModelConverter;

	private List<FieldMetric> metricFields;

	/**
	 * Constructor.
	 * @param fieldsMetricsToCalculate
	 *            metrics to calculate
	 */
	public CurrentResultSetMetricsUIService(List<FieldMetric> fieldsMetricsToCalculate) {
		this.metricFields = fieldsMetricsToCalculate;
	}

	/**
	 * retrieves Metrics data.
	 * 
	 * @param i360FoundationToken
	 *            i360FoundationToken
	 * @param channel
	 *            channel
	 * @param currentSearchContext
	 *            currentSearchContext
	 * @param backgroundContext
	 *            backgroundContext
	 * @return List<Metric>
	 */
	public List<Metric> getCurrentResultSetMetrics(String i360FoundationToken, String channel, SearchInteractionsContext currentSearchContext, SearchInteractionsContext backgroundContext) {

		List<com.verint.textanalytics.model.analyze.MetricDataChange> metrics;

		val userTenant = this.getTenantFromChannel(channel, i360FoundationToken);

		metrics = metricsService.getCurrentResultSetMetrics(userTenant, channel, currentSearchContext, backgroundContext, metricFields);

		List<Metric> res = viewModelConverter.convertToViewModelMetrics(metrics);

		return res;
	}
}
