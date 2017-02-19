package com.verint.textanalytics.web.uiservices;

import com.verint.textanalytics.common.utils.DataUtils;
import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import com.verint.textanalytics.model.modelEditor.Domain;
import com.verint.textanalytics.model.modelEditor.Language;
import com.verint.textanalytics.model.modelEditor.ModelsTree;
import com.verint.textanalytics.model.trends.TrendType;
import com.verint.textanalytics.web.viewmodel.Category;
import com.verint.textanalytics.web.viewmodel.SavedSearch;
import com.verint.textanalytics.web.viewmodel.TextElementTrend;
import lombok.val;
import org.junit.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ViewModelConverterTest {

	@Mock
	private ViewModelConverter viewModelConverter;

	/**
	 * @throws Exception Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}

	@Before
	public void setUp() throws Exception {

		// this will execute the injection
		MockitoAnnotations.initMocks(this);

		Mockito.when(viewModelConverter.convertToViewModelMergedTrend(null, TrendType.Entities)).thenReturn(this.buildMergedTrend());
		Mockito.when(viewModelConverter.convertToViewModelMergedTrend(Mockito.any(new ArrayList<TextElementTrend>().getClass()), Mockito.any(String.class),
		                                                              Mockito.any(String.class), Mockito.any(TrendType.class))).thenCallRealMethod();

		Mockito.when(viewModelConverter.convertToCategoryModel(Mockito.any())).thenCallRealMethod();
		Mockito.when(viewModelConverter.convertToCategoryViewModel(Mockito.any())).thenCallRealMethod();
		Mockito.when(viewModelConverter.savedSearchConverterToModel(Mockito.any())).thenCallRealMethod();
		Mockito.when(viewModelConverter.savedSearchConverterToViewModel(Mockito.any())).thenCallRealMethod();
		Mockito.when(viewModelConverter.categoriesListConverterToViewModel(Mockito.anyList())).thenCallRealMethod();
		Mockito.when(viewModelConverter.savedSearchesListConverterToViewModel(Mockito.anyList())).thenCallRealMethod();
	}

	/**
	 * @throws Exception Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {

	}

	private List<TextElementTrend> buildMergedTrend() {
		List<TextElementTrend> mergedTrend = new ArrayList<TextElementTrend>();

		TextElementTrend trend;

		trend = new TextElementTrend();
		trend.setSentiment(-1);
		trend.setName("US States");
		trend.setValue("1/US States");
		trend.setVolume(7578.0);
		trend.setAbsChangePercentage(14.6176123);
		trend.setRelChangePercentage(999.0);
		mergedTrend.add(trend);

		trend = new TextElementTrend();
		trend.setSentiment(-1);
		trend.setName("Travel Considerations");
		trend.setValue("1/Travel Considerations");
		trend.setVolume(7076.0);
		trend.setAbsChangePercentage(6.00765821831);
		trend.setRelChangePercentage(-164.0);
		mergedTrend.add(trend);

		trend = new TextElementTrend();
		trend.setSentiment(-1);
		trend.setName("Rewards");
		trend.setValue("1/Rewards");
		trend.setVolume(19191.0);
		trend.setAbsChangePercentage(-15.00765821831);
		trend.setRelChangePercentage(-1000.532);
		mergedTrend.add(trend);

		return mergedTrend;
	}

	@Test
	@Ignore
	public void convertToViewModelMergedTrend_SortByNameAsc() {

		String sortBy = "Name";
		String direction = "asc";

		List<TextElementTrend> mergedTrends = viewModelConverter.convertToViewModelMergedTrend(null, sortBy, direction, TrendType.Entities);

		assertEquals(mergedTrends.get(0).getName(), "Rewards");
		assertEquals(mergedTrends.get(1).getName(), "Travel Considerations");
		assertEquals(mergedTrends.get(2).getName(), "US States");

	}

	@Test
	public void convertToViewModelMergedTrend_SortByNameDesc() {

		String sortBy = "Name";
		String direction = "desc";

		List<TextElementTrend> mergedTrends = viewModelConverter.convertToViewModelMergedTrend(null, sortBy, direction, TrendType.Entities);

		assertEquals(mergedTrends.get(0).getName(), "US States");
		assertEquals(mergedTrends.get(1).getName(), "Travel Considerations");
		assertEquals(mergedTrends.get(2).getName(), "Rewards");

	}

	@Test
	public void convertToViewModelMergedTrend_SortByAbsChangePercentageAsc() {
		String sortBy = "AbsoluteChange";
		String direction = "asc";

		List<TextElementTrend> mergedTrends = viewModelConverter.convertToViewModelMergedTrend(null, sortBy, direction, TrendType.Entities);

		assertTrue(mergedTrends.get(0).getAbsChangePercentage() == 6.00765821831);
		assertTrue(mergedTrends.get(1).getAbsChangePercentage() == 14.6176123);
		assertTrue(mergedTrends.get(2).getAbsChangePercentage() == -15.00765821831);
	}

	@Test

	public void convertToViewModelMergedTrend_SortByAbsChangePercentageDesc() {

		String sortBy = "AbsoluteChange";
		String direction = "desc";

		List<TextElementTrend> mergedTrends = viewModelConverter.convertToViewModelMergedTrend(null, sortBy, direction, TrendType.Entities);

		assertTrue(mergedTrends.get(0).getAbsChangePercentage() == -15.00765821831);
		assertTrue(mergedTrends.get(1).getAbsChangePercentage() == 14.6176123);
		assertTrue(mergedTrends.get(2).getAbsChangePercentage() == 6.00765821831);
	}

	@Test

	public void convertToViewModelMergedTrend_SortByRelChangePercentageAsc() {

		String sortBy = "RelativeChange";
		String direction = "asc";

		List<TextElementTrend> mergedTrends = viewModelConverter.convertToViewModelMergedTrend(null, sortBy, direction, TrendType.Entities);

		assertTrue(mergedTrends.get(0).getRelChangePercentage() == -164.0);
		assertTrue(mergedTrends.get(1).getRelChangePercentage() == 999.0);
		assertTrue(mergedTrends.get(2).getRelChangePercentage() == -1000.532);
	}

	@Test

	public void convertToViewModelMergedTrend_SortByRelChangePercentageDesc() {

		String sortBy = "RelativeChange";
		String direction = "desc";

		List<TextElementTrend> mergedTrends = viewModelConverter.convertToViewModelMergedTrend(null, sortBy, direction, TrendType.Entities);

		assertTrue(mergedTrends.get(0).getRelChangePercentage() == -1000.532);
		assertTrue(mergedTrends.get(1).getRelChangePercentage() == 999.0);
		assertTrue(mergedTrends.get(2).getRelChangePercentage() == -164.0);

	}

	@Test
	public void categoryConverterToModelTest() {

		Category category = new Category();

		category.setActive(true);
		category.setColor(2);
		category.setDescription("description");
		category.setId(1);
		category.setImpact(3);
		category.setLastChangeDateTimeGMT("2015-03-12T11:22:33.123Z");
		category.setLastModifiedByUserId(123);
		category.setLastModifiedByUserName("123");
		category.setName("cat1");
		category.setPublished(true);
		val searchContext = new SearchInteractionsContext();
		category.setSearchContext(searchContext);
		category.setSearchContextVersion("1.0.0.0");

		com.verint.textanalytics.model.storedSearch.Category res = viewModelConverter.convertToCategoryModel(category);

		assertEquals(true, res.isActive());
		assertEquals(2, res.getColor());
		assertEquals("description", res.getDescription());
		assertEquals(1, res.getId());
		assertEquals(3, res.getImpact());
		assertEquals(DataUtils.getDateFromISO8601StringWithMilliseconds("2015-03-12T11:22:33.123Z"), res.getLastChangeDateTimeGMT());
		assertEquals(123, res.getLastModifiedByUserId());
		assertEquals("cat1", res.getName());
		assertEquals(true, res.isPublished());
		assertEquals(null, res.getQuery());
		assertEquals(searchContext, res.getSearchContext());
		assertEquals("1.0.0.0", res.getSearchContextVersion());
	}

	@Test
	@Ignore
	public void categoryConverterToViewModelTest() {
		com.verint.textanalytics.model.storedSearch.Category category = new com.verint.textanalytics.model.storedSearch.Category();

		category.setActive(true);
		category.setColor(2);
		category.setDescription("description");
		category.setId(1);
		category.setImpact(3);
		category.setLastChangeDateTimeGMT(DataUtils.getDateFromISO8601StringWithMilliseconds("2015-03-12T11:22:33.123Z"));
		category.setLastModifiedByUserId(123);
		category.setName("cat1");
		category.setPublished(true);
		category.setQuery("query");
		val searchContext = new SearchInteractionsContext();
		category.setSearchContext(searchContext);
		category.setSearchContextVersion("1.0.0.0");

		val res = viewModelConverter.convertToCategoryViewModel(category);

		assertEquals(true, res.isActive());
		assertEquals(2, res.getColor());
		assertEquals("description", res.getDescription());
		assertEquals(1, res.getId());
		assertEquals(3, res.getImpact());
		assertEquals("2015-03-12T11:22:33.123Z", res.getLastChangeDateTimeGMT());
		assertEquals(1426159353123l, res.getLastChangeDateTimeGMTMillis());
		assertEquals("USER NAME - TBD", res.getLastModifiedByUserName());
		assertEquals(123, res.getLastModifiedByUserId());
		assertEquals("cat1", res.getName());
		assertEquals(true, res.isPublished());
		assertEquals(searchContext, res.getSearchContext());
		assertEquals("1.0.0.0", res.getSearchContextVersion());
	}

	@Test
	public void savedSearchConverterToModelTest() {
		SavedSearch savedSearch = new SavedSearch();

		savedSearch.setDescription("description");
		savedSearch.setId(1);
		savedSearch.setLastChangeDateTimeGMT("2015-03-12T11:22:33.123Z");
		savedSearch.setLastModifiedByUserId(123);
		savedSearch.setLastModifiedByUserName("123");
		savedSearch.setName("cat1");
		savedSearch.setPublic(true);

		val searchContext = new SearchInteractionsContext();
		savedSearch.setSearchContext(searchContext);
		savedSearch.setSearchContextVersion("1.0.0.0");

		val res = viewModelConverter.savedSearchConverterToModel(savedSearch);

		assertEquals("description", res.getDescription());
		assertEquals(1, res.getId());
		assertEquals(DataUtils.getDateFromISO8601StringWithMilliseconds("2015-03-12T11:22:33.123Z"), res.getLastChangeDateTimeGMT());
		assertEquals(123, res.getLastModifiedByUserId());
		assertEquals("cat1", res.getName());
		assertEquals(true, res.isPublic());
		assertEquals(null, res.getQuery());
		assertEquals(searchContext, res.getSearchContext());
		assertEquals("1.0.0.0", res.getSearchContextVersion());
	}

	@Test
	public void savedSearchConverterToViewModelTest() {
		com.verint.textanalytics.model.storedSearch.SavedSearch savedSearch = new com.verint.textanalytics.model.storedSearch.SavedSearch();

		savedSearch.setDescription("description");
		savedSearch.setId(1);
		savedSearch.setLastChangeDateTimeGMT(DataUtils.getDateFromISO8601StringWithMilliseconds("2015-03-12T11:22:33.123Z"));
		savedSearch.setLastModifiedByUserId(123);
		savedSearch.setName("cat1");
		savedSearch.setPublic(true);
		savedSearch.setQuery("query");
		val searchContext = new SearchInteractionsContext();
		savedSearch.setSearchContext(searchContext);
		savedSearch.setSearchContextVersion("1.0.0.0");

		val res = viewModelConverter.savedSearchConverterToViewModel(savedSearch);

		assertEquals("description", res.getDescription());
		assertEquals(1, res.getId());
		assertEquals("2015-03-12T11:22:33.123Z", res.getLastChangeDateTimeGMT());
		assertEquals(1426159353123l, res.getLastChangeDateTimeGMTMillis());
		assertEquals("USER NAME - TBD", res.getLastModifiedByUserName());
		assertEquals(123, res.getLastModifiedByUserId());
		assertEquals("cat1", res.getName());
		assertEquals(true, res.isPublic());
		assertEquals(searchContext, res.getSearchContext());
		assertEquals("1.0.0.0", res.getSearchContextVersion());

	}

	@Test
	@Ignore
	public void categoriesListConverterToViewModelTest() {
		List<com.verint.textanalytics.model.storedSearch.Category> categoriesList = new ArrayList<com.verint.textanalytics.model.storedSearch.Category>();

		com.verint.textanalytics.model.storedSearch.Category category1 = new com.verint.textanalytics.model.storedSearch.Category();

		category1.setActive(true);
		category1.setColor(2);
		category1.setDescription("description");
		category1.setId(1);
		category1.setImpact(3);
		category1.setLastChangeDateTimeGMT(DataUtils.getDateFromISO8601StringWithMilliseconds("2015-03-12T11:22:33.123Z"));
		category1.setLastModifiedByUserId(123);
		category1.setName("cat1");
		category1.setPublished(true);
		category1.setQuery("query");
		val searchContext1 = new SearchInteractionsContext();
		category1.setSearchContext(searchContext1);
		category1.setSearchContextVersion("1.0.0.0");

		categoriesList.add(category1);

		com.verint.textanalytics.model.storedSearch.Category category2 = new com.verint.textanalytics.model.storedSearch.Category();

		category2.setActive(true);
		category2.setColor(22);
		category2.setDescription("description2");
		category2.setId(12);
		category2.setImpact(32);
		category2.setLastChangeDateTimeGMT(DataUtils.getDateFromISO8601StringWithMilliseconds("2012-03-12T11:22:33.123Z"));
		category2.setLastModifiedByUserId(1232);
		category2.setName("cat12");
		category2.setPublished(true);
		category2.setQuery("query2");
		val searchContext2 = new SearchInteractionsContext();
		category2.setSearchContext(searchContext2);
		category2.setSearchContextVersion("1.0.0.2");

		categoriesList.add(category2);

		val res = viewModelConverter.categoriesListConverterToViewModel(categoriesList);

		int i = 0;
		assertEquals(true, res.get(i).isActive());
		assertEquals(2, res.get(i).getColor());
		assertEquals("description", res.get(i).getDescription());
		assertEquals(1, res.get(i).getId());
		assertEquals(3, res.get(i).getImpact());
		assertEquals("2015-03-12T11:22:33.123Z", res.get(i).getLastChangeDateTimeGMT());
		assertEquals(1426159353123l, res.get(i).getLastChangeDateTimeGMTMillis());
		assertEquals("USER NAME - TBD", res.get(i).getLastModifiedByUserName());
		assertEquals(123, res.get(i).getLastModifiedByUserId());
		assertEquals("cat1", res.get(i).getName());
		assertEquals(true, res.get(i).isPublished());
		assertEquals(searchContext1, res.get(i).getSearchContext());
		assertEquals("1.0.0.0", res.get(i).getSearchContextVersion());

		i = 1;
		assertEquals(true, res.get(i).isActive());
		assertEquals(22, res.get(i).getColor());
		assertEquals("description2", res.get(i).getDescription());
		assertEquals(12, res.get(i).getId());
		assertEquals(32, res.get(i).getImpact());
		assertEquals("2012-03-12T11:22:33.123Z", res.get(i).getLastChangeDateTimeGMT());
		assertEquals(1331551353123l, res.get(i).getLastChangeDateTimeGMTMillis());
		assertEquals("USER NAME - TBD", res.get(i).getLastModifiedByUserName());
		assertEquals(1232, res.get(i).getLastModifiedByUserId());
		assertEquals("cat12", res.get(i).getName());
		assertEquals(true, res.get(i).isPublished());
		assertEquals(searchContext2, res.get(i).getSearchContext());
		assertEquals("1.0.0.2", res.get(i).getSearchContextVersion());
	}

	@Test
	public void savedSearchesListConverterToViewModelTest() {
		List<com.verint.textanalytics.model.storedSearch.SavedSearch> savedSearchesList = new ArrayList<com.verint.textanalytics.model.storedSearch.SavedSearch>();

		com.verint.textanalytics.model.storedSearch.SavedSearch savedSearch1 = new com.verint.textanalytics.model.storedSearch.SavedSearch();

		savedSearch1.setDescription("description");
		savedSearch1.setId(1);
		savedSearch1.setLastChangeDateTimeGMT(DataUtils.getDateFromISO8601StringWithMilliseconds("2015-03-12T11:22:33.123Z"));
		savedSearch1.setLastModifiedByUserId(123);
		savedSearch1.setName("cat1");
		savedSearch1.setPublic(true);
		savedSearch1.setQuery("query");
		val searchContext1 = new SearchInteractionsContext();
		savedSearch1.setSearchContext(searchContext1);
		savedSearch1.setSearchContextVersion("1.0.0.0");

		savedSearchesList.add(savedSearch1);

		com.verint.textanalytics.model.storedSearch.SavedSearch savedSearch2 = new com.verint.textanalytics.model.storedSearch.SavedSearch();

		savedSearch2.setDescription("description2");
		savedSearch2.setId(12);
		savedSearch2.setLastChangeDateTimeGMT(DataUtils.getDateFromISO8601StringWithMilliseconds("2012-03-12T11:22:33.123Z"));
		savedSearch2.setLastModifiedByUserId(1232);
		savedSearch2.setName("cat12");
		savedSearch2.setPublic(true);
		savedSearch2.setQuery("query2");
		val searchContext2 = new SearchInteractionsContext();
		savedSearch2.setSearchContext(searchContext2);
		savedSearch2.setSearchContextVersion("1.0.0.2");

		savedSearchesList.add(savedSearch2);

		val res = viewModelConverter.savedSearchesListConverterToViewModel(savedSearchesList);

		int i = 0;
		assertEquals("description", res.get(i).getDescription());
		assertEquals(1, res.get(i).getId());
		assertEquals("2015-03-12T11:22:33.123Z", res.get(i).getLastChangeDateTimeGMT());
		assertEquals(1426159353123l, res.get(i).getLastChangeDateTimeGMTMillis());
		assertEquals("USER NAME - TBD", res.get(i).getLastModifiedByUserName());
		assertEquals(123, res.get(i).getLastModifiedByUserId());
		assertEquals("cat1", res.get(i).getName());
		assertEquals(true, res.get(i).isPublic());
		assertEquals(searchContext1, res.get(i).getSearchContext());
		assertEquals("1.0.0.0", res.get(i).getSearchContextVersion());

		i = 1;
		assertEquals("description2", res.get(i).getDescription());
		assertEquals(12, res.get(i).getId());
		assertEquals("2012-03-12T11:22:33.123Z", res.get(i).getLastChangeDateTimeGMT());
		assertEquals(1331551353123l, res.get(i).getLastChangeDateTimeGMTMillis());
		assertEquals("USER NAME - TBD", res.get(i).getLastModifiedByUserName());
		assertEquals(1232, res.get(i).getLastModifiedByUserId());
		assertEquals("cat12", res.get(i).getName());
		assertEquals(true, res.get(i).isPublic());
		assertEquals(searchContext2, res.get(i).getSearchContext());
		assertEquals("1.0.0.2", res.get(i).getSearchContextVersion());
	}

	@Test
	public void modelsTreeConverterTest() {

		val realViewModelConverter = new ViewModelConverter();
		ModelsTree modelsTreeVM = new ModelsTree();

		List<Language> languages = new ArrayList<Language>();

		///////////////
		Language language1 = new Language();
		language1.setName("language1");

		Domain domain1 = new Domain();
		domain1.setName("domain1");
		Domain domain2 = new Domain();
		domain2.setName("domain2");

		List<Domain> domains1 = new ArrayList<Domain>();
		domains1.add(domain1);
		domains1.add(domain2);

		language1.setDomains(domains1);
		///////////////
		Language language2 = new Language();
		language2.setName("language2");

		Domain domain3 = new Domain();
		domain3.setName("domain3");
		Domain domain4 = new Domain();
		domain4.setName("domain4");
		Domain domain5 = new Domain();
		domain5.setName("domain5");

		List<Domain> domains2 = new ArrayList<Domain>();
		domains2.add(domain3);
		domains2.add(domain4);
		domains2.add(domain5);

		language2.setDomains(domains2);

		/////////////
		languages.add(language1);
		languages.add(language2);

		modelsTreeVM.setLanguages(languages);

		val res = realViewModelConverter.modelsTreeConverter(modelsTreeVM);

		assertNotNull(res);
		assertEquals(5, res.getModels().size());

		int i = 0;
		assertEquals("language1", res.getModels().get(i).getLanguage());
		assertEquals("domain1", res.getModels().get(i).getDomain());

		i = 1;
		assertEquals("language1", res.getModels().get(i).getLanguage());
		assertEquals("domain2", res.getModels().get(i).getDomain());

		i = 2;
		assertEquals("language2", res.getModels().get(i).getLanguage());
		assertEquals("domain3", res.getModels().get(i).getDomain());

		i = 3;
		assertEquals("language2", res.getModels().get(i).getLanguage());
		assertEquals("domain4", res.getModels().get(i).getDomain());

		i = 4;
		assertEquals("language2", res.getModels().get(i).getLanguage());
		assertEquals("domain5", res.getModels().get(i).getDomain());

	}
}
