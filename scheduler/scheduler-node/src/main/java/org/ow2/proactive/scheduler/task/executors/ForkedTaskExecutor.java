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
package org.ow2.proactive.scheduler.task.executors;

import org.apache.commons.io.FileUtils;
import org.objectweb.proactive.extensions.processbuilder.OSProcessBuilder;
import org.objectweb.proactive.extensions.processbuilder.exception.NotImplementedException;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.context.TaskContext;
import org.ow2.proactive.scheduler.task.context.TaskContextSerializer;
import org.ow2.proactive.scheduler.task.exceptions.ForkedJvmProcessException;
import org.ow2.proactive.scheduler.task.executors.forked.env.ExecuteForkedTaskInsideNewJvm;
import org.ow2.proactive.scheduler.task.utils.ProcessStreamsReader;
import org.ow2.proactive.utils.CookieBasedProcessTreeKiller;

import java.io.*;


/**
 * Executor in charge to fork a new process for running a non forked task in a dedicated JVM.
 *
 * @see ExecuteForkedTaskInsideNewJvm#fromForkedJVM(String)
 * @see InProcessTaskExecutor
 */
public class ForkedTaskExecutor implements TaskExecutor {

    private final ForkedProcessBuilderCreator forkedJvmProcessBuilderCreator = new ForkedProcessBuilderCreator();
    private final TaskContextSerializer taskContextSerializer = new TaskContextSerializer();

    private final File workingDir;

    public ForkedTaskExecutor(File workingDir) {
        this.workingDir = workingDir;
    }

    @Override
    public TaskResultImpl execute(TaskContext context, PrintStream outputSink, PrintStream errorSink) {
        CookieBasedProcessTreeKiller taskProcessTreeKiller = null;
        Process process = null;
        ProcessStreamsReader processStreamsReader = null;
        File serializedContext = null;

        try {
            serializedContext = taskContextSerializer.serializeContext(context, workingDir);

            OSProcessBuilder processBuilder = forkedJvmProcessBuilderCreator.createForkedProcessBuilder(
                    context, serializedContext, outputSink,
                    errorSink, workingDir);

            try {
                TaskId taskId = context.getTaskId();

                String cookieNameSuffix = "Job" + taskId.getJobId().value() + "Task" + taskId.value();

                taskProcessTreeKiller =
                        CookieBasedProcessTreeKiller.createProcessChildrenKiller(
                                cookieNameSuffix, processBuilder.environment());
            } catch (NotImplementedException e) {
                // SCHEDULING-986 : remove catch block when environment can be modified with runAsMe
            }

            process = processBuilder.start();
            processStreamsReader = new ProcessStreamsReader(process, outputSink, errorSink);

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                try {
                    Object error = deserializeTaskResult(serializedContext);
                    if (error instanceof TaskContext) {
                        return createTaskResult(context, new IOException(
                                "Forked task failed to remove serialized task context, " +
                                        "probably a permission issue on folder " + workingDir));
                    } else {
                        Throwable exception = (Throwable) error;
                        return createTaskResult(context, exception);
                    }
                } catch (Throwable cannotDeserializeResult) {
                    return createTaskResult(context, cannotDeserializeResult);
                }
            }

            return (TaskResultImpl) deserializeTaskResult(serializedContext);
        } catch (Throwable throwable) {
            return createTaskResult(context, throwable);
        } finally {
            FileUtils.deleteQuietly(serializedContext);

            if (process != null) {
                process.destroy();
            }
            if (taskProcessTreeKiller != null) {
                taskProcessTreeKiller.kill();
            }
            if (processStreamsReader != null) {
                processStreamsReader.close();
            }
        }
    }

    private TaskResultImpl createTaskResult(TaskContext context, Throwable throwable) {
        return new TaskResultImpl(context.getTaskId(), new ForkedJvmProcessException(
                "Failed to execute task in a forked JVM", throwable));
    }

    // 4 called by forker
    private Object deserializeTaskResult(File pathToFile) throws IOException, ClassNotFoundException {
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(pathToFile))) {
            return inputStream.readObject();
        } catch (IOException e) {
            throw new ForkedJvmProcessException(
                    "Could not read serialized task result (forked JVM may have been killed by the task or could not write to local space)",
                    e);
        } finally {
            FileUtils.forceDelete(pathToFile);
        }
    }

}
