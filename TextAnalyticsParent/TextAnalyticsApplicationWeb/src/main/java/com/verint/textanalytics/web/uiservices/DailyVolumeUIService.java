package com.verint.textanalytics.web.uiservices;

import com.verint.textanalytics.bl.applicationservices.DailyVolumeService;
import com.verint.textanalytics.model.analyze.AnalyzeInteractionsDailyVolumePoints;
import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import com.verint.textanalytics.web.viewmodel.AnalyzeDailyVolumeDataPoints;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Represents Daily Volume Widget.
 * 
 * @author NShunewich
 *
 */
public class DailyVolumeUIService extends BaseUIService {

	@Autowired
	private DailyVolumeService dailyVolumeService;

	@Autowired
	private ViewModelConverter viewModelConverter;

	/**
	 * retrieves daily volume data.
	 * 
	 * @param i360FoundationToken
	 *            i360FoundationToken
	 * @param channel
	 *            channel
	 * @param searchContext
	 *            searchContext
	 * @param backgroundContext
	 *            backgroundContext
	 * @return collection of points
	 */
	public List<AnalyzeDailyVolumeDataPoints> getInteractionsDailyVolume(String i360FoundationToken, String channel, SearchInteractionsContext searchContext, SearchInteractionsContext backgroundContext) {
		val userTenant = this.getTenantFromChannel(channel, i360FoundationToken);

		List<AnalyzeInteractionsDailyVolumePoints> datePoints = dailyVolumeService.getInteractionsDailyVolume(userTenant, channel, searchContext, backgroundContext);

		return viewModelConverter.convertToViewModelAnalyzeDailyVolume(datePoints);
	}
}
