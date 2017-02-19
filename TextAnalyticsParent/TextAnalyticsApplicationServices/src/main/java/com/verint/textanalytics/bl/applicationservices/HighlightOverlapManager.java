package com.verint.textanalytics.bl.applicationservices;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.verint.textanalytics.common.utils.CollectionUtils;
import com.verint.textanalytics.model.interactions.*;
import lombok.Getter;
import lombok.Setter;

import com.verint.textanalytics.bl.applicationservices.SearchInteractionsService.PositionType;

/**
 * Formats Highlights to prevent overlap.
 * 
 * @author NShunewich
 *
 */
public class HighlightOverlapManager {

	/**
	 * Describes the local positions.
	 *
	 * @author NShunewich
	 *
	 */
	public class Position {
		@Getter
		@Setter
		private int pos;

		@Getter
		@Setter
		private PositionType type;

		@Getter
		@Setter
		private List<BaseHighlight> highlights;

		/**
		 * Constructor.
		 *
		 * @param position
		 *            position of highlight.
		 * @param t
		 *            type of position.
		 */
		public Position(int position, PositionType t) {
			pos = position;
			type = t;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + pos;
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Position other = (Position) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (pos != other.pos)
				return false;
			if (type != other.type)
				return false;
			return true;
		}

		private HighlightOverlapManager getOuterType() {
			return HighlightOverlapManager.this;
		}
	}

	/**
	 * Parses interactions for solving overlapping.
	 * 
	 * @param interactions
	 *            List of interactions to operate
	 * 
	 */
	public void solveOverlappingForInteractions(List<Interaction> interactions) {

		if (!CollectionUtils.isEmpty(interactions)) {
			interactions.parallelStream().forEach(interaction -> {
				solveOverlppingForUtterances(interaction.getUtterances());
			});
		}
	}

	/**
	 * Parses highlights for each utterance to solve overlapping.
	 * 
	 * @param utterances
	 *            List of utterances
	 */
	public void solveOverlppingForUtterances(List<Utterance> utterances) {
		List<BaseHighlight> initialHighlights;
		List<BaseHighlight> solvedOvelapping;

		if (!CollectionUtils.isEmpty(utterances)) {

			for (Utterance utterance : utterances) {
				initialHighlights = utterance.getMergedHighlighting();

				if (!CollectionUtils.isEmpty(initialHighlights)) {
					solvedOvelapping = this.solveOverlappingForHighlights(initialHighlights);
					utterance.setMergedHighlighting(solvedOvelapping);
				}
			}
		}
	}

	/**
	 * Operates highlight.
	 * 
	 * @param highlights
	 *            The collection of highlights.
	 * @return resulted set.
	 */
	public List<BaseHighlight> solveOverlappingForHighlights(List<BaseHighlight> highlights) {
		List<Position> positions;

		positions = createPositionsList(highlights);

		List<BaseHighlight> resultedHighlights = buildNonOverlapped(positions, highlights);

		orderHighlightsByStartPosition(resultedHighlights);

		return resultedHighlights;
	}

	private List<Position> createPositionsList(List<BaseHighlight> highlights) {
		List<Position> positions = new ArrayList<HighlightOverlapManager.Position>();

		Position position;
		for (BaseHighlight h : highlights) {
			position = new Position(h.getStarts(), PositionType.START);
			if (!positions.contains(position)) {
				positions.add(position);
			}

			position = new Position(h.getEnds(), PositionType.END);
			if (!positions.contains(position)) {
				positions.add(position);
			}
		}

		positions.sort(new Comparator<Position>() {
			@Override
			public int compare(Position p1, Position p2) {
				return p1.getPos() - p2.getPos();
			}
		});

		return positions;
	}

	private List<BaseHighlight> buildNonOverlapped(List<Position> positions, List<BaseHighlight> highlights) {
		List<BaseHighlight> resultedHighlights = new ArrayList<BaseHighlight>();
		Position pStart, pEnd;
		int rangeStart, rangeEnd;
		PositionType rangeType;

		for (int i = 0; i < positions.size() - 1; i++) {

			pStart = positions.get(i);
			pEnd = positions.get(i + 1);

			rangeStart = positions.get(i).getPos();
			rangeEnd = positions.get(i + 1).getPos();
			rangeType = positions.get(i).getType();

			if (rangeStart == rangeEnd)
				continue;

			// if (rangeType == PositionType.END)
			// continue;

			List<BaseHighlight> highlightsInRange = getHighlightsByRange(rangeStart + 1, rangeEnd - 1, highlights);

			if (highlightsInRange.size() == 0)
				continue;

			BaseHighlight mergedHighlight = mergeInRangeHighlight(highlightsInRange, pStart, pEnd);

			resultedHighlights.add(mergedHighlight);
		}

		return resultedHighlights;
	}

	private List<BaseHighlight> getHighlightsByRange(int start, int end, List<BaseHighlight> highlights) {
		List<BaseHighlight> inRangeHighlights = new ArrayList<BaseHighlight>();

		for (BaseHighlight hl : highlights) {
			if (hl.getEnds() < start || hl.getStarts() > end)
				continue;

			inRangeHighlights.add(hl);
		}

		return inRangeHighlights;
	}

	private BaseHighlight mergeInRangeHighlight(List<BaseHighlight> inRangeHighlights, Position pStart, Position pEnd) {
		int start = pStart.getPos(), end = pEnd.getPos();
		MergedHighlight mergedHighlight = new MergedHighlight();

		// _start-start_____start-end_____________end-start_____________end-end
		// |*******|__________ACTUAL_______________SPACES______________|****|_____
		// ___|*******|___________________________________________________|****|__
		// s__s____e__e________________________________________________s__s_e__e__
		// |*||***||**|________________________________________________|*||*||*|__
		// s_es___es__e________________________________________________s_es_es_e__

		/*
		if (pStart.getType() == PositionType.START && pEnd.getType() == PositionType.START)
			end--;

		if (pStart.getType() == PositionType.END && pEnd.getType() == PositionType.END)
			start++;
		*/

		mergedHighlight.setStarts(start);
		mergedHighlight.setEnds(end);

		fillHighlightContent(mergedHighlight, inRangeHighlights);

		return mergedHighlight;
	}

	private void fillHighlightContent(MergedHighlight mergedHighlight, List<BaseHighlight> inRangeHighlights) {

		String data;
		HighlightContent content;

		mergedHighlight.setContents(new ArrayList<HighlightContent>());

		for (BaseHighlight hl : inRangeHighlights) {

			content = new HighlightContent();
			data = null;

			if (hl instanceof EntityHighlight) {
				EntityHighlight th = (EntityHighlight) hl;
				data = th.getTopic();
				content.setType(HighlightType.Entity);
			}
			if (hl instanceof RelationHighlight) {
				RelationHighlight rh = (RelationHighlight) hl;
				data = rh.getRelation();
				content.setType(HighlightType.Relation);
			}
			if (hl instanceof TermHighlight) {
				content.setType(HighlightType.Term);
				data = null;
			}
			if (hl instanceof SentimentHighlight) {
				content.setType(HighlightType.Sentiment);
				data = Integer.toString(((SentimentHighlight) hl).getValue());
			}
			if (hl instanceof KeyTermHighlight) {
				content.setType(HighlightType.KeyTerm);
				data = ((KeyTermHighlight) hl).getKeyterm();
			}

			content.setData(data);

			mergedHighlight.getContents().add(content);
		}
	}

	private void orderHighlightsByStartPosition(List<BaseHighlight> highlights) {
		highlights.sort(new Comparator<BaseHighlight>() {
			@Override
			public int compare(BaseHighlight o1, BaseHighlight o2) {
				return o1.getStarts() - o2.getStarts();
			};
		});

	}
}
