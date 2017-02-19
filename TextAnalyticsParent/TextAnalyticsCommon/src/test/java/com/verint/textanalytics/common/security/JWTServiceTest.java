package com.verint.textanalytics.common.security;

import com.verint.authorization.services.JWTTokenService;
import com.verint.textanalytics.common.configuration.FilePathType;
import com.verint.textanalytics.common.utils.FileUtils;
import lombok.val;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Map.Entry;

import static org.junit.Assert.assertNotNull;

/**
 * @author imor
 */
public class JWTServiceTest {

	@Test
	public void createHeaderTokenTest() {
		val jwtService = new JWTService();
		jwtService.setJwtTokenService(new JWTTokenService());
		String hashmapSerFileName = "hashmap.ser";
		String hashmapSerFilePath;
		URL resourceUrl = this.getResourceURL(hashmapSerFileName);
		hashmapSerFilePath = resourceUrl.getPath();

		ModelEditorSecretFile modelEditorSecretFile = new ModelEditorSecretFile(hashmapSerFilePath, FilePathType.AbsolutePath);
		jwtService.setModelEditorSecretFile(modelEditorSecretFile);

		jwtService.initIt();

		Entry<java.lang.String, java.lang.String> res = null;
		try {
			res = jwtService.createHeaderToken("t1", "applicationId", "userId", "jsonData");
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertNotNull(res);
	}

	protected final URL getResourceURL(String resource) {
		return Thread.currentThread().getContextClassLoader().getResource(resource);
	}

	protected String getResourceAsString(String resourcePath) throws IOException {
		URL resourceUrl = this.getResourceURL(resourcePath);

		return FileUtils.getResourceTextData(resourceUrl);
	}
}
