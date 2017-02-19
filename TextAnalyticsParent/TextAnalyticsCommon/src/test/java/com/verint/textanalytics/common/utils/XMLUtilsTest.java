package com.verint.textanalytics.common.utils;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/***
 * 
 * @author yzanis
 *
 */
public class XMLUtilsTest {

	// getXMLDocumentFromFile
	// getXMLElementsByTagName
	// getXMLElementsByTagName
	// getAttribute

	static File xmlFileToTest;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		xmlFileToTest = getTestedXMLFile();
	}

	@Test
	public void getXMLDocumentFromFileFileNotExistTst() {
		Document res = XmlUtils.getXMLDocumentFromFile(null);
		assertEquals(null, res);
	}

	@Test
	public void getXMLDocumentFromFileTest() {
		Document res = XmlUtils.getXMLDocumentFromFile(xmlFileToTest);
		assertNotNull(res);
	}

	@Test
	public void getXMLElementsByTagNameEmptyParamTest() {
		Document xmlToTest = XmlUtils.getXMLDocumentFromFile(xmlFileToTest);
		NodeList res = XmlUtils.getXMLElementsByTagName(xmlToTest, "");
		assertEquals(null, res);
	}

	@Test
	public void getXMLElementsByTagNameEmptyParamElementTest() {
		Document xmlToTest = XmlUtils.getXMLDocumentFromFile(xmlFileToTest);
		NodeList el = XmlUtils.getXMLElementsByTagName(xmlToTest, "TestTag");
		NodeList res = XmlUtils.getXMLElementsByTagName((Element) el.item(0), "");
		assertEquals(null, res);
	}

	@Test
	public void getXMLElementsByTagNameTest() {
		Document xmlToTest = XmlUtils.getXMLDocumentFromFile(xmlFileToTest);
		NodeList el = XmlUtils.getXMLElementsByTagName(xmlToTest, "TestTag");
		assertNotNull(el);
		assertEquals(1, el.getLength());
	}

	@Test
	public void getXMLElementsByTagNameElementTest() {
		Document xmlToTest = XmlUtils.getXMLDocumentFromFile(xmlFileToTest);
		NodeList el = XmlUtils.getXMLElementsByTagName(xmlToTest, "TestTag");
		NodeList nl = XmlUtils.getXMLElementsByTagName((Element) el.item(0), "Child");
		assertEquals(4, nl.getLength());
	}

	@Test
	public void getAttributeTest() {

		Document doc = XmlUtils.getXMLDocumentFromFile(xmlFileToTest);
		NodeList el = XmlUtils.getXMLElementsByTagName(doc, "TestTag");
		NodeList nl = XmlUtils.getXMLElementsByTagName((Element) el.item(0), "Child");
		Element child = (Element) nl.item(0);
		String res = XmlUtils.getAttribute(child, "id", "8");
		assertNotEquals("8", res);
	}

	@Test
	public void getAttributeNotExistTest() {
		Document xmlToTest = XmlUtils.getXMLDocumentFromFile(xmlFileToTest);
		NodeList el = XmlUtils.getXMLElementsByTagName(xmlToTest, "TestTag");
		NodeList nl = XmlUtils.getXMLElementsByTagName((Element) el.item(0), "Child");
		Element child = (Element) nl.item(0);
		String res = XmlUtils.getAttribute(child, "test", "NotFound");
		assertEquals("NotFound", res);
	}

	@Test
	public void parseXmlTest() {
		String documentXml = "<d><a>1</a><b>2</b><a>2</a><c>2</c></d>";
		Document xmlToTest = XmlUtils.parseXml(documentXml);
		assertEquals(2, xmlToTest.getElementsByTagName("a")
		                         .getLength());
		assertEquals(1, xmlToTest.getElementsByTagName("b")
		                         .getLength());
		assertEquals(1, xmlToTest.getElementsByTagName("c")
		                         .getLength());
		assertEquals(1, xmlToTest.getElementsByTagName("d")
		                         .getLength());
	}

	private static File getTestedXMLFile() {
		try {
			return new File(Thread.currentThread()
			                      .getContextClassLoader()
			                      .getResource("XMLTest.xml")
			                      .toURI());
		} catch (URISyntaxException e) {

		}
		return null;
	}
}