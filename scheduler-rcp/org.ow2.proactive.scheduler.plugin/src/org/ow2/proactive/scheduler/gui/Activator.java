/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.gui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingException;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingService;


/**
 * The activator class controls the plug-in life cycle
 *
 * @author The ProActive Team
 */
public class Activator extends AbstractUIPlugin {

    /*  The plug-in ID */
    public static final String PLUGIN_ID = "org.ow2.proactive.scheduler.plugin";

    /* The name of the property that sets the log service provider */
    public static final String LOGSERVICE_CLASS_PROPERTY = "pa.scheduler.logs.provider";

    // The shared instance
    private static Activator plugin;
    public static LogForwardingService lfs = null;

    /*
     * The constructor
     */
    public Activator() {
    }

    /*
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    /*
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        terminateLoggerServer();
        plugin = null;
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
     * Returns the id of this plug-in.
     * 
     * @return the id of this plug-in
     */
    public static String getPluginId() {
        return PLUGIN_ID;
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
    }
}
