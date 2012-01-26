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
package org.ow2.proactive.tests.performance.deployment.rm;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;


public class DeployRMForTest {

    public static void main(String[] args) throws Exception {
        try {
            String deployResultPropertiesFileName = getRequiredProperty("rm.deploy.deployResultPropertiesFile");
            File resultPropertiesFile = new File(deployResultPropertiesFileName);
            if (!resultPropertiesFile.exists()) {
                if (!resultPropertiesFile.createNewFile()) {
                    throw new IllegalArgumentException("Failed to create file " +
                        resultPropertiesFile.getAbsolutePath());
                }
            }
            if (!resultPropertiesFile.canWrite()) {
                throw new IllegalArgumentException("Can't write to the " +
                    resultPropertiesFile.getAbsolutePath());
            }

            String rmHostName = getRequiredProperty("rm.deploy.rmHost");
            String protocol = getRequiredProperty("rm.deploy.protocol");
            String javaPath = getRequiredProperty("rm.deploy.javaPath");
            String rmPath = getRequiredProperty("rm.deploy.rmHomePath");

            String[] rmNodesHosts = {};
            String rmNodesHostsString = getRequiredProperty("rm.deploy.rmNodesHosts");
            if (!rmNodesHostsString.trim().isEmpty()) {
                rmNodesHosts = rmNodesHostsString.split(",");
            }

            int nodesPerHost = Integer.valueOf(getRequiredProperty("rm.deploy.rmNodesPerHosts"));
            String[] testNodes = {};
            String testNodesString = getRequiredProperty("rm.deploy.testNodes");
            if (!testNodesString.trim().isEmpty()) {
                testNodes = testNodesString.split(",");
            }

            TestRMDeployer testRMDeployer = new TestRMDeployer(javaPath, rmPath, rmHostName, protocol, Arrays
                    .asList(rmNodesHosts), nodesPerHost, Arrays.asList(testNodes));

            Map<String, String> properties = testRMDeployer.startRM();
            if (properties != null) {
                System.out.println("Writing dynamic properties to the " +
                    resultPropertiesFile.getAbsolutePath());
                PrintWriter writer = new PrintWriter(resultPropertiesFile);
                try {
                    for (Map.Entry<String, String> property : properties.entrySet()) {
                        String string = String.format("%s=%s", property.getKey(), property.getValue());
                        System.out.println(string);
                        writer.println(string);
                    }
                } finally {
                    writer.close();
                }
                System.out.println("RM deployment finished successfully");
                System.exit(0);
            } else {
                System.out.println("RM deployment failed");
                System.exit(1);
            }
        } catch (Throwable t) {
            t.printStackTrace(System.out);
            System.out.println("RM deployment failed: " + t);
            System.exit(1);
        }
    }

    static String getRequiredProperty(String name) {
        String value = System.getProperty(name);
        if (value == null) {
            throw new IllegalArgumentException("Property '" + name + "' isn't set");
        }
        System.out.println(name + "=" + value);
        return value;
    }
}
