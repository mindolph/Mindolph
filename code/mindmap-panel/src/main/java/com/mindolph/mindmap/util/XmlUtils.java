package com.mindolph.mindmap.util;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mindolph.com@gmail.com
 */
public class XmlUtils {
    public static Document loadHtmlDocument(InputStream inStream, String charset, boolean autoClose) throws IOException {
        try {
            org.jsoup.nodes.Document result = Jsoup.parse(IOUtils.toString(inStream, charset));
            return new W3CDom().fromJsoup(result);
        } finally {
            if (autoClose) {
                IOUtils.closeQuietly(inStream);
            }
        }
    }

    /**
     * Load and parse XML document from input stream.
     *
     * @param inStream  stream to read document
     * @param charset   charset to be used for loading, can be null
     * @param autoClose true if stream must be closed, false otherwise
     * @return parsed document
     * @throws IOException                  will be thrown if transport error
     * @throws ParserConfigurationException will be thrown if parsing error
     * @throws SAXException                 will be thrown if SAX error
     * 
     */

    public static Document loadXmlDocument(InputStream inStream, String charset, boolean autoClose) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (ParserConfigurationException ex) {
//            Utils.LOGGER.error("Can't set feature for XML parser : " + ex.getMessage(), ex);
            throw new SAXException("Can't set flag to use security processing of XML file");
        }

        try {
            factory.setFeature("http://apache.org/xml/features/validation/schema", false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        } catch (ParserConfigurationException ex) {
//            Utils.LOGGER.warn("Can't set some features for XML parser : " + ex.getMessage());
            ex.printStackTrace();
        }

        factory.setIgnoringComments(true);
        factory.setValidating(false);

        DocumentBuilder builder = factory.newDocumentBuilder();

        Document document;
        try {
            InputStream stream;
            if (charset == null) {
                stream = inStream;
            }
            else {
                stream = new ByteArrayInputStream(IOUtils.toString(inStream, charset).getBytes(StandardCharsets.UTF_8));
            }
            document = builder.parse(stream);
        } finally {
            if (autoClose) {
                IOUtils.closeQuietly(inStream);
            }
        }

        return document;
    }

    /**
     * Get first direct child for name.
     *
     * @param node        element to find children
     * @param elementName name of child element
     * @return found first child or null if not found
     * 
     */
    public static Element findFirstElement(Element node, String elementName) {
        Element result = null;
        for (Element l : findDirectChildrenForName(node, elementName)) {
            result = l;
            break;
        }
        return result;
    }

    /**
     * Find all direct children with defined name.
     *
     * @param element          parent element
     * @param childElementname child element name
     * @return list of found elements
     * 
     */
    public static List<Element> findDirectChildrenForName(Element element, String childElementname) {
        List<Element> resultList = new ArrayList<>();
        NodeList list = element.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (element.equals(node.getParentNode()) && node instanceof Element && childElementname.equals(node.getNodeName())) {
                resultList.add((Element) node);
            }
        }
        return resultList;
    }
}
