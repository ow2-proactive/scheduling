/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler;

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
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.ssh.httpssh.Handler;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;
import org.osgi.service.url.URLStreamHandlerSetter;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingException;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingService;
import org.ow2.proactive.scheduler.gui.Internal;
import org.ow2.proactive.scheduler.gui.data.DataServers;
import org.ow2.proactive.scheduler.gui.perspective.SchedulerPerspectiveAdapter;


/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
    /** The plug-in ID */
    public static final String PLUGIN_ID = "org.ow2.proactive.scheduler";
    /** The name of the property that sets the log service provider */
    public static final String LOGSERVICE_CLASS_PROPERTY = "pa.scheduler.logs.provider";
    /** The shared instance */
    private static Activator plugin;
    /** Static reference to the log forwarding service */
    public static LogForwardingService lfs = null;

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
        // FIX "Could not set location once it is set"   
        if (!instanceLoc.isSet())
        {
	        URL customLocURL = null;
	        try {
	            customLocURL = new URL("file:" + System.getProperty("user.home") +
	                "/.ProActive_Scheduler/workspace/");
	            instanceLoc.set(customLocURL, false);
	        } catch (Exception e) {
	            if (e instanceof IllegalStateException) {
	                System.err.println("Unable to set the platform instance location to " + customLocURL);
	                System.err.println("The current location is " + instanceLoc.getURL());
	                System.err.println("Be sure that the program arguments contains -data @noDefault");
	            }
	            e.printStackTrace();
	        }
        }
        // Get the bundle (this real location of this plugin)
        final Bundle bundle = context.getBundle();
        final URL configFolderURL = FileLocator.toFileURL(bundle.getEntry("/config"));

        // Specify the security policy file if it was not specified
        final String securityPolicyProperty = System.getProperty("java.security.policy");
        if (securityPolicyProperty == null) {
            System.setProperty("java.security.policy", configFolderURL.toString() + "scheduler.java.policy");
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

    
        // Add a listener to the workbench to listen for perspective change and
		// perform custom actions
		// Add the listener once the workbenchh is fully started
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				// wait until the workbench has been initialized
				// if the workbench is still starting, reschedule the execution
				// of this runnable
				if (PlatformUI.getWorkbench().isStarting()) {
					Display.getDefault().timerExec(1000, this);
				} else { 
					// the workbench finished the initialization process 
					IWorkbenchWindow workbenchWindow = PlatformUI
							.getWorkbench().getActiveWorkbenchWindow();
					SchedulerPerspectiveAdapter schedPerspectiveListener = new SchedulerPerspectiveAdapter();
					workbenchWindow
							.addPerspectiveListener(schedPerspectiveListener);

					// update rmPerspectiveListener with the current perspective
					// we need to perform this update by hand as no event is
					// sent by the platform at
					// platform initialization and we need to have the current
					// perspective set
					schedPerspectiveListener.perspectiveActivated(PlatformUI
							.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage(), PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage()
							.getPerspective());
				}
			}// else - isStarting()
		});
    }

    /**
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(final BundleContext context) throws Exception {
        plugin = null;
        DataServers.cleanup();
        terminateLoggerServer();
        // Dispose the image registry
        final ImageRegistry reg = super.getImageRegistry();
        if (reg != null) {
            reg.dispose();
        }
        super.stop(context);
    }

    /**
     * Start a new logger server
     * @throws LogForwardingException if the logger server cannot be started
     */
    public static void startLoggerServer() throws LogForwardingException {
        // start the log server
        String logProviderClass = System.getProperty(LOGSERVICE_CLASS_PROPERTY);
        if (logProviderClass != null && !logProviderClass.equals("")) {
            lfs = new LogForwardingService(logProviderClass);
            lfs.initialize();
        } else {
            throw new LogForwardingException("Cannot find " + LOGSERVICE_CLASS_PROPERTY + " property");
        }
    }

    /**
     * Stop the current logger server.
     * @throws LogForwardingException if the logger server cannot be stopped
     */
    public static void terminateLoggerServer() throws LogForwardingException {
        if (lfs != null) {
            lfs.terminate();
        }
        lfs = null;
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
     * Logs into the RPC's log file
     * @param severity - the severity, see IStatus
     * @param message
     * @param t
     */
    public static void log(int severity, String message, Throwable t) {
        IStatus status = new Status(severity, Activator.getDefault().getBundle().getSymbolicName(),
            IStatus.OK, message, t);
        Activator.getDefault().getLog().log(status);
    }

    /**
     * Initializes an image registry with images which are frequently used by the plugin. 
     * @see the registry to initialize
     */
    @Override
    protected void initializeImageRegistry(final ImageRegistry reg) {
        super.initializeImageRegistry(reg);
        reg.put(Internal.IMG_CONNECT, Activator.getImageDescriptor("icons/" + Internal.IMG_CONNECT));
        reg.put(Internal.IMG_DISCONNECT, Activator.getImageDescriptor("icons/" + Internal.IMG_DISCONNECT));
        reg.put(Internal.IMG_FILEOBJ, Activator.getImageDescriptor("icons/" + Internal.IMG_FILEOBJ));
        reg.put(Internal.IMG_HORIZONTAL, Activator.getImageDescriptor("icons/" + Internal.IMG_HORIZONTAL));
        reg.put(Internal.IMG_JOBKILL, Activator.getImageDescriptor("icons/" + Internal.IMG_JOBKILL));
        reg.put(Internal.IMG_JOBOUTPUT, Activator.getImageDescriptor("icons/" + Internal.IMG_JOBOUTPUT));
        reg.put(Internal.IMG_JOBPAUSERESUME, Activator.getImageDescriptor("icons/" +
            Internal.IMG_JOBPAUSERESUME));
        reg.put(Internal.IMG_JOBPRIORITY, Activator.getImageDescriptor("icons/" + Internal.IMG_JOBPRIORITY));
        reg.put(Internal.IMG_JOBSUBMIT, Activator.getImageDescriptor("icons/" + Internal.IMG_JOBSUBMIT));
        reg.put(Internal.IMG_MAXIMIZE, Activator.getImageDescriptor("icons/" + Internal.IMG_MAXIMIZE));
        reg.put(Internal.IMG_SCHEDULERFREEZE, Activator.getImageDescriptor("icons/" +
            Internal.IMG_SCHEDULERFREEZE));
        reg.put(Internal.IMG_SCHEDULERKILL, Activator.getImageDescriptor("icons/" +
            Internal.IMG_SCHEDULERKILL));
        reg.put(Internal.IMG_SCHEDULERPAUSE, Activator.getImageDescriptor("icons/" +
            Internal.IMG_SCHEDULERPAUSE));
        reg.put(Internal.IMG_SCHEDULERRESUME, Activator.getImageDescriptor("icons/" +
            Internal.IMG_SCHEDULERRESUME));
        reg.put(Internal.IMG_SCHEDULERSHUTDOWN, Activator.getImageDescriptor("icons/" +
            Internal.IMG_SCHEDULERSHUTDOWN));
        reg.put(Internal.IMG_SCHEDULERSTART, Activator.getImageDescriptor("icons/" +
            Internal.IMG_SCHEDULERSTART));
        reg.put(Internal.IMG_SCHEDULERSTOP, Activator.getImageDescriptor("icons/" +
            Internal.IMG_SCHEDULERSTOP));
        reg.put(Internal.IMG_VERTICAL, Activator.getImageDescriptor("icons/" + Internal.IMG_VERTICAL));
        reg.put(Internal.IMG_REMOTE_CONNECTION, Activator.getImageDescriptor("icons/" +
            Internal.IMG_REMOTE_CONNECTION));
        reg.put(Internal.IMG_EXIT, Activator.getImageDescriptor("icons/" + Internal.IMG_EXIT));
        reg.put(Internal.IMG_SERVER, Activator.getImageDescriptor("icons/" + Internal.IMG_SERVER));
        reg.put(Internal.IMG_SERVER_ADD, Activator.getImageDescriptor("icons/" + Internal.IMG_SERVER_ADD));
        reg.put(Internal.IMG_SERVER_REMOVE, Activator.getImageDescriptor("icons/" +
            Internal.IMG_SERVER_REMOVE));
        reg.put(Internal.IMG_DATA, Activator.getImageDescriptor("icons/" + Internal.IMG_DATA));
        reg.put(Internal.IMG_COPY, Activator.getImageDescriptor("icons/" + Internal.IMG_COPY));
        reg.put(Internal.IMG_SERVER_STARTED, Activator.getImageDescriptor("icons/" +
            Internal.IMG_SERVER_STARTED));
        reg.put(Internal.IMG_REFRESH, Activator.getImageDescriptor("icons/" + Internal.IMG_REFRESH));
        reg.put(Internal.IMG_SERVER_STOPPED, Activator.getImageDescriptor("icons/" +
            Internal.IMG_SERVER_STOPPED));
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
