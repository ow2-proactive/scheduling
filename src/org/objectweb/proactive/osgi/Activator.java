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

import java.rmi.AlreadyBoundException;
import java.util.Properties;

import javax.servlet.Servlet;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.rmi.ClassServer;
import org.objectweb.proactive.core.rmi.ClassServerServlet;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;


/**
 * @author vlegrand
 * This is the entry point of the proActiveBundle
 */
public class Activator implements BundleActivator, ServiceListener {
    private static final String aliasRes = "/";
    private static final String aliasServlet = ClassServerServlet.SERVLET_NAME;
    private static final String OSGI_NODE_NAME = "OSGiNode";

    static {
        ProActiveConfiguration.load();
    }

    private org.osgi.framework.ServiceRegistration reg = null;
    private ProActiveService service = null;
    private Servlet servlet;
    private BundleContext bc;
    private OsgiParameters parameters = new OsgiParameters();
    private HttpService http;
    private Node node;

    public void start(BundleContext ctx) throws Exception {
        this.bc = ctx;

        ServiceReference ref = bc.getServiceReference(
                "org.osgi.service.http.HttpService");

        if (ref != null) {
            this.http = (HttpService) bc.getService(ref);
            createProActiveService();
        }

        this.bc.addServiceListener(this);
    }

    public void stop(BundleContext context) throws Exception {
        try {
            this.node.killAllActiveObjects();
            System.out.println("Arret du noeud proactive");
            ProActiveRuntimeImpl.getProActiveRuntime().killNode(OSGI_NODE_NAME);
        } catch (ProActiveRuntimeException e) {
        }
    }

    private void createProActiveService() {
        int port = Integer.parseInt(this.bc.getProperty(
                    "org.osgi.service.http.port"));
        this.servlet = new ClassServerServlet(port);

        boolean b = registerServlet();
        Properties props = new Properties();
        props.put("name", "proactive");

        try {
            this.node = NodeFactory.createNode(ClassServer.getUrl() + '/' +
                    OSGI_NODE_NAME);
        } catch (NodeException e) {
            System.out.println("Unable to create OsgiNode");
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            System.out.println("The OsgiNode is already bound in the registry");
            e.printStackTrace();
        }

        //Register the service
        this.service = new ProActiveServicesImpl(this.node);

        reg = bc.registerService("org.objectweb.proactive.osgi.ProActiveService",
                service, props);
    }

    private boolean registerServlet() {
        try {
            this.http.registerServlet(aliasServlet, this.servlet, null, null);

            return true;
        } catch (Throwable t) {
            t.printStackTrace();

            return false;
        }
    }

    public void serviceChanged(ServiceEvent event) {
        switch (event.getType()) {
        case ServiceEvent.REGISTERED:
            ServiceReference sr = (ServiceReference) event.getSource();
            Object service = this.bc.getService(sr);

            if ((service != null) && service instanceof HttpService) {
                this.http = (HttpService) service;
                this.createProActiveService();
            }

            //        this.createProActiveService(); 
            break;
        default:
            break;
        }
    }
}
