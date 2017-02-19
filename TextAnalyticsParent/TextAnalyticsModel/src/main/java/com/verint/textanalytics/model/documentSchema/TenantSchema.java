package com.verint.textanalytics.model.documentSchema;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Tenant.
 * 
 * @author imor
 *
 */
public class TenantSchema {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String name;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<ChannelSchema> channels;

	/**
	 * C'tor.
	 */
	public TenantSchema() {
		this.channels = new ArrayList<>();
	}

	/**
	 * getChannel.
	 * 
	 * @param channelName
	 *            channelName
	 * @return Channel
	 */
	public ChannelSchema getChannel(String channelName) {
		for (ChannelSchema channel : channels) {
			if (channel.getName()
			           .equals(channelName)) {
				return channel;
			}
		}
		return null;
	}

	/**
	 * Adds channel schema.
	 * @param channelSchema
	 *            schema of channel
	 */
	public void addChannelSchema(ChannelSchema channelSchema) {
		this.channels.add(channelSchema);
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof TenantSchema)) {
			return false;
		}

		return this.getName()
		           .equals(((TenantSchema) obj).getName());

	}

	@Override
	public int hashCode() {
		int hash = 3;
		final int hashPrefix = 53;

		hash = hashPrefix * hash + (this.name != null ? this.name.hashCode() : 0);
		return hash;
	}
}
