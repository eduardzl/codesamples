package com.verint.textanalytics.bl.applicationservices;

import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.model.interactions.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class SnippetsGeneratorTest {

	private ConfigurationManager configurationManager;

	private SearchInteractionsService searchService;

	private SnippetsGenerator snippetsGenerator;

	private SnippetsConfiguration snippetsConfig;

	@Mock
	SearchInteractionsContext searchContext;

	/**
	 * 
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		setConfiguration(2, 2, 2000, 5);
	}

	private void setConfiguration(int maxPrecedingWords, int maxFollowingWord, int fullTextMaxLength, int maxUtterancesToOperate) {
		MockitoAnnotations.initMocks(this);

		ApplicationConfiguration mockedApplicationConfiguration = mock(ApplicationConfiguration.class);
		configurationManager = mock(ConfigurationManager.class);

		Mockito.when(mockedApplicationConfiguration.getInteractionSnippetsMaxPrecedingWords()).thenReturn(maxPrecedingWords);
		Mockito.when(mockedApplicationConfiguration.getInteractionSnippetsMaxFollowingWords()).thenReturn(maxFollowingWord);
		Mockito.when(mockedApplicationConfiguration.getInteractionSnippetsFullTextMaxLength()).thenReturn(fullTextMaxLength);
		Mockito.when(mockedApplicationConfiguration.getInteractionSnippetsMaxUtterancesToOperate()).thenReturn(maxUtterancesToOperate);
		Mockito.when(configurationManager.getApplicationConfiguration()).thenReturn(mockedApplicationConfiguration);

		searchService = new SearchInteractionsService();
		snippetsGenerator = new SnippetsGenerator();
		snippetsConfig = new SnippetsConfiguration();

		snippetsGenerator.setConfigurationManager(configurationManager);
		snippetsConfig.setConfigurationManager(configurationManager);

		snippetsGenerator.setWordTokinizer(new LuceneStandardTokinizer());
		snippetsConfig.snippetsConfigurationInit();
		snippetsGenerator.setSnippetsConfig(snippetsConfig);
		searchService.setSnippetsGenerator(snippetsGenerator);
	}

	@Test
	public void testSnippetsGenerator_testBasic() {
		/*
		 * String text =
		 * "I note that the article does not mention how many people were invited to respond, and what percentage did. Neither does it say how respondents were selected. It should be obvious that BYOD fans would be significantly more likely to respond than others ... but we cannot assess that on the information provided."
		 * ; String term = "people";
		 * 
		 * InteractionBuilder builder = new InteractionBuilder();
		 * builder.highlight(text, term).utterance(text).interaction();
		 * 
		 * SnippetsGenerator generator = new SnippetsGenerator(new
		 * SnippetsConfiguration(2, 2, 2000));
		 * generator.generate(builder.cInteraction
		 * .getUtterances().get(0).getText(),
		 * builder.cInteraction.getUtterances().get(0).getHighlighting());
		 * 
		 * Snippet snippet = generator.getSnippets().get(0);
		 * 
		 * String expectedSnippet = "how many people were invited";
		 * assertTrue(snippet.getText().equals(expectedSnippet));
		 * assertTrue(snippet.getSnippetHighlights().get(0).getStarts() ==
		 * expectedSnippet.indexOf(term));
		 * assertTrue(snippet.getSnippetHighlights().get(0).getEnds() ==
		 * expectedSnippet.indexOf(term) + term.length());
		 */
	}

	@Test
	public void testSnippetsGenerator_mergedHighlights() throws Exception {
		String text = "I realized that the article, that I know, that I saw, that is the part of my work, was badly written. ";
		String term = "that";
		List<Snippet> snippets;

		InteractionBuilder builder = new InteractionBuilder();
		builder.highlight(text, term).utterance(text).interaction();

		searchService.mergeHighlightsForUtterances(builder.cInteraction.getUtterances());

		snippets = snippetsGenerator.generate(builder.cInteraction.getUtterances().get(0).getText(), (List) builder.cInteraction.getUtterances().get(0).getTermsHighlighting(),
		                                      true, builder.cInteraction.getUtterances().get(0).getId());

		Snippet snippet = snippets.get(0);

		String expectedSnippet = "I realized that the article, that I know, that I saw, that is the part of my work, was badly written. ";
		assertTrue(snippet.getText().equals(expectedSnippet));
		assertTrue(snippet.getSnippetHighlights().get(0).getStarts() == expectedSnippet.indexOf(term));
		assertTrue(snippet.getSnippetHighlights().get(0).getEnds() == expectedSnippet.indexOf(term) + term.length());

		int second = expectedSnippet.indexOf(term) + term.length();
		assertTrue(snippet.getSnippetHighlights().get(1).getStarts() == expectedSnippet.indexOf(term, second));
		assertTrue(snippet.getSnippetHighlights().get(1).getEnds() == expectedSnippet.indexOf(term, second) + term.length());

		int third = expectedSnippet.indexOf(term, second) + term.length();
		assertTrue(snippet.getSnippetHighlights().get(2).getStarts() == expectedSnippet.indexOf(term, third));
		assertTrue(snippet.getSnippetHighlights().get(2).getEnds() == expectedSnippet.indexOf(term, third) + term.length());

		int fourth = expectedSnippet.indexOf(term, third) + term.length();
		assertTrue(snippet.getSnippetHighlights().get(3).getStarts() == expectedSnippet.indexOf(term, fourth));
		assertTrue(snippet.getSnippetHighlights().get(3).getEnds() == expectedSnippet.indexOf(term, fourth) + term.length());

	}

	@Test
	public void testSnippetsGenerator_lastHighlightTakesAllText() throws Exception {
		String text = "I realized that the article, that I know, that I saw, that is the part of my work, was badly written.";
		String term = "that";
		List<Snippet> snippets;

		InteractionBuilder builder = new InteractionBuilder();
		builder.highlight(text, term).utterance(text).interaction();

		searchService.mergeHighlightsForUtterances(builder.cInteraction.getUtterances());

		snippets = snippetsGenerator.generate(builder.cInteraction.getUtterances().get(0).getText(), (List) builder.cInteraction.getUtterances().get(0).getTermsHighlighting(),
		                                      true, builder.cInteraction.getUtterances().get(0).getId());

		Snippet snippet = snippets.get(0);

		String expectedSnippet = "I realized that the article, that I know, that I saw, that is the part of my work, was badly written.";
		assertTrue(snippet.getText().equals(expectedSnippet));
		assertTrue(snippet.getSnippetHighlights().get(0).getStarts() == expectedSnippet.indexOf(term));
		assertTrue(snippet.getSnippetHighlights().get(0).getEnds() == expectedSnippet.indexOf(term) + term.length());

		int second = expectedSnippet.indexOf(term) + term.length();
		assertTrue(snippet.getSnippetHighlights().get(1).getStarts() == expectedSnippet.indexOf(term, second));
		assertTrue(snippet.getSnippetHighlights().get(1).getEnds() == expectedSnippet.indexOf(term, second) + term.length());

		int third = expectedSnippet.indexOf(term, second) + term.length();
		assertTrue(snippet.getSnippetHighlights().get(2).getStarts() == expectedSnippet.indexOf(term, third));
		assertTrue(snippet.getSnippetHighlights().get(2).getEnds() == expectedSnippet.indexOf(term, third) + term.length());

		int fourth = expectedSnippet.indexOf(term, third) + term.length();
		assertTrue(snippet.getSnippetHighlights().get(3).getStarts() == expectedSnippet.indexOf(term, fourth));
		assertTrue(snippet.getSnippetHighlights().get(3).getEnds() == expectedSnippet.indexOf(term, fourth) + term.length());

	}

	@Test
	public void testSnippetsGenerator_mergedHighlightsWidthAdditionalHighlight() throws Exception {
		String text = "I realized that the article, that I know, that I saw, that is the part of my work, was badly written that I know.";
		String term = "that";
		List<Snippet> snippets;

		InteractionBuilder builder = new InteractionBuilder();
		builder.highlight(text, term).utterance(text).interaction();

		snippets = snippetsGenerator.generate(builder.cInteraction.getUtterances().get(0).getText(), (List) builder.cInteraction.getUtterances().get(0).getTermsHighlighting(),
		                                      true, builder.cInteraction.getUtterances().get(0).getId());

		Snippet snippet = snippets.get(0);

		String expectedSnippet = "I realized that the article, that I know, that I saw, that is the";
		assertTrue(snippet.getText().equals(expectedSnippet));
		assertTrue(snippet.getSnippetHighlights().get(0).getStarts() == expectedSnippet.indexOf(term));
		assertTrue(snippet.getSnippetHighlights().get(0).getEnds() == expectedSnippet.indexOf(term) + term.length());

		int second = expectedSnippet.indexOf(term) + term.length();
		assertTrue(snippet.getSnippetHighlights().get(1).getStarts() == expectedSnippet.indexOf(term, second));
		assertTrue(snippet.getSnippetHighlights().get(1).getEnds() == expectedSnippet.indexOf(term, second) + term.length());

		int third = expectedSnippet.indexOf(term, second) + term.length();
		assertTrue(snippet.getSnippetHighlights().get(2).getStarts() == expectedSnippet.indexOf(term, third));
		assertTrue(snippet.getSnippetHighlights().get(2).getEnds() == expectedSnippet.indexOf(term, third) + term.length());

		int fourth = expectedSnippet.indexOf(term, third) + term.length();
		assertTrue(snippet.getSnippetHighlights().get(3).getStarts() == expectedSnippet.indexOf(term, fourth));
		assertTrue(snippet.getSnippetHighlights().get(3).getEnds() == expectedSnippet.indexOf(term, fourth) + term.length());

		snippet = snippets.get(1);
		expectedSnippet = "badly written that I know.";
		assertTrue(snippet.getText().equals(expectedSnippet));
		assertTrue(snippet.getSnippetHighlights().get(0).getStarts() == expectedSnippet.indexOf(term));
		assertTrue(snippet.getSnippetHighlights().get(0).getEnds() == expectedSnippet.indexOf(term) + term.length());
	}

	/**
	 * The bug: The single snippet does not show the whole string at its right
	 * side.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSnippetsGenerator_singleSnippet() throws Exception {

		setConfiguration(5, 5, 2000, 5);

		String text = "I cant stand Samsung devices, had one briefly and the junk they had piled on top of Android was astonishing, sold it within a week and bought my first Nexus phone. I don't use any of the 'top ten' battery drainers. Having no friends has it's advantages.";
		String term = "Samsung";
		List<Snippet> snippets;

		InteractionBuilder builder = new InteractionBuilder();
		builder.highlight(text, term).utterance(text).interaction();

		snippets = snippetsGenerator.generate(builder.cInteraction.getUtterances().get(0).getText(), (List) builder.cInteraction.getUtterances().get(0).getTermsHighlighting(),
		                                      true, builder.cInteraction.getUtterances().get(0).getId());

		Snippet snippet = snippets.get(0);

		String expectedSnippet = "I cant stand Samsung devices, had one briefly and the junk they had piled on top of Android was astonishing, sold it within a week and bought my first Nexus phone. I don't use any of the 'top ten' battery drainers. Having no friends has it's advantages.";
		assertTrue(snippet.getText().equals(expectedSnippet));
		assertTrue(snippet.getSnippetHighlights().get(0).getStarts() == expectedSnippet.indexOf(term));
		assertTrue(snippet.getSnippetHighlights().get(0).getEnds() == expectedSnippet.indexOf(term) + term.length());
	}

	@Test
	public void testSnippetsGenerator_highlightIsLastWord() throws Exception {

		setConfiguration(5, 5, 2000, 5);

		String text = "I have Blackberry z3, but I want a new mobile ";
		String term = "mobile";
		List<Snippet> snippets;

		InteractionBuilder builder = new InteractionBuilder();
		builder.highlight(text, term).utterance(text).interaction();

		searchService.mergeHighlightsForUtterances(builder.cInteraction.getUtterances());

		snippets = snippetsGenerator.generate(builder.cInteraction.getUtterances().get(0).getText(), (List) builder.cInteraction.getUtterances().get(0).getTermsHighlighting(),
		                                      true, builder.cInteraction.getUtterances().get(0).getId());

		assertTrue(snippets.size() == 1);
		Snippet snippet = snippets.get(0);

		String expectedSnippet = "but I want a new mobile ";
		assertTrue(snippet.getText().equals(expectedSnippet));
		assertTrue(snippet.getSnippetHighlights().get(0).getStarts() == expectedSnippet.indexOf(term));
		assertTrue(snippet.getSnippetHighlights().get(0).getEnds() == expectedSnippet.indexOf(term) + term.length());
	}

	@Test
	public void testSnippetsGenerator_twoTermsLinked() throws Exception {
		String text = "I think that Spurs reloading like they do every season of the next year.";
		String term1 = "like", term2 = "they";
		List<Snippet> snippets;

		InteractionBuilder builder = new InteractionBuilder();
		builder.highlight(text, term1).highlight(text, term2).utterance(text).interaction();

		searchService.mergeHighlightsForUtterances(builder.cInteraction.getUtterances());

		snippets = snippetsGenerator.generate(builder.cInteraction.getUtterances().get(0).getText(), (List) builder.cInteraction.getUtterances().get(0).getTermsHighlighting(),
		                                      true, builder.cInteraction.getUtterances().get(0).getId());

		Snippet snippet = snippets.get(0);

		String expectedSnippet = "Spurs reloading like they do every season of the next year.";
		assertTrue(snippet.getSnippetHighlights().get(0).getStarts() == expectedSnippet.indexOf(term1));
		assertTrue(snippet.getSnippetHighlights().get(0).getEnds() == expectedSnippet.indexOf(term1) + term1.length());
		assertTrue(snippet.getSnippetHighlights().get(1).getStarts() == expectedSnippet.indexOf(term2));
		assertTrue(snippet.getSnippetHighlights().get(1).getEnds() == expectedSnippet.indexOf(term2) + term2.length());
	}

	@Test
	public void testSnippetsGenerator_reduceSnippetsBasic() {
		List<Snippet> snippets = new ArrayList<Snippet>();
		Snippet snippet;
		snippet = new Snippet();
		snippet.setText("This is a first snippet");
		snippets.add(snippet);
		snippet = new Snippet();
		snippet.setText("This is a second snippet");
		snippets.add(snippet);

		ApplicationConfiguration mockedApplicationConfiguration = mock(ApplicationConfiguration.class);
		Mockito.when(mockedApplicationConfiguration.getInteractionSnippetsMaxPrecedingWords()).thenReturn(2);
		Mockito.when(mockedApplicationConfiguration.getInteractionSnippetsMaxFollowingWords()).thenReturn(2);
		Mockito.when(mockedApplicationConfiguration.getInteractionSnippetsFullTextMaxLength()).thenReturn(30);
		Mockito.when(configurationManager.getApplicationConfiguration()).thenReturn(mockedApplicationConfiguration);

		SnippetsGenerator generator = new SnippetsGenerator();
		generator.setConfigurationManager(configurationManager);
		generator.setWordTokinizer(new RegularExpressionWordTokinizer());

		List<Snippet> dilutedSnippets;
		dilutedSnippets = generator.diluteSnippetByLength(snippets);

		assertTrue(dilutedSnippets.size() == 1);
	}

	@Test
	public void testSnippetsGenerator_reduceSnippets_ToEmpty() {
		List<Snippet> snippets = new ArrayList<Snippet>();
		Snippet snippet;
		snippet = new Snippet();
		snippet.setText("This is a first snippet");
		snippets.add(snippet);
		snippet = new Snippet();
		snippet.setText("This is a second snippet");
		snippets.add(snippet);

		ApplicationConfiguration mockedApplicationConfiguration = mock(ApplicationConfiguration.class);
		Mockito.when(mockedApplicationConfiguration.getInteractionSnippetsMaxPrecedingWords()).thenReturn(2);
		Mockito.when(mockedApplicationConfiguration.getInteractionSnippetsMaxFollowingWords()).thenReturn(2);
		Mockito.when(mockedApplicationConfiguration.getInteractionSnippetsFullTextMaxLength()).thenReturn(5);
		Mockito.when(configurationManager.getApplicationConfiguration()).thenReturn(mockedApplicationConfiguration);

		SnippetsGenerator generator = new SnippetsGenerator();
		generator.setConfigurationManager(configurationManager);
		generator.setWordTokinizer(new RegularExpressionWordTokinizer());

		List<Snippet> dilutedSnippets;
		dilutedSnippets = generator.diluteSnippetByLength(snippets);

		assertTrue(dilutedSnippets.size() == 0);
	}

	@Test
	public void testSingleSnippetBuildHebrew() {

		String text = "חברת ורינט , ספקית מערכות ושירותים בתחום המודיעין העסקי והביטחוני נסחרת בשווי של כ-3.25 מיליארד דולר";
		String term = "המודיעין";

		InteractionBuilder builder = new InteractionBuilder();
		builder.highlight(text, term).utterance(text).interaction();

		searchService.mergeHighlightsForUtterances(builder.cInteraction.getUtterances());

		snippetsGenerator.buildSnippets(searchContext, builder.cInteraction, true);
		Snippet snippet = builder.cInteraction.getSnippets().get(0);

		String expectedSnippet = "ושירותים בתחום המודיעין העסקי והביטחוני נסחרת בשווי של כ-3.25 מיליארד דולר";
		assertTrue(snippet.getText().equals(expectedSnippet));
		assertTrue(snippet.getSnippetHighlights().get(0).getStarts() == expectedSnippet.indexOf(term));
		assertTrue(snippet.getSnippetHighlights().get(0).getEnds() == expectedSnippet.indexOf(term) + term.length());
	}

	@Test
	public void testSingleSnippetBuildEnglish_Apostroph() {

		String text = "verint systems is a melville, new york's based analytics company which was founded in 2002";
		String term = "analytics";

		InteractionBuilder builder = new InteractionBuilder();
		builder.highlight(text, term).utterance(text).interaction();

		searchService.mergeHighlightsForUtterances(builder.cInteraction.getUtterances());

		snippetsGenerator.buildSnippets(searchContext, builder.cInteraction, true);
		Snippet snippet = builder.cInteraction.getSnippets().get(0);

		String expectedSnippet = "york's based analytics company which was founded in 2002";
		assertTrue(snippet.getText().equals(expectedSnippet));
		assertTrue(snippet.getSnippetHighlights().get(0).getStarts() == expectedSnippet.indexOf(term));
		assertTrue(snippet.getSnippetHighlights().get(0).getEnds() == expectedSnippet.indexOf(term) + term.length());
	}

	@Test
	public void testSingleSnippetBuildEnglish_fromSOLR() {

		String text = "I note that the article does not mention how many people were invited to respond, and what percentage did. Neither does it say how respondents were selected. It should be obvious that BYOD fans would be significantly more likely to respond than others ... but we cannot assess that on the information provided.";
		String term = "people";

		InteractionBuilder builder = new InteractionBuilder();
		builder.highlight(text, term).utterance(text).interaction();

		searchService.mergeHighlightsForUtterances(builder.cInteraction.getUtterances());

		snippetsGenerator.buildSnippets(searchContext, builder.cInteraction, true);

		Snippet snippet = builder.cInteraction.getSnippets().get(0);

		String expectedSnippet = "how many people were invited to respond, and what percentage did. Neither does it say how respondents were selected. It should be obvious that BYOD fans would be significantly more likely to respond than others ... but we cannot assess that on the information provided.";
		assertTrue(snippet.getText().equals(expectedSnippet));
		assertTrue(snippet.getSnippetHighlights().get(0).getStarts() == expectedSnippet.indexOf(term));
		assertTrue(snippet.getSnippetHighlights().get(0).getEnds() == expectedSnippet.indexOf(term) + term.length());
	}

	@Test
	public void testSnippetBuild_SingleSnippetMultipleMatches() {

		String text = "I note that the article does not mention how many people were invited to respond, and what percentage did. Neither does it say how respondents were selected. It should be obvious that BYOD fans would be significantly more likely to respond than others ... but we cannot assess that on the information provided.";
		String term = "that";

		InteractionBuilder builder = new InteractionBuilder();
		builder.highlight(text, term).utterance(text).interaction();

		searchService.mergeHighlightsForUtterances(builder.cInteraction.getUtterances());

		snippetsGenerator.buildSnippets(searchContext, builder.cInteraction, true);

		int snippetNo;
		Snippet snippet;
		String expected;

		snippetNo = 0;
		snippet = builder.cInteraction.getSnippets().get(snippetNo);
		expected = "I note that the article";
		assertTrue(snippet.getText().equals(expected));
		assertTrue(snippet.getSnippetHighlights().get(0).getStarts() == expected.indexOf(term));
		assertTrue(snippet.getSnippetHighlights().get(0).getEnds() == expected.indexOf(term) + term.length());

		snippetNo = 1;
		snippet = builder.cInteraction.getSnippets().get(snippetNo);
		expected = "be obvious that BYOD fans";
		assertTrue(snippet.getText().equals(expected));
		assertTrue(snippet.getSnippetHighlights().get(0).getStarts() == expected.indexOf(term));
		assertTrue(snippet.getSnippetHighlights().get(0).getEnds() == expected.indexOf(term) + term.length());

		snippetNo = 2;
		snippet = builder.cInteraction.getSnippets().get(snippetNo);
		expected = "cannot assess that on the information provided.";
		assertTrue(snippet.getText().equals(expected));
		assertTrue(snippet.getSnippetHighlights().get(0).getStarts() == expected.indexOf(term));
		assertTrue(snippet.getSnippetHighlights().get(0).getEnds() == expected.indexOf(term) + term.length());
	}

	public void testSnippetBuild_TwoUtterances() {

		String text1 = "I note that the article does not mention how many people were invited to respond, and what percentage did. Neither does it say how respondents were selected. It should be obvious that BYOD fans would be significantly more likely to respond than others ... but we cannot assess that on the information provided.";
		String text2 = "I note that the article does not mention how many people were invited to respond, and what percentage did. Neither does it say how respondents were selected. It should be obvious that BYOD fans would be significantly more likely to respond than others ... but we cannot assess that on the information provided.";
		String term = "people";

		InteractionBuilder builder = new InteractionBuilder();
		builder.highlight(text1, term).utterance(text1).highlight(text2, term).utterance(text2).interaction();

		searchService.mergeHighlightsForUtterances(builder.cInteraction.getUtterances());

		snippetsGenerator.buildSnippets(searchContext, builder.cInteraction, true);

		Snippet snippet = builder.cInteraction.getSnippets().get(0);

		String expectedSnippet = "how many people were invited";
		assertTrue(snippet.getText().equals(expectedSnippet));
		assertTrue(snippet.getSnippetHighlights().get(0).getStarts() == expectedSnippet.indexOf(term));
		assertTrue(snippet.getSnippetHighlights().get(0).getEnds() == expectedSnippet.indexOf(term) + term.length());

		snippet = builder.cInteraction.getSnippets().get(1);

		expectedSnippet = "how many people were invited to respond, and what percentage did. Neither does it say how respondents were selected. It should be obvious that BYOD fans would be significantly more likely to respond than others ... but we cannot assess that on the information provided.";
		assertTrue(snippet.getText().equals(expectedSnippet));
		assertTrue(snippet.getSnippetHighlights().get(0).getStarts() == expectedSnippet.indexOf(term));
		assertTrue(snippet.getSnippetHighlights().get(0).getEnds() == expectedSnippet.indexOf(term) + term.length());

	}

	@Test
	public void testSnippetBuild_TwoUtterances4Highlights() {

		String text1 = "Verint is a global leader in Actionable Intelligence solutions and value-added services.";
		String text2 = "Verint Enterprise Intelligence Solutions help organizations of all sizes capture and analyze customer interactions, sentiments and trends across multiple channels.";
		String term1 = "Verint";
		String term2 = "and";

		InteractionBuilder builder = new InteractionBuilder();
		builder.highlight(text1, term1).highlight(text1, term2).utterance(text1).highlight(text2, term1).highlight(text2, term2).utterance(text2).interaction();

		searchService.mergeHighlightsForUtterances(builder.cInteraction.getUtterances());

		snippetsGenerator.buildSnippets(searchContext, builder.cInteraction, true);

		Snippet snippet = builder.cInteraction.getSnippets().get(0);
		String expectedSnippet = "Verint is a";
		assertTrue(snippet.getText().equals(expectedSnippet));
		assertTrue(snippet.getSnippetHighlights().get(0).getStarts() == expectedSnippet.indexOf(term1));
		assertTrue(snippet.getSnippetHighlights().get(0).getEnds() == expectedSnippet.indexOf(term1) + term1.length());

		snippet = builder.cInteraction.getSnippets().get(1);
		expectedSnippet = "Intelligence solutions and value-added services.";
		assertTrue(snippet.getText().equals(expectedSnippet));
		assertTrue(snippet.getSnippetHighlights().get(0).getStarts() == expectedSnippet.indexOf(term2));
		assertTrue(snippet.getSnippetHighlights().get(0).getEnds() == expectedSnippet.indexOf(term2) + term2.length());

		snippet = builder.cInteraction.getSnippets().get(2);
		expectedSnippet = "Verint Enterprise Intelligence";
		assertTrue(snippet.getText().equals(expectedSnippet));
		assertTrue(snippet.getSnippetHighlights().get(0).getStarts() == expectedSnippet.indexOf(term1));
		assertTrue(snippet.getSnippetHighlights().get(0).getEnds() == expectedSnippet.indexOf(term1) + term1.length());

		snippet = builder.cInteraction.getSnippets().get(3);
		expectedSnippet = "sizes capture and analyze customer interactions, sentiments and trends across multiple channels.";
		assertTrue(snippet.getText().equals(expectedSnippet));
		assertTrue(snippet.getSnippetHighlights().get(0).getStarts() == expectedSnippet.indexOf(term2));
		assertTrue(snippet.getSnippetHighlights().get(0).getEnds() == expectedSnippet.indexOf(term2) + term2.length());
	}

	@Test
	public void testSnippetBuild_twoTermsLinked() {
		String text = "I think that Spurs reloading like they do every season of the next year.";
		String term1 = "like", term2 = "they";

		InteractionBuilder builder = new InteractionBuilder();
		builder.highlight(text, term1).highlight(text, term2).utterance(text).interaction();

		searchService.mergeHighlightsForUtterances(builder.cInteraction.getUtterances());

		snippetsGenerator.buildSnippets(searchContext, builder.cInteraction, true);

		Snippet snippet = builder.cInteraction.getSnippets().get(0);

		String expectedSnippet = "Spurs reloading like they do every season of the next year.";
		assertTrue(snippet.getText().equals(expectedSnippet));
		assertTrue(snippet.getSnippetHighlights().get(0).getStarts() == expectedSnippet.indexOf(term1));
		assertTrue(snippet.getSnippetHighlights().get(0).getEnds() == expectedSnippet.indexOf(term1) + term1.length());
		assertTrue(snippet.getSnippetHighlights().get(1).getStarts() == expectedSnippet.indexOf(term2));
		assertTrue(snippet.getSnippetHighlights().get(1).getEnds() == expectedSnippet.indexOf(term2) + term2.length());
	}

	@Test
	public void testSnippetBuild_UtterancesWithoutHighlight() {
		String text1 = "verint systems is a melville, new york's based analytics company which was founded in 2002";
		String text2 = "verint systems a melville, new york's based analytics company which was founded in 2002";
		String term = "is";

		InteractionBuilder builder = new InteractionBuilder();
		builder.highlight(text1, term).utterance(text1).utterance(text2).interaction();

		searchService.mergeHighlightsForUtterances(builder.cInteraction.getUtterances());

		snippetsGenerator.buildSnippets(searchContext, builder.cInteraction, true);

		Snippet snippet = builder.cInteraction.getSnippets().get(0);

		String expectedSnippet = "verint systems is a melville, new york's based analytics company which was founded in 2002";
		assertTrue(snippet.getText().equals(expectedSnippet));
		assertTrue(snippet.getSnippetHighlights().get(0).getStarts() == expectedSnippet.indexOf(term));
		assertTrue(snippet.getSnippetHighlights().get(0).getEnds() == expectedSnippet.indexOf(term) + term.length());
	}

	@Test
	public void testSnippetBuild_SingleUtteranceWithoutHighlight() {
		MockitoAnnotations.initMocks(this);
		String text1 = "verint systems is a melville, new york's based analytics company which was founded in 2002";
		InteractionBuilder builder = new InteractionBuilder();
		builder.utterance(text1).interaction();

		searchService.mergeHighlightsForUtterances(builder.cInteraction.getUtterances());

		snippetsGenerator.buildSnippets(searchContext, builder.cInteraction, true);

		Snippet snippet = builder.cInteraction.getSnippets().get(0);

		assertTrue(snippet.getText().equals(text1));
		assertTrue(snippet.getSnippetHighlights().size() == 0);
	}

	@Test
	public void testSnippetBuild_SingleUtteranceWithoutHighlight_CutByMaxSnippetLength() {
		String text1 = "verint systems is a melville, new york's based analytics company which was founded in 2002";

		setConfiguration(2, 2, 30, 5);

		InteractionBuilder builder = new InteractionBuilder();
		builder.utterance(text1).interaction();

		searchService.mergeHighlightsForUtterances(builder.cInteraction.getUtterances());

		snippetsGenerator.buildSnippets(searchContext, builder.cInteraction, true);

		Snippet snippet = builder.cInteraction.getSnippets().get(0);

		String expectedText = text1.substring(0, configurationManager.getApplicationConfiguration().getInteractionSnippetsFullTextMaxLength() - 1);
		assertTrue(snippet.getText().equals(expectedText));
		assertTrue(snippet.getSnippetHighlights().size() == 0);
	}

	@Test
	public void testSnippetBuild_SingleSnippetMultipleMatches_CutByMaxLength() {

		setConfiguration(2, 2, 50, 5);

		String text = "I note that the article does not mention how many people were invited to respond, and what percentage did. Neither does it say how respondents were selected. It should be obvious that BYOD fans would be significantly more likely to respond than others ... but we cannot assess that on the information provided.";
		String term = "that";

		InteractionBuilder builder = new InteractionBuilder();
		builder.highlight(text, term).utterance(text).interaction();

		searchService.mergeHighlightsForUtterances(builder.cInteraction.getUtterances());

		snippetsGenerator.buildSnippets(searchContext, builder.cInteraction, true);

		int snippetNo;
		Snippet snippet;
		String expected;

		assertTrue(builder.cInteraction.getSnippets().size() == 2);

		snippetNo = 0;
		snippet = builder.cInteraction.getSnippets().get(snippetNo);
		expected = "I note that the article";
		assertTrue(snippet.getText().equals(expected));
		assertTrue(snippet.getSnippetHighlights().get(0).getStarts() == expected.indexOf(term));
		assertTrue(snippet.getSnippetHighlights().get(0).getEnds() == expected.indexOf(term) + term.length());

		snippetNo = 1;
		snippet = builder.cInteraction.getSnippets().get(snippetNo);
		expected = "be obvious that BYOD fans";
		assertTrue(snippet.getText().equals(expected));
		assertTrue(snippet.getSnippetHighlights().get(0).getStarts() == expected.indexOf(term));
		assertTrue(snippet.getSnippetHighlights().get(0).getEnds() == expected.indexOf(term) + term.length());
	}

	@Test
	public void testSnippetBuild_SingleSearchOneMatchInMiddle() {

		String text = "verint systems is a melville, new york based analytics company which was founded in 2002";
		String term = "company";

		InteractionBuilder builder = new InteractionBuilder();
		builder.highlight(text, term).utterance(text).interaction();

		searchService.mergeHighlightsForUtterances(builder.cInteraction.getUtterances());

		snippetsGenerator.buildSnippets(searchContext, builder.cInteraction, true);

		Snippet snippet = builder.cInteraction.getSnippets().get(0);

		String expectedSnippet = "based analytics company which was founded in 2002";
		assertTrue(snippet.getText().equals(expectedSnippet));
		assertTrue(snippet.getSnippetHighlights().get(0).getStarts() == expectedSnippet.indexOf(term));
		assertTrue(snippet.getSnippetHighlights().get(0).getEnds() == expectedSnippet.indexOf(term) + term.length());
	}

	@Test
	public void highlight_IsLastWord() {
		String text = "You have been connected to Ruben.";
		String term = "Ruben";

		InteractionBuilder builder = new InteractionBuilder();
		builder.highlight(text, term).utterance(text).interaction();

		setConfiguration(5, 5, 2000, 5);

		searchService.mergeHighlightsForUtterances(builder.cInteraction.getUtterances());

		snippetsGenerator.buildSnippets(searchContext, builder.cInteraction, true);

		Snippet snippet = builder.cInteraction.getSnippets().get(0);

		String expectedSnippet = "You have been connected to Ruben.";
		assertTrue(snippet.getText().equals(expectedSnippet));
		assertTrue(snippet.getSnippetHighlights().get(0).getStarts() == expectedSnippet.indexOf(term));
		assertTrue(snippet.getSnippetHighlights().get(0).getEnds() == expectedSnippet.indexOf(term) + term.length());
	}

	@Test
	public void highlight_IsPreLastWord() {
		String text = "You have been connected to Ruben.";
		String term = "to";

		InteractionBuilder builder = new InteractionBuilder();
		builder.highlight(text, term).utterance(text).interaction();

		setConfiguration(5, 5, 2000, 5);

		searchService.mergeHighlightsForUtterances(builder.cInteraction.getUtterances());

		snippetsGenerator.buildSnippets(searchContext, builder.cInteraction, true);

		Snippet snippet = builder.cInteraction.getSnippets().get(0);

		String expectedSnippet = "You have been connected to Ruben.";
		assertTrue(snippet.getText().equals(expectedSnippet));
		assertTrue(snippet.getSnippetHighlights().get(0).getStarts() == expectedSnippet.indexOf(term));
		assertTrue(snippet.getSnippetHighlights().get(0).getEnds() == expectedSnippet.indexOf(term) + term.length());
	}

	@Test
	public void highlight_IsTwoWordsBeforeLastNotSingleUtterance() {
		String text = "You have been connected to Ray.";
		String term = "connected";
		String text2 = "You have been connected to Mary.";
		String term2 = "Mary";

		InteractionBuilder builder = new InteractionBuilder();
		builder.highlight(text, term).utterance(text).highlight(text2, term2).utterance(text2).interaction();

		setConfiguration(5, 5, 2000, 5);

		searchService.mergeHighlightsForUtterances(builder.cInteraction.getUtterances());

		snippetsGenerator.buildSnippets(searchContext, builder.cInteraction, true);

		assertTrue(builder.cInteraction.getSnippets().size() == 2);
		Snippet snippet = builder.cInteraction.getSnippets().get(0);

		String expectedSnippet = "You have been connected to Ray.";
		assertTrue(snippet.getText().equals(expectedSnippet));
		assertTrue(snippet.getSnippetHighlights().get(0).getStarts() == expectedSnippet.indexOf(term));
		assertTrue(snippet.getSnippetHighlights().get(0).getEnds() == expectedSnippet.indexOf(term) + term.length());
	}

	@Test
	public void testSnippetsGenerator_twoHighlightsOnTheSameWord() throws Exception {

		setConfiguration(5, 5, 2000, 5);

		String text = "When you submit your request, we will look for properties that are at your chosen star level or higher to give you the highest quality hotel at the best value.";
		String term = "highest quality";
		List<Snippet> snippets;

		InteractionBuilder builder = new InteractionBuilder();
		builder.highlight(text, term).utterance(text).interaction();

		searchService.mergeHighlightsForUtterances(builder.cInteraction.getUtterances());

		snippets = snippetsGenerator.generate(builder.cInteraction.getUtterances().get(0).getText(), (List) builder.cInteraction.getUtterances().get(0).getTermsHighlighting(),
		                                      true, builder.cInteraction.getUtterances().get(0).getId());

		Snippet snippet = snippets.get(0);

		String expectedSnippet = "higher to give you the highest quality hotel at the best value.";
		assertTrue(snippet.getText().equals(expectedSnippet));
		assertTrue(snippet.getSnippetHighlights().get(0).getStarts() == expectedSnippet.indexOf(term));
		assertTrue(snippet.getSnippetHighlights().get(0).getEnds() == expectedSnippet.indexOf(term) + term.length());
	}

	@Test
	public void testSnippetsGenerator_embeddedWord() {
		MockitoAnnotations.initMocks(this);

		setConfiguration(5, 5, 2000, 5);

		String text = "When you open the link, all you need to do is to provide a U.S. or Canadian phone number and you will be connected to a phone service agent. Here's the link:";
		// ________________________________________________________________________________________|****|____________________________________________________
		// ____________________________________________________________________________________________________________________________________|****|
		// ____________________________________________________________________________________________________________________________________|************|
		// ________________________________________________________________________________________|****|______________________________________|****||******|
		// _______________________________________________________________________________________76____81____________________________________120_125_126__133

		String term1 = "phone", term2 = "phone", term3 = "service";
		InteractionBuilder builder = new InteractionBuilder();
		// term2 is created on the second occurrence
		builder.highlight(text, term1).highlight(text, term3).utterance(text).interaction();

		searchService.mergeHighlightsForUtterances(builder.cInteraction.getUtterances());

		snippetsGenerator.buildSnippets(searchContext, builder.cInteraction, true);

		Snippet snippet = builder.cInteraction.getSnippets().get(0);

		String expectedText = "provide a U.S. or Canadian phone number and you will be connected to a phone service agent. Here's the link:";

/*
		assertTrue(snippet.getText().equals(expectedText));

		assertTrue(snippetHighlight.getStarts() == expectedText.indexOf(term1));

		assertTrue(snippetHighlight.getEnds() == expectedText.indexOf(term1) + term1.length());


		snippetHighlight = snippet.getSnippetHighlights().get(1);
		assertTrue(snippetHighlight.getStarts() == expectedText.indexOf(term2, i + 1));
		assertTrue(snippetHighlight.getEnds() == expectedText.indexOf(term2, i + 1) + term2.length());


		snippetHighlight = snippet.getSnippetHighlights().get(2);
		assertTrue(snippetHighlight.getStarts() == expectedText.indexOf(term3));
		assertTrue(snippetHighlight.getEnds() == expectedText.indexOf(term3) + term3.length());
		*/

	}

	@Test
	public void testSnippetsGenerator_generate_Term_With_Entity() {
		MockitoAnnotations.initMocks(this);

		setConfiguration(5, 5, 2000, 5);

		String text = "other than just the price (i.e. either add an Area, select a lower star level, or change your dates ";
					// ____________________________________________________|*******||***|____________________________________
					// _____________________________________________________________|***|____________________________________
					// ____________________________________________________|*******|________________________________________
					// _____________________________________________________________|***|____________________________________
					//                                                    52      61 62 66

		String term1 = "select a", term2 = "lower";
		InteractionBuilder builder = new InteractionBuilder();

		builder
				.highlightByContentType(text, HighlightType.Entity, term1)
				.highlightToCurrentUtterance(HighlightType.Entity, text)

				.highlightByContentType(text, HighlightType.Term, term2)
				.content(builder.cHighlight, HighlightType.Entity, term1 + " " + term2)
				.highlightToCurrentUtterance(HighlightType.Term, text)
				.interaction();

		searchService.mergeHighlightsForUtterances(builder.cInteraction.getUtterances());

		snippetsGenerator.buildSnippets(searchContext, builder.cInteraction, true);

		Snippet snippet = builder.cInteraction.getSnippets().get(0);

		String expectedText = "i.e. either add an Area, select a lower star level, or change your dates ";
							// _________________________|************|___________________________________

		assertTrue(snippet.getText().equals(expectedText));

		BaseHighlight snippetHighlight;

		snippetHighlight = snippet.getSnippetHighlights().get(0);
		int i = expectedText.indexOf(term1);
		assertTrue(snippetHighlight.getStarts() == expectedText.indexOf(term1));
		assertTrue(snippetHighlight.getEnds() == expectedText.indexOf(term1) + term1.length());
		assertTrue(snippetHighlight.getContents().size() == 1);
		assertTrue(snippetHighlight.getContents().get(0).getType() == HighlightType.Entity);

		snippetHighlight = snippet.getSnippetHighlights().get(1);
		i = expectedText.indexOf(term2);
		assertTrue(snippetHighlight.getStarts() == expectedText.indexOf(term2));
		assertTrue(snippetHighlight.getEnds() == expectedText.indexOf(term2) + term2.length());
		assertTrue(snippetHighlight.getContents().size() == 2);
		assertTrue(snippetHighlight.getContents().get(0).getType() == HighlightType.Term);
		assertTrue(snippetHighlight.getContents().get(1).getType() == HighlightType.Entity);
	}

	@Test
	public void testSnippetsGenerator_generate_Term_Entity_And_Relation() {
		MockitoAnnotations.initMocks(this);

		setConfiguration(5, 5, 2000, 5);

		String text = "other than just the price (i.e. either add an Area, select a lower star level, or change your dates ";
		// _______________________________________________________________|*******||***|____________________________________
		// ________________________________________________________________________|***|____________________________________
		// _______________________________________________________________|*******|________________________________________
		// ________________________________________________________________________|***|____________________________________
		//                                                               52      61 62 66

		String term1 = "select a", term2 = "lower", term3 = "select a lower";
		InteractionBuilder builder = new InteractionBuilder();

		builder
				.highlightByContentType(text, HighlightType.Entity, term1)
				.content(builder.cEntityHighlight, HighlightType.Relation, term1)
				.highlightToCurrentUtterance(HighlightType.Entity, text + " " + term2)

				.highlightByContentType(text, HighlightType.Term, term2)
				.content(builder.cHighlight, HighlightType.Entity, term1 + " " + term2)
				.content(builder.cHighlight, HighlightType.Relation, term1 + " " + term2)
				.highlightToCurrentUtterance(HighlightType.Term, text)
				.interaction();

		searchService.mergeHighlightsForUtterances(builder.cInteraction.getUtterances());

		snippetsGenerator.buildSnippets(searchContext, builder.cInteraction, true);

		Snippet snippet = builder.cInteraction.getSnippets().get(0);

		String expectedText = "i.e. either add an Area, select a lower star level, or change your dates  lower";
		// ____________________________________________|********|_______________________________________
		// ____________________________________________          |****|_________________________________

		assertTrue(snippet.getText().equals(expectedText));

		BaseHighlight snippetHighlight;

		snippetHighlight = snippet.getSnippetHighlights().get(0);
		int i = expectedText.indexOf(term1);
		assertTrue(snippetHighlight.getStarts() == expectedText.indexOf(term1));
		assertTrue(snippetHighlight.getEnds() == expectedText.indexOf(term1) + term1.length());
		assertTrue(snippetHighlight.getContents().size() == 2);
		assertTrue(snippetHighlight.getContents().get(0).getType() == HighlightType.Entity);
		assertTrue(snippetHighlight.getContents().get(1).getType() == HighlightType.Relation);

		snippetHighlight = snippet.getSnippetHighlights().get(1);
		i = expectedText.indexOf(term2);
		assertTrue(snippetHighlight.getStarts() == expectedText.indexOf(term2));
		assertTrue(snippetHighlight.getEnds() == expectedText.indexOf(term2) + term2.length());
		assertTrue(snippetHighlight.getContents().size() == 3);
		assertTrue(snippetHighlight.getContents().get(0).getType() == HighlightType.Term);
		assertTrue(snippetHighlight.getContents().get(1).getType() == HighlightType.Entity);
		assertTrue(snippetHighlight.getContents().get(2).getType() == HighlightType.Relation);
	}

	@Test
	public void testSnippetsGenerator_generate_Term_Entity_And_Relation2() {
		MockitoAnnotations.initMocks(this);

		setConfiguration(5, 5, 2000, 5);

		String text = "you can take the contract and later use your bank details to pay the bill.";
		// ________________________________________________________________________|***|____________________________________
		// ________________________________________________________________________|***|____________________________________

		String term = "pay", entity = "pay", relation = "pay";
		InteractionBuilder builder = new InteractionBuilder();

		builder
				.highlightByContentType(text, HighlightType.Term, term) // for term don't add content
				.content(builder.cHighlight, HighlightType.Entity, entity)
				.content(builder.cHighlight, HighlightType.Relation, relation)
		.highlightToCurrentUtterance(HighlightType.Term, text + " " + term)
				.interaction();

		searchService.mergeHighlightsForUtterances(builder.cInteraction.getUtterances());

		snippetsGenerator.buildSnippets(searchContext, builder.cInteraction, true);

		Snippet snippet = builder.cInteraction.getSnippets().get(0);

		String expectedText = "use your bank details to pay the bill. pay";
		// ____________________________________________|***|______________

		assertTrue(snippet.getText().equals(expectedText));

		BaseHighlight snippetHighlight;

		snippetHighlight = snippet.getSnippetHighlights().get(0);
		int i = expectedText.indexOf(term);
		assertTrue(snippetHighlight.getStarts() == expectedText.indexOf(term));
		assertTrue(snippetHighlight.getEnds() == expectedText.indexOf(term) + term.length());
		assertTrue(snippetHighlight.getContents().size() == 3);
		assertTrue(snippetHighlight.getContents().get(0).getType() == HighlightType.Term);
		assertTrue(snippetHighlight.getContents().get(1).getType() == HighlightType.Entity);
		assertTrue(snippetHighlight.getContents().get(2).getType() == HighlightType.Relation);
	}


	public class InteractionBuilder {

		public List<Interaction> cInteractions;

		public List<Utterance> cUtterances;
		public List<TermHighlight> cHighlights;
		public List<EntityHighlight> cEntityHighlights;
		public List<RelationHighlight> cRelationHighlights;
		public List<KeyTermHighlight> cKeyTermHighlights;

		public Interaction cInteraction;
		private Utterance cUtterance;

		private TermHighlight cHighlight;
		private EntityHighlight cEntityHighlight;
		private RelationHighlight cRelationHighlight;
		private KeyTermHighlight cKeyTermHighlight;

		private BaseHighlight cHighlightByType;

		private String cTerm;

		public InteractionBuilder() {
			cInteractions = new ArrayList<Interaction>();
			cUtterances = new ArrayList<Utterance>();

			cHighlights = new ArrayList<TermHighlight>();
			cEntityHighlights = new ArrayList<EntityHighlight>();
			cRelationHighlights = new ArrayList<RelationHighlight>();
			cKeyTermHighlights = new ArrayList<KeyTermHighlight>();

			cHighlight = new TermHighlight();
			cHighlight.setContents(new ArrayList<HighlightContent>());

			cEntityHighlight = new EntityHighlight();
			cEntityHighlight.setContents(new ArrayList<HighlightContent>());
			cRelationHighlight = new RelationHighlight();
			cRelationHighlight.setContents(new ArrayList<HighlightContent>());
			cKeyTermHighlight = new KeyTermHighlight();
			cKeyTermHighlight.setContents(new ArrayList<HighlightContent>());
		}

		public InteractionBuilder interactions() {
			cInteractions.add(cInteraction);
			return this;
		}

		public InteractionBuilder interaction() {
			cInteraction = new Interaction();
			cInteraction.setUtterances(cUtterances);
			cInteractions.add(cInteraction);
			return this;
		}

		public InteractionBuilder utterance(String text) {
			cUtterance = new Utterance();
			cUtterance.setText(text);

			cUtterance.setTermsHighlighting(cHighlights);
			cUtterance.setEntitiesHighlighting(cEntityHighlights);
			cUtterance.setRelationsHighlighting(cRelationHighlights);
			cUtterance.setKeytermsHighlighting(cKeyTermHighlights);

			cUtterances.add(cUtterance);
			cHighlights = new ArrayList<TermHighlight>();
			return this;
		}

		public InteractionBuilder content(BaseHighlight highlight, HighlightType type, String name) {

			HighlightContent content = new HighlightContent();

			switch(type) {
				case Term:
					content.setType(HighlightType.Term);
					break;
				case Entity:
					content.setType(HighlightType.Entity);
					content.setData(name);

					break;
				case Relation:
					content.setType(HighlightType.Relation);
					content.setData(name);
					break;
				case KeyTerm:
					content.setType(HighlightType.Relation);
					content.setData(name);
					break;
			}

			highlight.getContents().add(content);

			return this;
		}

		public InteractionBuilder highlightToCurrentUtterance(HighlightType highlightType, String text) {
			if (cUtterance == null) {
				cUtterance = new Utterance();
				cUtterance.setText(text);
				cUtterances.add(cUtterance);
			}

			switch (highlightType) {
				case Term:
					if (cUtterance.getTermsHighlighting()==null)
						cUtterance.setTermsHighlighting(new ArrayList<TermHighlight>());

					for (int i = 0; i< cHighlights.size(); i++)
						cUtterance.getTermsHighlighting().add(cHighlights.get(i));
					break;
				case Entity:
					if (cUtterance.getEntitiesHighlighting()==null)
						cUtterance.setEntitiesHighlighting(new ArrayList<EntityHighlight>());
					for (int i = 0; i< cEntityHighlights.size(); i++)
						cUtterance.getEntitiesHighlighting().add(cEntityHighlights.get(i));
					break;
				case Relation:
					if (cUtterance.getRelationsHighlighting()==null)
						cUtterance.setRelationsHighlighting(new ArrayList<RelationHighlight>());
					for (int i = 0; i< cRelationHighlights.size(); i++)
						cUtterance.getRelationsHighlighting().add(cRelationHighlights.get(i));
					break;
				case KeyTerm:
					if (cUtterance.getKeytermsHighlighting()==null)
						cUtterance.setKeytermsHighlighting(new ArrayList<KeyTermHighlight>());
					for (int i = 0; i< cKeyTermHighlights.size(); i++)
						cUtterance.getKeytermsHighlighting().add(cKeyTermHighlights.get(i));
					break;
			}

			return this;
		}

		/**
		 * Parse a text for term and create Highlight for each occurrence
		 * 
		 * @param text
		 * @param term
		 * @return
		 */
		public InteractionBuilder highlight(String text, String term) {
			Pattern p = Pattern.compile(term, Pattern.UNICODE_CHARACTER_CLASS);
			Matcher matcher = p.matcher(text);

			while (matcher.find())
				this.highlight(term, matcher.start(), matcher.end());

			return this;
		}

		public InteractionBuilder highlightByContentType(String text, HighlightType contentType, String name) {

			Pattern p = Pattern.compile(name, Pattern.UNICODE_CHARACTER_CLASS);
			Matcher matcher = p.matcher(text);

			while (matcher.find())
				this.highlightByContentType(contentType, name, matcher.start(), matcher.end());

			return this;
		}

		private InteractionBuilder highlight(int start, int end) {
			highlight(cTerm, start, end);
			return this;
		}

		private InteractionBuilder highlight(String term, int start, int end) {
			cTerm = term;
			cHighlight = new TermHighlight();
			cHighlight.setTerm(term).setStarts(start).setEnds(end);
			cHighlights.add(cHighlight);
			return this;
		}

		private InteractionBuilder highlightByContentType(HighlightType contentType, String name, int start, int end) {
			cHighlightByType = new BaseHighlight();
			cHighlightByType.setStarts(start);
			cHighlightByType.setEnds(end);
			cHighlightByType.setContents(new ArrayList<HighlightContent>());

			switch(contentType) {
				case Term:
					cTerm = name;
					cHighlight.setTerm(name).setStarts(start).setEnds(end);
					cHighlights.add(cHighlight);
					content(cHighlight, contentType, name);

					break;
				case Entity:
					cEntityHighlight.setTopic(name);
					cEntityHighlight.setStarts(start);
					cEntityHighlight.setEnds(end);
					cEntityHighlights.add(cEntityHighlight);

					content(cEntityHighlight, contentType, name);
					break;
				case Relation:
					cRelationHighlight.setRelation(name);
					cRelationHighlight.setStarts(start);
					cRelationHighlight.setEnds(end);

					cRelationHighlights.add(cRelationHighlight);

					content(cRelationHighlight, contentType, name);
					break;
				case KeyTerm:
					cKeyTermHighlight.setKeyterm(name);
					cKeyTermHighlight.setStarts(start);
					cKeyTermHighlight.setEnds(end);
					cKeyTermHighlights.add(cKeyTermHighlight);

					content(cKeyTermHighlight, contentType, name);

					break;
			}

			return this;
		}

		public BaseHighlight generateBaseHighlight(String text, String term) {
			int start, end;
			start = text.indexOf(term);
			end = text.indexOf(term) + term.length();
			BaseHighlight newHighlight = new BaseHighlight();
			newHighlight.setStarts(start);
			newHighlight.setEnds(end);
			return newHighlight;
		}

		public BaseHighlight generateTopicHighlight(String text, String term, String topic) {
			return generateTopicHighlight(text, term, topic, 0);
		}

		public BaseHighlight generateTopicHighlight(String text, String term, String topic, int startFrom) {
			int start, end;
			start = text.indexOf(term, startFrom);
			end = text.indexOf(term, startFrom) + term.length();
			EntityHighlight newHighlight = new EntityHighlight();
			newHighlight.setStarts(start);
			newHighlight.setEnds(end);
			newHighlight.setTopic(topic);
			return newHighlight;
		}

		public BaseHighlight generateTermHighlight(String text, String term) {
			return generateTermHighlight(text, term, 0);
		}

		public BaseHighlight generateTermHighlight(String text, String term, int startFrom) {
			int start, end;
			start = text.indexOf(term, startFrom);
			end = text.indexOf(term, startFrom) + term.length();
			TermHighlight newHighlight = new TermHighlight();
			newHighlight.setStarts(start);
			newHighlight.setEnds(end);
			return newHighlight;
		}

	}
}
