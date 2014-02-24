package com.melexis.cashttpclient;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpCoreContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Page object for the  CAS login page to get the hidden info.
 */
@NotThreadSafe
public class CasLoginPage {

    private final Document doc;

    public CasLoginPage(InputStream content) throws IOException, ParserConfigurationException {
        doc = Jsoup.parse(content, "utf-8", "http://example.com/");
    }

    public String getFormUrl() {
        return doc.select("form#fm1").attr("action");
    }

    public String getLt() {
        return doc.select("input[name=lt]").attr("value");
    }

    public String getEventId() {
        return doc.select("input[name=_eventId]").attr("value");
    }

    public String getExecution() {
        return doc.select("input[name=execution]").attr("value");
    }

    public HttpEntity fillForm(String username, String password) throws UnsupportedEncodingException {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("password", password));
        params.add(new BasicNameValuePair("lt", getLt()));
        params.add(new BasicNameValuePair("execution", getExecution()));
        params.add(new BasicNameValuePair("_eventId", getEventId()));
        params.add(new BasicNameValuePair("submit", "LOGIN"));
        return new UrlEncodedFormEntity(params);
    }

    String derivePostUrl(HttpCoreContext context) throws XPathExpressionException, URISyntaxException {
        String formUrl = getFormUrl();
        final URI requestUri = new URI(formUrl);
        if (!requestUri.isAbsolute()) {
            formUrl = context.getTargetHost().toURI() + formUrl;
        }
        return formUrl;
    }
}
