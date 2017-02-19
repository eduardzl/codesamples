package com.verint.textanalytics.web.portal;

import static org.junit.Assert.*;
import lombok.val;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.verint.textanalytics.common.security.ChannelAuthorizationNotRequired;
import com.verint.textanalytics.common.security.OperationPrivelegesAnnotation;
import com.verint.textanalytics.common.security.OperationPrivelegesAnnotation.PrivilegeType;
import com.verint.textanalytics.web.portal.restinfra.PrivilegesAnnotationExtractor;
import com.verint.textanalytics.web.portal.restinfra.RequestMethodInfo;

public class PrivilegesAnnotationExtractorTest {

	PrivilegesAnnotationExtractor testedClass;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		testedClass = new PrivilegesAnnotationExtractor();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void getMethodSecurityContextTest() {

	}

	@OperationPrivelegesAnnotation(requiredAllPrivileges = { PrivilegeType.USEAPPLICATION })
	@Test
	public void getMethodSecurityContextPrivlageAnnotationTest() throws Exception {
		RequestMethodInfo info = new RequestMethodInfo();
		info.parseUrl("/PrivilegesAnnotationExtractorTest/getMethodSecurityContextPrivlageAnnotationTest");

		val res = testedClass.getMethodSecurityContext(info);
		assertNotNull(res);
		assertNotNull(res.getRequiredAllPrivileges());
		assertEquals(1, res.getRequiredAllPrivileges().length);
		assertEquals(PrivilegeType.USEAPPLICATION, res.getRequiredAllPrivileges()[0]);
		assertNotNull(res.getRequiredAnyPrivileges());
		assertEquals(1, res.getRequiredAnyPrivileges().length);
		assertEquals(PrivilegeType.NONE, res.getRequiredAnyPrivileges()[0]);
		assertTrue(res.isChannelAuthenticationRequired());
	}

	@ChannelAuthorizationNotRequired
	@Test
	public void getMethodSecurityContextChannelAnnotationTest() throws Exception {
		RequestMethodInfo info = new RequestMethodInfo();
		info.parseUrl("/PrivilegesAnnotationExtractorTest/getMethodSecurityContextChannelAnnotationTest");

		val res = testedClass.getMethodSecurityContext(info);
		assertNotNull(res);
		assertNull(res.getRequiredAllPrivileges());
		assertNull(res.getRequiredAnyPrivileges());
		assertFalse(res.isChannelAuthenticationRequired());
	}

	@Test
	public void getMethodSecurityContextNoAnnotationTest() throws Exception {
		RequestMethodInfo info = new RequestMethodInfo();
		info.parseUrl("/PrivilegesAnnotationExtractorTest/getMethodSecurityContextNoAnnotationTest");

		val res = testedClass.getMethodSecurityContext(info);
		assertNotNull(res);
		assertNull(res.getRequiredAllPrivileges());
		assertNull(res.getRequiredAnyPrivileges());
		assertTrue(res.isChannelAuthenticationRequired());
	}

	@Test
	public void getMethodSecurityContextClassNotExistTest() throws Exception {
		RequestMethodInfo info = new RequestMethodInfo();
		info.parseUrl("/ClassNotExists/Method");

		val res = testedClass.getMethodSecurityContext(info);
		assertNull(res);
	}

	@Test
	public void getMethodSecurityContextMethodNotExistTest() throws Exception {

		RequestMethodInfo info = new RequestMethodInfo();
		info.parseUrl("/PrivilegesAnnotationExtractorTest/Method");

		val res = testedClass.getMethodSecurityContext(info);
		assertNull(res);
	}

}