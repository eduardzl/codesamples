package com.verint.textanalytics.common.utils;

import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;

/**
 * Created by yzanis on 09-May-16.
 */
public class IDGeneratorUtilsTest {

	@Mock
	private ConfigurationManager configurationManager;

	@Before
	public void setUp() throws Exception {

		MockitoAnnotations.initMocks(this);

		ApplicationConfiguration applicationConfiguration = Mockito.mock(ApplicationConfiguration.class);
		Mockito.when(applicationConfiguration.getSystemID()).thenReturn("123");

		Mockito.when(configurationManager.getApplicationConfiguration()).thenReturn(applicationConfiguration);



	}

	@Test
	public void generateChannelIDTest() {
		int channel = 5;

		assertEquals(1005123, IDGeneratorUtils.generateChannelID(channel, "123"));

	}

	@Test
	public void generateChannelIDTest1() {
		int channel = 455;

		assertEquals(1455123, IDGeneratorUtils.generateChannelID(channel, "123"));

	}

	@Test
	public void generateChannelIDTest2() {
		int channel = 1234;

		assertEquals(2234123, IDGeneratorUtils.generateChannelID(channel, "123"));

	}

	@Test
	public void generateTenantIDTest() {
		int channel = 5;

		assertEquals(1005123, IDGeneratorUtils.generateTenantID(channel, "123"));

	}

	@Test
	public void generateTenantIDTest1() {
		int channel = 455;

		assertEquals(1455123, IDGeneratorUtils.generateTenantID(channel, "123"));

	}

	@Test
	public void generateTenantIDTest2() {
		int channel = 1234;

		assertEquals(2234123, IDGeneratorUtils.generateTenantID(channel, "123"));

	}

	@Test
	public void getDataSourceIDFromNameTest() {
		String channel = "1001123";

		assertEquals(1, IDGeneratorUtils.getDataSourceIDFromName(channel));

	}

	@Test
	public void getDataSourceIDFromNameTest2() {
		String channel = "1455123";

		assertEquals(455, IDGeneratorUtils.getDataSourceIDFromName(channel));

	}

	@Test
	public void getDataSourceIDFromNameTest3() {
		String channel = "2234123";

		assertEquals(1234, IDGeneratorUtils.getDataSourceIDFromName(channel));

	}

	@Test
	public void getDataSourceIDFromNameTest4() {
		String channel = "Telco";

		assertEquals(0, IDGeneratorUtils.getDataSourceIDFromName(channel));

	}

	@Test
	public void generateCategoryIDTest() {
		String channel = "1001123";

		assertEquals(600001, IDGeneratorUtils.generateCategoryID(channel, 1));

	}

	@Test
	public void generateCategoryIDTest2() {
		String channel = "1455123";

		assertEquals(800455, IDGeneratorUtils.generateCategoryID(channel, 3));

	}

	@Test
	public void generateCategoryIDTest3() {
		String channel = "2234123";

		assertEquals(1001234, IDGeneratorUtils.generateCategoryID(channel, 5));

	}

	@Test
	public void generateCategoryIDTest4() {
		String channel = "Telco";

		assertEquals(1100000, IDGeneratorUtils.generateCategoryID(channel, 6));

	}

}