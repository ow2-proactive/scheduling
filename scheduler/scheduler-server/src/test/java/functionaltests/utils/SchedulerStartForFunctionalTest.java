/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionaltests.utils;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.extensions.pnp.PNPConfig;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.LocalInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.scheduler.SchedulerFactory;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.task.utils.ForkerUtils;
import org.ow2.proactive.scheduler.util.SchedulerHsqldbStarter;

import java.io.Serializable;
import java.net.URI;


/**
 * Starts Scheduler and Resource Manager.
 * It is used to start scheduler in a separate JVM than the Test itself.
 *
 * @author The ProActive Team
 */
public class SchedulerStartForFunctionalTest implements Serializable {

    public static final int RM_NODE_DEPLOYMENT_TIMEOUT = 100000;

    public static final int RM_NODE_NUMBER = 2;

    public static final String RM_NODE_NAME = "TEST";

    protected static String schedulerUrl;

    /**
     * Start a Scheduler and Resource Manager. Must be called with following
     * arguments: 
     * <ul>
     * <li>first argument: true if the RM started with the scheduler has to start some nodes
     * <li>second argument: path to a scheduler Properties file
     * <li>third argument: path to a RM Properties file
     * <li>fourth argument (optional): URL to RM
     * </ul>
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            throw new IllegalArgumentException(
                "Invalid number of parameters, exactly 3 parameters are expected: localNodes schedPropPath rmPropPath");
        }

        if (args.length == 4) {
            createWithExistingRm(args[1], args[3]);
        } else {
            createRMAndScheduler(args);
        }

        System.out.println("Scheduler successfully created!");
    }

    private static void createRMAndScheduler(String[] args) throws Exception {
        final boolean deployLocalNodes = Boolean.valueOf(args[0]);
        String schedPropPath = args[1];
        String rmPropPath = args[2];

        PAResourceManagerProperties.updateProperties(rmPropPath);
        PASchedulerProperties.updateProperties(schedPropPath);

        RMFactory.setOsJavaProperty();

        new SchedulerHsqldbStarter().startIfNeeded();

        new Thread() {
            public void run() {
                try {
                    RMFactory.startLocal();

                    // waiting the initialization
                    RMAuthentication rmAuth = RMConnection.waitAndJoin(schedulerUrl);

                    if (deployLocalNodes) {
                        Credentials credentials =
                                Credentials.getCredentials(PAResourceManagerProperties
                                    .getAbsolutePath(PAResourceManagerProperties.RM_CREDS.getValueAsString()));

                        ResourceManager rmAdmin = rmAuth.login(credentials);
                        rmAdmin.createNodeSource(
                                RM_NODE_NAME,
                                LocalInfrastructure.class.getName(),
                                new Object[] {
                                        credentials.getBase64(),
                                        RM_NODE_NUMBER,
                                        RM_NODE_DEPLOYMENT_TIMEOUT,
                                        getJavaPropertiesLine() },
                                StaticPolicy.class.getName(),
                                new Object[] { "ALL", "ALL" });
                        rmAdmin.disconnect();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

        schedulerUrl = "pnp://localhost:" + PNPConfig.PA_PNP_PORT.getValue() + "/";

        SchedulerFactory.createScheduler(new URI(schedulerUrl),
                PASchedulerProperties.SCHEDULER_DEFAULT_POLICY.getValueAsString());

        SchedulerConnection.waitAndJoin(schedulerUrl);
    }

    private static String getJavaPropertiesLine() {
        StringBuilder line = new StringBuilder();
        line.append("-Dproactive.test=true");
        line.append(" ");
        line.append(CentralPAPropertyRepository.PA_RUNTIME_PING.getCmdLine() + "false");

        String forkMethodKeyValue = System.getProperty(ForkerUtils.FORK_METHOD_KEY);
        if (forkMethodKeyValue != null) {
            line.append(" ");
            line.append("-D" + ForkerUtils.FORK_METHOD_KEY + "=" + forkMethodKeyValue);
        }

        if (System.getProperty("proactive.test.runAsMe") != null) {
            line.append(" ");
            line.append("-Dproactive.test.runAsMe=true");
        }

        return line.toString();
    }

    private static void createWithExistingRm(String schedulerPropertiesPath, String rmUrl) throws Exception {
        System.out.println("Creating with existing " + rmUrl);

        PASchedulerProperties.updateProperties(schedulerPropertiesPath);

        SchedulerFactory.createScheduler(new URI(rmUrl), PASchedulerProperties.SCHEDULER_DEFAULT_POLICY
                .getValueAsString());

        SchedulerConnection.waitAndJoin(schedulerUrl);
    }

}
