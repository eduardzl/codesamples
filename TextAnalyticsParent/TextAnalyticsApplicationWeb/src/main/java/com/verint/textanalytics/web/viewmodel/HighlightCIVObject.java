package com.verint.textanalytics.web.viewmodel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * The base class with common properties of highlighting.
 *
 * @author NShunewich
 */
@AllArgsConstructor
public class HighlightCIVObject {

	@Getter
	@Setter
	@Accessors(chain = true)
	protected String interactionId;

	@Getter
	@Setter
	@Accessors(chain = true)
	protected List<BaseHighlightCIV> highlights;


	/*@Setter
	@Getter
	private Integer sentimentHighlight;*/

	/**
	 * BaseHighlight constructor.
	 */
	public HighlightCIVObject() {
	}

}
