package com.verint.textanalytics.model.facets;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.verint.textanalytics.common.utils.StringUtils;
import com.verint.textanalytics.model.analyze.MetricData;

/**
 * Represents Entity node in Entities facet.
 * @author EZlotnik
 *
 */
@Accessors(chain = true)
public class EntityFacetNode {

	@Getter
	@Setter
	private String value;

	@Getter
	@Setter
	private String name;

	@Getter
	@Setter
	private double percentage;

	@Getter
	@Setter
	private int numberOfInteractions;

	@Getter
	@Setter
	private List<MetricData> metrics;

	@Getter
	@Setter
	private List<EntityFacetNode> children;

	/**
	 * Constructor.
	 */
	private EntityFacetNode() {
		this.children = new ArrayList<EntityFacetNode>();
		this.metrics = new ArrayList<MetricData>();
	}

	/**
	 * Adds child node.
	 * @param childNode
	 *            node to add
	 */
	public void addChild(EntityFacetNode childNode) {
		this.children.add(childNode);
	}

	/**
	 * Builder method.
	 * @param pathString
	 *            entity path
	 * @return generated entity node.
	 */
	public static EntityFacetNode buildFromPathString(String pathString) {
		EntityFacetNode entity = null;
		if (!StringUtils.isNullOrBlank(pathString)) {
			String[] pathTokens = pathString.split("/");

			entity = new EntityFacetNode();
			entity.setValue(pathString);
			entity.setName(pathTokens[pathTokens.length - 1]);
		}

		return entity;
	}

}
