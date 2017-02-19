package com.verint.textanalytics.dal.modelEditor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import lombok.val;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.dal.rest.RestDataAccess;

/***
 * 
 * @author imor
 *
 */
public class ConfigServiceRequestGeneratorTest {

	private ConfigServiceRequestGenerator requestGenerator;

	protected String tenant = "myTenant";
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

		val categoriesQuery = requestGenerator.getRetrieveModelsTreeQuery(tenant);

		assertNotNull(categoriesQuery);

		val restDataAccess = this.getRestDataAccess();
		String requestUrl = restDataAccess.getRequestUrl(baseUrl, categoriesQuery.getQueryPaths(), categoriesQuery.getQueryParams(), true);

		assertEquals("http://10.165.140.102:8080/TextAnalyticsConfigServiceAPI/rest/tenant/myTenant?tree=", requestUrl);
	}

	protected RestDataAccess getRestDataAccess() {
		val restDataAccess = new RestDataAccess(configurationManagerMock);

		return restDataAccess;
	}
}
