package com.verint.textanalytics.web.viewmodel;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Created by yzanis on 29-Mar-16.
 */
public class SentimentsCIV {

	@Getter
	@Setter
	@Accessors(chain = true)
	protected String interactionId;

	@Getter
	@Setter
	@Accessors(chain = true)
	protected Integer sentiment;

	@Getter
	@Setter
	@Accessors(chain = true)
	protected List<SentimentUtteranceCIV> sentiments;
}
