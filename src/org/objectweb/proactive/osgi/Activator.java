/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
package org.objectweb.proactive.osgi;

import java.util.Properties;

import javax.servlet.Servlet;

import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.rmi.ClassServerServlet;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;


/**
 * @author vlegrand
 * This is the entry point of the proActiveBundle
 */
public class Activator implements BundleActivator {
    private org.osgi.framework.ServiceRegistration reg = null;
    private ProActiveService service = null;
    private Servlet servlet;
    private BundleContext bc;
    private static final String aliasRes = "/";
    private static final String aliasServlet = ClassServerServlet.SERVLET_NAME;
    private OsgiParameters parameters = new OsgiParameters();

    static {
        ProActiveConfiguration.load();
    }

    public void start(BundleContext ctx) throws Exception {
        this.bc = ctx;
        //		
        //        System.out.println("port (bundles.property)==== " + this.bc.getProperty("org.osgi.service.http.port"));
        int port = Integer.parseInt(this.bc.getProperty(
                    "org.osgi.service.http.port"));
        this.servlet = new ClassServerServlet(port);
        boolean b = registerServlet();

        ////		
        //        System.out.println("--> " + b);
        service = new ProActiveServicesImpl();
        Properties props = new Properties();
        props.put("name", "proactive");
        //Register the service
        reg = bc.registerService("org.objectweb.proactive.osgi.ProActiveService",
                service, props);
    }

    public void stop(BundleContext arg0) throws Exception {
        // TODO implements	
    }

    private boolean registerServlet() {
        //TODO implements with listener
        //         System.out.println(" ------------------- Enregistrement de la servlet de test");
        ServiceReference ref = bc.getServiceReference(
                "org.osgi.service.http.HttpService");

        //        System.out.println("** REF HTTP : = " + ref);             
        if (ref != null) {
            try {
                HttpService http = (HttpService) bc.getService(ref);
                http.registerServlet(aliasServlet, this.servlet, null, null);
                return true;
            } catch (Throwable t) {
                t.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }
}
