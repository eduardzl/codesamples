package com.verint.textanalytics.web.uiservices;

import com.verint.textanalytics.bl.applicationservices.StoredSearchService;
import com.verint.textanalytics.bl.security.MembershipProvider;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.utils.CollectionUtils;
import com.verint.textanalytics.common.utils.DataUtils;
import com.verint.textanalytics.model.analyze.MetricData;
import com.verint.textanalytics.model.facets.Facet;
import com.verint.textanalytics.model.facets.FacetResultGroup;
import com.verint.textanalytics.model.facets.FacetSingleValueResultGroup;
import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import com.verint.textanalytics.model.security.User;
import com.verint.textanalytics.model.storedSearch.CategoriesRepository;
import com.verint.textanalytics.model.storedSearch.Category;
import com.verint.textanalytics.model.storedSearch.SavedSearch;
import com.verint.textanalytics.model.storedSearch.SavedSearchesRepository;
import com.verint.textanalytics.web.viewmodel.MetricsLimits;
import com.verint.textanalytics.web.viewmodel.TextElementFacetTreeMapNode;
import com.verint.textanalytics.web.viewmodel.TextElementFacetTreeMapResult;
import com.verint.textanalytics.web.viewmodel.TextElementFacetTreeNode;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Stored Search UI Service.
 *
 * @author EZlotnik
 */

/**
 * @author imor
 */
public class StoredSearchUIService extends BaseUIService {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	private ViewModelConverter viewModelConverter;

	@Autowired
	private MembershipProvider membershipProvider;

	@Autowired
	private StoredSearchService storedSearchService;

	@Autowired
	private ConfigurationManager configurationManager;


	/**
	 * @param i360FoundationToken i360FoundationToken
	 * @param channel             channel
	 * @param category            category
	 * @return category
	 */
	public com.verint.textanalytics.web.viewmodel.Category saveAsCategory(String i360FoundationToken, String channel, com.verint.textanalytics.web.viewmodel.Category category) {
		val userTenant = this.getTenantFromChannel(channel, i360FoundationToken);
		User user = this.getUser(i360FoundationToken);

		Category vtaCategoryModel = viewModelConverter.convertToCategoryModel(category);
		vtaCategoryModel.setLastModifiedByUserId(user.getUserID());

		Category addedCategory = storedSearchService.addCategory(userTenant, channel, vtaCategoryModel);
		if (addedCategory != null) {
			return viewModelConverter.convertToCategoryViewModel(addedCategory);
		}

		return null;
	}

	/**
	 * @param i360FoundationToken i360FoundationToken
	 * @param channel             channel
	 * @param savedSearch         savedSearch
	 */
	public void saveAsSearch(String i360FoundationToken, String channel, com.verint.textanalytics.web.viewmodel.SavedSearch savedSearch) {
		val userTenant = this.getTenantFromChannel(channel, i360FoundationToken);

		SavedSearch savedSearchModel = viewModelConverter.savedSearchConverterToModel(savedSearch);

		savedSearchModel.setLastModifiedByUserId(this.getUser(i360FoundationToken).getUserID());

		storedSearchService.addSavedSearch(userTenant, channel, savedSearchModel);
	}

	/**
	 * Updates existing category.
	 *
	 * @param i360FoundationToken authentication token
	 * @param channel             channel
	 * @param vwCategory          savedSearch object respresenting category
	 * @return category
	 */
	public com.verint.textanalytics.web.viewmodel.SaveStoredSearchResult updateCategory(String i360FoundationToken, String channel, com.verint.textanalytics.web.viewmodel.Category vwCategory) {
		val saveStoredSearchResult = new com.verint.textanalytics.web.viewmodel.SaveStoredSearchResult();

		String userTenant = this.getTenantFromChannel(channel, i360FoundationToken);

		Category category = viewModelConverter.convertToCategoryModel(vwCategory);
		category.setLastModifiedByUserId(this.getUser(i360FoundationToken).getUserID());

		// update category
		Category updatedCategory = storedSearchService.updateCategory(userTenant, channel, category);

		// get updated category last change date
		saveStoredSearchResult.setLastChangedDateTimeGMT(DataUtils.getISO8601StringFromDate(updatedCategory.getLastChangeDateTimeGMT()));
		saveStoredSearchResult.setLastChangedDateTimeGMTMillis(updatedCategory.getLastChangeDateTimeGMT().getMillis());

		return saveStoredSearchResult;
	}

	/**
	 * @param i360FoundationToken i360FoundationToken
	 * @param channel             channel
	 * @return List<SavedSearch>
	 */
	public com.verint.textanalytics.web.viewmodel.CategoriesRepository getCategories(String i360FoundationToken, String channel) {
		val userTenant = this.getTenantFromChannel(channel, i360FoundationToken);

		val res = storedSearchService.getCategories(userTenant, channel);
		return viewModelConverter.categoriesListConverterToViewModel(res);
	}

	/**
	 * Convert Categories to view model.
	 * @param i360FoundationToken authentication token
	 * @param channel channel
	 * @return view model categories
	 */
	public com.verint.textanalytics.web.viewmodel.CategoriesRepository getCategoriesWithReprocessingState(String i360FoundationToken, String channel) {
		String userTenant = this.getTenantFromChannel(channel, i360FoundationToken);

		CategoriesRepository categoriesRepository = storedSearchService.getCategoriesWithReprocessingState(userTenant, channel);
		if (categoriesRepository != null) {
			return viewModelConverter.categoriesListConverterToViewModel(categoriesRepository);
		}

		return null;
	}

	/**
	 * @param i360FoundationToken i360FoundationToken
	 * @param channel             channel
	 * @return List<SavedSearch>
	 */
	public com.verint.textanalytics.web.viewmodel.SavedSearchesRepository getSavedSearches(String i360FoundationToken, String channel) {
		val userTenant = this.getTenantFromChannel(channel, i360FoundationToken);

		SavedSearchesRepository savedSearchesRepository = storedSearchService.getSavedSearches(userTenant, channel);
		if (savedSearchesRepository != null) {
			return viewModelConverter.savedSearchesListConverterToViewModel(savedSearchesRepository);
		}

		return null;
	}

	/**
	 * @param i360FoundationToken i360FoundationToken
	 * @param channel             channel
	 * @param category            category
	 */
	public void deleteCategory(String i360FoundationToken, String channel, com.verint.textanalytics.web.viewmodel.Category category) {

		val userTenant = this.getTenantFromChannel(channel, i360FoundationToken);

		Category vtaCategoryModel = viewModelConverter.convertToCategoryModel(category);

		storedSearchService.deleteCategory(userTenant, channel, vtaCategoryModel);
	}

	/**
	 * @param i360FoundationToken i360FoundationToken
	 * @param channel             channel
	 * @param savedSearch         savedSearch
	 */
	public void deleteSavedSearch(String i360FoundationToken, String channel, com.verint.textanalytics.web.viewmodel.SavedSearch savedSearch) {

		val userTenant = this.getTenantFromChannel(channel, i360FoundationToken);

		SavedSearch savedSearchModel = viewModelConverter.savedSearchConverterToModel(savedSearch);

		storedSearchService.deleteSavedSearch(userTenant, channel, savedSearchModel);
	}

	/**
	 * Invokes reprocess category request.
	 *
	 * @param i360FoundationToken foundation token of request
	 * @param channel             channel
	 * @param categoryId          category id
	 */
	public void invokeReprocessCategory(String i360FoundationToken, String channel, int categoryId) {
		String tenant = this.getTenantFromChannel(channel, i360FoundationToken);

		// invoke Category Reprocessing
		storedSearchService.invokeCategoryReprocessing(tenant, channel, categoryId);
	}

	/**
	 * Retrieves categories facet.
	 *
	 * @param i360FoundationToken i360FoundationToken
	 * @param channel             channel
	 * @param searchContext       searchContext
	 * @return List<Facet>
	 */
	public TextElementFacetTreeNode getCategoriesFacet(String i360FoundationToken, String channel, SearchInteractionsContext searchContext) {

		TextElementFacetTreeNode result = new TextElementFacetTreeNode();

		val userTenant = this.getTenantFromChannel(channel, i360FoundationToken);

		Facet categoriesFacet = storedSearchService.getCategoriesFacetTree(userTenant, channel, searchContext);
		if (categoriesFacet != null) {

			List<FacetResultGroup> facetValues = categoriesFacet.getValues();
			if (!CollectionUtils.isEmpty(facetValues)) {

				val lstTextElementsNodes = new ArrayList<TextElementFacetTreeNode>();

				for (FacetResultGroup facetValue : facetValues) {
					// convert category facet node to text element
					lstTextElementsNodes.add(this.viewModelConverter.convertToViewModelTextElementFacet(facetValue));
				}

				result.setChildren(lstTextElementsNodes);
			}
		}

		return result;
	}

	/**
	 * Retrieves categories facet.
	 *
	 * @param i360FoundationToken i360FoundationToken
	 * @param channel             channel
	 * @param searchContext       searchContext
	 * @param backgroundContext   backgroundContext

	 * @return List<Facet>
	 */
	public TextElementFacetTreeMapResult getCategoriesFacetWithMetrics(String i360FoundationToken, String channel, SearchInteractionsContext searchContext, SearchInteractionsContext backgroundContext) {
		TextElementFacetTreeMapResult result = new TextElementFacetTreeMapResult();
		MetricsLimits metricsLimits = new MetricsLimits();
		TextElementFacetTreeMapNode categoryNode = null;

		val userTenant = this.getTenantFromChannel(channel, i360FoundationToken);

		Facet categoriesFacet = storedSearchService.getCategoriesFacetWithMetrics(userTenant, channel, searchContext, backgroundContext);

		if (categoriesFacet != null) {

			List<FacetResultGroup> facetValues = categoriesFacet.getValues();
			if (!CollectionUtils.isEmpty(facetValues)) {

				val lstTextElementsNodes = new ArrayList<TextElementFacetTreeMapNode>();

				for (FacetResultGroup facetValue : facetValues) {
					// convert category facet node to text element
					categoryNode = this.viewModelConverter.convertCategoryFacetGroupToViewModelTextElementTreeMapNode((FacetSingleValueResultGroup) facetValue);

					lstTextElementsNodes.add(categoryNode);

					// update limits of metrics (min, max ... for each metric)
					updateMetricsLimitsData(categoryNode, metricsLimits, 1);
				}

				result.setTextElementFacetTreeMapNodes(lstTextElementsNodes);
				result.setMetricsLimitsData(metricsLimits.getMetricsLimitsMapByLevel());
			}
		}

		return result;
	}

	/**
	 * Retrieves category metrics.
	 * @param i360FoundationToken foundation token
	 * @param channel channel
	 * @param searchContext search interactions context
	 * @param backgroundContext search backgound context
	 * @param categoryId category
	 * @param categoryName name of category to prevent searching category repository
	 * @return category metrics
	 */
	public TextElementFacetTreeMapNode getCategoryMetrics(String i360FoundationToken, String channel, SearchInteractionsContext searchContext, SearchInteractionsContext backgroundContext, Integer categoryId, String categoryName) {
		TextElementFacetTreeMapNode categoryWithMetrics = null;

		String userTenant = this.getTenantFromChannel(channel, i360FoundationToken);

		List<MetricData> categoryMetrics = storedSearchService.getCategoryMetrics(userTenant, channel, searchContext, backgroundContext, categoryId);

		categoryWithMetrics = viewModelConverter.convertToCategoryMetrics(categoryId, categoryName, categoryMetrics);

		return categoryWithMetrics;
	}


	private void updateMetricsLimitsData(TextElementFacetTreeMapNode categoryNode, MetricsLimits metricsLimits, int level) {
		if (categoryNode.getMetrics() != null) {
			for (val metricData : categoryNode.getMetrics().values()) {
				// updates metrics limit data structure
				metricsLimits.updateMetricLimit(level, metricData);
			}
		}

		if (categoryNode.getChildren() != null) {
			for (val childNode : categoryNode.getChildren()) {
				updateMetricsLimitsData(childNode, metricsLimits, level + 1);
			}
		}
	}
}
