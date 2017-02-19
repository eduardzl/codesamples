package com.verint.textanalytics.web.portal;

import com.codahale.metrics.Timer;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.diagnostics.OperationType;
import com.verint.textanalytics.common.diagnostics.PerformanceMetrics;
import com.verint.textanalytics.common.security.OperationPrivelegesAnnotation;
import com.verint.textanalytics.common.security.OperationPrivelegesAnnotation.PrivilegeType;
import com.verint.textanalytics.common.utils.JSONUtils;
import com.verint.textanalytics.model.facets.Facet;
import com.verint.textanalytics.model.facets.TextElementType;
import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import com.verint.textanalytics.web.uiservices.FacetsUIService;
import com.verint.textanalytics.web.viewmodel.TextElementFacetTreeMapNode;
import com.verint.textanalytics.web.viewmodel.TextElementFacetTreeMapResult;
import com.verint.textanalytics.web.viewmodel.TextElementFacetTreeNode;
import com.verint.textanalytics.web.viewmodel.requestparams.SearchInteractionsParams;
import com.verint.textanalytics.web.viewmodel.requestparams.TextElementMetricsParams;
import com.verint.textanalytics.web.viewmodel.requestparams.TextElementsFacetWithMetricsParams;
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
 * Facets related service.
 *
 * @author EZlotnik
 */
@Path("/FacetsService")
public class FacetsService {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	private FacetsUIService facetsUIService;

	@Autowired
	private ConfigurationManager configurationManager;

	@Autowired
	private PerformanceMetrics performanceMetrics;


	/**
	 * Faceted Search REST method. Accepts a Search interactions context and
	 * returns interactions which match a search
	 *
	 * @param i360FoundationToken
	 *            foundation token
	 * @param searchInteractionsParams
	 *            object which encapsulates all request parameters
	 * @return DataResult with interactions
	 */
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { PrivilegeType.USEAPPLICATION })
	@Path("facetedSearch")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public List<Facet> facetedSearch(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final SearchInteractionsParams searchInteractionsParams) {
		Timer.Context context = this.performanceMetrics.startTimedOperation(OperationType.FacetedSearchRest);

		logger.debug("facetedSearch request invoked. Request params  = {}", () -> JSONUtils.getObjectJSON(searchInteractionsParams));

		List<Facet> facets = facetsUIService.facetedSearch(i360FoundationToken, searchInteractionsParams.getChannel(), searchInteractionsParams.getSearchContext(),
		                                                   searchInteractionsParams.getFacetsQueries());

		this.performanceMetrics.stopTimedOperation(context);

		return facets;
	}

	/**
	 * Sentiment facet search REST method. accepts search context, returns
	 * interactions that match the sentiment facet
	 *
	 * @param i360FoundationToken
	 *            foundation token
	 * @param searchInteractionsParams
	 *            object which encapsulates all request parameters
	 * @return DataResult with interactions
	 */
	@Path("getSentimentFacetSearch")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public List<Facet> getSentimentFacetSearch(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final SearchInteractionsParams searchInteractionsParams) {
		Timer.Context context = this.performanceMetrics.startTimedOperation(OperationType.SentimentFacetRest);

		logger.debug("getSentimentFacetSearch request invoked. Request params  = {}", () -> JSONUtils.getObjectJSON(searchInteractionsParams));

		List<Facet> facets = facetsUIService.getSentimentFacetSearch(i360FoundationToken, searchInteractionsParams.getChannel(), searchInteractionsParams.getSearchContext(),
		                                                             searchInteractionsParams.getFacetsQueries());

		this.performanceMetrics.stopTimedOperation(context);

		return facets;
	}

	/**
	 * Get Entities Facet Tree REST method.
	 * Being displayed in the Context panel as Tree
	 * context and returns Entities Facet Tree
	 *
	 * @param i360FoundationToken
	 *            foundation token
	 * @param searchInteractionsParams
	 *            object which encapsulates all request parameters
	 * @return DataResult with interactions
	 */
	@Path("getEntitiesFacetTree")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public TextElementFacetTreeNode getEntitiesFacetTree(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final SearchInteractionsParams searchInteractionsParams) {

		Timer.Context context = this.performanceMetrics.startTimedOperation(OperationType.EntitiesFacetRest);

		logger.debug("getEntitiesFacet request invoked. Request params - {}", () -> JSONUtils.getObjectJSON(searchInteractionsParams));

		TextElementFacetTreeNode facetRootNodeResult = null;

		if (searchInteractionsParams != null) {
			facetRootNodeResult = facetsUIService.getTextElementsFacetTree(i360FoundationToken, searchInteractionsParams.getChannel(), searchInteractionsParams.getSearchContext(),
			                                                               searchInteractionsParams.getBackgroundContext(), TextElementType.Entities,
			                                                               searchInteractionsParams.getQuerySpeakerType());

		}

		this.performanceMetrics.stopTimedOperation(context);

		return facetRootNodeResult;
	}

	/**
	 * Get Relations Facet Tree REST method. Accepts a Search interactions
	 * context and returns Relations Facet Tree.
	 * The tree being displayed in the left Context panel
	 *
	 * @param i360FoundationToken
	 *            foundation token
	 * @param searchInteractionsParams
	 *            object which encapsulates all request parameters
	 * @return DataResult with interactions
	 */
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { PrivilegeType.USEAPPLICATION })
	@Path("getRelationsFacetTree")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public TextElementFacetTreeNode getRelationsFacetTree(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final SearchInteractionsParams searchInteractionsParams) {
		Timer.Context context = this.performanceMetrics.startTimedOperation(OperationType.RelationsFacetRest);

		logger.debug("getRelationsFacetTree request invoked. Request parameters  = {}", () -> JSONUtils.getObjectJSON(searchInteractionsParams));

		TextElementFacetTreeNode facetRootNodeResult = null;

		if (searchInteractionsParams != null) {
			facetRootNodeResult = facetsUIService.getTextElementsFacetTree(i360FoundationToken, searchInteractionsParams.getChannel(), searchInteractionsParams.getSearchContext(),
			                                                               searchInteractionsParams.getBackgroundContext(), TextElementType.Relations,
			                                                               searchInteractionsParams.getQuerySpeakerType());

		}

		this.performanceMetrics.stopTimedOperation(context);

		return facetRootNodeResult;
	}


	/**
	 * Get Entities Facet TreeMap REST method. Accepts a Search interactions context and returns Entities Facet TreeMap
	 * @param i360FoundationToken   foundation token
	 * @param facetWithMetricsParam object which encapsulates all request parameters
	 * @return DataResult with interactions
	 */
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { PrivilegeType.USEAPPLICATION })
	@Path("getEntitiesFacetTreeMap")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public TextElementFacetTreeMapResult getEntitiesFacetTreeMap(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final TextElementsFacetWithMetricsParams facetWithMetricsParam) {
		TextElementFacetTreeMapResult result = null;

		Timer.Context context = facetWithMetricsParam.getLeavesOnly() ? this.performanceMetrics.startTimedOperation(OperationType.EntitiesFacetLeavesOnlyWithStatsRest)
																	  :  this.performanceMetrics.startTimedOperation(OperationType.EntitiesFacetWithStatsRest);

		logger.debug("getEntitiesFacetTreeMap request invoked. Request params  = {}", () -> JSONUtils.getObjectJSON(facetWithMetricsParam));

		SearchInteractionsContext bgContext = facetWithMetricsParam.getBackgroundContext();
		if (bgContext == null) {
			bgContext = new SearchInteractionsContext();
		}

		result = facetsUIService.getTextElementsFacetTreeMap(i360FoundationToken, facetWithMetricsParam.getChannel(), facetWithMetricsParam.getSearchContext(), bgContext,
		                                                     TextElementType.Entities, facetWithMetricsParam.getHierarchyLevelNumber(), facetWithMetricsParam.getTextElements(),
		                                                     facetWithMetricsParam.getSizeByMetric(), facetWithMetricsParam.getColorByMetric(),
		                                                     facetWithMetricsParam.getQuerySpeaker(), facetWithMetricsParam.getQueryOnSameUtterance(),
		                                                     facetWithMetricsParam.getLeavesOnly(), null, null);

		this.performanceMetrics.stopTimedOperation(context);

		return result;
	}


	/**
	 * Get Relations Facet TreeMap REST method. Accepts a Search interactions
	 * context and returns Relations Facet TreeMap
	 *
	 * @param i360FoundationToken   foundation token
	 * @param facetWithMetricsParam object which encapsulates all request parameters
	 * @return DataResult with interactions
	 */
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { PrivilegeType.USEAPPLICATION })
	@Path("getRelationsFacetTreeMap")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public TextElementFacetTreeMapResult getRelationsFacetTreeMap(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final TextElementsFacetWithMetricsParams facetWithMetricsParam) {
		TextElementFacetTreeMapResult result = null;

		Timer.Context context = facetWithMetricsParam.getLeavesOnly() ?  this.performanceMetrics.startTimedOperation(OperationType.RelationsFacetLeavesOnlyWithStatsRest)
																	:  this.performanceMetrics.startTimedOperation(OperationType.RelationsFacetWithStatsRest);

		logger.debug("getRelationsFacetTreeMap request invoked. Request params  = {}", () -> JSONUtils.getObjectJSON(facetWithMetricsParam));

		SearchInteractionsContext bgContext = facetWithMetricsParam.getBackgroundContext();
		if (bgContext == null) {
			bgContext = new SearchInteractionsContext();
		}

		result = facetsUIService.getTextElementsFacetTreeMap(i360FoundationToken, facetWithMetricsParam.getChannel(), facetWithMetricsParam.getSearchContext(), bgContext,
		                                                     TextElementType.Relations, facetWithMetricsParam.getHierarchyLevelNumber(), facetWithMetricsParam.getTextElements(),
		                                                     facetWithMetricsParam.getSizeByMetric(), facetWithMetricsParam.getColorByMetric(),
		                                                     facetWithMetricsParam.getQuerySpeaker(), facetWithMetricsParam.getQueryOnSameUtterance(),
		                                                     facetWithMetricsParam.getLeavesOnly(), null, null);

		this.performanceMetrics.stopTimedOperation(context);

		return result;
	}

	/**
	 * Get Relations Facet TreeMap REST method. Accepts a Search interactions
	 * context and returns Relations Facet TreeMap
	 * 
	 * @param i360FoundationToken
	 *            foundation token
	 * @param facetWithMetricsParam object which encapsulates all request parameters
	 * @return DataResult with interactions
	 */
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { PrivilegeType.USEAPPLICATION })
	@Path("getThemesFacetTreeMap")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public TextElementFacetTreeMapResult getThemesFacetTreeMap(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final TextElementsFacetWithMetricsParams facetWithMetricsParam) {
		TextElementFacetTreeMapResult result = null;

		Timer.Context context = facetWithMetricsParam.getLeavesOnly() ? this.performanceMetrics.startTimedOperation(OperationType.ThemesFacetLeavesOnlyWithStatsRest)
																	  : this.performanceMetrics.startTimedOperation(OperationType.ThemesFacetWithStatsRest);

		logger.debug("getRelationsFacetTreeMap request invoked. Request params  = {}", () -> JSONUtils.getObjectJSON(facetWithMetricsParam));

		// TODO: Apply Background context
		SearchInteractionsContext bgContext = new SearchInteractionsContext();

		result = facetsUIService.getThemesFacetTreeMap(i360FoundationToken, facetWithMetricsParam.getChannel(), facetWithMetricsParam.getSearchContext(), bgContext,
		                                             facetWithMetricsParam.getSizeByMetric(), facetWithMetricsParam.getColorByMetric(),
		                                             facetWithMetricsParam.getQuerySpeaker(),
		                                             facetWithMetricsParam.getQueryOnSameUtterance(),
		                                             facetWithMetricsParam.getLeavesOnly());

		this.performanceMetrics.stopTimedOperation(context);

		return result;
	}

	/**
	 * Retrieves metrics of entity.
	 * @param i360FoundationToken authentication token
	 * @param textElementMetricsParams request parameters
	 * @return metrics of text element
	 */
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { PrivilegeType.USEAPPLICATION })
	@Path("getEntityMetrics")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public TextElementFacetTreeMapNode getEntityMetrics(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final TextElementMetricsParams textElementMetricsParams) {
		TextElementFacetTreeMapNode result = null;

		Timer.Context context = this.performanceMetrics.startTimedOperation(OperationType.TextElementMetricsRest);

		logger.debug("getEntityMetrics request invoked. Request params  = {}", () -> JSONUtils.getObjectJSON(textElementMetricsParams));

		SearchInteractionsContext bgContext = new SearchInteractionsContext();

		result = facetsUIService.getTextElementMetrics(i360FoundationToken, textElementMetricsParams.getChannel(), textElementMetricsParams.getSearchContext(), bgContext,
		                                               textElementMetricsParams.getTextElementType(),
		                                               textElementMetricsParams.getTextElementValue());

		this.performanceMetrics.stopTimedOperation(context);

		return result;
	}

	/**
	 * Retrieves Relation metrics.
	 * @param i360FoundationToken foundation token
	 * @param textElementMetricsParams request parameters
	 * @return metrics of relation
	 */
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { PrivilegeType.USEAPPLICATION })
	@Path("getRelationMetrics")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public TextElementFacetTreeMapNode getRelationMetrics(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final TextElementMetricsParams textElementMetricsParams) {
		TextElementFacetTreeMapNode result = null;

		Timer.Context context = this.performanceMetrics.startTimedOperation(OperationType.TextElementMetricsRest);

		logger.debug("getEntityMetrics request invoked. Request params  = {}", () -> JSONUtils.getObjectJSON(textElementMetricsParams));

		SearchInteractionsContext bgContext = new SearchInteractionsContext();

		result = facetsUIService.getTextElementMetrics(i360FoundationToken, textElementMetricsParams.getChannel(), textElementMetricsParams.getSearchContext(), bgContext,
		                                               textElementMetricsParams.getTextElementType(),
		                                               textElementMetricsParams.getTextElementValue());

		this.performanceMetrics.stopTimedOperation(context);

		return result;
	}


}
