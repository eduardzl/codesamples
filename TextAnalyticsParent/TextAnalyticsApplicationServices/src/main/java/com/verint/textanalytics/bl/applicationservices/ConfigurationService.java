package com.verint.textanalytics.bl.applicationservices;

import com.verint.textanalytics.dal.configService.ConfigurationServiceProvider;
import com.verint.textanalytics.dal.darwin.TextAnalyticsProvider;
import com.verint.textanalytics.dal.darwin.TextEngineSchemaService;
import com.verint.textanalytics.model.documentSchema.TextSchemaField;
import com.verint.textanalytics.model.interactions.SourceType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration service.
 * 
 * @author EZlotnik
 *
 */
public class ConfigurationService {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	private TextEngineSchemaService textEngineSschemaService;

	@Autowired
	private TextAnalyticsProvider textAnalyticsProvider;

	@Autowired
	private ConfigurationServiceProvider configurationServiceProvider;

	/**
	 * Extracts list of dynamic fields for channel.
	 * 
	 * @param tenant
	 *            tenant
	 * @param channel
	 *            channel
	 * @return list of dynamic fields for the channel
	 */
	public List<TextSchemaField> getChannelDynamicFields(String tenant, String channel) {
		return textEngineSschemaService.getChannelDynamicFields(tenant, channel);
	}

	/***
	 * 
	 * @param tenant
	 *            tenant
	 * @param channel
	 *            tenant
	 * @return list of the source types for this channel
	 */
	public List<SourceType> getChannelSourceTypes(String tenant, String channel) {

		List<SourceType> retVal = new ArrayList<SourceType>();

		for (SourceType sourceType : SourceType.values()) {
			// check for all source types except "Unknown"
			if (!sourceType.equals(SourceType.Unknown)) {
				try {
					if (textAnalyticsProvider.isSourceTypeExistInChannel(tenant, channel, sourceType)) {
						logger.debug("Source type {} presents in channel {} of tenant {}", sourceType, tenant, channel);

						retVal.add(sourceType);
					}
				} catch (Exception ex) {
					//we don't want to fail even if the source type isn't exists 
					logger.error("Error while checking Source type {}  in channel {} of tenant {}", sourceType, tenant, channel);

				}
			}
		}

		return retVal;
	}

	/**
	 * @param sourceTypes
	 *            sourceTypes
	 * @return List<TextSchemaField>
	 */
	public List<TextSchemaField> getSourceTypeSpecificFields(List<SourceType> sourceTypes) {
		return textEngineSschemaService.getSourceTypeTextSchemaFields(sourceTypes);
	}

	/***
	 * @param tenant  tenant
	 * @param channel tenant
	 * @return lqanguage configured for this channel
	 */
	@Cacheable(value = "TextAnalyticsLanguageCache")
	public String getChannelLanguage(String tenant, String channel) {
		return configurationServiceProvider.getChannelLanguage(tenant, channel);
	}
}
