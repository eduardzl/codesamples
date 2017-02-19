package com.verint.textanalytics.web.portal.restinfra;

import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.exceptions.*;
import com.verint.textanalytics.common.utils.ExceptionUtils;
import com.verint.textanalytics.dal.darwin.vtasyntax.errors.VTASyntaxException;
import com.verint.textanalytics.dal.darwin.vtasyntax.errors.VTASyntaxProcessingException;
import com.verint.textanalytics.dal.darwin.vtasyntax.errors.VTASyntaxRecognitionException;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * VTASyntaxExceptionMapper.
 */
@Provider
public class VTASyntaxExceptionMapper implements ExceptionMapper<VTASyntaxException> {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	private ConfigurationManager configurationManager;

	@Override
	public Response toResponse(VTASyntaxException ex) {
		Response res;
		ErrorDetails errorDetails = null;

		if (ex instanceof  VTASyntaxProcessingException) {
			errorDetails = vtaSyntaxProcessingToErrorDetails((VTASyntaxProcessingException) ex);
		} else if (ex instanceof VTASyntaxRecognitionException) {
			errorDetails = vtaSyntaxRecognitionToErrorDetails((VTASyntaxRecognitionException) ex);
		}

		// add stack trace if we in debug mode
		if (configurationManager != null && configurationManager.getApplicationConfiguration().isSendExceptionDetailsToClient()) {
			errorDetails.setStackTrace(ExceptionUtils.getStackTrace(ex));
		}

		res = Response.status(Status.INTERNAL_SERVER_ERROR).entity(errorDetails).type(MediaType.APPLICATION_JSON).build();

		return res;
	}

	/**
	 * Generates Error Details from exception.
	 * @param ex exception
	 * @return error details
	 */
	public ErrorDetails vtaSyntaxProcessingToErrorDetails(VTASyntaxProcessingException ex) {
		val errorDetails = new ErrorDetails();

		String errorKey = "";
		switch (ex.getErrorType()) {
			case StopWordsFilterInitializationFailed:
			case VTASyntaxAnalyzerInitializationFailed:
			case WildCardAnalyzerInitializationFailed:
			case TermTokensExtractionFailed:
			case TermParsingFailed:
			case PrefixLengthInWildCardSearchIsTooShort:
			case WildCardIsNotAllowedInPhrasesOrProximityQuery:
			case SearchIsEmptyOrIncludeStopWordOnly:
			case WildCardPatternIsNotAllowedAsFirstCharacterOfTerm:
				errorKey = String.format("%s_%s", TAConstants.ErrorMessageKeys.vtaSyntaxErrorPrefix, ex.getErrorType().name());
				break;

			default:
				errorKey = String.format("%s_%s", TAConstants.ErrorMessageKeys.vtaSyntaxErrorPrefix, TAConstants.ErrorMessageKeys.vtaSyntaxProcessingGenericError);
				break;
		}

		errorDetails.setSeverity(ErrorSeverity.Medium);
		errorDetails.setMessageKey(errorKey);

		return errorDetails;
	}

	/**
	 * Generates Error Details from exception.
	 * @param ex exception
	 * @return error details
	 */
	public ErrorDetails vtaSyntaxRecognitionToErrorDetails(VTASyntaxRecognitionException ex) {
		val errorDetails = new ErrorDetails();

		errorDetails.setSeverity(ErrorSeverity.Low);
		errorDetails.setMessageKey(String.format("%s_%s", TAConstants.ErrorMessageKeys.vtaSyntaxErrorPrefix, TAConstants.ErrorMessageKeys.vtaSyntaxRecognitionError));

		return errorDetails;
	}
}