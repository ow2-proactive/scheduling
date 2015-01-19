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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;

import org.objectweb.proactive.extensions.processbuilder.OSProcessBuilder;
import org.ow2.proactive.scheduler.common.task.Decrypter;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.utils.ForkerUtils;
import org.ow2.proactive.scheduler.util.process.ThreadReader;
import org.apache.commons.io.FileUtils;


public class ForkedTaskExecutor implements TaskExecutor {

    private File workingDir;
    private Decrypter decrypter;

    // Called in forked JVM
    public static void main(String[] args) throws Throwable {
        try {
            String contextPath = args[0];

            TaskContext container = readFromFile(contextPath);

            TaskResultImpl result = new NonForkedTaskExecutor().execute(container, System.out, System.err);

            writeToFile(result, contextPath);

        } catch (Throwable throwable) {
            throwable.printStackTrace(); // will be read by forker
            System.exit(-1);
        }
    }

    public ForkedTaskExecutor(File workingDir, Decrypter decrypter) {
        this.workingDir = workingDir;
        this.decrypter = decrypter;
    }

    // Called by forker to run create forked JVM
    public TaskResultImpl execute(TaskContext context, PrintStream outputSink, PrintStream errorSink) {
        File serializedContext = null;
        try {
            serializedContext = serialize(context, workingDir);

            // setup walltime ? should it include file input copies?

            // fork

            OSProcessBuilder pb;
            String nativeScriptPath = PASchedulerProperties.SCHEDULER_HOME.getValueAsString(); // TODO inject
            if (isRunAsUser(context)) {
                workingDir.setWritable(true); // TODO warning log
                pb = ForkerUtils.getOSProcessBuilderFactory(nativeScriptPath).getBuilder(
                        ForkerUtils.checkConfigAndGetUser(decrypter));
            } else {
                pb = ForkerUtils.getOSProcessBuilderFactory(nativeScriptPath).getBuilder();
            }

            ForkEnvironment forkEnvironment = context.getExecutableContainer().getForkEnvironment();
            String javaHome;
            if (forkEnvironment != null && forkEnvironment.getJavaHome() != null &&
                !"".equals(forkEnvironment.getJavaHome())) {
                javaHome = forkEnvironment.getJavaHome();
            } else {
                javaHome = System.getProperty("java.home");
            }
            String javaExecutablePath = javaHome + File.separatorChar + "bin" + File.separatorChar + "java";

            // TODO limit classpath to a bare minimum
            pb.command(javaExecutablePath, "-cp", System.getProperty("java.class.path"),
                    ForkedTaskExecutor.class.getName(), serializedContext.getAbsolutePath()).directory(
              workingDir);

            Process process = pb.start();
            BufferedReader sout = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader serr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            Thread tsout = new Thread(new ThreadReader(sout, outputSink));
            Thread tserr = new Thread(new ThreadReader(serr, errorSink));
            tsout.start();
            tserr.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return new TaskResultImpl(context.getTaskId(),
                    new Exception("Failed to execute forked task"), null, 0);
            }

            outputSink.flush();
            errorSink.flush();

            return deserialize(serializedContext);
        } catch (Throwable throwable) {
            throwable.printStackTrace(errorSink); // double stack trace ?
            return new TaskResultImpl(context.getTaskId(),
              new Exception("Failed to execute forked task", throwable), null, 0);
        }
    }

    private boolean isRunAsUser(TaskContext context) {
        return context.getExecutableContainer().isRunAsUser();
    }

    private static void writeToFile(TaskResultImpl result, String contextPath) throws IOException {
        File file = new File(contextPath);

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(file));
        objectOutputStream.writeObject(result);
        objectOutputStream.close();
    }

    private static TaskContext readFromFile(String pathToFile) throws IOException, ClassNotFoundException {

        File f = new File(pathToFile);

        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(f));
        TaskContext container = (TaskContext) inputStream.readObject();

        FileUtils.forceDelete(f);
        return container;
    }

    private static TaskResultImpl deserialize(File pathToFile) throws IOException, ClassNotFoundException {

        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(pathToFile));
        TaskResultImpl scriptResult = (TaskResultImpl) inputStream.readObject();

        FileUtils.forceDelete(pathToFile);
        return scriptResult;
    }

    private File serialize(TaskContext context, File directory) throws IOException {
        File file = File.createTempFile(context.getTaskId().value(), null, directory);

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(file));
        objectOutputStream.writeObject(context);
        objectOutputStream.close();
        return file;
    }

}
