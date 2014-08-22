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
import java.io.FileInputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ow2.proactive.perftests.rest.utils.FindFreePortUtility;
import org.ow2.proactive.perftests.rest.utils.ZipUtility;
import org.ow2.proactive.process.ProcessExecutor;
import org.ow2.proactive.tests.performance.deployment.DeploymentTestUtils;
import org.ow2.proactive.tests.performance.deployment.HostTestEnv;
import org.ow2.proactive.tests.performance.deployment.TestDeployer;
import org.ow2.proactive.tests.performance.deployment.TestEnv;
import org.ow2.proactive.tests.performance.deployment.TestExecutionException;
import org.ow2.proactive.tests.performance.utils.TestFileUtils;
import org.ow2.proactive.tests.performance.utils.TestUtils;

import static java.io.File.separator;


/**
 * TestTomcatServerDeployer is used to deploy Tomcat Server on the target host
 * with the specified configuration.
 */
public class TestTomcatServerDeployer extends TestDeployer {

    private static final String CLIENT_CONFIG_FILE = "TomcatClientProActiveConfiguration.xml";

    private static final String TOMCAT_CONFIG_DIR = "tomcat";
    private static final String PORTAL_CONFIG_DIR = "scheduling-rest";

    private static final String PORTAL_PROP_TEMPLATE_FILE = PORTAL_CONFIG_DIR + separator +
        "portal.properties.template";

    private static final String TOMCAT_CONFIG_TEMPLATE_FILE = TOMCAT_CONFIG_DIR + separator +
        "server.xml.template";

    private static final String PA_CONFIG_FILE = "ProActiveConfiguration.xml";

    private static final String WEBAPP_DIR = "webapp";

    private static final String PORTAL_DIR = WEBAPP_DIR + separator + "SchedulingRest";

    private static final String PORTAL_PA_CONFIG_FILE = PORTAL_DIR + separator + "WEB-INF" + separator +
        PA_CONFIG_FILE;

    private static final String PORTAL_PROP_FILE = PORTAL_DIR + separator + "WEB-INF" + separator +
        "portal.properties";

    private static final String TOMCAT_CONFIG_FILE = "server.xml";

    private String schedulerUrl;
    private String rmUrl;
    private int httpPort;

    public static TestTomcatServerDeployer createAppServerDeployerUsingSystemProperties(String schedulerUrl,
            String rmUrl) throws Exception {
        String appServerHostName = TestUtils.getRequiredProperty("appserver.deploy.hostname");
        HostTestEnv serverHostEnv = new HostTestEnv(appServerHostName, TestEnv
                .getEnvUsingSystemProperties("appserver"));
        String protocol = TestUtils.getRequiredProperty("test.deploy.protocol");
        TestTomcatServerDeployer deployer = new TestTomcatServerDeployer(serverHostEnv, CLIENT_CONFIG_FILE,
            protocol, schedulerUrl, rmUrl);
        return deployer;
    }

    public TestTomcatServerDeployer(HostTestEnv serverHostEnv, String clientConfigFileName, String protocol,
            String schedulerUrl, String rmUrl) throws InterruptedException {
        super(serverHostEnv, clientConfigFileName, protocol);
        this.schedulerUrl = schedulerUrl;
        this.rmUrl = rmUrl;
    }

    public String getServerUrl() {
        return String.format("http://%s:%d/SchedulingRest/rest", serverHostEnv.getHost().getHostName(),
                httpPort);
    }

    @Override
    protected void waitForServerStartup(String expectedUrl) throws Exception {
        try {
            Thread.sleep(30 * 1000);
        } catch (InterruptedException ie) {
            // ignore
        }
    }

    @Override
    protected String getPamrServedReservedId() {
        return TestUtils.getRequiredProperty("appserver.deploy.pamr.serverReservedId");
    }

    @Override
    public List<String> createServerStartCommand() {
        Config localConfig = new Config(getLocalSchedulingRestPath());
        Config hostConfig = new Config(getServerHostEnv().getHost(), getServerHostSchedulingRestPath());

        createHostWebappDirectory(hostConfig);
        createTestServerConfig(localConfig, hostConfig);
        createPortalConfig(schedulerUrl, rmUrl, localConfig, hostConfig);
        createPAConfigFile(localConfig, hostConfig);

        File testConfigDir = hostConfig.getTestConfigDir();
        File testTmpDir = hostConfig.getTestTmpDir();

        File tomcatDir = new File(getServerHostTomcatDir());

        List<String> startup = new ArrayList<String>();

        startup.add(serverHostEnv.getEnv().getJavaPath());
        startup.add(asJvmParam("java.util.logging.config.file", absolutePath(tomcatDir,
                "conf/logging.properties")));
        startup.add(asJvmParam("java.util.logging.manager", "org.apache.juli.ClassLoaderLogManager"));
        String catalinaOpts = getCatalinaOpts();
        if (catalinaOpts != null) {
            startup.add(catalinaOpts);
        }
        startup.add(asJvmParam("java.endorsed.dirs", absolutePath(tomcatDir, "endorsed")));
        startup.add("-classpath");
        startup.add(absolutePath(tomcatDir, "bin/bootstrap.jar"));
        startup.add(asJvmParam("java.security.manager"));
        startup.add(asJvmParam("java.security.policy", absolutePath(testConfigDir, "grant.all.java.policy")));
        startup.add(asJvmParam("catalina.base", absolutePath(tomcatDir)));
        startup.add(asJvmParam("catalina.home", absolutePath(tomcatDir)));
        startup.add(asJvmParam("java.io.tmpdir", absolutePath(testTmpDir, "temp")));
        startup.add(asJvmParam(TestDeployer.TEST_JVM_OPTION_NAME, "true"));
        startup.add("org.apache.catalina.startup.Bootstrap");
        startup.add("-config");
        startup.add(absolutePath(testTmpDir, "server.xml"));
        startup.add("start");
        startup.add(">>");
        startup.add(absolutePath(testTmpDir, "PERFORMANCE_tomcat_server.log"));
        startup.add("2>&1");

        return startup;

    }

    private void createPortalConfig(String schedulerUrl, String rmUrl, Config localConfig, Config hostConfig) {
        File testTmpDir = localConfig.getTestTmpDir();
        File testConfigDir = localConfig.getTestConfigDir();
        try {
            File localFile = new File(testTmpDir, PORTAL_PROP_FILE);
            String portalProperties = TestFileUtils.readStreamToString(new FileInputStream(new File(
                testConfigDir, PORTAL_PROP_TEMPLATE_FILE)));
            portalProperties = portalProperties.replace("@SCHEDULER_URL@", schedulerUrl).replace("@RM_URL@",
                    rmUrl);
            TestFileUtils.writeStringToFile(localFile, portalProperties);
            copyFileFromLocalToHost(localFile, localConfig, hostConfig);

        } catch (Exception e) {
            throw new TestExecutionException("An error occurred while creating portal.properties file.", e);
        }
    }

    private void createPAConfigFile(Config localConfig, Config hostConfig) {
        try {
            File testTmpDir = localConfig.getTestTmpDir();
            File localFile = new File(testTmpDir, PORTAL_PA_CONFIG_FILE);
            Map<String, String> clientProperties = getClientJavaProperties(getServerHostEnv().getEnv());
            String config = DeploymentTestUtils.createProActiveConfiguration(clientProperties);

            TestFileUtils.writeStringToFile(localFile, config);
            copyFileFromLocalToHost(localFile, localConfig, hostConfig);

            File clientPAConfigFile = new File(localConfig.getTestTmpDir(), PA_CONFIG_FILE);
            TestFileUtils.writeStringToFile(clientPAConfigFile, config);
        } catch (Exception e) {
            throw new TestExecutionException("An error occurred while creating ProActiveConfiguration file.",
                e);
        }

    }

    private void createTestServerConfig(Config localConfig, Config hostConfig) {
        try {
            File localWebappDir = new File(localConfig.getTestTmpDir(), "webapp");
            String hostWebappDir = convertFileNameFromLocalToHost(localWebappDir, localConfig, hostConfig);

            Integer[] freePorts = FindFreePortUtility.findFreePorts(getServerHostEnv().getHost(),
                    getServerHostEnv().getEnv().getJavaPath(), hostConfig.getTestClassesDir()
                            .getAbsolutePath(), 4);

            httpPort = freePorts[0].intValue();

            File localFile = new File(localConfig.getTestTmpDir(), TOMCAT_CONFIG_FILE);
            String serverConfigTemplate = TestFileUtils.readStreamToString(new FileInputStream(new File(
                localConfig.getTestConfigDir(), TOMCAT_CONFIG_TEMPLATE_FILE)));
            String serverConfig = serverConfigTemplate.replace("@HTTP_PORT@", Integer.toString(httpPort))
                    .replace("@REDIRECT_PORT@", freePorts[1].toString()).replace("@AJP_PORT@",
                            freePorts[2].toString()).replace("@SHUTDOWN_PORT@", freePorts[3].toString())
                    .replace("@APP_BASE@", hostWebappDir);

            TestFileUtils.writeStringToFile(localFile, serverConfig);
            copyFileFromLocalToHost(localFile, localConfig, hostConfig);

        } catch (Exception e) {
            throw new TestExecutionException("An error occurred while creating server.xml file.", e);
        }
    }

    private String convertFileNameFromLocalToHost(File localFile, Config localConfig, Config hostConfig) {
        String localFileName = localFile.getAbsolutePath();
        String targetFileName = localFileName.replace(localConfig.getRootDir().getAbsolutePath(), hostConfig
                .getRootDir().getAbsolutePath());
        return targetFileName;
    }

    private void copyFileFromLocalToHost(File localFile, Config localConfig, Config hostConfig)
            throws InterruptedException {
        String hostFileName = convertFileNameFromLocalToHost(localFile, localConfig, hostConfig);
        createFileInHost(localFile.getAbsolutePath(), hostFileName, hostConfig.getHost());
    }

    private void createFileInHost(String localFileName, String hostFileName, InetAddress host)
            throws InterruptedException {
        List<String> command = new ArrayList<String>();
        command.add("rsync");
        command.add(localFileName);
        command.add(host.getHostName() + ":" + hostFileName);
        ProcessExecutor rsync = new ProcessExecutor("rsync", command, false, true);
        if (!rsync.executeAndWaitCompletion(10000, true)) {
            throw new TestExecutionException("Failed to copy file '" + localFileName + "' to the " + host +
                ":" + hostFileName);
        }

    }

    private void createHostWebappDirectory(Config hostConfig) {
        try {
            File testTmpDir = hostConfig.getTestTmpDir();
            String testPortalDir = (new File(testTmpDir, PORTAL_DIR)).getAbsolutePath();
            String zipFile = getServerHostWarFile();
            ZipUtility.unzipFile(zipFile, testPortalDir, serverHostEnv);
        } catch (Exception e) {
            throw new TestExecutionException("Failed to create webapp directory", e);
        }
    }

    private String getCatalinaOpts() {
        String catalinaOpts = System.getProperty("test.deploy.env." + serverHostEnvName() + ".catalina_opts");
        return catalinaOpts;
    }

    private String getLocalSchedulingRestPath() {
        return TestUtils.getRequiredProperty("test.deploy.env." + "local" + ".schedulingRestPath");
    }

    private String getServerHostSchedulingRestPath() {
        return TestUtils
                .getRequiredProperty("test.deploy.env." + serverHostEnvName() + ".schedulingRestPath");
    }

    private String getServerHostTomcatDir() {
        return TestUtils.getRequiredProperty("test.deploy.env." + serverHostEnvName() + ".tomcathome");
    }

    private String getServerHostWarFile() {
        return TestUtils.getRequiredProperty("test.deploy.env." + serverHostEnvName() + ".warfile");
    }

    private String serverHostEnvName() {
        return TestUtils.getRequiredProperty("test.deploy.env.appserver");
    }

    private String absolutePath(File dir) {
        return dir.getAbsolutePath();
    }

    private String absolutePath(File parentDir, String relativePath) {
        return (new File(parentDir, relativePath)).getAbsolutePath();
    }

    private String asJvmParam(String option) {
        return (new StringBuilder()).append("-D").append(option).toString();
    }

    private String asJvmParam(String option, String value) {
        return (new StringBuilder()).append("-D").append(option).append('=').append(value).toString();
    }

    private class Config {

        private InetAddress host;
        private File rootDir;
        private File testTmpDir;
        private File testConfigDir;
        private File testPerformanceDir;
        private File testClassesDir;

        public Config(String rootDirPath) {
            this(null, rootDirPath);
        }

        public Config(InetAddress host, String rootDirPath) {
            this.host = host;
            rootDir = new File(rootDirPath);
            testPerformanceDir = new File(rootDir, "performance");
            testClassesDir = new File(rootDir, "classes" + File.separator + "performance");
            testTmpDir = new File(testPerformanceDir, "tmp");
            testConfigDir = new File(testPerformanceDir, "config");
        }

        public File getRootDir() {
            return rootDir;
        }

        public File getTestTmpDir() {
            return testTmpDir;
        }

        public File getTestConfigDir() {
            return testConfigDir;
        }

        public File getTestClassesDir() {
            return testClassesDir;
        }

        public InetAddress getHost() {
            return host;
        }
    }
}
