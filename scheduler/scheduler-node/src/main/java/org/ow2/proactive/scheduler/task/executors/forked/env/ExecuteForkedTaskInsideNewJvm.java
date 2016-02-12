/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.task.executors.forked.env;

import org.apache.commons.io.FileUtils;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.context.TaskContext;
import org.ow2.proactive.scheduler.task.executors.InProcessTaskExecutor;

import java.io.*;

public class ExecuteForkedTaskInsideNewJvm {

    // 2 called by forked
    private static TaskContext deserializeContext(String pathToFile) throws IOException,
            ClassNotFoundException {
        File f = new File(pathToFile);
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(f))) {
            return (TaskContext) inputStream.readObject();
        } finally {
            FileUtils.forceDelete(f);
        }
    }

    // 3 called by forked
    private static void serializeTaskResult(Object result, String contextPath) throws IOException {
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

        fromForkedJVM(args[0]);

        // Call to System.exit is necessary at this point (when the task is finished normally) as the forked JVM can keep alive non-daemon threads
        System.exit(0);
    }

    private static void fromForkedJVM(String contextPath) {
        try {
            TaskContext container = deserializeContext(contextPath);

            TaskResultImpl result = new InProcessTaskExecutor().execute(container, System.out, System.err);

            serializeTaskResult(result, contextPath);
        } catch (Throwable throwable) {
            throwable.printStackTrace(System.err);
            try {
                serializeTaskResult(throwable, contextPath);
            } catch (Throwable couldNotSerializeException) {
                System.err.println("Could not serialize exception as task result");
                couldNotSerializeException.printStackTrace(System.err);
            }
            System.exit(1);
        }
    }
}
