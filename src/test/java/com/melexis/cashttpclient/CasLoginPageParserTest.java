package com.melexis.cashttpclient;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class CasLoginPageParserTest {


    private CasLoginPage parser;

    @Before
    public void setUp() throws IOException, SAXException, ParserConfigurationException {
        InputStream inputStream = new FileInputStream("src/test/resources/login");
        parser = new CasLoginPage(inputStream);
    }

    @Test
    public void testGetFormUrl() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        String url = parser.getFormUrl();
        assertThat(url, is("/cas/login;jsessionid=387B23181FD64DEC7AAFB8A8C4341143"));

    }

    @Test
    public void testGetLt() throws XPathExpressionException {
        String lt = parser.getLt();
        assertThat(lt, is("LT-1251-MsPLte6GawlzV5wVEx1xUtYTqkLcF1"));
    }

    @Test
    public void testEventId() throws XPathExpressionException {
        String eventId = parser.getEventId();
        assertThat(eventId, is("submit"));
    }
}
