package com.verint.textanalytics.web.viewmodel;

import java.util.List;

import com.verint.textanalytics.model.interactions.CategoryTagging;
import com.verint.textanalytics.model.interactions.DynamicField;
import com.verint.textanalytics.model.interactions.SourceType;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Document as view model.
 * 
 * @author EZlotnik
 *
 */
public class InteractionPreview {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String id;

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
	private String language;

	@Getter
	@Setter
	@Accessors(chain = true)
	private SourceType sourceType;

	@Getter
	@Setter
	@Accessors(chain = true)
	private Long date;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int sentiment;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<DynamicField> dynamicFields;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<CategoryTagging> categories;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<Utterance> utterances;
}
