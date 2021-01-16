/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package functionaltests;

import static com.jayway.awaitility.Awaitility.await;
import static functionaltests.utils.RestFuncTUtils.cleanupActiveObjectRegistry;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.rmi.AlreadyBoundException;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PARemoteObject;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.extensions.pnp.PNPConfig;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.http.HttpClientBuilder;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.synchronization.AOSynchronization;
import org.ow2.proactive.scheduler.util.SchedulerStarter;
import org.ow2.proactive.utils.CookieBasedProcessTreeKiller;
import org.ow2.proactive.web.WebProperties;

import com.jayway.awaitility.Duration;

import functionaltests.utils.ProcessStreamReader;
import functionaltests.utils.RestFuncTUtils;


public class RestFuncTHelper {

    final static URL defaultJobXml = AbstractRestFuncTestCase.class.getResource("config/test-job.xml");

    final static URL serverJavaPolicy = RestFuncTHelper.class.getResource("config/server-java.security.policy");

    final static URL rmHibernateConfig = RestFuncTHelper.class.getResource("config/rmHibernateConfig.xml");

    final static URL schedHibernateConfig = RestFuncTHelper.class.getResource("config/schedHibernateConfig.xml");

    public final static int DEFAULT_NUMBER_OF_NODES = 1;

    public static final String RM_CRED_RELATIVE_PATH = "config" + File.separator + "authentication" + File.separator +
                                                       "rm.cred";

    private static String restServerUrl;

    private static String restfulSchedulerUrl;

    private static String restfulRmUrl;

    private static Process schedProcess;

    private static Scheduler scheduler;

    private static ResourceManager rm;

    private static PublicKey schedulerPublicKey;

    private static final String SYNCHRONIZATION_NODE_NAME = "synchronization-node-0";

    private static final String SYNCHRONIZATION_V_NODE_NAME = "synchronization-v-node-0";

    private static AOSynchronization synchronization;

    private RestFuncTHelper() {
    }

    public static void startRestfulSchedulerWebapp(int nbNodes) throws Exception {
        // Kill all children processes on exit
        org.apache.log4j.BasicConfigurator.configure(new org.apache.log4j.varia.NullAppender());
        CookieBasedProcessTreeKiller.registerKillChildProcessesOnShutdown("rest_tests");

        List<String> cmd = new ArrayList<>();
        String javaPath = RestFuncTUtils.getJavaPathFromSystemProperties();
        cmd.add(javaPath);
        cmd.add("-Djava.security.manager");
        cmd.add("-Dresteasy.allowGzip=true");
        cmd.add(CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getCmdLine() + toPath(serverJavaPolicy));

        cmd.add(CentralPAPropertyRepository.PA_HOME.getCmdLine() + getSchedHome());
        cmd.add(PASchedulerProperties.SCHEDULER_HOME.getCmdLine() + getSchedHome());
        cmd.add(PAResourceManagerProperties.RM_HOME.getCmdLine() + getRmHome());

        cmd.add(PAResourceManagerProperties.RM_DB_HIBERNATE_DROPDB.getCmdLine() +
                System.getProperty("rm.deploy.dropDB", "true"));
        cmd.add(PAResourceManagerProperties.RM_DB_HIBERNATE_CONFIG.getCmdLine() + toPath(rmHibernateConfig));

        cmd.add(PASchedulerProperties.SCHEDULER_DB_HIBERNATE_DROPDB.getCmdLine() +
                System.getProperty("scheduler.deploy.dropDB", "true"));
        cmd.add(PASchedulerProperties.SCHEDULER_DB_HIBERNATE_CONFIG.getCmdLine() + toPath(schedHibernateConfig));

        cmd.add(WebProperties.WEB_HTTPS.getCmdLine() + "true");

        cmd.add(CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getCmdLine() + "pnp");
        cmd.add(PNPConfig.PA_PNP_PORT.getCmdLine() + "1200");

        cmd.add("-cp");
        cmd.add(getClassPath());
        cmd.add(SchedulerStarter.class.getName());
        cmd.add("-ln");
        cmd.add("" + nbNodes);

        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        processBuilder.redirectErrorStream(true);
        schedProcess = processBuilder.start();

        ProcessStreamReader out = new ProcessStreamReader("scheduler-output: ", schedProcess.getInputStream());
        out.start();

        // RM and scheduler are on the same url
        String port = "1200";
        String url = "pnp://localhost:" + port + "/";

        // Connect a scheduler client
        SchedulerAuthenticationInterface schedAuth = SchedulerConnection.waitAndJoin(url,
                                                                                     TimeUnit.SECONDS.toMillis(120));
        schedulerPublicKey = schedAuth.getPublicKey();
        Credentials schedCred = RestFuncTUtils.createCredentials("admin", "admin", schedulerPublicKey);
        scheduler = schedAuth.login(schedCred);

        // Connect a rm client
        RMAuthentication rmAuth = RMConnection.waitAndJoin(url, TimeUnit.SECONDS.toMillis(120));
        Credentials rmCredentials = getRmCredentials();
        rm = rmAuth.login(rmCredentials);

        restServerUrl = "https://localhost:8443/rest/";
        restfulSchedulerUrl = restServerUrl + "scheduler";
        restfulRmUrl = restServerUrl + "rm";

        startSynchronizationAPI();

        await().atMost(new Duration(900, TimeUnit.SECONDS)).until(restIsStarted());
    }

    public static void stopRestfulSchedulerWebapp() {
        // Kill all rm nodes
        try {
            Set<String> urls = rm.listAliveNodeUrls();
            for (String nodeUrl : urls) {
                try {
                    ProActiveRuntime runtime = (ProActiveRuntime) PARemoteObject.lookup(URI.create(nodeUrl));
                    runtime.killRT(false);
                } catch (Throwable noNeed) {
                }
            }
            rm.shutdown(true);
        } catch (Throwable noNeed) {
        }

        System.out.println("Shutting down the scheduler.");
        try {
            scheduler.shutdown();
        } catch (Throwable ignore) {
        }

        // Destroy the scheduler process
        System.out.println("Shutting down scheduler process.");
        if (schedProcess != null) {
            try {
                RestFuncTUtils.destroy(schedProcess);
            } catch (Throwable error) {
                System.err.println("An error occurred while shutting down scheduler process:");
                error.printStackTrace();
            } finally {
                try {
                    // clean synchronization api
                    cleanSynchronizationAPI();
                    cleanupActiveObjectRegistry(SchedulerConstants.SCHEDULER_DEFAULT_NAME);
                } catch (Throwable error) {

                }
            }
        }
    }

    private static Callable<Boolean> restIsStarted() {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    String resourceUrl = getSchedulerResourceUrl("version");
                    HttpClient client = new HttpClientBuilder().insecure(true).useSystemProperties().build();
                    HttpResponse response = client.execute(new HttpGet(resourceUrl));
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == HttpStatus.SC_OK) {
                        return true;
                    }
                } catch (IOException e) {
                }
                return false;
            }
        };
    }

    public static Scheduler getScheduler() {
        return scheduler;
    }

    public static ResourceManager getResourceManager() {
        return rm;
    }

    public static PublicKey getSchedulerPublicKey() {
        return schedulerPublicKey;
    }

    public static String getString(InputStream is) {
        return new Scanner(is).useDelimiter("\\A").next();
    }

    public static File getDefaultJobXmlfile() throws Exception {
        return new File(defaultJobXml.toURI());
    }

    public static String getRmCredentialsPath() throws Exception {
        return getRmHome() + File.separator + RM_CRED_RELATIVE_PATH;
    }

    private static String toPath(URL url) throws Exception {
        return (new File(url.toURI())).getCanonicalPath();
    }

    private static String getRmHome() throws Exception {
        return RestFuncTestConfig.getInstance().getProperty(RestFuncTestConfig.RESTAPI_TEST_RM_HOME);
    }

    private static String getSchedHome() throws Exception {
        return RestFuncTestConfig.getInstance().getProperty(RestFuncTestConfig.RESTAPI_TEST_SCHEDULER_HOME);
    }

    public static Credentials getRmCredentials() throws Exception {
        File rmCredentails = new File(getRmHome(), RM_CRED_RELATIVE_PATH);
        return Credentials.getCredentials(new FileInputStream(rmCredentails));
    }

    private static String getClassPath() throws Exception {
        return (getRmHome() + File.separator + "dist" + File.separator + "lib" + File.separator + "*") +
               File.pathSeparatorChar + getRmHome() + File.separator + "addons" + File.separator + "*" +
               filterClassPath(System.getProperty("java.class.path"));
    }

    private static String filterClassPath(String classPath) throws Exception {
        Set<String> distLibJars = findJarsNamesInPath(getRmHome() + File.separator + "dist" + File.separator + "lib");
        Set<String> addonsJars = findJarsNamesInPath(getRmHome() + File.separator + "addons");
        List<String> pathList = Arrays.asList(classPath.split("" + File.pathSeparatorChar));
        StringBuilder builder = new StringBuilder();
        for (String pathElement : pathList) {
            if (pathElement.endsWith(".so") || pathElement.endsWith(".dll") || pathElement.endsWith(".lib") ||
                pathElement.endsWith(".dylib")) {
                continue;
            } else if (distLibJars.contains(new File(pathElement).getName())) {
                continue;
            } else if (addonsJars.contains(new File(pathElement).getName())) {
                continue;
            } else {
                builder.append(File.pathSeparatorChar);
                builder.append(pathElement);
            }
        }
        return builder.toString();
    }

    private static Set<String> findJarsNamesInPath(String path) {
        File[] jarArray = new File(path).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });
        HashSet<String> jarNames = new HashSet<>();
        for (File jar : jarArray) {
            jarNames.add(jar.getName());
        }
        return jarNames;
    }

    public static String getRestServerUrl() {
        return restServerUrl;
    }

    public static String getRestfulSchedulerUrl() {
        return restfulSchedulerUrl;
    }

    public static String getRestfulRmUrl() {
        return restfulRmUrl;
    }

    public static String getSchedulerResourceUrl(String resource) {
        return getResourceUrl(RestFuncTHelper.getRestfulSchedulerUrl(), resource);
    }

    public static String getRmResourceUrl(String resource) {
        return getResourceUrl(RestFuncTHelper.getRestfulRmUrl(), resource);
    }

    public static String getResourceUrl(String baseRestUrl, String resource) {
        if (!baseRestUrl.endsWith("/")) {
            baseRestUrl = baseRestUrl + "/";
        }
        return baseRestUrl + resource;
    }

    private static void startSynchronizationAPI() {
        try {
            System.out.println("Starting Synchronization API");
            Node localNode = ProActiveRuntimeImpl.getProActiveRuntime().createLocalNode(SYNCHRONIZATION_NODE_NAME,
                                                                                        true,
                                                                                        SYNCHRONIZATION_V_NODE_NAME);
            AOSynchronization synchronization = PAActiveObject.newActive(AOSynchronization.class,
                                                                         new Object[] { PASchedulerProperties.getAbsolutePath(PASchedulerProperties.SCHEDULER_SYNCHRONIZATION_DATABASE.getValueAsString()) },
                                                                         localNode);
            PAActiveObject.registerByName(synchronization, SchedulerConstants.SYNCHRONIZATION_DEFAULT_NAME);

        } catch (Exception e) {
            System.err.println("Could not start synchronization service.");
        }
    }

    private static void cleanSynchronizationAPI() {
        try {
            PAActiveObject.unregister(PAActiveObject.getActiveObjectNodeUrl(synchronization));
            PAActiveObject.terminateActiveObject(synchronization, true);
            ProActiveRuntimeImpl.getProActiveRuntime().killNode(SYNCHRONIZATION_NODE_NAME);
        } catch (IOException e) {
            System.err.println("Could not properly clean synchronization service.");
        }
    }
}
