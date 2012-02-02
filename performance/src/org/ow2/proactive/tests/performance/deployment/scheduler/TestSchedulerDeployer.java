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
package org.ow2.proactive.tests.performance.deployment.scheduler;

import java.io.File;

import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.tests.performance.deployment.TestDeployHelper;
import org.ow2.proactive.tests.performance.deployment.TestDeployer;
import org.ow2.proactive.tests.performance.deployment.TestExecutionException;
import org.ow2.proactive.tests.performance.utils.TestUtils;


public class TestSchedulerDeployer extends TestDeployer {

    static final long SCHEDULER_START_TIMEOUT = 60000;

    static final String CLIENT_CONFIG_FILE_NAME = "SchedulerClientProActiveConfiguration.xml";

    public static TestSchedulerDeployer createSchedulerDeployerUsingSystemProperties(String rmUrl)
            throws Exception {
        String schedulerHostName = TestUtils.getRequiredProperty("scheduler.deploy.schedulerHost");
        if (rmUrl == null) {
            rmUrl = TestUtils.getRequiredProperty("scheduler.deploy.rmUrl");
        }
        String protocol = TestUtils.getRequiredProperty("test.deploy.protocol");
        String javaPath = TestUtils.getRequiredProperty("test.javaPath");
        String schedulingPath = TestUtils.getRequiredProperty("test.schedulingPath");

        TestSchedulerDeployer deployer = TestSchedulerDeployer.createSchedulerDeployer(javaPath,
                schedulingPath, schedulerHostName, protocol, rmUrl);

        return deployer;
    }

    public static TestSchedulerDeployer createSchedulerDeployer(String javaPath, String schedulingPath,
            String schedulerHostName, String protocol, String rmUrl) throws InterruptedException {
        TestSchedulerDeployer deployer = new TestSchedulerDeployer(javaPath, schedulingPath,
            schedulerHostName, protocol);
        TestDeployHelper deployHelper = new TestSchedulerDeployHelper(javaPath, deployer
                .getSchedulingFolder(), deployer.getServerHost(), protocol, rmUrl);
        deployer.setDeployHelper(deployHelper);
        return deployer;
    }

    private TestSchedulerDeployer(String javaPath, String schedulingPath, String schedulerHostName,
            String protocol) throws InterruptedException {
        super(javaPath, schedulingPath, CLIENT_CONFIG_FILE_NAME, schedulerHostName);
    }

    @Override
    protected void waitForServerStartup(String expectedUrl, String clientJavaOptions,
            File clientProActiveConfig) throws Exception {
        Credentials credentials = schedulingFolder.getSchedulingCredentials();

        System.out.println(String.format("Waiting for scheduler, url: %s, timeout: %d", expectedUrl,
                SCHEDULER_START_TIMEOUT));

        SchedulerAuthenticationInterface auth = SchedulerConnection.waitAndJoin(expectedUrl,
                SCHEDULER_START_TIMEOUT);
        Scheduler scheduler = auth.login(credentials);
        SchedulerStatus status = scheduler.getStatus();
        if (!status.equals(SchedulerStatus.STARTED)) {
            throw new TestExecutionException("Unexpected scheduler status: " + status);
        }
    }

}
