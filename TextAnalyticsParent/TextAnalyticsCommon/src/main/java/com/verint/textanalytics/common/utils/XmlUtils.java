package com.verint.textanalytics.common.utils;

import java.io.*;
import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.NodeList;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.apache.logging.log4j.*;

/***
 * 
 * @author yzanis
 *
 */
public class XmlUtils {
	private static Logger s_logger;

	static {
		s_logger = LogManager.getLogger(XmlUtils.class);
	}

	/***
	 * 
	 * @param xmlFile
	 *            xml file to create document from
	 * @return returns the parsed xml document
	 */
	public static Document getXMLDocumentFromFile(File xmlFile) {
		try {

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

			return dBuilder.parse(xmlFile);

		} catch (Exception e) {
			s_logger.error("Exception in XMLUtils.getXMLDocumentFromFile: {}", e);
		}
		return null;
	}

	/**
	 * Load xml into XML Document.
	 * @param documentXml
	 *            xml as string
	 * @return xml document object
	 */
	public static Document parseXml(String documentXml) {
		Document xmlDoc = null;

		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

			InputSource is = new InputSource(new StringReader(documentXml));
			xmlDoc = dBuilder.parse(is);
			xmlDoc.getDocumentElement().normalize();

		} catch (Exception ex) {
			s_logger.error("Failed to load xml document. Error - {}", ex);
			xmlDoc = null;
		}

		return xmlDoc;
	}

	/**
	 * Loads xml document.
	 * @param filePath
	 *            file path
	 * @return xml document
	 * @throws IOException
	 *             exception
	 */
	public static Document loadXml(String filePath) throws IOException {
		if (propel.core.utils.FileUtils.exists(filePath)) {
			return parseXml(FileUtils.readFileToEnd(filePath));
		} else {
			s_logger.error("Failure loading xml file. File was not found at {} ", filePath);
			return null;
		}
	}

	/***
	 * 
	 * @param xml
	 *            the XML Document
	 * @param nodeName
	 *            the name of the Nodes wanted
	 * @return node list matches the name in nodeName param
	 */
	public static NodeList getXMLElementsByTagName(Document xml, String nodeName) {
		try {

			if (xml != null && !nodeName.isEmpty()) {
				return xml.getElementsByTagName(nodeName);
			}
		} catch (Exception e) {
			s_logger.error("Exception in XMLUtils.getXMLElementsByTagName: {}", e);
		}
		return null;
	}

	/***
	 * 
	 * @param el
	 *            the element in the XML Document
	 * @param nodeName
	 *            the name of the Nodes wanted
	 * @return node list matches the name in nodeName param
	 */
	public static NodeList getXMLElementsByTagName(Element el, String nodeName) {
		try {

			if (el != null && !nodeName.isEmpty()) {
				return el.getElementsByTagName(nodeName);
			}
		} catch (Exception e) {
			s_logger.error("Exception in XMLUtils.getXMLElementsByTagName: ", e);
		}
		return null;
	}

	/***
	 * 
	 * @param el
	 *            xml element
	 * @param attributeName
	 *            the name of the attribute to return
	 * @param defaultValue
	 *            value the return in case of this attribute doesn't exist
	 * @return value of the attribute in the xml element
	 */
	public static String getAttribute(Element el, String attributeName, String defaultValue) {
		try {
			if (el != null && el.hasAttribute(attributeName)) {
				return el.getAttribute(attributeName);
			}
		} catch (Exception e) {
			s_logger.error("Exception in XMLUtils.getXMLDocumentFromFile: {}", e);
		}
		return defaultValue;
	}

	/**
	 * Retrives attribute value from node.
	 * @param node
	 *            xml node
	 * @param attributeName
	 *            attribute name
	 * @return attribute value
	 */
	public static String getAttribute(Node node, String attributeName) {
		return propel.core.utils.XmlUtils.parseAttribute(node, attributeName);
	}

	/**
	 * Find nodes under specific node.
	 * @param node
	 *            node
	 * @param childNodesName
	 *            child nodes name
	 * @return list of found nodes.
	 */
	public static List<Node> getNodes(Node node, String childNodesName) {
		return propel.core.utils.XmlUtils.findAllNodes(node, childNodesName);
	}

}