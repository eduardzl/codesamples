package com.verint.textanalytics.model.facets;

import java.util.Comparator;
import java.util.List;

import com.verint.textanalytics.model.analyze.MetricData;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.experimental.Accessors;

/**
 * FacetResultGroup.
 * 
 * @author imor
 *
 */
public class FacetResultGroup implements Comparator<FacetResultGroup>, Comparable<FacetResultGroup> {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String title;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String titleKey;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int count;

	@Getter
	@Setter
	@Accessors(chain = true)
	private double percentage;

	@Getter
	@Setter
	@Accessors(chain = true)
	private double correlationPercentage;

	@Getter
	@Setter
	private List<MetricData> metrics;

	/**
	 * Adds fie;d metric data to facet group.
	 * @param metricData
	 *            metric data
	 */
	public void addMetricData(MetricData metricData) {
		this.metrics.add(metricData);
	}

	@Override
	public int compare(FacetResultGroup o1, FacetResultGroup o2) {

		val diff = o1.getCount() - o2.getCount();
		if (diff == 0) {
			return o2.getTitle().compareTo(o1.getTitle());
		}
		return diff;
	}

	@Override
	public int compareTo(FacetResultGroup o) {

		return compare(this, o);
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof FacetResultGroup)) {
			return false;
		}

		return this.compareTo((FacetResultGroup) obj) == 0;

	}

	@Override
	public int hashCode() {
		int hash = 3;
		final int hashPrefix = 53;

		hash = hashPrefix * hash + (this.title != null ? this.title.hashCode() : 0);
		hash = hashPrefix * hash + (this.titleKey != null ? this.titleKey.hashCode() : 0);
		return hash;
	}
}
