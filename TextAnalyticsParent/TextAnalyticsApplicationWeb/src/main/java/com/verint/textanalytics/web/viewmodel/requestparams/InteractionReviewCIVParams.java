package com.verint.textanalytics.web.viewmodel.requestparams;

import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import lombok.Getter;
import lombok.Setter;

/**
 * Wrapper for Search Document request.
 *
 * @author EZlotnik
 */
public class InteractionReviewCIVParams extends RestRequestParams {
	@Setter
	@Getter
	private String interactionId;


	@Getter
	@Setter
	private SearchInteractionsContext searchContext;

}
