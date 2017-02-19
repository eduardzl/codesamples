package com.verint.textanalytics.bl.applicationservices;

import com.verint.textanalytics.common.exceptions.StoredSearchesErrorCode;
import com.verint.textanalytics.common.exceptions.StoredSearchesException;
import com.verint.textanalytics.common.utils.CollectionUtils;
import com.verint.textanalytics.model.storedSearch.Category;
import com.verint.textanalytics.model.storedSearch.SavedSearch;
import com.verint.textanalytics.model.storedSearch.StoredSearch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Optional;

/**
 * Created by EZlotnik on 12/8/2015.
 */
public class StoredSearchesMerger {
	private final static String CATEGORY_NAME_ERROR_KEY = "categoryName";
	private final static String SAVED_SEARCH_NAME_ERROR_KEY = "savedSearchName";
	private final static String CATEGORY_ID_ERROR_KEY = "categoryId";
	private final static String SAVED_SEARCH_ID_ERROR_KEY = "savedSearchId";

	private Logger logger = LogManager.getLogger(this.getClass());


	/**
	 * Adds new stored search to list of existing stored searches.
	 * @param storedSearches list of stored searches
	 * @param newStoredSearch stored search to add
	 * @param <T> type of stored search
	 * @return list of searches after adding
	 */
	public <T extends StoredSearch> List<T> addNewStoredSearch(List<T> storedSearches, T newStoredSearch) {

		if (storedNameExistsInRepository(storedSearches, newStoredSearch.getName(), -1)) {

			if (newStoredSearch instanceof Category) {
				throw new StoredSearchesException(StoredSearchesErrorCode.CategoryAddNameAllreadyExistsError).put(CATEGORY_NAME_ERROR_KEY, newStoredSearch.getName());
			} else if (newStoredSearch instanceof  SavedSearch) {
				throw new StoredSearchesException(StoredSearchesErrorCode.SavedSearchAddNameAllreadyExistsError).put(SAVED_SEARCH_NAME_ERROR_KEY, newStoredSearch.getName());
			}
		}

		storedSearches.add(newStoredSearch);

		return storedSearches;
	}

	/**
	 * Udates existing stored search.
	 * @param storedSearches list of stored searches in repository
	 * @param storedSearchToUpdate stored search to update
	 * @param <T> type of stored search
	 * @param dateTimeNow date time to use on as category last updated time
	 * @return modified list of stored searches
	 */
	public <T extends StoredSearch> List<T> updateStoredSearch(List<T> storedSearches, T storedSearchToUpdate, DateTime dateTimeNow) {

		if (!CollectionUtils.isEmpty(storedSearches)) {

			boolean isCategory = storedSearchToUpdate instanceof Category;
			boolean isSavedSearch = storedSearchToUpdate instanceof SavedSearch;

			// search for stored search
			Optional<T> storedSearchFound = storedSearches.stream().filter(s -> s.getId() == storedSearchToUpdate.getId()).findFirst();

			// search for sewtch with same name but different id
			Optional<T> sameNameDiffIdFound = storedSearches.stream()
			                                                .filter(c -> c.getId() != storedSearchToUpdate.getId() && c.getName().equalsIgnoreCase(storedSearchToUpdate.getName()))
			                                                .findFirst();

			if (sameNameDiffIdFound.isPresent()) {
				StoredSearch sameNameDiffIdSearch = sameNameDiffIdFound.get();

				logger.debug("Category with name {} but id {} was not found in Stored Searches repository", sameNameDiffIdSearch.getName(), sameNameDiffIdSearch.getId());

				if (isCategory) {
					throw new StoredSearchesException(StoredSearchesErrorCode.CategoryUpdateError).put(CATEGORY_NAME_ERROR_KEY, sameNameDiffIdSearch.getName());
				} else if (isSavedSearch) {
					throw new StoredSearchesException(StoredSearchesErrorCode.SavedSearchUpdateError).put(SAVED_SEARCH_NAME_ERROR_KEY, sameNameDiffIdSearch.getName());
				}
			}

			// Stored Search to update was not found
			if (!storedSearchFound.isPresent()) {
				logger.debug("Stored Search with id {} was not found in Stored Searches repository", storedSearchToUpdate.getId());

				if (isCategory) {
					throw new StoredSearchesException(StoredSearchesErrorCode.CategoryUpdateNotFoundError).put(CATEGORY_ID_ERROR_KEY, storedSearchToUpdate.getId());
				} else if (isSavedSearch) {
					throw new StoredSearchesException(StoredSearchesErrorCode.SavedSearchUpdateWasNotFoundError).put(SAVED_SEARCH_ID_ERROR_KEY, storedSearchToUpdate.getId());
				}

			} else {
				StoredSearch storedSearch = storedSearchFound.get();

				// Stored Search timestamp is different : stored version and updated version
				if (!storedSearch.getLastChangeDateTimeGMT().equals(storedSearchToUpdate.getLastChangeDateTimeGMT())) {

					logger.debug("Stored Search has different last upated time. Stored - {}, Updated  - {}.", storedSearch.getLastChangeDateTimeGMT(),
					             storedSearchToUpdate.getLastChangeDateTimeGMT());

					if (isCategory) {
						throw new StoredSearchesException(StoredSearchesErrorCode.CategoryUpdateNotLatestVersionError).put(CATEGORY_ID_ERROR_KEY, storedSearchToUpdate.getId());
					} else if (isSavedSearch) {
						throw new StoredSearchesException(StoredSearchesErrorCode.SavedSearchUpdateNotLatestVersionError).put(SAVED_SEARCH_ID_ERROR_KEY, storedSearchToUpdate.getId());
					}

				} else {
					storedSearch.setLastChangeDateTimeGMT(dateTimeNow);
					storedSearch.setLastModifiedByUserId(storedSearchToUpdate.getLastModifiedByUserId());
					storedSearch.setName(storedSearchToUpdate.getName());
					storedSearch.setDescription(storedSearchToUpdate.getDescription());
					storedSearch.setQuery(storedSearchToUpdate.getQuery());
					storedSearch.setSearchContext(storedSearchToUpdate.getSearchContext());
					storedSearch.setSearchContextVersion(storedSearchToUpdate.getSearchContextVersion());

					if (isCategory) {
						Category categoryFound = (Category) storedSearchFound.get();
						Category categoryData =  (Category) storedSearchToUpdate;

						categoryFound.setActive(categoryData.isActive());
						categoryFound.setColor(categoryData.getColor());
						categoryFound.setImpact(categoryData.getImpact());
						categoryFound.setPublished(categoryData.isPublished());
					} else if (isSavedSearch) {
						SavedSearch savedSearchFound = (SavedSearch) storedSearchFound.get();
						SavedSearch savedSearchData =  (SavedSearch) storedSearchToUpdate;

						savedSearchFound.setPublic(savedSearchData.isPublic());
					}
				}
			}
		} else {
			throw new StoredSearchesException(StoredSearchesErrorCode.StoredSearchesUpdateGenericError);
		}

		return storedSearches;
	}

	/**
	 * Deletes category from list of repository categories.
	 * @param storedSearches list of stored searches
	 * @param storedSearchToRemove stored serch to remove
	 * @param <T> type of stored search : Category or Saved Search
	 * @return modified list of saved searches
	 */
	public <T extends StoredSearch> List<T> deleteStoredSearch(List<T> storedSearches, T storedSearchToRemove) {
		boolean removed = false;

		if (!CollectionUtils.isEmpty(storedSearches)) {

			boolean isCategory = storedSearchToRemove instanceof Category;
			boolean isSavedSearch = storedSearchToRemove instanceof SavedSearch;

			// search for category to delete
			Optional<T> storedSearchFound = storedSearches.stream().filter(c -> c.getId() == storedSearchToRemove.getId()).findFirst();

			if (!storedSearchFound.isPresent()) {
				logger.info("Stored Search with id {} was not found in Stored Searches repository. Probably removed by another request.", storedSearchToRemove.getId());

				// if category was not found, it was removed by another account
			} else {
				T storedSearch = storedSearchFound.get();

				// Stored Search timestamp is different : stored version and updated version
				if (!storedSearch.getLastChangeDateTimeGMT().equals(storedSearchToRemove.getLastChangeDateTimeGMT())) {

					logger.debug("Stored Search has different last upated time. Stored - {}, Updated  - {}.", storedSearch.getLastChangeDateTimeGMT(),
					             storedSearchToRemove.getLastChangeDateTimeGMT());

					if (isCategory) {
						throw new StoredSearchesException(StoredSearchesErrorCode.CategoryRemoveNotLatestVersion).put(CATEGORY_ID_ERROR_KEY, storedSearchToRemove.getId());
					} else if (isSavedSearch) {
						throw new StoredSearchesException(StoredSearchesErrorCode.SavedSearchRemoveNotLatestVersion).put(SAVED_SEARCH_ID_ERROR_KEY, storedSearchToRemove.getId());
					}
				} else {
					logger.info("Removing Stored Search with id {} from Stored Searches collection", storedSearchToRemove.getId());

					// finally remove Stored search
					removed = storedSearches.removeIf(c -> c.getId() == storedSearchToRemove.getId());

					if (!removed) {
						logger.error("Failed to remove stored search with id {} from Stored Searches collection", storedSearchToRemove.getId());

						if (isCategory) {
							throw new StoredSearchesException(StoredSearchesErrorCode.CategoryRemoveError).put(CATEGORY_ID_ERROR_KEY, storedSearchToRemove.getId());
						} else if (isSavedSearch) {
							throw new StoredSearchesException(StoredSearchesErrorCode.SavedSearchesRemoveError).put(SAVED_SEARCH_ID_ERROR_KEY, storedSearchToRemove.getId());
						}
					}
				}
			}
		}

		return storedSearches;
	}



	private <T extends StoredSearch> boolean storedNameExistsInRepository(List<T> storedSearches, final String name, final int exceptsid) {

		for (T storedSearch : storedSearches) {
			if (storedSearch.getName().equalsIgnoreCase(name) && exceptsid != storedSearch.getId()) {
				return true;
			}
		}

		return false;
	}

}
