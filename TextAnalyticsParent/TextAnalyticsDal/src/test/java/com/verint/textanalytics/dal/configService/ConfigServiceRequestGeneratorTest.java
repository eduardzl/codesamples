package com.verint.textanalytics.dal.configService;

import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.dal.darwin.RestRequestPathsAndQueryParams;
import com.verint.textanalytics.dal.rest.RestDataAccess;
import lombok.val;
import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.text.ParseException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ConfigServiceRequestGeneratorTest {

	private ConfigServiceRequestGenerator requestGenerator;

	protected String tenant = "tenant";
	protected String channel = "channel";
	protected String baseUrl = "http://10.165.140.102:8080";

	@Mock
	protected ConfigurationManager configurationManagerMock;

	@Mock
	protected ApplicationConfiguration appConfigMock;

	public ConfigServiceRequestGeneratorTest() {
		requestGenerator = new ConfigServiceRequestGenerator();

		MockitoAnnotations.initMocks(this);

		Mockito.when(appConfigMock.getConfigServiceFolder()).thenReturn("TextAnalyticsConfigServiceAPI");
		Mockito.when(appConfigMock.getCategoryFile()).thenReturn("categories.prop");
		Mockito.when(appConfigMock.getSavedSearchesFile()).thenReturn("savedsearches.prop");
		Mockito.when(appConfigMock.getTimeStempFiled()).thenReturn("lastUpdate");
		Mockito.when(appConfigMock.getHttpClientMaxConnectionsTotal()).thenReturn(100);
		Mockito.when(appConfigMock.getHttpClientMaxConnectionsPerRoute()).thenReturn(100);

		Mockito.when(configurationManagerMock.getApplicationConfiguration()).thenReturn(appConfigMock);

		requestGenerator.setConfigurationManager(configurationManagerMock);
	}

	@Test
	public void getGetCategoriesQuaryTest() {

		RestRequestPathsAndQueryParams categoriesQuery = requestGenerator.getGetCategoriesQuery(tenant, channel);

		assertNotNull(categoriesQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, categoriesQuery.getQueryPaths(), categoriesQuery.getQueryParams(), false);

		assertEquals("http://10.165.140.102:8080/TextAnalyticsConfigServiceAPI/rest/channel/tenant/channel/categories.prop", requestUrl);
	}

	@Test
	public void getGetSavedSearchesQuaryTest() {

		RestRequestPathsAndQueryParams categoriesQuery = requestGenerator.getGetSavedSearchesQuery(tenant, channel);

		assertNotNull(categoriesQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, categoriesQuery.getQueryPaths(), categoriesQuery.getQueryParams(), false);

		assertEquals("http://10.165.140.102:8080/TextAnalyticsConfigServiceAPI/rest/channel/tenant/channel/savedsearches.prop", requestUrl);
	}

	@Test
	public void getWriteCategoriesQuaryTest() throws ParseException {

		DateTime time = DateTime.now();

		RestRequestPathsAndQueryParams categoriesQuery = requestGenerator.getWriteCategoriesQuery(tenant, channel, time.toString());

		assertNotNull(categoriesQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, categoriesQuery.getQueryPaths(), categoriesQuery.getQueryParams(), false);

		assertEquals("http://10.165.140.102:8080/TextAnalyticsConfigServiceAPI/rest/channel/tenant/channel/categories.prop?lastUpdate=" + time.toString(), requestUrl);
	}

	@Test
	public void getWriteSavedSearchesQuaryTest() throws ParseException {

		DateTime time = DateTime.now();

		val categoriesQuery = requestGenerator.getWriteSavedSearchesQuery(tenant, channel, time.toString());

		assertNotNull(categoriesQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, categoriesQuery.getQueryPaths(), categoriesQuery.getQueryParams(), false);

		assertEquals("http://10.165.140.102:8080/TextAnalyticsConfigServiceAPI/rest/channel/tenant/channel/savedsearches.prop?lastUpdate=" + time.toString(), requestUrl);
	}

	protected RestDataAccess getRestDataAccess() {
		val restDataAccess = new RestDataAccess(configurationManagerMock);

		return restDataAccess;
	}
}
