package com.verint.textanalytics.bl.applicationservices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.verint.textanalytics.bl.applicationservices.SnippetsGeneratorTest.InteractionBuilder;
import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.dal.darwin.TextAnalyticsProvider;
import com.verint.textanalytics.model.facets.SpeakerQueryType;
import com.verint.textanalytics.model.facets.TextElementType;
import com.verint.textanalytics.model.facets.TextElementsFacetNode;
import com.verint.textanalytics.model.interactions.BaseHighlight;

public class SearchInteractionsServiceTest {

	@Mock
	private SearchInteractionsService searchService;

	private TextAnalyticsProvider textAnalyticsProvider;

	private ConfigurationManager configurationManager;


	/**
	 * 
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		ApplicationConfiguration mockedApplicationConfiguration = mock(ApplicationConfiguration.class);
		configurationManager = mock(ConfigurationManager.class);

		textAnalyticsProvider = mock(TextAnalyticsProvider.class);

		Mockito.when(mockedApplicationConfiguration.getInteractionSnippetsMaxPrecedingWords()).thenReturn(2);
		Mockito.when(mockedApplicationConfiguration.getInteractionSnippetsMaxFollowingWords()).thenReturn(2);
		Mockito.when(mockedApplicationConfiguration.getInteractionSnippetsFullTextMaxLength()).thenReturn(2000);
		Mockito.when(mockedApplicationConfiguration.getInteractionSnippetsMaxUtterancesToOperate()).thenReturn(5);
		Mockito.when(configurationManager.getApplicationConfiguration()).thenReturn(mockedApplicationConfiguration);
	}

	@Test
	public void highlightOverlapManager_simple() {

		String text = "I realized that this arcticle is great";
		// ____________________________|________________|___|
		// ____________________________|****************|***|
		//                                     <1>       <2>
		String term1 = "this arcticle is great"; // term
		String term2 = "great"; // topic

		List<BaseHighlight> highlights = new ArrayList<>();

		SnippetsGeneratorTest snippetsG = new SnippetsGeneratorTest();
		InteractionBuilder builder = snippetsG.new InteractionBuilder();
		highlights.add(builder.generateTermHighlight(text, term1));
		highlights.add(builder.generateTopicHighlight(text, term2, "2/great"));

		HighlightOverlapManager mngr = new HighlightOverlapManager();
		highlights = mngr.solveOverlappingForHighlights(highlights);
		assertTrue(highlights.size() == 2);

		BaseHighlight h1 = highlights.get(0);
		assertTrue(h1.getStarts() == text.indexOf(term1));
		assertTrue(h1.getEnds() == text.indexOf(term2));

		BaseHighlight h2 = highlights.get(1);
		assertTrue(h2.getStarts() == text.indexOf(term2));
		assertTrue(h2.getEnds() == text.indexOf(term2) + term2.length());
	}

	@Test
	public void highlightOverlapManager_simple1() {

		String text;
		text = "and a per ticket service fee";
		// _______________|____________|_____
		// ______________________|_____|____
		// _______________|******|*****|____
		// ______________10______17____24___ (Starting from 0)

		String term1 = "ticket service"; // term
		String term2 = "service"; // topic

		List<BaseHighlight> highlights = new ArrayList<>();

		SnippetsGeneratorTest snippetsG = new SnippetsGeneratorTest();
		InteractionBuilder builder = snippetsG.new InteractionBuilder();
		highlights.add(builder.generateTermHighlight(text, term1));
		highlights.add(builder.generateTopicHighlight(text, term2, "2/great"));

		HighlightOverlapManager mngr = new HighlightOverlapManager();
		highlights = mngr.solveOverlappingForHighlights(highlights);
		assertTrue(highlights.size() == 2);

		BaseHighlight h1 = highlights.get(0);
		assertTrue(h1.getStarts() == text.indexOf(term1));
		assertTrue(h1.getEnds() == text.indexOf(term1) + term2.length());

		BaseHighlight h2 = highlights.get(1);
		assertTrue(h2.getStarts() == text.indexOf(term2));
		assertTrue(h2.getEnds() == text.indexOf(term2) + term2.length());
	}

	@Test
	public void highlightOverlapManager_complex1() {

		String text = "this arcticle is great and very helpful.";
		// h1 ______________|******|
		// h2 __________________________|***|
		// h3 _________________________________________|*****|
		// h4 ______________|***************|
		// h5 __________________________|********************|
		// _________________|**H1**|*H2*|H3*|****H4****|*H5**|

		String term1 = "arcticle"; // term
		String term2 = "great"; // topic
		String term3 = "helpful"; // topic
		String term4 = "arcticle is great"; // topic
		String term5 = "great and very helpful"; // topic

		List<BaseHighlight> highlights = new ArrayList<>();

		SnippetsGeneratorTest snippetsG = new SnippetsGeneratorTest();
		InteractionBuilder builder = snippetsG.new InteractionBuilder();
		highlights.add(builder.generateTermHighlight(text, term1));
		highlights.add(builder.generateTopicHighlight(text, term2, term2));
		highlights.add(builder.generateTopicHighlight(text, term3, term3));
		highlights.add(builder.generateTopicHighlight(text, term4, term4));
		highlights.add(builder.generateTopicHighlight(text, term5, term5));

		HighlightOverlapManager mngr = new HighlightOverlapManager();
		highlights = mngr.solveOverlappingForHighlights(highlights);
		assertTrue(highlights.size() == 5);

		BaseHighlight h1 = highlights.get(0);
		assertTrue(h1.getStarts() == text.indexOf(term1));
		assertTrue(h1.getEnds() == text.indexOf(term1) + term1.length());

		BaseHighlight h2 = highlights.get(1);
		assertTrue(h2.getStarts() == text.indexOf(term1) + term1.length());
		assertTrue(h2.getEnds() == text.indexOf(term2));

		BaseHighlight h3 = highlights.get(2);
		assertTrue(h3.getStarts() == text.indexOf(term2));
		assertTrue(h3.getEnds() == text.indexOf(term2) + term2.length());

		BaseHighlight h4 = highlights.get(3);
		assertTrue(h4.getStarts() == text.indexOf(term4) + term4.length());
		assertTrue(h4.getEnds() == text.indexOf(term3));

		BaseHighlight h5 = highlights.get(4);
		assertTrue(h5.getStarts() == text.indexOf(term3));
		assertTrue(h5.getEnds() == text.indexOf(term3) + term3.length());
	}

	@Test
	public void highlightOverlapManager_noOverlap() {
		String text = "Mingying, I apologize, but we have limited chat support for cancellation and exchanges of airline reservation. You will need to speak with one of our Air Travel Service Specialists. Please call us at 1-877-477-7441. You will need to enter your trip number and your call will be sent directly to an agent. Please remember that all changes must be made before your currently scheduled departure date, and they may incur an airline imposed per ticket change fee and a per ticket service fee. You can also review the specific rules and restrictions associated with your reservation in the Need to Make a Change section of your itinerary displayed on our website.";
		String term = "service"; // topic

		text = text.toLowerCase();

		List<BaseHighlight> highlights = new ArrayList<>();

		SnippetsGeneratorTest snippetsG = new SnippetsGeneratorTest();
		InteractionBuilder builder = snippetsG.new InteractionBuilder();
		highlights.add(builder.generateTopicHighlight(text, term, term));
		highlights.add(builder.generateTopicHighlight(text, term, term, text.indexOf(term) + term.length()));

		HighlightOverlapManager mngr = new HighlightOverlapManager();
		highlights = mngr.solveOverlappingForHighlights(highlights);
		assertTrue(highlights.size() == 2);

		BaseHighlight h1 = highlights.get(0);
		assertTrue(h1.getStarts() == text.indexOf(term));
		assertTrue(h1.getEnds() == text.indexOf(term) + term.length());

		BaseHighlight h2 = highlights.get(1);
		assertTrue(h2.getStarts() == text.indexOf(term, text.indexOf(term) + 5));
		assertTrue(h2.getEnds() == text.indexOf(term, text.indexOf(term) + 5) + term.length());
	}

	@Test
	public void highlightOverlapManager_embeddedWord() {
		String text = "When you open the link, all you need to do is to provide a U.S. or Canadian phone number and you will be connected to a phone service agent. Here's the link:";
		// ________________________________________________________________________________________|****|____________________________________________________
		// ____________________________________________________________________________________________________________________________________|****|
		// ____________________________________________________________________________________________________________________________________|************|
		// ________________________________________________________________________________________|****|______________________________________|*****|******|
		// _______________________________________________________________________________________76____81____________________________________120__125_____133
		//                                                                                                                                         126
		// ________________________________________________________________________________________h1-term1________________________________h2-term1___h3-term2

		String term1 = "phone", term2 = "phone service";

		List<BaseHighlight> highlights = new ArrayList<>();

		SnippetsGeneratorTest snippetsG = new SnippetsGeneratorTest();
		InteractionBuilder builder = snippetsG.new InteractionBuilder();
		highlights.add(builder.generateTopicHighlight(text, term1, term1));
		highlights.add(builder.generateTopicHighlight(text, term1, term1, text.indexOf(term1) + term1.length()));
		highlights.add(builder.generateTopicHighlight(text, term2, term2, text.indexOf(term1) + term1.length()));

		HighlightOverlapManager mngr = new HighlightOverlapManager();
		highlights = mngr.solveOverlappingForHighlights(highlights);
		assertEquals(highlights.size(), 3);

		BaseHighlight h1 = highlights.get(0);
		assertEquals(h1.getStarts(), text.indexOf(term1));
		assertEquals(h1.getEnds(), text.indexOf(term1) + term1.length());
		assertEquals(h1.getContents().size(), 1);

		BaseHighlight h2 = highlights.get(1);
		assertEquals(h2.getStarts(), text.indexOf(term1, text.indexOf(term1)+1));
		assertEquals(h2.getEnds(), text.indexOf(term1, text.indexOf(term1)+1) + term1.length());
		assertEquals(h2.getContents().size(), 2);

		BaseHighlight h3 = highlights.get(2);
		String term3 = " service";
		assertEquals(h3.getStarts(), text.indexOf(term3));
		assertEquals(h3.getEnds(), text.indexOf(term3) + term3.length());
		assertEquals(h3.getContents().size(), 1);

	}

}
