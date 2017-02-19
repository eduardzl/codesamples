package com.verint.textanalytics.dal.rest;

import com.google.common.base.Throwables;
import com.verint.textanalytics.common.collection.MultivaluedStringMap;
import com.verint.textanalytics.common.configuration.ApplicationConfiguration;
import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.exceptions.HttpExecutionErrorCode;
import com.verint.textanalytics.common.exceptions.HttpExecutionException;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionErrorCode;
import com.verint.textanalytics.common.exceptions.TextQueryExecutionException;
import com.verint.textanalytics.common.security.AWTService;
import com.verint.textanalytics.common.utils.StringUtils;
import com.verint.textanalytics.common.utils.UriUtils;
import lombok.val;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.filter.LoggingFilter;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import propel.core.functional.tuples.Pair;

import javax.net.ssl.*;
import javax.security.cert.CertificateException;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author EZlotnik Invokes REST requests
 */
public class RestDataAccess implements DisposableBean {

	@Autowired
	private AWTService awtService;

	public static final String RESPONSE_STATUS = "Response Status= ";
	private static final String EMPTY_RESPONSE_ACCEPTED = "{} Request : Empty Response accepted from host ";
	private static final String RESPONSE_ACCEPTED_FROM_HOST = "{} Request : Response accepted from host is {}";
	private static final String REQUEST_DECODED_URL = "{} Request : Decoded URL is {}";
	private static final String REQUEST_URL = "{} Request : URL is {}";
	private static final String REQUEST_RESPONSE_STATUS = "{} Request - Response status {}";
	private final String contentType = "Content-Type";

	private String jsonContentType = "application/json";
	private String formUrlEncoded = "application/x-www-form-urlencoded";

	private Logger logger = LogManager.getLogger(this.getClass());

	private final int responseNumberOfLinestoLog = 30;

	private final String amp = "&";
	private final String parameterNameWithValuePattern = "%s=%s";
	private final String requestBodyLogPattern = "{} Request : Body -  {}";
	private final String requestDecodedBodyLogPattern = "{} Request : Decoded Body -  {}";

	/**
	 * Client instances are expensive resources. It is recommended a configured
	 * instance is reused for the creation of Web resources. The creation of Web
	 * resources, the building of requests and receiving of responses are
	 * guaranteed to be thread safe. Thus a Client instance and WebResource
	 * instances may be shared between multiple threads
	 */

	private Client client;


	/**
	 * Constructor.
	 *
	 * @param configurationManager configuration manager
	 */
	public RestDataAccess(ConfigurationManager configurationManager) {

		ApplicationConfiguration appConfig = configurationManager.getApplicationConfiguration();
		final int millisecondsInSecond = 1000;

		PoolingHttpClientConnectionManager connectionManager = this.createHttpConnectionManager(appConfig);

		ClientConfig clientConfig = new ClientConfig();
		clientConfig.property(ApacheClientProperties.CONNECTION_MANAGER, connectionManager);

		ApacheConnectorProvider provider = new ApacheConnectorProvider();
        clientConfig.connectorProvider(provider);

		this.client = ClientBuilder.newBuilder().withConfig(clientConfig).build();

		this.client.property(ClientProperties.CONNECT_TIMEOUT, appConfig.getDarwinRestRequestTimeout() * millisecondsInSecond);
		this.client.property(ClientProperties.READ_TIMEOUT, appConfig.getDarwinRestRequestTimeout() * millisecondsInSecond);

		if (appConfig.getLogHttpClientRequests()) {
			client.register(new LoggingFilter());
		}
	}

	/**
	 * Executes a GET request to remote URL.
	 *
	 * @param requestUrl  URL to invoke the request
	 * @param queryPaths  parts to be joined to base url
	 * @param queryParams parameters to be added to query string of request
	 * @return a textual response of REST request
	 */
	public String executeGetRequest(String requestUrl, List<String> queryPaths, MultivaluedStringMap queryParams) {
		return this.executeGetRequest("", requestUrl, queryPaths, queryParams);
	}

	/**
	 * Executes a GET request to remote URL for specific application request.
	 *
	 * @param requestUrl  URL to invoke the request
	 * @param requestType an application request type
	 * @param queryPaths  parts to be joined to base url
	 * @param queryParams parameters to be added to query string of request
	 * @return a textual response of REST request
	 */
	public String executeGetRequest(String requestType, String requestUrl, List<String> queryPaths, MultivaluedStringMap queryParams) {
		String jsonResponse = "";
		int responseStatus = -1;

		try {
			WebTarget webTarget = this.createRequestWebTarget(requestUrl, queryPaths, queryParams);

			requestType = requestType != null ? requestType : "";

			String requestURI = webTarget.getUri().toString();

			val awtHeader = awtService.getAWTHeader("GET", getRelativeURL(requestURI));

			logger.info(REQUEST_URL, requestType, requestURI);
			logger.info(REQUEST_DECODED_URL, requestType, UriUtils.decodeUrl(requestURI));

			Invocation.Builder invocationBuilder = webTarget.request();
			invocationBuilder.header(contentType, this.jsonContentType);
			invocationBuilder.header(awtHeader.getKey(), awtHeader.getValue());

			Response response = invocationBuilder.get();

			// try to read response
			jsonResponse = response.readEntity(String.class);

			if (!StringUtils.isNullOrBlank(jsonResponse)) {
				logger.trace(RESPONSE_ACCEPTED_FROM_HOST, requestType, StringUtils.topNLines(jsonResponse, responseNumberOfLinestoLog));
			} else {
				logger.trace(EMPTY_RESPONSE_ACCEPTED, requestType);
			}

			responseStatus = response.getStatus();

			if (responseStatus != TAConstants.httpCode200) {
				logger.info(REQUEST_RESPONSE_STATUS, requestType, responseStatus);

				throw new Exception(RESPONSE_STATUS + responseStatus + " Response Content : " + jsonResponse);
			}

		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.RESTTextQueryExecutionError));
		}

		return jsonResponse;
	}

	/**
	 * Executes a GET request to remote URL for specific application request.
	 *
	 * @param requestUrl  URL to invoke the request
	 * @param requestType an application request type
	 * @param queryPaths  parts to be joined to base url
	 * @param queryParams parameters to be added to query string of request
	 * @param callback    call to be invoked when the request is completed
	 * @return a textual response of REST request
	 */
	public Future<Response> executeGetRequestAsync(String requestType, String requestUrl, List<String> queryPaths, MultivaluedStringMap queryParams, InvocationCallback<Response> callback) {
		Future<Response> responseFuture = null;
		int responseStatus = -1;

		try {
			WebTarget webTarget = this.createRequestWebTarget(requestUrl, queryPaths, queryParams);

			requestType = requestType != null ? requestType : "";

			String requestURI = webTarget.getUri().toString();

			logger.info(REQUEST_URL, requestType, requestURI);
			logger.info(REQUEST_DECODED_URL, requestType, UriUtils.decodeUrl(requestURI));

			val awtHeader = awtService.getAWTHeader("GET", getRelativeURL(requestURI));

			Invocation.Builder invocationBuilder = webTarget.request();
			invocationBuilder.header(contentType, this.jsonContentType);
			invocationBuilder.header(awtHeader.getKey(), awtHeader.getValue());

			// invoke Asynchronous Client  call and supply callback instance
			responseFuture = invocationBuilder.async().get(callback);
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.RESTTextQueryExecutionError));
		}

		return responseFuture;
	}


	/**
	 * Executes a GET request to remote URL and returns the HTTp response.
	 *
	 * @param requestUrl  URL to invoke the request
	 * @param queryPaths  parts to be joined to base url
	 * @param queryParams parameters to be added to query string of request
	 * @return response of REST request
	 */
	public Response executeGetRequestFullResponse(String requestUrl, List<String> queryPaths, MultivaluedStringMap queryParams) {
		Response response = null;

		try {

			WebTarget webTarget = this.createRequestWebTarget(requestUrl, queryPaths, queryParams);

			String requestURI = webTarget.getUri().toString();

			logger.info("Request : URL is {}", requestURI);



			Invocation.Builder invocationBuilder = webTarget.request();
			invocationBuilder.header(contentType, this.jsonContentType);

			val awtHeader = awtService.getAWTHeader("GET", getRelativeURL(requestURI));
			invocationBuilder.header(awtHeader.getKey(), awtHeader.getValue());

			response = invocationBuilder.get();

		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.RESTTextQueryExecutionError));
		}

		return response;
	}

	/**
	 * Executes a PUT request to remote URL for specific application request.
	 *
	 * @param requestUrl  URL to invoke the request
	 * @param requestType an application request type
	 * @param queryPaths  parts to be joined to base url
	 * @param headers     headers of request
	 * @param bodyParams  requst body parameters
	 * @param bodyString  requst body string
	 * @param queryParams parameters to be added to query string of request
	 * @return a textual response of PUT request
	 */
	public String executePutRequest(String requestType, String requestUrl, List<String> queryPaths, List<Pair<String, String>> bodyParams, String bodyString, Map<String, String> headers, MultivaluedStringMap queryParams) {
		String jsonResponse = "";
		int responseStatus = -1;

		try {
			WebTarget webTarget = this.createRequestWebTarget(requestUrl, queryPaths, queryParams);

			requestType = requestType != null ? requestType : "";

			String requestURI = webTarget.getUri().toString();

			logger.info(REQUEST_URL, requestType, requestURI);
			logger.info(REQUEST_DECODED_URL, requestType, UriUtils.decodeUrl(requestURI));

			Invocation.Builder invocationBuilder = webTarget.request();
			//MediaType.APPLICATION_JSON_TYPE);

			val awtHeader = awtService.getAWTHeader("PUT", getRelativeURL(requestURI));
			invocationBuilder.header(awtHeader.getKey(), awtHeader.getValue());

			// add headers to request
			if (headers != null) {
				headers.keySet().stream().forEach(key -> invocationBuilder.header(key, headers.get(key)));
			}

			Response response = null;

			if (bodyParams != null) {
				StringBuilder sbBodyParams = new StringBuilder();
				Pair<String, String> bodyParam = null;

				for (int i = 0; i < bodyParams.size(); i++) {
					bodyParam = bodyParams.get(i);

					sbBodyParams.append(i > 0 ? amp : "");
					sbBodyParams.append(String.format(parameterNameWithValuePattern, bodyParam.getFirst(), UriUtils.encodeQueryParam(bodyParam.getSecond())));
				}

				String requestBody = sbBodyParams.toString();

				logger.info(requestBodyLogPattern, requestType, requestBody);
				logger.info(requestDecodedBodyLogPattern, requestType, UriUtils.decodeUrl(requestBody));

				response = invocationBuilder.put(Entity.entity(requestBody, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
			} else {
				if (bodyString != null) {
					response = invocationBuilder.put(Entity.entity(bodyString, MediaType.APPLICATION_JSON_TYPE));
				} else {
					response = invocationBuilder.put(Entity.entity("{}", MediaType.APPLICATION_JSON_TYPE));
				}
			}

			// try to read response
			jsonResponse = response.readEntity(String.class);

			if (!StringUtils.isNullOrBlank(jsonResponse)) {
				logger.trace(RESPONSE_ACCEPTED_FROM_HOST, requestType, StringUtils.topNLines(jsonResponse, responseNumberOfLinestoLog));
			} else {
				logger.info(EMPTY_RESPONSE_ACCEPTED, requestType);
			}

			responseStatus = response.getStatus();

			if (responseStatus != TAConstants.httpCode200) {
				logger.info(REQUEST_RESPONSE_STATUS, requestType, responseStatus);

				HttpExecutionException exc = new HttpExecutionException(RESPONSE_STATUS + responseStatus + " Response Content : " + jsonResponse,
				                                                        HttpExecutionErrorCode.HttpExecutionFailed);

				exc.setResponseStatus(responseStatus);
				exc.setResponseText(jsonResponse);

				throw exc;
			}

		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, HttpExecutionException.class);
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.RESTTextQueryExecutionError));
		}

		return jsonResponse;
	}

	/**
	 * Executes a PUT request to remote URL for specific application request.
	 *
	 * @param requestUrl  URL to invoke the request
	 * @param requestType an application request type
	 * @param queryPaths  parts to be joined to base url
	 * @param headers     headers of request
	 * @param bodyParams  requst body parameters
	 * @param bodyString  requst body string
	 * @param queryParams parameters to be added to query string of request
	 * @return a response of PUT request
	 */
	public Response executePutRequestFullResponse(String requestType, String requestUrl, List<String> queryPaths, List<Pair<String, String>> bodyParams, String bodyString, Map<String, String> headers, MultivaluedStringMap queryParams) {
		Response response = null;

		try {
			WebTarget webTarget = this.createRequestWebTarget(requestUrl, queryPaths, queryParams);

			requestType = requestType != null ? requestType : "";

			String requestURI = webTarget.getUri().toString();

			logger.info(REQUEST_URL, requestType, requestURI);
			logger.info(REQUEST_DECODED_URL, requestType, UriUtils.decodeUrl(requestURI));

			Invocation.Builder invocationBuilder = webTarget.request();

			val awtHeader = awtService.getAWTHeader("PUT", getRelativeURL(requestURI));
			invocationBuilder.header(awtHeader.getKey(), awtHeader.getValue());

			// add headers to request
			if (headers != null) {
				headers.keySet().stream().forEach(key -> invocationBuilder.header(key, headers.get(key)));
			}

			if (bodyParams != null) {
				StringBuilder sbBodyParams = new StringBuilder();
				Pair<String, String> bodyParam = null;

				for (int i = 0; i < bodyParams.size(); i++) {
					bodyParam = bodyParams.get(i);

					sbBodyParams.append(i > 0 ? amp : "");
					sbBodyParams.append(String.format(parameterNameWithValuePattern, bodyParam.getFirst(), UriUtils.encodeQueryParam(bodyParam.getSecond())));
				}

				String requestBody = sbBodyParams.toString();

				logger.info(requestBodyLogPattern, requestType, requestBody);
				logger.info(requestDecodedBodyLogPattern, requestType, UriUtils.decodeUrl(requestBody));

				response = invocationBuilder.put(Entity.entity(requestBody, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
			} else {
				if (bodyString != null) {
					response = invocationBuilder.put(Entity.entity(bodyString, MediaType.APPLICATION_JSON_TYPE));
				} else {
					response = invocationBuilder.put(Entity.entity("{}", MediaType.APPLICATION_JSON_TYPE));
				}
			}

		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.RESTTextQueryExecutionError));
		}

		return response;
	}

	/**
	 * Executes a DELETE request to remote URL for specific application request.
	 *
	 * @param requestUrl  URL to invoke the request
	 * @param requestType an application request type
	 * @param queryPaths  parts to be joined to base url
	 * @param headers     headers of request
	 * @return a textual response of DELETE request
	 */
	public String executeDeleteRequest(String requestType, String requestUrl, List<String> queryPaths, Map<String, String> headers) {
		String jsonResponse = "";
		int responseStatus = -1;

		try {
			WebTarget webTarget = this.createRequestWebTarget(requestUrl, queryPaths, null);

			requestType = requestType != null ? requestType : "";

			String requestURI = webTarget.getUri().toString();

			logger.debug(REQUEST_URL, requestType, requestURI);
			logger.debug(REQUEST_DECODED_URL, requestType, UriUtils.decodeUrl(requestURI));

			Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);

			val awtHeader = awtService.getAWTHeader("DELETE", getRelativeURL(requestURI));
			invocationBuilder.header(awtHeader.getKey(), awtHeader.getValue());

			// add headers to request
			if (headers != null) {
				headers.keySet().stream().forEach(key -> invocationBuilder.header(key, headers.get(key)));
			}

			Response response = null;

			response = invocationBuilder.delete();

			// try to read response
			jsonResponse = response.readEntity(String.class);

			if (!StringUtils.isNullOrBlank(jsonResponse)) {
				logger.trace(RESPONSE_ACCEPTED_FROM_HOST, requestType, StringUtils.topNLines(jsonResponse, responseNumberOfLinestoLog));
			} else {
				logger.debug(EMPTY_RESPONSE_ACCEPTED, requestType);
			}

			responseStatus = response.getStatus();

			logger.debug(REQUEST_RESPONSE_STATUS, requestType, responseStatus);

			if (responseStatus != TAConstants.httpCode200) {
				HttpExecutionException exc = new HttpExecutionException(RESPONSE_STATUS + responseStatus + " Response Content : " + jsonResponse,
				                                                        HttpExecutionErrorCode.HttpExecutionFailed);

				exc.setResponseStatus(responseStatus);
				exc.setResponseText(jsonResponse);

				throw exc;
			}

		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, HttpExecutionException.class);
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.RESTTextQueryExecutionError));
		}

		return jsonResponse;
	}

	/**
	 * Executes a GET request to remote URL for specific application request.
	 *
	 * @param requestUrl  URL to invoke the request
	 * @param requestType an application request type
	 * @param queryPaths  parts to be joined to base url
	 * @param headers     headers of request
	 * @param bodyParams  requst body parameters
	 * @param bodyString  requst body string
	 * @return a textual response of REST request
	 */
	public String executePostRequest(String requestType, String requestUrl, List<String> queryPaths, List<Pair<String, String>> bodyParams, String bodyString, Map<String, String> headers) {
		String jsonResponse = "";
		int responseStatus = -1;

		try {
			WebTarget webTarget = this.createRequestWebTarget(requestUrl, queryPaths, null);

			requestType = requestType != null ? requestType : "";

			String requestURI = webTarget.getUri().toString();

			logger.info(REQUEST_URL, requestType, requestURI);
			logger.info(REQUEST_DECODED_URL, requestType, UriUtils.decodeUrl(requestURI));

			Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);

			val awtHeader = awtService.getAWTHeader("POST", getRelativeURL(requestURI));
			invocationBuilder.header(awtHeader.getKey(), awtHeader.getValue());

			// add headers to request
			if (headers != null) {
				headers.keySet().stream().forEach(key -> invocationBuilder.header(key, headers.get(key)));
			}

			Response response = null;

			if (bodyParams != null) {
				StringBuilder sbBodyParams = new StringBuilder();
				Pair<String, String> bodyParam = null;

				for (int i = 0; i < bodyParams.size(); i++) {
					bodyParam = bodyParams.get(i);

					sbBodyParams.append(i > 0 ? amp : "");
					sbBodyParams.append(String.format(parameterNameWithValuePattern, bodyParam.getFirst(), UriUtils.encodeQueryParam(bodyParam.getSecond())));
				}

				String requestBody = sbBodyParams.toString();

				logger.info(requestBodyLogPattern, requestType, requestBody);
				logger.info(requestDecodedBodyLogPattern, requestType, UriUtils.decodeUrl(requestBody));

				response = invocationBuilder.post(Entity.entity(requestBody, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
			} else {
				if (bodyString != null) {
					response = invocationBuilder.post(Entity.entity(bodyString, MediaType.APPLICATION_JSON_TYPE));
				} else {
					response = invocationBuilder.post(Entity.entity("", MediaType.APPLICATION_JSON_TYPE));
				}
			}

			// try to read response
			jsonResponse = response.readEntity(String.class);

			if (!StringUtils.isNullOrBlank(jsonResponse)) {
				logger.trace(RESPONSE_ACCEPTED_FROM_HOST, requestType, StringUtils.topNLines(jsonResponse, responseNumberOfLinestoLog));

			} else {
				logger.info(EMPTY_RESPONSE_ACCEPTED, requestType);
			}

			responseStatus = response.getStatus();

			if (responseStatus != TAConstants.httpCode200) {
				logger.info(REQUEST_RESPONSE_STATUS, requestType, responseStatus);
				throw new Exception(RESPONSE_STATUS + responseStatus);
			}

		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.RESTTextQueryExecutionError));
		}

		return jsonResponse;
	}


	/**
	 * Executes a GET request to remote URL for specific application request.
	 *
	 * @param requestUrl  URL to invoke the request
	 * @param requestType an application request type
	 * @param queryPaths  parts to be joined to base url
	 * @param queryParams parameters to be added to query string of request
	 * @param bodyParams  parameters to be added to body of request
	 * @param bodyString  string to be placed in body of request
	 * @param headers     headers to be added to request
	 * @param callback    call to be invoked when the request is completed
	 * @return a textual response of REST request
	 */
	public Future<Response> executePostRequestAsync(String requestType, String requestUrl, List<String> queryPaths, MultivaluedStringMap queryParams, List<Pair<String, String>> bodyParams, String bodyString, Map<String, String> headers, InvocationCallback<Response> callback) {
		Future<Response> responseFuture = null;
		int responseStatus = -1;

		try {
			WebTarget webTarget = this.createRequestWebTarget(requestUrl, queryPaths, queryParams);

			requestType = requestType != null ? requestType : "";

			String requestURI = webTarget.getUri().toString();

			logger.info(REQUEST_URL, requestType, requestURI);
			logger.info(REQUEST_DECODED_URL, requestType, UriUtils.decodeUrl(requestURI));

			Invocation.Builder invocationBuilder = webTarget.request();
			invocationBuilder.header(contentType, this.jsonContentType);

			val awtHeader = awtService.getAWTHeader("POST", getRelativeURL(requestURI));
			invocationBuilder.header(awtHeader.getKey(), awtHeader.getValue());

			// add headers to request
			if (headers != null) {
				headers.keySet().stream().forEach(key -> invocationBuilder.header(key, headers.get(key)));
			}

			if (bodyParams != null) {
				StringBuilder sbBodyParams = new StringBuilder();
				Pair<String, String> bodyParam = null;

				for (int i = 0; i < bodyParams.size(); i++) {
					bodyParam = bodyParams.get(i);

					sbBodyParams.append(i > 0 ? amp : "");
					sbBodyParams.append(String.format(this.parameterNameWithValuePattern, bodyParam.getFirst(), UriUtils.encodeQueryParam(bodyParam.getSecond())));
				}

				String requestBody = sbBodyParams.toString();

				logger.debug(requestBodyLogPattern, requestType, requestBody);
				logger.debug(requestDecodedBodyLogPattern, requestType, UriUtils.decodeUrl(requestBody));

				// invoke Asynchronous Client  call and supply callback instance
				responseFuture = invocationBuilder.async().post(Entity.entity(requestBody, MediaType.APPLICATION_FORM_URLENCODED_TYPE), callback);
			} else {
				// invoke Asynchronous Client  call and supply callback instance
				if (bodyString != null) {
					logger.debug(requestBodyLogPattern, requestType, bodyString);

					responseFuture = invocationBuilder.async().post(Entity.entity(bodyString, MediaType.APPLICATION_JSON_TYPE), callback);
				} else {
					logger.debug("No string was supplied as body to POST request {}", requestType, bodyString);

					responseFuture = invocationBuilder.async().post(Entity.entity("", MediaType.APPLICATION_JSON_TYPE), callback);
				}
			}
		} catch (Exception ex) {
			Throwables.propagateIfInstanceOf(ex, TextQueryExecutionException.class);
			Throwables.propagate(new TextQueryExecutionException(ex, TextQueryExecutionErrorCode.RESTTextQueryExecutionError));
		}

		return responseFuture;
	}

	/**
	 * Generates a request URL.
	 *
	 * @param requestBaseUrl base URL
	 * @param queryPaths     path segments.
	 * @param queryParams    query params
	 * @param encode         should URL be encoded or not
	 * @return a url generated by REST client.
	 */
	public String getRequestUrl(String requestBaseUrl, List<String> queryPaths, MultivaluedStringMap queryParams, Boolean encode) {
		WebTarget webTarget = this.createRequestWebTarget(requestBaseUrl, queryPaths, queryParams);

		// generate URL
		String requestUrl = webTarget.getUri().toString();

		return encode ? requestUrl : UriUtils.decodeUrl(requestUrl);
	}

	private WebTarget createRequestWebTarget(String requestUrl, List<String> queryPaths, MultivaluedStringMap queryParams) {
		WebTarget webTarget = this.client.target(requestUrl);

		// add path to URL
		if (queryPaths != null) {
			for (String path : queryPaths) {
				webTarget = webTarget.path(UriUtils.encodePathSegment(path));
			}
		}

		// add query parameters
		if (queryParams != null) {
			for (String queryParamKey : queryParams.keyList()) {
				Collection<String> queryParamsValues = queryParams.get(queryParamKey);
				if (queryParamKey != null) {
					for (String queryParamsValue : queryParamsValues) {
						if (queryParamsValue != null) {
							webTarget = webTarget.queryParam(queryParamKey, UriUtils.encodeQueryParam(queryParamsValue));
						}
					}
				}
			}
		}

		return webTarget;
	}

	private PoolingHttpClientConnectionManager createHttpConnectionManager(ApplicationConfiguration appConfig) {
		PoolingHttpClientConnectionManager connectionManager = null;

		try {
			if (appConfig.getIgnoreSSLCertificateErrors()) {
				X509TrustManager tm = new X509TrustManager() {

					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return null;
					}

					@Override
					public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1) throws java.security.cert.CertificateException {

					}

					@Override
					public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1) throws java.security.cert.CertificateException {

					}
				};

				SSLContext ctx = SSLContext.getInstance("SSL");
				ctx.init(null, new TrustManager[] { tm }, null);

				SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(ctx, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

				Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create().register("https", sslConnectionFactory).build();
				connectionManager = new PoolingHttpClientConnectionManager(registry);
			} else {
				connectionManager = new PoolingHttpClientConnectionManager();
			}

			connectionManager.setMaxTotal(appConfig.getHttpClientMaxConnectionsTotal());
            connectionManager.setDefaultMaxPerRoute(appConfig.getHttpClientMaxConnectionsPerRoute());
		} catch (Exception ex) {
			logger.error("Failed to create HttpClient Connection manager", ex);
		}

		return connectionManager;
	}

	private String getRelativeURL(String requestURI) {
		URIBuilder uriBuilder = null;
		try {
			uriBuilder = new URIBuilder(requestURI);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		String uri = uriBuilder.getPath();
		return uri;
	}


	@Override
	public void destroy() throws Exception {
		try {
			logger.debug("Rest Data Access destroy method. Disposing Jersey client");
			if (this.client != null) {
				this.client.close();
			}
		} catch (Exception ex) {
			logger.error("Exception disposing Jersey REST client", ex);
		}
	}
}
