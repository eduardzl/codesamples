package com.verint.textanalytics.bl.applicationservices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.verint.textanalytics.dal.darwin.TextEngineSchemaService;
import com.verint.textanalytics.model.interactions.HighlightResult;
import lombok.val;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.verint.textanalytics.dal.darwin.EASearchResponseConverter;
import com.verint.textanalytics.dal.darwin.SolrResponseConverter;
import com.verint.textanalytics.model.interactions.EntityHighlight;
import com.verint.textanalytics.model.interactions.TermHighlight;
import com.verint.textanalytics.model.interactions.UtteranceHighlights;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

@PowerMockIgnore("javax.management.*")
@RunWith(PowerMockRunner.class)
public class HighlightsMergerTest extends BaseTest {

	// Entities Facet
	private String interactionHighlightsAgentResourcePath;
	private String interactionHighlightsCustomerResourcePath;
	private String interactionHighlightsNoSPSResourcePath;

	private String interactionHighlightsAgentUtteranceResourcePath;
	private String interactionHighlightsCustomerUtteranceResourcePath;
	private String interactionHighlightsNoSPSUtteranceResourcePath;

	private String interactionHighlightsAgentHighlightsResourcePath;
	private String interactionHighlightsCustomerHighlightsResourcePath;
	private String interactionHighlightsNoSPSHighlightsResourcePath;

	// Topics
	private String interactionHighlightsAgentResourcePathWithTopicsResourcePath;
	private String interactionHighlightsNoSPSResourcePathWithTopicsResourcePath;

	private String tenant = "tenant";
	private String channel = "channel";

	@Mock
	private TextEngineSchemaService textEngineConfigurationService;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {


	}

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);

		Mockito.when(textEngineConfigurationService.getChannelDynamicFields(tenant, channel)).thenReturn(null);

		// Highlights
		interactionHighlightsAgentResourcePath = "highlightsmerge/HighlightsMergeAgent.txt";
		interactionHighlightsCustomerResourcePath = "highlightsmerge/HighlightsMergeCustomer.txt";
		interactionHighlightsNoSPSResourcePath = "highlightsmerge/HighlightsMergeNoSPS.txt";

		interactionHighlightsAgentUtteranceResourcePath = "highlightsmerge/HighlightsMergeAgentUtterance.txt";
		interactionHighlightsCustomerUtteranceResourcePath = "highlightsmerge/HighlightsMergeCustomerUtterance.txt";
		interactionHighlightsNoSPSUtteranceResourcePath = "highlightsmerge/HighlightsMergeNoSPSUtterance.txt";

		interactionHighlightsAgentHighlightsResourcePath = "highlightsmerge/HighlightsMergeAgentHighlights.txt";
		interactionHighlightsCustomerHighlightsResourcePath = "highlightsmerge/HighlightsMergeCustomerHighlights.txt";
		interactionHighlightsNoSPSHighlightsResourcePath = "highlightsmerge/HighlightsMergeNoSPSHighlights.txt";

		// Topics
		interactionHighlightsAgentResourcePathWithTopicsResourcePath = "highlightsmerge/HighlightsMergeAgentWithTopics.txt";
		interactionHighlightsNoSPSResourcePathWithTopicsResourcePath = "highlightsmerge/HighlightsMergeNoSPSWithTopics.txt";
	}


	@Test
	public void mergeHighlightsTest() throws IOException {
		val solrResponseConverter = this.getSolrResponseConverter();

		String agentHighlightsResponse = this.getResourceAsString(interactionHighlightsAgentResourcePath);
		HighlightResult agentHighlights = solrResponseConverter.getHighlights(agentHighlightsResponse, tenant, channel);

		String customerHighlightsResponse = this.getResourceAsString(interactionHighlightsCustomerResourcePath);
		HighlightResult customerHighlights = solrResponseConverter.getHighlights(customerHighlightsResponse, tenant, channel);

		String noSPSHighlightsJson = this.getResourceAsString(interactionHighlightsNoSPSResourcePath);
		List<List<UtteranceHighlights>> noSpeakerHighlights = new ArrayList<List<UtteranceHighlights>>();
		noSpeakerHighlights.add(solrResponseConverter.getHighlights(noSPSHighlightsJson, tenant, channel).getHighlights());



		assertNotNull(agentHighlights);
		assertNotNull(customerHighlights);
		assertNotNull(noSpeakerHighlights);

		val highlightsMerger = new HighlightsMerger();
		List<UtteranceHighlights> mergedUtterances = highlightsMerger.mergeHighlights(agentHighlights.getHighlights(), customerHighlights.getHighlights(), noSpeakerHighlights);

		this.validateUtterancesHighlights(mergedUtterances);
	}

	@Test
	public void mergeHighlightsUttertancesMergeTest() throws IOException {
		val solrResponseConverter = this.getSolrResponseConverter();

		String agentHighlightsResponse = this.getResourceAsString(interactionHighlightsAgentUtteranceResourcePath);
		HighlightResult agentHighlights = solrResponseConverter.getHighlights(agentHighlightsResponse, tenant, channel);

		String customerHighlightsResponse = this.getResourceAsString(interactionHighlightsCustomerUtteranceResourcePath);
		HighlightResult customerHighlights = solrResponseConverter.getHighlights(customerHighlightsResponse, tenant, channel);

		String noSPSHighlightsJson = this.getResourceAsString(interactionHighlightsNoSPSUtteranceResourcePath);
		List<List<UtteranceHighlights>> noSpeakerHighlights = new ArrayList<List<UtteranceHighlights>>();
		noSpeakerHighlights.add(solrResponseConverter.getHighlights(noSPSHighlightsJson, tenant, channel).getHighlights());

		assertNotNull(agentHighlights);
		assertNotNull(customerHighlights);
		assertNotNull(noSpeakerHighlights);

		val highlightsMerger = new HighlightsMerger();
		List<UtteranceHighlights> mergedUtterances = highlightsMerger.mergeHighlights(agentHighlights.getHighlights(), customerHighlights.getHighlights(), noSpeakerHighlights);

		this.validateUtterancesHighlights(mergedUtterances);
	}

	@Test
	public void mergeHighlightsHighlightsMergeTest() throws IOException {
		val solrResponseConverter = this.getSolrResponseConverter();

		String agentHighlightsResponse = this.getResourceAsString(interactionHighlightsAgentHighlightsResourcePath);
		HighlightResult agentHighlights = solrResponseConverter.getHighlights(agentHighlightsResponse, tenant, channel);

		String customerHighlightsResponse = this.getResourceAsString(interactionHighlightsCustomerHighlightsResourcePath);
		HighlightResult customerHighlights = solrResponseConverter.getHighlights(customerHighlightsResponse, tenant, channel);

		String noSPSHighlightsJson = this.getResourceAsString(interactionHighlightsNoSPSHighlightsResourcePath);
		List<List<UtteranceHighlights>> noSpeakerHighlights = new ArrayList<List<UtteranceHighlights>>();
		noSpeakerHighlights.add(solrResponseConverter.getHighlights(noSPSHighlightsJson, tenant, channel).getHighlights());


		assertNotNull(agentHighlights);
		assertNotNull(customerHighlights);
		assertNotNull(noSpeakerHighlights);

		val highlightsMerger = new HighlightsMerger();
		List<UtteranceHighlights> mergedUtterances = highlightsMerger.mergeHighlights(agentHighlights.getHighlights(), customerHighlights.getHighlights(), noSpeakerHighlights);

		this.validateUtterancesHighlights(mergedUtterances);
	}

	@Test
	public void mergeHighlightsNoAgentUtterancesTest() throws IOException {
		val solrResponseConverter = this.getSolrResponseConverter();

		String customerHighlightsResponse = this.getResourceAsString(interactionHighlightsCustomerHighlightsResourcePath);
		HighlightResult customerHighlights = solrResponseConverter.getHighlights(customerHighlightsResponse, tenant, channel);

		String noSPSHighlightsJson = this.getResourceAsString(interactionHighlightsNoSPSHighlightsResourcePath);
		List<List<UtteranceHighlights>> noSpeakerHighlights = new ArrayList<List<UtteranceHighlights>>();
		noSpeakerHighlights.add(solrResponseConverter.getHighlights(noSPSHighlightsJson, tenant, channel).getHighlights());

		assertNotNull(customerHighlights);
		assertNotNull(noSpeakerHighlights);

		val highlightsMerger = new HighlightsMerger();
		List<UtteranceHighlights> mergedUtterances = highlightsMerger.mergeHighlights(null, customerHighlights.getHighlights(), noSpeakerHighlights);

		this.validateUtterancesHighlights(mergedUtterances);
	}

	@Test
	public void mergeHighlightsNoCustomerUtterancesTest() throws IOException {
		val solrResponseConverter = this.getSolrResponseConverter();

		String agentHighlightsResponse = this.getResourceAsString(interactionHighlightsAgentHighlightsResourcePath);
		HighlightResult agentHighlights = solrResponseConverter.getHighlights(agentHighlightsResponse, tenant, channel);

		String noSPSHighlightsJson = this.getResourceAsString(interactionHighlightsNoSPSHighlightsResourcePath);
		List<List<UtteranceHighlights>> noSpeakerHighlights = new ArrayList<List<UtteranceHighlights>>();
		noSpeakerHighlights.add(solrResponseConverter.getHighlights(noSPSHighlightsJson, tenant, channel).getHighlights());

		assertNotNull(agentHighlights);
		assertNotNull(noSpeakerHighlights);

		val highlightsMerger = new HighlightsMerger();
		List<UtteranceHighlights> mergedUtterances = highlightsMerger.mergeHighlights(agentHighlights.getHighlights(), null, noSpeakerHighlights);

		this.validateUtterancesHighlights(mergedUtterances);
	}

	@Test
	public void mergeHighlightsNoSPSUtterancesTest() throws IOException {
		val solrResponseConverter = this.getSolrResponseConverter();

		String agentHighlightsResponse = this.getResourceAsString(interactionHighlightsAgentHighlightsResourcePath);
		HighlightResult agentHighlights = solrResponseConverter.getHighlights(agentHighlightsResponse, tenant, channel);

		String customerHighlightsResponse = this.getResourceAsString(interactionHighlightsCustomerHighlightsResourcePath);
		HighlightResult customerHighlights = solrResponseConverter.getHighlights(customerHighlightsResponse, tenant, channel);

		assertNotNull(agentHighlights);
		assertNotNull(customerHighlights);

		val highlightsMerger = new HighlightsMerger();
		List<UtteranceHighlights> mergedUtterances = highlightsMerger.mergeHighlights(agentHighlights.getHighlights(), customerHighlights.getHighlights(), null);

		this.validateUtterancesHighlights(mergedUtterances);
	}

	@Test
	public void mergeHighlightsNoAgentAndCustomerUtterancesTest() throws IOException {
		val solrResponseConverter = this.getSolrResponseConverter();

		String noSPSHighlightsJson = this.getResourceAsString(interactionHighlightsNoSPSResourcePath);
		List<List<UtteranceHighlights>> noSpeakerHighlights = new ArrayList<List<UtteranceHighlights>>();
		noSpeakerHighlights.add(solrResponseConverter.getHighlights(noSPSHighlightsJson, tenant, channel).getHighlights());

		assertNotNull(noSPSHighlightsJson);

		val highlightsMerger = new HighlightsMerger();
		List<UtteranceHighlights> mergedUtterances = highlightsMerger.mergeHighlights(null, null, noSpeakerHighlights);

		this.validateUtterancesHighlights(mergedUtterances);
	}

	@Test
	public void mergeHighlightsNoAgentAndNoSPSUtterancesTest() throws IOException {
		val solrResponseConverter = this.getSolrResponseConverter();

		String customerHighlightsResponse = this.getResourceAsString(interactionHighlightsCustomerResourcePath);
		HighlightResult customerHighlights = solrResponseConverter.getHighlights(customerHighlightsResponse, tenant, channel);

		assertNotNull(customerHighlights);

		val highlightsMerger = new HighlightsMerger();
		List<UtteranceHighlights> mergedUtterances = highlightsMerger.mergeHighlights(null, customerHighlights.getHighlights(), null);

		this.validateUtterancesHighlights(mergedUtterances);
	}

	@Test
	public void mergeHighlightsNoCustomerAndNoSPSUtterancesTest() throws IOException {
		val solrResponseConverter = this.getSolrResponseConverter();

		String agentHighlightsResponse = this.getResourceAsString(interactionHighlightsAgentResourcePath);
		HighlightResult agentHighlights = solrResponseConverter.getHighlights(agentHighlightsResponse, tenant, channel);

		assertNotNull(agentHighlights);

		val highlightsMerger = new HighlightsMerger();
		List<UtteranceHighlights> mergedUtterances = highlightsMerger.mergeHighlights(agentHighlights.getHighlights(), null, null);

		this.validateUtterancesHighlights(mergedUtterances);
	}

	// merge highlight with topic

	@Test
	public void mergeHighlightsWithTopicsTest() throws IOException {
		val eaSearchResponseConverter = new EASearchResponseConverter();
		val solrResponseConverter = this.getSolrResponseConverter();
		eaSearchResponseConverter.setSolrResponseConverter(solrResponseConverter);
		eaSearchResponseConverter.setTextEngineConfigurationService(textEngineConfigurationService);

		String agentHighlightsResponse = this.getResourceAsString(interactionHighlightsAgentResourcePathWithTopicsResourcePath);
		HighlightResult agentHighlights = eaSearchResponseConverter.getHighlights(agentHighlightsResponse, tenant, channel);

		String noSPSHighlightsJson = this.getResourceAsString(interactionHighlightsNoSPSResourcePathWithTopicsResourcePath);

		List<List<UtteranceHighlights>> noSpeakerHighlights = new ArrayList<List<UtteranceHighlights>>();
		noSpeakerHighlights.add(eaSearchResponseConverter.getHighlights(noSPSHighlightsJson, tenant, channel).getHighlights());

		assertNotNull(agentHighlights);
		assertNotNull(noSpeakerHighlights);

		val highlightsMerger = new HighlightsMerger();
		List<UtteranceHighlights> mergedUtterances = highlightsMerger.mergeHighlights(agentHighlights.getHighlights(), null, noSpeakerHighlights);

		for (UtteranceHighlights utteranceHighlights : mergedUtterances) {
			if (utteranceHighlights.getDocumentId().equals("1-4")) {

				assertTrue(utteranceHighlights.getTermHighlights().size() == 1);

				for (TermHighlight termHighlight : utteranceHighlights.getTermHighlights()) {
					assertNotNull(termHighlight.getTerm());
					assertTrue(termHighlight.getStarts() != 0);
					assertTrue(termHighlight.getEnds() != 0);
				}

				assertTrue(utteranceHighlights.getEntitiesHighlights().size() == 6);
				for (EntityHighlight topicHighlight : utteranceHighlights.getEntitiesHighlights()) {
					assertNotNull(topicHighlight.getTopic());
					assertTrue(topicHighlight.getStarts() != 0);
					assertTrue(topicHighlight.getEnds() != 0);
				}

			}
		}
	}

	private void validateUtterancesHighlights(List<UtteranceHighlights> mergedUtterances) {
		assertNotNull(mergedUtterances);
		assertEquals(17, mergedUtterances.size());

		for (val utterance : mergedUtterances) {
			int highlightsCount = utterance.getTermHighlights().size();

			switch (utterance.getDocumentId()) {
				case "15-1":
					assertEquals(0, highlightsCount);
					break;
				case "15-2":
					assertEquals(1, highlightsCount);
					break;
				case "15-3":
					assertEquals(0, highlightsCount);
					break;
				case "15-4":
					assertEquals(0, highlightsCount);
					break;
				case "7-1":
					assertEquals(1, highlightsCount);
					break;
				case "7-2":
					assertEquals(2, highlightsCount);
					break;
				case "7-3":
					assertEquals(1, highlightsCount);
					break;
				case "7-4":
					assertEquals(1, highlightsCount);
					break;
				case "8-1":
					assertEquals(1, highlightsCount);
					break;
				case "8-2":
					assertEquals(1, highlightsCount);
					break;
				case "8-3":
					assertEquals(0, highlightsCount);
					break;
				case "8-4":
					assertEquals(1, highlightsCount);
					break;
				case "8-5":
					assertEquals(1, highlightsCount);
					break;
				case "19-1":
					assertEquals(0, highlightsCount);
					break;
				case "19-2":
					assertEquals(0, highlightsCount);
					break;
				case "19-3":
					assertEquals(1, highlightsCount);
					break;
				case "19-4":
					assertEquals(1, highlightsCount);
					break;

			}
		}
	}


	private SolrResponseConverter getSolrResponseConverter() {
		SolrResponseConverter converter = new SolrResponseConverter();
		converter.setTextEngineConfigurationService(textEngineConfigurationService);
		return converter;
	}
}
