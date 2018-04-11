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

    private static final Logger logger = Logger.getLogger(IAMStarter.class);

    private static final String separator = File.separator;

    private static final String os = System.getProperty("os.name");

    private static final String microservice_name = "iam.war";

    private static final String ready_marker = "Ready to process requests";

    private static final int timeout = 180;

    private static Process process;

    private static List<String> command = new ArrayList<String>();

    private static boolean started = false;

    /**
     *  Start IAM microservice
     */
    public Process start(String pa_home, String microservices_path, String[] jvmArgs)
            throws InterruptedException, IOException, ExecutionException {

        if (!started  && buildJavaCommand(pa_home,jvmArgs) && buildMicroservicePath(pa_home,microservices_path)) {

            System.out.println("Starting IAM microservice");

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

            process = processBuilder.start();
            System.out.println(streamOutput(process.getInputStream()));

            started = true;
        }
        return process;
    }

    /**
     * Stream microservice output (using an executor) to check that it starts properly
     */
    private String streamOutput(InputStream inputStream) throws IOException, InterruptedException, ExecutionException {

        // Stream microservice output
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(new Callable() {

            public String call() throws IOException {

                try {
                    String line = null;

                    while ((line = br.readLine()) != null) {
                        System.out.print(".");
                        if (line.contains(ready_marker)) {
                            break;
                        }
                    }
                } finally {
                    br.close();
                }
                return "\nIAM Microservice started";
            }
        });

        // Check for timeout
        try {
            return future.get(timeout, TimeUnit.SECONDS);
        } catch (TimeoutException toe) {
            // Stop streaming the output, but the microservice continues to execute
            br.close();
            return "\nWarning: IAM Microservice timed out to start. See the logs for the details.";
        } finally {
            executor.shutdownNow();
            future.cancel(true);
        }
    }

    /**
     * Prepare java command with jvm args
     */
    private boolean buildJavaCommand(String pa_home, String[] jvmArgs) {

        String javaCmd = pa_home + separator + "jre" + separator + "bin" + separator + "java";

        if (new File(javaCmd).exists()) {
            command.add(javaCmd);
            command.add("-jar");
            command.addAll(Arrays.asList(jvmArgs));
            return true;
        } else {
            System.out.println("Java command not found when starting IAM microservice");
            return false;
        }

    }

    /**
     * Check the microservice executable war
     */
    private boolean buildMicroservicePath(String pa_home, String microservices_path) {

        String microservice_file = pa_home + separator + microservices_path + separator + microservice_name;

        if (new File(microservice_file).exists()) {
            command.add(microservice_file);
            return true;
        } else {
            System.out.println("IAM microservice is not deployed");
            return false;
        }
    }
}
