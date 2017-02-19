package com.verint.textanalytics.web.uiservices;

import com.verint.textanalytics.bl.applicationservices.ConfigurationService;
import com.verint.textanalytics.bl.applicationservices.DataSourcesManagmentService;
import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.utils.CollectionUtils;
import com.verint.textanalytics.dal.darwin.TextAnalyticsProvider;
import com.verint.textanalytics.dal.rest.RestDataAccess;
import com.verint.textanalytics.model.security.Channel;
import com.verint.textanalytics.model.security.Tenant;
import com.verint.textanalytics.model.security.User;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration UI service.
 *
 * @author EZlotnik
 */
public class ConfigurationUIService extends BaseUIService {

	@Autowired
	private ConfigurationManager configurationManager;

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private ViewModelConverter viewModelConverter;

	@Autowired
	private DataSourcesManagmentService dataSourceService;

	/**
	 * Extracts ExtJs application configuration and list of channel for current
	 * tenant.
	 *
	 * @param i360FoundationToken Foundation token
	 * @return a configuration
	 */
	public com.verint.textanalytics.web.viewmodel.Configuration getConfiguration(String i360FoundationToken) {
		val channels = new ArrayList<Channel>();
		
		// retrieve channels available for user
		List<Channel> userChannels = this.getUserChannels(i360FoundationToken);
		if (!CollectionUtils.isEmpty(userChannels)) {
			channels.addAll(channels.size(), userChannels);
		}

		String userTenant;
		// add list of dynamic fields to each Channel configuration and the channel source types
		for (Channel channel : channels) {
			userTenant = this.getTenantFromChannel(channel.getId(), i360FoundationToken);
			channel.setDynamicFields(configurationService.getChannelDynamicFields(userTenant, channel.getId()));
			channel.setSourceTypes(configurationService.getChannelSourceTypes(userTenant, channel.getId()));
			channel.setSourceTypeSpecificFields(configurationService.getSourceTypeSpecificFields(channel.getSourceTypes()));
		}

		ApplicationConfiguration appConfig = configurationManager.getApplicationConfiguration();

		val configuration = viewModelConverter.convertToViewModelConfiguration(false, channels, appConfig);

		return configuration;
	}

	/**
	 * Create tenant if not exist.
	 *
	 * @param i360FoundationToken Foundation token
	 * @param tenantName - tenant name
	 * @return a configuration
	 */
	public boolean createTenant(String i360FoundationToken, String tenantName) {
		return this.dataSourceService.isTenantExists(tenantName);

	}

	private List<Channel> getUserChannels(String i360FoundationToken) {

		ArrayList<Channel> result = new ArrayList<Channel>();

		User curUser = this.getUser(i360FoundationToken);

		List<Tenant> userTenants = curUser.getTenantsList();

		for (Tenant tenant : userTenants) {
			result.addAll(tenant.getChannels());
		}

		return result;
	}

}
