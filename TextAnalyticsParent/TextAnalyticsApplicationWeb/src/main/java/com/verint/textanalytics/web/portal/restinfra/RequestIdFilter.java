package com.verint.textanalytics.web.portal.restinfra;

import java.io.IOException;
import java.util.UUID;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.*;
import com.verint.textanalytics.common.constants.*;

/**
 * @author NShunevich
 *
 */
@Provider
@Priority(TAConstants.RequestFiltersPriority.requestIdFilter)
public class RequestIdFilter implements ContainerRequestFilter, ContainerResponseFilter {
	private Logger logger = LogManager.getLogger(this.getClass());

	/**
	 * Constructor.
	 */
	public RequestIdFilter() {

	}

	@Override
	public void filter(ContainerRequestContext requestContext) {
		// generate a unique Id
		String requestId = UUID.randomUUID().toString();

		logger.trace("Request id {} was generated for request with path {}, HTTP method {}", requestId, requestContext.getUriInfo().getAbsolutePath(), requestContext.getMethod());

		// place newly generated request id into thread context, so logger can use it to log messages		
		ThreadContext.put(TAConstants.requestId, requestId);
	}

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		ThreadContext.clearAll();
	}
}
