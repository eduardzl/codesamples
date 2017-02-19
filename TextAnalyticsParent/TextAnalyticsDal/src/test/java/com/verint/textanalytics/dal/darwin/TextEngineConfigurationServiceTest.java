package com.verint.textanalytics.dal.darwin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URL;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.verint.textanalytics.common.configuration.ConfigurationFile;
import com.verint.textanalytics.common.configuration.ConfigurationSource;
import com.verint.textanalytics.common.configuration.FilePathType;
import com.verint.textanalytics.common.exceptions.ConfigurationException;
import com.verint.textanalytics.model.documentSchema.DocumentHierarchyType;
import com.verint.textanalytics.model.documentSchema.FieldDataType;

public class TextEngineConfigurationServiceTest {

    static String xml = "xml";
    static String textEngineConfiguration = "textEngineConfiguration";
    static String wrongTextEngineConfiguration = "wrongTextEngineConfiguration";

    static ConfigurationFile wrongConfigFile;
    static ConfigurationFile configFile;


    public TextEngineConfigurationServiceTest() throws IOException {
        createConfigFiles();

        createWrongConfigFiles();
    }

    private void createWrongConfigFiles() throws IOException {
        wrongConfigFile = new ConfigurationFile(getFilePath(wrongTextEngineConfiguration + "." + xml), FilePathType.AbsolutePath, ConfigurationSource.XMLConfiguration, true);
    }

    private void createConfigFiles() throws IOException {
        configFile = new ConfigurationFile(getFilePath(textEngineConfiguration + "." + xml), FilePathType.AbsolutePath, ConfigurationSource.XMLConfiguration, true);
    }


    @Test
    public final void testTextEngineConfigurationService() {
        /**
         * TextEngineSchemeService service = new TextEngineSchemeService(configFile, configFile);
         * 
         * TextEngineScheme textEngineConfiguration = service.getTextEngineScheme();
         * 
         * assertNotNull(textEngineConfiguration);
         * 
         * assertEquals(2, textEngineConfiguration.getTenants().size()); assertEquals("tenant1", textEngineConfiguration.getTenants().get(0).getName()); assertEquals("tenant2", textEngineConfiguration.getTenants().get(1).getName());
         * 
         * assertEquals(2, textEngineConfiguration.getTenants().get(0).getChannels().size()); assertEquals("channel1", textEngineConfiguration.getTenants().get(0).getChannels().get(0). getName()); assertEquals("channel2", textEngineConfiguration.getTenants().get(0).getChannels().get(1). getName());
         * 
         * assertEquals(1, textEngineConfiguration.getTenants().get(1).getChannels().size()); assertEquals("channel1", textEngineConfiguration.getTenants().get(1).getChannels().get(0). getName());
         * 
         * assertEquals(5, textEngineConfiguration.getTenants().get(0).getChannels().get(0). getFields().size()); assertEquals("Meta_i_x1", textEngineConfiguration.getTenants().get(0).getChannels().get(0). getFields().get(0).getName()); assertEquals("x1",
         * textEngineConfiguration.getTenants().get(0).getChannels().get(0). getFields().get(0).getDisplayFieldName()); assertEquals(FieldDataType.Int, textEngineConfiguration.getTenants().get(0).getChannels().get(0). getFields().get(0).getFieldDataType());
         * assertEquals(DocumentHierarchyType.Interaction, textEngineConfiguration.getTenants().get(0).getChannels().get(0). getFields().get(0).getDocumentHierarchyType());
         * 
         * assertEquals("Meta_i_x2", textEngineConfiguration.getTenants().get(0).getChannels().get(0). getFields().get(1).getName()); assertEquals("x2", textEngineConfiguration.getTenants().get(0).getChannels().get(0). getFields().get(1).getDisplayFieldName()); assertEquals(FieldDataType.Int,
         * textEngineConfiguration.getTenants().get(0).getChannels().get(0). getFields().get(1).getFieldDataType()); assertEquals(DocumentHierarchyType.Utterance, textEngineConfiguration.getTenants().get(0).getChannels().get(0). getFields().get(1).getDocumentHierarchyType());
         * 
         * assertEquals("Meta_s_x3", textEngineConfiguration.getTenants().get(0).getChannels().get(0). getFields().get(2).getName()); assertEquals("x3", textEngineConfiguration.getTenants().get(0).getChannels().get(0). getFields().get(2).getDisplayFieldName()); assertEquals(FieldDataType.Text,
         * textEngineConfiguration.getTenants().get(0).getChannels().get(0). getFields().get(2).getFieldDataType()); assertEquals(DocumentHierarchyType.Interaction, textEngineConfiguration.getTenants().get(0).getChannels().get(0). getFields().get(2).getDocumentHierarchyType());
         * 
         * assertEquals("Meta_s_x4", textEngineConfiguration.getTenants().get(0).getChannels().get(0). getFields().get(3).getName()); assertEquals("x4", textEngineConfiguration.getTenants().get(0).getChannels().get(0). getFields().get(3).getDisplayFieldName()); assertEquals(FieldDataType.Text,
         * textEngineConfiguration.getTenants().get(0).getChannels().get(0). getFields().get(3).getFieldDataType()); assertEquals(DocumentHierarchyType.Utterance, textEngineConfiguration.getTenants().get(0).getChannels().get(0). getFields().get(3).getDocumentHierarchyType());
         * 
         * assertEquals("Meta_s_x5", textEngineConfiguration.getTenants().get(0).getChannels().get(0). getFields().get(4).getName()); assertEquals("x5", textEngineConfiguration.getTenants().get(0).getChannels().get(0). getFields().get(4).getDisplayFieldName()); assertEquals(FieldDataType.Text,
         * textEngineConfiguration.getTenants().get(0).getChannels().get(0). getFields().get(4).getFieldDataType()); assertEquals(DocumentHierarchyType.Utterance, textEngineConfiguration.getTenants().get(0).getChannels().get(0). getFields().get(4).getDocumentHierarchyType());
         * 
         * assertEquals(2, textEngineConfiguration.getTenants().get(0).getChannels().get(1). getFields().size()); assertEquals("Meta_s_x3", textEngineConfiguration.getTenants().get(0).getChannels().get(1). getFields().get(0).getName()); assertEquals("x3",
         * textEngineConfiguration.getTenants().get(0).getChannels().get(1). getFields().get(0).getDisplayFieldName()); assertEquals(FieldDataType.Text, textEngineConfiguration.getTenants().get(0).getChannels().get(1). getFields().get(0).getFieldDataType());
         * assertEquals(DocumentHierarchyType.Interaction, textEngineConfiguration.getTenants().get(0).getChannels().get(1). getFields().get(0).getDocumentHierarchyType());
         * 
         * assertEquals("Meta_s_x4", textEngineConfiguration.getTenants().get(0).getChannels().get(1). getFields().get(1).getName()); assertEquals("x4", textEngineConfiguration.getTenants().get(0).getChannels().get(1). getFields().get(1).getDisplayFieldName()); assertEquals(FieldDataType.Text,
         * textEngineConfiguration.getTenants().get(0).getChannels().get(1). getFields().get(1).getFieldDataType()); assertEquals(DocumentHierarchyType.Utterance, textEngineConfiguration.getTenants().get(0).getChannels().get(1). getFields().get(1).getDocumentHierarchyType());
         * 
         * assertEquals(3, textEngineConfiguration.getTenants().get(1).getChannels().get(0). getFields().size()); assertEquals("Meta_i_x1", textEngineConfiguration.getTenants().get(1).getChannels().get(0). getFields().get(0).getName()); assertEquals("x1",
         * textEngineConfiguration.getTenants().get(1).getChannels().get(0). getFields().get(0).getDisplayFieldName()); assertEquals(FieldDataType.Int, textEngineConfiguration.getTenants().get(1).getChannels().get(0). getFields().get(0).getFieldDataType());
         * assertEquals(DocumentHierarchyType.Interaction, textEngineConfiguration.getTenants().get(1).getChannels().get(0). getFields().get(0).getDocumentHierarchyType());
         * 
         * assertEquals("Meta_i_x2", textEngineConfiguration.getTenants().get(1).getChannels().get(0). getFields().get(1).getName()); assertEquals("x2", textEngineConfiguration.getTenants().get(1).getChannels().get(0). getFields().get(1).getDisplayFieldName()); assertEquals(FieldDataType.Int,
         * textEngineConfiguration.getTenants().get(1).getChannels().get(0). getFields().get(1).getFieldDataType()); assertEquals(DocumentHierarchyType.Utterance, textEngineConfiguration.getTenants().get(1).getChannels().get(0). getFields().get(1).getDocumentHierarchyType());
         * 
         * assertEquals("Meta_s_x3", textEngineConfiguration.getTenants().get(1).getChannels().get(0). getFields().get(2).getName()); assertEquals("x3", textEngineConfiguration.getTenants().get(1).getChannels().get(0). getFields().get(2).getDisplayFieldName()); assertEquals(FieldDataType.Text,
         * textEngineConfiguration.getTenants().get(1).getChannels().get(0). getFields().get(2).getFieldDataType()); assertEquals(DocumentHierarchyType.Interaction, textEngineConfiguration.getTenants().get(1).getChannels().get(0). getFields().get(2).getDocumentHierarchyType());
         **/
    }

    public final void testTextEngineConfigurationServiceWithWrongFile() {
        TextEngineSchemaService service = new TextEngineSchemaService(wrongConfigFile);
        assertEquals(service.isValid(), false);
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
