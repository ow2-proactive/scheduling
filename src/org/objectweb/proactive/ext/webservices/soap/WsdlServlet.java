/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.ext.webservices.soap;

import org.apache.soap.SOAPException;
import org.apache.soap.server.DefaultConfigManager;
import org.apache.soap.server.DeploymentDescriptor;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.Hashtable;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * This servlet return a WSDL document whn called with the urn of the service
 * @author vlegrand
  */
public class WsdlServlet extends HttpServlet {
    public void doGet(HttpServletRequest req, HttpServletResponse res)
        throws IOException {
        DefaultConfigManager cm = new DefaultConfigManager();
        PrintWriter out = res.getWriter();
        res.setHeader("content-type", "text/xml");

        try {
            String id = req.getParameter("id");
            System.out.println("id = " + id);

            if (id == null) {
                error(out);

                return;
            }

            Hashtable options = new Hashtable();

            //System.out.println("user dir = " + System.getProperty("user.dir"));
            options.put("filename", "webapps/soap/DeployedServices.ds");
            cm.setOptions(options);
            cm.loadRegistry();

            String[] liste = cm.list();
            System.out.println("liste = " + liste);

            for (int i = 0; i < liste.length; i++) {
                System.out.println("**" + liste[i]);
            }

            DeploymentDescriptor dd = cm.query(id);
            System.out.println("dd = " + dd);

            if (dd == null) {
                error(out);

                return;
            }

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
