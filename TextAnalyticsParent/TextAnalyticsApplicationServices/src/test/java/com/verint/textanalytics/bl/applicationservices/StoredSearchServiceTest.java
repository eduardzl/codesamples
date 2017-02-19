package com.verint.textanalytics.bl.applicationservices;

import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.exceptions.StoredSearchesErrorCode;
import com.verint.textanalytics.common.exceptions.StoredSearchesException;
import com.verint.textanalytics.common.utils.DataUtils;
import com.verint.textanalytics.dal.configService.ConfigurationServiceProvider;
import com.verint.textanalytics.dal.darwin.TextAnalyticsProvider;
import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import com.verint.textanalytics.model.storedSearch.*;
import lombok.val;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class StoredSearchServiceTest extends BaseTest {

	String tenant;
	String channel;
	String language;

	@InjectMocks
	private StoredSearchService storedSearchService;

	@Mock
	private ConfigurationManager configurationManagerMock;

	@Mock
	protected ApplicationConfiguration appConfigMock;

	@Mock
	private ConfigurationServiceProvider configurationServiceProviderMock;

	@Mock
	private TextAnalyticsProvider textAnalyticsProvider;

	@Mock
	private StoredSearchesMerger storedSearchesMerger;

	@Mock
	private ConfigurationService configurationService;

	private

	SearchInteractionsContext contextSearch;
	SearchInteractionsContext bgSearch;
	
	@Before
	public void initialize() {

		this.channel = "channel";
		this.tenant = "tenant";
		this.language = "en";

		MockitoAnnotations.initMocks(this);

		Mockito.when(textAnalyticsProvider.getSearchInteractionsQueryForCategory(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.anyString()))
		       .thenReturn(new StoredSearchQuery("TestQuery", "TestQuery"));

		Mockito.when(appConfigMock.getTimeStempFiled()).thenReturn("lastUpdate");
		Mockito.when(appConfigMock.getNumberOfRetriesInStoredSearches()).thenReturn(3);
		Mockito.when(appConfigMock.getStoredSearchNameMaxLength()).thenReturn(30);
		Mockito.when(appConfigMock.getCategoryReprocessingAllowedIntervalMinutes()).thenReturn(10);

		Mockito.when(configurationManagerMock.getApplicationConfiguration()).thenReturn(appConfigMock);
		Mockito.when(configurationService.getChannelLanguage(tenant, channel)).thenReturn("en");


		storedSearchService.setNumOfRetries(3);

		/*
		Facet contextSearchFacet = new Facet();
		Facet bgSearchFacet = new Facet();
		List<FilterField> filterFields;
		FilterField filterField;
		
		contextSearch = new SearchInteractionsContext();
		filterFields = new ArrayList<FilterField>();
		filterField = new FilterField();
		filterField.setName("ContextField");
		filterFields.add(filterField);
		contextSearch.setFilterFields(filterFields);
		
		bgSearch = new SearchInteractionsContext();
		filterFields = new ArrayList<FilterField>();
		filterField = new FilterField();
		filterField.setName("bgField");
		filterFields.add(filterField);
		bgSearch.setFilterFields(filterFields);

		this.fillCorrelatedData(contextSearchFacet, bgSearchFacet);
		Mockito.when(storedSearchService.getCategoriesCorrelatedFacetWithMetrics(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any(),  Mockito.anyString())).thenCallRealMethod();
		Mockito.when(storedSearchService.getCategoriesFacetTree(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.anyString())).then(t -> {
			SearchInteractionsContext para = t.getArgumentAt(2,SearchInteractionsContext.class);
			if ("ContextField".equals(para.getFilterFields().get(0).getName()) == true) {
				return contextSearchFacet;
			}
			
			if ("bgField".equals(para.getFilterFields().get(0).getName()) == true) {
				return bgSearchFacet;	
			}

			return null;
		});
		*/
	}

	@Test
	public void addCategory_addWhenNoFileExistsTest() {

		List<Category> categories = new ArrayList<>();
		categories.add((Category) new Category().setId(1).setName("Category 1"));
		categories.add((Category) new Category().setId(2).setName("Category 2"));
		categories.add((Category) new Category().setId(3).setName("Category 3"));

		Category newCategory;
		SearchInteractionsContext searchContext = new SearchInteractionsContext();
		searchContext.setTerms(new ArrayList<String>(Arrays.asList("yeah", "iphone")));

		newCategory = (Category) new Category().setActive(true)
		                                       .setColor(1)
		                                       .setImpact(100)
		                                       .setPublished(true)
		                                       .setDescription("Test")
		                                       .setName("TestCategory")
		                                       .setSearchContext(searchContext);

		StoredSearchesMerger storedSearchesMerger = new StoredSearchesMerger();

		List<Category> mergedCategories = storedSearchesMerger.addNewStoredSearch(categories, newCategory);

		assertTrue(mergedCategories != null);
		assertTrue(mergedCategories.size() == 4);

		Optional<Category> addedCategory = mergedCategories.stream().filter(c -> c.getName().equalsIgnoreCase("TestCategory")).findAny();
		assertTrue(addedCategory.isPresent());
	}

	@Test
	public void addCategory_categoriesNameExistsTest() {

		SearchInteractionsContext searchContext = new SearchInteractionsContext();
		searchContext.setTerms(new ArrayList<String>(Arrays.asList("yeah", "iphone")));

		Category category, newCategory;
		category = (Category) new Category().setActive(true)
											.setColor(1)
											.setImpact(100)
											.setPublished(true)
											.setDescription("Test")
											.setName("TestCategory")
											.setSearchContext(searchContext)
											.setId(123);

		List<Category> categories = new ArrayList<>();
		categories.add(category);


		newCategory = (Category) new Category().setActive(true)
		                                       .setColor(1)
		                                       .setImpact(100)
		                                       .setPublished(true)
		                                       .setDescription("Test")
		                                       .setName("TestCategory")
		                                       .setSearchContext(searchContext);

		try {
			StoredSearchesMerger merger = new StoredSearchesMerger();
			merger.addNewStoredSearch(categories, newCategory);

		} catch (Exception e) {
			assertTrue(e instanceof StoredSearchesException);
			assertEquals(StoredSearchesErrorCode.CategoryAddNameAllreadyExistsError, ((StoredSearchesException) e).getAppExecutionErrorCode());
			return;
		}

		fail();
	}

	@Test
	//@formatter:off
	public void addCategory_retryWhenFailedInTheFirstTimeTest() {
		Category category, newCategory;
		SearchInteractionsContext searchContext = new SearchInteractionsContext();
		searchContext.setTerms(new ArrayList<String>(Arrays.asList("yeah", "iphone")));

		category = (Category) new Category().setActive(true)
		                                    .setColor(1)
		                                    .setImpact(100)
		                                    .setPublished(true)
		                                    .setDescription("Test")
		                                    .setName("TestCategory")
		                                    .setSearchContext(searchContext)
		                                    .setId(123);

		CategoriesRepository rep = (CategoriesRepository) new CategoriesRepository();
		rep.addCategories(new ArrayList<Category>(Arrays.asList(category)));
		rep.setMaxId(0)
		   .setTimeStamp(DateTime.now().toString());

		searchContext.setTerms(new ArrayList<String>(Arrays.asList("yeah", "iphone")));

		newCategory = (Category) new Category().setActive(true)
		                                       .setColor(1)
		                                       .setImpact(100)
		                                       .setPublished(true)
		                                       .setDescription("Test")
		                                       .setName("newCat")
		                                       .setSearchContext(searchContext);

		Mockito.when(storedSearchesMerger.addNewStoredSearch(rep.getCategories(), newCategory)).thenReturn(rep.getCategories());
		Mockito.when(configurationServiceProviderMock.getListOfCategories(tenant, channel))
		       .thenThrow(new RuntimeException("Yuliya is the King"))
		       .thenThrow(new RuntimeException("Idan is the King"))
		       .thenReturn(rep);


		try {
			storedSearchService.addCategory(tenant, channel, newCategory);
		}
		catch(Exception ex) {
			fail();
		}
	}
	//@formatter:on

	@Test
	//@formatter:off
	public void addCategory_retryWhenFailedInAllTimesTest() {
		Category newCategory;
		SearchInteractionsContext searchContext = new SearchInteractionsContext();
		searchContext.setTerms(new ArrayList<String>(Arrays.asList("yeah", "iphone")));

		Mockito.when(configurationServiceProviderMock.getListOfCategories(tenant, channel)).thenThrow(new RuntimeException("Yuliya is the King"))
		                                                                            .thenThrow(new RuntimeException("Idan is the King"))
		                                                                            .thenThrow(new RuntimeException("We all are the Kings"));

		searchContext.setTerms(new ArrayList<String>(Arrays.asList("yeah", "iphone")));

		newCategory = (Category) new Category().setActive(true)
		                                       .setColor(1)
		                                       .setImpact(100)
		                                       .setPublished(true)
		                                       .setDescription("Test")
		                                       .setName("newCat")
		                                       .setSearchContext(searchContext);

		Mockito.when(storedSearchesMerger.addNewStoredSearch(Mockito.anyListOf(Category.class), Mockito.any(Category.class)))
		                                 .thenReturn(new ArrayList<Category>());


		try {
			storedSearchService.addCategory(tenant, channel, newCategory);
		} catch (Exception e) {
			assertTrue(e instanceof StoredSearchesException);
			assertEquals(StoredSearchesErrorCode.CategoriesAddError, ((StoredSearchesException) e).getAppExecutionErrorCode());
			assertNotNull(((StoredSearchesException) e).get("RetryNumber-0"));
			assertNotNull(((StoredSearchesException) e).get("RetryNumber-1"));
			assertNotNull(((StoredSearchesException) e).get("RetryNumber-2"));
			return;
		}
		fail();
	}
	//@formatter:on

	@Test
	//@formatter:off
	public void deleteCategory_validateNoCategoryExistsAfterDeletion() {

		SearchInteractionsContext searchContext = new SearchInteractionsContext();
		searchContext.setTerms(new ArrayList<String>(Arrays.asList("yeah", "iphone")));
		DateTime categoryDateTime = DataUtils.getDateFromISO8601String("2015-12-05T13:11:59Z");

		Category category1 = (Category) new Category().setActive(true)
		                                       .setColor(1)
		                                       .setImpact(100)
		                                       .setPublished(true)
		                                       .setDescription("Test")
		                                       .setName("newCat1")
		                                        .setId(1)
		                                       .setSearchContext(searchContext)
											   .setLastChangeDateTimeGMT(categoryDateTime);

		Category category2 = (Category) new Category().setActive(true)
		                                                 .setColor(2)
		                                                 .setImpact(100)
		                                                 .setPublished(true)
		                                                 .setDescription("Test")
		                                                 .setName("newCat2")
		                                                 .setId(2)
		                                                 .setSearchContext(searchContext)
		                                                 .setLastChangeDateTimeGMT(categoryDateTime);

		Category category3 = (Category) new Category().setActive(true)
		                                                 .setColor(2)
		                                                 .setImpact(100)
		                                                 .setPublished(true)
		                                                 .setDescription("Test")
		                                                 .setName("newCat3")
		                                                 .setId(3)
		                                                 .setSearchContext(searchContext)
														 .setLastChangeDateTimeGMT(categoryDateTime);


		List<Category> categories = new ArrayList<>();
		categories.add(category1);
		categories.add(category2);
		categories.add(category3);

		Category categoryToRemove;

		categoryToRemove = (Category) new Category().setId(1).setLastChangeDateTimeGMT(categoryDateTime);

		StoredSearchesMerger merger = new StoredSearchesMerger();
		List<Category> mergedCategories =  merger.deleteStoredSearch(categories, categoryToRemove);

		assertEquals(mergedCategories.size(), 2);
		Optional<Category> deleted = mergedCategories.stream().filter(c -> c.getId() == 1).findFirst();
		assertFalse(deleted.isPresent());
	}
	//@formatter:on

	@Test
	//@formatter:off
	public void deleteCategory_lastModifiedDateTimeOutdatedTest() {

		SearchInteractionsContext searchContext = new SearchInteractionsContext();
		searchContext.setTerms(new ArrayList<String>(Arrays.asList("yeah", "iphone")));
		DateTime categoryDateTime = DataUtils.getDateFromISO8601String("2015-12-05T13:11:59Z");

		Category category1 = (Category) new Category().setActive(true)
		                                       .setColor(1)
		                                       .setImpact(100)
		                                       .setPublished(true)
		                                       .setDescription("Test")
		                                       .setName("newCat1")
		                                        .setId(1)
		                                       .setSearchContext(searchContext)
											   .setLastChangeDateTimeGMT(categoryDateTime);

		Category category2 = (Category) new Category().setActive(true)
		                                                 .setColor(2)
		                                                 .setImpact(100)
		                                                 .setPublished(true)
		                                                 .setDescription("Test")
		                                                 .setName("newCat2")
		                                                 .setId(2)
		                                                 .setSearchContext(searchContext)
		                                                 .setLastChangeDateTimeGMT(categoryDateTime);

		Category category3 = (Category) new Category().setActive(true)
		                                                 .setColor(2)
		                                                 .setImpact(100)
		                                                 .setPublished(true)
		                                                 .setDescription("Test")
		                                                 .setName("newCat3")
		                                                 .setId(3)
		                                                 .setSearchContext(searchContext)
														 .setLastChangeDateTimeGMT(categoryDateTime);


		List<Category> categories = new ArrayList<>();
		categories.add(category1);
		categories.add(category2);
		categories.add(category3);

		Category categoryToRemove;

		categoryToRemove = (Category) new Category().setId(1).setLastChangeDateTimeGMT(categoryDateTime.minusMinutes(10));

		try {
			StoredSearchesMerger merger = new StoredSearchesMerger();
			List<Category> mergedCategories = merger.deleteStoredSearch(categories, categoryToRemove);
		}
		catch(Exception ex) {
			assertEquals(ex.getClass(), StoredSearchesException.class);
			StoredSearchesException sex = (StoredSearchesException) ex;

			assertEquals(sex.getAppExecutionErrorCode(), StoredSearchesErrorCode.CategoryRemoveNotLatestVersion);
		}
	}
	//@formatter:on

	@Test
	//@formatter:off
	public void deleteCategory_retryWhenFailedInTheFirstTimeTest() {
		Category category, categoryToRemove;
		SearchInteractionsContext searchContext = new SearchInteractionsContext();
		searchContext.setTerms(new ArrayList<String>(Arrays.asList("yeah", "iphone")));

		category = (Category) new Category().setActive(true)
		                                    .setColor(1)
		                                    .setImpact(100)
		                                    .setPublished(true)
		                                    .setDescription("Test")
		                                    .setName("TestCategory")
		                                    .setSearchContext(searchContext)
		                                    .setId(123);

		CategoriesRepository rep = (CategoriesRepository) new CategoriesRepository().addCategories(new ArrayList<Category>(Arrays.asList(category))).setMaxId(0).setTimeStamp(DateTime.now().toString());

		Mockito.when(configurationServiceProviderMock.getListOfCategories(tenant, channel))
		       .thenThrow(new RuntimeException("Yuliya is the King"))
		       .thenThrow(new RuntimeException("Idan is the King"))
		       .thenReturn(rep);

		Mockito.when(storedSearchesMerger.deleteStoredSearch(Mockito.anyListOf(Category.class), Mockito.any(Category.class)))
		       .thenReturn(new ArrayList<Category>());


		categoryToRemove = (Category) new Category().setId(123);

		try {
			storedSearchService.deleteCategory(tenant, channel, categoryToRemove);
		}
		catch (Exception ex) {
			fail();
		}
	}
	//@formatter:on

	@Test
	//@formetter:off
	public void deleteCategory_retryWhenFailedInAllTimesTest() {
		Category categoryToRemove;
		SearchInteractionsContext searchContext = new SearchInteractionsContext();
		searchContext.setTerms(new ArrayList<String>(Arrays.asList("yeah", "iphone")));

		// throw exception 3 times
		Mockito.when(configurationServiceProviderMock.getListOfCategories(tenant, channel))
		       .thenThrow(new RuntimeException("Yuliya is the King"))
		       .thenThrow(new RuntimeException("Idan is the King"))
		       .thenThrow(new RuntimeException(
		                                                                                                                                                                                           "We all are the Kings"));

		searchContext.setTerms(new ArrayList<String>(Arrays.asList("yeah", "iphone")));

		categoryToRemove = (Category) new Category().setActive(true)
		                                            .setColor(1)
		                                            .setImpact(100)
		                                            .setPublished(true)
		                                            .setDescription("Test")
		                                            .setName("newCat")
		                                            .setSearchContext(searchContext);

		try {
			storedSearchService.deleteCategory(tenant, channel, categoryToRemove);
		} catch (Exception e) {
			assertTrue(e instanceof StoredSearchesException);
			assertEquals(StoredSearchesErrorCode.CategoryRemoveError, ((StoredSearchesException) e).getAppExecutionErrorCode());
			assertNotNull(((StoredSearchesException) e).get("RetryNumber-0"));
			assertNotNull(((StoredSearchesException) e).get("RetryNumber-1"));
			assertNotNull(((StoredSearchesException) e).get("RetryNumber-2"));
			return;
		}
		fail();
	}
	//@formatter:on

	public void addSavedSearch_addWhenNoFileExistsTest() {

		Mockito.when(configurationServiceProviderMock.getListOfSavedSearches(tenant, channel))
		       .thenReturn(
				       (SavedSearchesRepository) new SavedSearchesRepository().setSavedSearches(new ArrayList<SavedSearch>()).setMaxId(0).setTimeStamp(DateTime.now().toString()));

		SavedSearch newSavedSearch;
		SearchInteractionsContext searchContext = new SearchInteractionsContext();
		searchContext.setTerms(new ArrayList<String>(Arrays.asList("yeah", "iphone")));

		newSavedSearch = (SavedSearch) new SavedSearch().setPublic(true).setDescription("Test").setName("TestCategory").setSearchContext(searchContext);

		storedSearchService.addSavedSearch(tenant, channel, newSavedSearch);
	}

	@Test
	//@formatter:of
	public void addSavedSearch_SavedSearchesNameExistsTest() {

		SearchInteractionsContext searchContext = new SearchInteractionsContext();
		searchContext.setTerms(new ArrayList<String>(Arrays.asList("yeah", "iphone")));

		SavedSearch savedSearch, newSavedSearch;
		SavedSearch savedSearch1 = (SavedSearch) new SavedSearch().setPublic(true)
		                                             .setDescription("Test")
		                                             .setName("SavedSearch1")
		                                             .setSearchContext(searchContext)
		                                             .setId(1);

		SavedSearch savedSearch2 = (SavedSearch) new SavedSearch().setPublic(true)
		                                             .setDescription("Test")
		                                             .setName("SavedSearch2")
		                                             .setSearchContext(searchContext)
		                                             .setId(2);

		SavedSearch savedSearch3 = (SavedSearch) new SavedSearch().setPublic(true)
		                                                          .setDescription("Test")
		                                                          .setName("SavedSearch3")
		                                                          .setSearchContext(searchContext)
		                                                          .setId(3);
		List<SavedSearch> savedSearches = new ArrayList<>();
		savedSearches.add(savedSearch1);
		savedSearches.add(savedSearch2);
		savedSearches.add(savedSearch3);


		newSavedSearch = (SavedSearch) new SavedSearch().setPublic(true)
		                                                .setDescription("Test")
		                                                .setName("SavedSearch1")
		                                                .setSearchContext(searchContext);

		try {
			StoredSearchesMerger merger = new StoredSearchesMerger();
			merger.addNewStoredSearch(savedSearches, newSavedSearch);
		} catch (Exception e) {
			assertTrue(e instanceof StoredSearchesException);
			assertEquals(StoredSearchesErrorCode.SavedSearchAddNameAllreadyExistsError , ((StoredSearchesException) e).getAppExecutionErrorCode());
			return;
		}
		fail();
	}
	//@formatter:on

	@Test
	public void addSavedSearch_retryWhenFailedInTheFirstTimeTest() {
		SavedSearch savedSearch, newSavedSearch;
		SearchInteractionsContext searchContext = new SearchInteractionsContext();
		searchContext.setTerms(new ArrayList<String>(Arrays.asList("yeah", "iphone")));

		savedSearch = (SavedSearch) new SavedSearch().setDescription("Test").setName("TestCategory").setSearchContext(searchContext).setId(123);

		SavedSearchesRepository rep = (SavedSearchesRepository) new SavedSearchesRepository().setSavedSearches(new ArrayList<SavedSearch>(Arrays.asList(savedSearch))).setMaxId(0).setTimeStamp(DateTime.now().toString());
		Mockito.when(configurationServiceProviderMock.getListOfSavedSearches(tenant, channel)).thenThrow(new RuntimeException("Yuliya is the King")).thenThrow(new RuntimeException(
		                                                                                                                                                       "Idan is the King")).thenReturn(rep);

		searchContext.setTerms(new ArrayList<String>(Arrays.asList("yeah", "iphone")));

		newSavedSearch = (SavedSearch) new SavedSearch().setDescription("Test").setName("new").setSearchContext(searchContext);

		storedSearchService.addSavedSearch(tenant, channel, newSavedSearch);
	}

	@Test
	public void addSavedSearch_retryWhenFailedInAllTimesTest() {
		SavedSearch newSavedSearch;
		SearchInteractionsContext searchContext = new SearchInteractionsContext();
		searchContext.setTerms(new ArrayList<String>(Arrays.asList("yeah", "iphone")));

		Mockito.when(configurationServiceProviderMock.getListOfSavedSearches(tenant, channel)).thenThrow(new RuntimeException("Yuliya is the King")).thenThrow(new RuntimeException(
		                                                                                                                                                       "Idan is the King")).thenThrow(new RuntimeException(
		                                                                                                                                                                                              "We all are the Kings"));

		searchContext.setTerms(new ArrayList<String>(Arrays.asList("yeah", "iphone")));

		newSavedSearch = (SavedSearch) new SavedSearch().setDescription("Test").setName("newCat").setSearchContext(searchContext);

		try {
			storedSearchService.addSavedSearch(tenant, channel, newSavedSearch);
		} catch (Exception e) {
			assertTrue(e instanceof StoredSearchesException);
			assertEquals(StoredSearchesErrorCode.SavedSearchAddError, ((StoredSearchesException) e).getAppExecutionErrorCode());
			assertNotNull(((StoredSearchesException) e).get("RetryNumber-0"));
			assertNotNull(((StoredSearchesException) e).get("RetryNumber-1"));
			assertNotNull(((StoredSearchesException) e).get("RetryNumber-2"));
			return;
		}
		fail();
	}

	@Test
	public void deleteSavedSearch_addWhenNoFileExists_And_NoCategoryIDTest() {

		Mockito.when(configurationServiceProviderMock.getListOfSavedSearches(tenant, channel))
		       .thenReturn(
				       (SavedSearchesRepository) new SavedSearchesRepository().setSavedSearches(new ArrayList<SavedSearch>()).setMaxId(0).setTimeStamp(DateTime.now().toString()));

		SavedSearch savedSearchToRemove;
		SearchInteractionsContext searchContext = new SearchInteractionsContext();
		searchContext.setTerms(new ArrayList<String>(Arrays.asList("yeah", "iphone")));

		savedSearchToRemove = (SavedSearch) new SavedSearch().setDescription("Test").setName("TestCategory").setSearchContext(searchContext).setId(123);

		storedSearchService.deleteSavedSearch(tenant, channel, savedSearchToRemove);
	}

	@Test
	public void deleteSavedSearch_retryWhenFailedInTheFirstTimeTest() {
		SavedSearch savedSearch, savedSearchToRemove;
		SearchInteractionsContext searchContext = new SearchInteractionsContext();
		searchContext.setTerms(new ArrayList<String>(Arrays.asList("yeah", "iphone")));

		savedSearch = (SavedSearch) new SavedSearch().setDescription("Test").setName("TestCategory").setSearchContext(searchContext).setId(123);

		SavedSearchesRepository rep = (SavedSearchesRepository) new SavedSearchesRepository().setSavedSearches(new ArrayList<SavedSearch>(Arrays.asList(savedSearch))).setMaxId(0).setTimeStamp(DateTime.now().toString());
		Mockito.when(configurationServiceProviderMock.getListOfSavedSearches(tenant, channel)).thenThrow(new RuntimeException("Yuliya is the King")).thenThrow(new RuntimeException(
		                                                                                                                                                       "Idan is the King")).thenReturn(rep);

		searchContext.setTerms(new ArrayList<String>(Arrays.asList("yeah", "iphone")));

		savedSearchToRemove = (SavedSearch) new SavedSearch().setDescription("Test").setName("newCat").setSearchContext(searchContext).setId(123);

		storedSearchService.deleteSavedSearch(tenant, channel, savedSearchToRemove);
	}

	@Test
	public void deleteSavedSearch_retryWhenFailedInAllTimesTest() {
		SavedSearch savedSearchToRemove;
		SearchInteractionsContext searchContext = new SearchInteractionsContext();
		searchContext.setTerms(new ArrayList<String>(Arrays.asList("yeah", "iphone")));

		Mockito.when(configurationServiceProviderMock.getListOfSavedSearches(tenant, channel)).thenThrow(new RuntimeException("Yuliya is the King")).thenThrow(new RuntimeException(
		                                                                                                                                                       "Idan is the King")).thenThrow(new RuntimeException(
		                                                                                                                                                                                              "We all are the Kings"));

		searchContext.setTerms(new ArrayList<String>(Arrays.asList("yeah", "iphone")));

		savedSearchToRemove = (SavedSearch) new SavedSearch().setDescription("Test").setName("newCat").setSearchContext(searchContext);

		try {
			storedSearchService.deleteSavedSearch(tenant, channel, savedSearchToRemove);
		} catch (Exception e) {
			assertTrue(e instanceof StoredSearchesException);
			assertEquals(StoredSearchesErrorCode.SavedSearchesRemoveError, ((StoredSearchesException) e).getAppExecutionErrorCode());
			assertNotNull(((StoredSearchesException) e).get("RetryNumber-0"));
			assertNotNull(((StoredSearchesException) e).get("RetryNumber-1"));
			assertNotNull(((StoredSearchesException) e).get("RetryNumber-2"));
			return;
		}
		fail();
	}

	@Test
	//@formatter:of
	public void updateCategory_dataValidAfterUpdateTest() {

		SearchInteractionsContext searchContext = new SearchInteractionsContext();
		searchContext.setTerms(new ArrayList<String>(Arrays.asList("yeah", "iphone")));

		Category category1 = (Category) new Category()
		                                                 .setDescription("Category1")
		                                                 .setName("Category1")
		                                                 .setSearchContext(searchContext)
		                                                 .setId(1)
														 .setLastChangeDateTimeGMT(DataUtils.getDateFromISO8601String("2015-12-05T13:11:59Z"));


		Category category2 = (Category) new Category().setDescription("Category2")
		                                              .setName("Category2")
		                                              .setSearchContext(searchContext)
		                                              .setId(2)
		                                              .setLastChangeDateTimeGMT(DataUtils.getDateFromISO8601String("2015-12-05T13:11:59Z"));

		Category category3 = (Category) new Category().setDescription("Category2")
		                                              .setName("Category2")
		                                              .setSearchContext(searchContext)
		                                              .setId(3)
		                                              .setLastChangeDateTimeGMT(DataUtils.getDateFromISO8601String("2015-12-05T13:11:59Z"));

		List<Category> categories = new ArrayList<>();
		categories.add(category1);
		categories.add(category2);
		categories.add(category3);


		Category categoryToUpdate = (Category) new Category().setDescription("UpdatedCategory")
		                                                .setName("UpdatedCategory")
		                                                .setSearchContext(searchContext)
														.setId(3)
														.setLastChangeDateTimeGMT(DataUtils.getDateFromISO8601String("2015-12-05T13:11:59Z"));
		categoryToUpdate.setPublished(true)
					   .setImpact(100)
					   .setColor(5)
					   .setLastModifiedByUserId(7000);

		DateTime dateTimeNow = DateTime.now(DateTimeZone.UTC);


		StoredSearchesMerger merger = new StoredSearchesMerger();
		List<Category> updatedCategories = merger.updateStoredSearch(categories, categoryToUpdate, dateTimeNow);

		Optional<Category> afterUpdate = updatedCategories.stream().filter(c -> c.getId() == categoryToUpdate.getId()).findFirst();

		assertTrue(afterUpdate.isPresent());
		Category categoryAfterUpdate = afterUpdate.get();

		assertEquals("UpdatedCategory", categoryAfterUpdate.getName());
		assertEquals("UpdatedCategory", categoryAfterUpdate.getDescription());
		assertEquals(true, categoryAfterUpdate.isPublished());
		assertEquals(100, categoryAfterUpdate.getImpact());
		assertEquals(5, categoryAfterUpdate.getColor());
		assertEquals(7000, categoryAfterUpdate.getLastModifiedByUserId());
	}
	//@formatter:on


	@Test
	//@formatter:of
	public void updateCategory_updatingNotExistingCategoryTest() {

		SearchInteractionsContext searchContext = new SearchInteractionsContext();
		searchContext.setTerms(new ArrayList<String>(Arrays.asList("yeah", "iphone")));

		Category category1 = (Category) new Category().setDescription("Category1")
		                                              .setName("Category1")
		                                              .setSearchContext(searchContext)
		                                              .setId(1)
		                                              .setLastChangeDateTimeGMT(DataUtils.getDateFromISO8601String("2015-12-05T13:11:59Z"));



		Category category2 = (Category) new Category().setDescription("Category2")
		                                              .setName("Category2")
		                                              .setSearchContext(searchContext)
		                                              .setId(2)
		                                              .setLastChangeDateTimeGMT(DataUtils.getDateFromISO8601String("2015-12-05T13:11:59Z"));

		Category category3 = (Category) new Category().setDescription("Category2")
		                                              .setName("Category2")
		                                              .setSearchContext(searchContext)
		                                              .setId(3)
		                                              .setLastChangeDateTimeGMT(DataUtils.getDateFromISO8601String("2015-12-05T13:11:59Z"));

		List<Category> categories = new ArrayList<>();
		categories.add(category1);
		categories.add(category2);
		categories.add(category3);


		Category categoryToUpdate = (Category) new Category().setDescription("UpdatedCategory")
		                                                     .setName("UpdatedCategory")
		                                                     .setSearchContext(searchContext)
		                                                     .setId(100)
		                                                     .setLastChangeDateTimeGMT(DataUtils.getDateFromISO8601String("2015-12-05T13:11:59Z"));
		categoryToUpdate.setPublished(true)
		                .setImpact(100)
		                .setColor(5)
		                .setLastModifiedByUserId(7000);

		try {
			DateTime dateTimeNow = DateTime.now(DateTimeZone.UTC);

			StoredSearchesMerger merger = new StoredSearchesMerger();
			List<Category> updatedCategories = merger.updateStoredSearch(categories, categoryToUpdate, dateTimeNow);
		}
		catch(Exception ex) {
			assertEquals(ex.getClass(), StoredSearchesException.class);
			StoredSearchesException sex = (StoredSearchesException) ex;
			assertEquals(sex.getAppExecutionErrorCode(), StoredSearchesErrorCode.CategoryUpdateNotFoundError);
		}
	}
	//@formatter:on


	@Test
	//@formatter:of
	public void updateCategory_updatingAllreadyModifiedCategoryTest() {

		SearchInteractionsContext searchContext = new SearchInteractionsContext();
		searchContext.setTerms(new ArrayList<String>(Arrays.asList("yeah", "iphone")));

		Category category1 = (Category) new Category()
				.setDescription("Category1")
				.setName("Category1")
				.setSearchContext(searchContext)
				.setId(1)
				.setLastChangeDateTimeGMT(DataUtils.getDateFromISO8601String("2015-12-05T13:11:59Z"));


		Category category2 = (Category) new Category().setDescription("Category2")
		                                              .setName("Category2")
		                                              .setSearchContext(searchContext)
		                                              .setId(2)
		                                              .setLastChangeDateTimeGMT(DataUtils.getDateFromISO8601String("2015-12-05T13:11:59Z"));

		DateTime category3DateTime = DataUtils.getDateFromISO8601String("2015-12-05T13:11:59Z");

		Category category3 = (Category) new Category().setDescription("Category2")
		                                              .setName("Category2")
		                                              .setSearchContext(searchContext)
		                                              .setId(3)
		                                              .setLastChangeDateTimeGMT(category3DateTime);

		List<Category> categories = new ArrayList<>();
		categories.add(category1);
		categories.add(category2);
		categories.add(category3);


		Category categoryToUpdate = (Category) new Category().setDescription("UpdatedCategory")
		                                                     .setName("UpdatedCategory")
		                                                     .setSearchContext(searchContext)
		                                                     .setId(3)
		                                                     .setLastChangeDateTimeGMT(category3DateTime.minusMinutes(10));
		categoryToUpdate.setPublished(true)
		                .setImpact(100)
		                .setColor(5)
		                .setLastModifiedByUserId(7000);


		try {
			DateTime dateTimeNow = DateTime.now(DateTimeZone.UTC);

			StoredSearchesMerger merger = new StoredSearchesMerger();
			List<Category> updatedCategories = merger.updateStoredSearch(categories, categoryToUpdate, dateTimeNow);
		}
		catch(Exception ex) {
			assertEquals(ex.getClass(), StoredSearchesException.class);
			StoredSearchesException sex = (StoredSearchesException) ex;

			assertEquals(sex.getAppExecutionErrorCode(), StoredSearchesErrorCode.CategoryUpdateNotLatestVersionError);
		}
	}
	//@formatter:on

	@Test
	public void categoryReprocessing_isReprocessingAllowedLastReprocessedDateTimeIsNull() {
		val categoryReprocessingService = new CategoriesReprocessingService();
		categoryReprocessingService.setConfigurationManager(this.configurationManagerMock);

		Category category = new Category();
		category.setId(0);
		category.setName("Category 1");
		category.setActive(true);
		category.setReprocessingState(new CategoryReprocessingState().setStatus(CategoryReprocessingStatus.Reprocessing));

		boolean isReprocessingAllowed = categoryReprocessingService.isCategoryReprocessingAllowed(category);

		assertFalse("Is Category which is being reprocessed allowed for reprocessing", isReprocessingAllowed);
	}

	@Test
	public void categoryReprocessing_isReprocessingAllowedForInactive() {
		val categoryReprocessingService = new CategoriesReprocessingService();
		categoryReprocessingService.setConfigurationManager(this.configurationManagerMock);

		Category category = new Category();
		category.setId(0);
		category.setName("Category 1");
		category.setActive(false);
		category.setReprocessingState(null);

		boolean isReprocessingAllowed = categoryReprocessingService.isCategoryReprocessingAllowed(category);

		assertFalse("Inactive Category not allowed for reprocessing", isReprocessingAllowed);
	}

	@Test
	public void categoryReprocessing_isReprocessingAllowedWhenNoReprocessingStatusSpecified() {
		val categoryReprocessingService = new CategoriesReprocessingService();
		categoryReprocessingService.setConfigurationManager(this.configurationManagerMock);

		Category category = new Category();
		category.setId(0);
		category.setName("Category 1");
		category.setActive(true);
		category.setReprocessingState(null);

		boolean isReprocessingAllowed = categoryReprocessingService.isCategoryReprocessingAllowed(category);

		assertTrue("Category with has never been reprocessed before is allowed for reprocessing", isReprocessingAllowed);
	}


	@Test
	public void categoryReprocessing_isReprocessingAllowedWhenReprocessed() {
		val categoryReprocessingService = new CategoriesReprocessingService();
		categoryReprocessingService.setConfigurationManager(this.configurationManagerMock);

		DateTime categoryLastReprocessed = DataUtils.getDateFromISO8601String("2015-12-20T13:00:00Z");

		Category category = new Category();
		category.setId(0);
		category.setName("Category 1");
		category.setActive(true);
		category.setReprocessingState(new CategoryReprocessingState().setStatus(CategoryReprocessingStatus.Error));

		boolean isReprocessingAllowed = categoryReprocessingService.isCategoryReprocessingAllowed(category);

		assertTrue("Category with Error status should be allowed for reprocessing", isReprocessingAllowed);
	}


	@Test
	public void categoryReprocessing_shouldReprocessWhenErrored() {
		val categoryReprocessingService = new CategoriesReprocessingService();

		val categoriesService = new StoredSearchService(null);
		categoriesService.setCategoryReprocessingService(categoryReprocessingService);

		categoryReprocessingService.setConfigurationManager(this.configurationManagerMock);

		Category category = new Category();
		category.setId(0);
		category.setName("Category 1");
		category.setActive(true);

		//@formatter:off
		category.setReprocessingState(new CategoryReprocessingState()
				                              .setStatus(CategoryReprocessingStatus.Error)
				                              .setLastReprocessedTime(DataUtils.getDateFromISO8601String("2015-12-20T13:00:00Z"))
				                              .setLastErrorTime(DataUtils.getDateFromISO8601String("2015-12-20T13:10:00Z"))
		);
		//@formatter:on

		CategoriesRepository repo = new CategoriesRepository();
		repo.setCategories(Arrays.asList(category));

		categoriesService.processCategoriesReprocessingStatus(repo);

		assertTrue("Category should be recommended to reprocess due to error", category.getShouldBeReprocessed());
	}

	@Test
	public void categoryReprocessing_shouldNotRecommendReprocess() {
		val categoryReprocessingService = new CategoriesReprocessingService();

		val categoriesService = new StoredSearchService(null);
		categoriesService.setCategoryReprocessingService(categoryReprocessingService);

		categoryReprocessingService.setConfigurationManager(this.configurationManagerMock);

		Category category = new Category();
		category.setId(0);
		category.setName("Category 1");
		category.setActive(true);

		//@formatter:off
		category.setReprocessingState(new CategoryReprocessingState()
				                              .setStatus(CategoryReprocessingStatus.Error)
				                              .setLastReprocessedTime(DataUtils.getDateFromISO8601String("2015-12-20T13:00:00Z"))
				                              .setLastErrorTime(DataUtils.getDateFromISO8601String("2015-12-20T12:55:00Z"))
		);
		//@formatter:on

		CategoriesRepository repo = new CategoriesRepository();
		repo.setCategories(Arrays.asList(category));

		categoriesService.processCategoriesReprocessingStatus(repo);

		assertFalse("Category should not be recommended to reprocess", category.getShouldBeReprocessed());
	}

	/*
	@Test
	public void getCategoriesCorrelatedFacetWithMetrics_simple () {
		
		Facet res = storedSearchService.getCategoriesCorrelatedFacetWithMetrics(null, null, contextSearch, bgSearch, null);
		
		assertTrue(res.getValues().get(0).getPercentage() == 0.5);
		assertTrue(res.getValues().get(1).getPercentage() == 0.5);
		assertTrue(res.getValues().get(2).getPercentage() == 0.5);
	}
	*/
	
	/*
	private void fillCorrelatedData (Facet contextSearchFacet, Facet bgSearchFacet) {

		FacetResultGroup item;
		contextSearchFacet.setValues(new ArrayList<FacetResultGroup>());
		
		item = new FacetResultGroup();
		item.setTitle("item1");
		item.setCount(10);
		contextSearchFacet.getValues().add(item);

		item = new FacetResultGroup();
		item.setTitle("item2");
		item.setCount(15);
		contextSearchFacet.getValues().add(item);

		item = new FacetResultGroup();
		item.setTitle("item3");
		item.setCount(20);
		contextSearchFacet.getValues().add(item);
		
		
		bgSearchFacet.setValues(new ArrayList<FacetResultGroup>());
		
		item = new FacetResultGroup();
		item.setTitle("item1");
		item.setCount(20);
		bgSearchFacet.getValues().add(item);

		item = new FacetResultGroup();
		item.setTitle("item2");
		item.setCount(30);
		bgSearchFacet.getValues().add(item);

		item = new FacetResultGroup();
		item.setTitle("item3");
		item.setCount(40);
		bgSearchFacet.getValues().add(item);
	}
	*/
}