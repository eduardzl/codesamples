package com.verint.textanalytics.dal.configService;

import com.verint.textanalytics.dal.darwin.BaseTest;
import com.verint.textanalytics.model.facets.SpeakerQueryType;
import com.verint.textanalytics.model.storedSearch.Category;
import com.verint.textanalytics.model.storedSearch.SavedSearch;
import lombok.val;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/***
 * @author imor
 */
public class ConfigServiceResponseConvertorTest extends BaseTest {

	private static final String ABOUT = "about";
	private static final String CATEGORY_TO_LOCATED_ABANDONING_CUSTOMERS = "Category to located abandoning customers ";
	private static final String ABANDONING_CUSTOMERS = "Abandoning customers ";
	protected static String s_CategoriesPropFileResourcePath;
	protected static String s_SavedSearchesPropFileResourcePath;
	protected static String s_WrongCategoriesPropFileResourcePath;
	protected static String s_WrongSavedSearchesPropFileResourcePath;

	public ConfigServiceResponseConvertorTest() throws Exception {
		s_CategoriesPropFileResourcePath = "CategoriesPropFile.txt";
		s_SavedSearchesPropFileResourcePath = "SavedSearchesPropFile.txt";
		s_WrongCategoriesPropFileResourcePath = "WrongCategoriesPropFile.txt";
		s_WrongSavedSearchesPropFileResourcePath = "WrongSavedSearchesPropFile.txt";
	}

	@Test
	public void convertCategoriesResponse_regularFlow() throws IOException {

		val response = this.getResourceAsString(s_CategoriesPropFileResourcePath);

		val configServiceResponseConvertor = new ConfigServiceResponseConvertor();

		val result = configServiceResponseConvertor.convertCategoriesResponse(response);

		assertEquals(2, result.getMaxId());
		assertEquals(3, result.getCategories().size());

		int i = 0;
		assertEquals(i, result.getCategories().get(i).getId());
		assertEquals(ABANDONING_CUSTOMERS + i, result.getCategories().get(i).getName());
		assertEquals(CATEGORY_TO_LOCATED_ABANDONING_CUSTOMERS + i, ((Category) result.getCategories().get(i)).getDescription());
		assertEquals(300 + i, ((Category) result.getCategories().get(i)).getLastModifiedByUserId());
		assertEquals(true, ((Category) result.getCategories().get(i)).isActive());
		assertEquals(true, ((Category) result.getCategories().get(i)).isPublished());
		assertEquals(i, ((Category) result.getCategories().get(i)).getColor());
		assertEquals(4000 + i, ((Category) result.getCategories().get(i)).getImpact());

		assertEquals(1, ((Category) result.getCategories().get(i)).getSearchContext().getTerms().size());
		assertEquals(ABOUT + i, ((Category) result.getCategories().get(i)).getSearchContext().getTerms().get(0));

		assertEquals(5, ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().size());
		assertEquals("topics_f", ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().get(0).getName());
		assertEquals(SpeakerQueryType.Any, ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().get(0).getSpeaker());
		assertEquals(2, ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().get(0).getValues().length);
		assertEquals("1/Fee", ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().get(0).getValueAt(0).getValue());
		assertEquals("1/Service", ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().get(0).getValueAt(1).getValue());

		assertEquals(1, ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().size());
		assertEquals("date", ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().get(0).getName());
		assertEquals("NOW-1YEAR/DAY", ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getLowerValue());
		assertEquals("NOW/DAY+1DAY", ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getUpperValue());
		assertEquals("true", ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getIsLowerInclusive());
		assertEquals("true", ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getIsUpperInclusive());

		i = 1;
		assertEquals(i, result.getCategories().get(i).getId());
		assertEquals(ABANDONING_CUSTOMERS + i, result.getCategories().get(i).getName());
		assertEquals(CATEGORY_TO_LOCATED_ABANDONING_CUSTOMERS + i, ((Category) result.getCategories().get(i)).getDescription());
		assertEquals(300 + i, ((Category) result.getCategories().get(i)).getLastModifiedByUserId());
		assertEquals(true, ((Category) result.getCategories().get(i)).isActive());
		assertEquals(true, ((Category) result.getCategories().get(i)).isPublished());
		assertEquals(i, ((Category) result.getCategories().get(i)).getColor());
		assertEquals(4000 + i, ((Category) result.getCategories().get(i)).getImpact());

		assertEquals(1, ((Category) result.getCategories().get(i)).getSearchContext().getTerms().size());
		assertEquals(ABOUT + i, ((Category) result.getCategories().get(i)).getSearchContext().getTerms().get(0));

		assertEquals(5, ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().size());
		assertEquals("topics_f", ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().get(0).getName());
		assertEquals(SpeakerQueryType.Any, ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().get(0).getSpeaker());
		assertEquals(2, ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().get(0).getValues().length);
		assertEquals("1/Fee", ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().get(0).getValueAt(0).getValue());
		assertEquals("1/Service", ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().get(0).getValueAt(1).getValue());

		assertEquals(1, ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().size());
		assertEquals("date", ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().get(0).getName());
		assertEquals("NOW-1YEAR/DAY", ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getLowerValue());
		assertEquals("NOW/DAY+1DAY", ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getUpperValue());
		assertEquals("true", ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getIsLowerInclusive());
		assertEquals("true", ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getIsUpperInclusive());

		i = 2;
		assertEquals(i, result.getCategories().get(i).getId());
		assertEquals(ABANDONING_CUSTOMERS + i, result.getCategories().get(i).getName());
		assertEquals(CATEGORY_TO_LOCATED_ABANDONING_CUSTOMERS + i, ((Category) result.getCategories().get(i)).getDescription());
		assertEquals(300 + i, ((Category) result.getCategories().get(i)).getLastModifiedByUserId());
		assertEquals(true, ((Category) result.getCategories().get(i)).isActive());
		assertEquals(true, ((Category) result.getCategories().get(i)).isPublished());
		assertEquals(i, ((Category) result.getCategories().get(i)).getColor());
		assertEquals(4000 + i, ((Category) result.getCategories().get(i)).getImpact());

		assertEquals(1, ((Category) result.getCategories().get(i)).getSearchContext().getTerms().size());
		assertEquals(ABOUT + i, ((Category) result.getCategories().get(i)).getSearchContext().getTerms().get(0));

		assertEquals(5, ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().size());
		assertEquals("topics_f", ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().get(0).getName());
		assertEquals(SpeakerQueryType.Any, ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().get(0).getSpeaker());
		assertEquals(2, ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().get(0).getValues().length);
		assertEquals("1/Fee", ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().get(0).getValueAt(0).getValue());
		assertEquals("1/Service", ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().get(0).getValueAt(1).getValue());

		assertEquals(1, ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().size());
		assertEquals("date", ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().get(0).getName());
		assertEquals("NOW-1YEAR/DAY", ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getLowerValue());
		assertEquals("NOW/DAY+1DAY", ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getUpperValue());
		assertEquals("true", ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getIsLowerInclusive());
		assertEquals("true", ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getIsUpperInclusive());

	}

	@Test
	public void convertCategoriesResponse_ExceptionFlow() throws IOException {

		val response = this.getResourceAsString(s_WrongCategoriesPropFileResourcePath);

		val configServiceResponseConvertor = new ConfigServiceResponseConvertor();

		val result = configServiceResponseConvertor.convertCategoriesResponse(response);

		assertEquals(2, result.getMaxId());
		assertEquals(3, result.getCategories().size());
		assertEquals(null, result.getCanNotParseCategories());
	}

	@Test
	public void converCategoriesToJSONString_regularFlow() throws IOException {

		val response = this.getResourceAsString(s_CategoriesPropFileResourcePath);

		val configServiceResponseConvertor = new ConfigServiceResponseConvertor();

		val convertCategoriesResponseResult = configServiceResponseConvertor.convertCategoriesResponse(response);

		val converCategoriesToJSONStringResult = configServiceResponseConvertor.convertCategoriesToJSONString(convertCategoriesResponseResult);

		val result = configServiceResponseConvertor.convertCategoriesResponse(converCategoriesToJSONStringResult);

		assertEquals(2, result.getMaxId());
		assertEquals(3, result.getCategories().size());

		int i = 0;
		assertEquals(i, result.getCategories().get(i).getId());
		assertEquals(ABANDONING_CUSTOMERS + i, result.getCategories().get(i).getName());
		assertEquals(CATEGORY_TO_LOCATED_ABANDONING_CUSTOMERS + i, ((Category) result.getCategories().get(i)).getDescription());
		assertEquals(300 + i, ((Category) result.getCategories().get(i)).getLastModifiedByUserId());
		assertEquals(true, ((Category) result.getCategories().get(i)).isActive());
		assertEquals(true, ((Category) result.getCategories().get(i)).isPublished());
		assertEquals(i, ((Category) result.getCategories().get(i)).getColor());
		assertEquals(4000 + i, ((Category) result.getCategories().get(i)).getImpact());

		assertEquals(1, ((Category) result.getCategories().get(i)).getSearchContext().getTerms().size());
		assertEquals(ABOUT + i, ((Category) result.getCategories().get(i)).getSearchContext().getTerms().get(0));

		assertEquals(5, ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().size());
		assertEquals("topics_f", ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().get(0).getName());
		assertEquals(SpeakerQueryType.Any, ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().get(0).getSpeaker());
		assertEquals(2, ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().get(0).getValues().length);
		assertEquals("1/Fee", ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().get(0).getValueAt(0).getValue());
		assertEquals("1/Service", ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().get(0).getValueAt(1).getValue());

		assertEquals(1, ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().size());
		assertEquals("date", ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().get(0).getName());
		assertEquals("NOW-1YEAR/DAY", ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getLowerValue());
		assertEquals("NOW/DAY+1DAY", ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getUpperValue());
		assertEquals("true", ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getIsLowerInclusive());
		assertEquals("true", ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getIsUpperInclusive());

		i = 1;
		assertEquals(i, result.getCategories().get(i).getId());
		assertEquals(ABANDONING_CUSTOMERS + i, result.getCategories().get(i).getName());
		assertEquals(CATEGORY_TO_LOCATED_ABANDONING_CUSTOMERS + i, ((Category) result.getCategories().get(i)).getDescription());
		assertEquals(300 + i, ((Category) result.getCategories().get(i)).getLastModifiedByUserId());
		assertEquals(true, ((Category) result.getCategories().get(i)).isActive());
		assertEquals(true, ((Category) result.getCategories().get(i)).isPublished());
		assertEquals(i, ((Category) result.getCategories().get(i)).getColor());
		assertEquals(4000 + i, ((Category) result.getCategories().get(i)).getImpact());

		assertEquals(1, ((Category) result.getCategories().get(i)).getSearchContext().getTerms().size());
		assertEquals(ABOUT + i, ((Category) result.getCategories().get(i)).getSearchContext().getTerms().get(0));

		assertEquals(5, ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().size());
		assertEquals("topics_f", ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().get(0).getName());
		assertEquals(SpeakerQueryType.Any, ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().get(0).getSpeaker());
		assertEquals(2, ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().get(0).getValues().length);
		assertEquals("1/Fee", ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().get(0).getValueAt(0).getValue());
		assertEquals("1/Service", ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().get(0).getValueAt(1).getValue());

		assertEquals(1, ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().size());
		assertEquals("date", ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().get(0).getName());
		assertEquals("NOW-1YEAR/DAY", ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getLowerValue());
		assertEquals("NOW/DAY+1DAY", ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getUpperValue());
		assertEquals("true", ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getIsLowerInclusive());
		assertEquals("true", ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getIsUpperInclusive());

		i = 2;
		assertEquals(i, result.getCategories().get(i).getId());
		assertEquals(ABANDONING_CUSTOMERS + i, result.getCategories().get(i).getName());
		assertEquals(CATEGORY_TO_LOCATED_ABANDONING_CUSTOMERS + i, ((Category) result.getCategories().get(i)).getDescription());
		assertEquals(300 + i, ((Category) result.getCategories().get(i)).getLastModifiedByUserId());
		assertEquals(true, ((Category) result.getCategories().get(i)).isActive());
		assertEquals(true, ((Category) result.getCategories().get(i)).isPublished());
		assertEquals(i, ((Category) result.getCategories().get(i)).getColor());
		assertEquals(4000 + i, ((Category) result.getCategories().get(i)).getImpact());

		assertEquals(1, ((Category) result.getCategories().get(i)).getSearchContext().getTerms().size());
		assertEquals(ABOUT + i, ((Category) result.getCategories().get(i)).getSearchContext().getTerms().get(0));

		assertEquals(5, ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().size());
		assertEquals("topics_f", ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().get(0).getName());
		assertEquals(SpeakerQueryType.Any, ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().get(0).getSpeaker());
		assertEquals(2, ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().get(0).getValues().length);
		assertEquals("1/Fee", ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().get(0).getValueAt(0).getValue());
		assertEquals("1/Service", ((Category) result.getCategories().get(i)).getSearchContext().getFilterFields().get(0).getValueAt(1).getValue());

		assertEquals(1, ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().size());
		assertEquals("date", ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().get(0).getName());
		assertEquals("NOW-1YEAR/DAY", ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getLowerValue());
		assertEquals("NOW/DAY+1DAY", ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getUpperValue());
		assertEquals("true", ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getIsLowerInclusive());
		assertEquals("true", ((Category) result.getCategories().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getIsUpperInclusive());

	}

	@Test
	public void converSavedSearchesResponse_regularFlow() throws IOException {

		val response = this.getResourceAsString(s_SavedSearchesPropFileResourcePath);

		val configServiceResponseConvertor = new ConfigServiceResponseConvertor();

		val result = configServiceResponseConvertor.convertSavedSearchesResponse(response);

		assertEquals(2, result.getMaxId());
		assertEquals(3, result.getSavedSearches().size());

		int i = 0;
		assertEquals(i, result.getSavedSearches().get(i).getId());
		assertEquals(i, result.getSavedSearches().get(i).getId());
		assertEquals(ABANDONING_CUSTOMERS + i, result.getSavedSearches().get(i).getName());
		assertEquals(300 + i, ((SavedSearch) result.getSavedSearches().get(i)).getLastModifiedByUserId());

		assertEquals(1, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getTerms().size());
		assertEquals(ABOUT + i, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getTerms().get(0));

		assertEquals(5, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().size());
		assertEquals("topics_f", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().get(0).getName());
		assertEquals(SpeakerQueryType.Any, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().get(0).getSpeaker());
		assertEquals(2, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().get(0).getValues().length);
		assertEquals("1/Fee", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().get(0).getValueAt(0).getValue());
		assertEquals("1/Service", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().get(0).getValueAt(1).getValue());

		assertEquals(1, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().size());
		assertEquals("date", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().get(0).getName());
		assertEquals("NOW-1YEAR/DAY", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getLowerValue());
		assertEquals("NOW/DAY+1DAY", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getUpperValue());
		assertEquals("true", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getIsLowerInclusive());
		assertEquals("true", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getIsUpperInclusive());

		i = 1;
		assertEquals(i, result.getSavedSearches().get(i).getId());
		assertEquals(i, result.getSavedSearches().get(i).getId());
		assertEquals(ABANDONING_CUSTOMERS + i, result.getSavedSearches().get(i).getName());
		assertEquals(300 + i, ((SavedSearch) result.getSavedSearches().get(i)).getLastModifiedByUserId());

		assertEquals(1, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getTerms().size());
		assertEquals(ABOUT + i, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getTerms().get(0));

		assertEquals(5, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().size());
		assertEquals("topics_f", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().get(0).getName());
		assertEquals(SpeakerQueryType.Any, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().get(0).getSpeaker());
		assertEquals(2, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().get(0).getValues().length);
		assertEquals("1/Fee", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().get(0).getValueAt(0).getValue());
		assertEquals("1/Service", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().get(0).getValueAt(1).getValue());

		assertEquals(1, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().size());
		assertEquals("date", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().get(0).getName());
		assertEquals("NOW-1YEAR/DAY", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getLowerValue());
		assertEquals("NOW/DAY+1DAY", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getUpperValue());
		assertEquals("true", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getIsLowerInclusive());
		assertEquals("true", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getIsUpperInclusive());

		i = 2;
		assertEquals(i, result.getSavedSearches().get(i).getId());
		assertEquals(i, result.getSavedSearches().get(i).getId());
		assertEquals(ABANDONING_CUSTOMERS + i, result.getSavedSearches().get(i).getName());
		assertEquals(300 + i, ((SavedSearch) result.getSavedSearches().get(i)).getLastModifiedByUserId());

		assertEquals(1, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getTerms().size());
		assertEquals(ABOUT + i, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getTerms().get(0));

		assertEquals(5, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().size());
		assertEquals("topics_f", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().get(0).getName());
		assertEquals(SpeakerQueryType.Any, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().get(0).getSpeaker());
		assertEquals(2, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().get(0).getValues().length);
		assertEquals("1/Fee", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().get(0).getValueAt(0).getValue());
		assertEquals("1/Service", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().get(0).getValueAt(1).getValue());

		assertEquals(1, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().size());
		assertEquals("date", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().get(0).getName());
		assertEquals("NOW-1YEAR/DAY", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getLowerValue());
		assertEquals("NOW/DAY+1DAY", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getUpperValue());
		assertEquals("true", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getIsLowerInclusive());
		assertEquals("true", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getIsUpperInclusive());

	}

	@Test
	public void converSavedSearchesResponse_ExceptionFlow() throws IOException {

		val response = this.getResourceAsString(s_WrongSavedSearchesPropFileResourcePath);

		val configServiceResponseConvertor = new ConfigServiceResponseConvertor();

		val result = configServiceResponseConvertor.convertSavedSearchesResponse(response);

		assertEquals(2, result.getMaxId());
		assertEquals(3, result.getSavedSearches().size());
		assertEquals(null, result.getCanNotParseSavedSearches());
	}

	@Test
	public void converSavedSearchesToJSONString_regularFlow() throws IOException {

		val response = this.getResourceAsString(s_SavedSearchesPropFileResourcePath);

		val configServiceResponseConvertor = new ConfigServiceResponseConvertor();

		val converSavedSearchesResponseResult = configServiceResponseConvertor.convertSavedSearchesResponse(response);

		val converSavedSearchesToJSONStringResult = configServiceResponseConvertor.convertSavedSearchesToJSONString(converSavedSearchesResponseResult);

		val result = configServiceResponseConvertor.convertSavedSearchesResponse(converSavedSearchesToJSONStringResult);

		assertEquals(2, result.getMaxId());
		assertEquals(3, result.getSavedSearches().size());

		int i = 0;
		assertEquals(i, result.getSavedSearches().get(i).getId());
		assertEquals(i, result.getSavedSearches().get(i).getId());
		assertEquals(ABANDONING_CUSTOMERS + i, result.getSavedSearches().get(i).getName());
		assertEquals(300 + i, ((SavedSearch) result.getSavedSearches().get(i)).getLastModifiedByUserId());

		assertEquals(1, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getTerms().size());
		assertEquals(ABOUT + i, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getTerms().get(0));

		assertEquals(5, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().size());
		assertEquals("topics_f", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().get(0).getName());
		assertEquals(SpeakerQueryType.Any, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().get(0).getSpeaker());
		assertEquals(2, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().get(0).getValues().length);
		assertEquals("1/Fee", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().get(0).getValueAt(0).getValue());
		assertEquals("1/Service", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().get(0).getValueAt(1).getValue());

		assertEquals(1, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().size());
		assertEquals("date", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().get(0).getName());
		assertEquals("NOW-1YEAR/DAY", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getLowerValue());
		assertEquals("NOW/DAY+1DAY", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getUpperValue());
		assertEquals("true", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getIsLowerInclusive());
		assertEquals("true", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getIsUpperInclusive());

		i = 1;
		assertEquals(i, result.getSavedSearches().get(i).getId());
		assertEquals(i, result.getSavedSearches().get(i).getId());
		assertEquals(ABANDONING_CUSTOMERS + i, result.getSavedSearches().get(i).getName());
		assertEquals(300 + i, ((SavedSearch) result.getSavedSearches().get(i)).getLastModifiedByUserId());

		assertEquals(1, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getTerms().size());
		assertEquals(ABOUT + i, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getTerms().get(0));

		assertEquals(5, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().size());
		assertEquals("topics_f", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().get(0).getName());
		assertEquals(SpeakerQueryType.Any, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().get(0).getSpeaker());
		assertEquals(2, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().get(0).getValues().length);
		assertEquals("1/Fee", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().get(0).getValueAt(0).getValue());
		assertEquals("1/Service", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().get(0).getValueAt(1).getValue());

		assertEquals(1, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().size());
		assertEquals("date", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().get(0).getName());
		assertEquals("NOW-1YEAR/DAY", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getLowerValue());
		assertEquals("NOW/DAY+1DAY", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getUpperValue());
		assertEquals("true", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getIsLowerInclusive());
		assertEquals("true", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getIsUpperInclusive());

		i = 2;
		assertEquals(i, result.getSavedSearches().get(i).getId());
		assertEquals(i, result.getSavedSearches().get(i).getId());
		assertEquals(ABANDONING_CUSTOMERS + i, result.getSavedSearches().get(i).getName());
		assertEquals(300 + i, ((SavedSearch) result.getSavedSearches().get(i)).getLastModifiedByUserId());

		assertEquals(1, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getTerms().size());
		assertEquals(ABOUT + i, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getTerms().get(0));

		assertEquals(5, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().size());
		assertEquals("topics_f", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().get(0).getName());
		assertEquals(SpeakerQueryType.Any, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().get(0).getSpeaker());
		assertEquals(2, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().get(0).getValues().length);
		assertEquals("1/Fee", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().get(0).getValueAt(0).getValue());
		assertEquals("1/Service", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getFilterFields().get(0).getValueAt(1).getValue());

		assertEquals(1, ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().size());
		assertEquals("date", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().get(0).getName());
		assertEquals("NOW-1YEAR/DAY", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getLowerValue());
		assertEquals("NOW/DAY+1DAY", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getUpperValue());
		assertEquals("true", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getIsLowerInclusive());
		assertEquals("true", ((SavedSearch) result.getSavedSearches().get(i)).getSearchContext().getRangeFilterFields().get(0).getRanges().get(0).getIsUpperInclusive());

	}
}
