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
package org.ow2.proactive.scheduler.gui;

import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingService;


/**
 * The activator class controls the plug-in life cycle
 * 
 * @author The ProActive Team
 */
public class Activator extends AbstractUIPlugin {

    /*  The plug-in ID */
    public static final String PLUGIN_ID = "Scheduler_Plugin";

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
        super.stop(context);
    }

    /**
     * Start a new logger server
     * @throws IOException if the logger server cannot be started
     */
    public static void startLoggerServer() throws IOException {
        // start the log server
        lfs = new LogForwardingService("org.ow2.proactive.scheduler.common.util.logforwarder.providers.ProActiveBasedForwardingProvider");
        lfs.initialize();
    }

    /**
     * Stop the current logger server.
     */
    public static void terminateLoggerServer() {
        if (lsf != null) {
            lsf.terminate();
        }
        lsf = null;
    }

    /*
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }


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

}
