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
package functionaltests;

import java.io.File;
import java.io.Serializable;
import java.net.URI;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.GCMInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.scheduler.SchedulerFactory;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
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

    protected String rmUsername = "demo";
    protected String rmPassword = "demo";

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
        RMFactory.setOsJavaProperty();
        RMFactory.startLocal();

        // waiting the initialization
        RMAuthentication rmAuth = RMConnection.waitAndJoin(null);

        ResourceManagerProxy rmp = ResourceManagerProxy.getProxy(new URI("rmi://localhost:" +
            CentralPAPropertyRepository.PA_RMI_PORT.getValue() + "/"));

        SchedulerFactory.createScheduler(rmp, PASchedulerProperties.SCHEDULER_DEFAULT_POLICY
                .getValueAsString());

        schedulerAuth = SchedulerConnection.waitAndJoin(schedulerDefaultURL);
        if (GCMDPath != null && GCMDPath.length() > 0) {
            RMAdmin rmAdmin = rmAuth.logAsAdmin(Credentials.getCredentials(PAResourceManagerProperties
                    .getAbsolutePath(PAResourceManagerProperties.RM_CREDS.getValueAsString())));
            byte[] GCMDeploymentData = FileToBytesConverter.convertFileToByteArray(new File(GCMDPath));
            rmAdmin.createNodesource(NodeSource.GCM_LOCAL, GCMInfrastructure.class.getName(),
                    new Object[] { GCMDeploymentData }, StaticPolicy.class.getName(), null);
        }
        System.out.println("Scheduler successfully created !");
        return schedulerAuth;
    }
}
