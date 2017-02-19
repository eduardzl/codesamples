package com.verint.textanalytics.model.storedSearch;

import java.util.ArrayList;
import java.util.List;

import com.verint.textanalytics.common.utils.CollectionUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author imor
 *
 */
public class CategoriesRepository extends StoredSearchRepository {

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<Category> categories;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<String> canNotParseCategories;

	/**
	 * C'tor.
	 */
	public CategoriesRepository() {
		this.categories = new ArrayList<>();
	}

	/**
	 * Adds Category to list of categories.
	 * @param category category to add
	 * @return reference to repository
	 */
	public CategoriesRepository addCategory(Category category) {
		this.categories.add(category);
		return this;
	}

	/**
	 * Adds list of categories to repository of categories.
	 * @param categoriesToAdd list of categories
	 * @return reference to repository
	 */
	public CategoriesRepository addCategories(List<Category> categoriesToAdd) {
		if (!CollectionUtils.isEmpty(categoriesToAdd)) {
			this.categories.addAll(categoriesToAdd);
		}

		return this;
	}
}
