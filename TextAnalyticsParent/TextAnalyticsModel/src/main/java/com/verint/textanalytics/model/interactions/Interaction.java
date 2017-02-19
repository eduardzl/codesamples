package com.verint.textanalytics.model.interactions;

import org.joda.time.*;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author EZlotnik Describes a document stored in index.
 */
public class Interaction {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String id;

	@Getter
	@Setter
	@Accessors(chain = true)
	private DocumentContentType contentType;

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
	private SourceType sourceType;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String language;

	@Getter
	@Setter
	@Accessors(chain = true)
	private DateTime startTime;

	@Getter
	@Setter
	@Accessors(chain = true)
	private DateTime agentLocalStartTime;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String agentTimeZone;

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
	private int numberOfRobotMessages;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int agentAvgResponseTime;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<String> customerNames;

	@Getter
	@Setter
	@Accessors(chain = true)
	private DateTime customerLocalStartTime;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String customerTimeZone;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int customerAvgResponseTime;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int customerMessagesCount;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int messagesCount;

	@Getter
	@Setter
	@Accessors(chain = true)
	private Double relevancyScore;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int handleTime;

	@Getter
	@Setter
	@Accessors(chain = true)
	private SentimentType sentiment;

	@Getter
	@Setter
	@Accessors(chain = true)
	private Boolean isSentimentMixed;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<CategoryTagging> categories;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<DynamicField> dynamicFields;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<DynamicField> sourceSpecificFields;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<Utterance> utterances;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String subject;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<Snippet> snippets;

	/**
	 * Adds utterance.
	 * @param utterance utterance to add
	 */
	public void addUtterance(Utterance utterance) {
		if (this.utterances == null) {
			this.utterances = new ArrayList<>();
		}

		this.utterances.add(utterance);
	}
}
