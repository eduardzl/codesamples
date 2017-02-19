package com.verint.textanalytics.web.uiservices;

import com.verint.textanalytics.bl.security.MembershipProvider;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.exceptions.AuthorizationErrorCode;
import com.verint.textanalytics.common.exceptions.AuthorizationException;
import com.verint.textanalytics.model.security.Channel;
import com.verint.textanalytics.model.security.User;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;

import java.util.ArrayList;
import java.util.List;

/**
 * Base for UI Services.
 *
 * @author EZlotnik
 */
public class BaseUIService {

	@Autowired
	@Setter
	@Getter
	private MembershipProvider membershipProvider;

	@Autowired
	private ConfigurationManager configurationManager;

	@Cacheable(value = "TextAnalyticsUserCache", key = "#i360FoundationToken")
	protected User getUser(String i360FoundationToken) {
		return membershipProvider.getUser(i360FoundationToken);
	}

	protected String getTenantFromChannel(String channel, String i360FoundationToken) {
		String tenant = null;

		User user = getUser(i360FoundationToken);
		tenant = user.getTanantNameByChannel(channel);

		if (tenant == null) {
			throw new AuthorizationException("User Isnt authorized to this channel", AuthorizationErrorCode.InvalidAccessError);
		}

		return tenant;

	}

	//TODO: add function to get tenant from channel per user
	protected List<Channel> getDebugUserTenantChannels(String i360FoundationToken) {
		List<Channel> lstChannels = new ArrayList<Channel>();

		val configuration = this.configurationManager.getApplicationConfiguration();
		val channels = configuration.getDebugTenantChannels();
		if (channels != null) {
			for (val channel : channels) {
				lstChannels.add(new Channel(channel));
			}
		}

		return lstChannels;
	}
}
