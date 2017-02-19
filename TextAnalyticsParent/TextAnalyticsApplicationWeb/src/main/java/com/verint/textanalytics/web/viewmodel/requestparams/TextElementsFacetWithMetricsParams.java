package com.verint.textanalytics.web.viewmodel.requestparams;

import com.verint.textanalytics.model.facets.SpeakerQueryType;
import com.verint.textanalytics.model.facets.TextElementMetricType;
import com.verint.textanalytics.model.facets.TextElementsFacetNode;
import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Text Element Facet with metrics request parameters.
 * Created by EZlotnik on 2/4/2016.
 */
public class TextElementsFacetWithMetricsParams {
	@Getter
	@Setter
	private String channel;

	@Getter
	@Setter
	private SearchInteractionsContext searchContext;

	@Getter
	@Setter
	private SearchInteractionsContext backgroundContext;

	@Getter
	@Setter
	private int hierarchyLevelNumber;

	@Getter
	@Setter
	private List<TextElementsFacetNode> textElements;

	@Getter
	@Setter
	private TextElementMetricType sizeByMetric;

	@Getter
	@Setter
	private TextElementMetricType colorByMetric;

	@Getter
	@Setter
	private SpeakerQueryType querySpeaker;

	@Getter
	@Setter
	private Boolean queryOnSameUtterance;

	@Getter
	@Setter
	private Boolean leavesOnly;

	/***
	 * C'tor.
	 */
	public TextElementsFacetWithMetricsParams() {
		this.queryOnSameUtterance = false;
		this.leavesOnly = false;
	}
}
