package com.verint.textanalytics.model.storedSearch;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author imor
 *
 */
public class SavedSearchesRepository extends StoredSearchRepository {

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<SavedSearch> savedSearches;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<String> canNotParseSavedSearches;
}
