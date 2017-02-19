package com.verint.textanalytics.dal.configService;

import com.verint.textanalytics.dal.darwin.RestRequestPathsAndQueryParams;
import lombok.val;

/**
 * Created by EZlotnik on 12/23/2015.
 */
public class AnalyticsTaggerRequestGenerator {

	/**
	 * Generates Category Reprocessing request.
	 *
	 * @param tenant     tenant
	 * @param channel    channel
	 * @param categoryId category id
	 * @return query parameters of request
	 */
	public RestRequestPathsAndQueryParams getInvokeCategoryReprocessingQuery(String tenant, String channel, int categoryId) {
		val restRequestPathsAndQueryParams = new RestRequestPathsAndQueryParams();

		// generate request according to Categories API definition
		addCategoriesAPI(restRequestPathsAndQueryParams);

		return restRequestPathsAndQueryParams;
	}

	private void addCategoriesAPI(RestRequestPathsAndQueryParams requestParams) {
		requestParams.getQueryPaths().add("categoriesAPI");
	}

	private void addCategoryReprocessingAPI(RestRequestPathsAndQueryParams requestParams) {
		requestParams.getQueryPaths().add(0, "tagger");
		requestParams.getQueryPaths().add(1, "reprocessCategory");
	}

	private void addTenant(RestRequestPathsAndQueryParams requestParams, String tenant) {
		requestParams.getQueryPaths().add(tenant);
	}

	private void addChannel(RestRequestPathsAndQueryParams requestParams, String channel) {
		requestParams.getQueryPaths().add(channel);
	}

	private void addCategoryId(RestRequestPathsAndQueryParams requestParams, int categoryId) {
		requestParams.getQueryPaths().add(String.valueOf(categoryId));
	}
}
