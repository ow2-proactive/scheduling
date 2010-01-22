/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
package org.ow2.proactive.resourcemanager.gui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * The activator class controls the plug-in life cycle
 * @author The ProActive Team
 */
public class Activator extends AbstractUIPlugin {

    /** The plug-in ID */
    public static final String PLUGIN_ID = "org.ow2.proactive.resourcemanager.gui";

    // The shared instance
    private static Activator plugin;

    /**
     * The constructor
     */
    public Activator() {
    }

    /**
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    /**
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        // Dispose the image registry
        final ImageRegistry reg = super.getImageRegistry();
        if (reg != null) {
            reg.dispose();
        }
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
     * plug-in relative path.
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
        reg.put(Internal.IMG_REMOVENODE, Activator.getImageDescriptor("icons/" + Internal.IMG_REMOVENODE));
        reg
                .put(Internal.IMG_REMOVESOURCE, Activator.getImageDescriptor("icons/" +
                    Internal.IMG_REMOVESOURCE));
        reg.put(Internal.IMG_RMSHUTDOWN, Activator.getImageDescriptor("icons/" + Internal.IMG_RMSHUTDOWN));
        reg.put(Internal.IMG_SOURCE, Activator.getImageDescriptor("icons/" + Internal.IMG_SOURCE));
        reg.put(Internal.IMG_TORELEASE, Activator.getImageDescriptor("icons/" + Internal.IMG_TORELEASE));
    }
}