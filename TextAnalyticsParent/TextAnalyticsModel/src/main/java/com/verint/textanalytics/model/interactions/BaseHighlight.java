package com.verint.textanalytics.model.interactions;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * The base class with common properties of highlighting.
 * 
 * @author NShunewich
 *
 */
@AllArgsConstructor
public class BaseHighlight {

	@Getter
	@Setter
	@Accessors(chain = true)
	protected int starts;

	@Getter
	@Setter
	@Accessors(chain = true)
	protected int ends;

	@Setter
	@Getter
	private List<HighlightContent> contents;

	/*@Setter
	@Getter
	private Integer sentimentHighlight;*/

	/**
	 * BaseHighlight constructor.
	 */
	public BaseHighlight() {
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ends;
		result = prime * result + starts;
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
		BaseHighlight other = (BaseHighlight) obj;
		if (ends != other.ends)
			return false;
		if (starts != other.starts)
			return false;
		return true;
	}

}
