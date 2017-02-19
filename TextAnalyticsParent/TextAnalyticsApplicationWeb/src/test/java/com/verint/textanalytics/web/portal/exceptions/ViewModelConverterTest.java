package com.verint.textanalytics.web.portal.exceptions;

import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.utils.DataUtils;
import com.verint.textanalytics.model.interactions.*;
import com.verint.textanalytics.model.interactions.SpeakerType;
import com.verint.textanalytics.model.security.Channel;
import com.verint.textanalytics.web.uiservices.ViewModelConverter;
import com.verint.textanalytics.web.viewmodel.*;
import lombok.val;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ViewModelConverterTest {

	/**
	 * @throws Exception
	 * Exception
	 */
	private static String text1 = "Android (vulnerable), Windows phone (why would anyone buy into MS again) and iPhone (arrogant but can and probably will fall).";
	private static String text2 = "I cant stand Samsung devices, had one briefly and the junk they had piled on top of Android was astonishing, sold it within a week and bought my first Nexus phone. I don't use any of the 'top ten' battery drainers. Having no friends has it's advantages.";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}

	/**
	 * @throws Exception Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {

	}

	@Test
	public void convertToViewModelInteractionTest() throws ParseException {

		val intr = this.buildInteraction();

		ViewModelConverter converter = new ViewModelConverter();

		com.verint.textanalytics.web.viewmodel.Interaction viewModelIntr = converter.convertToViewModelInteraction(intr);

		assertNotNull(viewModelIntr);
		assertEquals("15", viewModelIntr.getId());
		assertEquals("en", viewModelIntr.getLanguage());
		assertEquals(0, viewModelIntr.getSentiment());
		assertEquals("1271", viewModelIntr.getChannel());
		assertEquals("tenant1", viewModelIntr.getTenant());
		assertEquals(null, viewModelIntr.getCategories());
		assertEquals(Long.valueOf("1424991600000"), viewModelIntr.getStartTimeTicks());

		// @formatter:on
	}

	@Test
	public void convertToViewModelInteractionPreviewTest() throws ParseException {
		val intr = this.buildInteraction();

		ViewModelConverter converter = new ViewModelConverter();

		InteractionPreview viewModelIntrPreview = converter.convertToViewModelInteractionPreview(intr);

		assertNotNull(viewModelIntrPreview);
		assertEquals("15", viewModelIntrPreview.getId());
		assertEquals("en", viewModelIntrPreview.getLanguage());
		assertEquals(0, viewModelIntrPreview.getSentiment());
		assertEquals("1271", viewModelIntrPreview.getChannel());
		assertEquals("tenant1", viewModelIntrPreview.getTenant());
		assertEquals(null, viewModelIntrPreview.getCategories());

		assertEquals(Long.valueOf("1424991600000"), viewModelIntrPreview.getDate());

		assertNotNull(viewModelIntrPreview.getUtterances());
		assertEquals(2, viewModelIntrPreview.getUtterances().size());

		Boolean deviceFound = false;
		Boolean samsungFound = false;
		Boolean galaxyFound = false;

		for (val utterance : viewModelIntrPreview.getUtterances()) {
			switch (utterance.getId()) {
				case "15-1":
					assertNotNull(utterance.getEntities());
					assertEquals("15-1", utterance.getId());
					assertEquals("15", utterance.getParentId());
					assertEquals(DocumentContentType.CHILD, utterance.getContentType());
					assertEquals(com.verint.textanalytics.web.viewmodel.SpeakerType.agent, utterance.getSpeakerType());
					assertEquals(text1, utterance.getText());
					assertNotNull(utterance.getEntities());
					assertEquals(2, utterance.getEntities().size());

					deviceFound = false;
					samsungFound = false;

					for (val entity : utterance.getEntities()) {
						switch (entity.getName()) {
							case "device":
								deviceFound = true;
								assertEquals("/device", entity.getValue());
								break;
							case "samsung":
								samsungFound = true;
								assertEquals("/device/samsung", entity.getValue());
								break;
						}
					}

					assertTrue(deviceFound);
					assertTrue(samsungFound);

					break;
				case "15-2":
					assertNotNull(utterance.getEntities());
					assertEquals("15-2", utterance.getId());
					assertEquals("15", utterance.getParentId());
					assertEquals(DocumentContentType.CHILD, utterance.getContentType());
					assertEquals(com.verint.textanalytics.web.viewmodel.SpeakerType.customer, utterance.getSpeakerType());
					assertEquals(text2, utterance.getText());
					assertNotNull(utterance.getEntities());
					assertEquals(3, utterance.getEntities().size());

					deviceFound = false;
					samsungFound = false;

					for (val entity : utterance.getEntities()) {
						switch (entity.getName()) {
							case "device":
								deviceFound = true;
								assertEquals("/device", entity.getValue());
								break;
							case "samsung":
								samsungFound = true;
								assertEquals("/device/samsung", entity.getValue());
								break;
							case "galaxy":
								galaxyFound = true;
								assertEquals("/device/samsung/galaxy", entity.getValue());
								break;
						}
					}

					assertTrue(deviceFound);
					assertTrue(samsungFound);
					break;
			}
		}
	}

	@Test
	public void convertToViewModelInteractionPreviewCIVTest() throws ParseException {
		val intr = this.buildInteraction();

		ViewModelConverter converter = new ViewModelConverter();

		InteractionPreviewCIV viewModelIntrPreviewCIV = converter.convertToViewModelInteractionPreviewCIV(intr);

		assertNotNull(viewModelIntrPreviewCIV);
		InteractionCIV interactionCIV = viewModelIntrPreviewCIV.getInteraction();
		assertNotNull(interactionCIV);

		assertEquals("15", viewModelIntrPreviewCIV.getMessageIdentifier());
		assertEquals("tenant1", viewModelIntrPreviewCIV.getTenant());
		assertEquals("1271", viewModelIntrPreviewCIV.getChannel());
		assertEquals(SourceType.Chat, viewModelIntrPreviewCIV.getInteractionType());

		assertEquals("en", interactionCIV.getLanguage());
		assertEquals("2015-02-26T23:00:00.000Z", interactionCIV.getMeta_dt_interactionStartTime());
		assertEquals("2015-02-26T23:00:00.000Z", interactionCIV.getMeta_dt_interactionEndTime());

		assertNotNull(interactionCIV.getMeta_ss_customerNames());
		for (String name : interactionCIV.getMeta_ss_customerNames()) {
			switch (name) {
				case "customer1":
					break;
				default:
					assert (false);
			}
		}

		assertNotNull(interactionCIV.getMeta_ss_customerTimeZone());
		for (String time : interactionCIV.getMeta_ss_customerTimeZone()) {
			assertEquals("GMT-01:00", time);
		}
		//meta_ss_employeesNames
		//meta_ss_employeeTimeZone

		assertNotNull(interactionCIV.getMeta_ss_employeesNames());
		for (String name : interactionCIV.getMeta_ss_employeesNames()) {
			switch (name) {
				case "sup1":
					break;
				case "sup2":
					break;
				default:
					assert (false);
			}
		}

		assertNotNull(interactionCIV.getMeta_ss_employeeTimeZone());
		for (String time : interactionCIV.getMeta_ss_employeeTimeZone()) {
			assertEquals("GMT+00:00", time);
		}

	}

	@Test
	public void convertToViewModelUtterancesCIVTest() throws ParseException {
		val intr = this.buildInteraction();

		ViewModelConverter converter = new ViewModelConverter();

		InteractionPreviewCIV viewModelIntrPreviewCIV = converter.convertToViewModelInteractionPreviewCIV(intr);

		assertNotNull(viewModelIntrPreviewCIV);
		InteractionCIV interactionCIV = viewModelIntrPreviewCIV.getInteraction();
		assertNotNull(interactionCIV);

		List<UtteranceCIV> utterances = interactionCIV.getUtterances();
		assertNotNull(utterances);

		for (val utterance : utterances) {
			switch (utterance.getId()) {
				case "15-1":
					assertEquals(text1, utterance.getText());
					assertEquals(com.verint.textanalytics.web.viewmodel.SpeakerType.agent, utterance.getMeta_s_speaker());
					break;
				case "15-2":
					assertEquals(text2, utterance.getText());
					assertEquals(com.verint.textanalytics.web.viewmodel.SpeakerType.customer, utterance.getMeta_s_speaker());
					break;
			}
		}

	}

	@Test
	public void updateUtteranceHighlightsCIVTest() throws ParseException {
		val intr = this.buildInteraction();
		ViewModelConverter converter = new ViewModelConverter();

		InteractionPreviewCIV viewModelIntrPreviewCIV = converter.convertToViewModelInteractionPreviewCIV(intr);
		assertNotNull(viewModelIntrPreviewCIV);

		HighlightCIVObject highlightObject = viewModelIntrPreviewCIV.getHighlightObject();
		assertNotNull(highlightObject);

		Boolean deviceFound1 = false;
		Boolean samsungFound1 = false;
		Boolean deviceFound2 = false;
		Boolean samsungFound2 = false;

		for (BaseHighlightCIV highlight : highlightObject.getHighlights()) {
			switch (highlight.getData()) {
				case "android1":
					deviceFound1 = true;
					assertEquals(10, highlight.getStart());
					assertEquals(17, highlight.getEnd());
					assertEquals("15-1", highlight.getUtteranceId());
					assertEquals(HighlightTypeCIV.Search, highlight.getType());
					break;
				case "samsung1":
					samsungFound1 = true;
					assertEquals(20, highlight.getStart());
					assertEquals(30, highlight.getEnd());
					assertEquals("15-1", highlight.getUtteranceId());
					assertEquals(HighlightTypeCIV.Search, highlight.getType());
					break;
				case "android2":
					deviceFound2 = true;
					assertEquals(40, highlight.getStart());
					assertEquals(45, highlight.getEnd());
					assertEquals("15-2", highlight.getUtteranceId());
					assertEquals(HighlightTypeCIV.Search, highlight.getType());
					break;
				case "samsung2":
					samsungFound2 = true;
					assertEquals(50, highlight.getStart());
					assertEquals(60, highlight.getEnd());
					assertEquals("15-2", highlight.getUtteranceId());
					assertEquals(HighlightTypeCIV.Search, highlight.getType());
					break;
				default:
					assert (false);
			}
		}

		assertTrue(deviceFound1);
		assertTrue(samsungFound1);
		assertTrue(deviceFound2);
		assertTrue(samsungFound2);


	}

	private com.verint.textanalytics.model.interactions.Interaction buildInteraction() throws ParseException {

		val intr = new com.verint.textanalytics.model.interactions.Interaction();

		//@formatter:off
		intr.setId("15")						
			.setContentType(DocumentContentType.PARENT)
			.setSourceType(SourceType.Chat)
			.setLanguage("en")
			.setSentiment(SentimentType.Neutral)
			.setIsSentimentMixed(false)
			.setChannel("1271")
			.setTenant("tenant1")
			.setStartTime(DataUtils.getDateFromISO8601String("2015-02-26T23:00:00Z"))
			.setAgentLocalStartTime(DataUtils.getDateFromISO8601String("2015-04-26T23:00:00Z"))
			.setCustomerLocalStartTime(DataUtils.getDateFromISO8601String("2015-05-26T23:00:00Z"))
			.setCategories(null);

		intr.setAgentNames( Arrays.asList("sup1", "sup2"));
		intr.setCustomerNames( Arrays.asList("customer1"));
		intr.setCustomerTimeZone("-01:00");
		intr.setAgentTimeZone("+03:00");

		val utterances = new ArrayList<com.verint.textanalytics.model.interactions.Utterance>();
		val utterance1 = new com.verint.textanalytics.model.interactions.Utterance();
		val utterance2 = new com.verint.textanalytics.model.interactions.Utterance();
		
		Entity[]  ent1 = new Entity[] { 
										new Entity("device", "/device", 1), 
										new Entity("samsung", "/device/samsung",2 ) 
									  };
		
		TermHighlight[] ht1 = new TermHighlight[] { 
										   new TermHighlight("android1", 10 , 17),
										   new TermHighlight("samsung1", 20, 30) 
										  };
		
		utterance1.setId("15-1")
				  .setParentId("15")
				  .setContentType(DocumentContentType.CHILD)
				  .setSpeakerType(SpeakerType.Agent)
				  .setText(text1)
				  .setEntities(Arrays.asList(ent1))
				  .setDate(DataUtils.getDateFromISO8601String("2015-02-26T23:00:00Z"))
				  .setTermsHighlighting(Arrays.asList(ht1));
		
		
		Entity[]  ent2 = new Entity[] { new Entity("device", "/device", 1), 
										new Entity("samsung", "/device/samsung", 2),
										new Entity("galaxy", "/device/samsung/galaxy", 3)
									  };
		
		TermHighlight[] ht2 = new TermHighlight[] { 
											new TermHighlight("android2", 40 , 45),
											new TermHighlight("samsung2", 50, 60) 
										  };
		
		utterance2.setId("15-2")
		  .setParentId("15")
		  .setContentType(DocumentContentType.CHILD)
		  .setSpeakerType(SpeakerType.Customer)
		  .setText(text2)
		  .setEntities(Arrays.asList(ent2))
		  .setDate(DataUtils.getDateFromISO8601String("2015-02-26T23:00:00Z"))
		  .setTermsHighlighting(Arrays.asList(ht2));
		
		utterances.add(utterance1);
		utterances.add(utterance2);
		
		intr.setUtterances(utterances);
		
		return intr;
	}
	
	
	@Test
	public void convertToViewModelConfigurationTest() throws ParseException {
		
		val converter = new ViewModelConverter();
		val channels = new ArrayList<Channel>();
		channels.add(new Channel("channel1"));
		channels.add(new Channel("channel2"));
		
		ApplicationConfiguration appConfig = new ApplicationConfiguration();
		appConfig.setAutoCompletePrefixMinLengthConfiguration(3);
		appConfig.setDarwinRestRequestTimeout(300);
		appConfig.setSearchTermsSuggestionType(SearchTermsSuggestionType.FacetOnTerms.getSuggestionsType());
		
		val viewModelConfiguration = converter.convertToViewModelConfiguration(false, channels, appConfig);

		assertNotNull(viewModelConfiguration);
		assertEquals(false, viewModelConfiguration.getCustomerUsageAnalyticsEnabled());
		assertNotNull(viewModelConfiguration.getTenantChannels());

		assertEquals(2, viewModelConfiguration.getTenantChannels().size());
		assertEquals(3, viewModelConfiguration.getAutoCompletePrefixMinLengthConfiguration());
		assertEquals(300, viewModelConfiguration.getAjaxRequestTimeout());
		assertEquals(SearchTermsSuggestionType.FacetOnTerms.getSuggestionsType(), viewModelConfiguration.getSearchTermsSuggestionType());
	}
			
	@Test
	public void convertToViewModelSuggestionItemTest() throws ParseException {
		
		val converter = new ViewModelConverter();

		SearchSuggestionResult inputSuggestion = new SearchSuggestionResult();
		val suggestion = new ArrayList<SearchSuggestion>();
		suggestion.add(new SearchSuggestion("to",10));
		suggestion.add(new SearchSuggestion("tow",5));
		suggestion.add(new SearchSuggestion("took",4));
		suggestion.add(new SearchSuggestion("town",3));
		suggestion.add(new SearchSuggestion("toal",1));
		
		inputSuggestion.setTotalNumberFound(10);
		inputSuggestion.setSuggestions(suggestion);
		
		List<SuggestionItem> viewModelSuggestion = converter.convertToViewModelSuggestionItem(inputSuggestion);
		
		assertEquals(5, viewModelSuggestion.size());
	}
	
	@Test
	public void convertToViewModelSuggestionItemMoreThen100Test() throws ParseException {
	
		val converter = new ViewModelConverter();

		SearchSuggestionResult inputSuggestion = new SearchSuggestionResult();
		val suggestion = new ArrayList<SearchSuggestion>();
		suggestion.add(new SearchSuggestion("to",11));
		suggestion.add(new SearchSuggestion("tow",5));
		suggestion.add(new SearchSuggestion("took",4));
		suggestion.add(new SearchSuggestion("town",3));
		suggestion.add(new SearchSuggestion("toal",1));
		
		inputSuggestion.setTotalNumberFound(10);
		inputSuggestion.setSuggestions(suggestion);
		
		List<SuggestionItem> viewModelSuggestion = converter.convertToViewModelSuggestionItem(inputSuggestion);
		
		assertTrue(100.0 ==  viewModelSuggestion.get(0).getPrecent());
		
	}
	
	
}
