package com.verint.textanalytics.web.portal.restinfra;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;

/**
 * @author EZlotnik Web application class (Jersey).
 */
public class TextAnalyticsWebApplication extends ResourceConfig {

	/**
	 * Register filter to each request.
	 */
	public TextAnalyticsWebApplication() {
		register(RequestContextFilter.class);
		register(com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider.class);
	}
}
