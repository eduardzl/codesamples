package com.verint.textanalytics.web.portal.restinfra;

import java.lang.reflect.Method;

import lombok.val;
import propel.core.utils.*;
import org.apache.logging.log4j.*;
import org.springframework.cache.annotation.Cacheable;
import com.verint.textanalytics.common.security.ChannelAuthorizationNotRequired;
import com.verint.textanalytics.common.security.MethodSecurityContext;
import com.verint.textanalytics.common.security.OperationPrivelegesAnnotation;
import com.verint.textanalytics.common.constants.*;

/**
 * Extracts Authorization Annotations extractor.
 */
public class PrivilegesAnnotationExtractor {
	private Logger logger = LogManager.getLogger(this.getClass());

	/**
	 * Constructor.
	 */
	public PrivilegesAnnotationExtractor() {

	}

	/***
	 * Extracts the authorization annotations from the method. Required
	 * privileges and if channel check is needed.
	 * @param requestedMethodInfo
	 *            information of method which serves REST request
	 * @return object that has all the extracted data.
	 * @throws ClassNotFoundException
	 *             exception
	 */
	@Cacheable(value = "TextAnalyticsPrivilegesCache", key = "#requestedMethodInfo.getMethodName()")
	public MethodSecurityContext getMethodSecurityContext(RequestMethodInfo requestedMethodInfo) throws ClassNotFoundException {

		try {
			logger.trace("Retreiving method security contaxt for method :{} from class : {}", requestedMethodInfo.getMethodName(), requestedMethodInfo.getClassName());

			Class<?> cls = Class.forName(TAConstants.restServicesPackageName + requestedMethodInfo.getClassName());

			Method method = ReflectionUtils.getMethod(cls, requestedMethodInfo.getMethodName(), true);
			if (method != null) {

				MethodSecurityContext result = new MethodSecurityContext();

				// get method OperationPriveleges annotations
				OperationPrivelegesAnnotation methodPrivileges = method.getAnnotation(OperationPrivelegesAnnotation.class);
				if (methodPrivileges != null) {
					result.setRequiredAllPrivileges(methodPrivileges.requiredAllPrivileges());
					result.setRequiredAnyPrivileges(methodPrivileges.requiredAnyOfPrivileges());
				} else {

					logger.debug("Priveleges annotation was missing ,for method :{} from class : {}", requestedMethodInfo.getMethodName(), requestedMethodInfo.getClassName());
				}

				ChannelAuthorizationNotRequired channelAnnotation = method.getAnnotation(ChannelAuthorizationNotRequired.class);

				if (channelAnnotation == null) {
					result.setChannelAuthenticationRequired(true);
				} else {
					result.setChannelAuthenticationRequired(false);
				}

				return result;

			} else {
				logger.error("Method {} not found in class {}", requestedMethodInfo.getMethodName(), cls.getName());
			}

		} catch (ClassNotFoundException ex) {
			logger.error("Class Not found , class : {}. Error - {}", requestedMethodInfo.getClassName(), ex);

		} catch (Exception e) {
			logger.error("Error while extracting SecurityContext . Error - {}", e);
		}

		return null;
	}

}
