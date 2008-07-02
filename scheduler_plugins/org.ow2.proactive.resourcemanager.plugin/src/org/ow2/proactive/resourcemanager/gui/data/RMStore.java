package org.ow2.proactive.resourcemanager.gui.data;

import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;


/**
 * @author The ProActive Team
 */
public class RMStore {

    private static RMStore instance = null;
    private RMAdmin rmAdmin = null;
    private RMMonitoring rmMonitoring = null;
    private String baseURL;

    private RMStore(String url) throws RMException {
        baseURL = url;
        if (!url.endsWith("/"))
            url += "/";
        rmAdmin = RMConnection.connectAsAdmin(url + RMConstants.NAME_ACTIVE_OBJECT_RMADMIN);
        rmMonitoring = RMConnection.connectAsMonitor(url + RMConstants.NAME_ACTIVE_OBJECT_RMMONITORING);
    }

    public static void newInstance(String url) throws RMException {
        instance = new RMStore(url);
    }

    public static RMStore getInstance() {
        return instance;
    }

    /**
     * To get the rmAdmin
     * 
     * @return the rmAdmin
     */
    public RMAdmin getRMAdmin() {
        return rmAdmin;
    }

    /**
     * To get the rmMonitoring
     * 
     * @return the rmMonitoring
     */
    public RMMonitoring getRMMonitoring() {
        return rmMonitoring;
    }

    public String getURL() {
        return this.baseURL;

    }

}
