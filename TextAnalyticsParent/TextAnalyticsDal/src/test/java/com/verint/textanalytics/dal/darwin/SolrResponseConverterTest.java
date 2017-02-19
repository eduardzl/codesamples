package com.verint.textanalytics.dal.darwin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionException;
import com.verint.textanalytics.common.utils.StringUtils;
import com.verint.textanalytics.model.analyze.*;
import com.verint.textanalytics.model.documentSchema.*;
import com.verint.textanalytics.model.facets.*;
import com.verint.textanalytics.model.interactions.*;
import com.verint.textanalytics.model.trends.*;

import lombok.val;
import propel.core.utils.Linq;

/**
 * Json Response Converter Test.
 * 
 * @author imor
 *
 */
@PowerMockIgnore("javax.management.*")
@RunWith(PowerMockRunner.class)
public class SolrResponseConverterTest extends BaseTest {

	private static final double DELTA = 0.0000001;

	protected static String s_searchInteractionsJsonResourcePath;

	// Entities Facet
	protected static String s_entitiesFacetJsonResponseResourcePath;
	protected static String s_textElementsFacetPathResourcePath;

	protected static String s_entitiesFacetWithStatsJsonResponseResourcePath;
	protected static String s_entitiesFacetNoDataResponsePath;
	protected static String s_getHighlightingJsonResponseResourcePath;

	protected static String s_wrongInputResourcePath;
	protected static String s_getQuantityJsonResponseResourcePath;

	// Interaction Preview
	protected static String s_interactionPreviewResponseResourcePath;
	protected static String s_interactionPreviewNoDataResourcePath;

	// Highlights
	protected static String s_interactionHighlightsCustomerResourcePath;
	protected static String s_interactionHighlightsAgentResourcePath;
	protected static String s_interactionHighlightsNoSPSResourcePath;

	protected static String tenant = "tenant1";
	protected static String channel = "channel1";

	// Faceted Search
	protected static String s_facetedSearchResourcePath;

	// Entities Trends
	protected static String s_getEntitiesTrendsJsonResponseResourcePath;

	// SuggestionsForAutocomplete
	protected static String s_getSuggestionsForAutocompleteResourcePath;

	protected static String s_solrSuggestionsResourcePath;


	@Mock
	protected TextEngineSchemaService textEngineConfigurationServiceMock;

	protected TextSchemaField textSchemeField1;
	protected TextSchemaField textSchemeField2;

	private ArrayList<FieldMetric> metricFields;

	public SolrResponseConverterTest() {
		MockitoAnnotations.initMocks(this);

		createLoggerMock();

		textSchemeField1 = new TextSchemaField().setName("topic_f").setDisplayFieldName("displayFieldName").setFieldDataType(FieldDataType.Int).setDocumentHierarchyType(DocumentHierarchyType.Utterance);

		textSchemeField2 = new TextSchemaField().setName("Meta_s_author").setDisplayFieldName("Author").setFieldDataType(FieldDataType.Text).setDocumentHierarchyType(DocumentHierarchyType.Interaction);

		Mockito.when(textEngineConfigurationServiceMock.getTextSchemaField(tenant, channel, "topic_f")).thenReturn(textSchemeField1);

		Mockito.when(textEngineConfigurationServiceMock.getTextSchemaField(tenant, channel, "Meta_s_author")).thenReturn(textSchemeField2);

		Mockito.when(textEngineConfigurationServiceMock.isParentDocumentField(tenant, channel, "Meta_s_author")).thenReturn(true);

		Mockito.when(textEngineConfigurationServiceMock.isParentDocumentField(tenant, channel, "topic_f")).thenReturn(false);

		Mockito.when(textEngineConfigurationServiceMock.isValid()).thenReturn(true);

		metricFields = new ArrayList<FieldMetric>();
		metricFields.add(new FieldMetric("AvgHandleTime", "Meta_l_handleTime", "METRIC_Meta_l_handleTime", MetricType.TIME, StatFunction.avg, true, 2));
		metricFields.add(new FieldMetric("AvgEmployeeResponseTime", "Meta_l_avgEmployeeResponseTime", "METRIC_Meta_l_avgEmployeeResponseTime", MetricType.TIME, StatFunction.avg,
		        true, 3));
		metricFields.add(new FieldMetric("AvgCustomerResponseTime", "Meta_l_avgCustomerResponseTime", "METRIC_Meta_l_avgCustomerResponseTime", MetricType.TIME, StatFunction.avg,
		        true, 4));
		metricFields.add(new FieldMetric("AvgMessagesCount", "Meta_i_messagesCount", "METRIC_Meta_i_messagesCount", MetricType.NUMBER, StatFunction.avg, true, 5));
		metricFields.add(new FieldMetric("AvgEmployeesMessages", "Meta_i_employeesMessages", "METRIC_Meta_i_employeesMessages", MetricType.NUMBER, StatFunction.avg, true, 6));
		metricFields.add(new FieldMetric("AvgCustomerMessages", "Meta_i_customerMessages", "METRIC_Meta_i_customerMessages", MetricType.NUMBER, StatFunction.avg, true, 7));
	}

	/**
	 * @throws Exception Exception
	 *
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		s_searchInteractionsJsonResourcePath = "SearchInteractionsJson.txt";
		s_textElementsFacetPathResourcePath = "TextElementsFacetPathResourcePath.txt";

		s_entitiesFacetJsonResponseResourcePath = "EntitiesFacetResponse.txt";
		s_entitiesFacetWithStatsJsonResponseResourcePath = "EntitiesFacetWithStatsResponse.json";
		s_entitiesFacetNoDataResponsePath = "EntitiesFacetNoData.txt";

		// Interaction Preview
		s_interactionPreviewResponseResourcePath = "InteractionPreview.txt";
		s_interactionPreviewNoDataResourcePath = "InteractionPreviewNoData.txt";

		// Highlights
		s_interactionHighlightsCustomerResourcePath = "highlights/HighlightsCustomer.txt";
		s_interactionHighlightsAgentResourcePath = "highlights/HighlightsAgent.txt";
		s_interactionHighlightsNoSPSResourcePath = "highlights/HighlightsNoSPS.txt";
		s_getHighlightingJsonResponseResourcePath = "highlights/GetHighlighting.txt";

		s_interactionPreviewResponseResourcePath = "InteractionPreview.txt";
		s_wrongInputResourcePath = "WrongInput.txt";

		// Faceted Search
		s_facetedSearchResourcePath = "FacetedSearch.txt";

		// Get Quantity
		s_getQuantityJsonResponseResourcePath = "GetQuantity.txt";

		// Trends
		s_getEntitiesTrendsJsonResponseResourcePath = "GetEntitiesTrends.txt";
		s_getSuggestionsForAutocompleteResourcePath = "SuggestionsForAutocomplete.txt";
		s_solrSuggestionsResourcePath = "SolrSuggestions.json";
	}


	/**
	 * test GetInteractions - Return Right Number Of Interactions.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testSearchInteractionsReturnRightNumberOfDocuments() throws IOException {
		val jsonResponseConverter = createResponseConverter();
		SearchInteractionsResult searchResult = jsonResponseConverter.getInteractions(this.getResourceAsString(s_searchInteractionsJsonResourcePath), tenant, channel);

		assertTrue("Number of interactions is not right !", searchResult.getInteractions().size() == 19);
		assertTrue("Total number of interactions is not right !", searchResult.getTotalNumberFound() == 19);
	}

	/**
	 * test GetInteractions - Return Right Number Of Utterances.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testSearchInteractionsReturnRightNumberOfUtterances() throws IOException {
		val jsonResponseConverter = createResponseConverter();
		SearchInteractionsResult searchResult = jsonResponseConverter.getInteractions(this.getResourceAsString(s_searchInteractionsJsonResourcePath), tenant, channel);

		List<Interaction> interactions = searchResult.getInteractions();
		assertTrue("Number of Utterances in Interaction #1 is not right !", interactions.get(0).getUtterances().size() == 30);
		assertTrue("Number of Utterances in Interaction #2 is not right !", interactions.get(1).getUtterances().size() == 43);
		assertTrue("Number of Utterances in Interaction #2 is not right !", interactions.get(2).getUtterances().size() == 39);
	}

	/**
	 * test GetInteractions Return Utterances With Text Data.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testSearchInteractionsUtterancesHaveText() throws IOException {
		val jsonResponseConverter = createResponseConverter();
		SearchInteractionsResult searchResult = jsonResponseConverter.getInteractions(this.getResourceAsString(s_searchInteractionsJsonResourcePath), tenant, channel);
		List<Interaction> interactions = searchResult.getInteractions();

		for (Interaction interaction : interactions) {
			List<Utterance> subDocuments = interaction.getUtterances();

			for (Utterance subDocument : subDocuments) {
				assertTrue(String.format("Sub-Documents %s in Document %s is null !", interaction.getId(), subDocument.getId()), !StringUtils.isNullOrBlank(subDocument.getText()));
			}
		}
	}

	/**
	 * test GetInteractions - Return Utterances With Child Index.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testSearchInteractionsUtterancesContentType() throws IOException {
		val jsonResponseConverter = createResponseConverter();
		SearchInteractionsResult searchResult = jsonResponseConverter.getInteractions(this.getResourceAsString(s_searchInteractionsJsonResourcePath), tenant, channel);
		List<Interaction> interactions = searchResult.getInteractions();

		for (Interaction interaction : interactions) {
			List<Utterance> utterances = interaction.getUtterances();

			for (Utterance utterance : utterances) {
				assertTrue(String.format("Sub-Documents %s in Document %s has no CHILD content_type !", utterance.getId(), interaction.getId()),
				           "CHILD".equals(utterance.getContentType().name()));
			}
		}
	}

	/**
	 * test GetInteractions - Return Interactions With Parent Index.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testSearchInteractionsInteractionContentType() throws IOException {
		val jsonResponseConverter = createResponseConverter();
		SearchInteractionsResult searchResult = jsonResponseConverter.getInteractions(this.getResourceAsString(s_searchInteractionsJsonResourcePath), tenant, channel);
		List<Interaction> interactions = searchResult.getInteractions();

		for (Interaction interaction : interactions) {
			assertTrue(String.format("Document %s has no PARENT content_type !", interaction.getId()), "PARENT".equals(interaction.getContentType().name()));
		}
	}

	/**
	 * test GetInteractions - Return Utterances With Link To Parent.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testSearchInteractionsUtteranceParentIdEqualsToInteractionId() throws IOException {
		val jsonResponseConverter = createResponseConverter();
		SearchInteractionsResult searchResult = jsonResponseConverter.getInteractions(this.getResourceAsString(s_searchInteractionsJsonResourcePath), tenant, channel);
		List<Interaction> interactions = searchResult.getInteractions();

		for (Interaction interaction : interactions) {

			for (Utterance utterance : interaction.getUtterances()) {
				assertTrue(String.format("Sub-Documents %s has no link to parent Document %s!", utterance.getId(), interaction.getId()),
				           utterance.getParentId().equals(interaction.getId()));
			}
		}
	}

	@Test
	public void testGetInteractionsWrongInput() throws IOException {
		val jsonResponseConverter = createResponseConverter();
		SearchInteractionsResult searchResult = jsonResponseConverter.getInteractions(this.getResourceAsString(s_getQuantityJsonResponseResourcePath), tenant, channel);

		assertTrue("interactions is not empty", searchResult.getInteractions().size() == 0);
	}

	@Test(expected = TextQueryExecutionException.class)
	public void testGetInteractionsReturnException() throws IOException {
		val jsonResponseConverter = createResponseConverter();
		jsonResponseConverter.getInteractions(this.getResourceAsString(s_wrongInputResourcePath), tenant, channel);
	}

	// TestsEntities tests

	/**
	 * Tests Entities Facet response converter.
	 * 
	 * @throws MalformedURLException
	 *             throws
	 * @throws IOException
	 *             throws
	 */
	@Test
	public void testGetEntitiesFacetResponseNumberOfTopLevels() throws IOException {
		val entitiesResponseJson = this.getResourceAsString(s_entitiesFacetJsonResponseResourcePath);
		assertNotNull("EntitiesFacetResponse input is empty", entitiesResponseJson);
	}

	@Test
	@Ignore
	public void getTextElementsFacetPathTest() throws IOException {
		val entitiesResponseJson = this.getResourceAsString(s_textElementsFacetPathResourcePath);
		val jsonResponseConverter = createResponseConverter();
		List<TextElementType> elements = new ArrayList<TextElementType>();
		elements.add(TextElementType.Relations);
		elements.add(TextElementType.Relations);
		elements.add(TextElementType.Relations);

		TextElementsFacetNode result = jsonResponseConverter.getTextElementsFacetPath(entitiesResponseJson, elements, true);

		assertEquals(result.getChildren().get(0).getNumberOfInteractions(), 919);
		assertEquals(result.getChildren().get(0).getValue(), "2/TechAction->FeeObject/CHARGE->fee");

	}

	/**
	 * @author NShunewich
	 * @throws IOException
	 *             throws
	 **/
	@Test
	public void testEntitiesFacetsTreeLevelsVerification() throws IOException {
		val jsonResponseConverter = createResponseConverter();

		String entityFacetsResponse = this.getResourceAsString(s_entitiesFacetJsonResponseResourcePath);
		List<TextElementsFacetNode> facets = jsonResponseConverter.getTextElementsFacets(entityFacetsResponse, Arrays.asList(TAConstants.FacetQuery.textElementFacetAlias), false, false);

		// 1st Level
		Optional<TextElementsFacetNode> topic = facets.stream().filter(f -> f.getValue().equals("1/Fee")).findFirst();
		assertTrue(topic.isPresent());

		topic = facets.stream().filter(f -> f.getValue().equals("2/Fee/price")).findFirst();
		assertTrue(topic.isPresent());

		topic = facets.stream().filter(f -> f.getValue().equals("2/Fee/rate")).findFirst();
		assertTrue(topic.isPresent());

		topic = facets.stream().filter(f -> f.getValue().equals("2/Fee/rental")).findFirst();
		assertTrue(topic.isPresent());

		topic = facets.stream().filter(f -> f.getValue().equals("2/Fee/fee")).findFirst();
		assertTrue(topic.isPresent());

		topic = facets.stream().filter(f -> f.getValue().equals("2/Fee/charge")).findFirst();
		assertTrue(topic.isPresent());

		topic = facets.stream().filter(f -> f.getValue().equals("2/Fee/amount")).findFirst();
		assertTrue(topic.isPresent());
	}

	/**
	 * @author imor
	 * @throws IOException
	 *             throws
	 **/
	@Test
	public void testEntitiesFacetsWithStatsTreeLevelsVerification() throws IOException {
		val jsonResponseConverter = createResponseConverter();

		String entityFacetsResponse = this.getResourceAsString(s_entitiesFacetWithStatsJsonResponseResourcePath);
		List<TextElementsFacetNode> facets = jsonResponseConverter.getTextElementsFacets(entityFacetsResponse, Arrays.asList(TAConstants.FacetQuery.textElementFacetAlias), true, false);

		// 1st Level
		Optional<TextElementsFacetNode> topic = facets.stream().filter(f -> f.getValue().equals("1/Fee")).findFirst();
		assertTrue(topic.isPresent());


		int i = 0;
		topic = facets.stream().filter(f -> f.getValue().equals("2/Fee/charge")).findFirst();
		List<MetricData> topicMetrics = topic.get().getMetrics();

		MetricData metric = topicMetrics.stream().filter(m -> m.getName().equals("AvgHandleTime")).findFirst().get();
		assertEquals("AvgHandleTime", metric.getName());
		assertEquals(197.56986501276907, metric.getValue(), DELTA);

		metric = topicMetrics.stream().filter(m -> m.getName().equals("AvgEmployeeResponseTime")).findFirst().get();
		assertEquals("AvgEmployeeResponseTime", metric.getName());
		assertEquals(109.97920466982853, metric.getValue(), DELTA);

		metric = topicMetrics.stream().filter(m -> m.getName().equals("AvgCustomerResponseTime")).findFirst().get();
		assertEquals("AvgCustomerResponseTime", metric.getName());
		assertEquals(110.03429405326523, metric.getValue(), DELTA);

		metric = topicMetrics.stream().filter(m -> m.getName().equals("AvgMessagesCount")).findFirst().get();
		assertEquals("AvgMessagesCount", metric.getName());
		assertEquals(27.00948558920102, metric.getValue(), DELTA);

		metric = topicMetrics.stream().filter(m -> m.getName().equals("AvgEmployeesMessages")).findFirst().get();
		assertEquals("AvgEmployeesMessages", metric.getName());
		assertEquals(12.152499087924115, metric.getValue(), DELTA);

		metric = topicMetrics.stream().filter(m -> m.getName().equals("AvgCustomerMessages")).findFirst().get();
		assertEquals("AvgCustomerMessages", metric.getName());
		assertEquals(10.558920102152499, metric.getValue(), DELTA);

		i = 1;
		topic = facets.stream().filter(f -> f.getValue().equals("2/Fee/price")).findFirst();
		topicMetrics = topic.get().getMetrics();

		metric = topicMetrics.stream().filter(m -> m.getName().equals("AvgHandleTime")).findFirst().get();
		assertEquals("AvgHandleTime", metric.getName());
		assertEquals(200.32413525068014, metric.getValue(), DELTA);

		metric = topicMetrics.stream().filter(m -> m.getName().equals("AvgEmployeeResponseTime")).findFirst().get();
		assertEquals("AvgEmployeeResponseTime", metric.getName());
		assertEquals(109.92926544889234, metric.getValue(), DELTA);

		metric = topicMetrics.stream().filter(m -> m.getName().equals("AvgCustomerResponseTime")).findFirst().get();
		assertEquals("AvgCustomerResponseTime", metric.getName());
		assertEquals(110.0217644772639, metric.getValue(), DELTA);

		metric = topicMetrics.stream().filter(m -> m.getName().equals("AvgMessagesCount")).findFirst().get();
		assertEquals("AvgMessagesCount", metric.getName());
		assertEquals(28.207151185386707, metric.getValue(), DELTA);

		metric = topicMetrics.stream().filter(m -> m.getName().equals("AvgEmployeesMessages")).findFirst().get();
		assertEquals("AvgEmployeesMessages", metric.getName());
		assertEquals(13.056354450058297, metric.getValue(), DELTA);

		metric = topicMetrics.stream().filter(m -> m.getName().equals("AvgCustomerMessages")).findFirst().get();
		assertEquals("AvgCustomerMessages", metric.getName());
		assertEquals(10.816167897396035, metric.getValue(), DELTA);
	}

	/**
	 * @author NShunewich
	 * @throws IOException
	 *             throws
	 **/
	@Test
	public void testEntitiesFacetsNoData() throws IOException {
		val jsonResponseConverter = createResponseConverter();
		String entitiesFacetNoDataResponse = this.getResourceAsString(s_entitiesFacetNoDataResponsePath);

		List<TextElementsFacetNode> facets = jsonResponseConverter.getTextElementsFacets(entitiesFacetNoDataResponse, Arrays.asList(TAConstants.FacetQuery.textElementFacetAlias), false, false);
		assertTrue(facets.size() == 0);
	}

	@Test
	public void getInteractionPreviewTest() throws IOException {
		val jsonResponseConverter = createResponseConverter();
		String interactionPreviewResponse = this.getResourceAsString(s_interactionPreviewResponseResourcePath);

		SearchInteractionsResult searchResult = jsonResponseConverter.getInteractions(interactionPreviewResponse, tenant, channel);
		List<Interaction> interactions = searchResult.getInteractions();

		assertNotNull(interactions);
		// only one interaction should be present
		assertEquals(interactions.size(), 1);

		Interaction preview = interactions.get(0);

		val utterances = preview.getUtterances();
		assertNotNull(utterances);
		assertEquals(utterances.size(), 16);

		for (val utterance : utterances) {
			assertNotNull(utterance);
			assertNotNull(utterance.getText());
			assertTrue(!StringUtils.isNullOrBlank(utterance.getText()));
		}
	}

	@Test
	public void getInteractionPreviewNoDataTest() throws IOException {
		val jsonResponseConverter = createResponseConverter();
		String interactionPreviewResponse = this.getResourceAsString(s_interactionPreviewNoDataResourcePath);

		SearchInteractionsResult searchResult = jsonResponseConverter.getInteractions(interactionPreviewResponse, tenant, channel);

		assertEquals(0, searchResult.getInteractions().size());
	}

	@Test
	public void testGetEntitiesFacetsWrongInput() throws IOException {
		val jsonResponseConverter = createResponseConverter();
		List<TextElementsFacetNode> facets = jsonResponseConverter.getTextElementsFacets(this.getResourceAsString(s_getQuantityJsonResponseResourcePath), Arrays.asList(TAConstants.FacetQuery.textElementFacetAlias), false, false);
		assertTrue("facets is not empty", facets.size() == 0);
	}

	@Test(expected = TextQueryExecutionException.class)
	public void testGetEntitiesFacetsException() throws IOException {
		val jsonResponseConverter = createResponseConverter();
		jsonResponseConverter.getTextElementsFacets(this.getResourceAsString(s_wrongInputResourcePath), Arrays.asList(TAConstants.FacetQuery.textElementFacetAlias), false, false);
	}

	@Test
	public void getInteractionsHighlightsCustomerTest() throws IOException {
		val jsonResponseConverter = (SolrResponseConverter) createResponseConverter();
		String interactionsHighlightsJson = this.getResourceAsString(s_interactionHighlightsCustomerResourcePath);

		HighlightResult highlightResult = jsonResponseConverter.getHighlights(interactionsHighlightsJson, tenant, channel);
		List<UtteranceHighlights> highlights = highlightResult.getHighlights();

		assertNotNull(highlights);
		// 3 utterances
		assertEquals(highlights.size(), 3);

		UtteranceHighlights utterance;

		for (int i = 0; i < highlights.size(); i++) {
			utterance = highlights.get(i);

			switch (utterance.getDocumentId()) {
				case "19-1":
					assertNotNull(utterance.getTermHighlights());
					assertEquals(3, utterance.getTermHighlights().size());
					break;
				case "19-2":
					assertNotNull(utterance.getTermHighlights());
					assertEquals(3, utterance.getTermHighlights().size());
					break;
				case "19-3":
					assertNotNull(utterance.getTermHighlights());
					assertEquals(0, utterance.getTermHighlights().size());
					break;
			}
		}
	}

	// getHighlight tests

	@Test
	public void testGetHighlightingReturnRightNumberOfUtterances() throws IOException {

		SolrResponseConverter jsonResponseConverter = (SolrResponseConverter) createResponseConverter();
		HighlightResult highlightResult = jsonResponseConverter.getHighlights(this.getResourceAsString(s_getHighlightingJsonResponseResourcePath), tenant, channel);

		assertTrue("Number of utterancesHighlighting is not right !", highlightResult.getHighlights().size() == 3);
	}

	@Test
	public void testGetHighlightingReturnRightNumberOfHighlighWordsInUtterances() throws IOException {

		SolrResponseConverter jsonResponseConverter = (SolrResponseConverter) createResponseConverter();
		String json = this.getResourceAsString(s_getHighlightingJsonResponseResourcePath);

		HighlightResult utterancesHighlights = jsonResponseConverter.getHighlights(json, tenant, channel);

		List<UtteranceHighlights> highlights = utterancesHighlights.getHighlights();
		assertTrue("Number of Highlighting Words in utterance 0 is not right !", highlights.get(0).getTermHighlights().size() == 3);
		assertTrue("Number of Highlighting Words in utterance 1 is not right !", highlights.get(1).getTermHighlights().size() == 3);
		assertTrue("Number of Highlighting Words in utterance 2 is not right !", highlights.get(2).getTermHighlights().size() == 12);
	}

	@Test
	public void testGetHighlightingReturnsAllHighlighData() throws IOException {
		SolrResponseConverter jsonResponseConverter = (SolrResponseConverter) createResponseConverter();

		String json = this.getResourceAsString(s_getHighlightingJsonResponseResourcePath);
		HighlightResult utterancesHighlights = jsonResponseConverter.getHighlights(json, tenant, channel);

		for (val highlight : utterancesHighlights.getHighlights()) {

			for (val highlighting : highlight.getTermHighlights()) {
				assertNotNull("One of the Terms is null", highlighting.getTerm());
				assertTrue("One of the Terms as starts that is zero", highlighting.getStarts() != 0);
				assertTrue("One of the Terms as ends that is zero", highlighting.getEnds() != 0);
			}
		}
	}

	@Test
	public void getInteractionsHighlightsCustomerNoSPS() throws IOException {
		val jsonResponseConverter = (SolrResponseConverter) createResponseConverter();
		String interactionsHighlightsJson = this.getResourceAsString(s_interactionHighlightsNoSPSResourcePath);

		HighlightResult highlightsResult = jsonResponseConverter.getHighlights(interactionsHighlightsJson, tenant, channel);
		List<UtteranceHighlights> highlights = highlightsResult.getHighlights();

		// 3 utterances
		assertNotNull(highlights);
		assertEquals(17, highlights.size());

		UtteranceHighlights utterance;

		for (int i = 0; i < highlights.size(); i++) {
			utterance = highlights.get(i);

			switch (utterance.getDocumentId()) {
				case "15-1":
					assertEquals(0, utterance.getTermHighlights().size());
					break;
				case "15-2":
					assertNotNull(utterance.getTermHighlights());
					assertEquals(1, utterance.getTermHighlights().size());
					break;
				case "7-2":
					assertNotNull(utterance.getTermHighlights());
					assertEquals(2, utterance.getTermHighlights().size());
					break;
			}
		}
	}

	@Test
	public void testGetHighlightingWrongInput() throws IOException {
		SolrResponseConverter jsonResponseConverter = (SolrResponseConverter) createResponseConverter();
		HighlightResult highlightsResult = jsonResponseConverter.getHighlights(this.getResourceAsString(s_getQuantityJsonResponseResourcePath), tenant, channel);

		assertTrue("utterancesHighlighting is not empty", highlightsResult.getHighlights().size() == 0);
	}

	@Test(expected = TextQueryExecutionException.class)
	public void testGetHighlightingException() throws IOException {
		SolrResponseConverter jsonResponseConverter = (SolrResponseConverter) createResponseConverter();
		HighlightResult highlightsResult = jsonResponseConverter.getHighlights(this.getResourceAsString(s_wrongInputResourcePath), tenant, channel);
	}

	@Test
	public void getInteractionsHighlightsAgent() throws IOException {
		val jsonResponseConverter = (SolrResponseConverter) createResponseConverter();
		String interactionsHighlightsJson = this.getResourceAsString(s_interactionHighlightsAgentResourcePath);

		HighlightResult highlightResult = jsonResponseConverter.getHighlights(interactionsHighlightsJson, tenant, channel);

		List<UtteranceHighlights> highlights = highlightResult.getHighlights();

		// 3 utterances
		assertNotNull(highlights);
		assertEquals(3, highlights.size());

		UtteranceHighlights utterance;

		for (int i = 0; i < highlights.size(); i++) {
			utterance = highlights.get(i);

			switch (utterance.getDocumentId()) {
				case "19-1":
					assertEquals(0, utterance.getTermHighlights().size());
					break;
				case "19-2":
					assertEquals(0, utterance.getTermHighlights().size());
					break;
				case "19-4":
					assertNotNull(utterance.getTermHighlights());
					assertEquals(1, utterance.getTermHighlights().size());
					break;
			}
		}
	}

	// getQuantity tests

	@Test
	public void testGetQuantity() throws IOException {
		val jsonResponseConverter = createResponseConverter();
		int quantity = jsonResponseConverter.getQuantity(this.getResourceAsString(s_getQuantityJsonResponseResourcePath));

		assertTrue("quantity is not 18 like it should be", quantity == 18);
	}

	@Test(expected = TextQueryExecutionException.class)
	public void testGetQuantityException() throws IOException {
		val jsonResponseConverter = createResponseConverter();
		jsonResponseConverter.getQuantity(this.getResourceAsString(s_wrongInputResourcePath));
	}

	/**
	 * test GetInteractions - Return Right Number Of Interactions.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testFacetedSearchInteractionLevelResponse() throws IOException {
		val jsonResponseConverter = createResponseConverter();

		val fieldName = "Meta_s_author";

		Facet facetResults = jsonResponseConverter.getFacet(this.getResourceAsString(s_facetedSearchResourcePath), tenant, channel, fieldName, null);

		assertNotNull(facetResults);
		assertTrue("Meta_s_author".equals(facetResults.getFieldName()));
		assertTrue("Total Count not right!", facetResults.getValues().size() == 10);

		List<FacetResultGroup> values = facetResults.getValues();

		for (int i = 0; i < values.size(); i++) {
			val facetGroup = values.get(i);

			switch (i) {
				case 0:
					assertTrue("q-ccc-pcln unknown product".equals(facetGroup.getTitle()));
					assertEquals(facetGroup.getCount(), 472074);
					break;
				case 1:
					assertTrue("q-sales-retail hotel".equals(facetGroup.getTitle()));
					assertEquals(facetGroup.getCount(), 442941);
					break;
				case 2:
					assertTrue("q-sales-nyop hotel".equals(facetGroup.getTitle()));
					assertEquals(facetGroup.getCount(), 217458);
					break;
				case 3:
					assertTrue("q-sales-pcln pkg".equals(facetGroup.getTitle()));
					assertEquals(facetGroup.getCount(), 210032);
					break;
				case 4:
					assertTrue("q-ccc-pcln air retail".equals(facetGroup.getTitle()));
					assertEquals(facetGroup.getCount(), 120510);
					break;
			}
		}
	}

	// Trend
	@Test
	public void testEntityTrendsNoData() throws IOException {
		// Faceted Search tests
		val jsonResponseConverter = createResponseConverter();
		List<TextElementTrend> entityTrends = new ArrayList<TextElementTrend>();
		entityTrends = jsonResponseConverter.getTextElementyTrends("{\"numFounds\":52,\"trends\":[]}");

		assertTrue("empty", entityTrends.size() == 0);
	}

	@Test
	public void testEntityTrends() throws IOException {
		// Faceted Search tests
		val jsonResponseConverter = createResponseConverter();
		List<TextElementTrend> entityTrends = new ArrayList<TextElementTrend>();
		String input = this.getResourceAsString(s_getEntitiesTrendsJsonResponseResourcePath);
		entityTrends = jsonResponseConverter.getTextElementyTrends(input);

		assertTrue("not empty", entityTrends.size() == 3);
		assertTrue("names exist 0", "1/Service".equals(entityTrends.get(0).getName()));
		assertTrue("names exist 0", "1/Program".equals(entityTrends.get(1).getName()));
		assertTrue("names exist 2", "1/Fee".equals(entityTrends.get(2).getName()));

		assertTrue("relative change exist 0", entityTrends.get(0).getRelativeVolumeChange() != 0);
		assertTrue("relative change exist 1", entityTrends.get(1).getRelativeVolumeChange() != 0);
		assertTrue("relative change exist 2", entityTrends.get(2).getRelativeVolumeChange() != 0);

		assertTrue("absolute change exist 0", entityTrends.get(0).getAbsoluteVolumeChange() != 0);
		assertTrue("absolute change exist 1", entityTrends.get(1).getAbsoluteVolumeChange() != 0);
		assertTrue("absolute change exist 2", entityTrends.get(2).getAbsoluteVolumeChange() != 0);

		assertTrue("volume exist 0", entityTrends.get(0).getVolume() != 0);
		assertTrue("volume exist 1", entityTrends.get(1).getVolume() != 0);
		assertTrue("volume exist 2", entityTrends.get(2).getVolume() != 0);
	}

	@Test
	public void testGetSuggestionsForAutocomplete() throws IOException {
		val jsonResponseConverter = createResponseConverter();
		val SearchSuggestionList = jsonResponseConverter.getTermsAutoCompleteSuggestions(this.getResourceAsString(s_getSuggestionsForAutocompleteResourcePath), "en",
		                                                                                 TAConstants.FacetQuery.termsAutoCompleteFacetAlias,
		                                                                                 TAConstants.FacetQuery.interactionsCountStatAlias);

		assertEquals(4, SearchSuggestionList.size());
	}

	@Test
	public void getIsSourceTypeInChannelExistTest() throws IOException {
		val jsonResponseConverter = createResponseConverter();

		boolean response = jsonResponseConverter.getIsSourceTypeInChannel(this.getResourceAsString("SourceTypeExistsResponse.txt"));

		assertTrue(response);
	}

	@Test
	public void getIsSourceTypeInChannelNotExistTest() throws IOException {
		val jsonResponseConverter = createResponseConverter();

		boolean response = jsonResponseConverter.getIsSourceTypeInChannel(this.getResourceAsString("SourceTypeNotExistsResponse.txt"));

		assertTrue(!response);
	}

	@Test
	public void solrSuggestionsConvertValidResponseTest() throws IOException {
		val jsonResponseConverter = createResponseConverter();

		String suggestionsJson = this.getResourceAsString(s_solrSuggestionsResourcePath);
		List<WeightedSuggestion> lstSuggestions = jsonResponseConverter.getFreeTextLookupSuggestions(suggestionsJson, "mySuggester");

		assertNotNull(lstSuggestions);
		assertEquals(lstSuggestions.size(), 251);
	}

	protected ResponseConverter createResponseConverter() {
		SolrResponseConverter solrResponseConverter = new SolrResponseConverter();
		solrResponseConverter.setTextEngineConfigurationService(textEngineConfigurationServiceMock);
		solrResponseConverter.setMetricFields(metricFields);
		return solrResponseConverter;
	}
}
