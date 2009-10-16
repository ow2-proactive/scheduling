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
package org.ow2.proactive.scheduler.gui.actions;

import java.net.URL;

import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.objectweb.proactive.ic2d.chartit.data.provider.IDataProvider;
import org.objectweb.proactive.ic2d.chartit.data.resource.IResourceDescriptor;
import org.objectweb.proactive.ic2d.chartit.editor.ChartItDataEditor;
import org.objectweb.proactive.ic2d.chartit.util.SimpleJMXClient;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.gui.Activator;


/**
 * This class represents an action that starts the ChartIt plugin feeded with statistical data 
 * that comes from JMX monitoring of the Scheduler. 
 * <b>
 * The operation of this action can be described as:
 * <ul>
 * <li>1. The Scheduler GUI asks the user to connect to an existing Scheduler
 * <li>2. The JMX client tries to connect to the Scheduler
 * <li>3. If connection is not established this action does nothing
 * <li>4. Before disconnecting the Scheduler GUI the jmx client must be disconnected
 * </ul>
 * <b>
 * By default this action is disabled. It is enabled if and only if the internal JMX client 
 * obtains a valid JMX connection. 
 *  
 * @author The ProActive Team 
 */
public class JMXChartItAction extends Action {
    /**
     * The name of the JMX ChartIt editor
     */
    private static final String JMX_CHARTIT_EDITOR_NAME = "JMX Monitoring";

    /** The static reference on the single instance of this class */
    private static JMXChartItAction instance;

    /** An instance of internal jmx client */
    private final SimpleJMXClient jmxClient;

    /** The URL of the predefined ChartIt config */
    private URL predefinedChartItConfigURL;

    /**
     * Returns the single instance of this class
     * @return The single instance of this class
     */
    public static JMXChartItAction getInstance() {
        if (JMXChartItAction.instance == null) {
            JMXChartItAction.instance = new JMXChartItAction();
        }
        return JMXChartItAction.instance;
    }

    /**
     * Internal private constructor, to access to the instance of this class please use
     * this static <code>getInstance()</code> method.
     */
    private JMXChartItAction() {
        // Set a descriptive icon
        super.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(
                org.objectweb.proactive.ic2d.chartit.Activator.getDefault().getBundle(), new Path(
                    "icons/areacharticon.gif"), null)));
        // This action is disabled by default
        super.setEnabled(false);
        super.setText(JMX_CHARTIT_EDITOR_NAME);
        super.setToolTipText("Show " + JMX_CHARTIT_EDITOR_NAME);
        this.jmxClient = new SimpleJMXClient();
    }

    /**
     * Initializes the JMX client and if the initialization was successful enables this action if
     * not sets as tool tip a message.
     * @param auth the Scheduler authentication interface
     * @param login the login used to connect to the Scheduler
     * @param password the password used for the connection
     * @param isAdmin used to distinguish the admin user from anonymous user
     */
    public void initJMXClient(final SchedulerAuthenticationInterface auth, final String login,
            final String password, final boolean isAdmin) {
        // Try to get the URL of the JMX connector
        String connectorURL;
        try {
            connectorURL = auth.getJMXConnectorURL();
        } catch (JMException e) {
            // By default the action is disabled just add the massage that comes from the exception as
            // a ToolTip of the action
            this.setToolTipText(e.getMessage());
            return;
        }
        String mBeanName = "SchedulerFrontend:name=SchedulerWrapperMBean";
        String path = "config/scheduler_chartit_conf";
        if (isAdmin) {
            connectorURL += "_admin";
            mBeanName += "_admin";
            path += "_admin";
        }
        this.predefinedChartItConfigURL = FileLocator.find(Activator.getDefault().getBundle(), new Path(path +
            ".xml"), null);
        // Try to create the connector to the given URL
        if (this.jmxClient.createConnector(connectorURL, mBeanName, new String[] { login, password })) {
            this.setEnabled(true);
        }
    }

    /**
     * Disconnects the JMX client and disables this action and closes the associated editor
     */
    public void disconnectJMXClient() {
        this.setEnabled(false);
        this.jmxClient.disconnect();
        try {
            activateIfFound(JMX_CHARTIT_EDITOR_NAME, false); // false for close
        } catch (Throwable t) {
            MessageDialog.openError(Display.getDefault().getActiveShell(), "Unable to close the " +
                JMXChartItAction.JMX_CHARTIT_EDITOR_NAME, t.getMessage());
            t.printStackTrace();
        }
    }

    @Override
    public void run() {
        // Try to connect the JMX client
        if (this.jmxClient.connect()) {
            try {
                if (!activateIfFound(JMX_CHARTIT_EDITOR_NAME, true)) { // true for activate
                    // First build a ResourceDescriptor
                    final IResourceDescriptor resourceDescriptor = new SchedulerChartItDescriptor();
                    // Open new editor based on the descriptor
                    ChartItDataEditor.openNewFromResourceDescriptor(resourceDescriptor);
                }
            } catch (Throwable t) {
                MessageDialog.openError(Display.getDefault().getActiveShell(), "Unable to open the " +
                    JMXChartItAction.JMX_CHARTIT_EDITOR_NAME, t.getMessage());
                t.printStackTrace();
            }
        }
    }

    /**
     * Activates or closes an editor by name.
     * 
     * @param activate <code>True</code> to activate,
     *         <code>False</code> to close
     * @param name
     *            The name of the editor to activate
     * @return <code>True</code> if the existing editor was activated,
     *         <code>False</code> otherwise
     * @throws PartInitException
     *             Thrown if the part can not be activated
     */
    private boolean activateIfFound(final String name, final boolean activate) throws PartInitException {
        final IWorkbenchWindow currentWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        // Navigate through EditorReference->EditorInput then find the
        // Editor through ActivePage.findEditor(editorInputRef)
        // First list all EditorReferences
        for (final IEditorReference ref : currentWindow.getActivePage().getEditorReferences()) {
            if (ref.getEditorInput().getName().equals(name)) {
                final IEditorPart editor = currentWindow.getActivePage().findEditor(ref.getEditorInput());
                if (activate) {
                    // If the Editor input was found activate it
                    currentWindow.getActivePage().activate(editor);
                } else {
                    currentWindow.getActivePage().closeEditor(editor, false); // close and don't save
                }
                return true;
            }
        }
        return false;
    }

    final class SchedulerChartItDescriptor implements IResourceDescriptor {

        public IDataProvider[] getCustomDataProviders() {
            return new IDataProvider[] {};
        }

        public String getHostUrlServer() {
            return JMXChartItAction.this.jmxClient.getServiceURL().toString();
        }

        public MBeanServerConnection getMBeanServerConnection() {
            return JMXChartItAction.this.jmxClient.getConnection();
        }

        public String getName() {
            return JMXChartItAction.JMX_CHARTIT_EDITOR_NAME;
        }

        public ObjectName getObjectName() {
            return JMXChartItAction.this.jmxClient.getName();
        }

        public URL getConfigFileURL() {
            return JMXChartItAction.this.predefinedChartItConfigURL;
        }
    }
}
