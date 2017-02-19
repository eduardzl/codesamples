package com.verint.textanalytics.model.security;

import java.util.ArrayList;
import java.util.List;

import com.verint.textanalytics.common.utils.JSONUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/***
 * 
 * @author yzanis
 *
 */
public class Tenant implements Cloneable {

	@Setter
	@Getter
	@Accessors(chain = true)
	private String displayName;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String id;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int emId;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<Channel> channels;

	/**
	 * @return Tenant
	 */
	public Tenant cloneWithoutChannels() {
		Tenant tenant = new Tenant();
		tenant.setEmId(this.getEmId());
		tenant.setDisplayName(this.getDisplayName());
		tenant.setId(this.getId());
		tenant.setChannels(new ArrayList<Channel>());
		return tenant;
	}

	@Override
	public String toString() {
		return JSONUtils.getObjectJSON(this);
	}
}