package com.verint.textanalytics.bl.security.membership;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.verint.textanalytics.bl.security.EMConfigurationManager;
import com.verint.textanalytics.bl.security.FileMembershipProvider;
import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.dal.rest.RestDataAccess;
import com.verint.textanalytics.model.security.User;

public class FileMembershipProviderTest {

	@InjectMocks
	private FileMembershipProvider fileMembershipProvider;

	// this mock will inject to the tested object
	@Mock
	private ConfigurationManager configurationManager;

	// this mock will inject to the tested object
	@Mock
	private EMConfigurationManager emConfigurationManager;

	@Mock
	protected ApplicationConfiguration appConfigMock;

	/**
	 * @throws java.lang.Exception
	 *             Exception
	 */
	@Before
	public void setUp() throws Exception {
		// this will execute the injection
		MockitoAnnotations.initMocks(this);

		Mockito.when(emConfigurationManager.getDataSourcesDir()).thenReturn(getResourceFolderURL());

		Mockito.when(configurationManager.getApplicationConfiguration()).thenReturn(appConfigMock);
	}

	@Test
	public void getUserTest() {
		User user = fileMembershipProvider.getUser("UserKuku");

		Assert.assertNotNull(user);
		Assert.assertNotNull(user.getTenantsList());
		Assert.assertNotNull(user.getPrivilegesList());
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