package com.melexis.cashttpclient.mock;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.server.session.SessionHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * The simplest possible Jetty server.
 */
public class SimplestServer {

    Server server;

    public SimplestServer() throws Exception {
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setResourceBase("./src/test/resources");
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{
                resource_handler,
                new DefaultHandler()
        });
        server = new Server(13002);
        server.setHandler(handlers);
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }

    public void join() throws Exception {
        server.join();
    }

    public static void main(String[] args) throws Exception {
        SimplestServer server = new SimplestServer();
        server.join();
    }
}