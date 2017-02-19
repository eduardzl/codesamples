package com.verint.textanalytics.dal.configService;

import com.codahale.metrics.Timer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Throwables;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.diagnostics.OperationType;
import com.verint.textanalytics.common.diagnostics.PerformanceMetrics;
import com.verint.textanalytics.common.exceptions.StoredSearchesErrorCode;
import com.verint.textanalytics.common.exceptions.StoredSearchesException;
import com.verint.textanalytics.common.utils.CollectionUtils;
import com.verint.textanalytics.common.utils.JSONUtils;
import com.verint.textanalytics.common.utils.StringUtils;
import com.verint.textanalytics.common.utils.UriUtils;
import com.verint.textanalytics.dal.darwin.RestRequestPathsAndQueryParams;
import com.verint.textanalytics.dal.rest.RestDataAccess;
import com.verint.textanalytics.model.storedSearch.*;
import lombok.Getter;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/***
 * @author yzanis
 */
public class ConfigurationServiceProvider {

	private static final String RESPONSE = "response";
	private static final String HTTP_CODE = "httpCode";
	public static final String DEFUAL_LANGUAGE = "en";

	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	private RestDataAccess restDataAccess;

	@Autowired
	private ConfigServiceRequestGenerator requestGenerator;

	@Autowired
	private ConfigServiceResponseConvertor responseConverter;

	@Autowired
	private AnalyticsTaggerRequestGenerator analyticsTaggerRequestGenerator;

	@Autowired
	private AnalyticsTaggerResponseGenerator analyticsTaggerResponseConverter;

	@Autowired
	private ConfigurationManager configurationManager;

	@Autowired
	private PerformanceMetrics performanceMetrics;

	private String storedSearchesRepositoryServiceBaseUrl;

	private String analyticsTaggerServiceBaseUrl;

	private final int numberOfLinesToLogOnCategoryReprocessing = 50;

	/***
	 *
	 */
	public void initialize() {
		logger.debug("Initializing ConfigurationServiceProvider");

		val applicationConfig = configurationManager.getApplicationConfiguration();

		this.storedSearchesRepositoryServiceBaseUrl = applicationConfig.getConfigServiceURL();

		this.analyticsTaggerServiceBaseUrl = UriUtils.getUrl(applicationConfig.getAnalyticsTaggerServiceURL(), applicationConfig.getCategoryReprocessingContextPrefixTaggerURL() + "/" + applicationConfig.getCategoryReprocessingContextURL());

		logger.debug("Stored Search provider was initialized with following base url , Config Server - {}, Analytics Tagger Base Url - {}",
		             !StringUtils.isNullOrBlank(this.storedSearchesRepositoryServiceBaseUrl) ? this.storedSearchesRepositoryServiceBaseUrl : "",
		             !StringUtils.isNullOrBlank(this.analyticsTaggerServiceBaseUrl) ? this.analyticsTaggerServiceBaseUrl : "");

		logger.debug("ConfigurationServiceProvider was initialized");
	}

	/***
	 * this will return the repository of categories.
	 *
	 * @param tenant  tenant
	 * @param channel channel
	 * @return categories repository and time stemp
	 */
	public CategoriesRepository getListOfCategories(String tenant, String channel) {

		CategoriesRepository repository = null;

		try {
			logger.info("Executing Retrive list of categories for tenant - {}, channel - {}", tenant, channel);

			Timer.Context timer = performanceMetrics.startTimedOperation(OperationType.GetCategoriesFile);

			RestRequestPathsAndQueryParams restRequestPathsAndQueryParams = requestGenerator.getGetCategoriesQuery(tenant, channel);

			Response response = this.restDataAccess.executeGetRequestFullResponse(this.storedSearchesRepositoryServiceBaseUrl, restRequestPathsAndQueryParams.getQueryPaths(),
			                                                                      restRequestPathsAndQueryParams.getQueryParams());

			switch (response.getStatus()) {

				case TAConstants.httpCode200:
					// 200 OK – Success
					String file = response.readEntity(String.class);
					if (!StringUtils.isNullOrBlank(file)) {
						repository = responseConverter.convertCategoriesResponse(file);
						repository.setTimeStamp(response.getHeaderString("Last-Modified"));

						logger.debug("{} Categories retrived from Repository for tenant - {}, channel - {}, time stamp - {}",
						             !CollectionUtils.isEmpty(repository.getCategories()) ? repository.getCategories().size() : "0", tenant, channel, repository.getTimeStamp());
					}
					break;
				case TAConstants.httpCode404:
					// 404 File not found - Channel does not exists
					repository = new CategoriesRepository();
					repository.setMaxId(0);
					break;
				case TAConstants.httpCode400:
					// 400 Bad Request – invalid URL detected
				case TAConstants.httpCode500:
					// 500 Server error - general error has occurred, more information will be included in the response body
				default:
					throw new StoredSearchesException(StoredSearchesErrorCode.CategoriesRetriveError).put(HTTP_CODE, response.getStatus()).put(RESPONSE, response);
			}

			performanceMetrics.stopTimedOperation(timer);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, StoredSearchesException.class);
			Throwables.propagate(new StoredSearchesException(ex, StoredSearchesErrorCode.CategoriesRetriveError));
		}
		return repository;
	}

	/***
	 * this will return the repository of categories.
	 *
	 * @param tenant  tenant
	 * @param channel channel
	 * @return SavedSearches Repository and time stemp
	 */
	public SavedSearchesRepository getListOfSavedSearches(String tenant, String channel) {

		SavedSearchesRepository repository = null;

		try {
			logger.info("Executing Retrive list of saved searches for tenant - {}, channel - {}", tenant, channel);

			Timer.Context timer = performanceMetrics.startTimedOperation(OperationType.GetSavedSearchesFile);

			RestRequestPathsAndQueryParams restRequestPathsAndQueryParams = requestGenerator.getGetSavedSearchesQuery(tenant, channel);

			Response response = this.restDataAccess.executeGetRequestFullResponse(this.storedSearchesRepositoryServiceBaseUrl, restRequestPathsAndQueryParams.getQueryPaths(),
			                                                                      restRequestPathsAndQueryParams.getQueryParams());

			switch (response.getStatus()) {

				case TAConstants.httpCode200:
					// 200 OK – Success
					String file = response.readEntity(String.class);
					if (!StringUtils.isNullOrBlank(file)) {
						repository = responseConverter.convertSavedSearchesResponse(file);
						repository.setTimeStamp(response.getHeaderString("Last-Modified"));

						logger.debug("{} Saved Searches retrived from Repository for tenant - {}, channel - {}, time stamp - {}",
						             !CollectionUtils.isEmpty(repository.getSavedSearches()) ? repository.getSavedSearches().size() : "0", tenant, channel, repository.getTimeStamp());
					}
					break;
				case TAConstants.httpCode404:
					// 404 File not found - Channel does not exists
					repository = new SavedSearchesRepository();
					repository.setSavedSearches(new ArrayList<SavedSearch>());
					repository.setMaxId(0);
					break;
				case TAConstants.httpCode400:
					// 400 Bad Request – invalid URL detected
				case TAConstants.httpCode500:
					// 500 Server error - general error has occurred, more information will be included in the response body
				default:
					throw new StoredSearchesException(StoredSearchesErrorCode.SavedSearchesRetriveError).put(HTTP_CODE, response.getStatus()).put(RESPONSE, response);
			}

			performanceMetrics.stopTimedOperation(timer);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, StoredSearchesException.class);
			Throwables.propagate(new StoredSearchesException(ex, StoredSearchesErrorCode.SavedSearchesRetriveError));
		}
		return repository;
	}

	/**
	 * Retrives list of Categories Reprocessing statuses.
	 *
	 * @param tenant  tenant
	 * @param channel channel
	 * @return list of reprocessing statuses
	 */
	public List<CategoryReprocessingState> getCategoriesReprocessingState(String tenant, String channel) {
		List<CategoryReprocessingState> reprocessingStates = null;

		try {
			logger.info("Retrieving list of Categories Reprocessing states for tenant - {}, channel - {}", tenant, channel);

			Timer.Context timer = performanceMetrics.startTimedOperation(OperationType.GetCategoriesReprocessingStates);

			RestRequestPathsAndQueryParams restRequestPathsAndQueryParams = requestGenerator.getCategoriesReprocessingStatesQuery(tenant, channel);

			Response response = this.restDataAccess.executeGetRequestFullResponse(this.storedSearchesRepositoryServiceBaseUrl, restRequestPathsAndQueryParams.getQueryPaths(),
			                                                                      restRequestPathsAndQueryParams.getQueryParams());

			switch (response.getStatus()) {

				case TAConstants.httpCode200:
					// 200 OK – Success
					String reprocessingStateJson = response.readEntity(String.class);
					if (!StringUtils.isNullOrBlank(reprocessingStateJson)) {
						reprocessingStates = responseConverter.convertToCategoriesRerocessingStates(reprocessingStateJson);

						logger.debug("{} Categories Reprocessing Statues retrived from Repository for tenant - {}, channel - {}",
						             !CollectionUtils.isEmpty(reprocessingStates) ? reprocessingStates.size() : "0", tenant, channel);
					}

					break;
				case TAConstants.httpCode404:
					// 404 File not found - Channel does not exists
					break;
				case TAConstants.httpCode400:
					// 400 Bad Request – invalid URL detected
				case TAConstants.httpCode500:
					// 500 Server error - general error has occurred, more information will be included in the response body
				default:
					throw new StoredSearchesException(StoredSearchesErrorCode.CategoriesReprocessingStatusRetrieveError).put(HTTP_CODE, response.getStatus()).put(RESPONSE, response);
			}
			performanceMetrics.stopTimedOperation(timer);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, StoredSearchesException.class);
			Throwables.propagate(new StoredSearchesException(ex, StoredSearchesErrorCode.CategoriesReprocessingStatusRetrieveError));
		}

		return reprocessingStates;
	}

	/***
	 * @param tenant      tenant
	 * @param channel     channel
	 * @param updatedRepo the categories repository with all updates
	 * @param timeStemp   the time step of the file
	 */
	public void updateCategoryFile(String tenant, String channel, CategoriesRepository updatedRepo, String timeStemp) {
		try {
			logger.info("Executing Update list of categories for tenant - {}, channel - {}, time stamp - {}", tenant, channel, timeStemp);

			Timer.Context timer = performanceMetrics.startTimedOperation(OperationType.WriteCategoriesFile);

			RestRequestPathsAndQueryParams restRequestPathsAndQueryParams = requestGenerator.getWriteCategoriesQuery(tenant, channel, timeStemp);

			String repoFile = responseConverter.convertCategoriesToJSONString(updatedRepo);

			Response response = this.restDataAccess.executePutRequestFullResponse("", this.storedSearchesRepositoryServiceBaseUrl, restRequestPathsAndQueryParams.getQueryPaths(),
			                                                                      null, repoFile, null, restRequestPathsAndQueryParams.getQueryParams());
			switch (response.getStatus()) {

				case TAConstants.httpCode200:
					// 200 OK – Success
					break;
				case TAConstants.httpCode409:
					// 409 - Conflict - model is not updated
				case TAConstants.httpCode400:
					// 400 Bad Request – invalid URL detected
				case TAConstants.httpCode500:
					// 500 Server error - general error has occurred, more information will be included in the response body
				default:
					throw new StoredSearchesException(StoredSearchesErrorCode.CategoryUpdateError).put(HTTP_CODE, response.getStatus()).put(RESPONSE, response);
			}

			performanceMetrics.stopTimedOperation(timer);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, StoredSearchesException.class);
			Throwables.propagate(new StoredSearchesException(ex, StoredSearchesErrorCode.CategoryUpdateError));
		}
	}

	/***
	 * @param tenant      tenant
	 * @param channel     channel
	 * @param updatedRepo the saved searches repository with all updates
	 * @param timeStemp   the time step of the file
	 */
	public void updateSavedSearchesFile(String tenant, String channel, SavedSearchesRepository updatedRepo, String timeStemp) {
		try {
			logger.info("Executing Update list of categories for tenant - {}, channel - {}, time stamp - {}", tenant, channel, timeStemp);

			Timer.Context timer = performanceMetrics.startTimedOperation(OperationType.WriteSavedSearchesFile);

			RestRequestPathsAndQueryParams restRequestPathsAndQueryParams = requestGenerator.getWriteSavedSearchesQuery(tenant, channel, timeStemp);

			String repoFile = responseConverter.convertSavedSearchesToJSONString(updatedRepo);

			Response response = this.restDataAccess.executePutRequestFullResponse("", this.storedSearchesRepositoryServiceBaseUrl, restRequestPathsAndQueryParams.getQueryPaths(),
			                                                                      null, repoFile, null, restRequestPathsAndQueryParams.getQueryParams());

			switch (response.getStatus()) {

				case TAConstants.httpCode200:
					// 200 OK – Success
					break;
				case TAConstants.httpCode409:
					// 409 - Conflict - model is not updated
				case TAConstants.httpCode400:
					// 400 Bad Request – invalid URL detected
				case TAConstants.httpCode500:
					// 500 Server error - general error has occurred, more information will be included in the response body
				default:
					throw new StoredSearchesException(StoredSearchesErrorCode.SavedSearchUpdateError).put(HTTP_CODE, response.getStatus()).put(RESPONSE, response);
			}

			performanceMetrics.stopTimedOperation(timer);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, StoredSearchesException.class);
			Throwables.propagate(new StoredSearchesException(ex, StoredSearchesErrorCode.SavedSearchUpdateError));
		}
	}

	/**
	 * Invokes request for Category reprocessing.
	 *
	 * @param tenant        tenant
	 * @param channel       channel
	 * @param categoryId    category id
	 * @param reprocessType category reprocessing type
	 */
	public void invokeCategoryReprocessing(String tenant, String channel, int categoryId, CategoryReprocessingType reprocessType) {
		long startTimeMillisecs, endTimeMillisecs;

		try {
			logger.info("Executing Invocation of Category Reprocessing request for category id {} for tenant - {}, channel - {}", categoryId, tenant, channel);

			InvocationCallback<Response> invocationCallback = new InvocationCallback<Response>() {
				@Override
				public void completed(Response response) {

					try {
						// try to read response
						String jsonResponse = response.readEntity(String.class);

						if (!StringUtils.isNullOrBlank(jsonResponse)) {
							logger.trace("Response accepted from Category Reprocessing call {}", StringUtils.topNLines(jsonResponse, numberOfLinesToLogOnCategoryReprocessing));
						} else {
							logger.trace("Empty response accepted from invokeCategoryReprocessing call");
						}

						int responseStatus = response.getStatus();
						if (responseStatus != TAConstants.httpCode200) {
							logger.info("HTTP status {} accepted on invokeCategoryReprocessing request", responseStatus);
						} else {
							analyticsTaggerResponseConverter.convertInvokeCategoryReprocessing(jsonResponse);
						}
					} catch (Exception ex) {
						logger.error("Failure in invokeCategoryReprocessing InvocationCallback. Error {}", ex);
					}
				}

				@Override
				public void failed(Throwable throwable) {
					try {
						logger.error("invokeCategoryReprocessing request failed. Error - {}", throwable);
					} catch (Exception ex) {
						logger.error("Failure in processing invokeCategoryReprocessing error. Error - {}", ex);
					}
				}
			};

			String reprocessingBody = JSONUtils.getObjectJSON(new CategoryReprocessingRequest(tenant, channel, String.valueOf(categoryId), reprocessType.toString()));

			// invoke asyncroneous request
			Future<Response> invokeReprocessingResponse = this.restDataAccess.executePostRequestAsync(StoredSearchQueryType.ReprocessCategory.toString(),
			                                                                                          this.analyticsTaggerServiceBaseUrl, null, null, null, reprocessingBody, null,
			                                                                                          invocationCallback);

		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, StoredSearchesException.class);
			Throwables.propagate(new StoredSearchesException(ex, StoredSearchesErrorCode.CategoryReprocessingInvocationError));
		}
	}

	/***
	 * @param tenant  tenant
	 * @param channel channel
	 * @return the language from prop file docoment
	 */
	public String getChannelLanguage(String tenant, String channel) {
		String language = DEFUAL_LANGUAGE;
		try {
			logger.info("Executing Retrive Channel prop file for tenant - {}, channel - {}", tenant, channel);

			Timer.Context timer = performanceMetrics.startTimedOperation(OperationType.GetChannelPropFile);

			RestRequestPathsAndQueryParams restRequestPathsAndQueryParams = requestGenerator.getGetChannelConfigurationQuery(tenant, channel);

			Response response = this.restDataAccess.executeGetRequestFullResponse(this.storedSearchesRepositoryServiceBaseUrl, restRequestPathsAndQueryParams.getQueryPaths(),
			                                                                      restRequestPathsAndQueryParams.getQueryParams());

			switch (response.getStatus()) {

				case TAConstants.httpCode200:
					// 200 OK – Success
					String file = response.readEntity(String.class);
					if (!StringUtils.isNullOrBlank(file)) {
						val channelPropFile = responseConverter.convertChannelPropFileResponse(file);
						language = channelPropFile.get(configurationManager.getApplicationConfiguration().getLanguageProperty());
						if (language == null) {
							language = DEFUAL_LANGUAGE;
						}

					}
					break;
				case TAConstants.httpCode404:
					// 404 File not found - Channel does not exists, set Language to be en as defualt
				case TAConstants.httpCode400:
					// 400 Bad Request – invalid URL detected
				case TAConstants.httpCode500:
					// 500 Server error - general error has occurred, more information will be included in the response body
				default:
					language = DEFUAL_LANGUAGE;
			}

			performanceMetrics.stopTimedOperation(timer);
		} catch (Exception ex) {
			language = DEFUAL_LANGUAGE;
		}
		return language;
	}

	/**
	 * @author EZlotnik
	 */
	enum StoredSearchQueryType {
		// @formatter:off
		ReprocessCategory("Reprocess Category");
		// @formatter:on

		private String requestType;

		/**
		 * Constructor.
		 *
		 * @param requestType
		 */
		StoredSearchQueryType(String requestType) {
			this.requestType = requestType;
		}
	}

	/**
	 * Params for Category Reprocessing requests.
	 */
	public class CategoryReprocessingRequest {
		@Getter
		@JsonProperty("Tenant")
		private String tenant;

		@Getter
		@JsonProperty("channel")
		private String channel;

		@Getter
		@JsonProperty("category")
		private String category;

		@Getter
		@JsonProperty("action")
		private String action;

		/**
		 * Constructor.
		 *
		 * @param tenant   tenant
		 * @param channel  channel
		 * @param category category
		 * @param action   action
		 */
		public CategoryReprocessingRequest(String tenant, String channel, String category, String action) {
			this.tenant = tenant;
			this.channel = channel;
			this.category = category;
			this.action = action;
		}
	}
}