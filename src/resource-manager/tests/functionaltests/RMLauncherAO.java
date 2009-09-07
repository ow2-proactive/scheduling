package functionaltests;

import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;


/**
 * Active object that launches Resource Manager on a forked JVM.
 *
 * @author ProActive team
 *
 */
public class RMLauncherAO {

    /**
     * ProActive empty constructor
     */
    public RMLauncherAO() {
    }

    /**
     * Start a Resource Manager in Active's Object's JVM
     * @param RMPropPath  path to a RM Properties file, or null if not needed
     * @return SchedulerAuthenticationInteface of created Scheduler
     * @throws Exception if any error occurs.
     */
    public RMAuthentication createAndJoinForkedRM(String RMPropPath) throws Exception {

        RMAuthentication auth = null;

        if (RMPropPath != null) {
            PAResourceManagerProperties.updateProperties(RMPropPath);
        }

        //Starting a local RM
        RMFactory.setOsJavaProperty();
        RMFactory.startLocal();

        // waiting the initialization
        auth = RMConnection.waitAndJoin(null);

        System.out.println("Resource Manager successfully created !");
        return auth;
    }
}
