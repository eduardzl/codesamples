package com.verint.textanalytics.web.uiservices;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.verint.textanalytics.bl.security.EMConfigurationManager;
import com.verint.textanalytics.bl.security.FileMembershipProvider;
import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.model.analyze.FieldMetric;
import com.verint.textanalytics.model.analyze.MetricType;
import com.verint.textanalytics.model.analyze.StatFunction;

public class BaseUIServiceTest {

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
		avgHandleTime.setName(TAConstants.MetricsQuery.averageHandleTime);
		avgHandleTime.setFieldName(TAConstants.SchemaFieldNames.handleTime);
		avgHandleTime.setDisplayKey("METRIC_Meta_l_handleTime");
		avgHandleTime.setType(MetricType.TIME);
		avgHandleTime.setStatFunction(StatFunction.avg);
		avgHandleTime.setInnerFacet(true);
		avgHandleTime.setIndex(2);

		FieldMetric avgEmployeeResponseTime = new FieldMetric();
		avgEmployeeResponseTime.setName(TAConstants.MetricsQuery.averageEmployeeResponseTime);
		avgEmployeeResponseTime.setFieldName(TAConstants.SchemaFieldNames.agentAvgResponseTime);
		avgEmployeeResponseTime.setDisplayKey("METRIC_Meta_l_avgEmployeeResponseTime");
		avgEmployeeResponseTime.setType(MetricType.TIME);
		avgEmployeeResponseTime.setStatFunction(StatFunction.avg);
		avgEmployeeResponseTime.setInnerFacet(true);
		avgEmployeeResponseTime.setIndex(3);

		FieldMetric avgCustomerResponseTime = new FieldMetric();
		avgCustomerResponseTime.setName(TAConstants.MetricsQuery.averageCustomerResponseTime);
		avgCustomerResponseTime.setFieldName(TAConstants.SchemaFieldNames.customerAvgResponseTime);
		avgCustomerResponseTime.setDisplayKey("METRIC_Meta_l_avgCustomerResponseTime");
		avgCustomerResponseTime.setType(MetricType.TIME);
		avgCustomerResponseTime.setStatFunction(StatFunction.avg);
		avgCustomerResponseTime.setInnerFacet(true);
		avgCustomerResponseTime.setIndex(4);

		FieldMetric avgMessagesCount = new FieldMetric();
		avgMessagesCount.setName(TAConstants.MetricsQuery.averageMessagesCount);
		avgMessagesCount.setFieldName(TAConstants.SchemaFieldNames.messagesCount);
		avgMessagesCount.setDisplayKey("METRIC_Meta_i_messagesCount");
		avgMessagesCount.setType(MetricType.NUMBER);
		avgMessagesCount.setStatFunction(StatFunction.avg);
		avgMessagesCount.setInnerFacet(true);
		avgMessagesCount.setIndex(5);

		FieldMetric avgEmployeeMessagesCount = new FieldMetric();
		avgEmployeeMessagesCount.setName(TAConstants.MetricsQuery.averageEmployeeMessages);
		avgEmployeeMessagesCount.setFieldName(TAConstants.SchemaFieldNames.agentMessagesCount);
		avgEmployeeMessagesCount.setDisplayKey("METRIC_Meta_i_employeesMessages");
		avgEmployeeMessagesCount.setType(MetricType.NUMBER);
		avgEmployeeMessagesCount.setStatFunction(StatFunction.avg);
		avgEmployeeMessagesCount.setInnerFacet(true);
		avgEmployeeMessagesCount.setIndex(6);

		FieldMetric avgCustomerMessagesCount = new FieldMetric();
		avgCustomerMessagesCount.setName(TAConstants.MetricsQuery.averageCustomerMessages);
		avgCustomerMessagesCount.setFieldName(TAConstants.SchemaFieldNames.customerMessagesCount);
		avgCustomerMessagesCount.setDisplayKey("METRIC_Meta_i_customerMessages");
		avgCustomerMessagesCount.setType(MetricType.NUMBER);
		avgCustomerMessagesCount.setStatFunction(StatFunction.avg);
		avgCustomerMessagesCount.setInnerFacet(true);
		avgCustomerMessagesCount.setIndex(6);

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