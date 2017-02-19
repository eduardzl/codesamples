package com.verint.textanalytics.web.uiservices;

import com.verint.textanalytics.bl.applicationservices.TrendsService;
import com.verint.textanalytics.common.utils.CollectionUtils;
import com.verint.textanalytics.common.utils.DataUtils;
import com.verint.textanalytics.model.facets.SpeakerQueryType;
import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import com.verint.textanalytics.model.trends.DiscoverTrendsContext;
import com.verint.textanalytics.model.trends.TextElementTrend;
import com.verint.textanalytics.model.trends.TrendType;
import com.verint.textanalytics.web.viewmodel.InteractionDailyVolumeDataPoint;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author EZlotnik Trends UI Service.
 */
public class TrendsUIService extends BaseUIService {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	private ViewModelConverter viewModelConverter;

	@Autowired
	private TrendsService trendsService;

	/**
	 * Retrieves interactions daily volume series.
	 *
	 * @param i360FoundationToken   authentication token.
	 * @param channel               channel
	 * @param discoverTrendsContext discover trends search context
	 * @param clientTimeZoneOffset  a client time zone offset in milliseconds
	 * @return series of daily volume
	 * @throws ParseException exception to be thrown when date
	 */
	public List<InteractionDailyVolumeDataPoint> getInteractionsDailyVolumeSeries(String i360FoundationToken, String channel, SearchInteractionsContext discoverTrendsContext, int clientTimeZoneOffset) {
		List<InteractionDailyVolumeDataPoint> dailyVolumeSeries = new ArrayList<>();

		List<com.verint.textanalytics.model.trends.InteractionDailyVolumeDataPoint> dailyVolumeDataPoints = null;

		val userTenant = this.getTenantFromChannel(channel, i360FoundationToken);

		logger.debug("Invoking getInteractionsDailyVolumeSeries for tenant {} and channel {} ", userTenant, channel);

		dailyVolumeDataPoints = this.trendsService.getInteractionsDailyVolumeSeries(userTenant, channel, discoverTrendsContext);
		if (!CollectionUtils.isEmpty(dailyVolumeDataPoints)) {

			// No need to sort daily volume series, it is being sorted by Solr

			// get date of the last data point in series
			DateTime seriesEndDate = dailyVolumeDataPoints.get(dailyVolumeDataPoints.size() - 1).getDate();
			DateTime dateTimeNow = DateTime.now();

			// pad the series till "today"
			while (DataUtils.getDaysBetween(seriesEndDate, dateTimeNow) >= 0) {
				dailyVolumeDataPoints.add(new com.verint.textanalytics.model.trends.InteractionDailyVolumeDataPoint(seriesEndDate, null));

				seriesEndDate = seriesEndDate.plusDays(1);
			}
		} else {
			// there is no daily volume data, so create an an empty series for
			// one month data
			DateTime dateTimeNow = DateTime.now();
			DateTime startDateTime = dateTimeNow.minusMonths(1);

			dailyVolumeDataPoints = new ArrayList<>();

			// populate array with values
			while (DataUtils.getDaysBetween(startDateTime, dateTimeNow) >= 0) {
				dailyVolumeDataPoints.add(new com.verint.textanalytics.model.trends.InteractionDailyVolumeDataPoint(startDateTime, null));

				startDateTime = startDateTime.plusDays(1);
			}
		}

		if (dailyVolumeDataPoints != null) {
			//@formatter:off
			// add demo date at the beginning of the series
			val demoDataPoint = new com.verint.textanalytics.model.trends.InteractionDailyVolumeDataPoint()
											.setDate(dailyVolumeDataPoints.get(0).getDate().minusDays(1))
											.setValue(null);
			dailyVolumeDataPoints.add(0, demoDataPoint);
			//@formatter:on

			// convert date to ticks as HighStock requires ticks to be supplied
			dailyVolumeSeries = dailyVolumeDataPoints.parallelStream()
			                                         .map(d -> viewModelConverter.convertToViewModelDailyVolumeDataPoint(d, clientTimeZoneOffset))
													 .collect(toList());

		}

		return dailyVolumeSeries;
	}

	/**
	 * Generates a tree hierarchy of entities trends.
	 *
	 * @param i360FoundationToken authentication token.
	 * @param channel             channel
	 * @param searchContext       discover trends search context
	 * @param trendtype           the type of requested trend
	 * @param textElementValue    value of text element
	 * @param sortProperty        sort field
	 * @param sortDirection       sort direction
	 * @param speaker             trends according to speaker type
	 * @return a hierarchy of entities trends
	 */
	public List<com.verint.textanalytics.web.viewmodel.TextElementTrend> getTextElementsTrends(String i360FoundationToken, String channel, DiscoverTrendsContext searchContext, TrendType trendtype, String textElementValue, String sortProperty, String sortDirection, SpeakerQueryType speaker) {

		val userTenant = this.getTenantFromChannel(channel, i360FoundationToken);
		List<TextElementTrend> trends;

		trends = this.trendsService.getTextElementsTrends(userTenant, channel, searchContext, trendtype, textElementValue,
		                                                  viewModelConverter.mapTextElementsTrendFieldForSort(sortProperty), sortDirection.toLowerCase(), speaker);

		List<com.verint.textanalytics.web.viewmodel.TextElementTrend> vmEntityTrends = viewModelConverter.convertToViewModelMergedTrend(trends, trendtype);

		return vmEntityTrends;
	}

	/**
	 * Generates a tree hierarchy of entities trends.
	 *
	 * @param i360FoundationToken authentication token.
	 * @param channel             channel
	 * @param searchContext       discover trends search context
	 * @param textElementValue    value of text element
	 * @param sortProperty        sort field
	 * @param sortDirection       sort direction
	 * @return a hierarchy of entities trends
	 */
	public List<com.verint.textanalytics.web.viewmodel.TextElementTrend> getCategoryTrends(String i360FoundationToken, String channel, DiscoverTrendsContext searchContext, String textElementValue, String sortProperty, String sortDirection) {

		val userTenant = this.getTenantFromChannel(channel, i360FoundationToken);
		List<TextElementTrend> trends;

		trends = this.trendsService.getCategoriesTrends(userTenant, channel, searchContext, textElementValue, viewModelConverter.mapTextElementsTrendFieldForSort(sortProperty), sortDirection.toLowerCase());

		List<com.verint.textanalytics.web.viewmodel.TextElementTrend> vmEntityTrends = viewModelConverter.convertToViewModelMergedTrend(trends, TrendType.Categories);

		return vmEntityTrends;
	}

	/**
	 * Retrieves an interactions daily volume for specific entity.
	 *
	 * @param i360FoundationToken  authentication token.
	 * @param entityValue          the value of entity
	 * @param channel              channel
	 * @param trendType            trendType
	 * @param searchContext        discover trends search context
	 * @param clientTimeZoneOffset client timezome offset
	 * @param speaker              speaker
	 * @return a list of interactions daily volume data points
	 */
	public List<InteractionDailyVolumeDataPoint> getDailyVolumeTrendSeriesByType(String i360FoundationToken, String entityValue, String channel, TrendType trendType, SearchInteractionsContext searchContext, int clientTimeZoneOffset, SpeakerQueryType speaker) {
		List<InteractionDailyVolumeDataPoint> textElementTrendSeries = null;

		val userTenant = this.getTenantFromChannel(channel, i360FoundationToken);

		logger.debug("Invoking getDailyVolumeTrendSeriesByType for tenant {} and channel {} ", userTenant, channel);

		// the series is sorted by Solr, no need to sort it again
		val textElementDailyVolumeDataPoints = this.trendsService.getEntityTrendDailyVolumeSeriesByType(userTenant, channel, trendType, entityValue, searchContext,  speaker);
		if (!CollectionUtils.isEmpty(textElementDailyVolumeDataPoints)) {

			//@formatter:off
			// convert to view model objects
			textElementTrendSeries = textElementDailyVolumeDataPoints.parallelStream()
			                                                         .map(d -> viewModelConverter.convertToViewModelDailyVolumeDataPoint(d, clientTimeZoneOffset))
																	 .collect(toList());
			//@formatter:on
		}

		return textElementTrendSeries;
	}

	/**
	 * Generates a tree hierarchy of entities trends.
	 *
	 * @param i360FoundationToken authentication token.
	 * @param channel             channel
	 * @param searchContext       discover trends search context
	 * @param textElementValue    value of text element
	 * @param sortProperty        sort field
	 * @param sortDirection       sort direction
	 * @param trendType           the type of requested trend
	 * @param speaker             trends according to speaker type
	 * @return a hierarchy of entities trends
	 */
	public List<com.verint.textanalytics.web.viewmodel.TextElementTrend> getMergedTextElementsTrends(String i360FoundationToken, String channel, DiscoverTrendsContext searchContext, String textElementValue, String sortProperty, String sortDirection, TrendType trendType, SpeakerQueryType speaker) {

		val userTenant = this.getTenantFromChannel(channel, i360FoundationToken);

		List<TextElementTrend> mergedTrends = this.trendsService.getMergedElementsTrends(userTenant, channel, searchContext,  textElementValue,
		                                                                               viewModelConverter.mapTextElementsTrendFieldForSort(sortProperty),
		                                                                               sortDirection.toLowerCase(), speaker);

		List<com.verint.textanalytics.web.viewmodel.TextElementTrend> vmEntityTrends = viewModelConverter.convertToViewModelMergedTrend(mergedTrends, sortProperty, sortDirection.toLowerCase(), trendType);

		return vmEntityTrends;
	}

}
