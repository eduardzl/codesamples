package com.verint.textanalytics.web.portal;

import com.codahale.metrics.Timer;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.diagnostics.OperationType;
import com.verint.textanalytics.common.diagnostics.PerformanceMetrics;
import com.verint.textanalytics.common.security.OperationPrivelegesAnnotation;
import com.verint.textanalytics.common.utils.JSONUtils;
import com.verint.textanalytics.web.uiservices.StoredSearchUIService;
import com.verint.textanalytics.web.viewmodel.SaveStoredSearchResult;
import com.verint.textanalytics.web.viewmodel.TextElementFacetTreeMapNode;
import com.verint.textanalytics.web.viewmodel.TextElementFacetTreeMapResult;
import com.verint.textanalytics.web.viewmodel.TextElementFacetTreeNode;
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
 * Stored Search Service.
 * 
 * @author imor
 *
 */
@Path("/StoredSearchService")
public class StoredSearchService {

	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	private StoredSearchUIService storedSearchUIService;

	@Autowired
	private PerformanceMetrics performanceMetrics;

	/**
	 * @param i360FoundationToken
	 *            token
	 * @param categoryActionParams
	 *            CategoryActionParams
	 * @return category
	 *
	 */
	@Path("saveAsCategory")
	@Produces(MediaType.APPLICATION_JSON)
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { OperationPrivelegesAnnotation.PrivilegeType.USEAPPLICATION, OperationPrivelegesAnnotation.PrivilegeType.ADDFORM })
	@POST
	public com.verint.textanalytics.web.viewmodel.Category saveAsCategory(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final CategoryActionParams categoryActionParams) {

		logger.debug("saveAsCategory request invoked. Request params - {}", () -> JSONUtils.getObjectJSON(categoryActionParams));

		return storedSearchUIService.saveAsCategory(i360FoundationToken, categoryActionParams.getChannel(), categoryActionParams.getCategory());
	}

	/**
	 * @param i360FoundationToken
	 *            token
	 * @param saveAsSearchParams
	 *            saveAsSearchParams
	 */
	@Path("saveAsSearch")
	@Produces(MediaType.APPLICATION_JSON)
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { OperationPrivelegesAnnotation.PrivilegeType.USEAPPLICATION})
	@POST
	public void saveAsSearch(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final SavedSearchActionParams saveAsSearchParams) {

		logger.debug("saveAsSearch request invoked. Request params - {}", () -> JSONUtils.getObjectJSON(saveAsSearchParams));

		storedSearchUIService.saveAsSearch(i360FoundationToken, saveAsSearchParams.getChannel(), saveAsSearchParams.getSavedSearch());
	}

	/**
	 * Updates Category.
	 * @param i360FoundationToken  i360FoundationToken
	 *            token
	 * @param categoryParams category data to update
	 * @return category
	 */
	@Path("updateCategory")
	@Produces(MediaType.APPLICATION_JSON)
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { OperationPrivelegesAnnotation.PrivilegeType.USEAPPLICATION, OperationPrivelegesAnnotation.PrivilegeType.ADDFORM })
	@POST
	public SaveStoredSearchResult updateCategory(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final CategoryActionParams categoryParams) {
		logger.debug("updateCategory request invoked. Request params - {}", () -> JSONUtils.getObjectJSON(categoryParams));

		return storedSearchUIService.updateCategory(i360FoundationToken, categoryParams.getChannel(), categoryParams.getCategory());
	}

	/**
	 * @param i360FoundationToken
	 *            i360FoundationToken
	 * @param categoryActionParams
	 *            SaveAsSearchParams
	 * @return CategoriesRepository
	 */
	@Path("getCategoriesWithoutReprocessingState")
	@Produces(MediaType.APPLICATION_JSON)
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { OperationPrivelegesAnnotation.PrivilegeType.USEAPPLICATION, OperationPrivelegesAnnotation.PrivilegeType.ADDFORM })
	@POST
	public com.verint.textanalytics.web.viewmodel.CategoriesRepository getCategories(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final CategoryActionParams categoryActionParams) {

		logger.debug("getCategories request invoked. Request params - {}", () -> JSONUtils.getObjectJSON(categoryActionParams));

		return storedSearchUIService.getCategories(i360FoundationToken, categoryActionParams.getChannel());
	}


	/**
	 * @param i360FoundationToken i360FoundationToken
	 * @param categoryActionParams	tenant and channel
	 * @return CategoriesRepository list if categories
	 */
	@Path("getCategories")
	@Produces(MediaType.APPLICATION_JSON)
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { OperationPrivelegesAnnotation.PrivilegeType.USEAPPLICATION, OperationPrivelegesAnnotation.PrivilegeType.ADDFORM })
	@POST
	public com.verint.textanalytics.web.viewmodel.CategoriesRepository getCategoriesWithReprocessingState(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final CategoryActionParams categoryActionParams) {

		logger.debug("Retrieving Categories with getCategories request invoked. Request params - {}", () -> JSONUtils.getObjectJSON(categoryActionParams));

		return storedSearchUIService.getCategoriesWithReprocessingState(i360FoundationToken, categoryActionParams.getChannel());
	}

	/**
	 * @param i360FoundationToken
	 *            i360FoundationToken
	 * @param saveAsSearchParams
	 *            saveAsSearchParams
	 * @return SavedSearchesRepository
	 */
	@Path("getSavedSearches")
	@Produces(MediaType.APPLICATION_JSON)
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { OperationPrivelegesAnnotation.PrivilegeType.USEAPPLICATION})
	@POST
	public com.verint.textanalytics.web.viewmodel.SavedSearchesRepository getSavedSearches(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final SavedSearchActionParams saveAsSearchParams) {

		logger.debug("getSavedSearches request invoked. Request params - {}", () -> JSONUtils.getObjectJSON(saveAsSearchParams));

		return storedSearchUIService.getSavedSearches(i360FoundationToken, saveAsSearchParams.getChannel());
	}

	/** Deletes Category.
	 * @param i360FoundationToken token
	 * @param storedSearchParams  StoredSearchParams
	 */
	@Path("deleteCategory")
	@Produces(MediaType.APPLICATION_JSON)
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { OperationPrivelegesAnnotation.PrivilegeType.USEAPPLICATION, OperationPrivelegesAnnotation.PrivilegeType.ADDFORM })
	@POST
	public void deleteCategory(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final CategoryActionParams storedSearchParams) {
		logger.debug("deleteCategory request invoked. Request params - {}", () -> JSONUtils.getObjectJSON(storedSearchParams));

		storedSearchUIService.deleteCategory(i360FoundationToken, storedSearchParams.getChannel(), storedSearchParams.getCategory());
	}

	/** Delete Saved Search.
	 * @param i360FoundationToken     token
	 * @param savedSearchActionParams savedSearchActionParams
	 */
	@Path("deleteSavedSearch")
	@Produces(MediaType.APPLICATION_JSON)
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { OperationPrivelegesAnnotation.PrivilegeType.USEAPPLICATION})
	@POST
	public void deleteSavedSearch(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final SavedSearchActionParams savedSearchActionParams) {

		logger.debug("deleteSavedSearch request invoked. Request params - {}", () -> JSONUtils.getObjectJSON(savedSearchActionParams));

		storedSearchUIService.deleteSavedSearch(i360FoundationToken, savedSearchActionParams.getChannel(), savedSearchActionParams.getSavedSearch());
	}

	/**
	 * Category Reprocessing invocation.
	 * @param i360FoundationToken Foundation token
	 * @param categoryParams category to reprocess
	 */
	@Path("invokeReprocessCategory")
	@Produces(MediaType.APPLICATION_JSON)
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { OperationPrivelegesAnnotation.PrivilegeType.USEAPPLICATION, OperationPrivelegesAnnotation.PrivilegeType.ADDFORM })
	@POST
	public void invokeReprocessCategory(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final CategoryReprocessParams categoryParams) {
		logger.debug("invokeReprocessCategory request invoked. Request params - {}", () -> JSONUtils.getObjectJSON(categoryParams));

		storedSearchUIService.invokeReprocessCategory(i360FoundationToken, categoryParams.getChannel(), categoryParams.getCategoryId());
	}

	/**
	 * Retrieves facets for categories.
	 * @param i360FoundationToken
	 *            token
	 * @param searchInteractionsParams
	 *            context parameters
	 * @return facets collection
	 */
	@Path("getCategoriesFacet")
	@Produces(MediaType.APPLICATION_JSON)
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { OperationPrivelegesAnnotation.PrivilegeType.USEAPPLICATION})
	@POST
	public TextElementFacetTreeNode getCategoriesFacet(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final SearchInteractionsParams searchInteractionsParams) {
		TextElementFacetTreeNode result = null;

		Timer.Context context = this.performanceMetrics.startTimedOperation(OperationType.CategoriesFacetRest);

		logger.debug("getCategoiesFacet request invoked. Request params - {}", () -> JSONUtils.getObjectJSON(searchInteractionsParams));

		result = storedSearchUIService.getCategoriesFacet(i360FoundationToken, searchInteractionsParams.getChannel(), searchInteractionsParams.getSearchContext());

		this.performanceMetrics.stopTimedOperation(context);

		return result;
	}

	/**
	 * Retrieves facets for categories.
	 * @param i360FoundationToken
	 *            token
	 * @param searchInteractionsParams
	 *            context parameters
	 * @return facets collection
	 */
	@Path("getCategoriesFacetWithMetrics")
	@Produces(MediaType.APPLICATION_JSON)
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { OperationPrivelegesAnnotation.PrivilegeType.USEAPPLICATION})
	@POST
	public TextElementFacetTreeMapResult getCategoriesFacetWithMetrics(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final SearchInteractionsParams searchInteractionsParams) {
		TextElementFacetTreeMapResult result = null;

		Timer.Context context = this.performanceMetrics.startTimedOperation(OperationType.CategoriesFacetWithMetricsRest);

		logger.debug("getCategoiesFacet request invoked. Request params - {}", () -> JSONUtils.getObjectJSON(searchInteractionsParams));

		result = storedSearchUIService.getCategoriesFacetWithMetrics(i360FoundationToken, searchInteractionsParams.getChannel(), searchInteractionsParams.getSearchContext(),
		                                                             searchInteractionsParams.getBackgroundContext());

		this.performanceMetrics.stopTimedOperation(context);

		return result;
	}

	/**
	 * Retrieves category metrics.
	 *
	 * @param i360FoundationToken      token
	 * @param categoryMetricsParams category metrics paramters
	 * @return category metrics
	 */
	@Path("getCategoryMetrics")
	@Produces(MediaType.APPLICATION_JSON)
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { OperationPrivelegesAnnotation.PrivilegeType.USEAPPLICATION})
	@POST
	public TextElementFacetTreeMapNode getCategoryMetrics(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final TextElementMetricsParams categoryMetricsParams) {
		TextElementFacetTreeMapNode result = null;

		Timer.Context context = this.performanceMetrics.startTimedOperation(OperationType.CategoryMetricsRest);

		logger.debug("getCategoryMetricsrequest invoked. Request params - {}", () -> JSONUtils.getObjectJSON(categoryMetricsParams));

		result = storedSearchUIService.getCategoryMetrics(i360FoundationToken, categoryMetricsParams.getChannel(), categoryMetricsParams.getSearchContext(),
		                                                             categoryMetricsParams.getBackgroundContext(),

		                                                  Integer.valueOf(categoryMetricsParams.getTextElementValue()),
		                                                             categoryMetricsParams.getTextElementName());

		this.performanceMetrics.stopTimedOperation(context);

		return result;
	}
}
