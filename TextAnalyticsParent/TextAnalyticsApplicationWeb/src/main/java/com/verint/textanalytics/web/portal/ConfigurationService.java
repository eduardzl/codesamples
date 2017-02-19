package com.verint.textanalytics.web.portal;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.security.ChannelAuthorizationNotRequired;
import com.verint.textanalytics.common.security.OperationPrivelegesAnnotation;
import com.verint.textanalytics.common.security.OperationPrivelegesAnnotation.PrivilegeType;
import com.verint.textanalytics.web.uiservices.ConfigurationUIService;
import com.verint.textanalytics.web.viewmodel.Configuration;

/**
 * @author EZlotnik REST service for Configuration
 */
@Path("/ConfigurationService")
public class ConfigurationService {

	@Autowired
	private ConfigurationUIService configurationService;

	/**
	 * Retrieves configuration for ExtJs application.
	 * 
	 * @param i360FoundationToken
	 *            Foundation token, should be validation by AuthorizationFilter
	 * @return configuration object
	 */
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { PrivilegeType.USEAPPLICATION })
	@ChannelAuthorizationNotRequired
	@Path("getConfiguration")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public Configuration getConfiguration(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken) {
		return this.configurationService.getConfiguration(i360FoundationToken);
	}

	/**
	 * Retrieves configuration for ExtJs application.
	 *
	 * @param i360FoundationToken
	 *            Foundation token, should be validation by AuthorizationFilter
	 * @param tenantName - the name of the tenant
	 * @return success
	 */
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { PrivilegeType.USEAPPLICATION, PrivilegeType.TEXTMODELMANAGEMENT })
	@Path("createTenant")
	@Produces(MediaType.APPLICATION_JSON)
	@PUT
	public boolean createTenant(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, String tenantName) {
		boolean success = this.configurationService.createTenant(i360FoundationToken, tenantName);
		return success;
	}
}
