package com.verint.textanalytics.bl.applicationservices;

import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.model.storedSearch.Category;
import com.verint.textanalytics.model.storedSearch.CategoryReprocessingStatus;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by EZlotnik on 12/23/2015.
 */
public class CategoriesReprocessingService {

	private Logger logger = LogManager.getLogger(this.getClass());

	@Setter
	private ConfigurationManager configurationManager;

	/**
	 * Constructor.
	 */
	public CategoriesReprocessingService() {

	}

	/**
	 * Determine if Category reprocessing allowed.
	 * @param category category to reprocess
	 * @return indication wether category is allowed for reprocessing
	 */
	public boolean isCategoryReprocessingAllowed(Category category) {
		boolean reprocessingAllowed = false;

		ApplicationConfiguration appConfig = this.configurationManager.getApplicationConfiguration();

		// only Active Category can be reprocessed
		if (category.isActive()) {
			if (category.getReprocessingState() != null) {
				reprocessingAllowed = category.getReprocessingState().getStatus() != CategoryReprocessingStatus.Reprocessing;
				logger.trace("Checking if category reprocessing allowed, Is allowed :{}", reprocessingAllowed);
			} else {
				reprocessingAllowed = true;
			}
		}

		return reprocessingAllowed;
	}
}
