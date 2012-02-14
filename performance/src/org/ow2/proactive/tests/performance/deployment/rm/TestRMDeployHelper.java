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
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.utils.RMStarter;
import org.ow2.proactive.tests.performance.deployment.HostTestEnv;
import org.ow2.proactive.tests.performance.deployment.SchedulingFolder;
import org.ow2.proactive.tests.performance.deployment.TestDeployHelper;
import org.ow2.proactive.tests.performance.deployment.TestDeployer;
import org.ow2.proactive.tests.performance.deployment.TestEnv;
import org.ow2.proactive.tests.performance.deployment.TestExecutionException;
import org.ow2.proactive.tests.performance.utils.TestFileUtils;
import org.ow2.proactive.tests.performance.utils.TestUtils;


public class TestRMDeployHelper extends TestDeployHelper {

    private final String rmHibernateConfig;

    public TestRMDeployHelper(HostTestEnv serverHostEnv, String protocol) throws InterruptedException {
        super(serverHostEnv, protocol);
        rmHibernateConfig = createTestHibernateConfig();
    }

    @Override
    protected String getPamrServedReservedId() {
        return TestUtils.getRequiredProperty("rm.deploy.pamr.serverReservedId");
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

        String rmJavaOpts = System.getProperty("rm.deploy.javaOpts");
        if (rmJavaOpts != null && !rmJavaOpts.isEmpty()) {
            for (String opt : rmJavaOpts.split(" ")) {
                result.add(opt);
            }
        }

        result.add("-D" + TEST_JVM_OPTION);

        result.add("-Djava.security.manager");

        String dropDB = System.getProperty("rm.deploy.dropDB", "false");
        result.add(PAResourceManagerProperties.RM_DB_HIBERNATE_DROPDB.getCmdLine() + dropDB);
        result.add(PAResourceManagerProperties.RM_DB_HIBERNATE_CONFIG.getCmdLine() + rmHibernateConfig);

        File rootDir = schedulingFolder.getRootDir();
        result.add(CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getCmdLine() +
            TestDeployer.getFileName(rootDir, "/config/security.java.policy-server"));

        result.add(CentralPAPropertyRepository.PA_HOME.getCmdLine() + schedulingFolder.getRootDirPath());
        result.add(PAResourceManagerProperties.RM_HOME.getCmdLine() + schedulingFolder.getRootDirPath());

        result.add(CentralPAPropertyRepository.PA_CONFIGURATION_FILE.getCmdLine() +
            TestDeployer.getFileName(schedulingFolder.getTestConfigDir(), "EmptyProActiveConfiguration.xml"));

        result.add(CentralPAPropertyRepository.LOG4J.getCmdLine() + "file:" +
            TestDeployer.getFileName(schedulingFolder.getTestConfigDir(), "/log4j/log4j-rm-server"));

        result.add("-cp");
        result.add(buildSchedulingClasspath());
        result.add(RMStarter.class.getName());

        return result;
    }

    @Override
    public Map<String, String> getClientJavaProperties(TestEnv env) {
        Map<String, String> result = super.getClientJavaProperties(env);
        result.put(PAResourceManagerProperties.RM_HOME.getKey(), env.getSchedulingFolder().getRootDirPath());
        return result;
    }

    private String createTestHibernateConfig() {
        try {
            TestEnv localEnv = TestEnv.getLocalEnvUsingSystemProperties();
            File localFile = new File(localEnv.getSchedulingFolder().getTestTmpDir(), "rm.hibernate.cfg.xml");

            String hibernateCfg = TestFileUtils.readStreamToString(new FileInputStream(new File(localEnv
                    .getSchedulingFolder().getTestConfigDir(), "rm/rm.hibernate.cfg.xml")));
            hibernateCfg = hibernateCfg.replace("@RM_DB_DIR", serverHostEnv.getEnv().getSchedulingFolder()
                    .getTestTmpDir() +
                "/RM_DB");
            TestFileUtils.writeStringToFile(localFile, hibernateCfg);

            return serverHostEnv.copyFileFromLocalEnv(localEnv, localFile);
        } catch (Exception e) {
            throw new TestExecutionException("Failed to prepare test configuration", e);
        }
    }

}
