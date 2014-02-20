package com.melexis.cashttpclient.mock;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.server.session.SessionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * Generate a fake CAS Server
 */
public class MockCasServer {

    private static final Logger log = LoggerFactory.getLogger(MockCasServer.class);
    private int port;
    Server server;

    class CasServerHandler extends AbstractHandler {

        @Override
        public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
            HttpSession session = request.getSession(true);

            request.extractParameters();
            String service = request.getParameter("service");
            final String username = request.getParameter("username");
            final String password = request.getParameter("password");

            // store the service in the session for redirecting on POST
            if (service != null) {
                session.setAttribute("service",service);
            } else {
                service = (String) session.getAttribute("service");
            }

            // check if this is a login request
            if (request.getMethod().equals("POST")
                    && username.equals("test_user")
                    && password.equals("test_pass")) {

                log.info("Logging in to service as {}", username);
                log.info("redirectiong to {}", service);
                // store ticket in session
                final String ticket = CasSecuredServer.TGT_SAMPLE_TICKET;
                session.setAttribute("ticket", ticket);
                httpServletResponse.sendRedirect(service + "?ticket=" + URLEncoder.encode(ticket,"utf-8"));
                request.setHandled(true);
            }

            // redirect to the service if we are already logged in
            final String ticket = (String) session.getAttribute("ticket");
            final String pathInfo = request.getPathInfo();
            if (ticket != null
                    && ticket.equals(CasSecuredServer.TGT_SAMPLE_TICKET)
                    && pathInfo.startsWith("/login")) {
                httpServletResponse.sendRedirect(service + "?ticket=" + URLEncoder.encode(ticket,"utf-8"));
                request.setHandled(true);
            }
        }
    }

    public MockCasServer() throws Exception {
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setResourceBase("./src/test/resources");
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] {
                new SessionHandler(),
                new CasServerHandler(),
                resource_handler,
                new DefaultHandler()
        });
        server = new Server(13000);
        server.setHandler(handlers);
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }

    public void join() throws Exception {
        server.join();
    }

    public static void main (String[] args) throws Exception {
        MockCasServer server = new MockCasServer();
        server.join();
    }
}
