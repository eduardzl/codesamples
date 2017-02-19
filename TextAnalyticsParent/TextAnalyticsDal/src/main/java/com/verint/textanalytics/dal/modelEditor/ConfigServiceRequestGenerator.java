package com.verint.textanalytics.dal.modelEditor;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.Validate.NotNull;
import lombok.val;
import lombok.experimental.Accessors;

import org.springframework.beans.factory.annotation.Autowired;

import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.dal.darwin.RestRequestPathsAndQueryParams;

/***
 * 
 * @author imor
 *
 */
public class ConfigServiceRequestGenerator {

	@Getter
	@Setter
	@Accessors(chain = true)
	@Autowired
	private ConfigurationManager configurationManager;

	/***
	 * 
	 * @param tenant
	 *            tenant
	 * @return get RetrieveModelsTree Query
	 */
	public RestRequestPathsAndQueryParams getRetrieveModelsTreeQuery(String tenant) {
		val restRequestPathsAndQueryParams = new RestRequestPathsAndQueryParams();

		addCommonProperties(tenant, restRequestPathsAndQueryParams);

		return restRequestPathsAndQueryParams;

	}

	private void addCommonProperties(@NotNull String tenant, final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {

		addConfigServiceName(restRequestPathsAndQueryParams);
		addRestToPath(restRequestPathsAndQueryParams);
		addTenantToPath(restRequestPathsAndQueryParams);
		addTenantName(tenant, restRequestPathsAndQueryParams);
		addTreeRequest(restRequestPathsAndQueryParams);
	}

	private void addConfigServiceName(final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		List<String> requestPaths = restRequestPathsAndQueryParams.getQueryPaths();
		requestPaths.add(configurationManager.getApplicationConfiguration().getConfigServiceFolder());
	}

	private void addRestToPath(final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		List<String> requestPaths = restRequestPathsAndQueryParams.getQueryPaths();
		requestPaths.add("rest");
	}

	private void addTenantToPath(final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		List<String> requestPaths = restRequestPathsAndQueryParams.getQueryPaths();
		requestPaths.add("tenant");
	}

	private void addTenantName(@NotNull String tenant, final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		List<String> requestPaths = restRequestPathsAndQueryParams.getQueryPaths();
		requestPaths.add(tenant);
	}

	private void addTreeRequest(final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		val queryParams = restRequestPathsAndQueryParams.getQueryParams();
		queryParams.add("tree", "");
	}
}