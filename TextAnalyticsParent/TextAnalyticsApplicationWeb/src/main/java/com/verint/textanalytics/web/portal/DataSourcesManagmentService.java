package com.verint.textanalytics.web.portal;

import java.util.List;

import javax.ws.rs.CookieParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.logging.log4j.*;
import org.springframework.beans.factory.annotation.Autowired;

import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.security.ChannelAuthorizationNotRequired;
import com.verint.textanalytics.common.security.OperationPrivelegesAnnotation;
import com.verint.textanalytics.common.security.OperationPrivelegesAnnotation.PrivilegeType;
import com.verint.textanalytics.common.utils.JSONUtils;
import com.verint.textanalytics.web.uiservices.DataSourcesManagmentUIService;
import com.verint.textanalytics.web.viewmodel.Channel;
import com.verint.textanalytics.web.viewmodel.ListDataResult;
import com.verint.textanalytics.web.viewmodel.Tenant;
import com.verint.textanalytics.web.viewmodel.requestparams.ChannelsManagmentParams;

/***
 * 
 * @author yzanis
 *
 */
@Path("/DataSourcesManagmentService")
public class DataSourcesManagmentService {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	private DataSourcesManagmentUIService dataSourcesManagmentUIService;

	/***
	 * 
	 * @param i360FoundationToken
	 *            user token
	 * @return list of tenants that the user can see by his permissions
	 */
	@ChannelAuthorizationNotRequired
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { PrivilegeType.USEAPPLICATION, PrivilegeType.TEXTMODELMANAGEMENT })
	@Path("getTenants")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public ListDataResult<Tenant> getTenants(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken) {
		logger.debug("getTenants request invoked.");

		List<Tenant> result = dataSourcesManagmentUIService.getAvailableTenantsList(i360FoundationToken);

		return new ListDataResult<Tenant>().setData(result).setSuccess(true).setTotalCount(result.size());
	}

	/**
	 * @param i360FoundationToken
	 *            user token
	 * @param tenantData
	 *            tenant name and id
	 * @return list of channels to this tenant by user permissions
	 */
	@ChannelAuthorizationNotRequired
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { PrivilegeType.USEAPPLICATION, PrivilegeType.TEXTMODELMANAGEMENT })
	@Path("getChannels")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public ListDataResult<Channel> getChannels(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, ChannelsManagmentParams tenantData) {

		logger.debug("getChannels request invoked. Request parameters - {}", () -> JSONUtils.getObjectJSON(tenantData));

		List<Channel> result = dataSourcesManagmentUIService.getChannelsByStore(i360FoundationToken, tenantData.getTenantID());

		return new ListDataResult<Channel>().setData(result).setSuccess(true).setTotalCount(result.size());
	}

	/**
	 * @param i360FoundationToken
	 *            user token
	 * @param tenantData
	 *            tenant id
	 * @return this deletes the tenant and the tenant data, return true if was
	 *         deleted false if the tenant does not exists or error in any other
	 *         case.
	 */
	@ChannelAuthorizationNotRequired
	@OperationPrivelegesAnnotation(requiredAllPrivileges = { PrivilegeType.USEAPPLICATION, PrivilegeType.TEXTMODELMANAGEMENT })
	@Path("deleteTenant")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public boolean deleteTenant(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, ChannelsManagmentParams tenantData) {
		logger.debug("DeleteTenant request invoked. Request parameters  - {}", () -> JSONUtils.getObjectJSON(tenantData));

		return dataSourcesManagmentUIService.deleteTenantAndTenantData(i360FoundationToken, tenantData.getTenantID());
	}
}