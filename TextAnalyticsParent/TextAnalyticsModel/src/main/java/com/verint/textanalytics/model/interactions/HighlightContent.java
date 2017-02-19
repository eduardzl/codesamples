package com.verint.textanalytics.model.interactions;

import lombok.Getter;
import lombok.Setter;

/**
 * Describes the single content defined in the highlight. Used for generating
 * snippets.
 * 
 * @author NShunewich
 *
 */
public class HighlightContent {

	private static final int magicNumber31 = 31;

	@Getter
	@Setter
	private HighlightType type;

	@Getter
	@Setter
	private String data;

	@Override
	public int hashCode() {
		int result = type != null ? type.hashCode() : 0;
		result = magicNumber31 * result + (data != null ? data.hashCode() : 0);
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
		HighlightContent other = (HighlightContent) obj;
		if (other.type.name().equals(other.type.name()) == false)
			return false;
		if (getData() != null)
			if (!getData().equals(null) && !getData().equals(other.getData()))
				return false;

		return true;
	}
}
