package com.verint.textanalytics.bl.applicationservices;

import com.google.common.base.Throwables;
import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.exceptions.StoredSearchesErrorCode;
import com.verint.textanalytics.common.exceptions.StoredSearchesException;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionErrorCode;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionException;
import com.verint.textanalytics.common.utils.CollectionUtils;
import com.verint.textanalytics.common.utils.IDGeneratorUtils;
import com.verint.textanalytics.common.utils.StringUtils;
import com.verint.textanalytics.common.utils.ThreadUtils;
import com.verint.textanalytics.dal.configService.ConfigurationServiceProvider;
import com.verint.textanalytics.dal.darwin.TextAnalyticsProvider;
import com.verint.textanalytics.model.analyze.FieldMetric;
import com.verint.textanalytics.model.analyze.MetricData;
import com.verint.textanalytics.model.documentSchema.FieldDataType;
import com.verint.textanalytics.model.facets.Facet;
import com.verint.textanalytics.model.facets.FacetResultGroup;
import com.verint.textanalytics.model.interactions.FilterField;
import com.verint.textanalytics.model.interactions.FilterFieldValue;
import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import com.verint.textanalytics.model.storedSearch.*;
import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Categories management service.
 *
 * @author EZlotnik
 */
public class StoredSearchService {
	private static final String RETRY = "RetryNumber-";

	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	private ConfigurationServiceProvider configurationServiceProvider;

	@Autowired
	private TextAnalyticsProvider textAnalyticsProvider;

	@Autowired
	private StoredSearchesMerger storedSearchesMerger;

	@Autowired
	private FacetsService facetsService;

	@Autowired
	private ConfigurationManager configurationManager;

	@Autowired
	private CategoriesReprocessingService categoriesReprocessingService;

	@Autowired
	private CurrentResultSetMetricsService resultSetMetricsService;

	@Autowired
	private SearchInteractionsService searchInteractionsService;

	@Autowired
	@Setter
	private CategoriesReprocessingService categoryReprocessingService;

	@Autowired
	@Setter
	private ConfigurationService configurationService;

	private List<FieldMetric> categoriesFacetMetrics;

	private List<String> categoriesFacetQuery;

	@Setter
	private int numOfRetries;

	private final String categoryMetricsThreadKey = "categoryMetrics";
	private final String categoryMetricsBackgroundThreadKey = "categoryMetricsBackground";
	private final String interactionsNumberThreadKey = "interactionsNumber";
	private final String categoriesThreadKey = "categories";
	private final String categoriesReprocessingStatusThreadKey = "categoriesReprocessingStatus";


	/**
	 * Constructor.
	 *
	 * @param categoriesFacetMetrics metrics to calculate for facet on Chart
	 */
	public StoredSearchService(List<FieldMetric> categoriesFacetMetrics) {
		this.categoriesFacetMetrics = categoriesFacetMetrics;
		this.categoriesFacetQuery = Arrays.asList(TAConstants.SchemaFieldNames.categoriesIds);
	}

	/**
	 * Initialization function.
	 */
	public void initialize() {
		this.numOfRetries = configurationManager.getApplicationConfiguration().getNumberOfRetriesInStoredSearches();
	}

	/***
	 * @param tenant  tenant
	 * @param channel channel
	 * @return CategoriesRepository
	 */
	public CategoriesRepository getCategories(String tenant, String channel) {
		CategoriesRepository repository = configurationServiceProvider.getListOfCategories(tenant, channel);
		return repository;
	}

	/**
	 * Retrieves categories with reprocessing state.
	 *
	 * @param tenant  tenant
	 * @param channel channel
	 * @return list of categories
	 */
	public CategoriesRepository getCategoriesWithReprocessingState(String tenant, String channel) {
		ExecutorService threadPool = null;
		CategoriesRepository categoriesRepo = null;
		List<CategoryReprocessingState> categoriesReprocessingStatuses = null;

		try {
			threadPool = Executors.newFixedThreadPool(2);

			String requestId = ThreadContext.get(TAConstants.requestId);
			val tasks = new ArrayList<Callable<Object>>();

			tasks.add(() -> {
				ThreadContext.put(TAConstants.requestId, requestId);

				return new ImmutablePair<>(categoriesThreadKey, configurationServiceProvider.getListOfCategories(tenant, channel));
			});

			tasks.add(() -> {
				ThreadContext.put(TAConstants.requestId, requestId);

				return new ImmutablePair<>(categoriesReprocessingStatusThreadKey, configurationServiceProvider.getCategoriesReprocessingState(tenant, channel));
			});

			ApplicationConfiguration appConfig = configurationManager.getApplicationConfiguration();

			// invokeAll() returns when all tasks are complete
			List<Future<Object>> lstFutures = threadPool.invokeAll(tasks, appConfig.getDarwinRestRequestTimeout(), TimeUnit.SECONDS);

			if (lstFutures != null) {
				for (Future<?> future : lstFutures) {
					if (future.isDone()) {

						// get tasks result of task is done
						Object taskResult = future.get();
						if (taskResult != null) {
							ImmutablePair<String, ?> resultPair = (ImmutablePair<String, ?>) taskResult;

							switch (resultPair.getLeft()) {
								case categoriesThreadKey:
									categoriesRepo = (CategoriesRepository) resultPair.getRight();
									break;
								case categoriesReprocessingStatusThreadKey:
									categoriesReprocessingStatuses = (List<CategoryReprocessingState>) resultPair.getRight();
									break;
								default:
									break;
							}
						}
					}
				}
			}

		} catch (ExecutionException ex) {
			Boolean propagateEx = true;

			List<Throwable> causeChain = Throwables.getCausalChain(ex);
			if (!CollectionUtils.isEmpty(causeChain)) {

				Optional<Throwable> storedSearchThOpt = causeChain.stream().filter(th -> th instanceof StoredSearchesException).findFirst();
				if (storedSearchThOpt.isPresent()) {
					StoredSearchesException stEx = (StoredSearchesException) storedSearchThOpt.get();

					// Categories Reprocessing status error should not be propogated
					// to allow Categories Grid to be display when no Categories Reprocessing status is available
					if (stEx.getAppExecutionErrorCode().equals(StoredSearchesErrorCode.CategoriesReprocessingStatusRetrieveError)) {
						// just propogate
						propagateEx = false;

						logger.error("Exception while retrieving Categories Reprocessing Status for tenant - {}, channel - {}", tenant, channel, ex);
					}
				}
			}

			if (propagateEx) {
				Throwables.propagate(ex);
			}
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, StoredSearchesException.class);
			Throwables.propagate(new StoredSearchesException(ex, StoredSearchesErrorCode.CategoriesRetriveError));
		} finally {

			try {
				ThreadUtils.shutdownExecutionThreadPool(threadPool);
			} catch (Exception ex) {
				logger.warn("Failed to shutdown Thread execution pool.", ex);
			}

			if (categoriesRepo != null && !CollectionUtils.isEmpty(categoriesRepo.getCategories())) {
				// merge category reprocessing state
				this.mergeCategoriesReprocessingStatus(categoriesRepo, categoriesReprocessingStatuses);

				// update "is reprocessing allowed" and "should be reprocessed" indications
				this.processCategoriesReprocessingStatus(categoriesRepo);
			}
		}

		return categoriesRepo;
	}

	/***
	 * @param tenant  tenant
	 * @param channel channel
	 * @return SavedSearchesRepository
	 */
	public SavedSearchesRepository getSavedSearches(String tenant, String channel) {
		SavedSearchesRepository repository = configurationServiceProvider.getListOfSavedSearches(tenant, channel);
		return repository;
	}

	/***
	 * @param tenant      tenant
	 * @param channel     channel
	 * @param newCategory the category that needs to be added
	 * @return category
	 */
	public Category addCategory(String tenant, String channel, Category newCategory) {

		isValidCategory(newCategory);

		StoredSearchesException storedSearchesException = null;

		String language = configurationService.getChannelLanguage(tenant, channel);

		// update query to category
		StoredSearchQuery storedSearchQuery = textAnalyticsProvider.getSearchInteractionsQueryForCategory(tenant, channel, newCategory.getSearchContext(), language);
		newCategory.setQuery(storedSearchQuery.getQuery());
		newCategory.setDebugQuery(storedSearchQuery.getDebugQuery());

		CategoriesRepository repository;
		int i;

		for (i = 0; i < this.numOfRetries; i++) {
			try {
				repository = configurationServiceProvider.getListOfCategories(tenant, channel);

				repository.setMaxId(repository.getMaxId() + 1);

				newCategory.setId(IDGeneratorUtils.generateCategoryID(channel, repository.getMaxId()));
				newCategory.setLastChangeDateTimeGMT(DateTime.now(DateTimeZone.UTC));

				// add new category
				repository.setCategories(this.storedSearchesMerger.addNewStoredSearch(repository.getCategories(), newCategory));

				// store categories repository
				configurationServiceProvider.updateCategoryFile(tenant, channel, repository, repository.getTimeStamp());

				storedSearchesException = null;

				break;

			} catch (Exception ex) {

				logger.debug("addCategory : Failed to add category to server. Attempt number: {} , Tenant: {} , channel: {}. Error Messsage : {}", i, tenant, channel,
				             ex.getMessage());

				// capture exception only first exception
				if (storedSearchesException == null) {

					// if exception thrown is allready StoredSearchesException, so use it
					if (ex instanceof StoredSearchesException) {
						storedSearchesException = (StoredSearchesException) ex;
					} else {
						storedSearchesException = new StoredSearchesException(ex, StoredSearchesErrorCode.CategoriesAddError);
					}
				}

				storedSearchesException.put(RETRY + i, i);
			}
		}

		// we throw exception just in case that we failed all the times
		if (storedSearchesException != null) {
			throw storedSearchesException;
		}

		return newCategory;
	}

	/***
	 * @param tenant         tenant
	 * @param channel        channel
	 * @param newSavedSearch the saved search that needs to be added
	 */
	public void addSavedSearch(String tenant, String channel, SavedSearch newSavedSearch) {

		isValidSavedSearch(newSavedSearch);

		StoredSearchesException storedSearchesException = null;

		String language = configurationService.getChannelLanguage(tenant, channel);

		// update query to saved search
		StoredSearchQuery storedSearchQuery = textAnalyticsProvider.getSearchInteractionsQueryForCategory(tenant, channel, newSavedSearch.getSearchContext(), language);
		newSavedSearch.setQuery(storedSearchQuery.getQuery());
		newSavedSearch.setDebugQuery(storedSearchQuery.getDebugQuery());

		SavedSearchesRepository repository;

		int i;
		for (i = 0; i < this.numOfRetries; i++) {
			try {
				repository = configurationServiceProvider.getListOfSavedSearches(tenant, channel);
				repository.setMaxId(repository.getMaxId() + 1);

				newSavedSearch.setId(repository.getMaxId());
				newSavedSearch.setLastChangeDateTimeGMT(DateTime.now(DateTimeZone.UTC));

				repository.setSavedSearches(this.storedSearchesMerger.addNewStoredSearch(repository.getSavedSearches(), newSavedSearch));

				configurationServiceProvider.updateSavedSearchesFile(tenant, channel, repository, repository.getTimeStamp());

				storedSearchesException = null;

				break;
			} catch (Exception ex) {

				logger.debug("addSavedSearch : Failed to add SavedSearch to server. Attempt number: {} , Tenant: {} , channel: {}. Error Messsage : {}", i, tenant, channel,
				             ex.getMessage());

				// capture exception only first exception
				if (storedSearchesException == null) {

					// if exception thrown is allready StoredSearchesException, so use it
					if (ex instanceof StoredSearchesException) {
						storedSearchesException = (StoredSearchesException) ex;
					} else {
						storedSearchesException = new StoredSearchesException(ex, StoredSearchesErrorCode.SavedSearchAddError);
					}
				}

				storedSearchesException.put(RETRY + i, i);
			}
		}

		// we throw exception just in case that we failed all the times
		if (storedSearchesException != null) {
			throw storedSearchesException;
		}
	}

	/***
	 * @param tenant           tenant
	 * @param channel          channel
	 * @param categoryToRemove the category that needs to be deleted
	 */
	public void deleteCategory(String tenant, String channel, Category categoryToRemove) {
		StoredSearchesException storedSearchesException = null;
		CategoriesRepository repository;

		logger.info("Deleting Category with Id - {}, Name - {} for channel - {}, tenant - {}", categoryToRemove.getId(), categoryToRemove.getName(), channel, tenant);

		int i;
		for (i = 0; i < numOfRetries; i++) {
			try {
				repository = configurationServiceProvider.getListOfCategories(tenant, channel);

				// remove category from list
				// if category not found, exception will be thrown
				repository.setCategories(this.storedSearchesMerger.deleteStoredSearch(repository.getCategories(), categoryToRemove));

				configurationServiceProvider.updateCategoryFile(tenant, channel, repository, repository.getTimeStamp());

				// reset exeption if updated succeeded
				storedSearchesException = null;

				logger.info("Category with Id - {}, Name - {} was deleted successefully for channel - {}, tenant - {}", categoryToRemove.getId(), categoryToRemove.getName(),
				            channel, tenant);

				break;

			} catch (Exception ex) {
				logger.debug("deleteCategory : Failed to delete category to server. Attempt number: {} , Tenant: {} , channel: {}. Error Messsage : {}", i, tenant, channel,
				             ex.getMessage());

				// capture exception only first exception
				if (storedSearchesException == null) {

					// if exception thrown is allready StoredSearchesException, so use it
					if (ex instanceof StoredSearchesException) {
						storedSearchesException = (StoredSearchesException) ex;
					} else {
						storedSearchesException = new StoredSearchesException(ex, StoredSearchesErrorCode.CategoryRemoveError);
					}
				}

				storedSearchesException.put(RETRY + i, i);
			}
		}

		// we throw exception just in case that we failed all the times
		if (storedSearchesException != null) {
			throw storedSearchesException;
		}
	}

	/***
	 * @param tenant              tenant
	 * @param channel             channel
	 * @param savedSearchToRemove the saved search that needs to be deleted
	 */
	public void deleteSavedSearch(String tenant, String channel, SavedSearch savedSearchToRemove) {

		StoredSearchesException storedSearchesException = null;
		SavedSearchesRepository repository;

		logger.info("Deleting Saved Search with Id - {}, Name - {} for channel - {}, tenant - {}", savedSearchToRemove.getId(), savedSearchToRemove.getName(), channel, tenant);

		int i;
		for (i = 0; i < this.numOfRetries; i++) {
			try {

				repository = configurationServiceProvider.getListOfSavedSearches(tenant, channel);

				repository.setSavedSearches(this.storedSearchesMerger.deleteStoredSearch(repository.getSavedSearches(), savedSearchToRemove));

				configurationServiceProvider.updateSavedSearchesFile(tenant, channel, repository, repository.getTimeStamp());

				// reset exeption if updated succeeded
				storedSearchesException = null;

				logger.info("Saved Search with Id - {}, Name - {} was deleted successefully for channel - {}, tenant - {}", savedSearchToRemove.getId(),
				            savedSearchToRemove.getName(), channel, tenant);

				break;

			} catch (Exception ex) {
				logger.error("deleteSavedSearch : Failed to delete SavedSearch to server. Attempt number: {} , Tenant: {} , channel: {}. Error Messsage : {}", i, tenant, channel,
				             ex.getMessage());

				// capture exception only first exception
				if (storedSearchesException == null) {

					// if exception thrown is allready StoredSearchesException, so use it
					if (ex instanceof StoredSearchesException) {
						storedSearchesException = (StoredSearchesException) ex;
					} else {
						storedSearchesException = new StoredSearchesException(ex, StoredSearchesErrorCode.SavedSearchesRemoveError);
					}
				}

				storedSearchesException.put(RETRY + i, i);
			}
		}

		// we throw exception just in case that we failed all the times
		if (storedSearchesException != null) {
			throw storedSearchesException;
		}
	}


	/***
	 * @param tenant  tenant
	 * @param channel channel
	 * @return map of id and category for this tenant and channel
	 */
	public Map<Integer, Category> getCategoriesMap(String tenant, String channel) {
		try {
			CategoriesRepository categoriesRep = this.getCategories(tenant, channel);

			if (categoriesRep != null && categoriesRep.getCategories() != null) {

				// @formatter:off
				return  categoriesRep.getCategories()
				                     .stream()
				                     .collect(Collectors.toMap(c -> c.getId(), c -> c));
				// @formatter:on
			}
		} catch (Exception ex) {
			logger.error("getCategoriesMap : Failed to get categories map. Error Messsage : {}", ex.getMessage());
		}

		return new HashMap<Integer, Category>();
	}

	/***
	 * @param tenant  tenant
	 * @param channel channel
	 * @return map of id and category for this tenant and channel
	 */
	public Map<String, Category> getActiveCategoriesMap(String tenant, String channel) {

		try {
			CategoriesRepository categoriesRep = this.getCategories(tenant, channel);

			if (categoriesRep != null && categoriesRep.getCategories() != null) {

				// @formatter:off
				return  categoriesRep.getCategories()
		                             .stream()
		                             .filter(c -> c.isActive())
		                             .collect(Collectors.toMap(c -> String.valueOf(c.getId()), c -> c));
				// @formatter:on
			}
		} catch (StoredSearchesException ex) {
			Throwables.propagate(ex);
		} catch (Exception ex) {
			logger.error("getCategoriesMap : Failed to get categories map.", ex);

			Throwables.propagate(new StoredSearchesException(ex, StoredSearchesErrorCode.CategoriesRetriveError));
		}

		return new HashMap<String, Category>();
	}

	/***
	 * @param tenant   tenant
	 * @param channel  channel
	 * @param category the category that needs to be updated
	 * @return the updated Category
	 */
	public Category updateCategory(String tenant, String channel, Category category) {
		StoredSearchesException storedSearchesException = null;

		int i = 0;
		if (category != null) {
			logger.info("Updating Category with Id - {}, Name - {} for channel - {}, tenant - {}", category.getId(), category.getName(), channel, tenant);

			isValidCategory(category);

			String language = configurationService.getChannelLanguage(tenant, channel);

			// update query to category
			StoredSearchQuery storedSearchQuery = textAnalyticsProvider.getSearchInteractionsQueryForCategory(tenant, channel, category.getSearchContext(), language);
			category.setQuery(storedSearchQuery.getQuery());
			category.setDebugQuery(storedSearchQuery.getDebugQuery());

			CategoriesRepository repository;

			for (i = 0; i < this.numOfRetries; i++) {
				try {

					repository = configurationServiceProvider.getListOfCategories(tenant, channel);

					DateTime dateTimeNow = DateTime.now(DateTimeZone.UTC);

					repository.setCategories(this.storedSearchesMerger.updateStoredSearch(repository.getCategories(), category, dateTimeNow));

					configurationServiceProvider.updateCategoryFile(tenant, channel, repository, repository.getTimeStamp());

					// reset stored search exception if update succeeded
					storedSearchesException = null;

					category.setLastChangeDateTimeGMT(dateTimeNow);

					logger.info("Category with Id - {}, Name - {} was updated successefully for channel - {}, tenant - {}", category.getId(), category.getName(), channel, tenant);

					break;

				} catch (Exception ex) {

					logger.error("updateCategory : Failed to update category to server. Attempt number: {} , Tenant: {} , channel: {}, category id: {}. Error Messsage : {}", i,
					             tenant, channel, category.getId(), ex.getMessage());

					// capture exception only first exception
					if (storedSearchesException == null) {

						// if exception thrown is allready StoredSearchesException, so use it
						if (ex instanceof StoredSearchesException) {
							storedSearchesException = (StoredSearchesException) ex;
						} else {
							storedSearchesException = new StoredSearchesException(ex, StoredSearchesErrorCode.CategoryUpdateError);
						}
					}

					storedSearchesException.put(RETRY + i, i);
				}
			}
		}

		// we throw exception just in case that we failed all the times
		if (storedSearchesException != null) {
			throw storedSearchesException;
		}

		return category;
	}

	/***
	 * @param tenant      tenant
	 * @param channel     channel
	 * @param savedSearch the saved search that needs to be updated
	 * @return the updated saved search
	 */
	public SavedSearch updateSavedSearch(String tenant, String channel, SavedSearch savedSearch) {

		StoredSearchesException storedSearchesException = null;
		SavedSearchesRepository repository;
		List<SavedSearch> savedSearches = null;

		int i = 0;
		if (savedSearch != null) {
			logger.info("Updating Saved Search with Id - {}, Name - {} for channel - {}, tenant - {}", savedSearch.getId(), savedSearch.getName(), channel, tenant);

			String language = configurationService.getChannelLanguage(tenant, channel);

			// update query of saved search
			StoredSearchQuery savedSearchQuery = textAnalyticsProvider.getSearchInteractionsQueryForCategory(tenant, channel, savedSearch.getSearchContext(), language);
			savedSearch.setQuery(savedSearchQuery.getQuery());
			savedSearch.setDebugQuery(savedSearchQuery.getDebugQuery());

			for (i = 0; i < this.numOfRetries; i++) {
				try {
					repository = configurationServiceProvider.getListOfSavedSearches(tenant, channel);
					if (repository != null) {
						savedSearches = repository.getSavedSearches();

						DateTime dateTimeNow = DateTime.now(DateTimeZone.UTC);

						repository.setSavedSearches(this.storedSearchesMerger.updateStoredSearch(repository.getSavedSearches(), savedSearch, dateTimeNow));

						configurationServiceProvider.updateSavedSearchesFile(tenant, channel, repository, repository.getTimeStamp());

						// reset stored search exception if update succeeded
						storedSearchesException = null;

						logger.info("Saved Search with Id - {}, Name - {}  was updated succesefully for channel - {}, tenant - {}", savedSearch.getId(), savedSearch.getName(),
						            channel, tenant);

						// update Saved Search last changed date
						savedSearch.setLastChangeDateTimeGMT(dateTimeNow);

						break;
					}
				} catch (Exception ex) {

					logger.error(
							"updateSavedSearch : Failed to update saved search to server. Attempt number: {} , Tenant: {} , channel: {}, saved search id: {}. Error Messsage : {}",
							i, tenant, channel, savedSearch.getId(), ex.getMessage());

					// capture exception only first exception
					if (storedSearchesException == null) {

						// if exception thrown is allready StoredSearchesException, so use it
						if (ex instanceof StoredSearchesException) {
							storedSearchesException = (StoredSearchesException) ex;
						} else {
							storedSearchesException = new StoredSearchesException(ex, StoredSearchesErrorCode.SavedSearchUpdateError);
						}
					}

					storedSearchesException.put(RETRY + i, i);
				}
			}
		}

		// we throw exception just in case that we failed all the times
		if (storedSearchesException != null) {
			throw storedSearchesException;
		}

		return savedSearch;
	}

	/**
	 * Invokes request for category reprocessing.
	 *
	 * @param tenant     tenant
	 * @param channel    channel
	 * @param categoryId category id
	 */
	public void invokeCategoryReprocessing(String tenant, String channel, int categoryId) {
		try {
			logger.debug("Invoking request to reprocess category with id {} for channel {} tenant {}", categoryId, channel, tenant);

			// retrieve list of categories
			CategoriesRepository categoriesRepo = this.configurationServiceProvider.getListOfCategories(tenant, channel);

			if (categoriesRepo != null) {
				List<Category> categories = categoriesRepo.getCategories();
				if (!CollectionUtils.isEmpty(categories)) {

					// locate category to reprocess
					Optional<Category> categoryToReprocessFound = categories.stream().filter(c -> c.getId() == categoryId).findFirst();
					if (categoryToReprocessFound.isPresent()) {
						Category categoryToReprocess = categoryToReprocessFound.get();

						DateTime dateTimeNow = DateTime.now(DateTimeZone.UTC);

						if (this.categoriesReprocessingService.isCategoryReprocessingAllowed(categoryToReprocess)) {
							// invoke start reprocessing request
							this.configurationServiceProvider.invokeCategoryReprocessing(tenant, channel, categoryId, CategoryReprocessingType.Update);

							categoriesRepo.setCategories(categories);

							// update categories in storage
							this.configurationServiceProvider.updateCategoryFile(tenant, channel, categoriesRepo, categoriesRepo.getTimeStamp());

						} else {
							// category reprocessing is not allowed
							throw new StoredSearchesException(StoredSearchesErrorCode.CategoryReprocessingIsNotAllowed);
						}
					} else {
						// category to reprocess was not found
						throw new StoredSearchesException(StoredSearchesErrorCode.CategoryUpdateNotFoundError);
					}
				} else {
					// No Categories for this channel, category to reprocess was not found
					throw new StoredSearchesException(StoredSearchesErrorCode.CategoryUpdateNotFoundError);
				}
			}

		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, StoredSearchesException.class);
			Throwables.propagate(new StoredSearchesException(ex, StoredSearchesErrorCode.CategoryReprocessingInvocationError));
		}
	}

	/**
	 * Calculates category metrics.
	 *
	 * @param tenant        tenant
	 * @param channel       channel
	 * @param searchContext search context
	 * @param categoryId    category id
	 * @return metrics data calculated on category result set
	 */
	public List<MetricData> getCategoryMetrics(String tenant, String channel, SearchInteractionsContext searchContext, Integer categoryId) {
		ExecutorService threadPool = null;
		List<MetricData> categoryMetrics = null;

		if (searchContext != null) {
			SearchInteractionsContext clonedContext = new SearchInteractionsContext(searchContext.getTerms(), searchContext.getFilterFields(), searchContext.getRangeFilterFields());

			// Add category id to Search
			FilterField filterField = new FilterField();
			filterField.setName(TAConstants.SchemaFieldNames.categoriesIds);
			filterField.setDataType(FieldDataType.Int);
			filterField.setValues(new FilterFieldValue[] { new FilterFieldValue(String.valueOf(categoryId)) });
			clonedContext.getFilterFields().add(filterField);

			// retrieve Category Metrics
			return resultSetMetricsService.getResultSetMetrics(tenant, channel, clonedContext, categoriesFacetMetrics, true);
		}

		return null;
	}

	/**
	 * Retrives Category metrics data include VolumePercentage and Correlation percentage.
	 *
	 * @param tenant            tenant
	 * @param channel           channel
	 * @param searchContext     search interactions context
	 * @param backgroundContext search interactions background context
	 * @param categoryId        category id
	 * @return category metrics including VolumePercentage and Correlation percentage
	 */
	public List<MetricData> getCategoryMetrics(String tenant, String channel,  SearchInteractionsContext searchContext, SearchInteractionsContext backgroundContext, Integer categoryId) {
		List<MetricData> categoryMetrics = null;
		Integer searchInteractionsNumber = 0;
		Integer backgoundInteractionsNumber = 0;

		ExecutorService threadPool = null;

		try {
			threadPool = Executors.newFixedThreadPool(3);

			String requestId = ThreadContext.get(TAConstants.requestId);

			val tasks = new ArrayList<Callable<Object>>();

			//add tasks to get all data after filter and tree bulding
			ApplicationConfiguration appConfig = configurationManager.getApplicationConfiguration();

			tasks.add(() -> {
				ThreadContext.put(TAConstants.requestId, requestId);
				return  new ImmutablePair<>(categoryMetricsThreadKey, this.getCategoryMetrics(tenant, channel, searchContext, categoryId));
			});

			tasks.add(() -> {
				ThreadContext.put(TAConstants.requestId, requestId);

				// Add Category Id to Backgound Search Context
				FilterField filterField = new FilterField();
				filterField.setName(TAConstants.SchemaFieldNames.categoriesIds);
				filterField.setDataType(FieldDataType.Int);
				filterField.setValues(new FilterFieldValue[] { new FilterFieldValue(String.valueOf(categoryId)) });
				backgroundContext.getFilterFields().add(filterField);

				return new ImmutablePair<>(categoryMetricsBackgroundThreadKey, searchInteractionsService.getResultSetInteractionsQuantity(tenant, channel, backgroundContext));
			});

			// Get counting for interactions
			tasks.add(() -> {
				ThreadContext.put(TAConstants.requestId, requestId);
				return new ImmutablePair<>(interactionsNumberThreadKey,  searchInteractionsService.getResultSetInteractionsQuantity(tenant, channel, searchContext));
			});

			// invokeAll() returns when all tasks are complete
			List<Future<Object>> lstFutures = threadPool.invokeAll(tasks, appConfig.getDarwinRestRequestTimeout() * 2, TimeUnit.SECONDS);

			if (lstFutures != null) {

				for (Future<?> future : lstFutures) {
					if (future.isDone()) {
						// get tasks result of task is done
						val taskResult = (Pair<String, Object>) future.get();
						if (taskResult != null) {
							switch (taskResult.getLeft()) {
								case categoryMetricsThreadKey:
									if (taskResult.getRight() != null) {
										categoryMetrics = (List<MetricData>) taskResult.getRight();
									}
									break;
								case categoryMetricsBackgroundThreadKey:
									if (taskResult.getRight() != null) {
										backgoundInteractionsNumber = (Integer) taskResult.getRight();
									}
									break;
								case interactionsNumberThreadKey:
									if (taskResult != null) {
										searchInteractionsNumber = (Integer) taskResult.getRight();
									}
									break;
								default:
									break;
							}
						}
					}
				}
			}

			// make sure list not empty
			if (categoryMetrics == null) {
				categoryMetrics = new ArrayList<>();
			}

			// add Percentage metric
			Optional<MetricData> volumeMetric = categoryMetrics.stream().filter(m -> m.getName().equals(TAConstants.MetricsQuery.volume)).findFirst();
			if (volumeMetric.isPresent()) {
				categoryMetrics.add(new MetricData(TAConstants.MetricsQuery.volumePercentage,
				                                   searchInteractionsNumber != 0 ? volumeMetric.get().getValue() / searchInteractionsNumber * TAConstants.percentage_100 : 0));

				categoryMetrics.add(new MetricData(TAConstants.MetricsQuery.correlationPercentage,
				                                   backgoundInteractionsNumber != 0 ? volumeMetric.get().getValue() / backgoundInteractionsNumber * TAConstants.percentage_100 : 0));
			}
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
		} finally {
			ThreadUtils.shutdownExecutionThreadPool(threadPool);
		}

		return categoryMetrics;
	}

	private void isValidCategory(Category newCategory) {
		if (StringUtils.isNullOrBlank(newCategory.getName().trim())) {
			throw new StoredSearchesException(StoredSearchesErrorCode.CategoryAddInvalidNameError).put("categoryName", newCategory.getName()).put("Problem", "Empty String");
		}

		int storedSearchNameMaxLength = configurationManager.getApplicationConfiguration().getStoredSearchNameMaxLength();
		if (newCategory.getName().length() > storedSearchNameMaxLength) {
			throw new StoredSearchesException(StoredSearchesErrorCode.CategoryAddInvalidNameError).put("categoryName", newCategory.getName())
			                                                                                      .put("Problem",
			                                                                                           "Name lenght is more then " + storedSearchNameMaxLength + " characters");
		}

	}

	private void isValidSavedSearch(SavedSearch newSavedSearch) {
		if (StringUtils.isNullOrBlank(newSavedSearch.getName().trim())) {
			throw new StoredSearchesException(StoredSearchesErrorCode.SavedSearchAddInvalidNameError).put("savedSearchName", newSavedSearch.getName());
		}

		int storedSearchNameMaxLength = configurationManager.getApplicationConfiguration().getStoredSearchNameMaxLength();
		if (newSavedSearch.getName().length() > storedSearchNameMaxLength) {
			throw new StoredSearchesException(StoredSearchesErrorCode.SavedSearchAddInvalidNameError).put("savedSearchName", newSavedSearch.getName())
			                                                                                         .put("Problem",
			                                                                                              "Name lenght is more then " + storedSearchNameMaxLength + " characters");
		}
	}

	/**
	 * Perform faceted Search.
	 *
	 * @param tenant        tenant
	 * @param channel       channel
	 * @param searchContext searchContext
	 * @return List<Facet> list of Facets
	 */
	public Facet getCategoriesFacetTree(String tenant, String channel, SearchInteractionsContext searchContext) {

		return this.getCategoriesFacet(tenant, channel, searchContext, null);
	}

	/**
	 * Perform faceted Search.
	 *
	 * @param tenant        tenant
	 * @param channel       channel
	 * @param searchContext searchContext
	 * @return List<Facet> list of Facets
	 */
	public Facet getCategoriesFacetWithMetrics(String tenant, String channel, SearchInteractionsContext searchContext) {
		return this.getCategoriesFacet(tenant, channel, searchContext, categoriesFacetMetrics);
	}

	/**
	 * Perform faceted Search.
	 *
	 * @param tenant            tenant
	 * @param channel           channel
	 * @param searchContext     searchContext
	 * @param backgroundContext backgroundContext
	 * @return List<Facet> list of Facets
	 */
	public Facet getCategoriesFacetWithMetrics(String tenant, String channel, SearchInteractionsContext searchContext, SearchInteractionsContext backgroundContext) {

		ExecutorService threadPool = null;
		Facet searchContextFacet = null, backgroundFacet = null;

		try {
			threadPool = Executors.newFixedThreadPool(2);

			String requestId = ThreadContext.get(TAConstants.requestId);

			val tasks = new ArrayList<Callable<Object>>();

			// search context
			tasks.add(() -> {
				ThreadContext.put(TAConstants.requestId, requestId);
				return new ImmutablePair<>("SearchContextFacet", this.getCategoriesFacetWithMetrics(tenant, channel, searchContext));
			});

			// background context
			tasks.add(() -> {
				ThreadContext.put(TAConstants.requestId, requestId);
				return new ImmutablePair<>("BackgroundContextFacet", this.getCategoriesFacetTree(tenant, channel, backgroundContext));
			});

			ApplicationConfiguration appConfig = configurationManager.getApplicationConfiguration();

			// invokeAll() returns when all tasks are complete
			List<Future<Object>> lstFutures = threadPool.invokeAll(tasks, appConfig.getDarwinRestRequestTimeout(), TimeUnit.SECONDS);

			Integer totalInteractions = 0;

			if (lstFutures != null) {
				for (Future<?> future : lstFutures) {
					if (future.isDone()) {

						// get tasks result of task is done
						Object taskResult = future.get();
						if (taskResult != null) {
							ImmutablePair<String, ?> resultPair = (ImmutablePair<String, ?>) taskResult;

							switch (resultPair.getLeft()) {
								case "SearchContextFacet":
									searchContextFacet = (Facet) resultPair.getRight();
									break;
								case "BackgroundContextFacet":
									backgroundFacet = (Facet) resultPair.getRight();
									break;
								default:
									break;
							}
						}
					}
				}
			}

			this.calculateCorrelatedFacet(searchContextFacet, backgroundFacet);

		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, StoredSearchesException.class);
			Throwables.propagate(new StoredSearchesException(ex, StoredSearchesErrorCode.RetrieveCategoriesFacetError));
		} finally {
			ThreadUtils.shutdownExecutionThreadPool(threadPool);
		}

		return searchContextFacet;
	}

	@SuppressWarnings("unchecked")
	private Facet getCategoriesFacet(String tenant, String channel, SearchInteractionsContext searchContext, List<FieldMetric> facetMetrics) {
		ExecutorService threadPool = null;
		List<Facet> facets = null;
		Facet categoriesFacet = new Facet();

		Map<String, Category> categoriesMap = null;

		try {
			logger.debug("Generating Categories facet  for tenant {}, channel {}", tenant, channel);

			String language = configurationService.getChannelLanguage(tenant, channel);

			threadPool = Executors.newFixedThreadPool(3);

			String requestId = ThreadContext.get(TAConstants.requestId);

			val tasks = new ArrayList<Callable<Object>>();

			// place request id into logger context
			tasks.add(() -> {
				ThreadContext.put(TAConstants.requestId, requestId);
				return new ImmutablePair<>("CategoriesFacet",
				                           facetsService.facetedSearch(tenant, channel, searchContext, this.categoriesFacetQuery, facetMetrics, Integer.valueOf(-1),
				                                                       true));
			});

			// retrieve number of interactions in current search result set
			tasks.add(() -> {
				ThreadContext.put(TAConstants.requestId, requestId);
				return new ImmutablePair<>("CurrentSearchInteractionsCount", textAnalyticsProvider.getResultSetInteractionsQuantity(tenant, channel, searchContext, language));
			});

			// place request id into logger context
			tasks.add(() -> {
				ThreadContext.put(TAConstants.requestId, requestId);
				return new ImmutablePair<>("Categories", this.getActiveCategoriesMap(tenant, channel));
			});

			ApplicationConfiguration appConfig = configurationManager.getApplicationConfiguration();

			// invokeAll() returns when all tasks are complete
			List<Future<Object>> lstFutures = threadPool.invokeAll(tasks, appConfig.getDarwinRestRequestTimeout(), TimeUnit.SECONDS);

			Integer totalInteractions = 0;

			if (lstFutures != null) {
				for (Future<?> future : lstFutures) {
					if (future.isDone()) {

						// get tasks result of task is done
						Object taskResult = future.get();
						if (taskResult != null) {
							ImmutablePair<String, ?> resultPair = (ImmutablePair<String, ?>) taskResult;

							switch (resultPair.getLeft()) {
								case "CategoriesFacet":
									facets = (List<Facet>) resultPair.getRight();
									break;
								case "Categories":
									categoriesMap = (Map<String, Category>) resultPair.getRight();
									break;
								case "CurrentSearchInteractionsCount":
									totalInteractions = (Integer) resultPair.getRight();
									break;
								default:
									break;
							}
						}
					}
				}
			}

			if (!CollectionUtils.isEmpty(facets)) {

				Integer total = totalInteractions;
				Map<String, Category> activeCategories = categoriesMap;

				// Merge with Category Names

				//@formatter:off
				categoriesFacet.setValues(facets.get(0).getValues().stream()
				                                // get only categories with names
												.filter(f -> activeCategories.containsKey(f.getTitle()))
						                        .map(f -> {
							                        String categoryId = f.getTitle();
							                        f.setTitle(activeCategories.get(categoryId).getName());
							                        f.setTitleKey(categoryId);

							                        // percentage
							                        if (f.getCount() > 0) {
								                        f.setPercentage((double) f.getCount() / total * TAConstants.percentage_100);
							                        }
							                        return f;
												})
						                        .collect(Collectors.toList()));
			}
			//@formatter:on
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, StoredSearchesException.class);
			Throwables.propagate(new StoredSearchesException(ex, StoredSearchesErrorCode.RetrieveCategoriesFacetError));
		} finally {
			ThreadUtils.shutdownExecutionThreadPool(threadPool);
		}

		return categoriesFacet;
	}

	/**
	 * Recalculate facet accordingly to background context.
	 *
	 * @param searchContextFacet     searchContextFacet
	 * @param backgroundContextFacet backgroundContextFacet
	 * @return Facet
	 */
	private void calculateCorrelatedFacet(Facet searchContextFacet, Facet backgroundContextFacet) {

		HashMap<String, FacetResultGroup> bgFacetHash = new HashMap<>();

		// Fill HashTable
		backgroundContextFacet.getValues().forEach(f -> {
			bgFacetHash.put(f.getTitle(), f);
		});

		// Calculate Percentage

		searchContextFacet.getValues().forEach(f -> {
			if (bgFacetHash.containsKey(f.getTitle())) {
				float a = (float) f.getCount();
				float b = (float) bgFacetHash.get(f.getTitle()).getCount();
				f.setCorrelationPercentage(a / b);
			} else {
				logger.debug("Correlation Percentage: the background value for " + f.getTitle() + "was not found");
			}

		});
	}

	private void mergeCategoriesReprocessingStatus(CategoriesRepository categoriesRepo, List<CategoryReprocessingState> categoriesReprocessingStatuses) {

		if (categoriesReprocessingStatuses != null) {
			// formatter:off

			// map category id to reprocessing state
			Map<String, CategoryReprocessingState> statuses = categoriesReprocessingStatuses.stream()
			                                                                                .collect(Collectors.toMap(st -> st.getCategoryId(), st -> st));

			// attach reprocessing state to category
			categoriesRepo.getCategories().stream()
			                              .filter(Objects::nonNull)
					                      .filter(c -> statuses.containsKey(String.valueOf(c.getId())))
					                      .forEach(c ->  {
						                      String categoryId = String.valueOf(c.getId());
						                      c.setReprocessingState(statuses.get(categoryId));
					                      });

			// @formatter:on
		}
	}

	/**
	 * Update category reprocessing allowed and should reprocess.
	 * @param categoriesRepo categories repo
	 */
	public void processCategoriesReprocessingStatus(CategoriesRepository categoriesRepo) {
		// formatter:off
		categoriesRepo.getCategories().stream()
								      .filter(Objects::nonNull)
				                      .forEach(c -> {
					                      // update indication whether category is allowed for reprocessing
					                      c.setIsReprocessingAllowed(categoryReprocessingService.isCategoryReprocessingAllowed(c));

					                      // update indication whether category should be reprocessed
					                      CategoryReprocessingState reprocessingState = c.getReprocessingState();
					                      if (reprocessingState != null) {
						                      CategoryReprocessingStatus status =  reprocessingState.getStatus();
						                      if (status != CategoryReprocessingStatus.Reprocessing) {
							                      Boolean neverReprocessed = reprocessingState.getLastReprocessedTime() == null;

							                      Boolean reprocessExpired = false;
							                      if (c.getLastChangeDateTimeGMT() != null) {
								                      reprocessExpired = reprocessingState.getLastReprocessedTime() != null && c.getLastChangeDateTimeGMT().isAfter(reprocessingState.getLastReprocessedTime());
							                      }

							                      Boolean errored = false;
							                      if (reprocessingState.getLastErrorTime() != null) {
								                      if ((reprocessingState.getLastReprocessedTime() == null)
								                           || (reprocessingState.getLastReprocessedTime() != null && reprocessingState.getLastErrorTime().isAfter(reprocessingState.getLastReprocessedTime()))) {
									                      errored = true;
								                      }

							                      }

							                      c.setShouldBeReprocessed(neverReprocessed || reprocessExpired || errored);
						                      }
					                      } else {
						                      // category was never reprocessed, so no reprocessing status exists
						                      c.setShouldBeReprocessed(true);
					                      }
				                      });
		// @formatter:on
	}
}