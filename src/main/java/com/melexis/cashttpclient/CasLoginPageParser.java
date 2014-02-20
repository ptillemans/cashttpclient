package com.melexis.cashttpclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

/**
 * Created by pti on 19/02/14.
 */
public class CasLoginPageParser {

    Logger log = LoggerFactory.getLogger(CasLoginPageParser.class);

    private final XPath xpath;
    private final Document doc;

    public CasLoginPageParser(InputStream content) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver(new EntityResolver() {
            @Override
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                String fname = new URL(systemId).getFile().replaceFirst(".*/","DTD/");
                log.info("Getting entity {}", fname );
                InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(fname);
                return new InputSource(inputStream);
            }
        });
        doc = builder.parse(content);
        XPathFactory xPathfactory = XPathFactory.newInstance();
        xpath = xPathfactory.newXPath();
    }

    public String getFormUrl() throws XPathExpressionException {
        XPathExpression expr = xpath.compile("//form[@id='fm1']");
        return getAttributeValue("action",expr);
    }

    public String getLt() throws XPathExpressionException {
        XPathExpression expr = xpath.compile("//input[@name='lt']");
        return getAttributeValue("value", expr);
    }

    public String getEventId() throws XPathExpressionException {
        XPathExpression expr = xpath.compile("//input[@name='_eventId']");
        return getAttributeValue("value", expr);
    }

    public String getAttributeValue(String attr, XPathExpression expr) throws XPathExpressionException {
        NodeList nodes = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
        Element input = (Element)nodes.item(0);
        return input.getAttribute(attr);
    }
}
