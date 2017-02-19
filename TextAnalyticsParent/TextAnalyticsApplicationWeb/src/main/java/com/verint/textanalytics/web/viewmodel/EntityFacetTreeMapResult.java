package com.verint.textanalytics.web.viewmodel;

import java.util.HashMap;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * @author imor View Model class representing a node in Entities Facet Tree Map
 *         Result.
 */
public class EntityFacetTreeMapResult {

	@Getter
	@Setter
	private HashMap<Integer, List<MetricLimitsData>> metricsLimitsData;

	@Getter
	@Setter
	private List<EntityFacetTreeMapNode> entityFacetTreeMapNodes;
}
