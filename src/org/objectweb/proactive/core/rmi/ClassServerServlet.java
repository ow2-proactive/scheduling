package org.objectweb.proactive.core.rmi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.osgi.OsgiParameters;


public class ClassServerServlet extends HttpServlet {
    public static final String SERVLET_NAME = "/ProActiveHTTP";
    private String url;
    private ClassServer classServer;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private int port;

    public ClassServerServlet(int port) {
        this.port = port;
        ClassServerHelper helper = new ClassServerHelper();
        System.setProperty("proactive.http.port", this.port + "");
        try {
            helper.initializeClassServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            System.out.println(" -- >  doGet");
            PrintWriter out = response.getWriter();
            out.println("Some informations about the ProActive runtime : \n");
            out.println("Proactive communication protocol = " +
                System.getProperty("proactive.communication.protocol") + "\n");

            out.println("servlet Enabled =  " +
                OsgiParameters.servletEnabled());
            out.println("Servlet url = " + ClassServerServlet.getUrl());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            this.request = request;
            this.response = response;
            InputStream in = request.getInputStream();
            OutputStream out = response.getOutputStream();
            RequestInfo reqInfo = new RequestInfo();
            reqInfo.read(this.request);

            HTTPRequestHandler service = new HTTPRequestHandler(in, out,
                    reqInfo, response);
            service.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getUrl() {
        try {
            int port = Integer.parseInt(System.getProperty(
                        "proactive.http.port"));
            return UrlBuilder.buildUrl(UrlBuilder.getHostNameorIP(
                    java.net.InetAddress.getLocalHost()), "", "http:", port) +
            "/" + SERVLET_NAME;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }
}
