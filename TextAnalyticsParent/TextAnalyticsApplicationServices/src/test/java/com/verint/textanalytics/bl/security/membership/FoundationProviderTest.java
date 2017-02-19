package com.verint.textanalytics.bl.security.membership;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import lombok.val;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.verint.textanalytics.bl.security.EMConfigurationManager;
import com.verint.textanalytics.bl.security.FoundationMembershipProvider;
import com.verint.textanalytics.bl.security.FoundationResponseConverter;
import com.verint.textanalytics.bl.security.MembershipProvider;
import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.utils.FileUtils;
import com.verint.textanalytics.dal.rest.RestDataAccess;
import com.verint.textanalytics.model.security.Tenant;
import com.verint.textanalytics.model.security.User;

/**
 * @author EZlotnik
 *
 */
public class FoundationProviderTest {

	// this is the tested object
	@InjectMocks
	private FoundationMembershipProvider membershipProvider;

	@Mock
	private FoundationResponseConverter foundationResponseConverter;

	// this mock will inject to the tested object
	@Mock
	private ConfigurationManager configurationManager;

	// this mock will inject to the tested object
	@Mock
	private RestDataAccess restDataAccess;

	// this mock will inject to the tested object
	@Mock
	private EMConfigurationManager emConfigurationManager;

	@Before
	public void setUp() throws IOException {
		// this will execute the injection
		MockitoAnnotations.initMocks(this);

		String FoundationServiceURL = "http://10.165.166.253";
		// create mock for applicationConfiguration
		ApplicationConfiguration applicationConfiguration = Mockito.mock(ApplicationConfiguration.class);
		Mockito.when(applicationConfiguration.getFoundationServiceURL())
		       .thenReturn(FoundationServiceURL);

		Mockito.when(applicationConfiguration.getDarwinRestRequestTimeout())
		       .thenReturn(30000);

		Mockito.when(configurationManager.getApplicationConfiguration())
		       .thenReturn(applicationConfiguration);

		Mockito.when(
		        restDataAccess.executePostRequest(Matchers.anyString(), Matchers.anyString(), Matchers.anyListOf(String.class), Matchers.anyList(), Matchers.anyString(),
		                Matchers.anyMap()))
		       .thenReturn(getResourceAsString("userContext.txt"));

		Mockito.when(emConfigurationManager.getEMConfiguratedDataSources())
		       .thenReturn(new ArrayList<Tenant>());

		Mockito.when(foundationResponseConverter.getUser(Matchers.anyString()))
		       .thenReturn(new User());

	}

	/**
	 * Tests user retrieval.
	 */
	@Test
	public void getUserPrivilegesTest() {
		val user = membershipProvider.getUser("E3xyZnJL84");

		assertNotNull(user);
		assertNotNull(user.getPrivilegesList());
	}

	// ///////////////////////////////////////////////////////
	// private functions

	private final URL getResourceURL(String resource) {
		return Thread.currentThread()
		             .getContextClassLoader()
		             .getResource(resource);
	}

	private String getResourceAsString(String resourcePath) throws IOException {
		URL resourceUrl = this.getResourceURL(resourcePath);

		return FileUtils.getResourceTextData(resourceUrl);
	}
}
