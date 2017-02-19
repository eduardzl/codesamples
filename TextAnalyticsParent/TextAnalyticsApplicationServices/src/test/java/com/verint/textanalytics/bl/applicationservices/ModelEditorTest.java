package com.verint.textanalytics.bl.applicationservices;

import com.verint.authorization.services.JWTTokenService;
import com.verint.textanalytics.bl.security.MembershipProvider;
import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.configuration.FilePathType;
import com.verint.textanalytics.common.security.JWTService;
import com.verint.textanalytics.common.security.ModelEditorSecretFile;
import com.verint.textanalytics.model.security.Tenant;
import com.verint.textanalytics.model.security.User;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class ModelEditorTest extends BaseTest {

	private String i360FoundationToken;

	@InjectMocks
	private ModelEditorService modelEditorService;

	@Mock
	private MembershipProvider membershipProvider;

	@Mock
	private ConfigurationManager configurationManager;

	@Mock
	private ApplicationConfiguration applicationConfiguration;

	@Before
	public void initialize() {

		this.i360FoundationToken = "i360FoundationToken";

		MockitoAnnotations.initMocks(this);

		// the user to return from Foundation membership provider
		User user = new User();
		user.setName("user");
		user.setUserID(123);

		List<Tenant> tenantsList = new ArrayList<Tenant>();
		Tenant tenant1 = new Tenant();
		tenant1.setId("t1");
		tenant1.setDisplayName("t1Name");
		tenant1.setEmId(111111);

		Tenant tenant2 = new Tenant();
		tenant2.setId("XTelco");
		tenant2.setDisplayName("XTelcoName");
		tenant2.setEmId(222222);

		Tenant tenant3 = new Tenant();
		tenant3.setId("myTenant");
		tenant3.setDisplayName("myTenantName");
		tenant3.setEmId(333333);

		tenantsList.add(tenant1);
		tenantsList.add(tenant2);
		tenantsList.add(tenant3);

		user.setTenantsList(tenantsList);

		Mockito.when(membershipProvider.getUser(this.i360FoundationToken)).thenReturn(user);

		Mockito.when(configurationManager.getApplicationConfiguration()).thenReturn(applicationConfiguration);
		Mockito.when(applicationConfiguration.getApplicationID()).thenReturn("xxx");
	}

	@Test
	public void getModelEditorHeaderTokenTest() throws Exception {
		// the tested object
		JWTService jwtService = new JWTService();
		jwtService.setJwtTokenService(new JWTTokenService());
		modelEditorService.setJwtService(jwtService);
		String hashmapSerFileName = "hashmap.ser";
		String hashmapSerFilePath;
		URL resourceUrl = this.getResourceURL(hashmapSerFileName);
		hashmapSerFilePath = resourceUrl.getPath();
		ModelEditorSecretFile modelEditorSecretFile = new ModelEditorSecretFile(hashmapSerFilePath, FilePathType.AbsolutePath);
		jwtService.setModelEditorSecretFile(modelEditorSecretFile);

		jwtService.initIt();

		// return the token
		val res = modelEditorService.getModelEditorHeaderToken(i360FoundationToken);

		// create the object that will extract the token in the Model Editor
		// service
		JWTTokenService tokenService = new JWTTokenService();
		hashmapSerFileName = "hashmap.ser";
		resourceUrl = this.getResourceURL(hashmapSerFileName);
		hashmapSerFilePath = resourceUrl.getPath();
		tokenService.setSecretStoragePath(hashmapSerFilePath);

		// extract the token
		val afterExtraction = tokenService.extractHeaderTokenData(res.getValue());

		// check if the extracted token contains the data
		assertTrue(afterExtraction.contains("t1"));
		assertTrue(afterExtraction.contains("t1Name"));
		assertTrue(afterExtraction.contains("XTelco"));
		assertTrue(afterExtraction.contains("XTelcoName"));
		assertTrue(afterExtraction.contains("myTenant"));
		assertTrue(afterExtraction.contains("myTenantName"));

		assertTrue(afterExtraction.contains("123"));
	}
}