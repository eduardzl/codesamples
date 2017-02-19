package com.verint.textanalytics.dal.modelEditor;

import static org.junit.Assert.*;

import java.io.IOException;

import lombok.val;

import org.junit.Before;
import org.junit.Test;

import com.verint.textanalytics.dal.darwin.BaseTest;

/***
 * 
 * @author imor
 *
 */
public class ConfigServiceResponseConvertorTest extends BaseTest {

	protected static String s_ModelsTreeResourcePath;

	public ConfigServiceResponseConvertorTest() throws Exception {
		s_ModelsTreeResourcePath = "ModelsTree.txt";
	}

	@Test
	public void convertModelsTreeResponse_regularFlow() throws IOException {

		val response = this.getResourceAsString(s_ModelsTreeResourcePath);

		val configServiceResponseConvertor = new ConfigServiceResponseConvertor();

		val result = configServiceResponseConvertor.convertModelsTreeResponse(response);

		assertNotNull(result);
		assertNotNull(result.getLanguages());
		assertEquals(2, result.getLanguages().size());

		int i = 0;
		assertEquals("en", result.getLanguages().get(i).getName());
		assertEquals(4, result.getLanguages().get(i).getDomains().size());
		assertEquals("customer_experience", result.getLanguages().get(i).getDomains().get(0).getName());
		assertEquals("telco", result.getLanguages().get(i).getDomains().get(1).getName());
		assertEquals("travel_and_leisure", result.getLanguages().get(i).getDomains().get(2).getName());
		assertEquals("banking", result.getLanguages().get(i).getDomains().get(3).getName());

		i = 1;
		assertEquals("sp", result.getLanguages().get(i).getName());
		assertEquals(1, result.getLanguages().get(i).getDomains().size());
		assertEquals("customer_experience", result.getLanguages().get(i).getDomains().get(0).getName());
	}
}
