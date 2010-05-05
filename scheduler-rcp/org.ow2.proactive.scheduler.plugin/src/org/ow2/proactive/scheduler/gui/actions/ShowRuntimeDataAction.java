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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.gui.actions;

import java.net.URL;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.chartit.data.provider.IDataProvider;
import org.objectweb.proactive.ic2d.chartit.data.resource.IResourceDescriptor;
import org.objectweb.proactive.ic2d.chartit.editor.ChartItDataEditor;
import org.ow2.proactive.scheduler.gui.Activator;


/**
 * This class represents an action that corresponds to a chartable resource from
 * the Runtime Data MBean. 
 *  
 * @author The ProActive Team 
 */
public final class ShowRuntimeDataAction extends Action {

    public static final String NAME = "Runtime Data";

    /** The actions manager */
    private final JMXActionsManager manager;

    /** The name of the runtime data MBean */
    private final ObjectName mBeanName;

    /** The URL of the configuration file */
    private final URL configFileURL;

    /**
     * Creates a new instance of this class.
     */
    ShowRuntimeDataAction(final JMXActionsManager manager) throws Exception {
        // Set a descriptive icon
        super.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(
                org.objectweb.proactive.ic2d.chartit.Activator.getDefault().getBundle(), new Path(
                    "icons/areacharticon.gif"), null)));
        // This action is disabled by default
        super.setEnabled(false);
        super.setToolTipText("Show " + NAME);

        this.manager = manager;
        this.mBeanName = new ObjectName("ProActiveScheduler:name=RuntimeData");
        this.configFileURL = FileLocator.find(Activator.getDefault().getBundle(), new Path(
            "config/RuntimeDataChartItConf.xml"), null);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        // Each time the action is disabled close the corresponding chart editor
        if (!enabled) {
            try {
                // activateIfFound method with false to close the editor
                JMXActionsManager.activateIfFound(NAME, false);
            } catch (Exception t) {
                MessageDialog.openError(Display.getDefault().getActiveShell(), "Unable to close the " + NAME,
                        t.getMessage());
                t.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        // Try to connect the JMX client    	
        if (!this.manager.getJMXClientHelper().isConnected()) {
            return;
        }
        try {
            // true for activate
            if (!JMXActionsManager.activateIfFound(NAME, true)) {
                // Acquire the connection
                final MBeanServerConnection con = this.manager.getJMXClientHelper().getConnector()
                        .getMBeanServerConnection();
                // Open new editor based on the descriptor
                ChartItDataEditor
                        .openNewFromResourceDescriptor((IResourceDescriptor) new RuntimeDataResourceDescriptor(
                            con));
            }
        } catch (Exception e) {
            MessageDialog.openError(Display.getDefault().getActiveShell(), "Unable to open the " + NAME, e
                    .getMessage());
            e.printStackTrace();
        }
    }

    //////////////////

    private final class RuntimeDataResourceDescriptor implements IResourceDescriptor {
        private final MBeanServerConnection connection;

        private RuntimeDataResourceDescriptor(final MBeanServerConnection connection) {
            this.connection = connection;
        }

        public IDataProvider[] getCustomDataProviders() {
            return new IDataProvider[] {};
        }

        public URL getConfigFileURL() {
            return ShowRuntimeDataAction.this.configFileURL;
        }

        public String getHostUrlServer() {
            return ShowRuntimeDataAction.this.manager.getJMXClientHelper().getConnector().toString();
        }

        public MBeanServerConnection getMBeanServerConnection() {
            return this.connection;
        }

        public String getName() {
            return ShowRuntimeDataAction.NAME;
        }

        public ObjectName getObjectName() {
            return ShowRuntimeDataAction.this.mBeanName;
        }
    }
}