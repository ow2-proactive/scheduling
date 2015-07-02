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
package org.ow2.proactive.scheduler.task.executors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.objectweb.proactive.extensions.processbuilder.OSProcessBuilder;
import org.objectweb.proactive.extensions.processbuilder.exception.NotImplementedException;
import org.ow2.proactive.resourcemanager.utils.OneJar;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.task.exceptions.ForkedJvmProcessException;
import org.ow2.proactive.scheduler.task.TaskContext;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.utils.Decrypter;
import org.ow2.proactive.scheduler.task.utils.ForkerUtils;
import org.ow2.proactive.scheduler.task.utils.ProcessStreamsReader;
import org.ow2.proactive.scheduler.task.utils.TaskProcessTreeKiller;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.ScriptHandler;
import org.ow2.proactive.scripting.ScriptLoader;
import org.ow2.proactive.scripting.ScriptResult;
import com.google.common.base.Strings;
import org.apache.commons.io.FileUtils;

/**
 * Executor in charge to fork a new process for running a non forked task in a dedicated JVM.
 *
 * @see ForkedTaskExecutor#fromForkedJVM(String)
 * @see InProcessTaskExecutor
 */
public class ForkedTaskExecutor implements TaskExecutor {

    private static final String FORK_ENVIRONMENT_BINDING_NAME = "forkEnvironment";

    private File workingDir;
    private Decrypter decrypter;

    public ForkedTaskExecutor(File workingDir, Decrypter decrypter) {
        this.workingDir = workingDir;
        this.decrypter = decrypter;
    }

    public TaskResultImpl execute(TaskContext context, PrintStream outputSink, PrintStream errorSink) {
        TaskProcessTreeKiller taskProcessTreeKiller = new TaskProcessTreeKiller(context.getTaskId().value());

        Process process = null;
        ProcessStreamsReader processStreamsReader = null;
        File serializedContext = null;

        try {
            serializedContext = serializeContext(context, workingDir);

            OSProcessBuilder processBuilder = createForkedProcess(context, serializedContext, outputSink, errorSink);

            try {
                taskProcessTreeKiller.tagEnvironment(processBuilder.environment());
            } catch (NotImplementedException e) {
                // SCHEDULING-986 : remove catch block when environment can be modified with runAsMe
            }

            process = processBuilder.start();
            processStreamsReader = new ProcessStreamsReader(process, outputSink, errorSink);

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                try {
                    Throwable exception = (Throwable) deserializeTaskResult(serializedContext);
                    return createTaskResult(context, exception);
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
            taskProcessTreeKiller.kill();
            if (processStreamsReader != null) {
                processStreamsReader.close();
            }
        }
    }

    private TaskResultImpl createTaskResult(TaskContext context, Throwable throwable) {
        return new TaskResultImpl(context.getTaskId(), new ForkedJvmProcessException(
                "Failed to execute task in a forked JVM", throwable));
    }

    private OSProcessBuilder createForkedProcess(TaskContext context, File serializedContext, PrintStream outputSink, PrintStream errorSink)
            throws Exception {
        OSProcessBuilder processBuilder;
        String nativeScriptPath = context.getSchedulerHome();

        if (context.isRunAsUser()) {
            boolean workingDirCanBeWrittenByForked = workingDir.setWritable(true);
            if (!workingDirCanBeWrittenByForked) {
                throw new Exception("Working directory will not be writable by runAsMe user");
            }
            processBuilder = ForkerUtils.getOSProcessBuilderFactory(nativeScriptPath).getBuilder(
                    ForkerUtils.checkConfigAndGetUser(decrypter));
        } else {
            processBuilder = ForkerUtils.getOSProcessBuilderFactory(nativeScriptPath).getBuilder();
        }

        String javaHome = System.getProperty("java.home");
        String jvmArguments = "";
        StringBuilder classpath = new StringBuilder("." + File.pathSeparatorChar);
        classpath.append(System.getProperty("java.class.path", ""));

        for (String classpathEntry : OneJar.getClasspath()) {
            classpath.append(File.pathSeparatorChar).append(classpathEntry);
        }

        ForkEnvironment forkEnvironment = context.getInitializer().getForkEnvironment();

        if (forkEnvironment != null) {
            if (forkEnvironment.getEnvScript() != null) {
                ScriptHandler scriptHandler = ScriptLoader.createLocalHandler();
                Map<String, Serializable> variables = InProcessTaskExecutor.taskVariables(context);
                Map<String, String> thirdPartyCredentials = InProcessTaskExecutor
                        .thirdPartyCredentials(context);
                InProcessTaskExecutor
                        .createBindings(context, scriptHandler, variables, thirdPartyCredentials);

                scriptHandler.addBinding(FORK_ENVIRONMENT_BINDING_NAME, forkEnvironment);
                Script<?> script = forkEnvironment.getEnvScript();

                InProcessTaskExecutor.replaceScriptParameters(script, thirdPartyCredentials, variables,
                        errorSink);
                ScriptResult scriptResult = scriptHandler.handle(script, outputSink, errorSink);
                if (scriptResult.errorOccured()) {
                    throw new Exception("Failed to execute fork environment script",
                        scriptResult.getException());
                }
            }

            for (String jvmArgument : forkEnvironment.getJVMArguments()) {
                jvmArguments += jvmArgument;
            }

            if (!Strings.isNullOrEmpty(forkEnvironment.getJavaHome())) {
                javaHome = forkEnvironment.getJavaHome();
            }

            for (String classpathEntry : forkEnvironment.getAdditionalClasspath()) {
                classpath.append(File.pathSeparatorChar).append(classpathEntry);
            }

            processBuilder.environment().putAll(forkEnvironment.getSystemEnvironment());
        }

        List<String> javaCommand = processBuilder.command();
        javaCommand.add(javaHome + File.separatorChar + "bin" + File.separatorChar + "java");
        javaCommand.add("-cp");
        javaCommand.add(classpath.toString());
        if(!Objects.equals(jvmArguments, "")){
            javaCommand.add(jvmArguments);
        }
        javaCommand.add(ForkedTaskExecutor.class.getName());
        javaCommand.add(serializedContext.getAbsolutePath());

        processBuilder.directory(workingDir);
        return processBuilder;
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
        try {
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(pathToFile));
            Object scriptResult = inputStream.readObject();
            FileUtils.forceDelete(pathToFile);
            return scriptResult;
        } catch (IOException e) {
            throw new ForkedJvmProcessException(
                "Could not read serialized task result (forked JVM may have been killed by the task or could not write to local space)",
                e);
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
