package com.verint.textanalytics.bl.applicationservices;

import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.dal.darwin.TextAnalyticsProvider;
import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import com.verint.textanalytics.model.storedSearch.Category;
import com.verint.textanalytics.model.trends.TextElementTrend;
import com.verint.textanalytics.model.trends.TrendType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class TrendsServiceTest {

	@Mock
	TextAnalyticsProvider textAnalyticsProvider;

	@Mock
	private ConfigurationManager configurationManager;

	@Mock
	StoredSearchService storedSearchService;

	@Mock
	private ApplicationConfiguration applicationConfiguration;

	@InjectMocks
	private DailyVolumeService dailyVolumeService;

	@Mock
	private TrendsService trendsService;

	String tenant;
	String channel;
	SearchInteractionsContext searchContext;
	SearchInteractionsContext backgroundContext;
	String language;

	@Before
	public void setUp() throws Exception {

		MockitoAnnotations.initMocks(this);

		Mockito.when(configurationManager.getApplicationConfiguration()).thenReturn(applicationConfiguration);
		Mockito.when(applicationConfiguration.getDarwinRestRequestTimeout()).thenReturn(30);

		Mockito.when(trendsService.getTextElementsTrends(null, null, null, TrendType.Entities, null, null, null, null)).thenReturn(this.initEntitiesMockData());

		Mockito.when(trendsService.getTextElementsTrends(null, null, null, TrendType.Relations, null, null, null, null)).thenReturn(this.initRelationsMockData());

		// Categories
		Mockito.when(trendsService.getStoredSearchService()).thenReturn(storedSearchService);

		Mockito.when(trendsService.getMergedElementsTrends(null, null, null, null, null, null, null)).thenCallRealMethod();
		Mockito.when(trendsService.getCategoriesTrends(null, null, null, null, null, null)).thenCallRealMethod();

		Mockito.when(trendsService.getConfigurationManager()).thenReturn(configurationManager);
	}

	//region Data mocks

	private List<TextElementTrend> initEntitiesMockData() {
		TextElementTrend trend;
		List<TextElementTrend> entities = new ArrayList<>();

		trend = new TextElementTrend();
		trend.setTrendType(TrendType.Entities);
		trend.setName("entity1");
		trend.setPrVolume(1);
		entities.add(trend);

		trend = new TextElementTrend();
		trend.setTrendType(TrendType.Entities);
		trend.setName("entity2");
		trend.setPrVolume(2);
		entities.add(trend);

		return entities;
	}

	private List<TextElementTrend> initRelationsMockData() {
		TextElementTrend trend;
		List<TextElementTrend> relations = new ArrayList<>();

		trend = new TextElementTrend();
		trend.setTrendType(TrendType.Relations);
		trend.setName("rel1->relation1");
		trend.setPrVolume(1);
		relations.add(trend);

		trend = new TextElementTrend();
		trend.setTrendType(TrendType.Relations);
		trend.setName("rel2->relation2");
		trend.setPrVolume(2);
		relations.add(trend);

		return relations;
	}

	//endregion

	@Test
	public void getMergedElementsTrends() {

		//trendsService.setConfigurationManager(configurationManager);

		List<TextElementTrend> res;
		res = trendsService.getMergedElementsTrends(null, null, null, null, null, null, null);
		assertTrue(res.get(0).getName() == "entity1");
		assertTrue(res.get(1).getName() == "entity2");
		assertTrue(res.get(2).getName() == "rel1->relation1");
		assertTrue(res.get(3).getName() == "rel2->relation2");

		assertTrue(res.get(0).getPrVolume() == 1);
		assertTrue(res.get(1).getPrVolume() == 2);
		assertTrue(res.get(2).getPrVolume() == 1);
		assertTrue(res.get(3).getPrVolume() == 2);
	}

	//@Test
	public void getCategoriesTrends_simplest() {

		//region Initialize Mock data

		Mockito.when(trendsService.getTextElementsTrends(null, null, null, TrendType.Categories, null, null, null, null)).thenAnswer(answer -> {

			TextElementTrend trend;
			List<TextElementTrend> categories = new ArrayList<>();

			trend = new TextElementTrend();
			trend.setTrendType(TrendType.Categories);
			trend.setName("1");
			trend.setValue("1");
			categories.add(trend);

			trend = new TextElementTrend();
			trend.setTrendType(TrendType.Categories);
			trend.setName("2");
			trend.setValue("2");
			categories.add(trend);

			return categories;
		});

		Mockito.when(storedSearchService.getCategoriesMap(null, null)).thenAnswer(answer -> {
			HashMap<Integer, Category> catNames = new HashMap<>();
			Category cat = new Category();
			cat.setId(1);
			cat.setName("catLabel1");
			cat.setActive(true);
			catNames.put(1, cat);

			cat = new Category();
			cat.setId(2);
			cat.setName("catLabel2");
			cat.setActive(true);
			catNames.put(2, cat);

			return catNames;
		});

		//endregion

		List<TextElementTrend> res;
		res = trendsService.getCategoriesTrends(null, null, null, null, null, null);

		Assert.assertEquals(res.size(), 2);
		Assert.assertEquals(res.get(0).getName(), "catLabel1");
		Assert.assertEquals(res.get(0).getValue(), "1");
		Assert.assertEquals(res.get(1).getName(), "catLabel2");
		Assert.assertEquals(res.get(1).getValue(), "2");
	}

	//	@Test
	public void getCategoriesTrends_missingAllCategoryNames() {

		//region Initialize Mock data

		Mockito.when(trendsService.getTextElementsTrends(null, null, null, TrendType.Categories, null, null, null, null)).thenAnswer(answer -> {

			TextElementTrend trend;
			List<TextElementTrend> categories = new ArrayList<>();

			trend = new TextElementTrend();
			trend.setTrendType(TrendType.Categories);
			trend.setName("1");
			trend.setValue("1");
			categories.add(trend);

			trend = new TextElementTrend();
			trend.setTrendType(TrendType.Categories);
			trend.setName("2");
			trend.setValue("2");
			categories.add(trend);

			return categories;
		});

		Mockito.when(storedSearchService.getCategoriesMap(null, null)).thenAnswer(answer -> {
			HashMap<Integer, Category> catNames = new HashMap<>();
			return catNames;
		});

		//endregion

		List<TextElementTrend> res;
		res = trendsService.getCategoriesTrends(null, null, null, null, null, null);

		Assert.assertEquals(res.size(), 0);

	}

	//@Test
	public void getCategoriesTrends_missingSomeCategoryNames() {

		//region Initialize Mock data

		Mockito.when(trendsService.getTextElementsTrends(null, null, null, TrendType.Categories, null, null, null, null)).thenAnswer(answer -> {

			TextElementTrend trend;
			List<TextElementTrend> categories = new ArrayList<>();

			trend = new TextElementTrend();
			trend.setTrendType(TrendType.Categories);
			trend.setName("1");
			trend.setValue("1");
			categories.add(trend);

			trend = new TextElementTrend();
			trend.setTrendType(TrendType.Categories);
			trend.setName("2");
			trend.setValue("2");
			categories.add(trend);

			return categories;
		});

		Mockito.when(storedSearchService.getCategoriesMap(null, null)).thenAnswer(answer -> {
			HashMap<Integer, Category> catNames = new HashMap<>();
			Category cat = new Category();
			cat.setId(1);
			cat.setName("catLabel1");
			cat.setActive(true);
			catNames.put(1, cat);

			return catNames;
		});

		//endregion

		List<TextElementTrend> res;
		res = trendsService.getCategoriesTrends(null, null, null, null, null, null);

		Assert.assertEquals(res.size(), 1);
		Assert.assertEquals(res.get(0).getName(), "catLabel1");
		Assert.assertEquals(res.get(0).getValue(), "1");

	}

}
