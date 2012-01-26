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
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.utils.RMStarter;
import org.ow2.proactive.tests.performance.deployment.SchedulingFolder;
import org.ow2.proactive.tests.performance.deployment.TestExecutionException;
import org.ow2.proactive.tests.performance.utils.TestFileUtils;


public abstract class TestRMDeployHelper {

    public static final String TEST_JVM_OPTION = "org.ow2.proactive.tests.performance=true";

    public static final String PAMR_ROUTER_HOST_PROPERTY = "rm.deploy.pamr.routerHost";

    protected final SchedulingFolder schedulingFolder;

    protected final InetAddress rmHost;

    protected final String javaPath;

    private final File hibernateConfigFile;

    public static TestRMDeployHelper createRMDeployHelper(String protocol, SchedulingFolder schedulingFolder,
            InetAddress rmHost, String javaPath) throws InterruptedException {
        if (protocol.equalsIgnoreCase("pnp")) {
            return new TestPnpRMDeployHelper(schedulingFolder, rmHost, javaPath);
        } else if (protocol.equalsIgnoreCase("pamr")) {
            String pamrHostName = System.getProperty(PAMR_ROUTER_HOST_PROPERTY);
            if (pamrHostName == null || pamrHostName.isEmpty()) {
                throw new TestExecutionException("Property '" + PAMR_ROUTER_HOST_PROPERTY +
                    "' required for PAMR protocol not specified");
            }
            Collection<String> hosts = Collections.singleton(pamrHostName);
            InetAddress pamrHost = TestRMDeployer.prepareHostsForTest(hosts, javaPath,
                    schedulingFolder.getRootDirPath()).get(0);
            return new TestPamrRMDeployHelper(schedulingFolder, rmHost, pamrHost, javaPath);
        } else {
            throw new IllegalArgumentException("Test doesn't support protocol " + protocol);
        }
    }

    public TestRMDeployHelper(SchedulingFolder schedulingFolder, InetAddress rmHost, String javaPath) {
        this.schedulingFolder = schedulingFolder;
        this.rmHost = rmHost;
        this.javaPath = javaPath;
        this.hibernateConfigFile = createTestHibernateConfig(schedulingFolder.getTestConfigDir(),
                schedulingFolder.getTestTmpDir());
    }

    private File createTestHibernateConfig(File testConfigDir, File testTmpDir) {
        try {
            String hibernateCfg = TestFileUtils.readStreamToString(new FileInputStream(new File(
                testConfigDir, "rm/rm.hibernate.cfg.xml")));
            hibernateCfg = hibernateCfg.replace("@RM_DB_DIR", testTmpDir.getAbsolutePath() + "/RM_DB");

            File tmpConfigFile = new File(testTmpDir, "rm.hibernate.cfg.xml");
            TestFileUtils.writeStringToFile(tmpConfigFile, hibernateCfg);

            return tmpConfigFile;
        } catch (IOException e) {
            throw new TestExecutionException("Failed to prepare test configuration", e);
        }
    }

    public abstract String prepareForDeployment() throws Exception;

    protected abstract List<String> getAdditionalRMJavaOptions();

    public final List<String> createRMStartCommand() {
        List<String> result = new ArrayList<String>();
        result.add(javaPath);

        List<String> javaOpts = getAdditionalRMJavaOptions();
        if (javaOpts != null) {
            result.addAll(javaOpts);
        }

        result.add("-D" + TEST_JVM_OPTION);

        result.add("-Djava.security.manager");

        result.add(PAResourceManagerProperties.RM_DB_HIBERNATE_DROPDB.getCmdLine() + "true");
        result.add(PAResourceManagerProperties.RM_DB_HIBERNATE_CONFIG.getCmdLine() +
            hibernateConfigFile.getAbsolutePath());

        File rootDir = schedulingFolder.getRootDir();
        result.add(CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getCmdLine() +
            getFileName(rootDir, "/config/security.java.policy-server"));

        result.add(CentralPAPropertyRepository.PA_HOME.getCmdLine() + schedulingFolder.getRootDirPath());
        result.add(PAResourceManagerProperties.RM_HOME.getCmdLine() + schedulingFolder.getRootDirPath());

        result.add(CentralPAPropertyRepository.PA_CONFIGURATION_FILE.getCmdLine() +
            getFileName(schedulingFolder.getTestConfigDir(), "EmptyProActiveConfiguration.xml"));

        result.add(CentralPAPropertyRepository.LOG4J.getCmdLine() + "file:" +
            getFileName(schedulingFolder.getTestConfigDir(), "/log4j/log4j-server"));

        result.add("-cp");
        result.add(buildClasspath());
        result.add(RMStarter.class.getName());

        return result;
    }

    public List<String> getClientJavaOptions() {
        List<String> result = new ArrayList<String>();
        result.add("-D" + PAResourceManagerProperties.RM_HOME.getKey() + "=" +
            schedulingFolder.getRootDirPath());
        result.add("-D" + TEST_JVM_OPTION);
        return result;
    }

    public Map<String, String> getClientProActiveProperties() {
        Map<String, String> properties = new LinkedHashMap<String, String>();

        properties.put(CentralPAPropertyRepository.PA_HOME.getName(), schedulingFolder.getRootDirPath());
        properties.put(CentralPAPropertyRepository.LOG4J.getName(), "file:" +
            getFileName(schedulingFolder.getTestConfigDir(), "/log4j/log4j-client"));

        return properties;
    }

    private String buildClasspath() {
        List<String> distLibJars = TestFileUtils.listDirectoryJars(new File(schedulingFolder.getRootDir(),
            "/dist/lib").getAbsolutePath());
        List<String> addonsJars = TestFileUtils.listDirectoryJars(new File(schedulingFolder.getRootDir(),
            "/addons").getAbsolutePath());

        List<String> allJars = new ArrayList<String>(distLibJars);
        allJars.addAll(addonsJars);
        StringBuilder result = new StringBuilder();
        for (String jar : allJars) {
            result.append(jar).append(File.pathSeparatorChar);
        }

        return result.toString();
    }

    protected String getFileName(File parent, String path) {
        File file = new File(parent, path);
        if (!file.exists()) {
            throw new TestExecutionException("Failed to find file: " + file.getAbsolutePath());
        }
        return file.getAbsolutePath();
    }

}
