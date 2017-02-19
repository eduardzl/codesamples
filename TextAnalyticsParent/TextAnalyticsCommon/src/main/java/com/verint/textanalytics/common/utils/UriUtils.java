package com.verint.textanalytics.common.utils;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.verint.textanalytics.common.constants.TAConstants;
import org.apache.logging.log4j.*;

/**
 * Utils for handling uris.
 * @author EZlotnik
 *
 */
public final class UriUtils {
	private static Logger s_logger;

	static {
		s_logger = LogManager.getLogger(UriUtils.class);
	}

	private UriUtils() {

	}

	/**
	 * Encode Query param.
	 * @param queryParam queryParam
	 * @return returns encoded string
	 * @throws UnsupportedEncodingException exception to be thrown
	 */
	public static String encodeQueryParam(String queryParam) {
		try {
			return org.springframework.web.util.UriUtils.encodeQueryParam(queryParam, TAConstants.utf8Encoding);
		} catch (UnsupportedEncodingException ex) {
			s_logger.error("Unsupported encoding {}", ex);
			return queryParam;
		}
	}

	/**
	 * Encodes path segment.
	 * @param pathSegment
	 *            path segment
	 * @return return
	 * @throws UnsupportedEncodingException
	 */
	public static String encodePathSegment(String pathSegment) {
		try {
			return org.springframework.web.util.UriUtils.encodePathSegment(pathSegment, TAConstants.utf8Encoding);
		} catch (UnsupportedEncodingException ex) {
			s_logger.error("Unsupported encoding {}", ex);
			return pathSegment;
		}
	}

	/**
	 * Decodes a url.
	 * @param url - url to decode
	 * @return a decoded url
	 */
	public static String decodeUrl(String url) {
		try {
			return org.springframework.web.util.UriUtils.decode(url, TAConstants.utf8Encoding);
		} catch (UnsupportedEncodingException ex) {
			s_logger.error("Unsupported encoding {}", ex);
			return url;
		}
	}

	/**
	 * Parses an URL to extract query parameters.
	 * @param url url to parse
	 * @return query parameters extracted from url
	 */
	public static MultiValueMap<String, String> getQueryParams(String url) {
		UriComponentsBuilder componentsBuilder = UriComponentsBuilder.newInstance().fromHttpUrl(url);
		UriComponents components = componentsBuilder.build();
		return components.getQueryParams();
	}

	/**
	 * Parses an URL to extract path segments.
	 *  * @param url URL to parse.
	 * @return list of path segments.
	 */
	public static List<String> getPathSegments(String url) {
		UriComponentsBuilder componentsBuilder = UriComponentsBuilder.newInstance().fromHttpUrl(url);
		UriComponents components = componentsBuilder.build();
		return components.getPathSegments();
	}

	/**
	 * Generates URL.
	 * @param baseUrl base URL
	 * @param pathSegment  path segment
	 * @return combined url
	 */
	public static String getUrl(String baseUrl, String pathSegment) {
		UriComponentsBuilder componentsBuilder = UriComponentsBuilder.newInstance().fromHttpUrl(baseUrl);
		componentsBuilder.path(pathSegment);

		return componentsBuilder.toUriString();
	}
}
