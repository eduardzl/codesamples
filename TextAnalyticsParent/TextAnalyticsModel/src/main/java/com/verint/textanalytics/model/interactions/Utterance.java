package com.verint.textanalytics.model.interactions;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

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
	private SpeakerType speakerType = SpeakerType.Unknown;

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
	private List<TermHighlight> termsHighlighting;

	@Getter
	@Setter
	@Accessors(chain = true)
	private DateTime date;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<EntityHighlight> entitiesHighlighting;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<RelationHighlight> relationsHighlighting;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<KeyTermHighlight> keytermsHighlighting;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<EntityHighlight> allEntitiesHighlighting;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<RelationHighlight> allRelationsHighlighting;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<KeyTermHighlight> allKeytermsHighlighting;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<SentimentHighlight> sentimentHighlighting;

	@Getter
	@Setter
	private List<BaseHighlight> mergedHighlighting;

	@Getter
	@Setter
	@Accessors(chain = true)
	private SentimentType utteranceSentiment;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String language;

	/**
	 * @return List<RelationHighlight>
	 */
	public List<BaseHighlight> getRelationsHighlightingForeachPosition() {
		val res = new ArrayList<BaseHighlight>();
		RelationHighlight newRelationHighlight;
		SentimentHighlight newSentimentHighlight;
		if (relationsHighlighting != null) {
			for (RelationHighlight relationHighlighting : relationsHighlighting) {
				if (relationHighlighting.getPositions() != null) {
					for (Position position : relationHighlighting.getPositions()) {
						newRelationHighlight = new RelationHighlight();
						newRelationHighlight.setStarts(position.getStarts());
						newRelationHighlight.setEnds(position.getEnds());
						newRelationHighlight.setRelation(relationHighlighting.getRelation());
						newRelationHighlight.setSentiments(relationHighlighting.getSentiments());
						newRelationHighlight.setContents(relationHighlighting.getContents());
						if (!res.contains(newRelationHighlight)) {
							res.add(newRelationHighlight);

							// Create Sentiment Highlight for entities/topics on each position
							if (newRelationHighlight.getSentiments() != null) {
								for (SentimentHighlight sentiment : newRelationHighlight.getSentiments()) {

									if (sentiment.getPositions() != null) {
										for (Position sentimentPosition : sentiment.getPositions()) {
											newSentimentHighlight = new SentimentHighlight();
											newSentimentHighlight.setStarts(sentimentPosition.getStarts());
											newSentimentHighlight.setEnds(sentimentPosition.getEnds());
											newSentimentHighlight.setValue(sentiment.getValue());
											res.add(newSentimentHighlight);
										}
									}
								}
							}
						}
					}
				}
			}
		}

		return res;
	}

	/**
	 * getEntitiesHighlightingForeachPosition gets the sentiment from the enitites.
	 */
	public void getEntitiesHighlightingForeachPositionTest() {
		for (EntityHighlight entityHighlighting : entitiesHighlighting) {
			if (entityHighlighting.getSentiments() != null) {
				entityHighlighting.getSentiments();
			}
		}
	}

	/**
	 * @return List<EntityHighlight>
	 */
	public List<BaseHighlight> getEntitiesHighlightingForeachPosition() {
		val res = new ArrayList<BaseHighlight>();
		EntityHighlight newEntityHighlight;
		SentimentHighlight newSentimentHighlight;
		HighlightContent highlightContent;
		if (entitiesHighlighting != null) {
			for (EntityHighlight entityHighlighting : entitiesHighlighting) {

				// Create Entity Highlight
				newEntityHighlight = new EntityHighlight();
				newEntityHighlight.setStarts(entityHighlighting.getStarts());
				newEntityHighlight.setEnds(entityHighlighting.getEnds());
				newEntityHighlight.setContents(entityHighlighting.getContents());
				newEntityHighlight.setTopic(entityHighlighting.getTopic());
				res.add(newEntityHighlight);

				if (!res.contains(newEntityHighlight)) {
					res.add(newEntityHighlight);
				}

				// Create Sentiment Highlight for entities/topics on each position
				if (entityHighlighting.getSentiments() != null) {

					for (SentimentHighlight sentiment : entityHighlighting.getSentiments()) {
						if (sentiment.getPositions() != null) {
							for (Position position : sentiment.getPositions()) {
								newSentimentHighlight = new SentimentHighlight();
								newSentimentHighlight.setStarts(position.getStarts());
								newSentimentHighlight.setEnds(position.getEnds());
								newSentimentHighlight.setContents(new ArrayList<HighlightContent>());
								highlightContent = new HighlightContent();
								highlightContent.setType(HighlightType.Sentiment);
								newSentimentHighlight.getContents().add(highlightContent);
								newSentimentHighlight.setValue(sentiment.getValue());

								if (!res.contains(newSentimentHighlight)) {
									res.add(newSentimentHighlight);
								}
							}
						}
					}
				}
			}
		}

		return res;
	}

	/**
	 * @return List<RelationHighlight>
	 */
	public List<BaseHighlight> getKeytermsHighlightingForeachPosition() {
		val res = new ArrayList<BaseHighlight>();
		KeyTermHighlight newKeyTermHighlight;
		SentimentHighlight newSentimentHighlight;

		if (keytermsHighlighting != null) {
			for (KeyTermHighlight keyTermHighlighting : keytermsHighlighting) {

				// Create Entity Highlight
				newKeyTermHighlight = new KeyTermHighlight();
				newKeyTermHighlight.setStarts(keyTermHighlighting.getStarts());
				newKeyTermHighlight.setEnds(keyTermHighlighting.getEnds());
				newKeyTermHighlight.setContents(keyTermHighlighting.getContents());
				newKeyTermHighlight.setKeyterm(keyTermHighlighting.getKeyterm());
				res.add(newKeyTermHighlight);

				// Create Sentiment Highlight for entities/topics on each position
				if (keyTermHighlighting.getSentiments() != null) {
					for (SentimentHighlight sentiment : keyTermHighlighting.getSentiments()) {

						if (sentiment.getPositions() != null) {
							for (Position sentimentPosition : sentiment.getPositions()) {
								newSentimentHighlight = new SentimentHighlight();
								newSentimentHighlight.setStarts(sentimentPosition.getStarts());
								newSentimentHighlight.setEnds(sentimentPosition.getEnds());
								newSentimentHighlight.setValue(sentiment.getValue());
								res.add(newSentimentHighlight);
							}
						}
					}
				}
			}
		}
		return res;
	}

}
