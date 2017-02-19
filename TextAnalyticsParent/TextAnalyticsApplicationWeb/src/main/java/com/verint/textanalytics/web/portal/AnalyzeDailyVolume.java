package com.verint.textanalytics.web.portal;

import com.codahale.metrics.Timer;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.diagnostics.OperationType;
import com.verint.textanalytics.common.diagnostics.PerformanceMetrics;
import com.verint.textanalytics.common.security.OperationPrivelegesAnnotation;
import com.verint.textanalytics.common.security.OperationPrivelegesAnnotation.PrivilegeType;
import com.verint.textanalytics.web.uiservices.DailyVolumeUIService;
import com.verint.textanalytics.web.viewmodel.AnalyzeDailyVolumeDataPoints;
import com.verint.textanalytics.web.viewmodel.requestparams.AnalyzeDailyVolumeParams;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Operates request data for Analyze Daily Volume chart.
 * 
 * @author NShunewich
 *
 */
@Path("/AnalyzeDailyVolume")
public class AnalyzeDailyVolume {

	@Autowired
	private DailyVolumeUIService dailyVolumeService;

	@Autowired
	private PerformanceMetrics performanceMetrics;

	/**
	 * retrieves data for Daily Volume chart.
	 * 
	 * @param i360FoundationToken
	 *            i360FoundationToken
	 * @param params
	 *            all required params
	 * @return collection of points
	 */
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { PrivilegeType.USEAPPLICATION })
	@Path("getDailyVolume")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public List<AnalyzeDailyVolumeDataPoints> getDailyVolume(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, AnalyzeDailyVolumeParams params) {
		Timer.Context context = this.performanceMetrics.startTimedOperation(OperationType.InteractionsDailyVolumeSeriesRest);

		List<AnalyzeDailyVolumeDataPoints> result = dailyVolumeService.getInteractionsDailyVolume(i360FoundationToken, params.getChannel(), params.getSearchContext(),
		                                                                                          params.getBackgroundContext());

		this.performanceMetrics.stopTimedOperation(context);

		return result;
	}
}
