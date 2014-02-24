package com.melexis.cashttpclient;

import org.apache.http.*;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpCoreContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
@ThreadSafe
public class CasHttpClient {

    private static Logger log = LoggerFactory.getLogger(CasHttpClient.class);

    HttpClient client;

    private String username;
    private String password;

    String casPrefix;

    public void setUsername(String username) {
        this.username = username;
    }
    public void setPassword(String password) {
        this.password = password;
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
        HttpRequestBase httpRequest = new HttpGet(url);
        return getHttpResponse(httpRequest, responseHandler);
    }

    public String get(String url) throws IOException {
        return get(url, new BasicResponseHandler());
    }

    /**
     * Return the HttpResponse to a head request.
      
     * This method is called when the content of the headers are needed.
     * It does not use the default response to string handler, but return
     * the HttpResponse object directly.
     *
     * @param url           the url to get the head info from
     * @return response     the HttpResponse object
     * @throws IOException
     */
    public HttpResponse head(String url) throws IOException {
        HttpRequestBase httpRequest = new HttpHead(url);
        // use a dummy response handler to return the response.
        return getHttpResponse(httpRequest, new ResponseHandler<HttpResponse>() {
            @Override
            public HttpResponse handleResponse(HttpResponse response) throws IOException {
                return response;
            }
        });

    }

    public <T> T post(String url, List<NameValuePair> params, ResponseHandler<? extends T> responseHandler) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new UrlEncodedFormEntity(params));
        return getHttpResponse(httpPost, responseHandler);
    }

    public String post(String url, List<NameValuePair> params) throws IOException {
        return post(url,params, new BasicResponseHandler());
    }

    public <T> T put(String url, List<NameValuePair> params, ResponseHandler<? extends T> responseHandler) throws IOException {
        HttpPut httpPut = new HttpPut(url);
        httpPut.setEntity(new UrlEncodedFormEntity(params));
        return getHttpResponse(httpPut, responseHandler);
    }

    public String put(String url, List<NameValuePair> params) throws IOException {
        return put(url, params, new BasicResponseHandler());
    }

    public <T> T delete(String url, ResponseHandler<? extends T> responseHandler) throws IOException {
        HttpDelete httpDelete = new HttpDelete(url);
        return getHttpResponse(httpDelete, responseHandler);
    }

    public String delete(String url, List<NameValuePair> params) throws IOException {
        return post(url,params, new BasicResponseHandler());
    }


    private <T> T getHttpResponse(HttpRequestBase httpRequest, ResponseHandler<? extends T> responseHandler) throws IOException {
        HttpCoreContext context = new HttpCoreContext();
        try {
            HttpResponse response = client.execute(httpRequest,context);
            if (isLoginRequired(context)) {
                try {
                    String location = loginToCas(response, context);
                    // replace the original URI with the one returned from CAS
                    // which contains the authentication ticket.
                    httpRequest.setURI(new URI(location));
                    response = client.execute(httpRequest,context);
                } catch (Exception e) {
                    log.error(e.getClass().getName(), e);
                    throw new ClientProtocolException("Unable to parse CAS Login page.", e);
                }

            }
            return responseHandler.handleResponse(response);

        } catch (IOException e) {
            log.error("IOException caught:", e);
            throw e;
        } finally {
            // free the connection as soon as we're done with it.
            httpRequest.releaseConnection();
        }
    }

    private boolean isLoginRequired(HttpCoreContext context) {
        HttpHost targetHost = context.getTargetHost();
        String url_prefix = targetHost.toURI();
        final String uri = context.getRequest().getRequestLine().getUri();
        String responseUrl = url_prefix + uri;
        return responseUrl.startsWith(casPrefix);
    }

    private String loginToCas(HttpResponse response, HttpCoreContext context) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException, URISyntaxException {
        log.info("Logging in to CAS server");
        CasLoginPage loginPage = new CasLoginPage(response.getEntity().getContent());
        HttpPost httpPost = new HttpPost(loginPage.derivePostUrl(context));
        try {
            httpPost.setEntity(loginPage.fillForm(username, password));
            HttpResponse loginResponse = client.execute(httpPost,context);
            log.info("response: {}", loginResponse.getStatusLine());
            if (loginResponse.getStatusLine().getStatusCode() == 302) {
                // after successful login we are redirected to the service
                // the service will be in the Location header
                return loginResponse.getFirstHeader("Location").getValue();
            } else {
                // apparently something went wrong logging in.
                log.error("Something went wrong logging into CAS, redirect expected");
                throw new IOException("Unable to log in into CAS");
            }
        } finally {
            httpPost.releaseConnection();
        }
    }

}
