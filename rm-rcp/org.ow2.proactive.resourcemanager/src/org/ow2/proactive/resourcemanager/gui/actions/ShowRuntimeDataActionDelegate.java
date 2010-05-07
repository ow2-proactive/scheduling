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
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.gui.actions;

import java.net.URL;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.actions.ActionDelegate;
import org.objectweb.proactive.ic2d.chartit.data.provider.IDataProvider;
import org.objectweb.proactive.ic2d.chartit.data.resource.IResourceDescriptor;
import org.objectweb.proactive.ic2d.chartit.editor.ChartItDataEditor;
import org.ow2.proactive.resourcemanager.Activator;


/**
 * This class represents an action delegate that corresponds to a chartable resource from
 * the Runtime Data MBean. 
 *  
 * @author The ProActive Team 
 */
public class ShowRuntimeDataActionDelegate extends ActionDelegate implements IWorkbenchWindowActionDelegate {

    public static final String NAME = "Runtime Data";

    /** The actions manager */
    private final JMXActionsManager manager;

    /** The name of the runtime data MBean */
    private final ObjectName mBeanName;

    /** The URL of the configuration file */
    private final URL configFileURL;

    public ShowRuntimeDataActionDelegate() throws Exception {
        this.manager = JMXActionsManager.getInstance();
        this.mBeanName = new ObjectName("ProActiveResourceManager:name=RuntimeData");
        this.configFileURL = FileLocator.find(Activator.getDefault().getBundle(), new Path(
            "config/RuntimeDataChartItConf.xml"), null); // TODO RENAME THE CONFIG FILE        
    }

    @Override
    public void init(final IAction action) {
        action.setEnabled(false);
        this.manager.addAction(action);
    }

    public void init(IWorkbenchWindow window) {
    }

    @Override
    public void run(IAction action) {
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

    /**
     * Internal definition of a ChartIt Resource Descriptor 
     */
    private final class RuntimeDataResourceDescriptor implements IResourceDescriptor {
        private final MBeanServerConnection connection;

        private RuntimeDataResourceDescriptor(final MBeanServerConnection connection) {
            this.connection = connection;
        }

        public IDataProvider[] getCustomDataProviders() {
            return new IDataProvider[] {};
        }

        public URL getConfigFileURL() {
            return ShowRuntimeDataActionDelegate.this.configFileURL;
        }

        public String getHostUrlServer() {
            return ShowRuntimeDataActionDelegate.this.manager.getJMXClientHelper().getConnector().toString();
        }

        public MBeanServerConnection getMBeanServerConnection() {
            return this.connection;
        }

        public String getName() {
            return ShowRuntimeDataActionDelegate.NAME;
        }

        public ObjectName getObjectName() {
            return ShowRuntimeDataActionDelegate.this.mBeanName;
        }
    }
}
