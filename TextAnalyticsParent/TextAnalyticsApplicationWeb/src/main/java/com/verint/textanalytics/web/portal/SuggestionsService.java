package com.verint.textanalytics.web.portal;

import com.codahale.metrics.Timer;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.diagnostics.OperationType;
import com.verint.textanalytics.common.diagnostics.PerformanceMetrics;
import com.verint.textanalytics.common.security.OperationPrivelegesAnnotation;
import com.verint.textanalytics.common.security.OperationPrivelegesAnnotation.PrivilegeType;
import com.verint.textanalytics.common.utils.JSONUtils;
import com.verint.textanalytics.model.interactions.WeightedSuggestion;
import com.verint.textanalytics.web.uiservices.SuggestionsUIService;
import com.verint.textanalytics.web.viewmodel.SuggestionItem;
import com.verint.textanalytics.web.viewmodel.WeightedSuggestionsDataResult;
import com.verint.textanalytics.web.viewmodel.requestparams.AutoCompleteSuggestionsParms;
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
 * @author YZanis REST service for autocomplete suggestions API
 */

@Path("/SuggestionsService")
public class SuggestionsService {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	private SuggestionsUIService suggestionsUIService;

	@Autowired
	private PerformanceMetrics performanceMetrics;

	/**
	 * 
	 * @param i360FoundationToken
	 *            user token
	 * @param searchedText
	 *            text that needs the autocomplete
	 * @return autocomplete text suggestions
	 */
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { PrivilegeType.USEAPPLICATION })
	@Path("getAutoCompleteSuggestions")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public List<com.verint.textanalytics.web.viewmodel.SuggestionItem> getAutoCompleteSuggestions(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final AutoCompleteSuggestionsParms searchedText) {
		Timer.Context context = this.performanceMetrics.startTimedOperation(OperationType.TermsAutoCompleteSuggestionsRest);

		logger.debug("getAutoCompleteSuggestions request invoked with params = {}", () -> JSONUtils.getObjectJSON(searchedText));

		List<SuggestionItem> result = suggestionsUIService.getAutoCompleteSuggestions(i360FoundationToken, searchedText.getChannel(), searchedText.getSearchContext(),
		                                                                              searchedText.getTextPrefix());

		this.performanceMetrics.stopTimedOperation(context);

		return result;
	}

	/**
	 * 
	 * @param i360FoundationToken
	 *            user token
	 * @param searchedText
	 *            text that needs the autocomplete
	 * @return autocomplete text suggestions
	 */
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { PrivilegeType.USEAPPLICATION })
	@Path("getWeightedTermsSuggestions")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public WeightedSuggestionsDataResult<WeightedSuggestion> getWeightedTermsSuggestions(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final AutoCompleteSuggestionsParms searchedText) {

		logger.debug("getSolrSuggestions request invoked with params = {}", () -> JSONUtils.getObjectJSON(searchedText));

		WeightedSuggestionsDataResult<WeightedSuggestion> result = suggestionsUIService.getWeightedTermsSuggestions(i360FoundationToken, searchedText.getChannel(),
		                                                                                                            searchedText.getSearchContext(), searchedText.getTextPrefix());

		return result;
	}
}