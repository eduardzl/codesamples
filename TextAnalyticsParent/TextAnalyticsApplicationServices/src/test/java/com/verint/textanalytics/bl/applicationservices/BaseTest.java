package com.verint.textanalytics.bl.applicationservices;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;
import org.powermock.api.mockito.PowerMockito;

import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.utils.FileUtils;
import com.verint.textanalytics.model.analyze.FieldMetric;
import com.verint.textanalytics.model.analyze.MetricType;
import com.verint.textanalytics.model.analyze.StatFunction;

public class BaseTest {
	private Logger logger = Logger.getLogger(this.getClass().toString());

	protected final URL getResourceURL(String resource) {
		return Thread.currentThread().getContextClassLoader().getResource(resource);
	}

	protected String getResourceAsString(String resourcePath) throws IOException {
		URL resourceUrl = this.getResourceURL(resourcePath);

		return FileUtils.getResourceTextData(resourceUrl);
	}

	protected void createLoggerMock() {

	}

	protected List<FieldMetric> getFieldsMetrics() {
		List<FieldMetric> metrics = new ArrayList<>();

		FieldMetric avgInteractionSentiment = new FieldMetric();
		avgInteractionSentiment.setName(TAConstants.MetricsQuery.averageSentiment);
		avgInteractionSentiment.setFieldName(TAConstants.SchemaFieldNames.interactionSentiment);
		avgInteractionSentiment.setDisplayKey("METRIC_interaction_sentiment");
		avgInteractionSentiment.setType(MetricType.SENTIMENT);
		avgInteractionSentiment.setStatFunction(StatFunction.avg);
		avgInteractionSentiment.setInnerFacet(true);
		avgInteractionSentiment.setIndex(1);

		FieldMetric avgHandleTime = new FieldMetric();
		avgInteractionSentiment.setName(TAConstants.MetricsQuery.averageHandleTime);
		avgInteractionSentiment.setFieldName(TAConstants.SchemaFieldNames.handleTime);
		avgInteractionSentiment.setDisplayKey("METRIC_Meta_l_handleTime");
		avgInteractionSentiment.setType(MetricType.TIME);
		avgInteractionSentiment.setStatFunction(StatFunction.avg);
		avgInteractionSentiment.setInnerFacet(true);
		avgInteractionSentiment.setIndex(2);

		FieldMetric avgEmployeeResponseTime = new FieldMetric();
		avgInteractionSentiment.setName(TAConstants.MetricsQuery.averageEmployeeResponseTime);
		avgInteractionSentiment.setFieldName(TAConstants.SchemaFieldNames.agentAvgResponseTime);
		avgInteractionSentiment.setDisplayKey("METRIC_Meta_l_avgEmployeeResponseTime");
		avgInteractionSentiment.setType(MetricType.TIME);
		avgInteractionSentiment.setStatFunction(StatFunction.avg);
		avgInteractionSentiment.setInnerFacet(true);
		avgInteractionSentiment.setIndex(3);

		FieldMetric avgCustomerResponseTime = new FieldMetric();
		avgInteractionSentiment.setName(TAConstants.MetricsQuery.averageCustomerResponseTime);
		avgInteractionSentiment.setFieldName(TAConstants.SchemaFieldNames.customerAvgResponseTime);
		avgInteractionSentiment.setDisplayKey("METRIC_Meta_l_avgCustomerResponseTime");
		avgInteractionSentiment.setType(MetricType.TIME);
		avgInteractionSentiment.setStatFunction(StatFunction.avg);
		avgInteractionSentiment.setInnerFacet(true);
		avgInteractionSentiment.setIndex(4);

		FieldMetric avgMessagesCount = new FieldMetric();
		avgInteractionSentiment.setName(TAConstants.MetricsQuery.averageMessagesCount);
		avgInteractionSentiment.setFieldName(TAConstants.SchemaFieldNames.messagesCount);
		avgInteractionSentiment.setDisplayKey("METRIC_Meta_i_messagesCount");
		avgInteractionSentiment.setType(MetricType.NUMBER);
		avgInteractionSentiment.setStatFunction(StatFunction.avg);
		avgInteractionSentiment.setInnerFacet(true);
		avgInteractionSentiment.setIndex(5);

		FieldMetric avgEmployeeMessagesCount = new FieldMetric();
		avgInteractionSentiment.setName(TAConstants.MetricsQuery.averageEmployeeMessages);
		avgInteractionSentiment.setFieldName(TAConstants.SchemaFieldNames.agentMessagesCount);
		avgInteractionSentiment.setDisplayKey("METRIC_Meta_i_employeesMessages");
		avgInteractionSentiment.setType(MetricType.NUMBER);
		avgInteractionSentiment.setStatFunction(StatFunction.avg);
		avgInteractionSentiment.setInnerFacet(true);
		avgInteractionSentiment.setIndex(6);

		FieldMetric avgCustomerMessagesCount = new FieldMetric();
		avgInteractionSentiment.setName(TAConstants.MetricsQuery.averageCustomerMessages);
		avgInteractionSentiment.setFieldName(TAConstants.SchemaFieldNames.customerMessagesCount);
		avgInteractionSentiment.setDisplayKey("METRIC_Meta_i_customerMessages");
		avgInteractionSentiment.setType(MetricType.NUMBER);
		avgInteractionSentiment.setStatFunction(StatFunction.avg);
		avgInteractionSentiment.setInnerFacet(true);
		avgInteractionSentiment.setIndex(6);

		metrics.add(avgInteractionSentiment);
		metrics.add(avgHandleTime);
		metrics.add(avgEmployeeResponseTime);
		metrics.add(avgCustomerResponseTime);
		metrics.add(avgMessagesCount);
		metrics.add(avgEmployeeMessagesCount);
		metrics.add(avgCustomerMessagesCount);

		return metrics;
	}
}
