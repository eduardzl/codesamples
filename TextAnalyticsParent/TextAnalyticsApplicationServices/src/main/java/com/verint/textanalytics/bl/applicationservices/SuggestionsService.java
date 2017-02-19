package com.verint.textanalytics.bl.applicationservices;

import com.google.common.base.Throwables;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionErrorCode;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionException;
import com.verint.textanalytics.common.utils.StringUtils;
import com.verint.textanalytics.dal.darwin.TextAnalyticsProvider;
import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import com.verint.textanalytics.model.interactions.SearchSuggestion;
import com.verint.textanalytics.model.interactions.SearchSuggestionResult;
import com.verint.textanalytics.model.interactions.WeightedSuggestion;
import lombok.Setter;
import lombok.val;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 
 * @author yzanis
 *
 */
public class SuggestionsService extends ApplicationService {
	@Autowired
	private TextAnalyticsProvider textAnalyticsProvider;

	@Autowired
	private ConfigurationManager configurationManager;

	@Autowired
	private SampleFilterService sampleFilterService;

	@Autowired
	@Setter
	private ConfigurationService configurationService;

	/** Generates terms suggestions list.
	 * @param tenant        tenant
	 * @param channel       channel
	 * @param searchContext current search
	 * @param searchPrefix  the prefix of the searched string from the client
	 * @return returns the list of suggestions for the prefix and the number of
	 * occurrences
	 */
	public SearchSuggestionResult getAutoCompleteSuggestions(String tenant, String channel, SearchInteractionsContext searchContext, String searchPrefix) {
		val suggestionResult = new SearchSuggestionResult();

		if (!StringUtils.isNullOrBlank(searchPrefix)) {

			try {
				String requestId = ThreadContext.get(TAConstants.requestId);

				String language = configurationService.getChannelLanguage(tenant, channel);

				SearchInteractionsContext clonedSearchInteractionsContext = searchContext.cloneMe();

				int totalInteractions =  textAnalyticsProvider.getResultSetInteractionsQuantity(tenant, channel, clonedSearchInteractionsContext, language);

				List<SearchSuggestion> suggestionsForAutocomplete = null;

				boolean isSampleAdded = this.sampleFilterService.addSampleFilter(clonedSearchInteractionsContext, totalInteractions);

				if (isSampleAdded) {
					// update interaction # with sample filter, so percentage values will be correct
					totalInteractions =  textAnalyticsProvider.getResultSetInteractionsQuantity(tenant, channel, clonedSearchInteractionsContext, language);
				}

				suggestionsForAutocomplete = textAnalyticsProvider.getTermsAutoCompleteSuggestions(tenant, channel, clonedSearchInteractionsContext, searchPrefix, language);

				suggestionResult.setBasedOnSample(isSampleAdded);
				suggestionResult.setSuggestions(suggestionsForAutocomplete);
				suggestionResult.setTotalNumberFound(totalInteractions);

			} catch (Exception ex) {
				Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
				Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.TextQueryExecutionError));
			}
		} else {
			throw new TextQueryExecutionException(TextQueryExecutionErrorCode.EmptyAutoCompletePrefix);
		}

		return suggestionResult;
	}

	/**
	 * @param tenant        tenant
	 * @param channel       channel
	 * @param searchContext current search
	 * @param searchPrefix  the prefix of the searched string from the client
	 * @return returns the list of suggestions for the prefix and the number of
	 * occurrences
	 */
	public List<WeightedSuggestion> getWeightedTermsSuggestions(String tenant, String channel, SearchInteractionsContext searchContext, String searchPrefix) {

		List<WeightedSuggestion> suggestions = null;

		String language = configurationService.getChannelLanguage(tenant, channel);

		suggestions = textAnalyticsProvider.getFreeTextLookupSuggestions(tenant, channel, searchContext, searchPrefix, language);
		if (suggestions != null) {
			Map<String, Double> suggestionsMap = suggestions.stream().collect(Collectors.groupingBy(WeightedSuggestion::getText,
			                                                                                        Collectors.averagingDouble(WeightedSuggestion::getWeight)));

			suggestions = new ArrayList<WeightedSuggestion>();
			for (String suggestionTerm : suggestionsMap.keySet()) {
				suggestions.add(new WeightedSuggestion(suggestionTerm, suggestionsMap.get(suggestionTerm)));
			}
		}

		return suggestions;
	}
}