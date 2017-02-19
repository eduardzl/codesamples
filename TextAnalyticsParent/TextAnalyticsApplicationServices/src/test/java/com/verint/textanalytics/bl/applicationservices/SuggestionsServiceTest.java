package com.verint.textanalytics.bl.applicationservices;

import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.dal.darwin.TextAnalyticsProvider;
import com.verint.textanalytics.model.interactions.SearchSuggestion;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

import static org.mockito.Mockito.mock;

public class SuggestionsServiceTest {

	private TextAnalyticsProvider textAnalyticsProvider;

	private ConfigurationManager configurationManager;

	@Mock
	private SuggestionsService suggestionsService;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		ApplicationConfiguration mockedApplicationConfiguration = mock(ApplicationConfiguration.class);
		configurationManager = mock(ConfigurationManager.class);

		textAnalyticsProvider = mock(TextAnalyticsProvider.class);
		Mockito.when(textAnalyticsProvider.getTermsAutoCompleteSuggestions(null, null, null, null, null)).thenReturn(new ArrayList<SearchSuggestion>());

		Mockito.when(configurationManager.getApplicationConfiguration()).thenReturn(mockedApplicationConfiguration);
	}

	//getAutoCompleteSuggestions

	@Test
	public void getAutoCompleteSuggestions_TextAnalyticsMethodCalled() {
		suggestionsService.getAutoCompleteSuggestions(null, null, null, null);

		Mockito.verify(suggestionsService, Mockito.atLeast(1)).getAutoCompleteSuggestions(null, null, null, null);
	}
}