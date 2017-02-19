package com.verint.textanalytics.web.viewmodel;

import com.verint.textanalytics.model.interactions.SourceType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Document as view model.
 *
 * @author EZlotnik
 */
public class InteractionPreviewCIV {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String messageIdentifier;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String tenant;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String channel;

	@Getter
	@Setter
	@Accessors(chain = true)
	private SourceType interactionType;

	@Getter
	@Setter
	@Accessors(chain = true)
	private InteractionCIV interaction;

	//Here is the Data needed for the plugin

	@Getter
	@Setter
	@Accessors(chain = true)
	private HighlightCIVObject highlightObject;

	@Getter
	@Setter
	@Accessors(chain = true)
	private SentimentsCIV sentimentsObject;

	@Getter
	@Setter
	@Accessors(chain = true)
	private SnippetsCIV snippetObject;




}
