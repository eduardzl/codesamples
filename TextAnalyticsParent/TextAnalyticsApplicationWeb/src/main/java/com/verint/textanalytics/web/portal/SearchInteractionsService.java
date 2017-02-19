package com.verint.textanalytics.web.portal;

import com.codahale.metrics.Timer;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.diagnostics.OperationType;
import com.verint.textanalytics.common.diagnostics.PerformanceMetrics;
import com.verint.textanalytics.common.security.ChannelAuthorizationNotRequired;
import com.verint.textanalytics.common.security.OperationPrivelegesAnnotation;
import com.verint.textanalytics.common.security.OperationPrivelegesAnnotation.PrivilegeType;
import com.verint.textanalytics.common.utils.JSONUtils;
import com.verint.textanalytics.model.interactions.ResultsQuantity;
import com.verint.textanalytics.web.uiservices.SearchInteractionsUIService;
import com.verint.textanalytics.web.viewmodel.*;
import com.verint.textanalytics.web.viewmodel.requestparams.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author EZlotnik REST service for Search Interactions API
 */
@Path("/SearchInteractionsService")
public class SearchInteractionsService {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	private SearchInteractionsUIService searchInteractionsUIService;

	@Autowired
	private PerformanceMetrics performanceMetrics;

	/**
	 * Search document REST method. Accepts a Search interactions context and
	 * returns interactions which match a search
	 * 
	 * @param i360FoundationToken
	 *            foundation token
	 * @param searchInteractionsParams
	 *            object which encapsulates all request parameters
	 * @return DataResult with interactions
	 */
	@Path("searchInteractions")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public InteractionsListDataResult<Interaction> searchInteractions(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final SearchInteractionsPagedParams searchInteractionsParams) {
		InteractionsListDataResult<Interaction> result = null;

		Timer.Context context = this.performanceMetrics.startTimedOperation(OperationType.SearchInteractionsPageRest);

		logger.debug("Search interactions request invoked. Request params  - {}", () -> JSONUtils.getObjectJSON(searchInteractionsParams));

		if (searchInteractionsParams != null) {
			result = searchInteractionsUIService.searchInteractions(i360FoundationToken, searchInteractionsParams.getChannel(), searchInteractionsParams.getSearchContext(),
			                                                        searchInteractionsParams.getPageStart(),
			                                                        searchInteractionsParams.getPageSize(), searchInteractionsParams.getSortProperty(),
			                                                        searchInteractionsParams.getSortDirection());
		}

		this.performanceMetrics.stopTimedOperation(context);

		return result;
	}

	/**
	 * get Results Quantity REST method. Accepts a Search interactions context
	 * and returns ResultsQuantity with the all interactions quantity and
	 * interactions quantity which match a search
	 *
	 * @param i360FoundationToken  foundation token
	 * @param resultQuantityParams object which encapsulates all request parameters
	 * @return DataResult with ResultsQuantity
	 */
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { PrivilegeType.USEAPPLICATION })
	@Path("getResultsQuantity")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public ResultsQuantity getResultsQuantity(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final ResultQuantityParams resultQuantityParams) {
		ResultsQuantity result = null;

		Timer.Context context = this.performanceMetrics.startTimedOperation(OperationType.CurrentResultSetQuantityRest);

		logger.debug("getResultsQuantity request invoked with params  - {}", () -> JSONUtils.getObjectJSON(resultQuantityParams));

		if (resultQuantityParams != null) {
			result = searchInteractionsUIService.getResultsQuantity(i360FoundationToken, resultQuantityParams.getChannel(), resultQuantityParams.getSearchContext(),
			                                                        resultQuantityParams.getBackgroundContext());
		}

		this.performanceMetrics.stopTimedOperation(context);

		return result;
	}

	/**
	 * Validates Search Terms Query. Accepts a Search interactions context and
	 * returns interactions which match a search
	 *
	 * @param i360FoundationToken foundation token
	 * @param searchQuery  search query
	 * @return Boolean indication of terms query validity
	 */
	@ChannelAuthorizationNotRequired
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { PrivilegeType.USEAPPLICATION })
	@Path("validateSearchTermsQuery")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public SearchQueryValidationResult validateSearchTermsQuery(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final ValidateSearchTermsQueryParams searchQuery) {
		logger.debug("Validate VTA Syntax Search Query request invoked with params = {}", () -> JSONUtils.getObjectJSON(searchQuery));

		return searchInteractionsUIService.validateSearchTermsQuery(i360FoundationToken, searchQuery.getSearchTermsQuery());
	}

	/**
	 * Retrieves interaction and it's utterances for review.
	 *
	 * @param i360FoundationToken     i360 Foundation Token
	 * @param interactionReviewParams channel and document id
	 * @return interactions
	 */
	@ChannelAuthorizationNotRequired
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { PrivilegeType.USEAPPLICATION })
	@Path("getInteractionPreview")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public InteractionPreview getInteractionPreview(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final InteractionReviewParams interactionReviewParams) {
		InteractionPreview result = null;

		Timer.Context context = this.performanceMetrics.startTimedOperation(OperationType.InteractionPreviewRest);

		logger.debug("getInteractionPreview request invoked with params = {}", () -> JSONUtils.getObjectJSON(interactionReviewParams));

		result = searchInteractionsUIService.getInteractionPreview(i360FoundationToken, interactionReviewParams.getChannel(), interactionReviewParams.getInteractionId(),
		                                                           interactionReviewParams.getSearchContext());

		this.performanceMetrics.stopTimedOperation(context);

		return result;
	}

	/**
	 * Retrieves interaction and it's utterances for review.
	 *
	 * @param i360FoundationToken     i360 Foundation Token
	 * @param interactionReviewParams channel and document id
	 * @return interactions
	 */
	@ChannelAuthorizationNotRequired
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { PrivilegeType.USEAPPLICATION })
	@Path("getInteractionPreviewCIV")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public InteractionPreviewCIV getInteractionPreviewCIV(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final InteractionReviewCIVParams interactionReviewParams) {
		InteractionPreviewCIV result = null;

		Timer.Context context = this.performanceMetrics.startTimedOperation(OperationType.InteractionPreviewRest);

		logger.debug("getInteractionPreviewCIV request invoked with params = {}", () -> JSONUtils.getObjectJSON(interactionReviewParams));

		result = searchInteractionsUIService.getInteractionPreviewCIV(i360FoundationToken, interactionReviewParams.getChannel(), interactionReviewParams.getInteractionId(),
		                                                              interactionReviewParams.getSearchContext());

		this.performanceMetrics.stopTimedOperation(context);

		return result;
	}
}
