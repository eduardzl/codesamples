package com.verint.textanalytics.model.security;

import java.util.*;

import com.verint.textanalytics.common.utils.JSONUtils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.verint.textanalytics.model.documentSchema.*;
import com.verint.textanalytics.model.interactions.SourceType;

/**
 * Represents a tenant's channel.
 * @author EZlotnik
 *
 */
@NoArgsConstructor
public class Channel {

	@Setter
	@Getter
	@Accessors(chain = true)
	private String id;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String displayName;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int emId;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<TextSchemaField> dynamicFields;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<SourceType> sourceTypes;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<TextSchemaField> sourceTypeSpecificFields;

	/**
	 * Constructor.
	 * @param channelName
	 *            name of channel
	 */
	public Channel(String channelName) {
		this.id = channelName;
		this.displayName = channelName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + emId;
		result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Channel other = (Channel) obj;
		if (emId == other.emId)
			return true;

		return false;
	}

	@Override
	public String toString() {
		return JSONUtils.getObjectJSON(this);
	}

}
