/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2014 INRIA/University of
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
package org.ow2.proactive.scheduler.newimpl;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.objectweb.proactive.extensions.processbuilder.OSProcessBuilder;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.newimpl.utils.Decrypter;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.utils.ForkerUtils;

import java.io.*;


public class DockerForkerTaskExecutor implements TaskExecutor {
    private static final Logger logger = Logger.getLogger(DockerForkerTaskExecutor.class);

    // TODO What to do with the decrypter it is contained in the context anyway

    private File workingDir;
    private Decrypter decrypter;
    private Class<DockerForkerTaskExecutor> taskExecutorClass = DockerForkerTaskExecutor.class;
    private TimedCommandExecutor executor;

    public DockerForkerTaskExecutor(File workingDir, Decrypter decrypter) {
        this(workingDir, decrypter, null);
    }

    public DockerForkerTaskExecutor(File workingDir, Decrypter decrypter, TimedCommandExecutor executor) {
        this.workingDir = workingDir;
        this.decrypter = decrypter;
        this.executor = executor;
    }

    private OSProcessBuilder createForkedProcess(TaskContext context)
            throws Exception {
        OSProcessBuilder pb;
        String nativeScriptPath = PASchedulerProperties.SCHEDULER_HOME.getValueAsString(); // TODO inject
        if (context.isRunAsUser()) {
            boolean workingDirCanBeWrittenByForked = workingDir.setWritable(true);
            if (!workingDirCanBeWrittenByForked) {
                throw new Exception("Working directory will not be writable by runAsMe user");
            }
            pb = ForkerUtils.getOSProcessBuilderFactory(nativeScriptPath).getBuilder(
                    ForkerUtils.checkConfigAndGetUser(decrypter));
        } else {
            pb = ForkerUtils.getOSProcessBuilderFactory(nativeScriptPath).getBuilder();
        }

        return pb;
    }

    // Called by forker to run context inside docker container
    public TaskResultImpl execute(TaskContext context, PrintStream outputSink, PrintStream errorSink) {
        // Create docker container
        DockerContainerWrapper container = new DockerContainerWrapper(context.getTaskId().value());

        //String containerPathToContext = "/data/context/" + workingDir.getName();
        String containerPathToContext = "/data/context";


        // Map working directory inside container
        container.addVolumeDirectory(workingDir.getAbsolutePath(), containerPathToContext);

        File serializedContext = null;
        try {
            // Check if process builder was added with constructor and create it if neccessary
            if( this.executor == null) {
                this.executor = new PBCommandExecutor(this.createForkedProcess(context));
            }

            // Serialize locally - because container maps local directory
            serializedContext = serializeContext(context, workingDir);

            String pathToContextFile = containerPathToContext + "/" + serializedContext.getName();
            // Create start command
            String[] startCommand = container.start(this.taskExecutorClass.getName(), pathToContextFile);

            // Execute command
            int exitCode = this.executor.executeCommand(outputSink, errorSink, startCommand);

            // If execution was not successful
            if (exitCode != 0) {
                Throwable exception;
                try {
                    // Check if written result is
                    Object resultContext = deserializeTaskResult(serializedContext);

                    // If exception found in context, return it as task result
                    if (resultContext instanceof Throwable) {
                        exception = (Throwable) resultContext;
                        return new TaskResultImpl(context.getTaskId(), new Exception(
                                "Failed to execute forked task.", exception), null, 0);
                    }
                } catch (ClassNotFoundException ignored) {
                    return new TaskResultImpl(context.getTaskId(), new Exception(
                            "Failed to deserialize result.", ignored), null, 0);
                }

            }

            // Execution successful return task result
            return (TaskResultImpl) deserializeTaskResult(serializedContext);
        } catch (Throwable throwable) {
            return new TaskResultImpl(context.getTaskId(), new Exception("Failed to execute forked task",
                    throwable), null, 0);
        } finally {
            // Clean up
            FileUtils.deleteQuietly(serializedContext);

            outputSink.flush();
            errorSink.flush();

            try {
                // Stop container - Thread might have been interrupted
                String[] stopCommand = container.stop();
                this.executor.executeTimedWhileInterrupted(null, null, stopCommand);

                // Remove container
                String[] removeCommand = container.remove();
                this.executor.executeTimedWhileInterrupted(null, null, removeCommand);
            } catch (FailedExecutionException e) {
                DockerForkerTaskExecutor.logger.warn("Failed to kill the running container.", e);
            } catch (InterruptedException e) {
                DockerForkerTaskExecutor.logger.warn("Killing of container " + container.getName() +
                        " interrupted." + "It can be stopped and remove with: sudo docker rm -f " +
                        container.getName(), e);
            }

        }
    }

    // 1 called by forker
    private static File serializeContext(TaskContext context, File directory) throws IOException {
        File file = File.createTempFile(context.getTaskId().value(), null, directory);

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(file));
        objectOutputStream.writeObject(context);
        objectOutputStream.close();
        return file;
    }

    // 2 called by forked
    private static TaskContext deserializeContext(String pathToFile) throws IOException,
            ClassNotFoundException {
        File f = new File(pathToFile);
        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(f));
        TaskContext container = (TaskContext) inputStream.readObject();
        FileUtils.forceDelete(f);
        return container;
    }

    // 3 called by forked
    private static void serializeTaskResult(Object result, String contextPath) throws IOException {
        File file = new File(contextPath);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(file));
        objectOutputStream.writeObject(result);
        objectOutputStream.close();
    }

    // 4 called by forker
    private static Object deserializeTaskResult(File pathToFile) throws IOException, ClassNotFoundException {
        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(pathToFile));
        Object scriptResult = inputStream.readObject();
        FileUtils.forceDelete(pathToFile);
        return scriptResult;
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

        // R Engine might not exit so do it here
        System.exit(0); // Reaching this point will always indicate successful execution
    }

    private static void fromForkedJVM(String contextPath) {
        try {
            TaskContext container = deserializeContext(contextPath);

            TaskResultImpl result = new NonForkedTaskExecutor().execute(container, System.out, System.err);
            serializeTaskResult(result, contextPath);


        } catch (Throwable throwable) {
            try {
                serializeTaskResult(throwable, contextPath);
            } catch (Throwable ignored) {
            }
            throwable.printStackTrace(System.err);
            System.exit(1);

        }
    }

}
