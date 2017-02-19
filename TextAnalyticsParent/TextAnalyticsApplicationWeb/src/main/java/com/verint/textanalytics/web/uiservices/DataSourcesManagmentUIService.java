package com.verint.textanalytics.web.uiservices;

import java.util.List;

import lombok.val;

import org.springframework.beans.factory.annotation.Autowired;
import org.apache.logging.log4j.*;
import com.verint.textanalytics.bl.applicationservices.DataSourcesManagmentService;
import com.verint.textanalytics.bl.security.EMConfigurationManager;
import com.verint.textanalytics.model.security.Tenant;
import com.verint.textanalytics.model.security.User;

/***
 * 
 * @author yzanis
 *
 */
public class DataSourcesManagmentUIService extends BaseUIService {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	private DataSourcesManagmentService dataSourcesManagmentService;

	@Autowired
	private EMConfigurationManager emConfigurationManager;

	@Autowired
	private ViewModelConverter viewModelConverter;

	/**
	 * C'tor.
	 */
	public DataSourcesManagmentUIService() {
		super();
	}

	/***
	 * 
	 * @param i360FoundationToken
	 *            user token
	 * @return list of stores (tenants) available to the user
	 */
	public List<com.verint.textanalytics.web.viewmodel.Tenant> getAvailableTenantsList(String i360FoundationToken) {

		User user = getUser(i360FoundationToken);
		val userTenants = user.getTenantsList();

		return viewModelConverter.convertToViewModelTenants(userTenants);
	}

	/****
	 * 
	 * @param i360FoundationToken
	 *            user token
	 * @param storeID
	 *            the ID of the Store (name in solr)
	 * @return list of channels that parent is the store
	 */
	public List<com.verint.textanalytics.web.viewmodel.Channel> getChannelsByStore(String i360FoundationToken, String storeID) {

		logger.debug("Getting list of channels for  tenantId = {}", storeID);

		User user = getUser(i360FoundationToken);

		for (Tenant tenant : user.getTenantsList()) {
			if (tenant.getId().equals(storeID)) {

				//Because the tenant exists we first need to check if this tenant was created in solr and only then return the list of channels.
				boolean isExists = dataSourcesManagmentService.isTenantExists(storeID);

				if (isExists) {
					logger.debug("The tenant tenantId = {}, Exists or was created", storeID);
				} else {
					logger.debug("Problem in creating the tenantId = {}, Tenant doesn't exists.", storeID);
				}

				val channels = tenant.getChannels();
				return viewModelConverter.convertToViewModelChannels(channels);
			}
		}

		return null;
	}

	/****
	 * 
	 * @param i360FoundationToken
	 *            user token
	 * @param storeID
	 *            the ID of the Store (name in solr)
	 * @return this deletes the tenant and the tenant data, return true if was
	 *         deleted false if the tenant does not exists or error in any other
	 *         case.
	 */
	public boolean deleteTenantAndTenantData(String i360FoundationToken, String storeID) {
		logger.debug("Deleting tenant and tenant data for tenantId = {}", storeID);

		boolean wasDeleted = false;

		User user = getUser(i360FoundationToken);

		for (Tenant tenant : user.getTenantsList()) {
			if (tenant.getId().equals(storeID)) {

				//only if the user has privileges to this tenant only then delete this tenant.
				wasDeleted = dataSourcesManagmentService.deleteTenantAndTenantData(storeID);

				if (wasDeleted) {
					logger.debug("The tenant tenantId = {}, was deleted", storeID);
				} else {
					logger.debug("The tenant tenantId = {}, didnt exists and wasnt deleted.", storeID);
				}
			}
		}

		return wasDeleted;
	}
}