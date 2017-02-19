package com.verint.textanalytics.web.uiservices;

import com.verint.textanalytics.bl.applicationservices.SuggestionsService;
import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import com.verint.textanalytics.model.interactions.WeightedSuggestion;
import com.verint.textanalytics.web.viewmodel.SuggestionItem;
import com.verint.textanalytics.web.viewmodel.WeightedSuggestionsDataResult;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/*******************
 * 
 * @author yzanis
 *
 */
public class SuggestionsUIService extends BaseUIService {

	@Autowired
	private SuggestionsService suggestionsService;

	@Autowired
	private ViewModelConverter viewModelConverter;

	private Comparator<WeightedSuggestion> suggestionsWeightComparator;

	/**
	 * C'tor.
	 */
	public SuggestionsUIService() {
		super();

		this.suggestionsWeightComparator = (s1, s2) -> Double.compare(s2.getWeight(), s1.getWeight());
	}

	/**
	 * return the autocomplete suggestion of the searched string.
	 * @param i360FoundationToken
	 *            authentication token.
	 * @param channel
	 *            channel
	 * @param searchContext
	 *            current search
	 * @param searchPrefix
	 *            the searched value
	 * @return list of suggestions and the percentage
	 */
	public List<SuggestionItem> getAutoCompleteSuggestions(String i360FoundationToken, String channel, SearchInteractionsContext searchContext, String searchPrefix) {
		val userTenant = this.getTenantFromChannel(channel, i360FoundationToken);

		val rsSuggestions = suggestionsService.getAutoCompleteSuggestions(userTenant, channel, searchContext, searchPrefix);

		List<com.verint.textanalytics.web.viewmodel.SuggestionItem> result = viewModelConverter.convertToViewModelSuggestionItem(rsSuggestions);

		return result;
	}

	/**
	 * Generates suggestions from Solr suggestions.
	 * @param i360FoundationToken
	 *            authentication token
	 * @param channel
	 *            channel
	 * @param searchContext
	 *            search context
	 * @param searchQuery
	 *            query to suggest for
	 * @return list of suggestions
	 */
	public WeightedSuggestionsDataResult<WeightedSuggestion> getWeightedTermsSuggestions(String i360FoundationToken, String channel, SearchInteractionsContext searchContext, String searchQuery) {
		val suggestionsResult = new WeightedSuggestionsDataResult<WeightedSuggestion>();

		String userTenant = this.getTenantFromChannel(channel, i360FoundationToken);

		List<WeightedSuggestion> rsSuggestions = suggestionsService.getWeightedTermsSuggestions(userTenant, channel, searchContext, searchQuery);
		if (rsSuggestions != null) {
			// sort suggestions by weight descending

			List<WeightedSuggestion> suggestions = rsSuggestions.stream().sorted(this.suggestionsWeightComparator).collect(Collectors.toList());
			Double maxWeight = suggestions.get(0).getWeight();

			suggestionsResult.setData(suggestions);
			suggestionsResult.setMaxWeight(maxWeight);
		}

		return suggestionsResult;
	}
}