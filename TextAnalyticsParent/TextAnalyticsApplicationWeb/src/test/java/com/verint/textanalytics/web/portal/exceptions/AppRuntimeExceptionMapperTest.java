package com.verint.textanalytics.web.portal.exceptions;

import static org.junit.Assert.assertEquals;

import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.dal.darwin.vtasyntax.errors.ProcessingErrorType;
import com.verint.textanalytics.dal.darwin.vtasyntax.errors.VTASyntaxProcessingException;
import com.verint.textanalytics.web.portal.restinfra.VTASyntaxExceptionMapper;
import lombok.val;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.verint.textanalytics.common.exceptions.AppRuntimeException;
import com.verint.textanalytics.common.exceptions.ConfigurationErrorCode;
import com.verint.textanalytics.common.exceptions.ConfigurationException;
import com.verint.textanalytics.common.exceptions.GenericAppExecutionErrorCode;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionErrorCode;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionException;
import com.verint.textanalytics.web.portal.restinfra.AppRuntimeExceptionMapper;
import com.verint.textanalytics.web.portal.restinfra.ErrorDetails;
import com.verint.textanalytics.web.portal.restinfra.ErrorSeverity;

public class AppRuntimeExceptionMapperTest {

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
	public final void testToResponseAppRuntimeException() {
		val exMessage = "test";
		val ex = new Exception(exMessage);
		val appEx = new AppRuntimeException(ex, GenericAppExecutionErrorCode.Error);
		val appRuntimeExceptionMapper = new AppRuntimeExceptionMapper();
		val response = appRuntimeExceptionMapper.toResponse(appEx);

		assertEquals(500, response.getStatus());
		assertEquals(GenericAppExecutionErrorCode.Error.toString(), ((ErrorDetails) response.getEntity()).getErrorCode());
		assertEquals(appEx.getMessage(), ((ErrorDetails) response.getEntity()).getExceptionMessage());
		assertEquals(appEx.getClass().getName(), ((ErrorDetails) response.getEntity()).getExceptionType());
		assertEquals(ErrorSeverity.High, ((ErrorDetails) response.getEntity()).getSeverity());
	}

	@Test
	public final void testToResponseTextQueryExecutionException1() {
		val exMessage = "test";
		val ex = new Exception(exMessage);
		val appEx = new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.RESTTextQueryExecutionError);
		val appRuntimeExceptionMapper = new AppRuntimeExceptionMapper();
		val response = appRuntimeExceptionMapper.toResponse(appEx);

		assertEquals(500, response.getStatus());
		assertEquals(TextQueryExecutionErrorCode.RESTTextQueryExecutionError.toString(), ((ErrorDetails) response.getEntity()).getErrorCode());
		assertEquals(appEx.getMessage(), ((ErrorDetails) response.getEntity()).getExceptionMessage());
		assertEquals(appEx.getClass().getName(), ((ErrorDetails) response.getEntity()).getExceptionType());
		assertEquals(ErrorSeverity.High, ((ErrorDetails) response.getEntity()).getSeverity());
	}

	@Test
	public final void testToResponseVTASyntaxProcessingException() {
		val exMessage = "test";
		val ex = new Exception(exMessage);
		val appEx = new VTASyntaxProcessingException(ProcessingErrorType.PrefixLengthInWildCardSearchIsTooShort, "");
		val vtaSyntaxExceptionMapper = new VTASyntaxExceptionMapper();
		val response = vtaSyntaxExceptionMapper.toResponse(appEx);

		ErrorDetails errorDetails = (ErrorDetails) response.getEntity();

		assertEquals(500, response.getStatus());
		assertEquals(String.format("%s_%s", TAConstants.ErrorMessageKeys.vtaSyntaxErrorPrefix, ProcessingErrorType.PrefixLengthInWildCardSearchIsTooShort.name()),
		             errorDetails.getMessageKey());

		assertEquals(ErrorSeverity.Medium, errorDetails.getSeverity());
	}

	@Test
	public final void testToResponseTextQueryExecutionException3() {
		val exMessage = "test";
		val ex = new Exception(exMessage);
		val appEx = new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.UtteranceHighlightsBuildError);
		val appRuntimeExceptionMapper = new AppRuntimeExceptionMapper();
		val response = appRuntimeExceptionMapper.toResponse(appEx);

		assertEquals(500, response.getStatus());
		assertEquals(TextQueryExecutionErrorCode.UtteranceHighlightsBuildError.toString(), ((ErrorDetails) response.getEntity()).getErrorCode());
		assertEquals(appEx.getMessage(), ((ErrorDetails) response.getEntity()).getExceptionMessage());
		assertEquals(appEx.getClass().getName(), ((ErrorDetails) response.getEntity()).getExceptionType());
		assertEquals(ErrorSeverity.High, ((ErrorDetails) response.getEntity()).getSeverity());
	}

	@Test
	public final void testToResponseConfigurationException() {
		val exMessage = "test";
		val ex = new Exception(exMessage);
		val appEx = new ConfigurationException(ex, ConfigurationErrorCode.ApplicationConfigurationNotValidError);
		val appRuntimeExceptionMapper = new AppRuntimeExceptionMapper();
		val response = appRuntimeExceptionMapper.toResponse(appEx);

		assertEquals(500, response.getStatus());
		assertEquals(ConfigurationErrorCode.ApplicationConfigurationNotValidError.toString(), ((ErrorDetails) response.getEntity()).getErrorCode());
		assertEquals(appEx.getMessage(), ((ErrorDetails) response.getEntity()).getExceptionMessage());
		assertEquals(appEx.getClass().getName(), ((ErrorDetails) response.getEntity()).getExceptionType());
		assertEquals(ErrorSeverity.High, ((ErrorDetails) response.getEntity()).getSeverity());

	}	
}
