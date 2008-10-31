package org.ow2.proactive.resourcemanager.gui.data;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;
import org.ow2.proactive.resourcemanager.gui.data.model.RMModel;
import org.ow2.proactive.resourcemanager.gui.dialog.SelectResourceManagerDialog;
import org.ow2.proactive.resourcemanager.gui.views.ResourceExplorerView;
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
    private RMAdmin rmAdmin = null;
    private RMMonitoring rmMonitoring = null;
    private EventsReceiver receiver = null;
    private RMModel model = null;
    private String baseURL;

    /*    static {
     statusItem.setText("diconnected");
     }*/

    private RMStore(String url) throws RMException {
        try {
            baseURL = url;
            if (!url.endsWith("/"))
                url += "/";
            rmAdmin = RMConnection.connectAsAdmin(url + RMConstants.NAME_ACTIVE_OBJECT_RMADMIN);
            rmMonitoring = RMConnection.connectAsMonitor(url + RMConstants.NAME_ACTIVE_OBJECT_RMMONITORING);
            RMStore.instance = this;
            model = new RMModel();
            receiver = (EventsReceiver) PAActiveObject.newActive(EventsReceiver.class.getName(),
                    new Object[] { rmMonitoring });
            SelectResourceManagerDialog.saveInformations();
            isConnected = true;
            RMStatusBarItem.getInstance().setText("connected");
        } catch (ActiveObjectCreationException e) {
            RMStatusBarItem.getInstance().setText("disconnected");
            e.printStackTrace();
            throw new RMException(e.getMessage(), e);
        } catch (NodeException e) {
            RMStatusBarItem.getInstance().setText("disconnected");
            e.printStackTrace();
            throw new RMException(e.getMessage(), e);
        }
    }

    public static void newInstance(String url) throws RMException {
        instance = new RMStore(url);
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
     */
    public RMAdmin getRMAdmin() {
        return rmAdmin;
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

    public void shutDownActions() {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                MessageDialog.openInformation(Display.getDefault().getActiveShell(), "shutdown",
                        "Resource manager " + RMStore.getInstance().getURL() +
                            " has been shutdown, now disconnect.");
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
        //clear stats view if stats panel is displayed
        if (StatisticsView.getStatsViewer() != null) {
            StatisticsView.getStatsViewer().setInput(null);
        }
        rmAdmin = null;
        rmMonitoring = null;
        model = null;
        baseURL = null;
        isConnected = false;
        PAActiveObject.terminateActiveObject(receiver, true);
        RMStatusBarItem.getInstance().setText("disconnected");
    }
}
