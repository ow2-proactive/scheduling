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
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.util.SchedulerStarter;
import org.ow2.proactive.tests.performance.deployment.SchedulingFolder;
import org.ow2.proactive.tests.performance.deployment.TestDeployHelper;
import org.ow2.proactive.tests.performance.deployment.TestDeployer;
import org.ow2.proactive.tests.performance.deployment.TestExecutionException;
import org.ow2.proactive.tests.performance.utils.TestFileUtils;
import org.ow2.proactive.tests.performance.utils.TestUtils;


public class TestSchedulerDeployHelper extends TestDeployHelper {

    private final File schedulerHibernateConfig;

    private final String rmUrl;

    public TestSchedulerDeployHelper(String javaPath, SchedulingFolder schedulingFolder,
            InetAddress serverHost, String protocol, String rmUrl) throws InterruptedException {
        super(javaPath, schedulingFolder, serverHost, protocol);
        this.rmUrl = rmUrl;
        schedulerHibernateConfig = createTestHibernateConfig(schedulingFolder.getTestConfigDir(),
                schedulingFolder.getTestTmpDir());
    }

    @Override
    protected String getPamrServedReservedId() {
        return TestUtils.getRequiredProperty("scheduler.deploy.pamr.serverReservedId");
    }

    private File createTestHibernateConfig(File testConfigDir, File testTmpDir) {
        try {
            String hibernateCfg = TestFileUtils.readStreamToString(new FileInputStream(new File(
                testConfigDir, "scheduler/scheduler.hibernate.cfg.xml")));
            hibernateCfg = hibernateCfg.replace("@SCHEDULER_DB_DIR", testTmpDir.getAbsolutePath() +
                "/SCHEDULER_DB");

            File tmpConfigFile = new File(testTmpDir, "scheduler.hibernate.cfg.xml");
            TestFileUtils.writeStringToFile(tmpConfigFile, hibernateCfg);

            return tmpConfigFile;
        } catch (IOException e) {
            throw new TestExecutionException("Failed to prepare test configuration", e);
        }
    }

    @Override
    public List<String> createServerStartCommand() {
        List<String> result = new ArrayList<String>();
        result.add(javaPath);

        List<String> javaOpts = protocolHelper.getAdditionalServerJavaOptions();
        if (javaOpts != null) {
            result.addAll(javaOpts);
        }

        result.add("-D" + TEST_JVM_OPTION);

        result.add("-Djava.security.manager");

        result.add(PASchedulerProperties.SCHEDULER_DB_HIBERNATE_DROPDB.getCmdLine() + "true");
        result.add(PASchedulerProperties.SCHEDULER_DB_HIBERNATE_CONFIG.getCmdLine() +
            schedulerHibernateConfig.getAbsolutePath());

        File rootDir = schedulingFolder.getRootDir();
        result.add(CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getCmdLine() +
            TestDeployer.getFileName(rootDir, "/config/security.java.policy-server"));

        result.add(CentralPAPropertyRepository.PA_HOME.getCmdLine() + schedulingFolder.getRootDirPath());
        result.add(PASchedulerProperties.SCHEDULER_HOME.getCmdLine() + schedulingFolder.getRootDirPath());

        result.add(CentralPAPropertyRepository.PA_CONFIGURATION_FILE.getCmdLine() +
            TestDeployer.getFileName(schedulingFolder.getTestConfigDir(), "EmptyProActiveConfiguration.xml"));

        result.add(CentralPAPropertyRepository.LOG4J.getCmdLine() + "file:" +
            TestDeployer.getFileName(schedulingFolder.getTestConfigDir(), "/log4j/log4j-scheduler-server"));

        result.add("-cp");
        result.add(buildSchedulingClasspath());
        result.add(SchedulerStarter.class.getName());
        result.add("-u");
        result.add(rmUrl);

        return result;
    }

}
