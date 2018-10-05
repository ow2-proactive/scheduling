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
package org.ow2.proactive.scheduler.task.executors;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.objectweb.proactive.extensions.processbuilder.OSProcessBuilder;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.context.TaskContext;
import org.ow2.proactive.scheduler.task.context.TaskContextSerializer;
import org.ow2.proactive.scheduler.task.exceptions.ForkedJvmProcessException;
import org.ow2.proactive.scheduler.task.executors.forked.env.ExecuteForkedTaskInsideNewJvm;
import org.ow2.proactive.scheduler.task.utils.ProcessStreamsReader;
import org.ow2.proactive.scheduler.task.utils.task.termination.CleanupTimeoutGetter;
import org.ow2.proactive.utils.CookieBasedProcessTreeKiller;


/**
 * Executor in charge to fork a new process for running a non forked task in a dedicated JVM.
 *
 * @see ExecuteForkedTaskInsideNewJvm#fromForkedJVM(String)
 * @see InProcessTaskExecutor
 */
public class ForkedTaskExecutor implements TaskExecutor {

    private static final Logger logger = Logger.getLogger(TaskExecutor.class);

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
            if (!workingDir.exists()) {
                FileUtils.forceMkdir(workingDir);
            }
            serializedContext = taskContextSerializer.serializeContext(context, workingDir);

            OSProcessBuilder processBuilder = forkedJvmProcessBuilderCreator.createForkedProcessBuilder(context,
                                                                                                        serializedContext,
                                                                                                        outputSink,
                                                                                                        errorSink,
                                                                                                        workingDir);

            TaskId taskId = context.getTaskId();

            String cookieNameSuffix = "Job" + taskId.getJobId().value() + "Task" + taskId.value();

            taskProcessTreeKiller = CookieBasedProcessTreeKiller.createProcessChildrenKiller(cookieNameSuffix,
                                                                                             processBuilder.environment());

            process = processBuilder.start();
            processStreamsReader = new ProcessStreamsReader(taskId.toString(), process, outputSink, errorSink);

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                try {
                    Object error = deserializeTaskResult(serializedContext);
                    if (error instanceof TaskContext) {
                        return createTaskResult(context,
                                                new IOException("Forked JVM process returned with exit code " +
                                                                exitCode + ", see task logs for more information"));
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
                try {
                    //wait for twice the time of the cleanup process
                    process.waitFor((new CleanupTimeoutGetter()).getCleanupTimeSeconds(), TimeUnit.SECONDS);
                } catch (Exception e) {
                    logger.info("Exception while waiting task to finish " + e);
                }
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
        return new TaskResultImpl(context.getTaskId(),
                                  new ForkedJvmProcessException("Failed to execute task in a forked JVM", throwable));
    }

    // 4 called by forker
    private Object deserializeTaskResult(File pathToFile) throws IOException, ClassNotFoundException {
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(pathToFile))) {
            return inputStream.readObject();
        } catch (IOException e) {
            throw new ForkedJvmProcessException("Could not read serialized task result (forked JVM may have been killed by the task or could not write to local space)",
                                                e);
        } finally {
            FileUtils.deleteQuietly(pathToFile);
        }
    }

}
