package com.verint.textanalytics.web.portal.restinfra;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.apache.logging.log4j.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.constants.TAConstants.Environment;
import com.verint.textanalytics.common.utils.JSONUtils;
import com.verint.textanalytics.common.utils.StringUtils;
import com.verint.textanalytics.common.utils.ExceptionUtils;

/**
 * Exception mapper.
 * @author EZlotnik
 *
 */
@Provider
public class JsonProcessingExceptionMapper implements ExceptionMapper<JsonProcessingException> {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Override
	public Response toResponse(JsonProcessingException ex) {
		ErrorDetails errorDetails = new ErrorDetails();
		errorDetails.setErrorCode(TAConstants.jacksonJsonSerializationErrorCode);
		errorDetails.setExceptionType(ex.getClass().getName());
		errorDetails.setExceptionMessage(TAConstants.ErrorMessages.jacksonJsonProcessingError);
		errorDetails.setSeverity(ErrorSeverity.High);
		errorDetails.setShowError(true);

		//@formatter:off
		logger.error("JsonProcessingExceptionMapper. Response to client - {}, {}. Stack Trace - {}.", 
		                Status.INTERNAL_SERVER_ERROR,
		                JSONUtils.getObjectJSON(errorDetails), 
		                ExceptionUtils.getStackTrace(ex));
		//@formatter:on

		return Response.status(Status.BAD_REQUEST).entity(errorDetails).build();
	}
}
