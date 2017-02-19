package com.verint.textanalytics.bl.applicationservices;

import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.model.documentSchema.FieldDataType;
import com.verint.textanalytics.model.interactions.FilterField;
import com.verint.textanalytics.model.interactions.FilterFieldValue;
import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TBaum on 7/12/2016.
 */
public class SampleFilterService {

	@Getter
	@Setter
	@Autowired
	private ConfigurationManager configurationManager;

	private final float percentageBlocks = 0.0625f;


	/**
	 * Invokes request to Darwin data access.
	 *
	 * @param context               search context to update
	 * @param interactionsCount     interaction count for this context
	 * @return true if sample filter applyed
	 */
	public boolean addSampleFilter(SearchInteractionsContext context, Integer interactionsCount) {

		int sampleThreshold = configurationManager.getApplicationConfiguration().getSampleSizeThreshold();

		if (interactionsCount <= sampleThreshold) {
			// should not add sample filter
			return false;
		}

		FilterField sampleFilter = new FilterField();
		sampleFilter.setName(TAConstants.SchemaFieldNames.uuid);
		sampleFilter.setDataType(FieldDataType.Text);

		int filtersToAdd = uuidSampleFiltersAmounts(sampleThreshold, interactionsCount);
		FilterFieldValue[] filterFieldsValues = new FilterFieldValue[filtersToAdd];

		for (int i = 0; i < filtersToAdd; i++) {
			filterFieldsValues[i] = new FilterFieldValue(Integer.toHexString(i) + "*");
		}

		sampleFilter.setValues(filterFieldsValues);

		List<FilterField> filterFields = context.getFilterFields();
		if (filterFields == null) {
			filterFields = new ArrayList<>();
			context.setFilterFields(filterFields);
		}
		context.getFilterFields().add(sampleFilter);

		return true;

	}

	private int uuidSampleFiltersAmounts(int maxResultsInSample, int totlaInteractionsInQuery) {
		// every uuid character is 1/16 == 6.25%
		// need to decide how many 6.25% to add to the query so the filter will provide max of maxResultsInSample

		float perc = ((float) maxResultsInSample / (float) totlaInteractionsInQuery);

		return Math.max(1, Math.round(perc / percentageBlocks));

	}


}
