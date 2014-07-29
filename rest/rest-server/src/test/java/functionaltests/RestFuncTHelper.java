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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $PROACTIVE_INITIAL_DEV$
 */
package functionaltests;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.objectweb.proactive.api.PARemoteObject;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.extensions.pnp.PNPConfig;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.util.SchedulerStarter;
import com.jayway.awaitility.Duration;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import functionaltests.utils.ProcessStreamReader;
import functionaltests.utils.RestFuncTUtils;

import static com.jayway.awaitility.Awaitility.await;


public class RestFuncTHelper {

    final static URL defaultJobXml = AbstractRestFuncTestCase.class.getResource("config/test-job.xml");

    final static URL serverJavaPolicy = RestFuncTHelper.class
            .getResource("config/server-java.security.policy");

    final static URL rmHibernateConfig = RestFuncTHelper.class.getResource("config/rmHibernateConfig.xml");

    final static URL schedHibernateConfig = RestFuncTHelper.class
            .getResource("config/schedHibernateConfig.xml");

    final static URL defaultPAConfigFile = RestFuncTHelper.class.getResource("config/defaultPAConfig.xml");

    final static URL allLog4JConfig = RestFuncTHelper.class.getResource("config/allLog4JConfig.properties");

    final static URL forkedTaskLog4JConfig = RestFuncTHelper.class
            .getResource("config/forkedTaskLog4JConfig.properties");

    final static int defaultNumberOfNodes = 1;

    private static String restServerUrl;
    private static String restfulSchedulerUrl;
    private static Process schedProcess;
    private static Scheduler scheduler;
    private static ResourceManager rm;
    private static PublicKey schedulerPublicKey;

    private RestFuncTHelper() {
    }

    public static void startRestfulSchedulerWebapp() throws Exception {
        // Kill all children processes on exit
        org.apache.log4j.BasicConfigurator.configure(new org.apache.log4j.varia.NullAppender());
        org.ow2.proactive.rm.util.process.EnvironmentCookieBasedChildProcessKiller.setCookie("killme");
        org.ow2.proactive.rm.util.process.EnvironmentCookieBasedChildProcessKiller
                .registerKillChildProcessesOnShutdown();

        List<String> cmd = new ArrayList<String>();
        String javaPath = RestFuncTUtils.getJavaPathFromSystemProperties();
        cmd.add(javaPath);
        cmd.add("-Djava.security.manager");
        cmd.add(CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getCmdLine() + toPath(serverJavaPolicy));

        cmd.add(CentralPAPropertyRepository.PA_HOME.getCmdLine() + getSchedHome());
        cmd.add(PASchedulerProperties.SCHEDULER_HOME.getCmdLine() + getSchedHome());
        cmd.add(PAResourceManagerProperties.RM_HOME.getCmdLine() + getRmHome());

        cmd.add(PAResourceManagerProperties.RM_DB_HIBERNATE_DROPDB.getCmdLine() +
            System.getProperty("rm.deploy.dropDB", "true"));
        cmd.add(PAResourceManagerProperties.RM_DB_HIBERNATE_CONFIG.getCmdLine() + toPath(rmHibernateConfig));

        cmd.add(PASchedulerProperties.SCHEDULER_DB_HIBERNATE_DROPDB.getCmdLine() +
            System.getProperty("scheduler.deploy.dropDB", "true"));
        cmd.add(PASchedulerProperties.SCHEDULER_DB_HIBERNATE_CONFIG.getCmdLine() +
            toPath(schedHibernateConfig));

        cmd.add(CentralPAPropertyRepository.PA_CONFIGURATION_FILE.getCmdLine() + toPath(defaultPAConfigFile));
        cmd.add(CentralPAPropertyRepository.LOG4J.getCmdLine() + "file:" + toPath(allLog4JConfig));
        cmd.add(PASchedulerProperties.SCHEDULER_DEFAULT_FJT_LOG4J.getCmdLine() + "file:" +
            toPath(forkedTaskLog4JConfig));

        cmd.add(CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getCmdLine() + "pnp");
        cmd.add(PNPConfig.PA_PNP_PORT.getCmdLine() + "1200");

        cmd.add("-cp");
        cmd.add(getClassPath());
        cmd.add(SchedulerStarter.class.getName());
        cmd.add("-ln");
        cmd.add("1");

        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        processBuilder.redirectErrorStream(true);
        schedProcess = processBuilder.start();

        ProcessStreamReader out = new ProcessStreamReader("scheduler-output: ",
            schedProcess.getInputStream(), System.out);
        out.start();

        // RM and scheduler are on the same url
        String port = "1200";
        String url = "pnp://localhost:" + port + "/";

        // Connect a scheduler client
        SchedulerAuthenticationInterface schedAuth = SchedulerConnection.waitAndJoin(url, TimeUnit.SECONDS
                .toMillis(60));
        schedulerPublicKey = schedAuth.getPublicKey();
        Credentials schedCred = RestFuncTUtils.createCredentials("admin", "admin", schedulerPublicKey);
        scheduler = schedAuth.login(schedCred);

        // Connect a rm client
        RMAuthentication rmAuth = RMConnection.waitAndJoin(url, TimeUnit.SECONDS.toMillis(60));
        Credentials rmCredentials = getRmCredentials();
        rm = rmAuth.login(rmCredentials);

        restServerUrl = "http://localhost:8080/rest/";
        restfulSchedulerUrl = restServerUrl + "scheduler";

        await().atMost(Duration.ONE_MINUTE).until(restIsStarted());
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

        // Destroy the scheduler process
        System.out.println("Shutting down scheduler process.");
        if (schedProcess != null) {
            try {
                RestFuncTUtils.destory(schedProcess);
            } catch (Throwable error) {
                System.err.println("An error occurred while shutting down scheduler process:");
                error.printStackTrace();
            } finally {
                try {
                    RestFuncTUtils.cleanupActiveObjectRegistry(SchedulerConstants.SCHEDULER_DEFAULT_NAME);
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
                    String resourceUrl = getResourceUrl("version");
                    HttpResponse response = new DefaultHttpClient().execute(new HttpGet(resourceUrl));
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

    public static PublicKey getSchedulerPublicKey() {
        return schedulerPublicKey;
    }

    public static String getString(InputStream is) {
        return new Scanner(is).useDelimiter("\\A").next();
    }

    public static File getDefaultJobXmlfile() throws Exception {
        return new File(defaultJobXml.toURI());
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

    private static Credentials getRmCredentials() throws Exception {
        File rmCredentails = new File(getRmHome(), "config/authentication/rm.cred");
        return Credentials.getCredentials(new FileInputStream(rmCredentails));
    }

    private static String getClassPath() throws Exception {
        StringBuilder classpath = new StringBuilder();
        classpath.append(getRmHome() + File.separator + "dist" + File.separator + "lib" + File.separator +
            "*");
        classpath.append(File.pathSeparatorChar);
        classpath.append(getRmHome() + File.separator + "addons" + File.separator + "*");
        classpath.append(File.pathSeparatorChar);
        classpath.append(System.getProperty("java.class.path"));
        return classpath.toString();
    }

    public static String getRestServerUrl() {
        return restServerUrl;
    }

    public static String getRestfulSchedulerUrl() {
        return restfulSchedulerUrl;
    }

    public static String getResourceUrl(String resource) {
        String restUrl = RestFuncTHelper.getRestfulSchedulerUrl();
        if (!restUrl.endsWith("/")) {
            restUrl = restUrl + "/";
        }
        return restUrl + resource;
    }
}