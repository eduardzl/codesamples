package com.verint.textanalytics.model.interactions;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * SubDocumentHighlighting.
 * 
 * @author imor
 *
 */
public class UtteranceHighlights {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String documentId;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<TermHighlight> termHighlights;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<EntityHighlight> entitiesHighlights;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<RelationHighlight> relationsHighlights;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<KeyTermHighlight> keyTermsHighlights;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<SentimentHighlight> sentimentHighlights;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<EntityHighlight> allEntitiesHighlights;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<RelationHighlight> allRelationsHighlights;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<KeyTermHighlight> allKeyTermsHighlights;

	@Getter
	@Setter
	@Accessors(chain = true)
	private SpeakerType speakerType;
}
