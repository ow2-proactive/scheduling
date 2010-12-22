/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds 
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.resourcemanager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.ssh.httpssh.Handler;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;
import org.osgi.service.url.URLStreamHandlerSetter;
import org.ow2.proactive.resourcemanager.gui.Internal;


/**
 * The activator class controls the plug-in life cycle
 */
public final class Activator extends AbstractUIPlugin {

    /** The plug-in ID */
    public static final String PLUGIN_ID = "org.ow2.proactive.resourcemanager";

    /** The shared instance */
    private static Activator plugin;

    /**
     * The constructor
     */
    public Activator() {
        plugin = this;
    }

    /**
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        // Call the upper class method as required (see AbstractUIPlugin#start javadoc) 
        super.start(context);

        // Customize the platform instance location 
        final Location instanceLoc = Platform.getInstanceLocation();
        URL customLocURL = null;
        try {
            customLocURL = new URL("file:" + System.getProperty("user.home") +
                "/.ProActive_ResourceManager/workspace/");
            instanceLoc.set(customLocURL, false);
        } catch (Exception e) {
            if (e instanceof IllegalStateException) {
                System.err.println("Unable to set the platform instance location to " + customLocURL);
                System.err.println("The current location is " + instanceLoc.getURL());
                System.err.println("Be sure that the program arguments contains -data @noDefault");
            }
            e.printStackTrace();
        }

        // Get the bundle (this real location of this plugin)
        final Bundle bundle = context.getBundle();
        final URL configFolderURL = FileLocator.toFileURL(bundle.getEntry("/config"));

        // Specify the security policy file if it was not specified
        final String securityPolicyProperty = System.getProperty("java.security.policy");
        if (securityPolicyProperty == null) {
            System.setProperty("java.security.policy", configFolderURL.toString() +
                "resource.manager.java.policy");
        }

        // Specify default log4j configuration file if it was not specified
        final String log4jConfProperty = System.getProperty(CentralPAPropertyRepository.LOG4J.getName());
        if (log4jConfProperty == null) {
            System.setProperty(CentralPAPropertyRepository.LOG4J.getName(), configFolderURL.toString() +
                "proactive-log4j");
        }

        // Specify default ProActive configuration file if it was not specified
        final String paConfProperty = System.getProperty(CentralPAPropertyRepository.PA_CONFIGURATION_FILE
                .getName());
        if (paConfProperty == null) {
            System.setProperty(CentralPAPropertyRepository.PA_CONFIGURATION_FILE.getName(), configFolderURL
                    .toString() +
                "ProActiveConfiguration.xml");
        }

        // Load proactive configuration 
        ProActiveConfiguration.load();

        // This code is used to the httpssh, fixes an Eclipse bug
        Hashtable<String, String[]> properties = new Hashtable<String, String[]>(1);
        properties.put(URLConstants.URL_HANDLER_PROTOCOL, new String[] { "httpssh" });
        String serviceClass = URLStreamHandlerService.class.getName();
        context.registerService(serviceClass, new IC2DHandler(), properties);
    }

    /**
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Initializes an image registry with images which are frequently used by the plugin. 
     * @see the registry to initialize
     */
    @Override
    protected void initializeImageRegistry(final ImageRegistry reg) {
        super.initializeImageRegistry(reg);
        reg.put(Internal.IMG_ADDNODE, Activator.getImageDescriptor("icons/" + Internal.IMG_ADDNODE));
        reg.put(Internal.IMG_BUSY, Activator.getImageDescriptor("icons/" + Internal.IMG_BUSY));
        reg.put(Internal.IMG_COLLAPSEALL, Activator.getImageDescriptor("icons/" + Internal.IMG_COLLAPSEALL));
        reg.put(Internal.IMG_CONNECT, Activator.getImageDescriptor("icons/" + Internal.IMG_CONNECT));
        reg
                .put(Internal.IMG_CREATESOURCE, Activator.getImageDescriptor("icons/" +
                    Internal.IMG_CREATESOURCE));
        reg.put(Internal.IMG_DISCONNECT, Activator.getImageDescriptor("icons/" + Internal.IMG_DISCONNECT));
        reg.put(Internal.IMG_DOWN, Activator.getImageDescriptor("icons/" + Internal.IMG_DOWN));
        reg.put(Internal.IMG_EXPANDALL, Activator.getImageDescriptor("icons/" + Internal.IMG_EXPANDALL));
        reg.put(Internal.IMG_FREE, Activator.getImageDescriptor("icons/" + Internal.IMG_FREE));
        reg.put(Internal.IMG_HOST, Activator.getImageDescriptor("icons/" + Internal.IMG_HOST));
        reg.put(Internal.IMG_DEPLOYING, Activator.getImageDescriptor("icons/" + Internal.IMG_DEPLOYING));
        reg.put(Internal.IMG_LOST, Activator.getImageDescriptor("icons/" + Internal.IMG_LOST));
        reg.put(Internal.IMG_REMOVENODE, Activator.getImageDescriptor("icons/" + Internal.IMG_REMOVENODE));
        reg
                .put(Internal.IMG_REMOVESOURCE, Activator.getImageDescriptor("icons/" +
                    Internal.IMG_REMOVESOURCE));
        reg.put(Internal.IMG_RMSHUTDOWN, Activator.getImageDescriptor("icons/" + Internal.IMG_RMSHUTDOWN));
        reg.put(Internal.IMG_SOURCE, Activator.getImageDescriptor("icons/" + Internal.IMG_SOURCE));
        reg.put(Internal.IMG_TORELEASE, Activator.getImageDescriptor("icons/" + Internal.IMG_TORELEASE));
        reg.put(Internal.IMG_CONFIGURING, Activator.getImageDescriptor("icons/" + Internal.IMG_CONFIGURING));
        reg.put(Internal.IMG_EXIT, Activator.getImageDescriptor("icons/" + Internal.IMG_EXIT));
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

    /**
     * Logs into the RCP's log file.
     * @param severity - the severity, see IStatus
     * @param message
     * @param t
     */
    public static void log(int severity, String message, Throwable t) {
        IStatus status = new Status(severity, PLUGIN_ID, IStatus.OK, message, t);
        Activator.getDefault().getLog().log(status);
    }

    //
    //---INNER CLASS---------------------------------------------
    //
    public class IC2DHandler extends Handler implements URLStreamHandlerService {
        protected URLStreamHandlerSetter realHandler;

        @Override
        public boolean equals(URL u1, URL u2) {
            return super.equals(u1, u2);
        }

        @Override
        public int getDefaultPort() {
            return super.getDefaultPort();
        }

        @Override
        public InetAddress getHostAddress(URL u) {
            return super.getHostAddress(u);
        }

        @Override
        public int hashCode(URL u) {
            return super.hashCode(u);
        }

        @Override
        public boolean hostsEqual(URL u1, URL u2) {
            return super.hostsEqual(u1, u2);
        }

        @Override
        public URLConnection openConnection(URL u) throws IOException {
            return super.openConnection(u);
        }

        public void parseURL(URLStreamHandlerSetter realHandler, URL u, String spec, int start, int limit) {
            this.realHandler = realHandler;
            super.parseURL(u, spec, start, limit);
        }

        @Override
        public boolean sameFile(URL u1, URL u2) {
            return super.sameFile(u1, u2);
        }

        @Override
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
        @Override
        protected void setURL(URL u, String proto, String host, int port, String file, String ref) {
            realHandler.setURL(u, proto, host, port, file, ref);
        }

        /**
         * This method calls
         * <code>realHandler.setURL(URL,String,String,int,String,String,String,String)</code>.
         *
         * @see "java.net.URLStreamHandler.setURL(URL,String,String,int,String,String,String,String)"
         */
        @Override
        protected void setURL(URL u, String proto, String host, int port, String auth, String user,
                String path, String query, String ref) {
            realHandler.setURL(u, proto, host, port, auth, user, path, query, ref);
        }
    }
}
