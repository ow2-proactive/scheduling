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
package org.objectweb.proactive.extensions.webservices.soap;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.soap.SOAPException;
import org.apache.soap.server.DefaultConfigManager;
import org.apache.soap.server.DeploymentDescriptor;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.extensions.webservices.WSConstants;


/**
 * This servlet return a WSDL document when called with the urn of the service
 * @author vlegrand
 */
public class WsdlServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        DefaultConfigManager cm = new DefaultConfigManager();
        PrintWriter out = res.getWriter();
        res.setHeader("content-type", "text/xml");

        try {
            String id = req.getParameter("id");

            if (id == null) {
                error(out);

                return;
            }

            Hashtable<String, String> options = new Hashtable<String, String>();
            String catalinaBase = PAProperties.CATALINA_BASE.getValue();
            options.put("filename", catalinaBase + "/webapps/" + WSConstants.WEBAPP_NAME +
                "/DeployedServices.ds");
            cm.setOptions(options);
            cm.loadRegistry();

            String[] liste = cm.list();

            DeploymentDescriptor dd = cm.query(id);
            if (dd == null) {
                error(out);

                return;
            }

            @SuppressWarnings("unchecked")
            Hashtable table = dd.getProps();
            String wsdl = (String) table.get("Wsdl");

            out.println(wsdl);
        } catch (SOAPException e) {
            e.printStackTrace();
        }
    }

    private void error(PrintWriter out) {
        out.println("This services doesn't exists on this server.");
    }
}
