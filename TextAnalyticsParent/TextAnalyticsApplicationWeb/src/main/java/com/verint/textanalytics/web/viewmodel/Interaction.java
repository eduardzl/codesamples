package com.verint.textanalytics.web.viewmodel;

import java.util.List;

import com.verint.textanalytics.model.interactions.CategoryTagging;
import com.verint.textanalytics.model.interactions.DynamicField;
import com.verint.textanalytics.model.interactions.Snippet;
import com.verint.textanalytics.model.interactions.TermHighlight;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Document as view model.
 * 
 * @author EZlotnik
 *
 */
public class Interaction {

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
	private String sourceType;

	@Getter
	@Setter
	@Accessors(chain = true)
	private Long startTimeTicks;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String startTime;

	@Getter
	@Setter
	@Accessors(chain = true)
	private Long agentLocalStartTimeTicks;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String agentLocalStartTime;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<String> agentNames;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int agentMessagesCount;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int agentAvgResponseTime;

	@Getter
	@Setter
	@Accessors(chain = true)
	private Long customerLocalStartTimeTicks;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String customerLocalStartTime;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<String> customerNames;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String subject;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int customerMessagesCount;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int customerAvgResponseTime;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int numberOfRobotMessages;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int messagesCount;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int handleTime;

	@Getter
	@Setter
	@Accessors(chain = true)
	private Double relevancyScore;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int sentiment;

	@Getter
	@Setter
	@Accessors(chain = true)
	private Boolean isSentimentMixed;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<DynamicField> dynamicFields;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<DynamicField> sourceTypeSpecificFields;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<TermHighlight> highlights;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<CategoryTagging> categories;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<Snippet> snippets;
}