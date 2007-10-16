/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.rmi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.util.URIBuilder;


public class ClassServerServlet extends HttpServlet {
    //	public static final String WEB_ROOT = "/proactive";
    public static final String SERVLET_NAME = "ProActiveHTTP";
    private String url;
    private ClassServer classServer;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private static int port;

    public ClassServerServlet(int newport) {
        ClassServerServlet.port = newport;
        ClassServerHelper helper = new ClassServerHelper();
        System.setProperty("proactive.http.port", ClassServerServlet.port + "");
        try {
            helper.initializeClassServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getPort() {
        return port;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        //        try {
        //        System.out.println(
        //            "*************************** DO GET ****************************** ");
        doPost(request, response);
        //            PrintWriter out = response.getWriter();
        //            out.println("Some informations about the ProActive runtime : \n");
        //            out.println("Proactive communication protocol = " +
        //                System.getProperty("proactive.communication.protocol") + "\n");
        //
        //            out.println("servlet Enabled =  " +
        //                ProActiveConfiguration.osgiServletEnabled());
        //            out.println("Servlet url = " + ClassServerServlet.getUrl());
        //        } catch (IOException e) {
        //            e.printStackTrace();
        //        }
    }

    @Override
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
            //            int port = Integer.parseInt(System.getProperty(
            //                        "proactive.http.port"));
            String url = Constants.XMLHTTP_PROTOCOL_IDENTIFIER + "://" +
                URIBuilder.getHostNameorIP(java.net.InetAddress.getLocalHost()) +
                ':' + port + '/' + SERVLET_NAME;

            //                    Constants.XMLHTTP_PROTOCOL_IDENTIFIER, port); /*+
            //            "/" + SERVLET_NAME ;*/

            //            if (url.charAt(url.length() - 1) == '/') {
            //                url = url.substring(0, url.length() - 1);
            //            }
            //            url += SERVLET_NAME + '/';
            return url;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }
}
