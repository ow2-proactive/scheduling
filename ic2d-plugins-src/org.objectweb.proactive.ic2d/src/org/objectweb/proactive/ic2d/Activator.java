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
package org.objectweb.proactive.ic2d;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.objectweb.proactive.core.ssh.httpssh.Handler;
import org.osgi.framework.BundleContext;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;
import org.osgi.service.url.URLStreamHandlerSetter;


/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
    // The plug-in ID
    public static final String PLUGIN_ID = "org.objectweb.proactive.ic2d";

    // The shared instance
    private static Activator plugin;

    /**
     * The constructor
     */
    public Activator() {
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);

        /* This code is used to the httpssh, fixes an Eclipse bug */
        Hashtable<String, String[]> properties = new Hashtable<String, String[]>(1);
        properties.put(URLConstants.URL_HANDLER_PROTOCOL,
            new String[] { "httpssh" });
        String serviceClass = URLStreamHandlerService.class.getName();
        context.registerService(serviceClass, new IC2DHandler(), properties);
        /* --------------------------------------------------- */
        super.start(context);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given
     * plug-in relative path
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    //
    //---INNER CLASS---------------------------------------------
    //
    public class IC2DHandler extends Handler implements URLStreamHandlerService {
        protected URLStreamHandlerSetter realHandler;

        public boolean equals(URL u1, URL u2) {
            return super.equals(u1, u2);
        }

        public int getDefaultPort() {
            return super.getDefaultPort();
        }

        public InetAddress getHostAddress(URL u) {
            return super.getHostAddress(u);
        }

        public int hashCode(URL u) {
            return super.hashCode(u);
        }

        public boolean hostsEqual(URL u1, URL u2) {
            return super.hostsEqual(u1, u2);
        }

        public URLConnection openConnection(URL u) throws IOException {
            return super.openConnection(u);
        }

        public void parseURL(URLStreamHandlerSetter realHandler, URL u,
            String spec, int start, int limit) {
            this.realHandler = realHandler;
            super.parseURL(u, spec, start, limit);
        }

        public boolean sameFile(URL u1, URL u2) {
            return super.sameFile(u1, u2);
        }

        public String toExternalForm(URL u) {
            return super.toExternalForm(u);
        }

        /**
         * This method calls
         * <code>realHandler.setURL(URL,String,String,int,String,String)</code>.
         *
         * @see "java.net.URLStreamHandler.setURL(URL,String,String,int,String,String)"
         * @deprecated This method is only for compatibility with handlers written
         *             for JDK 1.1.
         */
        protected void setURL(URL u, String proto, String host, int port,
            String file, String ref) {
            realHandler.setURL(u, proto, host, port, file, ref);
        }

        /**
         * This method calls
         * <code>realHandler.setURL(URL,String,String,int,String,String,String,String)</code>.
         *
         * @see "java.net.URLStreamHandler.setURL(URL,String,String,int,String,String,String,String)"
         */
        protected void setURL(URL u, String proto, String host, int port,
            String auth, String user, String path, String query, String ref) {
            realHandler.setURL(u, proto, host, port, auth, user, path, query,
                ref);
        }
    }
}
