package com.verint.textanalytics.model.interactions;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.verint.textanalytics.common.utils.StringUtils;

/**
 * Highlight.
 * 
 * @author imor
 *
 */
public class EntityHighlight extends BaseHighlight {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String topic;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<SentimentHighlight> sentiments;

	/**
	 * AllArgsConstructor.
	 * 
	 * @param topic
	 *            topic
	 * @param sentiments
	 *            sentiments
	 * @param starts
	 *            starts
	 * @param ends
	 *            ends
	 */
	public EntityHighlight(String topic, List<SentimentHighlight> sentiments, int starts, int ends) {
		super();

		this.topic = topic;
		this.sentiments = sentiments;
		this.starts = starts;
		this.ends = ends;
	}

	/**
	 * TermHighlight.
	 */
	public EntityHighlight() {
		super();
	}

	public String getKey() {
		return String.format("%d_%d", starts, ends);
	}

	/**
	 * Generates a list of entities from parent entity to current one. For
	 * example : from 3/Device/Iphone/Ipnone 5 -> [ 1/Device, 2/Device/Iphone,
	 * 3/Device/Iphone/Ipnone 5]
	 * @return array of topics
	 */
	public List<EntityHighlight> getEntitiesHierarchy() {

		List<EntityHighlight> entities = new ArrayList<EntityHighlight>();

		if (!StringUtils.isNullOrBlank(this.topic)) {

			StringBuilder topicName;
			EntityHighlight topicHighlightToAdd;

			String[] splitedTopic = this.topic.split("/");
			if (splitedTopic != null) {

				for (int i = 1; i < splitedTopic.length - 1; i++) {
					topicHighlightToAdd = new EntityHighlight();

					topicHighlightToAdd.setEnds(this.getEnds());
					topicHighlightToAdd.setStarts(this.getStarts());
					topicHighlightToAdd.setSentiments(this.getSentiments());

					topicName = new StringBuilder();

					for (int j = 1; j <= i; j++) {
						topicName.append("/");
						topicName.append(splitedTopic[j]);
					}

					topicHighlightToAdd.setTopic(topicName.toString());
					entities.add(topicHighlightToAdd);
				}
			}
		}

		return entities;
	}
}
