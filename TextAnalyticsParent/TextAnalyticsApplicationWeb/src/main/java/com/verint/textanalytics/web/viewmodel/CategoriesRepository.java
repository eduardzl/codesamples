package com.verint.textanalytics.web.viewmodel;

import java.util.List;

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

}
