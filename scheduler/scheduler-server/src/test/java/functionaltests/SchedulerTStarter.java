/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package functionaltests;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

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


/**
 * Starts Scheduler and Resource Manager.
 * It is used to start scheduler in a separate JVM than the Test itself.
 *
 * @author The ProActive Team
 */
public class SchedulerTStarter implements Serializable {

    public static final int RM_NODE_NUMBER = 2;
    public static final String RM_NODE_NAME = "TEST";
    public static final int RM_NODE_DEPLOYMENT_TIMEOUT = 100000;

    protected static String schedulerUrl;

    /**
     * Start a Scheduler and Resource Manager. Must be called with following
     * arguments: 
     * <ul>
     * <li>first argument: true if the RM started with the scheduler has to start some nodes
     * <li>second argument: path to a scheduler Properties file
     * <li>third argument: path to a RM Properties file
     * </ul>
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            throw new IllegalArgumentException(
                "SchedulerTStarter must be started with 3 parameters: localhodes schedPropPath rmPropPath");
        }

        if (args.length == 4) {
            createWithExistingRM(args);
        } else {
            createRMAndScheduler(args);
        }
        System.out.println("Scheduler successfully created !");
    }

    private static void createRMAndScheduler(String[] args) throws Exception {
        final boolean localnodes = Boolean.valueOf(args[0]);
        String schedPropPath = args[1];
        String RMPropPath = args[2];

        PAResourceManagerProperties.updateProperties(RMPropPath);
        PASchedulerProperties.updateProperties(schedPropPath);

        //Starting a local RM
        RMFactory.setOsJavaProperty();

        final Thread rmStarter = new Thread() {
            public void run() {
                try {
                    RMFactory.startLocal();

                    // waiting the initialization
                    RMAuthentication rmAuth = RMConnection.waitAndJoin(schedulerUrl);

                    if (localnodes) {
                        Credentials creds = Credentials.getCredentials(PAResourceManagerProperties
                                .getAbsolutePath(PAResourceManagerProperties.RM_CREDS.getValueAsString()));
                        ResourceManager rmAdmin = rmAuth.login(creds);
                        Map<String, String> params = new HashMap<>();
                        if (System.getProperty("pas.launcher.forkas.method") != null) {
                            params.put("pas.launcher.forkas.method",
                                    System.getProperty("pas.launcher.forkas.method"));
                        }
                        if (System.getProperty("proactive.test.runAsMe") != null) {
                            params.put("proactive.test.runAsMe", "true");
                        }

                        rmAdmin.createNodeSource(
                                RM_NODE_NAME,
                                LocalInfrastructure.class.getName(),
                                new Object[] {
                                        creds.getBase64(),
                                        RM_NODE_NUMBER,
                                        RM_NODE_DEPLOYMENT_TIMEOUT,
                                        "-Dproactive.test=true " +
                                            CentralPAPropertyRepository.PA_RUNTIME_PING.getCmdLine() + false },

                                StaticPolicy.class.getName(), new Object[] { "ALL", "ALL" });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        rmStarter.start();

        schedulerUrl = "pnp://localhost:" + PNPConfig.PA_PNP_PORT.getValue() + "/";



        SchedulerFactory.createScheduler(new URI(schedulerUrl),
                PASchedulerProperties.SCHEDULER_DEFAULT_POLICY.getValueAsString());

        SchedulerConnection.waitAndJoin(schedulerUrl);

    }

    private static void createWithExistingRM(String[] args) throws Exception {
        String schedPropPath = args[1];
        String rmUrl = args[3];

        System.out.println("Creating with existing " + rmUrl);

        PASchedulerProperties.updateProperties(schedPropPath);

        SchedulerFactory.createScheduler(new URI(rmUrl), PASchedulerProperties.SCHEDULER_DEFAULT_POLICY
                .getValueAsString());

        SchedulerConnection.waitAndJoin(schedulerUrl);

    }
}
