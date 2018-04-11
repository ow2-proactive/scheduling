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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import org.apache.log4j.Logger;


/**
 * Created by nebil on 09/04/18.
 */
public class MicroServiceStarter {

    private static final Logger logger = Logger.getLogger(MicroServiceStarter.class);

    private static final String separator = File.separator;

    private static final String os = System.getProperty("os.name");
    //private static final String ready_indicator = "Ready to process requests";

    private String pa_home;

    private String microservice_name;

    private boolean detached;

    private int timeout;

    private String ready_marker;

    private List<String> command = new ArrayList<String>();

    public MicroServiceStarter(String pa_home, String microservice_name, boolean detached, String ready_marker,
            int timeout) {
        this.pa_home = pa_home;
        this.microservice_name = microservice_name;
        this.detached = detached;
        this.timeout = timeout;
        this.ready_marker = ready_marker;
    }

    public Process start() throws InterruptedException, IOException {

        prepareCommand();

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

        System.out.println("Starting microservice " + microservice_name);
        Process process = processBuilder.start();
        System.out.println(streamOutput(process.getInputStream(), timeout));

        return process;
    }

    private String streamOutput(InputStream inputStream, int timeout) {

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(new Callable() {

            public String call() throws Exception {
                StringBuilder sb = new StringBuilder();
                BufferedReader br = null;

                try {
                    br = new BufferedReader(new InputStreamReader(inputStream));
                    String line = null;

                    while ((line = br.readLine()) != null) {
                        System.out.print(".");
                        if (line.contains(ready_marker)) {
                            break;
                        } else
                            sb.append(line + System.getProperty("line.separator"));
                    }
                } finally {
                    br.close();
                }
                return "\nMicroservice " + microservice_name + " started";
            }
        });

        try {
            return future.get(timeout, TimeUnit.SECONDS);
        } /*
           * catch (TimeoutException e) {
           * return "\nWarning: Microservice "
           * +microservice_name+" timed out to start. See the logs for the details.";
           * }
           */ catch (Exception e) {
            System.exit(1);
            return e.getMessage();
        } finally {
            executor.shutdownNow();
        }
    }

    private void prepareCommand() throws InterruptedException, IOException {
        if (detached)
            buildDetachedCommand(command);

        if (!buildJavaCommand(pa_home)) {
            System.out.println("java command not found");
            return;
        } else if (!buildMicroservicePath(pa_home, microservice_name)) {
            System.out.println("microservice " + microservice_name + " is not deployed");
            return;
        }

    }

    private void buildDetachedCommand(List<String> command) {

        if (os.equals(OperatingSystem.UNIX)) {
            // if the system is unix-based, we need to start the process with
            // the nohup indicator it normally goes with the end of the
            // command finished with the background indicator '&' (see the end
            // of command building)

            command.add("nohup");

        } else if (os.equals(OperatingSystem.WINDOWS)) {
            // Windows equivalent is to use the start command with /b option

            command.add("start /b");
        }
    }

    private boolean buildJavaCommand(String pa_home) {

        String javaCmd = pa_home + separator + "jre" + separator + "bin" + separator + "java";

        if (new File(javaCmd).exists()) {
            command.add(javaCmd);
            command.add("-jar");
            return true;
        } else
            return false;

    }

    private boolean buildMicroservicePath(String pa_home, String microservice_name) {

        String microservice_file = pa_home + separator + "dist" + separator + "boot" + separator + microservice_name;

        if (new File(microservice_file).exists()) {
            command.add(microservice_file);
            return true;
        } else
            return false;
    }

}
