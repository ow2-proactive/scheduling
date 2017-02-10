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
package org.ow2.proactive.scheduler.task.executors.forked.env;

import java.io.*;

import org.apache.commons.io.FileUtils;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.context.TaskContext;
import org.ow2.proactive.scheduler.task.executors.InProcessTaskExecutor;


public class ExecuteForkedTaskInsideNewJvm {

    public static final int MAX_CONTEXT_WAIT = 6000;

    public static final String CONTEXT_FILE = "Context file ";

    private ExecuteForkedTaskInsideNewJvm() {

    }

    public static ExecuteForkedTaskInsideNewJvm getInstance() {
        return new ExecuteForkedTaskInsideNewJvm();
    }

    // 2 called by forked
    private TaskContext deserializeContext(String pathToFile)
            throws IOException, ClassNotFoundException, InterruptedException {
        File f = new File(pathToFile);
        if (!f.exists()) {
            throw new IllegalStateException(CONTEXT_FILE + f + " does not exist");
        }
        if (!f.canRead()) {
            throw new IllegalStateException(CONTEXT_FILE + f + " exists but cannot be read");
        }
        if (!f.canWrite()) {
            throw new IllegalStateException(CONTEXT_FILE + f + " exists but cannot be written");
        }
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(f))) {
            return (TaskContext) inputStream.readObject();
        } finally {
            deleteContextFile(f);
        }
    }

    private void deleteContextFile(File contextFile) throws InterruptedException {
        int waitCounter = 0;
        while (contextFile.exists() && waitCounter < MAX_CONTEXT_WAIT) {
            try {
                waitCounter++;
                FileUtils.forceDelete(contextFile);
            } catch (Exception e) {
                if (waitCounter == MAX_CONTEXT_WAIT) {
                    throw new IllegalStateException("Cannot remove " + CONTEXT_FILE + contextFile, e);
                } else {
                    Thread.sleep(100);
                }
            }
        }
    }

    // 3 called by forked
    private void serializeTaskResult(Object result, String contextPath) throws IOException {
        File file = new File(contextPath);
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(file))) {
            objectOutputStream.writeObject(result);
        }
    }

    /**
     * Everything here and called from here should only use System.out and System.err
     */
    public static void main(String[] args) throws Throwable {
        if (args.length != 1) {
            System.err.println("Path to serialized task context is expected");
            System.exit(-1);
        }

        ExecuteForkedTaskInsideNewJvm instance = ExecuteForkedTaskInsideNewJvm.getInstance();

        instance.fromForkedJVM(args[0]);

        // Call to System.exit is necessary at this point (when the task is finished normally) as the forked JVM can keep alive non-daemon threads
        System.exit(0);
    }

    private void fromForkedJVM(String contextPath) {
        try {
            TaskContext container = deserializeContext(contextPath);

            TaskResultImpl result = new InProcessTaskExecutor().execute(container, System.out, System.err);

            serializeTaskResult(result, contextPath);
        } catch (Throwable throwable) {
            throwable.printStackTrace(System.err);
            try {
                serializeTaskResult(throwable, contextPath);
            } catch (Throwable couldNotSerializeException) {
                System.err.println("Could not serialize exception as task result:");
                couldNotSerializeException.printStackTrace(System.err);
            }
            System.exit(1);
        }
    }
}
