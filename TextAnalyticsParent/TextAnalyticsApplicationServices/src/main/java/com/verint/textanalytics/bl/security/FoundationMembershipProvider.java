package com.verint.textanalytics.bl.security;

import com.google.common.base.Throwables;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.exceptions.FoundationServicesExecutionErrorCode;
import com.verint.textanalytics.common.exceptions.FoundationServicesExecutionException;
import com.verint.textanalytics.common.utils.JSONUtils;
import com.verint.textanalytics.dal.rest.RestDataAccess;
import com.verint.textanalytics.model.security.Channel;
import com.verint.textanalytics.model.security.Tenant;
import com.verint.textanalytics.model.security.User;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author EZlotnik Provider for accessing user information.
 */
public class FoundationMembershipProvider implements MembershipProvider {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	private RestDataAccess restDataAccess;

	@Autowired
	private ConfigurationManager configurationManager;

	@Autowired
	private EMConfigurationManager emConfigurationManager;

	@Autowired
	private FoundationResponseConverter foundationResponseConverter;

	/**
	 * @param i360FoundationToken - i360FoundationToken.
	 * @return returns a user information
	 */
	@Override
	@Cacheable(value = "TextAnalyticsUserCache",
			key = "#i360FoundationToken")
	public User getUser(String i360FoundationToken) {

		val userJson = getUserFromFoundationService(i360FoundationToken);

		logger.trace("Retriving user information getUserFromFoundationService. Data retrived - {}", userJson != null ? userJson : "");

		// convert json from Foundation to use object
		User user = foundationResponseConverter.getUser(userJson);

		val authorizedChannels = foundationResponseConverter.getListOfProjectDataSources(userJson);

		if (user.isSuperUser()) {
			user.setTenantsList(emConfigurationManager.getEMConfiguratedDataSources());
			// TODO remove this
			// add privilege to him
			user.addPrivilege("IF.USEAPPLICATION");
			user.addPrivilege("IF.INTELLIFIND");
			user.addPrivilege("IF.SEARCHBYKEYWORDS");
			user.addPrivilege("IF.ADDFORM");
			user.addPrivilege("IF.TEXTMODELMANAGEMENT");
			user.addPrivilege("IF.VIEWTRANSCRIPT");

		} else {
			user.setTenantsList(emConfigurationManager.getDataSoursesListFiltered(authorizedChannels));

		}

		//add to the user debug tenant and channels - from config file
		val configuration = this.configurationManager.getApplicationConfiguration();

		val channels = configuration.getDebugTenantChannels();
		String tenantName = configuration.getDebugTenant();

		//check user doesnt ha
		Tenant tenant = new Tenant();

		if (channels != null & channels.size() > 0 & tenantName != null) {
			logger.info("Configuration Files has channels configuration.Loading channels to user permmisions");
			List<Channel> lstChannels = new ArrayList<Channel>();
			for (val channel : channels) {
				//check this channel doesnt exist in the user
				if (user.getTanantNameByChannel(channel) == null) {
					lstChannels.add(new Channel(channel));
				}
			}
			
			if (lstChannels.size() > 0) {
				tenant.setId(tenantName);
				tenant.setDisplayName(tenantName);
				tenant.setChannels(lstChannels);

				val tenantsList = user.getTenantsList();
				tenantsList.add(tenant);
				user.setTenantsList(tenantsList);
			}
		}

		logger.trace("User data generated {}", () -> user != null ? JSONUtils.getObjectJSON(user) : "");

		return user;
	}

	/***
	 * @param i360FoundationToken user token to get user context data
	 * @return user response JSON from Foundation service
	 */
	private String getUserFromFoundationService(String i360FoundationToken) {
		String responseJson = null;

		try {

			Map<String, String> postBodyHeaders = new HashMap<>();
			postBodyHeaders.put("Content-Type", "application/json");
			postBodyHeaders.put("Impact360AuthToken", i360FoundationToken);

			val applicationConfiguration = configurationManager.getApplicationConfiguration();
			String url = applicationConfiguration.getFoundationServiceURL();
			url += applicationConfiguration.getFoundationServiceUserContextURL();

			logger.debug("Invoking request to Foundtion API to get user context. Service url - {}", url);

			responseJson = restDataAccess.executePostRequest("getUserAvailableDataSourses", url, new ArrayList<String>(), null, "{}", postBodyHeaders);

		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, FoundationServicesExecutionException.class);
			Throwables.propagate(new FoundationServicesExecutionException(ex, FoundationServicesExecutionErrorCode.UserContextExecutionError));
		}

		return responseJson;
	}

}
