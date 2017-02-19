package com.verint.textanalytics.model.security;

import com.verint.textanalytics.common.utils.JSONUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a logged-in user.
 */
public class User {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String name;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int userID;

	@Getter
	@Setter
	@Accessors(chain = true)
	private boolean isSuperUser;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String viewTimeZone;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<String> privilegesList;

	@Getter
	@Accessors(chain = true)
	private List<Tenant> tenantsList;

	private Map<String, String> channelToTenantName;

	/**
	 * User C'otr.
	 */
	public User() {
		this.privilegesList = new ArrayList<String>();
		this.isSuperUser = false;
		this.name = "";
		this.tenantsList = new ArrayList<Tenant>();
		this.channelToTenantName = new HashMap<>();
	}

	/***
	 * sets tenant list for user.
	 *
	 * @param tenants tenant list
	 */
	public void setTenantsList(List<Tenant> tenants) {
		this.tenantsList = tenants;
		channelToTenantName = new HashMap<>();

		for (Tenant curTenant : tenants) {
			if (curTenant.getChannels() != null) {
				for (Channel curChannel : curTenant.getChannels()) {
					channelToTenantName.put(curChannel.getId(), curTenant.getId());
				}
			}
		}
	}

	/**
	 * @param privilege privilege
	 */
	public void addPrivilege(String privilege) {
		this.privilegesList.add(privilege);
	}

	/**
	 * @param privilege privilege
	 * @return boolean
	 */
	public boolean hasPrivilege(String privilege) {
		return this.privilegesList.contains(privilege);
	}

	@Override
	public String toString() {
		return JSONUtils.getObjectJSON(this);
	}

	/***
	 * @param channel channel
	 * @return the tenant name for this channel
	 */
	public String getTanantNameByChannel(String channel) {

		if (channelToTenantName.containsKey(channel)) {
			return channelToTenantName.get(channel);
		}

		return null;
	}

}
