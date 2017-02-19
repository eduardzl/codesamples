package com.verint.textanalytics.model.analyze;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author imor
 *
 */
@AllArgsConstructor
@NoArgsConstructor
public class FieldMetric {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String name;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String fieldName;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String displayKey;

	@Getter
	@Setter
	@Accessors(chain = true)
	private MetricType type;

	@Getter
	@Setter
	@Accessors(chain = true)
	private StatFunction statFunction;

	@Getter
	@Setter
	@Accessors(chain = true)
	private boolean isInnerFacet;

	@Getter
	@Setter
	@Accessors(chain = true)
	private int index;
}
