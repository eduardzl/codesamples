package com.verint.textanalytics.dal.darwin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import com.verint.textanalytics.model.interactions.EntityHighlight;
import com.verint.textanalytics.model.interactions.HighlightResult;
import com.verint.textanalytics.model.interactions.SentimentHighlight;
import lombok.val;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionException;
import com.verint.textanalytics.model.analyze.*;
import com.verint.textanalytics.model.interactions.UtteranceHighlights;
import com.verint.textanalytics.model.trends.InteractionDailyVolumeDataPoint;

public class EASearchResponseConverterTest extends SolrResponseConverterTest {

	private static final double DELTA = 0.000000001;

	// Highlights
	protected static String s_AddHighlightEASearchResourcePath = "highlights/AddHighlightEASearchTest.json";
	protected static String s_interactionsDailyVolumeResourcePath = "InteractionsDailyVolume.txt";
	protected static String s_MetricsPath = "Metrics.txt";
	protected static String s_entitiesMetricsPathJson = "EntitiesMetrics.txt";
	protected static String s_entitiesSentimentsMetricsPathJson = "EntitiesSentimentsMetrics.txt";
	protected static String s_entityTrendDailyVolumeResourcePath = "EntityTrendInteractionsDailyVolume.txt";
	protected static String s_interactionHighlightsAgentEASearchResourcePath = "highlights/EASearchHighlightsAgent.json";
	protected static String s_interactionHighlightsNoSPSEASearchResourcePath = "highlights/EASearchHighlightsNoSPS.json";
	protected static String s_interactionHighlightsNoSPSTopicRelationSentimentResponsePath = "highlights/HighlightsNoSPSTopicRelationSentimentResponse.txt";

	// getHighlight tests

	@Override
	@Test
	public void testGetHighlightingReturnRightNumberOfUtterances() throws IOException {
		val responseConverter = (ResponseConverter) createResponseConverter();
		HighlightResult result = responseConverter.getHighlights(this.getResourceAsString(s_AddHighlightEASearchResourcePath), tenant, channel);

		assertTrue("Number of utterancesHighlighting is not right !", result.getHighlights().size() == 5);
	}

	@Override
	@Test
	public void testGetHighlightingReturnRightNumberOfHighlighWordsInUtterances() throws IOException {
		val responseConverter = (ResponseConverter) createResponseConverter();
		HighlightResult result = responseConverter.getHighlights(this.getResourceAsString(s_AddHighlightEASearchResourcePath), tenant, channel);

		List<UtteranceHighlights> highlights = result.getHighlights();
		assertTrue("Number of Highlighting Words in utterance 0 is not right !", highlights.get(0).getTermHighlights().size() == 1);
		assertTrue("Number of Highlighting Words in utterance 1 is not right !", highlights.get(1).getTermHighlights().size() == 1);
		assertTrue("Number of Highlighting Words in utterance 2 is not right !", highlights.get(2).getTermHighlights() == null);
		assertTrue("Number of Highlighting Words in utterance 2 is not right !", highlights.get(3).getTermHighlights().size() == 1);
		assertTrue("Number of Highlighting Words in utterance 2 is not right !", highlights.get(4).getTermHighlights() == null);

		assertTrue("Number of Highlighting Topics in utterance 0 is not right !", highlights.get(0).getEntitiesHighlights().size() == 2);
		assertTrue("Number of Highlighting Topics in utterance 1 is not right !", highlights.get(1).getEntitiesHighlights().size() == 3);
		assertTrue("Number of Highlighting Topics in utterance 2 is not right !", highlights.get(2).getEntitiesHighlights().size() == 4);
		assertTrue("Number of Highlighting Topics in utterance 2 is not right !", highlights.get(3).getEntitiesHighlights().size() == 2);
		assertTrue("Number of Highlighting Topics in utterance 2 is not right !", highlights.get(4).getEntitiesHighlights().size() == 1);

		assertTrue("Number of Highlighting Relations in utterance 0 is not right !", highlights.get(0).getRelationsHighlights().size() == 1);
		assertTrue("Number of Highlighting Relations in utterance 1 is not right !", highlights.get(1).getRelationsHighlights().size() == 1);
		assertTrue("Number of Highlighting Relations in utterance 2 is not right !", highlights.get(2).getRelationsHighlights() == null);
		assertTrue("Number of Highlighting Relations in utterance 2 is not right !", highlights.get(3).getRelationsHighlights().size() == 2);
		assertTrue("Number of Highlighting Relations in utterance 2 is not right !", highlights.get(4).getRelationsHighlights() == null);
	}

	@Override
	@Test
	public void testGetHighlightingReturnsAllHighlighData() throws IOException {
		val responseConverter = (ResponseConverter) createResponseConverter();
		HighlightResult result = responseConverter.getHighlights(this.getResourceAsString(s_getHighlightingJsonResponseResourcePath), tenant, channel);

		List<UtteranceHighlights> highlights = result.getHighlights();
		for (val utteranceHighlighting : highlights) {

			for (val highlighting : utteranceHighlighting.getTermHighlights()) {
				assertNotNull("One of the Terms is null", highlighting.getTerm());
				assertTrue("One of the Terms as starts that is zero", highlighting.getStarts() != 0);
				assertTrue("One of the Terms as ends that is zero", highlighting.getEnds() != 0);
			}

			for (val highlighting : utteranceHighlighting.getEntitiesHighlights()) {
				assertNotNull("One of the Topics is null", highlighting.getTopic());
				assertTrue("One of the Topics as starts that is zero", highlighting.getStarts() != 0);
				assertTrue("One of the Topics as ends that is zero", highlighting.getEnds() != 0);
			}
		}
	}

	@Override
	@Test
	public void getInteractionsHighlightsCustomerNoSPS() throws IOException {
		val jsonResponseConverter = (ResponseConverter) createResponseConverter();
		String interactionsHighlightsJson = this.getResourceAsString(s_interactionHighlightsNoSPSEASearchResourcePath);

		HighlightResult result = jsonResponseConverter.getHighlights(interactionsHighlightsJson, tenant, channel);

		List<UtteranceHighlights> highlights = result.getHighlights();
		assertNotNull(highlights);

		// 13 utterances
		// only utterances which has at least one highlight of any type
		assertEquals(13, highlights.size());

		UtteranceHighlights utterance;

		for (int i = 0; i < highlights.size(); i++) {
			utterance = highlights.get(i);

			switch (utterance.getDocumentId()) {
				case "15-1":
					assertEquals(null, utterance.getTermHighlights());
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

	@Override
	@Test
	public void testGetHighlightingWrongInput() throws IOException {
		val responseConverter = (ResponseConverter) createResponseConverter();
		HighlightResult highlightResult = responseConverter.getHighlights(this.getResourceAsString(s_getQuantityJsonResponseResourcePath), tenant, channel);
		List<UtteranceHighlights> utterancesHighlights = highlightResult.getHighlights();

		assertTrue("utterancesHighlighting is not empty", utterancesHighlights.size() == 0);
	}

	@Override
	@Test(expected = TextQueryExecutionException.class)
	public void testGetHighlightingException() throws IOException {
		val responseConverter = (ResponseConverter) createResponseConverter();
		HighlightResult highlightResult = responseConverter.getHighlights(this.getResourceAsString(s_wrongInputResourcePath), tenant, channel);
	}

	@Override
	@Test
	public void getInteractionsHighlightsAgent() throws IOException {
		val jsonResponseConverter = (ResponseConverter) createResponseConverter();
		String interactionsHighlightsJson = this.getResourceAsString(s_interactionHighlightsAgentEASearchResourcePath);

		HighlightResult highlightResult = jsonResponseConverter.getHighlights(interactionsHighlightsJson, tenant, channel);
		List<UtteranceHighlights> utterancesHighlights  = highlightResult.getHighlights();

		assertNotNull(utterancesHighlights);

		// 2 utterances
		// only utterances which has at least one highlight
		assertEquals(2, utterancesHighlights.size());

		UtteranceHighlights utterance;

		for (int i = 0; i < utterancesHighlights.size(); i++) {
			utterance = utterancesHighlights.get(i);

			switch (utterance.getDocumentId()) {
				case "19-1":
					assertEquals(null, utterance.getTermHighlights());
					break;
				case "19-2":
					assertEquals(null, utterance.getTermHighlights());
					break;
				case "19-4":
					assertNotNull(utterance.getTermHighlights());
					assertEquals(1, utterance.getTermHighlights().size());
					break;
			}
		}
	}

	@Override
	@Test
	public void getInteractionsHighlightsCustomerTest() throws IOException {
		val jsonResponseConverter = (ResponseConverter) createResponseConverter();
		String interactionsHighlightsJson = this.getResourceAsString(s_AddHighlightEASearchResourcePath);

		HighlightResult highlightResult = jsonResponseConverter.getHighlights(interactionsHighlightsJson, tenant, channel);
		List<UtteranceHighlights> utterancesHighlights  = highlightResult.getHighlights();

		assertNotNull(utterancesHighlights);
		// 3 utterances
		assertEquals(utterancesHighlights.size(), 5);

		UtteranceHighlights utterance;

		for (int i = 0; i < utterancesHighlights.size(); i++) {
			utterance = utterancesHighlights.get(i);

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

	@Test
	public void getInteractionsDailyVolumeSeriesTest() throws IOException {
		val jsonResponseConverter = (ResponseConverter) createResponseConverter();
		String interactionsDailyVolumeJson = this.getResourceAsString(s_interactionsDailyVolumeResourcePath);

		List<InteractionDailyVolumeDataPoint> dailyVolumeDataPoints = jsonResponseConverter.getInteractionsDailyVolumeSeries(interactionsDailyVolumeJson,
		                                                                                                                     TAConstants.FacetQuery.interactionsDailyVolumeAlias);

		assertNotNull(dailyVolumeDataPoints);
		// 3 utterances
		assertEquals(dailyVolumeDataPoints.size(), 89);

		dailyVolumeDataPoints.sort((d1, d2) -> d1.getDate().compareTo(d2.getDate()));

		InteractionDailyVolumeDataPoint dataPoint;
		DateTime date;
		double interactionsCount;

		for (int i = 0; i < dailyVolumeDataPoints.size(); i++) {
			dataPoint = dailyVolumeDataPoints.get(i);
			date = dataPoint.getDate();
			interactionsCount = dataPoint.getValue();

			switch (i) {
				case 0:
					assertEquals(new DateTime(2016, 3, 8, 0, 0, 0, DateTimeZone.UTC), date);
					assertTrue(Double.compare(2095, interactionsCount) == 0);
					break;
				case 1:
					assertEquals(new DateTime(2016, 3, 9, 0, 0, 0, DateTimeZone.UTC), date);
					assertTrue(Double.compare(3807, interactionsCount) == 0);
					break;
				case 2:
					assertEquals(new DateTime(2016, 3, 10, 0, 0, 0, DateTimeZone.UTC), date);
					assertTrue(Double.compare(3776, interactionsCount) == 0);
					break;
				case 3:
					assertEquals(new DateTime(2016, 3, 11, 0, 0, 0, DateTimeZone.UTC), date);
					assertTrue(Double.compare(3528, interactionsCount) == 0);
					break;
			}

		}
	}

	@Test
	public void getMetricsTest() throws IOException {
		val jsonResponseConverter = (ResponseConverter) createResponseConverter();
		String metricsPathJson = this.getResourceAsString(s_MetricsPath);

		List<FieldMetric> fieldsMetrics = this.getFieldsMetrics();

		List<MetricData> metrics = jsonResponseConverter.getResultSetMetrics(metricsPathJson, fieldsMetrics, false);

		assertNotNull(metrics);
		assertEquals(7, metrics.size());

		MetricData metricData;

		for (int i = 0; i < metrics.size(); i++) {
			metricData = metrics.get(i);

			switch (i) {
				case 0:
					assertEquals(fieldsMetrics.get(i).getName(), metricData.getName());
					assertEquals("-0.08196721311475409", Double.valueOf(metricData.getValue()).toString());
					break;
				case 1:
					assertEquals(fieldsMetrics.get(i).getName(), metricData.getName());
					assertEquals("200.05901639344262", Double.valueOf(metricData.getValue()).toString());
					break;
				case 2:
					assertEquals(fieldsMetrics.get(i).getName(), metricData.getName());
					assertEquals("110.14754098360656", Double.valueOf(metricData.getValue()).toString());
					break;
				case 3:
					assertEquals(fieldsMetrics.get(i).getName(), metricData.getName());
					assertEquals("109.79344262295082", Double.valueOf(metricData.getValue()).toString());
					break;
				case 4:
					assertEquals(fieldsMetrics.get(i).getName(), metricData.getName());
					assertEquals("20.675409836065572", Double.valueOf(metricData.getValue()).toString());
					break;
				case 5:
					assertEquals(fieldsMetrics.get(i).getName(), metricData.getName());
					assertEquals("9.085245901639345", Double.valueOf(metricData.getValue()).toString());
					break;
				case 6:
					assertEquals(fieldsMetrics.get(i).getName(), metricData.getName());
					assertEquals("6.770491803278689", Double.valueOf(metricData.getValue()).toString());
					break;
			}
		}
	}

	@Test
	public void getEntitiesMetricsTest() throws IOException {
		//		val jsonResponseConverter = (ResponseConverter) createResponseConverter();
		//		String metricsPathJson = this.getResourceAsString(s_entitiesMetricsPathJson);
		//
		//		List<EntityMetric> metrics = jsonResponseConverter.getEntitiesMetrics(metricsPathJson, EntityMetricType.Volume, EntityMetricType.HandleTime);
		//
		//		assertNotNull(metrics);
		//		assertEquals(metrics.size(), 5);
		//
		//		EntityMetric entityMetric;
		//
		//		for (int i = 0; i < metrics.size(); i++) {
		//			entityMetric = metrics.get(i);
		//
		//			switch (i) {
		//				case 0:
		//					assertEquals("1/Fee", entityMetric.getEntity());
		//					assertEquals("5937.0", Double.valueOf(entityMetric.getX()).toString());
		//					assertEquals("199.09247094492167", Double.valueOf(entityMetric.getY()).toString());
		//					break;
		//				case 1:
		//					assertEquals("2/Fee/charge", entityMetric.getEntity());
		//					assertEquals("2670.0", Double.valueOf(entityMetric.getX()).toString());
		//					assertEquals("197.35131086142323", Double.valueOf(entityMetric.getY()).toString());
		//					break;
		//				case 2:
		//					assertEquals("2/Fee/price", entityMetric.getEntity());
		//					assertEquals("2533.0", Double.valueOf(entityMetric.getX()).toString());
		//					assertEquals("200.25227003553098", Double.valueOf(entityMetric.getY()).toString());
		//					break;
		//				case 3:
		//					assertEquals("1/Rewards", entityMetric.getEntity());
		//					assertEquals("1501.0", Double.valueOf(entityMetric.getX()).toString());
		//					assertEquals("200.27048634243837", Double.valueOf(entityMetric.getY()).toString());
		//					break;
		//				case 4:
		//					assertEquals("2/Fee/fee", entityMetric.getEntity());
		//					assertEquals("1714.0", Double.valueOf(entityMetric.getX()).toString());
		//					assertEquals("199.29229871645273", Double.valueOf(entityMetric.getY()).toString());
		//					break;
		//			}
		//		}
	}

	@Test
	public void getEntitiesSentimentsMetricsTest() throws IOException {
		val jsonResponseConverter = (ResponseConverter) createResponseConverter();
		String metricsPathJson = this.getResourceAsString(s_entitiesSentimentsMetricsPathJson);

		List<TextElementSentimentsMetric> metrics = jsonResponseConverter.getTextElementsSentimentsMetrics(metricsPathJson);

		assertNotNull(metrics);
		assertEquals(metrics.size(), 5);

		TextElementSentimentsMetric entitySentimentsMetric;
		for (int i = 0; i < metrics.size(); i++) {
			entitySentimentsMetric = metrics.get(i);

			switch (i) {
				case 0:
					assertEquals("1/Fee", entitySentimentsMetric.getTextElement());
					assertEquals(0.8757697456492637, entitySentimentsMetric.getSentimentAvg(), DELTA);
					break;
				case 1:
					assertEquals("2/Fee/price", entitySentimentsMetric.getTextElement());
					assertEquals(0.9136526386798817, entitySentimentsMetric.getSentimentAvg(), DELTA);
					break;
			}
		}
	}

	@Test
	public void getInteractionsHighlightsNoSPSTopicRelationSentimentResponse() throws IOException {
		val jsonResponseConverter = (ResponseConverter) createResponseConverter();
		String interactionsHighlightsJson = this.getResourceAsString(s_interactionHighlightsNoSPSTopicRelationSentimentResponsePath);

		HighlightResult highlightsResult = jsonResponseConverter.getHighlights(interactionsHighlightsJson, tenant, channel);
		List<UtteranceHighlights> utterancesHighlights = highlightsResult.getHighlights();

		assertNotNull(utterancesHighlights);

		// 23 utterances
		// only utterances which has at least one highlight
		assertEquals(24, utterancesHighlights.size());

		UtteranceHighlights utterance;

		for (int i = 0; i < utterancesHighlights.size(); i++) {
			utterance = utterancesHighlights.get(i);

			switch (utterance.getDocumentId()) {
				case "b248f91c-8061-412d-b01e-b300ddc7ecac-12":
					assertEquals(1, utterance.getEntitiesHighlights().size());
					assertEquals(null, utterance.getEntitiesHighlights().get(0).getSentiments());
					assertEquals(45, utterance.getEntitiesHighlights().get(0).getStarts());
					assertEquals(61, utterance.getEntitiesHighlights().get(0).getEnds());
					break;
				case "b248f91c-8061-412d-b01e-b300ddc7ecac-24":
					List<EntityHighlight> entitiesHighligts = utterance.getEntitiesHighlights();
					assertEquals(2, entitiesHighligts.size());

					EntityHighlight firstHighlight = entitiesHighligts.get(0);
					List<SentimentHighlight> sentimentsHighlights = firstHighlight.getSentiments();

					assertEquals("/Customer Experience/experience", firstHighlight.getTopic());
					assertEquals(2, sentimentsHighlights.get(0).getValue());
					assertEquals(9, sentimentsHighlights.get(0).getPositions().get(0).getStarts());
					assertEquals(18, sentimentsHighlights.get(0).getPositions().get(0).getEnds());
					break;
			}
		}
	}

	@Override
	protected ResponseConverter createResponseConverter() {
		EASearchResponseConverter eaSearchResponseConverter = new EASearchResponseConverter();
		val metricFields = this.getFieldsMetrics();

		eaSearchResponseConverter.setSolrResponseConverter(new SolrResponseConverter().setMetricFields(metricFields));
		eaSearchResponseConverter.setTextEngineConfigurationService(textEngineConfigurationServiceMock);
		return eaSearchResponseConverter;
	}

	protected ResponseConverter CreateSolrResponseConverter() {
		EASearchResponseConverter eaSearchResponseConverter = new EASearchResponseConverter();
		eaSearchResponseConverter.setSolrResponseConverter(new SolrResponseConverter());
		eaSearchResponseConverter.setTextEngineConfigurationService(textEngineConfigurationServiceMock);
		return eaSearchResponseConverter;
	}
}
