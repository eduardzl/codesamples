package com.verint.textanalytics.web.viewmodel.requestparams;

import com.verint.textanalytics.model.facets.TextElementType;
import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import lombok.Getter;
import lombok.Setter;

/**
 * Text Element request.
 * Created by EZlotnik on 2/4/2016.
 */
public class TextElementMetricsParams {
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
	private TextElementType textElementType;

	@Getter
	@Setter
	private String textElementValue;

	@Getter
	@Setter
	private String textElementName;

	/***
	 * C'tor.
	 */
	public TextElementMetricsParams() {
		
	}
}
