package com.verint.textanalytics.model.interactions;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by EZlotnik on 3/22/2016.
 */
public class HighlightResult {
	@Getter
	@Setter
	private List<Utterance> utterances;

	@Getter
	@Setter
	private List<UtteranceHighlights> highlights;
}
