package com.verint.textanalytics.web.portal.restinfra;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.apache.logging.log4j.*;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.utils.JSONUtils;
import com.verint.textanalytics.common.utils.StringUtils;
import com.verint.textanalytics.common.utils.ExceptionUtils;

/**
 * GenericExceptionMapper.
 * 
 * @author imor
 *
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Override
	public Response toResponse(Throwable ex) {

		ErrorDetails errorDetails = new ErrorDetails();
		errorDetails.setErrorCode(TAConstants.appGenericErrorCode);
		errorDetails.setExceptionType(ex.getClass().getName());
		errorDetails.setExceptionMessage(ex.getMessage());
		errorDetails.setSeverity(ErrorSeverity.High);
		errorDetails.setShowError(true);

		//@formatter:off
		logger.error("GenericExceptionMapper. Response to client - {}, {}. Stack Trace - {}.", 
		                Status.INTERNAL_SERVER_ERROR,
		                JSONUtils.getObjectJSON(errorDetails), 
		                ExceptionUtils.getStackTrace(ex));
		//@formatter:on

		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorDetails).type(MediaType.APPLICATION_JSON).build();
	}
}