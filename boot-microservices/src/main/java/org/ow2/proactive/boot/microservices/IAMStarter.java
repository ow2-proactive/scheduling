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
package org.ow2.proactive.boot.microservices;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.log4j.Logger;
import org.ow2.proactive.boot.microservices.util.IAMConfiguration;
import org.ow2.proactive.resourcemanager.utils.OperatingSystem;


public class IAMStarter {

    private static final Logger LOGGER = Logger.getLogger(IAMStarter.class);

    private static final String OS = System.getProperty("os.name");

    private static final String SEPARATOR = File.separator;

    private static Configuration config = new BaseConfiguration();

    private static Process process;

    private static List<String> command = new ArrayList<>();

    private static boolean started = false;

    private IAMStarter() {

    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (process != null) {
                process.destroyForcibly();
            }
        }));
    }

    /**
     * The method starts IAM microservice as separate java process,
     * given the executable war file and configuration directory of IAM.
     *
     * @param paHome ProActive environment home
     * @param iamMicroservicesPath path to the executable war folder of IAM (in ProActive environment)
     * @param iamConfigurationPath path to the configuration directory of IAM (in ProActive environment)
     * @return the started java process
     * @throws ConfigurationException If a problem occurs when loading IAM configuration
     * @throws ExecutionException if a problem occurs during the process execution
     * @throws IOException if a problem occurs when redirecting the process output
     * @throws InterruptedException if a problem occurs when getting the process result
     * @since version 8.3.0
     */
    public static Process start(String paHome, String iamMicroservicesPath, String iamConfigurationPath)
            throws InterruptedException, IOException, ExecutionException, ConfigurationException {

        if (!started) {
            LOGGER.info("Starting IAM microservice...");

            // load IAM configuration
            loadIAMConfiguration(iamConfigurationPath);

            // build java command to launch IAM
            buildJavaCommand(paHome);

            // add IAM war archive path
            buildMicroservicePath(iamMicroservicesPath);

            // add SpringBoot parameters for IAM to execute in ProActive environment
            buildSpringBootParams(iamConfigurationPath);

            LOGGER.debug("Starting IAM microservice using command: " + command.toString());

            // execute IAM as a separate JAva process
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
            process = processBuilder.start();

            LOGGER.debug(streamOutput(process.getInputStream()));

            started = true;
        }

        return process;
    }

    /**
     * Stream microservice output (using an executor) to check that it starts properly
     */
    private static String streamOutput(InputStream inputStream) throws ExecutionException, InterruptedException {

        // Stream microservice output
        ExecutorService executor = Executors.newSingleThreadExecutor();

        String readyMarker = config.getString(IAMConfiguration.READY_MARKER);
        String errorMarker = config.getString(IAMConfiguration.ERROR_MARKER);

        Future<String> future = executor.submit((Callable) () -> {

            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;

                while ((line = br.readLine()) != null) {
                    if (line.contains(readyMarker)) {
                        break;
                    } else if (line.contains(errorMarker)) {
                        throw new IAMStarterException("IAM started with errors. See IAM logs for the details.");
                    }
                }
            }
            return "IAM Microservice started at: " + config.getString(IAMConfiguration.IAM_URL);
        });

        // Check for timeout
        try {
            return future.get(config.getInt(IAMConfiguration.STARTUP_TIMEOUT), TimeUnit.SECONDS);
        } catch (TimeoutException toe) {
            return "IAM Microservice timed out to start. See IAM logs for the details.";
        } finally {
            executor.shutdownNow();
            future.cancel(true);
        }
    }

    /**
     * Prepare java command with jvm args
     */
    private static void buildJavaCommand(String paHome) {

        String javaCmd = null;

        if (OperatingSystem.getOperatingSystem(OS).equals(OperatingSystem.UNIX)) {
            javaCmd = "java";

        } else if (OperatingSystem.getOperatingSystem(OS).equals(OperatingSystem.WINDOWS)) {
            javaCmd = "java.exe";

        } else if (OperatingSystem.getOperatingSystem(OS).equals(OperatingSystem.MACOS)) {
            javaCmd = "java";
        }

        String javaPath = paHome + SEPARATOR + "jre" + SEPARATOR + "bin" + SEPARATOR + javaCmd;

        if (new File(javaPath).exists()) {
            command.add(javaPath);
        } else {
            command.add(javaCmd);
        }

        command.add("-Dpa.scheduler.home=" + paHome);

        command.add("-jar");

        command.addAll(config.getList(String.class, IAMConfiguration.JVM_ARGS));

    }

    /**
     * Check IAM microservice executable war
     */
    private static void buildMicroservicePath(String iamMicroservicesPath) {

        String microserviceFile = iamMicroservicesPath + SEPARATOR + config.getString(IAMConfiguration.ARCHIVE_NAME);

        if (new File(microserviceFile).exists()) {
            command.add(microserviceFile);

        } else {
            throw new IAMStarterException("IAM microservice is not deployed in: " + iamMicroservicesPath);
        }
    }

    /**
     * Add parameters needed by SpringBoot to start IAM in ProActive environment, instead oif using the default environment.
     */
    private static void buildSpringBootParams(String iamConfigurationPath) {

        command.add("--spring.profiles.active=" + IAMConfiguration.SPRING_PROACTIVE_ENV_PROFILE);

        command.add("--spring.config.location=" + iamConfigurationPath + SEPARATOR + IAMConfiguration.PROPERTIES_FILE);

    }

    /**
     * Load IAM configuration
     */
    private static void loadIAMConfiguration(String iamConfigurationPath) throws ConfigurationException {

        File configFile = new File(iamConfigurationPath + SEPARATOR + IAMConfiguration.PROPERTIES_FILE);

        if (!configFile.exists()) {
            throw new IAMStarterException("IAM configuration not found in : " + configFile.getAbsolutePath());
        }

        config = IAMConfiguration.loadConfig(configFile);

    }
}