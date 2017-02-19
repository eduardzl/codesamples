package com.verint.textanalytics.dal.configService;

import com.verint.textanalytics.common.collection.MultivaluedStringMap;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.utils.StringUtils;
import com.verint.textanalytics.dal.darwin.RestRequestPathsAndQueryParams;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/***
 * @author yzanis
 */
public class ConfigServiceRequestGenerator {

	@Getter
	@Setter
	@Accessors(chain = true)
	@Autowired
	private ConfigurationManager configurationManager;

	/***
	 * @param tenant  tenant
	 * @param channel channel
	 * @return query to get list of categories from config service
	 */
	public RestRequestPathsAndQueryParams getGetCategoriesQuery(String tenant, String channel) {
		val restRequestPathsAndQueryParams = new RestRequestPathsAndQueryParams();

		addCommonProperties(channel, tenant, restRequestPathsAndQueryParams);
		addCategoriesFileTypeToPath(restRequestPathsAndQueryParams);

		return restRequestPathsAndQueryParams;
	}

	/***
	 * @param tenant  tenant
	 * @param channel channel
	 * @return query to get list of categories from config service
	 */
	public RestRequestPathsAndQueryParams getGetChannelConfigurationQuery(String tenant, String channel) {
		val restRequestPathsAndQueryParams = new RestRequestPathsAndQueryParams();

		addCommonProperties(channel, tenant, restRequestPathsAndQueryParams);
		addChannelFileTypeToPath(restRequestPathsAndQueryParams);

		return restRequestPathsAndQueryParams;
	}

	/***
	 * @param tenant    tenant
	 * @param channel   channel
	 * @param timeStemp timeStemp
	 * @return query to write list of categories to config service with the time
	 * stemp of
	 */
	public RestRequestPathsAndQueryParams getWriteCategoriesQuery(String tenant, String channel, String timeStemp) {
		val restRequestPathsAndQueryParams = getGetCategoriesQuery(tenant, channel);

		// no timestamp should be added if stored file don't exist
		if (!StringUtils.isNullOrBlank(timeStemp)) {
			addTimeStempToParams(timeStemp, restRequestPathsAndQueryParams);
		}

		return restRequestPathsAndQueryParams;
	}

	/***
	 * @param tenant  tenant
	 * @param channel channel
	 * @return query to get list of saved serches from config service
	 */
	public RestRequestPathsAndQueryParams getGetSavedSearchesQuery(String tenant, String channel) {
		val restRequestPathsAndQueryParams = new RestRequestPathsAndQueryParams();

		addCommonProperties(channel, tenant, restRequestPathsAndQueryParams);
		addSavedSearchFileTypeToPath(restRequestPathsAndQueryParams);

		return restRequestPathsAndQueryParams;
	}

	/**
	 * Generates query for Categories Reprocessing states.
	 *
	 * @param tenant  tenant
	 * @param channel channel of categories
	 * @return query for category reprocessing states file
	 */
	public RestRequestPathsAndQueryParams getCategoriesReprocessingStatesQuery(String tenant, String channel) {
		val restRequestPathsAndQueryParams = new RestRequestPathsAndQueryParams();

		addCommonProperties(channel, tenant, restRequestPathsAndQueryParams);
		addCategoriesReprocessingStateFileParam(restRequestPathsAndQueryParams);

		return restRequestPathsAndQueryParams;
	}

	/***
	 * @param tenant    tenant
	 * @param channel   channel
	 * @param timeStemp timeStemp
	 * @return query to write list of saved searches to config service with the
	 * time stemp of
	 */
	public RestRequestPathsAndQueryParams getWriteSavedSearchesQuery(String tenant, String channel, String timeStemp) {
		val restRequestPathsAndQueryParams = getGetSavedSearchesQuery(tenant, channel);

		// no timestamp should be added if stored file don't exist
		if (!StringUtils.isNullOrBlank(timeStemp)) {
			addTimeStempToParams(timeStemp, restRequestPathsAndQueryParams);
		}

		return restRequestPathsAndQueryParams;
	}

	private void addCommonProperties(String channel, String tenant, final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		addConfigServiceName(restRequestPathsAndQueryParams);
		addRestToPath(restRequestPathsAndQueryParams);
		addChannelToPath(restRequestPathsAndQueryParams);
		addTenantName(tenant, restRequestPathsAndQueryParams);
		addChannelName(channel, restRequestPathsAndQueryParams);
	}

	private void addChannelToPath(final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		List<String> requestPaths = restRequestPathsAndQueryParams.getQueryPaths();
		requestPaths.add("channel");
	}

	private void addConfigServiceName(final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		List<String> requestPaths = restRequestPathsAndQueryParams.getQueryPaths();
		requestPaths.add(configurationManager.getApplicationConfiguration().getConfigServiceFolder());
	}

	private void addTenantName(String tenant, final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		List<String> requestPaths = restRequestPathsAndQueryParams.getQueryPaths();
		requestPaths.add(tenant);
	}

	private void addChannelName(String channel, final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		List<String> requestPaths = restRequestPathsAndQueryParams.getQueryPaths();
		requestPaths.add(channel);
	}

	private void addRestToPath(final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		List<String> requestPaths = restRequestPathsAndQueryParams.getQueryPaths();
		requestPaths.add("rest");
	}

	private void addCategoriesFileTypeToPath(final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		List<String> requestPaths = restRequestPathsAndQueryParams.getQueryPaths();
		requestPaths.add(configurationManager.getApplicationConfiguration().getCategoryFile());
	}

	private void addChannelFileTypeToPath(final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		List<String> requestPaths = restRequestPathsAndQueryParams.getQueryPaths();
		requestPaths.add(configurationManager.getApplicationConfiguration().getChannelFile());
	}

	private void addSavedSearchFileTypeToPath(final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		List<String> requestPaths = restRequestPathsAndQueryParams.getQueryPaths();
		requestPaths.add(configurationManager.getApplicationConfiguration().getSavedSearchesFile());
	}

	private void addCategoriesReprocessingStateFileParam(final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		List<String> requestPaths = restRequestPathsAndQueryParams.getQueryPaths();
		requestPaths.add(configurationManager.getApplicationConfiguration().getCategoriesReprocessingStateFile());
	}

	private void addTimeStempToParams(String timeStemp, final RestRequestPathsAndQueryParams restRequestPathsAndQueryParams) {
		MultivaluedStringMap requestPaths = restRequestPathsAndQueryParams.getQueryParams();
		requestPaths.add(configurationManager.getApplicationConfiguration().getTimeStempFiled(), timeStemp);
	}
}