package com.verint.textanalytics.dal.darwin;

import java.util.*;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.apache.logging.log4j.*;
import com.verint.textanalytics.common.configuration.ConfigurationFile;
import com.verint.textanalytics.common.utils.StringUtils;
import com.verint.textanalytics.common.utils.XmlUtils;
import com.verint.textanalytics.model.documentSchema.*;
import com.verint.textanalytics.model.interactions.*;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.experimental.Accessors;

/**
 * TextEngineConfigurationService.
 * 
 * @author imor
 *
 */
public class TextEngineSchemaService {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Getter
	@Setter
	@Accessors(chain = true)
	private List<TenantSchema> tenants;

	@Getter
	@Setter
	private Map<String, TextSchemaField> vtaInteractionLevelPredefinedFields;

	@Getter
	@Setter
	private Map<String, TextSchemaField> vtaUtteranceLevelPredefinedFields;

	@Getter
	@Setter
	private Map<SourceType, List<TextSchemaField>> sourceTypeSpecificFields;

	@Getter
	@Accessors(chain = true)
	private boolean isValid = false;

	/**
	 * TextEngineConfigurationService.
	 * 
	 * @param channelsSpecificFieldsFile
	 *            appConfigFile
	 */
	public TextEngineSchemaService(ConfigurationFile channelsSpecificFieldsFile) {

		try {
			this.loadChannelSpecificSchemaFields(channelsSpecificFieldsFile);

			logger.info("Build TextEngine Configuration Service finish successfully.");
		} catch (Exception e) {
			logger.error("Exception in TextEngineConfigurationService creation. Error - {}", e);
		}
	}

	/**
	 * @return true if TextEngineScheme object is valid, otherwise false
	 */
	public boolean validate() {

		this.isValid = true;
		if (tenants == null || tenants.size() == 0) {
			logger.error("TextEngineScheme is not valid");
			this.isValid = false;
		}
		return this.isValid;
	}

	@Override
	public String toString() {
		JSONObject jsonObject = new JSONObject(this);
		return jsonObject.toString();
	}

	/**
	 * Get Text Scheme Field. If there is a field with the name in the global
	 * object it will be return, and if there are two field with the same name
	 * the first one will return.
	 * 
	 * 
	 * @param tenant
	 *            tenant
	 * @param channel
	 *            channel
	 * @param fieldName
	 *            field name
	 * @return TextSchemeField
	 */
	public TextSchemaField getTextSchemaField(String tenant, String channel, String fieldName) {
		TextSchemaField schemaField = null;

		// first try to find the schema field in VTA predefined fields
		if (this.vtaInteractionLevelPredefinedFields != null && this.vtaInteractionLevelPredefinedFields.containsKey(fieldName)) {
			schemaField = this.vtaInteractionLevelPredefinedFields.get(fieldName);
		} else if (this.vtaUtteranceLevelPredefinedFields != null && this.vtaUtteranceLevelPredefinedFields.containsKey(fieldName)) {
			schemaField = this.vtaUtteranceLevelPredefinedFields.get(fieldName);
		}

		// First try to search the field in Interaction level fields
		if (schemaField == null) {
			schemaField = this.findFieldInChannelSpecificFields(tenant, channel, fieldName, DocumentHierarchyType.Interaction);
		}

		// search the field in Utterance level field
		if (schemaField == null) {
			schemaField = this.findFieldInChannelSpecificFields(tenant, channel, fieldName, DocumentHierarchyType.Utterance);
		}

		return schemaField;
	}

	/**
	 * getTextSchemeField.
	 * 
	 * @param tenant
	 *            tenant
	 * @param channel
	 *            channel
	 * @param field
	 *            field
	 * @param documentHierarchyType
	 *            documentHierarchyType
	 * @return TextSchemeField
	 */
	public TextSchemaField getTextSchemaField(String tenant, String channel, String field, DocumentHierarchyType documentHierarchyType) {
		return this.getTextSchemaFieldForHierarchy(tenant, channel, field, documentHierarchyType);
	}

	/**
	 * isParentDocumentField.
	 * 
	 * @param tenant
	 *            tenant
	 * @param channel
	 *            channel
	 * @param field
	 *            field
	 * @return TextSchemeField
	 */
	public boolean isParentDocumentField(String tenant, String channel, String field) {
		TextSchemaField schemaField = this.getTextSchemaFieldForHierarchy(tenant, channel, field, DocumentHierarchyType.Interaction);
		return schemaField != null;
	}

	/**
	 * isChildDocumentField.
	 * 
	 * @param tenant
	 *            tenant
	 * @param channel
	 *            channel
	 * @param field
	 *            field
	 * @return TextSchemeField
	 */
	public boolean isChildDocumentField(String tenant, String channel, String field) {
		TextSchemaField schemaField = this.getTextSchemaFieldForHierarchy(tenant, channel, field, DocumentHierarchyType.Utterance);
		return schemaField != null;
	}

	/**
	 * Extracts list of channel dynamic fields.
	 * @param tenant
	 *            tenant
	 * @param channel
	 *            channel
	 * @return list of channel dynamic fields
	 */
	public List<TextSchemaField> getChannelDynamicFields(String tenant, String channel) {
		List<TextSchemaField> channelDynamicFields = null;

		if (!StringUtils.isNullOrBlank(tenant) && !StringUtils.isNullOrBlank(channel)) {
			TenantSchema tenantSchema = this.getTenantSchema(tenant);
			if (tenantSchema != null && tenantSchema.getChannels() != null) {
				ChannelSchema channelSchema = tenantSchema.getChannel(channel);
				if (channelSchema != null) {
					channelDynamicFields = channelSchema.getFields();
				} else {
					logger.warn("Channel schema definition {} was not found in tenant {} channels definitions", channel, tenantSchema.getName());
				}
			}
		}

		return channelDynamicFields;
	}

	/**
	 * Retrieves list of fields for source type.
	 * @param sourceType
	 *            source type - Chat, Email ...
	 * @return list of source type specific fields
	 */
	public List<TextSchemaField> getSourceTypeTextSchemaFields(SourceType sourceType) {
		if (this.sourceTypeSpecificFields.containsKey(sourceType)) {
			return this.sourceTypeSpecificFields.get(sourceType);
		}

		return null;
	}

	/**
	 * @param sourceTypes
	 *            sourceTypes
	 * @return List<TextSchemaField>
	 */
	public List<TextSchemaField> getSourceTypeTextSchemaFields(List<SourceType> sourceTypes) {

		List<TextSchemaField> res = new ArrayList<TextSchemaField>();
		List<TextSchemaField> textSchemaFields;
		for (SourceType sourceType : sourceTypes) {

			textSchemaFields = sourceTypeSpecificFields.get(sourceType);
			if (textSchemaFields != null) {
				res.addAll(textSchemaFields);
			}
		}
		return res;
	}

	private TextSchemaField getTextSchemaFieldForHierarchy(String tenant, String channel, String field, DocumentHierarchyType documentHierarchyType) {
		TextSchemaField textSchemaField = null;

		switch (documentHierarchyType) {
			case Interaction:
				if (this.vtaInteractionLevelPredefinedFields != null && this.vtaInteractionLevelPredefinedFields.containsKey(field)) {
					textSchemaField = this.vtaInteractionLevelPredefinedFields.get(field);
				}
				break;
			case Utterance:
				if (this.vtaUtteranceLevelPredefinedFields != null && this.vtaUtteranceLevelPredefinedFields.containsKey(field)) {
					textSchemaField = this.vtaUtteranceLevelPredefinedFields.get(field);
				}
				break;
			default:
				break;
		}

		if (textSchemaField == null) {
			textSchemaField = this.findFieldInChannelSpecificFields(tenant, channel, field, documentHierarchyType);
		}

		return textSchemaField;
	}

	/**
	 * getTenant.
	 * 
	 * @param tenantName
	 *            tenantName
	 * @return Channel
	 */
	private TenantSchema getTenantSchema(String tenantName) {
		if (this.tenants != null) {
			val tenantOpt = this.tenants.stream().filter(f -> f.getName().equalsIgnoreCase(tenantName)).findFirst();
			return tenantOpt.isPresent() ? tenantOpt.get() : null;
		}

		return null;
	}

	private TextSchemaField findFieldInChannelSpecificFields(String tenant, String channel, String fieldName, DocumentHierarchyType hierarchyType) {
		TextSchemaField schemaField = null;

		TenantSchema tenantObj = this.getTenantSchema(tenant);
		if (tenantObj != null) {
			ChannelSchema channelObj = tenantObj.getChannel(channel);
			if (channelObj != null) {

				val textFieldFound = channelObj.getFields().stream().filter(f -> f.getName().equalsIgnoreCase(fieldName)
				        && f.getDocumentHierarchyType() == hierarchyType).findFirst();
				if (textFieldFound.isPresent()) {
					schemaField = textFieldFound.get();
				}
			}
		}

		return schemaField;
	}

	private void loadChannelSpecificSchemaFields(ConfigurationFile configurationFile) {
		// read data from configuration file
		try {
			this.tenants = new ArrayList<TenantSchema>();

			if (configurationFile != null) {
				String configurationFilePath = configurationFile.resolveFullPath();

				logger.debug("Loading metadata fields mapping file from {}", configurationFilePath);

				Document xmlDocument = XmlUtils.loadXml(configurationFilePath);
				if (xmlDocument != null) {

					// BEGIN GENERATED CODE
					NodeList tenantsNodes = xmlDocument.getElementsByTagName("tenants");
					if (tenantsNodes != null && tenantsNodes.getLength() > 0) {

						List<Node> tenantNodes = XmlUtils.getNodes(tenantsNodes.item(0), "tenant");
						tenantNodes.stream().forEach((tenantSchemaNode) -> {

							// add new TenantSchema 
							TenantSchema tenantSchema = new TenantSchema();
							tenantSchema.setName(XmlUtils.getAttribute(tenantSchemaNode, "name"));

							List<Node> channelsNodes = XmlUtils.getNodes(tenantSchemaNode, "channels");
							if (channelsNodes != null) {
								List<Node> channelNodes = XmlUtils.getNodes(channelsNodes.get(0), "channel");
								if (channelNodes != null) {
									channelNodes.stream().forEach((channelSchemaNode) -> {

										// add new ChannelSchema
										ChannelSchema channelSchema = new ChannelSchema();
										channelSchema.setName(XmlUtils.getAttribute(channelSchemaNode, "name"));

										List<Node> fieldsNodes = XmlUtils.getNodes(channelSchemaNode, "field");
										fieldsNodes.stream().forEach((fieldNode) -> {

											TextSchemaField metadataField = new TextSchemaField();

											metadataField.setName(XmlUtils.getAttribute(fieldNode, "name"));
											metadataField.setDisplayFieldName(XmlUtils.getAttribute(fieldNode, "displayName"));

											FieldDataType fieldDataType = FieldDataType.valueOf(XmlUtils.getAttribute(fieldNode, "dataType"));
											metadataField.setFieldDataType(fieldDataType);

											DocumentHierarchyType documentHierarchyType = DocumentHierarchyType.valueOf(XmlUtils.getAttribute(fieldNode, "documentHierarchyType"));
											metadataField.setDocumentHierarchyType(documentHierarchyType);

											boolean facetDisable = Boolean.valueOf(XmlUtils.getAttribute(fieldNode, "facetDisable"));
											metadataField.setFacetDisable(facetDisable);

											channelSchema.addSchemaField(metadataField);
										});

										logger.debug("{} Dynamic schema fields were loaded for channel  {} ", fieldsNodes.size(), channelSchema.getName());

										tenantSchema.addChannelSchema(channelSchema);
									});
								}
							}

							this.tenants.add(tenantSchema);
						});
					}
					// END GENERATED CODE
				}

				logger.debug("Dynamic schema fields were loaded for {} tenants ", this.tenants.size());

				this.isValid = true;
			} else {
				logger.error("Configuration file parameter is NULL.");
			}
		} catch (Exception e) {
			logger.error("Could not read Configuration File. Error - {}", e);
		}
	}
}
