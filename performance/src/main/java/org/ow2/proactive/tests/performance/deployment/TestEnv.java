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
package org.ow2.proactive.tests.performance.deployment;

import java.io.File;
import java.net.InetAddress;
import java.util.List;

import org.ow2.proactive.tests.performance.utils.TestUtils;


public class TestEnv {

    private SchedulingFolder schedulingFolder;

    private String javaPath;

    public static TestEnv getEnvUsingSystemProperties(String targetName) {
        String envName = TestUtils.getRequiredProperty("test.deploy.env." + targetName);
        return getEnv(envName);
    }

    public static TestEnv getLocalEnvUsingSystemProperties() {
        return getEnv("local");
    }

    public static TestEnv getEnv(String envName) {
        String schedulingPath = TestUtils.getRequiredProperty("test.deploy.env." + envName +
            ".schedulingPath");
        String javaPath = TestUtils.getRequiredProperty("test.deploy.env." + envName + ".javaPath");
        return new TestEnv(schedulingPath, javaPath);
    }

    public TestEnv(String schedulingPath, String javaPath) {
        this.schedulingFolder = new SchedulingFolder(schedulingPath);
        this.javaPath = javaPath;
    }

    public String convertFileNameForEnv(File file, TestEnv env) {
        return convertFileNameForEnv(file.getAbsolutePath(), env);
    }

    public String convertFileNameForEnv(String fileName, TestEnv env) {
        return fileName.replace(this.schedulingFolder.getRootDirPath(), env.getSchedulingFolder()
                .getRootDirPath());
    }

    public SchedulingFolder getSchedulingFolder() {
        return schedulingFolder;
    }

    public String getJavaPath() {
        return javaPath;
    }

    public InetAddress validateEnv(String hostName) throws Exception {
        InetAddress host = DeploymentTestUtils.checkHostIsAvailable(hostName);
        DeploymentTestUtils.checkJavaIsAvailable(host, javaPath);
        DeploymentTestUtils.checkPathIsAvailable(host, schedulingFolder.getRootDirPath());

        List<String> javaProcesses = DeploymentTestUtils.listProcesses(host, "java");
        if (!javaProcesses.isEmpty()) {
            System.out.println("WARNING: there are java processes on the host " + host + ":");
            for (String process : javaProcesses) {
                System.out.println(process);
            }
        }
        return host;
    }

}
