package com.verint.textanalytics.web.viewmodel;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.verint.textanalytics.common.utils.CollectionUtils;
import lombok.Getter;
import lombok.Setter;

/**
 * @author imor View Model class representing a node in Entities Facet Tree Map
 *         Result.
 */
public class TextElementFacetTreeMapResult {

	@Getter
	@Setter
	private Map<Integer, Collection<MetricLimitsData>> metricsLimitsData;

	@Getter
	@Setter
	private List<TextElementFacetTreeMapNode> textElementFacetTreeMapNodes;


	public Boolean isEmpty() {
		return CollectionUtils.isEmpty(textElementFacetTreeMapNodes);
	}
}
