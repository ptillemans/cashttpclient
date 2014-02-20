package com.melexis.cashttpclient.mock;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
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
public class CasSecuredServer {

    public static final String SERVICE_URL = "http://localhost:13001/hello.html";
    public static final String CAS_URL = "http://localhost:13000/login";
    public static final String CAS_URL_PREFIX = CAS_URL + "?service=";
    public static final String TGT_SAMPLE_TICKET = "TGT-SampleTicket";
    Server server;

    class CasHandler extends AbstractHandler {

        @Override
        public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
            HttpSession session = request.getSession(true);
            String ticket = (String) session.getAttribute("ticket");

            if (ticket != null && ticket.equals(TGT_SAMPLE_TICKET)) {
                // logged in already, proceed
                return;
            } else {
                // check if we got the ticket as a parameter
                ticket = request.getParameter("ticket");
                if (ticket != null && ticket.equals(TGT_SAMPLE_TICKET)) {
                    // store ticket in session
                    session = request.getSession(true);
                    session.setAttribute("ticket", ticket);
                } else {
                    // send to CAS server if no ticket is presented
                    httpServletResponse.sendRedirect(CAS_URL_PREFIX + URLEncoder.encode(SERVICE_URL, "utf-8"));
                    request.setHandled(true);
                }
            }
        }
    }

    public CasSecuredServer() throws Exception {
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setResourceBase("./src/test/resources");
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{
                new SessionHandler(),
                new CasHandler(),
                resource_handler,
                new DefaultHandler()
        });
        server = new Server(13001);
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
        CasSecuredServer server = new CasSecuredServer();
        server.join();
    }
}