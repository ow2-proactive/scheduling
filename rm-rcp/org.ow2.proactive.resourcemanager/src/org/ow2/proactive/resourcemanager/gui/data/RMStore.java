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
package org.ow2.proactive.resourcemanager.gui.data;

import java.security.KeyException;

import javax.security.auth.login.LoginException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.Activator;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.gui.actions.JMXActionsManager;
import org.ow2.proactive.resourcemanager.gui.data.model.RMModel;
import org.ow2.proactive.resourcemanager.gui.dialog.SelectResourceManagerDialog;
import org.ow2.proactive.resourcemanager.gui.views.ResourceExplorerView;
import org.ow2.proactive.resourcemanager.gui.views.ResourcesCompactView;
import org.ow2.proactive.resourcemanager.gui.views.ResourcesTabView;
import org.ow2.proactive.resourcemanager.gui.views.StatisticsView;


/**
 * @author The ProActive Team
 *
 */
public class RMStore {

    private static RMStore instance = null;
    private static boolean isConnected = false;

    /*    public static StatusLineContributionItem statusItem =
     new StatusLineContributionItem("LoggedInStatus");*/
    private ResourceManager resourceManager = null;

    private RMMonitoring rmMonitoring = null;
    private EventsReceiver receiver = null;
    private RMModel model = null;
    private String baseURL;

    /*    static {
     statusItem.setText("diconnected");
     }*/

    private RMStore(String url, String login, String password) throws RMException, SecurityException {
        try {
            baseURL = url;

            if (url != null && !url.endsWith("/"))
                url += "/";

            RMAuthentication auth = null;
            try {
                System.out.println("Joining resource manager on the following url " + url +
                    RMConstants.NAME_ACTIVE_OBJECT_RMAUTHENTICATION);
                auth = RMConnection.join(url + RMConstants.NAME_ACTIVE_OBJECT_RMAUTHENTICATION);
            } catch (RMException e) {
                throw new RMException("Resource manager does not exist on the following url: " + url, e);
            }

            Credentials creds = null;
            try {
                try {
                    creds = Credentials.createCredentials(login, password, auth.getPublicKey());
                } catch (KeyException e) {
                    throw new LoginException("Could not create encrypted credentials: " + e.getMessage());
                }
                resourceManager = auth.login(creds);
            } catch (LoginException e) {
                Activator.log(IStatus.INFO, "Login exception for user " + login, e);
                throw new RMException(e.getMessage());
            }

            rmMonitoring = resourceManager.getMonitoring();
            // checking if there were no exception
            rmMonitoring.toString();

            RMStore.instance = this;
            model = new RMModel();
            receiver = (EventsReceiver) PAActiveObject.newActive(EventsReceiver.class.getName(),
                    new Object[] { rmMonitoring });
            SelectResourceManagerDialog.saveInformations();
            isConnected = true;
            //ControllerView.getInstance().connectedEvent(isAdmin);

            // Initialize the JMX chartit action
            JMXActionsManager.getInstance().initJMXClient(auth, new Object[] { login, creds });

        } catch (ActiveObjectCreationException e) {
            Activator.log(IStatus.ERROR, "Exception when creating active object", e);
            e.printStackTrace();
            throw new RMException(e.getMessage(), e);
        } catch (NodeException e) {
            Activator.log(IStatus.ERROR, "Node exeption", e);
            e.printStackTrace();
            throw new RMException(e.getMessage(), e);
        }
    }

    public static void newInstance(String url, String login, String password) throws RMException,
            SecurityException {
        instance = new RMStore(url, login, password);
    }

    public static RMStore getInstance() {
        return instance;
    }

    public static boolean isConnected() {
        return isConnected;
    }

    public RMModel getModel() {
        return model;
    }

    /**
     * To get the rmAdmin
     * @return the rmAdmin
     * @throws RMException
     */
    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    /**
     * To get the rmMonitoring
     * @return the rmMonitoring
     */
    public RMMonitoring getRMMonitoring() {
        return rmMonitoring;
    }

    public String getURL() {
        return this.baseURL;
    }

    public void shutDownActions(final boolean failed) {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                String msg;
                if (failed) {
                    msg = "seems to be down";
                } else {
                    msg = "has been shutdown";
                }
                MessageDialog.openInformation(Display.getDefault().getActiveShell(), "shutdown",
                        "Resource manager  '" + RMStore.getInstance().getURL() + "'  " + msg +
                            ", now disconnect.");
                disconnectionActions();
            }
        });
    }

    /**
     * Clean displayed view ; detach them from model,
     * set status to disconnect. Terminates EventsReceiver AO
     *
     */
    public void disconnectionActions() {
        //clear Tab view if tab panel is displayed
        if (ResourcesTabView.getTabViewer() != null) {
            ResourcesTabView.getTabViewer().setInput(null);
        }
        //clear Tree view if tree panel is displayed
        if (ResourceExplorerView.getTreeViewer() != null) {
            ResourceExplorerView.getTreeViewer().setInput(null);
            //ResourceExplorerView.init();
        }
        if (ResourcesCompactView.getCompactViewer() != null) {
            ResourcesCompactView.getCompactViewer().clear();
        }
        //clear stats view if stats panel is displayed
        if (StatisticsView.getStatsViewer() != null) {
            StatisticsView.getStatsViewer().setInput(null);
        }
        // Disconnect JMX ChartIt action
        JMXActionsManager.getInstance().disconnectJMXClient();
        try {
            //disconnect user if user has not failed
            //protect it by a try catch
            resourceManager.disconnect();
        } catch (Exception e) {
        }
        resourceManager = null;
        rmMonitoring = null;
        model = null;
        baseURL = null;
        isConnected = false;
        PAActiveObject.terminateActiveObject(receiver, true);
        RMStatusBarItem.getInstance().setText("disconnected");
        //ControllerView.clearInstance();
    }
}
