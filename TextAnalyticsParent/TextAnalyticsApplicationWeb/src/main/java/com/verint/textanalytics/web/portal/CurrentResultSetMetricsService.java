package com.verint.textanalytics.web.portal;

import com.codahale.metrics.Timer;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.diagnostics.OperationType;
import com.verint.textanalytics.common.diagnostics.PerformanceMetrics;
import com.verint.textanalytics.common.security.OperationPrivelegesAnnotation;
import com.verint.textanalytics.common.security.OperationPrivelegesAnnotation.PrivilegeType;
import com.verint.textanalytics.common.utils.JSONUtils;
import com.verint.textanalytics.web.uiservices.CurrentResultSetMetricsUIService;
import com.verint.textanalytics.web.viewmodel.Metric;
import com.verint.textanalytics.web.viewmodel.requestparams.CurrentResultSetMetricsParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Operates request data for Analyze Metrics chart.
 * 
 * @author imor
 *
 */
@Path("/CurrentResultSetMetricsService")
public class CurrentResultSetMetricsService {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	private CurrentResultSetMetricsUIService metricsUIService;

	@Autowired
	private PerformanceMetrics performanceMetrics;

	/**
	 * retrieves data for analyze Metrics chart.
	 * 
	 * @param i360FoundationToken
	 *            i360FoundationToken
	 * @param params
	 *            all required params
	 * @return collection of Metrics
	 */
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { PrivilegeType.USEAPPLICATION })
	@Path("getCurrentResultSetMetrics")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public List<Metric> getCurrentResultSetMetrics(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, CurrentResultSetMetricsParams params) {
		Timer.Context context = this.performanceMetrics.startTimedOperation(OperationType.CurrentResultSetMetricsRest);

		logger.debug("getCurrentResultSetMetrics request invoked. Request params  - {}", () -> JSONUtils.getObjectJSON(params));

		List<Metric> metrics = metricsUIService.getCurrentResultSetMetrics(i360FoundationToken, params.getChannel(), params.getCurrentSearchContext(),
		                                                                   params.getBackgroundContext());

		this.performanceMetrics.stopTimedOperation(context);

		return metrics;
	}
}
