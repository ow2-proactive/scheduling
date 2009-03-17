/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $PROACTIVE_INITIAL_DEV$
 */
package functionaltests;

import java.io.File;
import java.io.Serializable;
import java.net.URI;

import org.objectweb.proactive.core.config.PAProperties;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.core.AdminScheduler;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.resourcemanager.ResourceManagerProxy;
import org.ow2.proactive.utils.FileToBytesConverter;


/**
 * MyAO is an active object that will start a new scheduler.
 * It is used to start A scheduler in a separate JVM than the Test itself.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class MyAO implements Serializable {

    protected String rmUsername = "jl";
    protected String rmPassword = "jl";

    protected static String schedulerDefaultURL = "//Localhost/";

    /**
     * ProActive empty constructor
     */
    public MyAO() {
    }

    /**
     * Start a Scheduler and Resource Manager in Active's Object's JVM
     *
     * @param GCMDPath path to a GCMDeployment file, to deploy at RM's startup.
     * @param schedPropPath path to a scheduler Properties file, or null if not needed
     * @param RMPropPath  path to a RM Properties file, or null if not needed
     * @return SchedulerAuthenticationInteface of created Scheduler
     * @throws Exception if any error occurs.
     */
    public SchedulerAuthenticationInterface createAndJoinForkedScheduler(String GCMDPath,
            String schedPropPath, String RMPropPath) throws Exception {

        SchedulerAuthenticationInterface schedulerAuth = null;

        if (RMPropPath != null) {
            PAResourceManagerProperties.updateProperties(RMPropPath);
        }

        if (schedPropPath != null) {
            PASchedulerProperties.updateProperties(schedPropPath);
        }

        //Starting a local RM
        RMFactory.startLocal();
        RMAuthentication auth = RMConnection.waitAndJoin(null);
        RMAdmin admin = auth.logAsAdmin(rmUsername, rmPassword);

        RMFactory.setOsJavaProperty();
        byte[] GCMDeploymentData = FileToBytesConverter.convertFileToByteArray(new File(GCMDPath));
        admin.createGCMNodesource(GCMDeploymentData, "GCM_Node_Source");
        admin.disconnect();

        ResourceManagerProxy rmp = ResourceManagerProxy.getProxy(new URI("rmi://localhost:" +
            PAProperties.PA_RMI_PORT.getValue() + "/"));

        AdminScheduler
                .createScheduler(rmp, PASchedulerProperties.SCHEDULER_DEFAULT_POLICY.getValueAsString());

        schedulerAuth = SchedulerConnection.waitAndJoin(schedulerDefaultURL);
        System.out.println("Scheduler successfully created !");
        return schedulerAuth;
    }
}
