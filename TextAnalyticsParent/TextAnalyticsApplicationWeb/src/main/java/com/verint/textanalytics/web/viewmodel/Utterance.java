package com.verint.textanalytics.web.viewmodel;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.verint.textanalytics.model.interactions.*;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author EZlotnik Describes a document stored in index
 */
public class Utterance {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String id;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String parentId;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<DynamicField> documentDynamicFields;

	@Getter
	@Setter
	@Accessors(chain = true)
	private SpeakerType speakerType = SpeakerType.unknown;

	@Getter
	@Setter
	@Accessors(chain = true)
	private DocumentContentType contentType;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<Entity> entities;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<Relation> relations;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<KeyTerm> keyterms;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String text;

	@Getter
	@Setter
	@Accessors(chain = true)
	private Long date;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<BaseHighlight> highlights;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String language;
}
