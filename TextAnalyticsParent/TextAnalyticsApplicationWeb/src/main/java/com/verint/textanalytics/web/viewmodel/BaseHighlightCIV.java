package com.verint.textanalytics.web.viewmodel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * The base class with common properties of highlighting.
 *
 * @author NShunewich
 */
@AllArgsConstructor
public class BaseHighlightCIV {

	@Getter
	@Setter
	@Accessors(chain = true)
	protected String utteranceId;

	@Getter
	@Setter
	@Accessors(chain = true)
	protected int start;

	@Getter
	@Setter
	@Accessors(chain = true)
	protected int end;

	@Getter
	@Setter
	@Accessors(chain = true)
	protected HighlightTypeCIV type;

	@Getter
	@Setter
	@Accessors(chain = true)
	protected String data;

	@Getter
	@Setter
	@Accessors(chain = true)
	protected HighlightTypeCIV originalType;

	@Getter
	@Setter
	@Accessors(chain = true)
	private SpeakerType utteranceSpeaker = SpeakerType.unknown;



	/*@Setter
	@Getter
	private Integer sentimentHighlight;*/

	/**
	 * BaseHighlight constructor.
	 */
	public BaseHighlightCIV() {
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + end;
		result = prime * result + start;
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
		BaseHighlightCIV other = (BaseHighlightCIV) obj;
		if (end != other.end)
			return false;
		if (start != other.start)
			return false;
		return true;
	}

}
