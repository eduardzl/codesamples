package com.verint.textanalytics.web.portal.restinfra;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;

import lombok.val;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * CustomJsonJacksonProvider.
 * @author EZlotnik
 *
 */
@Provider
@Consumes(MediaType.APPLICATION_JSON)
// NOTE: required to support "non-standard" JSON variants
@Produces(MediaType.APPLICATION_JSON)
public class CustomJsonJacksonProvider extends JacksonJsonProvider {
	@Autowired
	private JacksonNamingStrategy jacksonNamingStrategy;

	/* BEGIN GENERATED CODE */
	/*
	 * (non-Javadoc).
	 * 
	 * @see
	 * com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider#_locateMapperViaProvider
	 * (java.lang.Class, javax.ws.rs.core.MediaType)
	 */
	@Override
	@SuppressWarnings({ "checkstyle:MethodName", "deprecation" })
	protected ObjectMapper _locateMapperViaProvider(Class<?> type, MediaType mediaType) {
		val mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		// don't apply naming strategy for Entity FacetNode
		if (!type.getName().equalsIgnoreCase("com.verint.textanalytics.web.viewmodel.TextElementFacetTreeNode")) {

			mapper.setPropertyNamingStrategy(jacksonNamingStrategy);
		}

		// @formatter:off
		mapper.setVisibilityChecker(mapper.getSerializationConfig()
		                                  .getDefaultVisibilityChecker()
		                                  .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
		                                  .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
		                                  .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
		                                  .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
		// @formatter:on

		return mapper;
	}
	/* END GENERATED CODE */
}
