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
 * the My Account MBean. 
 *  
 * @author The ProActive Team 
 */
public final class ShowMyAccountActionDelegate extends ActionDelegate implements
        IWorkbenchWindowActionDelegate {

    public static final String NAME = "My Account";

    /** The actions manager */
    private final JMXActionsManager manager;

    /** The name of the runtime data MBean */
    private final ObjectName mBeanName;

    /** The URL of the configuration file */
    private final URL configFileURL;

    /**
     * Creates a new instance of this class.
     */
    public ShowMyAccountActionDelegate() throws Exception {
        this.manager = JMXActionsManager.getInstance();
        this.mBeanName = new ObjectName("ProActiveResourceManager:name=MyAccount");
        this.configFileURL = FileLocator.find(Activator.getDefault().getBundle(), new Path(
            "config/MyAccountChartItConf.xml"), null);
    }

    @Override
    public void init(final IAction action) {
        action.setEnabled(false);
        action.setId(NAME);
        this.manager.addAction(action);
    }

    public void init(IWorkbenchWindow window) {
    }

    @Override
    public void run(IAction action) {
        // If the jmx client is not connected do nothing
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
                        .openNewFromResourceDescriptor((IResourceDescriptor) new MyAccountResourceDescriptor(
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
    private final class MyAccountResourceDescriptor implements IResourceDescriptor {
        private final MBeanServerConnection connection;

        private MyAccountResourceDescriptor(final MBeanServerConnection connection) {
            this.connection = connection;
        }

        public IDataProvider[] getCustomDataProviders() {
            return new IDataProvider[] { new IDataProvider() {
                public Object provideValue() throws Throwable {
                    final long s = (Long) connection.getAttribute(mBeanName, "ProvidedNodeTime");
                    final long ss = s / 1000;
                    return String.format("%d h %02d m %02d s", ss / 3600, (ss % 3600) / 60, (ss % 60));
                }

                public String getType() {
                    return "String";
                }

                public String getName() {
                    return "ProvidedNodeTimeAsString";
                }

                public String getDescription() {
                    return "The provided node time";
                }
            }, new IDataProvider() {
                public Object provideValue() throws Throwable {
                    final long s = (Long) connection.getAttribute(mBeanName, "UsedNodeTime");
                    final long ss = s / 1000;
                    return String.format("%d h %02d m %02d s", ss / 3600, (ss % 3600) / 60, (ss % 60));
                }

                public String getType() {
                    return "String";
                }

                public String getName() {
                    return "UsedNodeTimeAsString";
                }

                public String getDescription() {
                    return "The used node time";
                }
            } };
        }

        public URL getConfigFileURL() {
            return ShowMyAccountActionDelegate.this.configFileURL;
        }

        public String getHostUrlServer() {
            return ShowMyAccountActionDelegate.this.manager.getJMXClientHelper().getConnector().toString();
        }

        public MBeanServerConnection getMBeanServerConnection() {
            return this.connection;
        }

        public String getName() {
            return ShowMyAccountActionDelegate.NAME;
        }

        public ObjectName getObjectName() {
            return ShowMyAccountActionDelegate.this.mBeanName;
        }
    }
}