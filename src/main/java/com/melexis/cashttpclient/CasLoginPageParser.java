package com.melexis.cashttpclient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Parse the CAS login page to get the hidden info.
 */
public class CasLoginPageParser {

    private final Document doc;

    public CasLoginPageParser(InputStream content) throws IOException, ParserConfigurationException {
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
}
