/**
 * 
 */
package com.verint.textanalytics.common.configuration;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.verint.textanalytics.common.exceptions.ConfigurationException;

public class ConfigurationManagerTest {

	static String engineServiceBaseUrlConfiguration = "com.verint.textanalytics.common.configuration.ApplicationConfiguration.darwinTextEngineServiceBaseUrl=";
	static String engineServiceBaseUrlConfigurationValue = "http://10.165.140.102:8983";
	static String repositoryServiceBaseurlConfiguration = "com.verint.textanalytics.common.configuration.ApplicationConfiguration.darwinTextRepositoryServiceBaseUrl=";
	static String repositoryServiceBaseurlConfigurationValue = "http://10.165.140.102:8983/solr";

	static String application = "application";

	static String environment = "environment";
	static String local = "local";
	static String search = "";

	static String properties = "properties";

	static String wrongApplication = "wrongApplication";
	static String wrongEnvironment = "wrongEnvironment";
	static String wrongLocal = "wrongLocal";

	static ConfigurationFile applicationPropertiesConfigFile;
	static ConfigurationFile environmentPropertiesConfigFile;
	static ConfigurationFile localPropertiesConfigFile;

	static ConfigurationFile wrongApplicationPropertiesConfigFile;
	static ConfigurationFile wrongEnvironmentPropertiesConfigFile;
	static ConfigurationFile wrongLocalPropertiesConfigFile;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		createPropertiesFiles();

		createWrongPropertiesFiles();
	}

	private static void createWrongPropertiesFiles() throws IOException {

		// create properties file object
		wrongApplicationPropertiesConfigFile = new ConfigurationFile(getFilePath(wrongApplication + "." + properties), FilePathType.AbsolutePath,
		        ConfigurationSource.PropertiesConfiguration, true);

		wrongEnvironmentPropertiesConfigFile = new ConfigurationFile(getFilePath(wrongEnvironment + "." + properties), FilePathType.AbsolutePath,
		        ConfigurationSource.PropertiesConfiguration, true);

		wrongLocalPropertiesConfigFile = new ConfigurationFile(getFilePath(wrongLocal + "." + properties), FilePathType.AbsolutePath, ConfigurationSource.PropertiesConfiguration,
		        false);
	}

	private static void createPropertiesFiles() throws IOException {

		// create properties file object
		applicationPropertiesConfigFile = new ConfigurationFile(getFilePath(application + "." + properties), FilePathType.AbsolutePath,
		        ConfigurationSource.PropertiesConfiguration, true);

		environmentPropertiesConfigFile = new ConfigurationFile(getFilePath(environment + "." + properties), FilePathType.AbsolutePath,
		        ConfigurationSource.PropertiesConfiguration, true);

		localPropertiesConfigFile = new ConfigurationFile(getFilePath(local + "." + properties), FilePathType.AbsolutePath, ConfigurationSource.PropertiesConfiguration, false);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testLocalConfigurationFileWithAll() throws Exception {

		ConfigurationManager configurationManager = new ConfigurationManager(applicationPropertiesConfigFile, environmentPropertiesConfigFile, localPropertiesConfigFile);

		ApplicationConfiguration applicationConfiguration = configurationManager.getApplicationConfiguration();

		assertEquals("Local Configuration File - Engine Service Base Url", engineServiceBaseUrlConfigurationValue + local + search,
		        applicationConfiguration.getDarwinTextEngineServiceBaseUrl());

	}

	@Test
	public void testLocalConfigurationFileWithEnvitonmentAndLocal() throws Exception {
		ConfigurationManager configurationManager = new ConfigurationManager(null, environmentPropertiesConfigFile, localPropertiesConfigFile);

		ApplicationConfiguration applicationConfiguration = configurationManager.getApplicationConfiguration();

		assertEquals("Local Configuration File - Engine Service Base Url", engineServiceBaseUrlConfigurationValue + local + search,
		        applicationConfiguration.getDarwinTextEngineServiceBaseUrl());

	}

	@Test
	public void testLocalConfigurationFileWithLocal() throws Exception {
		ConfigurationManager configurationManager = new ConfigurationManager(null, null, localPropertiesConfigFile);

		ApplicationConfiguration applicationConfiguration = configurationManager.getApplicationConfiguration();

		assertEquals("Local Configuration File - Engine Service Base Url", engineServiceBaseUrlConfigurationValue + local + search,
		        applicationConfiguration.getDarwinTextEngineServiceBaseUrl());

	}

	@Test
	public void testLocalConfigurationFileWithLocalWithAppAndLocal() throws Exception {
		ConfigurationManager configurationManager = new ConfigurationManager(applicationPropertiesConfigFile, null, localPropertiesConfigFile);

		ApplicationConfiguration applicationConfiguration = configurationManager.getApplicationConfiguration();

		assertEquals("Local Configuration File - Engine Service Base Url", engineServiceBaseUrlConfigurationValue + local + search,
		        applicationConfiguration.getDarwinTextEngineServiceBaseUrl());

	}

	@Test
	public void testEnvironmentConfigurationFileWithAppAndEnv() throws Exception {

		ConfigurationManager configurationManager = new ConfigurationManager(applicationPropertiesConfigFile, environmentPropertiesConfigFile, null);

		ApplicationConfiguration applicationConfiguration = configurationManager.getApplicationConfiguration();

		assertEquals("Environment Configuration File - Engine Service Base Url", engineServiceBaseUrlConfigurationValue + environment + search,
		        applicationConfiguration.getDarwinTextEngineServiceBaseUrl());

	}

	@Test
	public void testEnvironmentConfigurationFile() throws Exception {
		ConfigurationManager configurationManager = new ConfigurationManager(null, environmentPropertiesConfigFile, null);

		ApplicationConfiguration applicationConfiguration = configurationManager.getApplicationConfiguration();

		assertEquals("Environment Configuration File - Engine Service Base Url", engineServiceBaseUrlConfigurationValue + environment + search,
		        applicationConfiguration.getDarwinTextEngineServiceBaseUrl());

	}

	@Test
	public void testApplicationConfigurationFile() throws Exception {

		ConfigurationManager configurationManager = new ConfigurationManager(applicationPropertiesConfigFile, null, null);

		ApplicationConfiguration applicationConfiguration = configurationManager.getApplicationConfiguration();

		assertEquals("Application Configuration File - Engine Service Base Url", engineServiceBaseUrlConfigurationValue + application + search,
		        applicationConfiguration.getDarwinTextEngineServiceBaseUrl());

	}

	@Test
	public void testWrongLocalConfigurationFileWithAll() throws Exception {

		ConfigurationManager configurationManager = new ConfigurationManager(applicationPropertiesConfigFile, environmentPropertiesConfigFile, wrongLocalPropertiesConfigFile);

		ApplicationConfiguration applicationConfiguration = configurationManager.getApplicationConfiguration();

		assertEquals("Environment Configuration File - Engine Service Base Url", engineServiceBaseUrlConfigurationValue + environment + search,
		        applicationConfiguration.getDarwinTextEngineServiceBaseUrl());

	}

	@Test
	public void testWrongLocalConfigurationFileWithEnv() throws Exception {

		ConfigurationManager configurationManager = new ConfigurationManager(null, environmentPropertiesConfigFile, wrongLocalPropertiesConfigFile);

		ApplicationConfiguration applicationConfiguration = configurationManager.getApplicationConfiguration();

		assertEquals("Local Configuration File - Engine Service Base Url", engineServiceBaseUrlConfigurationValue + environment + search,
		        applicationConfiguration.getDarwinTextEngineServiceBaseUrl());

	}

	@Test
	public void testWrongEnvironmentConfigurationFileWithAppAndEnv() throws Exception {
		ConfigurationManager configurationManager = new ConfigurationManager(applicationPropertiesConfigFile, wrongEnvironmentPropertiesConfigFile, null);

		ApplicationConfiguration applicationConfiguration = configurationManager.getApplicationConfiguration();

		assertEquals("Environment Configuration File - Engine Service Base Url", engineServiceBaseUrlConfigurationValue + application + search,
		        applicationConfiguration.getDarwinTextEngineServiceBaseUrl());

	}

	@Test
	public void testWrongEnvironmentConfigurationFileWithAll() throws Exception {

		ConfigurationManager configurationManager = new ConfigurationManager(applicationPropertiesConfigFile, wrongEnvironmentPropertiesConfigFile, localPropertiesConfigFile);

		ApplicationConfiguration applicationConfiguration = configurationManager.getApplicationConfiguration();

		assertEquals("Local Configuration File - Engine Service Base Url", engineServiceBaseUrlConfigurationValue + local + search,
		        applicationConfiguration.getDarwinTextEngineServiceBaseUrl());

	}

	@Test
	public void testWrongApplicationConfigurationFileWithAppAndEnv() throws Exception {

		ConfigurationManager configurationManager = new ConfigurationManager(wrongApplicationPropertiesConfigFile, environmentPropertiesConfigFile, null);

		ApplicationConfiguration applicationConfiguration = configurationManager.getApplicationConfiguration();

		assertEquals("Application Configuration File - Engine Service Base Url", engineServiceBaseUrlConfigurationValue + environment + search,
		        applicationConfiguration.getDarwinTextEngineServiceBaseUrl());

	}

	@Test
	public void testWrongApplicationConfigurationFileWithAppAndLocal() throws Exception {

		ConfigurationManager configurationManager = new ConfigurationManager(wrongApplicationPropertiesConfigFile, null, localPropertiesConfigFile);

		ApplicationConfiguration applicationConfiguration = configurationManager.getApplicationConfiguration();

		assertEquals("Application Configuration File - Engine Service Base Url", engineServiceBaseUrlConfigurationValue + local + search,
		        applicationConfiguration.getDarwinTextEngineServiceBaseUrl());

	}

	@Test
	public void testWrongApplicationConfigurationFileWithAll() throws Exception {

		ConfigurationManager configurationManager = new ConfigurationManager(wrongApplicationPropertiesConfigFile, environmentPropertiesConfigFile, localPropertiesConfigFile);

		ApplicationConfiguration applicationConfiguration = configurationManager.getApplicationConfiguration();

		assertEquals("Application Configuration File - Engine Service Base Url", engineServiceBaseUrlConfigurationValue + local + search,
		        applicationConfiguration.getDarwinTextEngineServiceBaseUrl());

	}

	// Exception tests

	@Test(expected = ConfigurationException.class)
	public void testApplicationConfigurationException() throws Exception {

		ConfigurationManager configurationManager = new ConfigurationManager(wrongApplicationPropertiesConfigFile, wrongEnvironmentPropertiesConfigFile,
		        wrongLocalPropertiesConfigFile);

		configurationManager.getApplicationConfiguration();
	}

	private final static URL getResourceURL(String resource) {
		return Thread.currentThread()
		             .getContextClassLoader()
		             .getResource(resource);
	}

	private static String getFilePath(String resourcePath) throws IOException {

		URL resourceUrl = getResourceURL(resourcePath);

		return resourceUrl.getPath()
		                  .substring(1);
	}
}
