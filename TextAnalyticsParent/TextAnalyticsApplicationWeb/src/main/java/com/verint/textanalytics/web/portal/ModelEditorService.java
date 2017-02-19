package com.verint.textanalytics.web.portal;

import java.util.Map.Entry;

import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import lombok.val;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.security.ChannelAuthorizationNotRequired;
import com.verint.textanalytics.common.utils.JSONUtils;
import com.verint.textanalytics.web.uiservices.ModelEditorUIService;
import com.verint.textanalytics.web.viewmodel.ListDataResult;
import com.verint.textanalytics.web.viewmodel.Model;
import com.verint.textanalytics.web.viewmodel.requestparams.ChannelsManagmentParams;

/**
 * Model Editor Service.
 * 
 * @author imor
 *
 */
@Path("/ModelEditorService")
public class ModelEditorService {

	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	private ModelEditorUIService modelEditorUIService;

	/**
	 * @param i360FoundationToken
	 *            token
	 * @param tenantData
	 *            tenantData
	 * @return categories retrieve Models Tree
	 */
	@ChannelAuthorizationNotRequired
	@Path("retrieveModelsTree")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public ListDataResult<Model> retrieveModelsTree(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken, final ChannelsManagmentParams tenantData) {

		logger.debug("retrieveModelsTree request invoked. Request params - {}", () -> JSONUtils.getObjectJSON(tenantData));

		val result = modelEditorUIService.retrieveModelsTree(i360FoundationToken, tenantData.getTenantID());

		return new ListDataResult<Model>().setData(result.getModels()).setSuccess(true).setTotalCount(result.getModels().size());
	}

	/***
	 * 
	 * @param i360FoundationToken
	 *            user token
	 * @return list of tenants that the user can see by his permissions
	 */
	@ChannelAuthorizationNotRequired
	@Path("getModelEditorHeaderToken")
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public Entry<String, String> getModelEditorHeaderToken(@CookieParam(TAConstants.i360FoundationTokenHeader) String i360FoundationToken) {

		logger.debug("getModelEditorHeaderToken request invoked.");

		return modelEditorUIService.getModelEditorHeaderToken(i360FoundationToken);
	}
}
