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

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ow2.proactive.resourcemanager.utils.OneJar;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.util.VariableSubstitutor;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.task.context.TaskContext;
import org.ow2.proactive.scheduler.task.context.TaskContextVariableExtractor;
import org.ow2.proactive.scheduler.task.executors.forked.env.command.JavaPrefixCommandExtractor;
import org.ow2.proactive.scripting.ForkEnvironmentScriptResult;
import org.ow2.proactive.scripting.ScriptResult;
import com.google.common.base.Strings;

public class ForkedJvmTaskExecutionCommandCreator implements Serializable {
    private final static String javaHomePostfixJavaExecutable = File.separatorChar + "bin" + File.separatorChar + "java";
    private final TaskContextVariableExtractor taskContextVariableExtractor = new TaskContextVariableExtractor();
    private final JavaPrefixCommandExtractor javaPrefixCommandExtractor = new JavaPrefixCommandExtractor();

    /**
     * Creates a command to start a task inside a java virtual machine.
     *
     * @param taskContext                   TaskContext object describing the task.
     * @param forkEnvironmentScriptResult   Result from a running fork environment script. If it is
     *                                      of instance {@link ForkEnvironmentScriptResult}, the script return
     *                                      variables will be used for the construction of the command.
     * @param serializedContextAbsolutePath The serialized TaskContext object which will be read by the virtual
     *                                      machine to run the task.
     * @return A List, empty if the TaskContext is null, otherwise filled with a command.
     * @throws Exception If the {@link TaskContextVariableExtractor} could not extract all variables from the
     *                   TaskContext.
     */
    public List<String> createForkedJvmTaskExecutionCommand(TaskContext taskContext,
            ScriptResult forkEnvironmentScriptResult, String serializedContextAbsolutePath) throws Exception {
        if (taskContext == null) {
            return new ArrayList<>(0);
        }
        Map<String, Serializable> variables = taskContextVariableExtractor.extractTaskVariables(taskContext);
        String javaHome = System.getProperty("java.home");
        ArrayList<String> jvmArguments = new ArrayList<>(1);

        ForkEnvironment forkEnvironment = null;
        if (taskContext.getInitializer() != null) {
            forkEnvironment = taskContext.getInitializer().getForkEnvironment();
        }

        // set the task fork property so that script engines have a mean to know
        // if they are running in a forked task or not
        jvmArguments.add(PASchedulerProperties.TASK_FORK.getCmdLine() + "true");

        StringBuilder classpath = new StringBuilder("." + File.pathSeparatorChar);
        classpath.append(System.getProperty("java.class.path", ""));

        for (String classpathEntry : OneJar.getClasspath()) {
            classpath.append(File.pathSeparatorChar).append(classpathEntry);
        }

        if (forkEnvironment != null) {
            for (String jvmArgument : forkEnvironment.getJVMArguments()) {
                jvmArguments.add(VariableSubstitutor.filterAndUpdate(jvmArgument, variables));
            }

            for (String classpathEntry : forkEnvironment.getAdditionalClasspath()) {
                classpath.append(File.pathSeparatorChar).append(
                        VariableSubstitutor.filterAndUpdate(classpathEntry, variables));
            }

            if (!Strings.isNullOrEmpty(forkEnvironment.getJavaHome())) {
                javaHome = VariableSubstitutor.filterAndUpdate(forkEnvironment.getJavaHome(), variables);
            }
        }

        List<String> javaCommand = new ArrayList<>();
        javaCommand.addAll(javaPrefixCommandExtractor
                .extractJavaPrefixCommandToCommandListFromScriptResult(forkEnvironmentScriptResult));
        javaCommand.add(javaHome + javaHomePostfixJavaExecutable);
        javaCommand.add("-cp");
        javaCommand.add(classpath.toString());
        javaCommand.addAll(jvmArguments);
        javaCommand.add(ExecuteForkedTaskInsideNewJvm.class.getName());
        javaCommand.add(serializedContextAbsolutePath);

        return javaCommand;
    }
}
