package com.melexis.cashttpclient;

import com.melexis.cashttpclient.mock.CasSecuredServer;
import com.melexis.cashttpclient.mock.MockCasServer;
import com.melexis.cashttpclient.mock.SimplestServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.StringContains.containsString;

public class CasHttpClientTest {

    public static String SIMPLE_URL = "http://localhost:13002/simple.html";
    private SimplestServer server;
    private CasHttpClient client;
    private MockCasServer casServer;
    private CasSecuredServer secureServer;

    @Test
    public void testSimpleUrl() throws Exception {

        String result = client.get(SIMPLE_URL);
        assertThat(result, containsString("Simple Site"));

    }

    @Before
    public void setUp() throws Exception {
        server = new SimplestServer();
        casServer = new MockCasServer();
        secureServer = new CasSecuredServer();
        client = new CasHttpClient(CasSecuredServer.CAS_URL, 100);
        client.setUsername("test_user");
        client.setPassword("test_pass");
    }

    @Test
    public void testCasProtectedUrl() throws Exception {

        String result = client.get(CasSecuredServer.SERVICE_URL);
        assertThat(result, containsString("Logged in"));

    }

    @After
    public void tearDown() throws Exception {
        secureServer.stop();
        server.stop();
        casServer.stop();
    }

}
