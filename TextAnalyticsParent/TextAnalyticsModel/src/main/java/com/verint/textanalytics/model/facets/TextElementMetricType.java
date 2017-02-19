package com.verint.textanalytics.model.facets;

/**
 * TextElementMetricType.
 * 
 * @author imor
 *
 */
public enum TextElementMetricType {
	//@formatter:off
	AvgSentiment(0),
	Volume(1), 
	AvgHandleTime(2),
	AvgMessagesCount(3), 
	AvgEmployeesMessages(4),
	AvgCustomerMessages(5), 
	AvgEmployeeResponseTime(6), 
	AvgCustomerResponseTime(7), 
	CategoriesColor(8),
	CorrelationPercentage(9);
	//@formatter:on

	private int metricType;

	TextElementMetricType(int type) {
		this.metricType = type;
	}

	public int getMetricType() {
		return this.metricType;
	}
}
