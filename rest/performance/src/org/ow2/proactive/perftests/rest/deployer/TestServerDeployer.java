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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */

package org.ow2.proactive.perftests.rest.deployer;

import java.io.File;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.ow2.proactive.tests.performance.deployment.KillTestProcesses;
import org.ow2.proactive.tests.performance.deployment.TestDeployer;
import org.ow2.proactive.tests.performance.deployment.TestExecutionException;
import org.ow2.proactive.tests.performance.deployment.TestPamrProtocolHelper;
import org.ow2.proactive.tests.performance.deployment.TestPamrRouterDeployer;
import org.ow2.proactive.tests.performance.deployment.rm.TestRMDeployer;
import org.ow2.proactive.tests.performance.deployment.scheduler.TestSchedulerDeployer;
import org.ow2.proactive.tests.performance.utils.TestFileUtils;
import org.ow2.proactive.tests.performance.utils.TestUtils;

/**
 * TestServerDeployer is used to deploy PAMR Router (if needed), Resource
 * Manager, Scheduler and Application Server on target hosts with specified
 * configuration.
 * 
 */
public class TestServerDeployer {

    public static void main(String[] args) throws Exception {
        if (args.length != 5) {
            throw new TestExecutionException(
                    "Invalid parameters, expected parameters: startPamrIfNeeded startRM startScheduler startAppServer deployResultPropertiesFile");
        }

        boolean startPamrIfNeeded = Boolean.valueOf(args[0]);
        boolean startRM = Boolean.valueOf(args[1]);
        boolean startScheduler = Boolean.valueOf(args[2]);
        boolean startAppServer = Boolean.valueOf(args[3]);

        String deployResultPropertiesFileName = args[4];
        File resultPropertiesFile = TestFileUtils
                .getWritableFile(deployResultPropertiesFileName);

        String testsHostsNames = TestUtils.getRequiredProperty("testHosts");

        Map<String, String> deployResultProperties = new LinkedHashMap<String, String>();
        System.out
                .println(String
                        .format("RestSetupDeployer, startPamrIfNeeded: %s, startRM: %s, startScheduler: %s, startAppServer: %s",
                                startPamrIfNeeded, startRM, startScheduler,
                                startAppServer));

        String protocol = TestUtils.getRequiredProperty("test.deploy.protocol");
        if ((protocol.equalsIgnoreCase("pamr") || protocol
                .equalsIgnoreCase("multi")) && startPamrIfNeeded) {
            try {
                TestPamrRouterDeployer deployer = TestPamrRouterDeployer
                        .createPamrRouterDeployerUsingSystemProperties();
                deployer.startRouter();
                System.setProperty(
                        TestPamrProtocolHelper.PAMR_EXISTING_ROUTER_HOST,
                        deployer.getPamrHost().getHostName());
                System.setProperty(
                        TestPamrProtocolHelper.PAMR_EXISTING_ROUTER_PORT,
                        String.valueOf(deployer.getPamrPort()));
                System.out
                        .println("PAMR router deployment finished succesfully.");
            } catch (Throwable t) {
                t.printStackTrace(System.out);
                System.out.println("PAMR router deployment failed");
                cleanupAndExit(testsHostsNames);
            }
        }

        String rmUrl = null;
        if (startRM) {
            boolean deploymentResult;
            try {
                TestRMDeployer testRMDeployer = TestRMDeployer
                        .createRMDeployerUsingSystemProperties();
                Map<String, String> rmDeployProperties = deploy(testRMDeployer,
                        "rm");
                if (rmDeployProperties != null) {
                    deploymentResult = true;
                    rmUrl = testRMDeployer.getServerUrl();
                    deployResultProperties.putAll(rmDeployProperties);
                } else {
                    deploymentResult = false;
                }
            } catch (Throwable t) {
                deploymentResult = false;
                t.printStackTrace(System.out);
            }
            if (deploymentResult) {
                System.out.println("RM deployment finished successfully");
            } else {
                System.out.println("RM deployment failed");
                cleanupAndExit(testsHostsNames);
            }
        }

        String schedulerUrl = null;
        if (startScheduler) {
            boolean deploymentResult;
            try {
                TestSchedulerDeployer testSchedulerDeployer = TestSchedulerDeployer
                        .createSchedulerDeployerUsingSystemProperties(rmUrl);
                Map<String, String> schedulerDeployProperties = deploy(
                        testSchedulerDeployer, "scheduler");
                if (schedulerDeployProperties != null) {
                    deploymentResult = true;
                    schedulerUrl = testSchedulerDeployer.getServerUrl();
                    deployResultProperties.putAll(schedulerDeployProperties);
                } else {
                    deploymentResult = false;
                }
            } catch (Throwable t) {
                deploymentResult = false;
                t.printStackTrace(System.out);
            }
            if (deploymentResult) {
                System.out
                        .println("Scheduler deployment finished successfully");
            } else {
                System.out.println("Scheduler deployment failed");
                cleanupAndExit(testsHostsNames);
            }
        }

        if (startAppServer) {
            boolean deploymentResult;
            try {
                TestTomcatServerDeployer testAppServerDeployer = TestTomcatServerDeployer
                        .createAppServerDeployerUsingSystemProperties(
                                schedulerUrl, rmUrl);
                Map<String, String> appServerDeployProperties = deploy(
                        testAppServerDeployer, "appserver");
                if (appServerDeployProperties != null) {
                    deploymentResult = true;
                    appServerDeployProperties.put(
                            "appserver.deploy.result.serverUrl",
                            testAppServerDeployer.getServerUrl());
                    deployResultProperties.putAll(appServerDeployProperties);
                } else {
                    deploymentResult = false;
                }
            } catch (Throwable t) {
                deploymentResult = false;
                t.printStackTrace(System.out);
            }
            if (deploymentResult) {
                System.out
                        .println("AppServer deployment finished successfully");
            } else {
                System.out.println("AppServer deployment failed");
                cleanupAndExit(testsHostsNames);
            }
        }

        System.out.println("Writing deploy result properties to the "
                + resultPropertiesFile.getAbsolutePath());
        try {
            PrintWriter writer = new PrintWriter(resultPropertiesFile);
            try {
                for (Map.Entry<String, String> property : deployResultProperties
                        .entrySet()) {
                    String string = String.format("%s=%s", property.getKey(),
                            property.getValue());
                    System.out.println(string);
                    writer.println(string);
                }
            } finally {
                writer.close();
            }
        } finally {
            System.exit(0);
        }
    }

    private static Map<String, String> deploy(TestDeployer deployer,
            String propertiesPrefix) {
        try {
            Map<String, String> deployResultProperties = deployer.startServer();
            if (deployResultProperties != null) {
                Map<String, String> propertiesWithPrefix = new LinkedHashMap<String, String>();
                if (deployResultProperties != null) {
                    for (Map.Entry<String, String> property : deployResultProperties
                            .entrySet()) {
                        propertiesWithPrefix.put(propertiesPrefix + "."
                                + property.getKey(), property.getValue());
                    }
                }
                return propertiesWithPrefix;
            } else {
                return null;
            }
        } catch (Throwable t) {
            t.printStackTrace(System.out);
            return null;
        }
    }

    private static void cleanupAndExit(String hostsNamesString) {
        try {
            KillTestProcesses.killProcesses(hostsNamesString);
        } finally {
            System.exit(1);
        }
    }
}
