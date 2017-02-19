package com.verint.textanalytics.bl.security.membership;

import com.verint.textanalytics.bl.security.FoundationResponseConverter;
import com.verint.textanalytics.common.exceptions.UserNotLoggedInException;
import com.verint.textanalytics.common.utils.FileUtils;
import lombok.val;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

/**
 *
 */
public class FoundationResponseConverterTest {

	String userJson;

	private FoundationResponseConverter foundationResponseConverter;

	@Before
	public void setUp() throws IOException {
		foundationResponseConverter = new FoundationResponseConverter();

		userJson = getResourceAsString("userContext.txt");
	}

	@Test
	public void getListOfProjectDataSourcesTest() {

		val projectDataSources = foundationResponseConverter.getListOfProjectDataSources(userJson);

		Assert.assertEquals(3, projectDataSources.size());

		int i = 0;
		Assert.assertEquals(52, projectDataSources.get(i).getEmId());
		Assert.assertEquals("proj1", projectDataSources.get(i).getDisplayName());

		i = 1;
		Assert.assertEquals(55, projectDataSources.get(i).getEmId());
		Assert.assertEquals("proj2", projectDataSources.get(i).getDisplayName());

		i = 2;
		Assert.assertEquals(54, projectDataSources.get(i).getEmId());
		Assert.assertEquals("projc", projectDataSources.get(i).getDisplayName());
	}

	@Test
	public void getUserNameTest() {

		val userName = foundationResponseConverter.getUser(userJson).getName();

		Assert.assertEquals("usera", userName);
	}

	@Test
	public void getViewTimeZoneTest() {

		val viewTimeZone = foundationResponseConverter.getUser(userJson).getViewTimeZone();

		Assert.assertEquals("GMT", viewTimeZone);
	}

	@Test
	public void getUserIdTest() {

		val userid = foundationResponseConverter.getUser(userJson).getUserID();

		Assert.assertEquals(1, userid);
	}

	@Test
	public void getIsSuperUserTest() {

		val isSuperUser = foundationResponseConverter.getUser(userJson).isSuperUser();

		Assert.assertEquals(false, isSuperUser);
	}

	@Test
	public void getPrivilegesListTest() {

		val privilegesList = foundationResponseConverter.getUser(userJson).getPrivilegesList();

		Assert.assertEquals(4, privilegesList.size());
	}

	@Test(expected = UserNotLoggedInException.class)
	public void getExceptionWhenUserNotLogedInTest() throws IOException {

		String html = getResourceAsString("UserNotLogedIn.txt");

		foundationResponseConverter.getUser(html);

		Assert.assertTrue(false);
	}

	// ///////////////////////////////////////////////////////
	// private functions

	private final URL getResourceURL(String resource) {
		return Thread.currentThread().getContextClassLoader().getResource(resource);
	}

	private String getResourceAsString(String resourcePath) throws IOException {
		URL resourceUrl = this.getResourceURL(resourcePath);

		return FileUtils.getResourceTextData(resourceUrl);
	}
}
