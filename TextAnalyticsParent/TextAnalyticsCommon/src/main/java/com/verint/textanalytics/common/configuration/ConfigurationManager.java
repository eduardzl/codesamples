package com.verint.textanalytics.common.configuration;

import com.verint.textanalytics.common.exceptions.ConfigurationErrorCode;
import com.verint.textanalytics.common.exceptions.ConfigurationException;
import com.verint.textanalytics.common.utils.ExceptionUtils;
import lombok.Getter;
import lombok.val;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Represent the Application Configuration Manager.
 * 
 * @author imor
 *
 */
public class ConfigurationManager {
	private Logger logger = LogManager.getLogger(this.getClass());

	private ApplicationConfiguration applicationConfiguration;

	@Getter
	private final String tenantPrifix = "textstore";
	@Getter
	private final String channelPrifix = "textproject";

	/**
	 * @param applicationPropertiesConfigFile
	 *            - Application level Properties Configuration File
	 * @param environmentPropertiesConfigFile
	 *            - Environment level Properties Configuration File
	 * @param localPropertiesConfigFile
	 *            - Local level Properties Configuration File
	 */
	public ConfigurationManager(ConfigurationFile applicationPropertiesConfigFile, ConfigurationFile environmentPropertiesConfigFile, ConfigurationFile localPropertiesConfigFile) {

		try {

			// create new application configuration object
			this.applicationConfiguration = new ApplicationConfiguration();

			// update configuration object from application Properties
			// Configuration File
			this.buildConfiguration(applicationPropertiesConfigFile);

			// update configuration object from environment Properties
			// Configuration File
			this.buildConfiguration(environmentPropertiesConfigFile);

			// update configuration object from local Properties Configuration
			// File
			this.buildConfiguration(localPropertiesConfigFile);

			// validate configuration object
			if (!this.applicationConfiguration.validate()) {
				throw new ApplicationConfigurationNotValidException(this.applicationConfiguration.getInvalidFields());
			}

			logger.debug("Build Application Configuration finish successfully:: [{}]", this.applicationConfiguration.toString());

		} catch (Exception e) {
			logger.error("Exception in ConfigurationManager creation. Error - {}", ExceptionUtils.getStackTrace(e));
		}
	}

	/**
	 * @param configurationFile
	 *            Configuration file object that represent the configuration
	 *            file
	 */
	private void buildConfiguration(ConfigurationFile configurationFile) {

		if (configurationFile == null) {
			logger.warn("Configuration File is null");
			return;
		}

		String configurationFilePath = configurationFile.resolveFullPath();

		CompositeConfiguration config = new CompositeConfiguration();
		config.addConfiguration(new SystemConfiguration());

		// read data from configuration file
		try {
			config.addConfiguration(new PropertiesConfiguration(configurationFilePath));
			config.setThrowExceptionOnMissing(true);
		} catch (Exception e) {
			if (configurationFile.isMandatory()) {
				logger.error("Could not read Configuration File  {}. Error - {}", configurationFilePath, ExceptionUtils.getStackTrace(e));
			} else {
				logger.info("Could not read Configuration File: {}", configurationFilePath);
			}
			return;
		}

		// update applicationConfiguration object from configuration object
		List<Field> privateFields = getAllPrivateFields(ApplicationConfiguration.class);
		List<Method> setters = findSetters(ApplicationConfiguration.class);
		String className = ApplicationConfiguration.class.getName();
		String stringValue = null;
		Integer intValue = null;
		Boolean booleanValue = null;
		Double doubleValue = null;
		String privateFieldName;
		String fullPrivateFieldName;

		String startWithString;
		String startWithStringUpperString;
		String privateFieldNameWithUpperCase;

		for (Field privateField : privateFields) {
			privateFieldName = privateField.getName();
			fullPrivateFieldName = String.format("%s.%s", className, privateFieldName);

			Class<?> c = privateField.getType();

			startWithString = privateFieldName.substring(0, 1);
			startWithStringUpperString = startWithString.toUpperCase();
			privateFieldNameWithUpperCase = startWithStringUpperString + privateFieldName.substring(1);

			// gets the value from the application configuration file
			try {

				String setApplicationConfigurationFieldInfoFormat = "Set Application Configuration Field:: [{} = {}]";

				for (Method setter : setters) {
					if (setter.getName().endsWith(privateFieldNameWithUpperCase)) {
						if (c == String.class) {
							stringValue = config.getString(fullPrivateFieldName);
							setter.invoke(this.applicationConfiguration, stringValue);

							logger.info(setApplicationConfigurationFieldInfoFormat, privateFieldName, stringValue);
						} else if (c == int.class || c == Integer.class) {

							intValue = config.getInt(fullPrivateFieldName);
							setter.invoke(this.applicationConfiguration, intValue);

							logger.info("Set Application Configuration Field:: [{} =  {}]", privateFieldName, intValue);
						} else if (c.equals(boolean.class) || c.equals(Boolean.class)) {
							booleanValue = config.getBoolean(fullPrivateFieldName);
							setter.invoke(this.applicationConfiguration, booleanValue);

							logger.info(setApplicationConfigurationFieldInfoFormat, privateFieldName, booleanValue);
						} else if (c == double.class) {
							doubleValue = config.getDouble(fullPrivateFieldName);
							setter.invoke(this.applicationConfiguration, doubleValue);

							logger.info(setApplicationConfigurationFieldInfoFormat, privateFieldName, doubleValue);
						} else if (c == List.class) {
							List<Object> lstValue = config.getList(fullPrivateFieldName);
							setter.invoke(this.applicationConfiguration, lstValue);

							logger.info(setApplicationConfigurationFieldInfoFormat, privateFieldName, lstValue.toString());
						} else {
							logger.error("Unknown Member Type - Field:: [{}]", privateFieldName);
						}
					}
				}
			} catch (NoSuchElementException e) {
				logger.debug("Try to read from Configuration file but No Such Element:: [{}]", fullPrivateFieldName);
			} catch (ConversionException e) {
				logger.error("Conversion Exception when trying to read Element:: [{}] .Error - {}", fullPrivateFieldName, ExceptionUtils.getStackTrace(e));
			} catch (Exception e) {
				logger.error("Fail on Set Application Configuration Field:: [{}]. Error - {}", privateFieldName, ExceptionUtils.getStackTrace(e));
			}
		}
	}

	private List<Field> getAllPrivateFields(Class<?> c) {
		List<Field> privateFields = new ArrayList<Field>();
		Field[] allFields = c.getDeclaredFields();
		for (Field field : allFields) {
			if (Modifier.isPrivate(field.getModifiers())) {
				privateFields.add(field);
			}
		}
		return privateFields;
	}

	static List<Method> findSetters(Class<?> c) {
		List<Method> setters = new ArrayList<Method>();
		Method[] methods = c.getDeclaredMethods();

		for (Method method : methods) {
			if (isSetter(method)) {
				setters.add(method);
			}
		}
		return setters;
	}

	private static boolean isSetter(Method method) {
		return Modifier.isPublic(method.getModifiers()) && method.getParameterTypes().length == 1 && method.getName().matches("^set[A-Z].*");
	}

	/**
	 * @return the ApplicationConfiguration object. If the
	 *         ApplicationConfiguration object is not valid, an exception will
	 *         be thrown.
	 */
	public ApplicationConfiguration getApplicationConfiguration() {

		if (!this.applicationConfiguration.isValid()) {
			val ex = new ConfigurationException(ConfigurationErrorCode.ApplicationConfigurationNotValidError);
			ex.put("InvalidFields", applicationConfiguration.getInvalidFields());
			throw ex;
		}

		return applicationConfiguration;
	}

}
