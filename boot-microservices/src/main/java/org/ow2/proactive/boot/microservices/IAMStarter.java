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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import org.apache.log4j.Logger;


public enum IAMStarter {

    //singleton instance of IAMStarter
    INSTANCE;

    private static final Logger LOGGER = Logger.getLogger(IAMStarter.class);

    private static final String SEPARATOR = File.separator;

    private static final String MICROSERVICE_NAME = "iam.war";

    private static final String READY_MARKER = "Ready to process requests";

    private static final int TIMEOUT = 180;

    private static final String[] JVM_ARGS = {};

    private static Process process;

    private static List<String> command = new ArrayList<>();

    private static boolean started = false;

    /**
     *  Start IAM microservice
     */
    public static Process start(String paHome, String microservicesPath)
            throws InterruptedException, IOException, ExecutionException {

        if (!started && buildJavaCommand(paHome) && buildMicroservicePath(paHome, microservicesPath)) {

            LOGGER.info("Starting IAM microservice");

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

            process = processBuilder.start();
            LOGGER.info(streamOutput(process.getInputStream()));

            started = true;
        }
        return process;
    }

    /**
     * Stream microservice output (using an executor) to check that it starts properly
     */
    private static String streamOutput(InputStream inputStream)
            throws IOException, InterruptedException, ExecutionException {

        // Stream microservice output
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(new Callable() {

            public String call() throws IOException {

                try {
                    String line = null;

                    while ((line = br.readLine()) != null) {
                        //System.out.print(".");
                        if (line.contains(READY_MARKER)) {
                            break;
                        }
                    }
                } finally {
                    br.close();
                }
                return "IAM Microservice started";
            }
        });

        // Check for timeout
        try {
            return future.get(TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException toe) {
            // Stop streaming the output, but the microservice continues to execute
            br.close();
            return "Warning: IAM Microservice timed out to start. See the logs for the details.";
        } finally {
            executor.shutdownNow();
            future.cancel(true);
        }
    }

    /**
     * Prepare java command with jvm args
     */
    private static boolean buildJavaCommand(String paHome) {

        String javaCmd = paHome + SEPARATOR + "jre" + SEPARATOR + "bin" + SEPARATOR + "java";

        if (new File(javaCmd).exists()) {
            command.add(javaCmd);
            command.add("-jar");
            command.addAll(Arrays.asList(JVM_ARGS));
            return true;
        } else {
            throw new RuntimeException("Java command not found when starting IAM microservice");
            //LOGGER.error("Java command not found when starting IAM microservice");
            //System.exit(1);
            //return false;
        }

    }

    /**
     * Check the microservice executable war
     */
    private static boolean buildMicroservicePath(String paHome, String microservicesPath) {

        String microserviceFile = paHome + SEPARATOR + microservicesPath + SEPARATOR + MICROSERVICE_NAME;

        if (new File(microserviceFile).exists()) {
            command.add(microserviceFile);
            return true;
        } else {
            throw new RuntimeException("IAM microservice is not deployed");

            //LOGGER.error("IAM microservice is not deployed");
            //System.exit(0);
            //return false;
        }
    }
}
