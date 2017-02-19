package com.verint.textanalytics.model.interactions;

import java.util.*;

import com.verint.textanalytics.model.documentSchema.DocumentHierarchyType;
import com.verint.textanalytics.model.documentSchema.FieldDataType;

import lombok.Getter;
import lombok.Setter;

/**
 * @author EZlotnik Represents a range filter on specific field.
 */
public class RangeFilterField {

	@Getter
	@Setter
	private String name;

	@Getter
	@Setter
	private FieldDataType dataType;

	@Getter
	@Setter
	private List<Range> ranges;
}
