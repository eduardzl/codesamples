package com.verint.textanalytics.model.facets;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.verint.textanalytics.common.utils.StringUtils;
import com.verint.textanalytics.model.documentSchema.DocumentHierarchyType;

/**
 * FieldFacetQuery.
 * 
 * @author imor
 *
 */
@Accessors(chain = true)
public class FacetQuery {
	private static final String statQueryFormat = "%s : \"%s(%s)\"";

	@Getter
	@Setter
	private String globalStatsAlias;

	@Getter
	@Setter
	private List<FacetQueryStat> globalStats;

	@Getter
	private List<NestedFacet>  nestedFacets;

	@Getter
	@Setter
	private int offset = 0;


	/**
	 * Constructor.
	 */
	public FacetQuery() {
		this.globalStats = new ArrayList<>();
		this.nestedFacets = new ArrayList<>();
	}

	/**
	 * Adds a nested facet to global facet query.
	 * @param facet nested faced to add
	 */
	public void addNestedFacet(NestedFacet facet) {
		this.nestedFacets.add(facet);
	}

	/**
	 * Add statistics to be calculated for entire result set without breaking to
	 * buckets.
	 * @param stat
	 *            statistic expression to added to global stats
	 * @return object itself
	 */
	public FacetQuery addGlobalStat(FacetQueryStat stat) {
		this.globalStats.add(stat);
		return this;
	}

	/**
	 * Generates a JSON representing the facet. According to Solr 5 JSON Facet
	 * API.
	 * @param wrap
	 *            true to create wrapping of the JSON.
	 * @param prettyFormat
	 *            should new lines and tab be added to facet json
	 * @return JSON string representing the facet object.
	 */
	public String toJsonString(boolean wrap, boolean prettyFormat) {
		String nl = prettyFormat ? System.getProperty("line.separator") : "";
		String tab = prettyFormat ? "\t" : "";
		String brace = "}";

		StringBuilder queryJson = new StringBuilder();

		if (wrap) {
			queryJson.append(nl + "{" + nl);
		}

		if (this.globalStats.size() > 0) {
			Boolean isLastGlobalStat = false;
			FacetQueryStat queryStat;

			queryJson.append(this.globalStatsAlias + " : {" + nl);
			// add expression for each statistic to calculate
			for (int j = 0; j < this.globalStats.size(); j++) {
				queryStat = this.globalStats.get(j);
				isLastGlobalStat = (j == this.globalStats.size() - 1);

				if (!isLastGlobalStat) {
					queryJson.append(tab + String.format(statQueryFormat, queryStat.getAlias(), queryStat.getFunction(), queryStat.getField()) + "," + nl);
				} else {
					queryJson.append(tab + String.format(statQueryFormat, queryStat.getAlias(), queryStat.getFunction(), queryStat.getField()) +  nl);
				}
			}
			queryJson.append("}");
		}

		if (this.nestedFacets.size() > 0) {
			Boolean isLastNestedFacet = false;

			// if Global stats are present
			if (this.globalStats.size() > 0) {
				queryJson.append("," + nl);
			}

			for (int i = 0; i < this.nestedFacets.size(); i++) {
				isLastNestedFacet = (i == this.nestedFacets.size() - 1);
				this.nestedFacets.get(i).toJsonStrig(queryJson, prettyFormat, isLastNestedFacet);
			}
		}

		if (wrap) {
			queryJson.append(nl + brace);
		}

		return queryJson.toString();
	}
}
