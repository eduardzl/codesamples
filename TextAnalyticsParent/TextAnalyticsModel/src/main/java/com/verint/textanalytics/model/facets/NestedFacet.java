package com.verint.textanalytics.model.facets;

import com.verint.textanalytics.common.utils.CollectionUtils;
import com.verint.textanalytics.common.utils.StringUtils;
import com.verint.textanalytics.model.documentSchema.DocumentHierarchyType;
import com.verint.textanalytics.model.documentSchema.FieldDataType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by EZlotnik on 2/4/2016.
 */
@Accessors(chain = true)
public abstract class NestedFacet {
	private static final String statQueryFormat = "%s : \"%s(%s)\", %s";

	protected String brace = "}";
	protected String nl;
	protected String tab;
	protected String tab2;
	protected String tab3;
	protected String tab4;
	protected String tab5;

	@Getter
	@Setter
	protected FieldDataType fieldDataType;

	@Getter
	@Setter
	protected String alias;

	@Getter
	@Setter
	protected String fieldName;

	@Getter
	@Setter
	protected String prefix;

	@Getter
	@Setter
	protected Boolean addExcludeTag = false;

	@Getter
	@Setter
	protected String displayFieldName;

	@Getter
	@Setter
	protected FacetQueryType queryType;

	@Getter
	@Setter
	protected FacetMethodType methodType;

	@Getter
	@Setter
	protected String sortBy;

	@Getter
	@Setter
	protected String sortDirection;

	@Getter
	@Setter
	protected Integer limit = 0;

	@Getter
	@Setter
	protected Integer minCount = 1;

	@Getter
	protected List<FacetQueryStat> facetStats;

	@Getter
	@Setter
	protected List<FacetQueryStat> innerFacetStats;

	@Getter
	@Setter
	protected DocumentHierarchyType relationType;

	/**
	 * Constructor accepted facet type.
	 * @param queryType type of facet
	 */
	public NestedFacet(FacetQueryType queryType) {
		this.queryType = queryType;
		this.facetStats = new ArrayList<>();
	}

	/**
	 * Generates nested facet JSON.
	 * @param jsonQuery StringBuilder to add json to
	 * @param prettyFormat should pretty format
	 * @param isLast is the facet last one
	 */
	public abstract void toJsonStrig(StringBuilder jsonQuery, Boolean prettyFormat, Boolean isLast);

	/**
	 * Add Stat to be calculated for each bucket.
	 * @param stat statistic to be calculated
	 * @return object itself
	 */
	public NestedFacet addFacetStat(FacetQueryStat stat) {
		this.facetStats.add(stat);
		return this;
	}

	/**
	 * Add inner statistic to be calculated for each bucket.
	 * @param stat statistic to be calculated
	 * @return object itself
	 */
	public NestedFacet addInnerFacetStat(FacetQueryStat stat) {
		this.innerFacetStats.add(stat);
		return this;
	}

	protected void addFacetCommon(StringBuilder queryJson, Boolean prettyFormat, Boolean statsToCals) {
		this.addjustTabsSize(prettyFormat);

		queryJson.append(tab + this.alias + ": {" + nl);

		// facet type
		queryJson.append(tab2 + String.format("type : %s, %s", this.queryType, nl));

		// method type
		if (this.methodType != null) {
			queryJson.append(tab2 + String.format("method : %s, %s", this.methodType, nl));
		}

		// field name
		if (this.fieldName != null) {
			queryJson.append(tab2 + String.format("field : %s, %s", this.fieldName, nl));
		}

		if (sortBy != null && sortDirection != null) {
			queryJson.append(tab2 + String.format("sort : { %s : %s}, %s", sortBy, sortDirection, nl));
		}

		if (addExcludeTag) {
			queryJson.append(tab2 + String.format("excludeTags : \"%s\", %s", getFieldTagNameforExclusion(this.fieldName), nl));
		}

		// prefix to filter out buckets
		if (!StringUtils.isNullOrBlank(this.prefix)) {
			queryJson.append(tab2 + String.format("prefix : %s, %s", this.prefix, nl));
		}

		if (this.minCount != 0) {
			queryJson.append(tab2 + String.format("mincount : %d, %s", this.minCount, nl));
		}

		// if specific value was specified for limit
		if (statsToCals) {
			queryJson.append(tab2 + String.format("limit : %d, %s", this.limit, nl));
		} else {
			queryJson.append(tab2 + String.format("limit : %d %s", this.limit, nl));
		}
	}

	protected  void addFacetStats(StringBuilder queryJson) {
		if (!CollectionUtils.isEmpty(this.facetStats) || !CollectionUtils.isEmpty(this.innerFacetStats)) {

			queryJson.append(tab2 + "facet : { " + nl);

			// add expression for each statistic to calculate
			FacetQueryStat queryStat;
			int i;
			for (i = 0; i < this.facetStats.size(); i++) {
				queryStat = this.facetStats.get(i);
				if (this.innerFacetStats != null && this.innerFacetStats.size() > 0) {
					// with comma
					queryJson.append(tab3 + String.format(statQueryFormat, queryStat.getAlias(), queryStat.getFunction(), queryStat.getField(), nl));
				} else {
					if (i != this.facetStats.size() - 1) {
						// with comma
						queryJson.append(tab3 + String.format(statQueryFormat, queryStat.getAlias(), queryStat.getFunction(), queryStat.getField(), nl));
					} else {
						// without comma
						queryJson.append(tab3 + String.format("%s : \"%s(%s)\" %s", queryStat.getAlias(), queryStat.getFunction(), queryStat.getField(), nl));
					}
				}
			}

			// inner Facet Stats
			if (this.innerFacetStats != null && this.innerFacetStats.size() > 0) {
				queryJson.append(tab3 + "InnerFacetStats :  {" + nl);
				queryJson.append(tab4 + "\"type\" : \"terms\"," + nl);
				queryJson.append(tab4 + "\"field\" : \"content_type\"," + nl);
				queryJson.append(tab4 + "\"domain\" : {" + nl);

				if (this.getRelationType() != null && this.getRelationType().equals(DocumentHierarchyType.Interaction)) {
					queryJson.append(tab5 + "\"blockParent\" : \"content_type: CHILD\"" + nl);
				} else {
					queryJson.append(tab5 + "\"blockParent\" : \"content_type: PARENT\"" + nl);
				}

				queryJson.append(tab4 + "}," + nl);
				queryJson.append(tab4 + "\"facet\" : { " + nl);

				// add expression for each inner statistic to calculate
				for (i = 0; i < this.innerFacetStats.size(); i++) {
					queryStat = this.innerFacetStats.get(i);
					if (i < this.innerFacetStats.size() - 1) {
						// with comma
						queryJson.append(tab5 + String.format(statQueryFormat, queryStat.getAlias(), queryStat.getFunction(), queryStat.getField(), nl));
					} else {
						// without comma
						queryJson.append(tab5 + String.format("%s : \"%s(%s)\" %s", queryStat.getAlias(), queryStat.getFunction(), queryStat.getField(), nl));
					}
				}
				queryJson.append(tab4 + brace + nl);
				queryJson.append(tab3 + brace + nl);
			}
			queryJson.append(tab2 + "}" + nl);
		}
	}

	/**
	 * Generates exclusion tag.
	 * @param fieldName
	 *            field name
	 * @return name of tag
	 */
	public static String getFieldTagNameforExclusion(String fieldName) {
		return String.format("tagFor%s", fieldName);
	}

	private void addjustTabsSize(Boolean prettyFormat) {
		this.nl = prettyFormat ? System.getProperty("line.separator") : "";
		this.tab = prettyFormat ? "\t" : "";
		this.tab2 = prettyFormat ? "\t\t" : "";
		this.tab3 = prettyFormat ? "\t\t\t" : "";
		this.tab4 = prettyFormat ? "\t\t\t\t" : "";
		this.tab5 = prettyFormat ? "\t\t\t\t\t" : "";
	}
}
