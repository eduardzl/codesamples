package com.verint.textanalytics.web.portal.restinfra;

import com.google.common.base.Throwables;
import com.verint.textanalytics.bl.security.AuthorizationManager;
import com.verint.textanalytics.common.constants.TAConstants;
import com.verint.textanalytics.common.exceptions.AuthorizationException;
import com.verint.textanalytics.common.exceptions.UserNotLoggedInException;
import com.verint.textanalytics.common.security.MethodSecurityContext;
import com.verint.textanalytics.common.utils.CollectionUtils;
import com.verint.textanalytics.common.utils.JSONUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.InputStream;
import java.util.Map;



/**
 * @author NShunevich
 *
 */
@Provider
@Priority(TAConstants.RequestFiltersPriority.authorizationFilter)
public class AuthorizationFilter implements ContainerRequestFilter {

	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	private AuthorizationManager authorizationManager;

	@Autowired
	private PrivilegesAnnotationExtractor annotationExtractor;

	/**
	 * Constructor.
	 */
	public AuthorizationFilter() {
		logger.debug("AuthorizationFilter constructore invoked");
	}

	@Override
	public void filter(ContainerRequestContext requestContext) {
		logger.entry();

		// Retrieve class name and method Name from URL
		String urlPath = requestContext.getUriInfo().getAbsolutePath().toString();

		try {
			RequestMethodInfo requestInfo = new RequestMethodInfo();

			requestInfo.parseUrl(urlPath);

			// Get Method's Privileges Annotation from Cache or Reflection
			MethodSecurityContext methodPrivileges = annotationExtractor.getMethodSecurityContext(requestInfo);

			// check if request has cookies
			Map<String, Cookie> requestCookies = requestContext.getCookies();


			String impact360AuthToken = null;
			// so locate cookie with Impact360 Authentication token
			if (requestCookies.get(TAConstants.i360FoundationTokenHeader) != null) {
				impact360AuthToken = requestCookies.get(TAConstants.i360FoundationTokenHeader).getValue();
			}

			// Check if privileges are defined
			if (!CollectionUtils.isEmpty(methodPrivileges.getRequiredAllPrivileges()) || !CollectionUtils.isEmpty(methodPrivileges.getRequiredAnyPrivileges())) {

				if (!authorizationManager.checkAccess(impact360AuthToken, methodPrivileges)) {
					logger.debug("User : {}, does not have required permissions to access the method: {}", impact360AuthToken, requestInfo.getMethodName());

					requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
					                                 .entity("{\"message\": \"User cannot access the resource.\"}").build());
				}
			}

			if (methodPrivileges.isChannelAuthenticationRequired()) {
				logger.debug("checking user : {}, channels permissions to access the method : {}", impact360AuthToken, requestInfo.getMethodName());

				try {
					String json = IOUtils.toString(requestContext.getEntityStream());

					// check channel permission
					JSONTokener tokener = new JSONTokener(json);
					JSONObject root = new JSONObject(tokener);
					String channel = JSONUtils.getString("Channel", root, "");

					logger.debug("AuthorizationFilter: checking user : {}, channels permissions , the access channel : {}", impact360AuthToken, channel);

					InputStream in = null;

					if (!authorizationManager.checkChannelPermissions(impact360AuthToken, channel)) {
						logger.error("AuthorizationFilter: Authorization failed for url {}.,User is not authorized to access the channel : {} ", urlPath, channel);

						// replace input stream for Jersey as we've already read it
						in = IOUtils.toInputStream(json);
						requestContext.setEntityStream(in);

						requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).entity("{\"message\":\"Unauthorized Project for this user.\"}").build());

					} else {
						logger.trace("AuthorizationFilter:User :{} , has the authorization to user channel :{} ", impact360AuthToken, channel);
					}

					// replace input stream for Jersey as we've already read it
					if (in == null) {
						in = IOUtils.toInputStream(json);
						requestContext.setEntityStream(in);
					}
				} catch (UserNotLoggedInException e) {
					logger.error("AuthorizationFilter: Authorization failed for url {}. User Isn't LogedIn, go Back To Login Page. Error - {}", urlPath, e);

					Throwables.propagateIfInstanceOf(e, AuthorizationException.class);

					requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\":\"User Not Logedin. Please Login to the system\"}").build());

				} catch (Exception ex) {
					logger.error("AuthorizationFilter: Authorization failed for url {}. failed to extract channel from requst. Error - {} ", urlPath, ex);

					Throwables.propagateIfInstanceOf(ex, AuthorizationException.class);

					requestContext.abortWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"message\":\"Unauthorized Project for this user.\"}").build());

				}
			}
		} catch (UserNotLoggedInException e) {
			logger.error("AuthorizationFilter: Authorization failed for url {}. User Isn't LogedIn, go Back To Login Page", urlPath);

			Throwables.propagateIfInstanceOf(e, AuthorizationException.class);

			requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
			                                 .entity("{\"message\":\"User Not Logedin. Please Login to the system\"}").build());

		} catch (Exception e) {
			logger.error("AuthorizationFilter: Authorization failed for url {}. Error - {}", urlPath, e);

			Throwables.propagateIfInstanceOf(e, AuthorizationException.class);

			requestContext.abortWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
			                                 .entity("{\"message\":\"User cannot access the resource.\"}").build());
		}

		logger.exit();
	}
}