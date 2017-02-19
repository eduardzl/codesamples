package com.verint.textanalytics.model.interactions;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
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
@Data
@EqualsAndHashCode(callSuper = true)
public class RelationHighlight extends BaseHighlight {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String relation;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<SentimentHighlight> sentiments;

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<Position> positions;

	/**
	 * AllArgsConstructor.
	 * 
	 * @param relation
	 *            relation
	 * @param sentiments
	 *            sentiments
	 * @param starts
	 *            starts
	 * @param ends
	 *            ends
	 */
	public RelationHighlight(String relation, List<SentimentHighlight> sentiments, int starts, int ends) {
		super();

		this.relation = relation;
		this.sentiments = sentiments;
		this.starts = starts;
		this.ends = ends;
	}

	/**
	 * TermHighlight.
	 */
	public RelationHighlight() {
		super();
	}

	public String getKey() {
		return String.format("%d_%d", starts, ends);
	}

	/**
	 * Generates a list of relations from current relation using the path from
	 * top level parent till current one. For example : from
	 * 3/Device/Iphone/Ipnone 5 -> [ 1/Device, 2/Device/Iphone,
	 * 3/Device/Iphone/Ipnone 5]
	 * @return array of relations
	 */
	public List<RelationHighlight> getRelationHierarchy() {

		List<RelationHighlight> relations = new ArrayList<RelationHighlight>();

		if (!StringUtils.isNullOrBlank(this.relation)) {
			String[] splitedRelation = this.relation.split("/");

			if (splitedRelation != null) {
				RelationHighlight relationHighlightToAdd;
				StringBuilder relationName;

				for (int i = 1; i < splitedRelation.length - 1; i++) {
					relationHighlightToAdd = new RelationHighlight();

					relationHighlightToAdd.setEnds(this.getEnds());
					relationHighlightToAdd.setStarts(this.getStarts());
					relationHighlightToAdd.setSentiments(this.getSentiments());
					relationName = new StringBuilder();

					for (int j = 1; j <= i; j++) {
						relationName.append("/");
						relationName.append(splitedRelation[j]);
					}
					relationHighlightToAdd.setRelation(relationName.toString());
					relations.add(relationHighlightToAdd);
				}
			}
		}

		return relations;
	}
}
