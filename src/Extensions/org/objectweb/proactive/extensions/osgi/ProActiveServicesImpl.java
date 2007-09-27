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
package org.objectweb.proactive.extensions.osgi;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.AlreadyBoundException;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.rmi.ClassServerServlet;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.ungoverned.gravity.servicebinder.ServiceBinderContext;


/**
 * @see org.objectweb.proactive.osgi.ProActiveService
 * @author vlegrand
 *
 */
public class ProActiveServicesImpl implements ProActiveService {
    private Node node;
    private Servlet servlet;
    private BundleContext bc;
    private static final String aliasServlet = "/" +
        ClassServerServlet.SERVLET_NAME;
    private static final String OSGI_NODE_NAME = "OSGiNode";
    private HttpService http;
    private int port;

    static {
        ProActiveConfiguration.load();
    }

    public ProActiveServicesImpl() {
    }

    public ProActiveServicesImpl(ServiceBinderContext context) {
        this.bc = context.getBundleContext();
        this.port = Integer.parseInt(this.bc.getProperty(
                    "org.osgi.service.http.port"));
    }

    public void setNode(Node node) {
        this.node = node;
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#newActive(java.lang.String, java.lang.Object[])
     */
    public Object newActive(String className, Object[] params)
        throws ActiveObjectCreationException, NodeException {
        return ProActiveObject.newActive(className, params, this.node);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#register(java.lang.Object, java.lang.String)
     */
    public void register(Object obj, String url) throws IOException {
        ProActiveObject.register(obj, url);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#unregister(URI)
     */
    public void unregister(String url) throws IOException {
        ProActiveObject.unregister(url);
    }

    /**
     *
     * @param ref
     */
    public void bind(HttpService ref) {
        this.http = ref;
        createProActiveService();
    }

    /**
     *
     * @param ref
     */
    public void unbind(HttpService ref) {
        System.out.println(
            "Node is no more accessible by Http, temination of ProActiveService");
        terminate();
    }

    /**
     *
     *
     */
    private void createProActiveService() {
        this.servlet = new ClassServerServlet(port);
        boolean b = registerServlet();
        createNode();
    }

    /**
     *
     *
     */
    private void createNode() {
        //    	System.out.println("url du class server = ");
        try {
            Thread.currentThread()
                  .setContextClassLoader(ProActiveServicesImpl.class.getClassLoader());
            this.node = NodeFactory.createNode(OSGI_NODE_NAME);
        } catch (NodeException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @return
     */
    private boolean registerServlet() {
        try {
            HttpContext myContext = new HttpContext() {
                    public boolean handleSecurity(HttpServletRequest arg0,
                        HttpServletResponse arg1) throws IOException {
                        return false;
                    }

                    public URL getResource(String arg0) {
                        try {
                            return new URL(aliasServlet + "?" + arg0);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    public String getMimeType(String arg0) {
                        return null;
                    }
                };

            this.http.registerServlet(aliasServlet, this.servlet, null, null);
            //            this.http.registerResources("/", aliasServlet + "doc", myContext);
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    /**
     *
     */
    public void terminate() {

        /* kill Nodes */
        try {
            this.node.killAllActiveObjects();
            //            ProActiveRuntimeImpl.getProActiveRuntime().killNode(OSGI_NODE_NAME);
        } catch (NodeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ProActiveException e) {
            e.printStackTrace();
        }

        /* unregister Servlet */
        //this.http.unregister(aliasServlet);
    }

    public Object lookupActive(String className, String url) {
        try {
            return ProActiveObject.lookupActive(className, url);
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
