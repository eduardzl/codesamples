package com.verint.textanalytics.web.portal.exceptions;

import static org.junit.Assert.assertEquals;
import lombok.val;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.web.portal.restinfra.ErrorDetails;
import com.verint.textanalytics.web.portal.restinfra.ErrorSeverity;
import com.verint.textanalytics.web.portal.restinfra.GenericExceptionMapper;

public class GenericExceptionMapperTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testToResponseRuntimeException() {
		val exMessage = "test";
		val ex = new Exception(exMessage);
		val genericExceptionMapper = new GenericExceptionMapper();
		val response = genericExceptionMapper.toResponse(ex);

		assertEquals(500, response.getStatus());
		assertEquals(TAConstants.appGenericErrorCode, ((ErrorDetails) response.getEntity()).getErrorCode());
		assertEquals(ex.getMessage(), ((ErrorDetails) response.getEntity()).getExceptionMessage());
		assertEquals(ex.getClass().getName(), ((ErrorDetails) response.getEntity()).getExceptionType());
		assertEquals(ErrorSeverity.High, ((ErrorDetails) response.getEntity()).getSeverity());
	}
}
