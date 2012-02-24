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
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.util.SchedulerStarter;
import org.ow2.proactive.tests.performance.deployment.HostTestEnv;
import org.ow2.proactive.tests.performance.deployment.SchedulingFolder;
import org.ow2.proactive.tests.performance.deployment.TestDeployHelper;
import org.ow2.proactive.tests.performance.deployment.TestDeployer;
import org.ow2.proactive.tests.performance.deployment.TestEnv;
import org.ow2.proactive.tests.performance.deployment.TestExecutionException;
import org.ow2.proactive.tests.performance.utils.TestFileUtils;
import org.ow2.proactive.tests.performance.utils.TestUtils;


public class TestSchedulerDeployHelper extends TestDeployHelper {

    private final String schedulerHibernateConfig;

    private final String rmUrl;

    public TestSchedulerDeployHelper(HostTestEnv serverHostEnv, String protocol, String rmUrl)
            throws InterruptedException {
        super(serverHostEnv, protocol);
        this.rmUrl = rmUrl;
        schedulerHibernateConfig = createTestHibernateConfig();
    }

    @Override
    protected String getPamrServedReservedId() {
        return TestUtils.getRequiredProperty("scheduler.deploy.pamr.serverReservedId");
    }

    @Override
    public List<String> createServerStartCommand() {
        SchedulingFolder schedulingFolder = serverHostEnv.getEnv().getSchedulingFolder();

        List<String> result = new ArrayList<String>();
        result.add(serverHostEnv.getEnv().getJavaPath());

        List<String> javaOpts = protocolHelper.getAdditionalServerJavaOptions();
        if (javaOpts != null) {
            result.addAll(javaOpts);
        }

        String schedulerJavaOpts = System.getProperty("scheduler.deploy.javaOpts");
        if (schedulerJavaOpts != null && !schedulerJavaOpts.isEmpty()) {
            for (String opt : schedulerJavaOpts.split(" ")) {
                result.add(opt);
            }
        }

        result.add("-D" + TEST_JVM_OPTION);

        result.add("-Djava.security.manager");

        String dropDB = System.getProperty("scheduler.deploy.dropDB", "false");
        result.add(PASchedulerProperties.SCHEDULER_DB_HIBERNATE_DROPDB.getCmdLine() + dropDB);
        result.add(PASchedulerProperties.SCHEDULER_DB_HIBERNATE_CONFIG.getCmdLine() +
            schedulerHibernateConfig);

        File rootDir = schedulingFolder.getRootDir();
        result.add(CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getCmdLine() +
            TestDeployer.getFileName(rootDir, "/config/security.java.policy-server"));

        result.add(CentralPAPropertyRepository.PA_HOME.getCmdLine() + schedulingFolder.getRootDirPath());
        result.add(PASchedulerProperties.SCHEDULER_HOME.getCmdLine() + schedulingFolder.getRootDirPath());

        result.add(CentralPAPropertyRepository.PA_CONFIGURATION_FILE.getCmdLine() +
            TestDeployer.getFileName(schedulingFolder.getTestConfigDir(), "EmptyProActiveConfiguration.xml"));

        result.add(CentralPAPropertyRepository.LOG4J.getCmdLine() + "file:" +
            TestDeployer.getFileName(schedulingFolder.getTestConfigDir(), "/log4j/log4j-scheduler-server"));

        result.add(PASchedulerProperties.SCHEDULER_DEFAULT_FJT_LOG4J.getCmdLine() +
            TestDeployer.getFileName(schedulingFolder.getTestConfigDir(), "/log4j/log4j-forkedTask"));

        result.add("-cp");
        result.add(buildSchedulingClasspath());
        result.add(SchedulerStarter.class.getName());
        result.add("-u");
        result.add(rmUrl);

        return result;
    }

    private String createTestHibernateConfig() {
        try {
            TestEnv localEnv = TestEnv.getLocalEnvUsingSystemProperties();
            File localFile = new File(localEnv.getSchedulingFolder().getTestTmpDir(),
                "scheduler.hibernate.cfg.xml");

            String hibernateCfg = TestFileUtils.readStreamToString(new FileInputStream(new File(localEnv
                    .getSchedulingFolder().getTestConfigDir(), "scheduler/scheduler.hibernate.cfg.xml")));
            hibernateCfg = hibernateCfg.replace("@SCHEDULER_DB_DIR", serverHostEnv.getEnv()
                    .getSchedulingFolder().getTestTmpDir() +
                "/SCHEDULER_DB");
            TestFileUtils.writeStringToFile(localFile, hibernateCfg);

            return serverHostEnv.copyFileFromLocalEnv(localEnv, localFile);
        } catch (Exception e) {
            throw new TestExecutionException("Failed to prepare test configuration", e);
        }
    }

}
