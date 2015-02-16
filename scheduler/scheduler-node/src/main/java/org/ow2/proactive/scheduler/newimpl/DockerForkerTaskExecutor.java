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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;

import org.objectweb.proactive.extensions.processbuilder.OSProcessBuilder;
import org.ow2.proactive.scheduler.common.task.Decrypter;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.task.ExecutableContainer;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.forked.ForkedJavaExecutableContainer;
import org.ow2.proactive.scheduler.task.utils.ForkerUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;


public class DockerForkerTaskExecutor implements TaskExecutor {
    private static final Logger logger = Logger.getLogger(DockerForkerTaskExecutor.class);

    private File workingDir;
    private Decrypter decrypter;
    private Class<DockerForkerTaskExecutor> taskExecutorClass = DockerForkerTaskExecutor.class;

    public DockerForkerTaskExecutor(File workingDir, Decrypter decrypter) {
        this.workingDir = workingDir;
        this.decrypter = decrypter;
    }

    // Called by forker to run create forked JVM
    public TaskResultImpl execute(TaskContext context, PrintStream outputSink, PrintStream errorSink) {
        // Create docker container
        DockerContainerWrapper container = new DockerContainerWrapper(context.getTaskId().value());

        // Map working directory inside container
        container.addVolumeDirectory(workingDir.getAbsolutePath(), workingDir.getAbsolutePath());

        Process process = null;
        ProcessStreamsReader processStreamsReader = null;
        File serializedContext = null;

        try {
            serializedContext = serializeContext(context, workingDir);

            OSProcessBuilder pb;
            String nativeScriptPath = PASchedulerProperties.SCHEDULER_HOME.getValueAsString(); // TODO inject
            if (isRunAsUser(context)) {
                boolean workingDirCanBeWrittenByForked = workingDir.setWritable(true);
                if (!workingDirCanBeWrittenByForked) {
                    throw new Exception("Working directory will not be writable by runAsMe user");
                }
                pb = ForkerUtils.getOSProcessBuilderFactory(nativeScriptPath).getBuilder(
                        ForkerUtils.checkConfigAndGetUser(decrypter));
            } else {
                pb = ForkerUtils.getOSProcessBuilderFactory(nativeScriptPath).getBuilder();
            }

            // TODO limit classpath to a bare minimum
            pb.command("docker ", "run", "tobwiens/proactive-executer", "-Djava.security.policy=/home/sdolgov/proactive/repos/scheduling/config/security.java.policy-client", "-cp",
                    System.getProperty("java.class.path"), taskExecutorClass.getName(),
                    serializedContext.getAbsolutePath()).directory(workingDir);

            pb.command("/usr/bin/sudo",
                    "/usr/bin/docker",
                    "run" ,
                    "-v",
                    workingDir.getAbsolutePath()+":"+workingDir.getAbsolutePath(),
                    "--name", context.getTaskId().toString(),
                    "tobwiens/proactive-executer", taskExecutorClass.getName(),
                    serializedContext.getAbsolutePath()  );

            process = pb.start();
            processStreamsReader = new ProcessStreamsReader(process, outputSink, errorSink);

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                Throwable exception;
                try {
                    exception = (Throwable) deserializeTaskResult(serializedContext);
                } catch (Throwable ignored) {
                    return new TaskResultImpl(context.getTaskId(), new Exception("Failed to execute forked task",
                      ignored), null, 0);
                }
                return new TaskResultImpl(context.getTaskId(), new Exception("Failed to execute forked task",
                    exception), null, 0);
            }

            return (TaskResultImpl) deserializeTaskResult(serializedContext);
        } catch (Throwable throwable) {
            return new TaskResultImpl(context.getTaskId(), new Exception("Failed to execute forked task",
                throwable), null, 0);
        } finally {
            FileUtils.deleteQuietly(serializedContext);

            outputSink.flush();
            errorSink.flush();

            if (process != null) {
                process.destroy();
            }
            // Stop container
            container.stop();

            // Remove container
            container.remove();

            if (processStreamsReader != null) {
                processStreamsReader.close();
            }
        }
    }

    private String getJavaExecutablePath(ExecutableContainer executableContainer) {
        String javaHome;
        if (executableContainer instanceof ForkedJavaExecutableContainer &&
            ((ForkedJavaExecutableContainer) executableContainer).getForkEnvironment() != null &&
            ((ForkedJavaExecutableContainer) executableContainer).getForkEnvironment().getJavaHome() != null &&
            !((ForkedJavaExecutableContainer) executableContainer).getForkEnvironment().getJavaHome()
                    .isEmpty()) {
            javaHome = ((ForkedJavaExecutableContainer) executableContainer).getForkEnvironment()
                    .getJavaHome();
        } else {
            javaHome = System.getProperty("java.home");
        }
        return javaHome + File.separatorChar + "bin" + File.separatorChar + "java";
    }

    private boolean isRunAsUser(TaskContext context) {
        return context.getExecutableContainer().isRunAsUser();
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
    private static Object deserializeTaskResult(File pathToFile) throws IOException,
            ClassNotFoundException {
        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(pathToFile));
        Object scriptResult = inputStream.readObject();
        FileUtils.forceDelete(pathToFile);
        return scriptResult;
    }

    public static void main(String[] args) throws Throwable {
        if (args.length != 1) {
            System.err.println("Path to serialized task context is expected");
            System.exit(-1);
        }
        fromForkedJVM(args[0]);
    }

    private static void fromForkedJVM(String contextPath) {
        try {
            TaskContext container = deserializeContext(contextPath);

            TaskResultImpl result = new NonForkedTaskExecutor().execute(container, System.out, System.err);

            serializeTaskResult(result, contextPath);
        } catch (Throwable throwable) {
            try {
                serializeTaskResult(throwable, contextPath);
            } catch (Throwable ignored){
            }
            throwable.printStackTrace(System.err);
            System.exit(1);
        }
    }

}
