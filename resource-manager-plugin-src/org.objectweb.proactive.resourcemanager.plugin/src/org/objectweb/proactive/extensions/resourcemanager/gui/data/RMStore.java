package org.objectweb.proactive.extensions.resourcemanager.gui.data;

import org.objectweb.proactive.extensions.resourcemanager.common.RMConstants;
import org.objectweb.proactive.extensions.resourcemanager.exception.RMException;
import org.objectweb.proactive.extensions.resourcemanager.frontend.RMAdmin;
import org.objectweb.proactive.extensions.resourcemanager.frontend.RMConnection;
import org.objectweb.proactive.extensions.resourcemanager.frontend.RMMonitoring;


/**
 * @author The ProActive Team
 */
public class RMStore {

    private static RMStore instance = null;
    private RMAdmin rmAdmin = null;
    private RMMonitoring rmMonitoring = null;

    private RMStore(String url) throws RMException {
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
}
