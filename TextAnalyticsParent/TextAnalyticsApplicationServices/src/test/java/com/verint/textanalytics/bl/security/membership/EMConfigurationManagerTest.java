package com.verint.textanalytics.bl.security.membership;

import com.verint.textanalytics.bl.security.EMConfigurationManager;
import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.model.security.Tenant;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class EMConfigurationManagerTest {

	@Spy
	@InjectMocks
	private EMConfigurationManager eMConfigurationManager;

	@Mock
	private ConfigurationManager configurationManager;

	/**
	 * @throws java.lang.Exception
	 *             Exception
	 */
	@Before
	public void setUp() throws Exception {
		eMConfigurationManager = Mockito.spy(EMConfigurationManager.class);

		MockitoAnnotations.initMocks(this);

		ApplicationConfiguration applicationConfiguration = Mockito.mock(ApplicationConfiguration.class);
		Mockito.when(applicationConfiguration.getSystemID()).thenReturn("123");

		Mockito.when(eMConfigurationManager.getDataSourcesDir()).thenReturn(getResourceFolderURL());

		Mockito.when(configurationManager.getApplicationConfiguration()).thenReturn(applicationConfiguration);

	}

	@Test
	public void getEMConfiguratedDataSourcesTest() {

		List<Tenant> tenants = eMConfigurationManager.getEMConfiguratedDataSources();
		assertEquals(3, tenants.size());
	}

	@Test
	public void getEMConfiguratedDataSourcesTestChaneels() {

		List<Tenant> tenants = eMConfigurationManager.getEMConfiguratedDataSources();
		assertEquals(2, tenants.get(0).getChannels().size());
	}

	@Test
	public void getEMConfiguratedDataSourcesTestChaneelsData() {

		List<Tenant> tenants = eMConfigurationManager.getEMConfiguratedDataSources();
		assertEquals("ProjAA", tenants.get(0).getChannels().get(0).getDisplayName());
	}

	@Test
	public void getEMConfiguratedDataSourcesTestTenantName() {

		List<Tenant> tenants = eMConfigurationManager.getEMConfiguratedDataSources();
		assertEquals("storeC", tenants.get(1).getDisplayName());
	}

	private final Path getResourceFolderURL() {

		Path path = null;
		try {

			path = Paths.get(Thread.currentThread().getContextClassLoader().getResource("DataSource-1.xml").toURI());
			path = path.getParent();
			return path;

		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return path;
	}
}