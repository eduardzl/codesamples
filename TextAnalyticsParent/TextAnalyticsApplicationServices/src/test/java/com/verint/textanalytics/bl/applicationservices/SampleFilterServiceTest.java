package com.verint.textanalytics.bl.applicationservices;

import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.model.interactions.FilterField;
import com.verint.textanalytics.model.interactions.FilterFieldValue;
import com.verint.textanalytics.model.interactions.RangeFilterField;
import com.verint.textanalytics.model.interactions.SearchInteractionsContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.Assert.assertTrue;

/**
 * Created by TBaum on 7/12/2016.
 */
public class SampleFilterServiceTest extends BaseTest {

	@Mock
	private ConfigurationManager configurationManagerMock;

	@Mock
	protected ApplicationConfiguration appConfigMock;


	private SampleFilterService sampleFilterService;

	@Before
	public void setUp() throws Exception {

		MockitoAnnotations.initMocks(this);

		Mockito.when(configurationManagerMock.getApplicationConfiguration()).thenReturn(appConfigMock);

		Mockito.when(appConfigMock.getSampleSizeThreshold()).thenReturn(1000000);

		sampleFilterService = new SampleFilterService();
		sampleFilterService.setConfigurationManager(configurationManagerMock);

	}

	@Test
	public void sampleFilter_empty_context_noFilterToAdd() {
		SearchInteractionsContext searchContext = new SearchInteractionsContext();

		boolean result = sampleFilterService.addSampleFilter(searchContext, 100000);
		assertTrue(result == false);
		assertTrue(searchContext.getFilterFields() == null);
	}

	@Test
	public void sampleFilter_empty_context_addedFilters() {
		SearchInteractionsContext searchContext = new SearchInteractionsContext();


		boolean result = sampleFilterService.addSampleFilter(searchContext, 2000000);
		assertTrue(result == true);
		assertTrue(searchContext.getFilterFields() != null);
	}

	@Test
	public void sampleFilter_smallInteractionNumber_1_Test() {
		SearchInteractionsContext searchContext = new SearchInteractionsContext(new ArrayList<String>(), new ArrayList<FilterField>(), new ArrayList<RangeFilterField>());


		boolean result = sampleFilterService.addSampleFilter(searchContext, 100000);
		assertTrue(result == false);
		assertTrue(searchContext.getFilterFields().size() == 0);
	}

	@Test
	public void sampleFilter_smallInteractionNumber_2_Test() {
		SearchInteractionsContext searchContext = new SearchInteractionsContext(new ArrayList<String>(), new ArrayList<FilterField>(), new ArrayList<RangeFilterField>());


		boolean result = sampleFilterService.addSampleFilter(searchContext, 999999);
		assertTrue(result == false);
		assertTrue(searchContext.getFilterFields().size() == 0);
	}

	@Test
	public void sampleFilter_InteractionNumber_2000000_Test() {
		SearchInteractionsContext searchContext = new SearchInteractionsContext(new ArrayList<String>(), new ArrayList<FilterField>(), new ArrayList<RangeFilterField>());


		boolean result = sampleFilterService.addSampleFilter(searchContext, 2000000);
		assertTrue(result == true);

		List<FilterField> filters = searchContext.getFilterFields();
		assertTrue(filters.size() == 1);

		FilterFieldValue[] filterValues = filters.get(0).getValues();
		assertTrue(filterValues.length == 8);

		assertTrue(filterValues[0].getValue().equals("0*"));
		assertTrue(filterValues[1].getValue().equals("1*"));
		assertTrue(filterValues[2].getValue().equals("2*"));
		assertTrue(filterValues[3].getValue().equals("3*"));
		assertTrue(filterValues[4].getValue().equals("4*"));
		assertTrue(filterValues[5].getValue().equals("5*"));
		assertTrue(filterValues[6].getValue().equals("6*"));
		assertTrue(filterValues[7].getValue().equals("7*"));

	}


	@Test
	public void sampleFilter_InteractionNumber_10000000_Test() {
		SearchInteractionsContext searchContext = new SearchInteractionsContext(new ArrayList<String>(), new ArrayList<FilterField>(), new ArrayList<RangeFilterField>());


		boolean result = sampleFilterService.addSampleFilter(searchContext, 10000000);
		assertTrue(result == true);

		List<FilterField> filters = searchContext.getFilterFields();
		assertTrue(filters.size() == 1);

		FilterFieldValue[] filterValues = filters.get(0).getValues();
		assertTrue(filterValues.length == 2);

		assertTrue(filterValues[0].getValue().equals("0*"));
		assertTrue(filterValues[1].getValue().equals("1*"));


	}


	@Test
	public void sampleFilter_InteractionNumber_20000000_Test() {
		SearchInteractionsContext searchContext = new SearchInteractionsContext(new ArrayList<String>(), new ArrayList<FilterField>(), new ArrayList<RangeFilterField>());


		boolean result = sampleFilterService.addSampleFilter(searchContext, 20000000);
		assertTrue(result == true);

		List<FilterField> filters = searchContext.getFilterFields();
		assertTrue(filters.size() == 1);

		FilterFieldValue[] filterValues = filters.get(0).getValues();
		assertTrue(filterValues.length == 1);

		assertTrue(filterValues[0].getValue().equals("0*"));



	}

	@Test
	public void sampleFilter_InteractionNumber_30000000_Test() {
		SearchInteractionsContext searchContext = new SearchInteractionsContext(new ArrayList<String>(), new ArrayList<FilterField>(), new ArrayList<RangeFilterField>());

		boolean result = sampleFilterService.addSampleFilter(searchContext, 30000000);
		assertTrue(result == true);

		List<FilterField> filters = searchContext.getFilterFields();
		assertTrue(filters.size() == 1);

		FilterFieldValue[] filterValues = filters.get(0).getValues();
		assertTrue(filterValues.length == 1);

		assertTrue(filterValues[0].getValue().equals("0*"));

	}
}
