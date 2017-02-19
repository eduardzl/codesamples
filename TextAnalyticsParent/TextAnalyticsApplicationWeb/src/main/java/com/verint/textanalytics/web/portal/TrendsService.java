package com.verint.textanalytics.web.portal;

import com.codahale.metrics.Timer;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.diagnostics.OperationType;
import com.verint.textanalytics.common.diagnostics.PerformanceMetrics;
import com.verint.textanalytics.common.security.OperationPrivelegesAnnotation;
import com.verint.textanalytics.common.security.OperationPrivelegesAnnotation.PrivilegeType;
import com.verint.textanalytics.common.utils.JSONUtils;
import com.verint.textanalytics.model.facets.SpeakerQueryType;
import com.verint.textanalytics.model.trends.TrendType;
import com.verint.textanalytics.web.uiservices.TrendsUIService;
import com.verint.textanalytics.web.viewmodel.requestparams.DiscoverTrendsParams;
import com.verint.textanalytics.web.viewmodel.requestparams.InteractionsDailyVolumeParams;
import com.verint.textanalytics.web.viewmodel.requestparams.TrendSeriesParams;
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
 * Trends service.
 * 
 * @author EZlotnik
 *
 */
@Path("/TrendsService")
public class TrendsService {

	@Autowired
	private TrendsUIService trendsUIService;

	@Autowired
	private PerformanceMetrics performanceMetrics;

	private final Logger logger = LogManager.getLogger(this.getClass());

	/**
	 * REST method to retrieve Entities Trends.
	 * 
	 * @param i360FoundationToken
	 *            authentication token.
	 * @param discoverTrendsParams
	 *            discover trends search context.
	 * @return an hierarchy of entities trends.
	 */
	@Path("getEntityTrends")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public List<com.verint.textanalytics.web.viewmodel.TextElementTrend> getEntityTrends(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final DiscoverTrendsParams discoverTrendsParams) {
		List<com.verint.textanalytics.web.viewmodel.TextElementTrend> trends = null;

		Timer.Context context = this.performanceMetrics.startTimedOperation(OperationType.EntitiesTrendsRest);

		logger.debug("Get Entities Trends request invoked with params  = {}", () -> JSONUtils.getObjectJSON(discoverTrendsParams));

		String channel = discoverTrendsParams.getChannel();
		String textElement = discoverTrendsParams.getValue();
		String sortProperty = discoverTrendsParams.getSortProperty();
		String sortDirection = discoverTrendsParams.getSortDirection();
		SpeakerQueryType speaker = discoverTrendsParams.getSpeaker();

		trends = this.trendsUIService.getTextElementsTrends(i360FoundationToken, channel, discoverTrendsParams.getSearchContext(), TrendType.Entities, textElement, sortProperty,
		                                                    sortDirection, speaker);

		this.performanceMetrics.stopTimedOperation(context);

		return trends;
	}

	/**
	 * REST method to retrieve Relation Trends.
	 * 
	 * @param i360FoundationToken
	 *            authentication token.
	 * @param discoverTrendsParams
	 *            discover trends search context.
	 * @return an hierarchy of entities trends.
	 */
	@Path("getRelationTrends")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public List<com.verint.textanalytics.web.viewmodel.TextElementTrend> getRelationTrends(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final DiscoverTrendsParams discoverTrendsParams) {
		List<com.verint.textanalytics.web.viewmodel.TextElementTrend> trends = null;

		Timer.Context context = this.performanceMetrics.startTimedOperation(OperationType.RelationsTrendsRest);

		logger.debug("Get Relations Trends request invoked with params = {}", () -> JSONUtils.getObjectJSON(discoverTrendsParams));

		String channel = discoverTrendsParams.getChannel();
		String textElementValue = discoverTrendsParams.getValue();
		String sortProperty = discoverTrendsParams.getSortProperty();
		String sortDirection = discoverTrendsParams.getSortDirection();
		SpeakerQueryType speaker = discoverTrendsParams.getSpeaker();

		trends = this.trendsUIService.getTextElementsTrends(i360FoundationToken, channel, discoverTrendsParams.getSearchContext(), TrendType.Relations, textElementValue,
		                                                    sortProperty, sortDirection, speaker);

		this.performanceMetrics.stopTimedOperation(context);

		return  trends;
	}

	/**
	 * REST method to retrieve Keyword Trends.
	 * 
	 * @param i360FoundationToken
	 *            authentication token.
	 * @param discoverTrendsParams
	 *            discover trends search context.
	 * @return an hierarchy of entities trends.
	 */
	@Path("getKeywordTrends")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public List<com.verint.textanalytics.web.viewmodel.TextElementTrend> getKeywordTrends(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final DiscoverTrendsParams discoverTrendsParams) {
		List<com.verint.textanalytics.web.viewmodel.TextElementTrend> trends = null;

		Timer.Context context = this.performanceMetrics.startTimedOperation(OperationType.KeyTermsTrendsRest);

		logger.debug("Get KeyWords Trends request invoked with params = {}", () -> JSONUtils.getObjectJSON(discoverTrendsParams));

		String channel = discoverTrendsParams.getChannel();
		String textElementValue = discoverTrendsParams.getValue();
		String sortProperty = discoverTrendsParams.getSortProperty();
		String sortDirection = discoverTrendsParams.getSortDirection();
		SpeakerQueryType speaker = discoverTrendsParams.getSpeaker();

		trends = this.trendsUIService.getTextElementsTrends(i360FoundationToken, channel, discoverTrendsParams.getSearchContext(), TrendType.Keyterms, textElementValue,
		                                                    sortProperty, sortDirection, speaker);

		this.performanceMetrics.stopTimedOperation(context);

		return  trends;
	}

	/**
	 * REST method to retrieve Keyword Trends.
	 *
	 * @param i360FoundationToken
	 *            authentication token.
	 * @param discoverTrendsParams
	 *            discover trends search context.
	 * @return an hierarchy of entities trends.
	 */
	@Path("getCategoryTrends")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public List<com.verint.textanalytics.web.viewmodel.TextElementTrend> getCategoryTrends(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final DiscoverTrendsParams discoverTrendsParams) {
		List<com.verint.textanalytics.web.viewmodel.TextElementTrend> trends = null;

		Timer.Context context = this.performanceMetrics.startTimedOperation(OperationType.CategoriesTrendsRest);

		logger.debug("Get Category Trends request invoked with params = {}", () -> JSONUtils.getObjectJSON(discoverTrendsParams));

		String channel = discoverTrendsParams.getChannel();
		String textElementValue = discoverTrendsParams.getValue();
		String sortProperty = discoverTrendsParams.getSortProperty();
		String sortDirection = discoverTrendsParams.getSortDirection();

		trends = this.trendsUIService.getCategoryTrends(i360FoundationToken, channel, discoverTrendsParams.getSearchContext(), textElementValue, sortProperty, sortDirection);

		this.performanceMetrics.stopTimedOperation(context);

		return  trends;
	}

	/**
	 * REST method to retrieves an entity series data.
	 *
	 * @param i360FoundationToken  authentication token.
	 * @param discoverTrendsParams discover trends search context.
	 * @return a series for entity
	 */
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { PrivilegeType.USEAPPLICATION })
	@Path("getEntityTrendSeries")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public List<com.verint.textanalytics.web.viewmodel.InteractionDailyVolumeDataPoint> getEntityTrendSeries(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final TrendSeriesParams discoverTrendsParams) {
		List<com.verint.textanalytics.web.viewmodel.InteractionDailyVolumeDataPoint> result = null;

		Timer.Context context = this.performanceMetrics.startTimedOperation(OperationType.EntityInteractionsDailyVolumeSeriesRest);

		logger.debug("Get Entity Trend Series request invoked with params = {}", () -> JSONUtils.getObjectJSON(discoverTrendsParams));

		result = this.trendsUIService.getDailyVolumeTrendSeriesByType(i360FoundationToken, discoverTrendsParams.getValue(), discoverTrendsParams.getChannel(), TrendType.Entities,
		                                                              discoverTrendsParams.getSearchContext(), discoverTrendsParams.getClientTimeZoneOffset(),
		                                                              discoverTrendsParams.getSpeaker());

		this.performanceMetrics.stopTimedOperation(context);

		return result;
	}

	/**
	 * REST method to retrieves an relation series data.
	 *
	 * @param i360FoundationToken  authentication token.
	 * @param discoverTrendsParams discover trends search context.
	 * @return a series for relation
	 */
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { PrivilegeType.USEAPPLICATION })
	@Path("getRelationTrendSeries")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public List<com.verint.textanalytics.web.viewmodel.InteractionDailyVolumeDataPoint> getRelationTrendSeries(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final TrendSeriesParams discoverTrendsParams) {
		List<com.verint.textanalytics.web.viewmodel.InteractionDailyVolumeDataPoint> result = null;

		Timer.Context context = this.performanceMetrics.startTimedOperation(OperationType.RelationInteractionsDailyVolumeSeriesRest);

		logger.debug("Get Relation Trend Series request invoked with params  = {}", () -> JSONUtils.getObjectJSON(discoverTrendsParams));

		result = this.trendsUIService.getDailyVolumeTrendSeriesByType(i360FoundationToken, discoverTrendsParams.getValue(), discoverTrendsParams.getChannel(), TrendType.Relations,
		                                                              discoverTrendsParams.getSearchContext(), discoverTrendsParams.getClientTimeZoneOffset(),
		                                                              discoverTrendsParams.getSpeaker());
		this.performanceMetrics.stopTimedOperation(context);

		return  result;
	}

	/**
	 * REST method to retrieves an relation series data.
	 *
	 * @param i360FoundationToken
	 *            authentication token.
	 * @param discoverTrendsParams
	 *            discover trends search context.
	 * @return a series for relation
	 */
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { PrivilegeType.USEAPPLICATION })
	@Path("getCategoriesTrendSeries")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public List<com.verint.textanalytics.web.viewmodel.InteractionDailyVolumeDataPoint> getCategoriesTrendSeries(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final TrendSeriesParams discoverTrendsParams) {
		List<com.verint.textanalytics.web.viewmodel.InteractionDailyVolumeDataPoint> result = null;

		Timer.Context context = this.performanceMetrics.startTimedOperation(OperationType.CategoryInteractionsDailyVolumeSeriesRest);

		logger.debug("Get Relation Trend Series request invoked with params  = {}", () -> JSONUtils.getObjectJSON(discoverTrendsParams));

		result = this.trendsUIService.getDailyVolumeTrendSeriesByType(i360FoundationToken, discoverTrendsParams.getValue(), discoverTrendsParams.getChannel(), TrendType.Categories,
		                                                              discoverTrendsParams.getSearchContext(), discoverTrendsParams.getClientTimeZoneOffset(),
		                                                              SpeakerQueryType.Any);
		this.performanceMetrics.stopTimedOperation(context);

		return result;
	}

	/**
	 * REST method to retrieves an key term series data.
	 * 
	 * @param i360FoundationToken
	 *            authentication token.
	 * @param discoverTrendsParams
	 *            discover trends search context.
	 * @return a series for relation
	 */
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { PrivilegeType.USEAPPLICATION })
	@Path("getKeyTermTrendSeries")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public List<com.verint.textanalytics.web.viewmodel.InteractionDailyVolumeDataPoint> getKeyTermTrendSeries(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final TrendSeriesParams discoverTrendsParams) {
		List<com.verint.textanalytics.web.viewmodel.InteractionDailyVolumeDataPoint> result = null;

		Timer.Context context = this.performanceMetrics.startTimedOperation(OperationType.KeyTermInteractionsDailyVolumeSeriesRest);

		logger.debug("Get KeyWord Trend Series request invoked with  params  = {}", () -> JSONUtils.getObjectJSON(discoverTrendsParams));

		result = this.trendsUIService.getDailyVolumeTrendSeriesByType(i360FoundationToken, discoverTrendsParams.getValue(), discoverTrendsParams.getChannel(), TrendType.Keyterms,
		                                                              discoverTrendsParams.getSearchContext(), discoverTrendsParams.getClientTimeZoneOffset(),
		                                                              discoverTrendsParams.getSpeaker());
		this.performanceMetrics.stopTimedOperation(context);

		return result;
	}

	/**
	 * Retrieves interactions daily volume series.
	 *
	 * @param i360FoundationToken  foundation authentications token
	 * @param discoverTrendsParams discover trends search context
	 * @return an interactions daily volume series
	 */
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { PrivilegeType.USEAPPLICATION })
	@Path("getInteractionsDailyVolumeSeries")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public List<com.verint.textanalytics.web.viewmodel.InteractionDailyVolumeDataPoint> getInteractionsDailyVolumeSeries(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final InteractionsDailyVolumeParams discoverTrendsParams) {
		List<com.verint.textanalytics.web.viewmodel.InteractionDailyVolumeDataPoint> result = null;

		Timer.Context context = this.performanceMetrics.startTimedOperation(OperationType.InteractionsDailyVolumeSeriesRest);

		logger.debug("Get Interaction Daily Volume Series was invoked with  interactions request invoked with params  = {}", JSONUtils.getObjectJSON(discoverTrendsParams));

		result = this.trendsUIService.getInteractionsDailyVolumeSeries(i360FoundationToken, discoverTrendsParams.getChannel(), discoverTrendsParams.getSearchContext(),
		                                                               discoverTrendsParams.getClientTimeZoneOffset());

		this.performanceMetrics.stopTimedOperation(context);

		return result;
	}

	/**
	 * REST method to retrieve Theme Trends.
	 * 
	 * @param i360FoundationToken
	 *            authentication token.
	 * @param discoverTrendsParams
	 *            discover trends search context.
	 * @return an hierarchy of entities trends.
	 */
	@Path("getThemeTrends")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public List<com.verint.textanalytics.web.viewmodel.TextElementTrend> getThemeTrends(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final DiscoverTrendsParams discoverTrendsParams) {
		List<com.verint.textanalytics.web.viewmodel.TextElementTrend> result = null;

		Timer.Context context = this.performanceMetrics.startTimedOperation(OperationType.ThemesTrendsRest);

		logger.debug("Get Entities Trends request invoked with params  = {}", () -> JSONUtils.getObjectJSON(discoverTrendsParams));

		String channel = discoverTrendsParams.getChannel();
		String textElement = discoverTrendsParams.getValue();
		String sortProperty = discoverTrendsParams.getSortProperty();
		String sortDirection = discoverTrendsParams.getSortDirection();
		SpeakerQueryType speaker = discoverTrendsParams.getSpeaker();

		result = this.trendsUIService.getMergedTextElementsTrends(i360FoundationToken, channel, discoverTrendsParams.getSearchContext(), textElement, sortProperty, sortDirection,
		                                                          TrendType.Themes, speaker);

		this.performanceMetrics.stopTimedOperation(context);

		return  result;
	}
}
