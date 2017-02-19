package com.verint.textanalytics.dal.configService;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.verint.textanalytics.common.exceptions.StoredSearchesErrorCode;
import com.verint.textanalytics.common.exceptions.StoredSearchesException;
import com.verint.textanalytics.common.utils.DataUtils;
import com.verint.textanalytics.common.utils.ExceptionUtils;
import com.verint.textanalytics.common.utils.JSONUtils;
import com.verint.textanalytics.common.utils.StringUtils;
import com.verint.textanalytics.model.facets.SpeakerQueryType;
import com.verint.textanalytics.model.interactions.*;
import com.verint.textanalytics.model.storedSearch.*;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

/***
 * @author imor
 */
public class ConfigServiceResponseConvertor {
	private Logger logger = LogManager.getLogger(this.getClass());

	/**
	 * @param response The response that represent the stored searches
	 * @return SavedSearchesRepository
	 */
	public CategoriesRepository convertCategoriesResponse(String response) {
		val categoriesRepository = new CategoriesRepository();

		try {

			if (!StringUtils.isNullOrBlank(response)) {
				logger.trace("convertCategoriesResponse - FROM - {}", () -> response);

				ObjectMapper mapper = new ObjectMapper();
				JsonNode rootNode = mapper.readTree(response);

				// maxId is mandatory
				int maxId = rootNode.path(ResponseKey.MAX_ID).asInt();
				categoriesRepository.setMaxId(maxId);

				JsonNode categoriesArray = rootNode.path(ResponseKey.CATEGORIES);
				if (!categoriesArray.isMissingNode() && categoriesArray.isArray()) {
					Category category = null;
					boolean fail = false;

					for (JsonNode categoryNode : categoriesArray) {
						category = new Category();
						fail = false;

						try {
							// ID, query, isActive are mandatory
							category.setId(categoryNode.path(ResponseKey.id).asInt());
							category.setName(categoryNode.path(ResponseKey.NAME).asText(ResponseKey.DEFAULT_VALUE));
							category.setQuery(categoryNode.path(ResponseKey.QUERY).asText());
							category.setActive(categoryNode.path(ResponseKey.IS_ACTIVE).asBoolean());

							category.setLastChangeDateTimeGMT(
									DataUtils.getDateFromISO8601StringWithMilliseconds(categoryNode.path(ResponseKey.LAST_MODIFIED_DATE_TIME).asText(ResponseKey.DEFAULT_VALUE)));

							updateProperties(categoryNode, category);

						} catch (Exception ex) {
							logger.error("Failed to generate category from JSON for {}. Error - {}", categoryNode, ex);

							if (categoriesRepository.getCanNotParseCategories() == null) {
								categoriesRepository.setCanNotParseCategories(new ArrayList<String>());
							}
							categoriesRepository.getCanNotParseCategories()
							                    .add(String.format("{Id:%s, Name:%s, ExceptionStackTrace:%s", category.getId(), category.getName(),
							                                       ExceptionUtils.getStackTrace(ex)));
							fail = true;
						}

						if (!fail) {
							categoriesRepository.addCategory(category);
						}
					}
				}
			}

		} catch (Exception ex) {
			throw new StoredSearchesException(ex, StoredSearchesErrorCode.CategoriesParsingError).put("Response", response);
		}

		logger.trace("convertCategoriesResponse - TO - {}", () -> JSONUtils.getObjectJSON(categoriesRepository));

		return categoriesRepository;
	}

	/**
	 * Converts json to Saved Searches.
	 *
	 * @param response The response that represent the stored searches
	 * @return SavedSearchesRepository
	 */
	public SavedSearchesRepository convertSavedSearchesResponse(String response) {
		logger.debug("converSavedSearchesResponse - FROM - {}", () -> response);

		val res = new SavedSearchesRepository();
		res.setSavedSearches(new ArrayList<SavedSearch>());

		try {

			if (!StringUtils.isNullOrBlank(response)) {
				ObjectMapper mapper = new ObjectMapper();
				mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
				JsonNode rootNode = mapper.readTree(response);

				int maxId = rootNode.path(ResponseKey.MAX_ID).asInt();
				res.setMaxId(maxId);

				JsonNode savedSearchesNode = rootNode.path(ResponseKey.SAVED_SEARCHES);
				if (!savedSearchesNode.isMissingNode()) {
					// iterate over savedSearchesJson

					SavedSearch savedSearch = null;
					List<SavedSearch> savedSearches = new ArrayList<SavedSearch>();
					boolean fail = false;

					for (JsonNode savedSearchNode : savedSearchesNode) {
						savedSearch = new SavedSearch();

						fail = false;

						try {
							// Id and Query are mandatory, if missing JSONException exception will be thrown
							savedSearch.setId(savedSearchNode.path(ResponseKey.id).asInt());
							savedSearch.setName(savedSearchNode.path(ResponseKey.NAME).asText(ResponseKey.DEFAULT_VALUE));
							savedSearch.setQuery(savedSearchNode.path(ResponseKey.QUERY).asText());
							savedSearch.setLastChangeDateTimeGMT(
									DataUtils.getDateFromISO8601StringWithMilliseconds(savedSearchNode.path(ResponseKey.LAST_MODIFIED_DATE_TIME).asText(ResponseKey.DEFAULT_VALUE)));

							updateProperties(savedSearchNode, savedSearch);
						} catch (Exception ex) {
							logger.error("Failed to generate savedSearch from JSON for {}. Error - {}", rootNode, ex);

							if (res.getCanNotParseSavedSearches() == null) {
								res.setCanNotParseSavedSearches(new ArrayList<String>());
							}
							res.getCanNotParseSavedSearches()
							   .add(String.format("{Id:%s, Name:%s, ExceptionStackTrace:%s", savedSearch.getId(), savedSearch.getName(), ExceptionUtils.getStackTrace(ex)));
							fail = true;
						}

						if (!fail) {
							savedSearches.add(savedSearch);
						}
					}

					res.setSavedSearches(savedSearches);
				}
			}

		} catch (Exception ex) {
			throw new StoredSearchesException(ex, StoredSearchesErrorCode.CategoriesParsingError).put("Response", response);
		}

		logger.debug("converSavedSearchesResponse - TO - {}", () -> JSONUtils.getObjectJSON(res));

		return res;
	}

	/**
	 * @param categoriesRepository CategoriesRepository
	 * @return String
	 */
	public String convertCategoriesToJSONString(CategoriesRepository categoriesRepository) {

		logger.trace("converCategoriesToJSONString - FROM - {}", () -> JSONUtils.getObjectJSON(categoriesRepository));

		JSONObject jObject = new JSONObject();

		jObject.put(ResponseKey.MAX_ID, categoriesRepository.getMaxId());

		JSONArray categoriesJSONArray = new JSONArray();
		JSONObject categoryJSONObject;
		JSONObject propertiesJSONObject;
		JSONObject searchModelJSONObject;

		val categories = categoriesRepository.getCategories();
		for (Category vtaCategory : categories) {
			categoryJSONObject = new JSONObject();
			categoryJSONObject.put(ResponseKey.id, vtaCategory.getId());
			categoryJSONObject.put(ResponseKey.NAME, vtaCategory.getName());
			categoryJSONObject.put(ResponseKey.IS_ACTIVE, vtaCategory.isActive());
			categoryJSONObject.put(ResponseKey.QUERY, vtaCategory.getQuery());
			categoryJSONObject.put(ResponseKey.DEBUG_QUERY, vtaCategory.getDebugQuery());
			categoryJSONObject.put(ResponseKey.LAST_MODIFIED_DATE_TIME, DataUtils.getISO8601StringFromDate(vtaCategory.getLastChangeDateTimeGMT()));

			propertiesJSONObject = new JSONObject();
			propertiesJSONObject.put(ResponseKey.DESCRIPTION, vtaCategory.getDescription());
			propertiesJSONObject.put(ResponseKey.LAST_MODIFIED_BY_USER_ID, vtaCategory.getLastModifiedByUserId());
			propertiesJSONObject.put(ResponseKey.IS_PUBLISHED, vtaCategory.isPublished());
			propertiesJSONObject.put(ResponseKey.COLOR, vtaCategory.getColor());
			propertiesJSONObject.put(ResponseKey.IMPACT, vtaCategory.getImpact());

			searchModelJSONObject = new JSONObject();
			searchModelJSONObject.put(ResponseKey.VERSION, vtaCategory.getSearchContextVersion());
			searchModelJSONObject.put(ResponseKey.MODEL, new JSONObject(JSONUtils.getObjectJSON(vtaCategory.getSearchContext())));

			propertiesJSONObject.put(ResponseKey.SEARCH_MODEL, searchModelJSONObject);
			categoryJSONObject.put(ResponseKey.PROPERTIES, propertiesJSONObject);

			categoriesJSONArray.put(categoryJSONObject);
		}

		jObject.put(ResponseKey.CATEGORIES, categoriesJSONArray);

		logger.trace("converCategoriesToJSONString TO - {}", () -> JSONUtils.getObjectJSON(jObject));

		return jObject.toString(4);
	}

	/**
	 * Converts json to Saved Searches.
	 *
	 * @param response The response that represent the stored searches
	 * @return SavedSearchesRepository
	 */
	public List<CategoryReprocessingState> convertToCategoriesRerocessingStates(String response) {
		List<CategoryReprocessingState> reprocessingStates = new ArrayList<>();

		logger.debug("Converting Categories Reprocessing State from - {}", () -> response);

		try {
			if (!StringUtils.isNullOrBlank(response)) {

				ObjectMapper mapper = new ObjectMapper();
				mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
				JsonNode rootNode = mapper.readTree(response);

				JsonNode categoriesNode = rootNode.path(ResponseKey.categories);
				if (!categoriesNode.isMissingNode() && categoriesNode.isArray()) {
					for (JsonNode categoryNode : categoriesNode) {
						CategoryReprocessingState state = new CategoryReprocessingState();

						// @formatter:off
						state.setCategoryId(categoryNode.path(ResponseKey.id).asText(""))
							  .setLastAction(CategoryReprocessingAction.fromString(categoryNode.path(ResponseKey.lastAction).asText()))
						      .setLastReprocessedTime(DataUtils.getDateFromISO8601StringWithMillisecondsSafe(categoryNode.path(ResponseKey.lastReprocessedTime).asText()))
						      .setReprocessStartTime(DataUtils.getDateFromISO8601StringWithMillisecondsSafe(categoryNode.path(ResponseKey.reprocessStartTime).asText()))
							  .setLastErrorTime(DataUtils.getDateFromISO8601StringWithMillisecondsSafe(categoryNode.path(ResponseKey.lastErrorTime).asText()))
							  .setLastErrorMessage(categoryNode.path(ResponseKey.lastErrorMessage).asText())
							  .setStatus(CategoryReprocessingStatus.fromString(categoryNode.path(ResponseKey.reprocessingStatus).asText()));
						// @formatter:on

						reprocessingStates.add(state);
					}
				}
			}

		} catch (Exception ex) {
			throw new StoredSearchesException(ex, StoredSearchesErrorCode.CategoriesReprocessingStatusParsingError).put("Response", response);
		}

		return reprocessingStates;
	}

	/***
	 * @param response content of channel.prop file
	 * @return Prop file as a Map
	 */
	public Map<String, String> convertChannelPropFileResponse(String response) {
		HashMap<String, String> retValue = new HashMap<>();
		String key, value;

		if (!StringUtils.isNullOrBlank(response)) {
			final Properties prop = new Properties();
			try {
				prop.load(new StringReader(response));

				Enumeration<?> e = prop.propertyNames();
				while (e.hasMoreElements()) {
					key = (String) e.nextElement();
					value = prop.getProperty(key);
					retValue.put(key.toLowerCase(), value);
				}

			} catch (IOException e) {
				//Hnadel errors
				//TODO:yuliya
			}
		}

		return retValue;
	}

	/**
	 * @param savedSearchesRepository SavedSearchesRepository
	 * @return String
	 */
	public String convertSavedSearchesToJSONString(SavedSearchesRepository savedSearchesRepository) {

		logger.trace("converSavedSearchesToJSONString - FROM - {}", () -> JSONUtils.getObjectJSON(savedSearchesRepository));

		JSONObject jObject = new JSONObject();

		jObject.put(ResponseKey.MAX_ID, savedSearchesRepository.getMaxId());

		JSONArray savedSearchesJSONArray = new JSONArray();
		JSONObject savedSearchJSONObject;
		JSONObject propertiesJSONObject;
		JSONObject searchModelJSONObject;

		val savedSearches = savedSearchesRepository.getSavedSearches();
		for (SavedSearch savedSearch : savedSearches) {
			savedSearchJSONObject = new JSONObject();
			savedSearchJSONObject.put(ResponseKey.id, savedSearch.getId());
			savedSearchJSONObject.put(ResponseKey.NAME, savedSearch.getName());
			savedSearchJSONObject.put(ResponseKey.QUERY, savedSearch.getQuery());
			savedSearchJSONObject.put(ResponseKey.DEBUG_QUERY, savedSearch.getDebugQuery());
			savedSearchJSONObject.put(ResponseKey.LAST_MODIFIED_DATE_TIME, DataUtils.getISO8601StringFromDate(savedSearch.getLastChangeDateTimeGMT()));

			propertiesJSONObject = new JSONObject();
			propertiesJSONObject.put(ResponseKey.DESCRIPTION, savedSearch.getDescription());
			propertiesJSONObject.put(ResponseKey.LAST_MODIFIED_BY_USER_ID, savedSearch.getLastModifiedByUserId());
			propertiesJSONObject.put(ResponseKey.IS_PUBLIC, savedSearch.isPublic());

			searchModelJSONObject = new JSONObject();
			searchModelJSONObject.put(ResponseKey.VERSION, savedSearch.getSearchContextVersion());
			searchModelJSONObject.put(ResponseKey.MODEL, new JSONObject(JSONUtils.getObjectJSON(savedSearch.getSearchContext())));

			propertiesJSONObject.put(ResponseKey.SEARCH_MODEL, searchModelJSONObject);
			savedSearchJSONObject.put(ResponseKey.PROPERTIES, propertiesJSONObject);

			savedSearchesJSONArray.put(savedSearchJSONObject);
		}

		jObject.put(ResponseKey.SAVED_SEARCHES, savedSearchesJSONArray);

		logger.trace("converSavedSearchesToJSONString - TO - {}", () -> JSONUtils.getObjectJSON(jObject));

		return jObject.toString(4);
	}

	private void updateProperties(JsonNode categoryNode, Category category) {
		String lastReprocessingInvokedDateTimeGMT = "";

		// Properties
		JsonNode categoryPropertiesNode = categoryNode.path(ResponseKey.PROPERTIES);

		if (!categoryPropertiesNode.isMissingNode()) {
			category.setDescription(categoryPropertiesNode.path(ResponseKey.DESCRIPTION).asText(ResponseKey.DEFAULT_VALUE));
			category.setLastModifiedByUserId(categoryPropertiesNode.path(ResponseKey.LAST_MODIFIED_BY_USER_ID).asInt(-1));

			// Pusblished in Manadatory
			category.setPublished(categoryPropertiesNode.path(ResponseKey.IS_PUBLISHED).asBoolean());
			category.setColor(categoryPropertiesNode.path(ResponseKey.COLOR).asInt(-1));
			category.setImpact(categoryPropertiesNode.path(ResponseKey.IMPACT).asInt(-1));

			// Search Model
			JsonNode categorySearchModelNode = categoryPropertiesNode.path(ResponseKey.SEARCH_MODEL);
			if (!categorySearchModelNode.isMissingNode()) {

				category.setSearchContextVersion(categorySearchModelNode.path(ResponseKey.VERSION).asText(ResponseKey.DEFAULT_VALUE));
				// Model								
				JsonNode categoryModelJson = categorySearchModelNode.path(ResponseKey.MODEL);
				category.setSearchContext(new SearchInteractionsContext());
				if (!categoryModelJson.isMissingNode()) {
					updateTerms(category.getSearchContext(), categoryModelJson);
					updateFilterFields(category.getSearchContext(), categoryModelJson);
					updateRangeFilterFields(category.getSearchContext(), categoryModelJson);
				}
			}
		}
	}

	private void updateProperties(JsonNode savedSearchJson, SavedSearch savedSearch) {
		JSONObject savedSearchSearchModelJson;
		JSONObject savedSearchModelJson;

		// Properties
		JsonNode savedSearchProperties = savedSearchJson.path(ResponseKey.PROPERTIES);
		if (!savedSearchProperties.isMissingNode()) {

			savedSearch.setDescription(savedSearchProperties.path(ResponseKey.DESCRIPTION).asText(ResponseKey.DEFAULT_VALUE));
			savedSearch.setLastModifiedByUserId(savedSearchProperties.path(ResponseKey.LAST_MODIFIED_BY_USER_ID).asInt(-1));

			// Search Model
			JsonNode savedSearchSearchModel = savedSearchProperties.path(ResponseKey.SEARCH_MODEL);
			if (!savedSearchSearchModel.isMissingNode()) {

				savedSearch.setSearchContextVersion(savedSearchSearchModel.path(ResponseKey.VERSION).asText(ResponseKey.DEFAULT_VALUE));

				// Model			
				JsonNode savedSearchModel = savedSearchSearchModel.path(ResponseKey.MODEL);
				savedSearch.setSearchContext(new SearchInteractionsContext());
				if (!savedSearchModel.isMissingNode()) {
					updateTerms(savedSearch.getSearchContext(), savedSearchModel);
					updateFilterFields(savedSearch.getSearchContext(), savedSearchModel);
					updateRangeFilterFields(savedSearch.getSearchContext(), savedSearchModel);
				}
			}
		}
	}

	private void updateTerms(SearchInteractionsContext searchContext, JsonNode modelJson) {
		List<String> terms;

		// Terms
		terms = new ArrayList<String>();
		for (JsonNode termNode : modelJson.path(ResponseKey.TERMS)) {
			terms.add(termNode.asText());
		}

		searchContext.setTerms(terms);
	}

	private void updateFilterFields(SearchInteractionsContext searchContext, JsonNode modelJson) {
		// Filter Fields node is mandatory
		JsonNode filterFieldsNode = modelJson.path(ResponseKey.FILTER_FIELDS);

		List<FilterField> filterFields = new ArrayList<FilterField>();

		for (JsonNode filterFieldNode : filterFieldsNode) {
			FilterField filterField = new FilterField();

			// Name, Group Tag and Speaker are mandatory
			filterField.setName(filterFieldNode.path(ResponseKey.NAME).asText());
			filterField.setGroupTag(filterFieldNode.path(ResponseKey.GROUP_TAG).asText());
			filterField.setSpeaker(SpeakerQueryType.valueOf(filterFieldNode.path(ResponseKey.SPEAKER).asText()));

			JsonNode valuesJson = filterFieldNode.path(ResponseKey.VALUES);
			if (valuesJson.isArray()) {
				String value, valueTitleKey;

				ArrayNode valuesArrNode = (ArrayNode) valuesJson;
				FilterFieldValue[] values = new FilterFieldValue[valuesArrNode.size()];

				int k = 0;
				for (JsonNode valueNode : valuesArrNode) {
					// Value and Value Title Key are mandatory
					value = valueNode.path(ResponseKey.VALUE).asText();
					valueTitleKey = valueNode.path(ResponseKey.VALUE_TITLE_KEY).asText();
					values[k] = new FilterFieldValue(value, valueTitleKey);
					k++;
				}

				filterField.setValues(values);
			}

			filterFields.add(filterField);
		}

		searchContext.setFilterFields(filterFields);
	}

	private void updateRangeFilterFields(SearchInteractionsContext searchContext, JsonNode modelJson) {
		Range range;

		// Range Filter Fields									
		JsonNode rangeFilterFieldsNode = modelJson.path(ResponseKey.RANGE_FILTER_FIELDS);

		List<RangeFilterField> rangeFilterFields = new ArrayList<RangeFilterField>();
		for (JsonNode rangeFilterFieldNode : rangeFilterFieldsNode) {

			RangeFilterField rangeFilterField = new RangeFilterField();
			rangeFilterField.setName(rangeFilterFieldNode.path(ResponseKey.NAME).asText());
			JsonNode rangesJson = rangeFilterFieldNode.path(ResponseKey.RANGES);

			if (rangesJson.isArray()) {
				ArrayNode rangesArrayJson = (ArrayNode) rangesJson;

				List<Range> ranges = new ArrayList<Range>();

				for (JsonNode rangeNode : rangesArrayJson) {
					range = new Range();
					range.setLowerValue(rangeNode.path(ResponseKey.LOWER_VALUE).asText());
					range.setIsLowerInclusive(Boolean.toString(rangeNode.path(ResponseKey.IS_LOWER_INCLUSIVE).asBoolean()));
					range.setUpperValue(rangeNode.path(ResponseKey.UPPER_VALUE).asText());
					range.setIsUpperInclusive(Boolean.toString(rangeNode.path(ResponseKey.IS_UPPER_INCLUSIVE).asBoolean()));
					range.setKey(rangeNode.path(ResponseKey.KEY).asText());
					range.setTitleKey(rangeNode.path(ResponseKey.TITLE_KEY).asText());
					ranges.add(range);
				}

				rangeFilterField.setRanges(ranges);
			}

			rangeFilterFields.add(rangeFilterField);
		}

		searchContext.setRangeFilterFields(rangeFilterFields);
	}
}