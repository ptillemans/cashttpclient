package com.melexis.cashttpclient;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpCoreContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * HttpClient wrapper to transparent log in to CAS servers
 *
 * CAS protected services rely on a redirect and logging into a form
 * based authentication page and then be redirected back to the service.
 *
 * This kind of authentication is not supported out of the box in the
 * standard httpclient.
 */
public class CasHttpClient {

    private static Logger log = LoggerFactory.getLogger(CasHttpClient.class);

    HttpClient client;

    String casPrefix;

    public void setCasUserName(String casUserName) {
        this.casUserName = casUserName;
    }
    public void setCasPassWord(String casPassWord) {
        this.casPassWord = casPassWord;
    }

    private String casUserName;
    private String casPassWord;

    private String derivePostUrl(HttpCoreContext context, CasLoginPageParser parser) throws XPathExpressionException, URISyntaxException {
        String formUrl = parser.getFormUrl();
        final URI requestUri = new URI(formUrl);
        if (!requestUri.isAbsolute()) {
            formUrl = context.getTargetHost().toURI() + formUrl;
        }
        return formUrl;
    }

    private void addLoginParameters(CasLoginPageParser parser, HttpPost httpPost) throws XPathExpressionException, UnsupportedEncodingException {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", casUserName));
        params.add(new BasicNameValuePair("password", casPassWord));
        params.add(new BasicNameValuePair("lt", parser.getLt()));
        params.add(new BasicNameValuePair("execution", parser.getExecution()));
        params.add(new BasicNameValuePair("_eventId", parser.getEventId()));
        params.add(new BasicNameValuePair("submit", "LOGIN"));
        httpPost.setEntity(new UrlEncodedFormEntity(params));
    }

    public CasHttpClient(String casPrefix, int maxConnections) {
        // Create an HttpClient with the ThreadSafeClientConnManager.
        // This connection manager must be used if more than one thread will
        // be using the HttpClient.
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(maxConnections);
        client = HttpClientBuilder.create()
                .setConnectionManager(cm)
                .build();

        this.casPrefix = casPrefix;
    }

    public <T> T get(String url, ResponseHandler<? extends T> responseHandler) throws IOException {
        HttpCoreContext context = new HttpCoreContext();
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response;
        try {
            response = client.execute(httpGet, context);
            if (isLoginRequired(context)) {
                try {
                    String location = loginToCas(response, context);
                    response = client.execute(new HttpGet(location), context);
                } catch (Exception e) {
                    log.error(e.getClass().getName(), e);
                    throw new ClientProtocolException("Unable to parse CAS Login page.", e);
                }

            }
            return responseHandler.handleResponse(response);
        } finally {
            httpGet.releaseConnection();
        }
    }

    public boolean isLoginRequired(HttpCoreContext context) {
        HttpHost targetHost = context.getTargetHost();
        String url_prefix = targetHost.toURI();
        final String uri = context.getRequest().getRequestLine().getUri();
        String responseUrl = url_prefix + uri;
        return responseUrl.startsWith(casPrefix);
    }

    private String loginToCas(HttpResponse response, HttpCoreContext context) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException, URISyntaxException {
        log.info("Logging in to CAS server");
        CasLoginPageParser parser = new CasLoginPageParser(response.getEntity().getContent());
        HttpPost httpPost = new HttpPost(derivePostUrl(context, parser));
        try {
            addLoginParameters(parser, httpPost);
            HttpResponse loginResponse = client.execute(httpPost,context);
            log.info("response: {}", loginResponse.getStatusLine());
            if (loginResponse.getStatusLine().getStatusCode() == 302) {
                return loginResponse.getFirstHeader("Location").getValue();

            } else {
                log.error("Something went wrong logging into CAS, redirect expected");
                throw new IOException("Unable to log in into CAS");
            }
        } finally {
            httpPost.releaseConnection();
        }
    }

    public String get(String url) throws IOException {
        return get(url, new BasicResponseHandler());
    }

    public HttpResponse head(String url) throws IOException {
        HttpCoreContext context = new HttpCoreContext();
        HttpHead httpHead = new HttpHead(url);
        try {
            HttpResponse response = client.execute(httpHead,context);
            if (isLoginRequired(context)) {
                try {
                    String location = loginToCas(response, context);
                    response = client.execute(new HttpHead(location),context);
                } catch (Exception e) {
                    log.error(e.getClass().getName(), e);
                    throw new ClientProtocolException("Unable to parse CAS Login page.", e);
                }

            }
            return response;

        } catch (IOException e) {
            log.error("IOException caught:", e);
            throw e;
        } finally {
            httpHead.releaseConnection();
        }

    }

    public <T> T post(String url, List<NameValuePair> params, ResponseHandler<? extends T> responseHandler) throws IOException {
        HttpCoreContext context = new HttpCoreContext();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new UrlEncodedFormEntity(params));
        HttpResponse response = client.execute(httpPost,context);
        if (isLoginRequired(context)) {
            try {
                loginToCas(response, context);
                response = client.execute(httpPost,context);
            } catch (Exception e) {
                log.error(e.getClass().getName(), e);
                throw new ClientProtocolException("Unable to parse CAS Login page.", e);
            }

        }
        return responseHandler.handleResponse(response);
    }
}
