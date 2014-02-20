package com.melexis.cashttpclient;

import com.melexis.cashttpclient.mock.CasSecuredServer;
import com.melexis.cashttpclient.mock.MockCasServer;
import com.melexis.cashttpclient.mock.SimplestServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Created by pti on 19/02/14.
 */
public class MockCasServerTest {

    MockCasServer casServer;
    CasSecuredServer simplestServer;
    WebDriver driver;


    @Before
    public void setUp() throws Exception {
        casServer = new MockCasServer();
        simplestServer = new CasSecuredServer();

        driver = new HtmlUnitDriver();
    }

    @After
    public void tearDown() throws Exception {
        simplestServer.stop();
        casServer.stop();
    }

    @Test
    public void testLoginPage() throws UnsupportedEncodingException {
        WebDriver driver = new HtmlUnitDriver();

        driver.get(CasSecuredServer.CAS_URL_PREFIX
                + URLEncoder.encode(CasSecuredServer.SERVICE_URL, "utf-8"));

        // Find the text input element by its name
        WebElement username = driver.findElement(By.name("username"));
        WebElement password = driver.findElement(By.name("password"));

        assertNotNull(username);
        assertNotNull(password);
    }

    @Test
    public void testLoginPost() throws UnsupportedEncodingException {
        driver.get(CasSecuredServer.CAS_URL_PREFIX
                + URLEncoder.encode(CasSecuredServer.SERVICE_URL, "utf-8"));

        // Find the text input element by its name
        WebElement username = driver.findElement(By.name("username"));
        WebElement password = driver.findElement(By.name("password"));

        username.sendKeys("test_user");
        password.sendKeys("test_pass");

        password.submit();


        // WebElement body = driver.findElement(By.id("hello"));
        final String path = driver.getCurrentUrl().split("\\?")[0];
        assertThat("CAS should redirect to service url",
                CasSecuredServer.SERVICE_URL,
                is(path));


    }
}
