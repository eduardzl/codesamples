package com.verint.textanalytics.bl.security;

import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.utils.JSONUtils;
import com.verint.textanalytics.model.security.Channel;
import com.verint.textanalytics.model.security.Tenant;
import com.verint.textanalytics.model.security.User;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/***
 * @author yzanis
 */
public class FileMembershipProvider implements MembershipProvider {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	private ConfigurationManager configurationManager;

	@Autowired
	private EMConfigurationManager emConfigurationManager;

	@Override
	public User getUser(String i360FoundationToken) {
		logger.debug("Reading user data using FileMembershipProvider.");

		User user = new User();
		user.setSuperUser(true);

		List<Tenant> allTenants;

		// Create List Of Teanants and channels from configuration files
		val configuration = this.configurationManager.getApplicationConfiguration();
		val channels = configuration.getDebugTenantChannels();

		Tenant tenant = new Tenant();
		tenant.setEmId(1);
		tenant.setDisplayName("DebugTenant");
		tenant.setId(configurationManager.getApplicationConfiguration().getDebugTenant());
		tenant.setChannels(new ArrayList<Channel>());
		Channel channel;

		for (int i = 0; i < channels.size(); i++) {
			channel = new Channel();
			channel.setEmId(i + 1);
			channel.setDisplayName(channels.get(i));
			channel.setId(channels.get(i));

			tenant.getChannels().add(channel);
		}

		allTenants = new ArrayList<Tenant>();
		allTenants.add(tenant);

		user.setTenantsList(allTenants);

		user.addPrivilege("IF.USEAPPLICATION");
		user.addPrivilege("IF.INTELLIFIND");
		user.addPrivilege("IF.SEARCHBYKEYWORDS");
		user.addPrivilege("IF.ADDFORM");
		user.addPrivilege("IF.TEXTMODELMANAGEMENT");
		user.addPrivilege("IF.VIEWTRANSCRIPT");

		logger.debug("User data read using FileMembershipProvider. User - {}", () -> JSONUtils.getObjectJSON(user));

		return user;
	}
}