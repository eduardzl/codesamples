package com.verint.textanalytics.common.security;

import com.verint.authorization.services.JWTTokenService;
import com.verint.textanalytics.common.exceptions.JWTServiceErrorCode;
import com.verint.textanalytics.common.exceptions.JWTServiceException;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map.Entry;

/**
 * @author imor
 */
public class JWTService {

	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	@Setter
	private ModelEditorSecretFile modelEditorSecretFile;

	@Autowired
	@Setter
	private JWTTokenService jwtTokenService;

	/**
	 * JWTService Constructor.
	 */
	public JWTService() {
	}

	/**
	 * initIt.
	 */
	public void initIt() {
		jwtTokenService.setSecretStoragePath(modelEditorSecretFile.resolveFullPath());
	}

	/**
	 * creates a signed JWT token with json data inside.
	 *
	 * @param tenantId      - tenant id of current tenant.
	 * @param applicationId - from which application this token is being created/sent.
	 * @param userId        - current user id for additional validation.
	 * @param jsonData      - data to be passed.
	 * @return Signed token as a string
	 * @throws Exception - exception
	 */
	public Entry<String, String> createHeaderToken(String tenantId, String applicationId, String userId, String jsonData) {

		Entry<String, String> actual = null;
		try {
			logger.debug("Create Header Token: tenantId={}, applicationId={}, userId={}, jsonData={}", tenantId, applicationId, userId, jsonData);
			actual = jwtTokenService.createHeaderToken(tenantId, applicationId, userId, jsonData);
			logger.debug("Create Header Token Success");
		} catch (Exception e) {
			logger.debug("Create Header Token Fail");
			//TODO - idan - see which exceptions are returns ...
			throw new JWTServiceException(e, JWTServiceErrorCode.CreateHeaderTokenError);
		}

		return actual;
	}
}
