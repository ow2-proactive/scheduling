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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
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
    private final static String javaHomePostfixJavaExecutable = File.separatorChar + "bin" + File.separatorChar +
                                                                "java";

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
        Map<String, Serializable> variables = taskContextVariableExtractor.extractVariables(taskContext, true);
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
        if (!System.getProperty("java.class.path", "").contains("node.jar")) {
            // in case the class path of the node is not built with the node.jar, we
            // build the classpath with wildcards to avoid command too long errors on windows
            classpath.append(getStandardClassPathEntries(variables));
        }

        for (String classpathEntry : OneJar.getClasspath()) {
            classpath.append(File.pathSeparatorChar).append(classpathEntry);
        }

        if (forkEnvironment != null) {
            for (String jvmArgument : forkEnvironment.getJVMArguments()) {
                jvmArguments.add(VariableSubstitutor.filterAndUpdate(jvmArgument, variables));
            }

            for (String classpathEntry : forkEnvironment.getAdditionalClasspath()) {
                classpath.append(File.pathSeparatorChar)
                         .append(VariableSubstitutor.filterAndUpdate(classpathEntry, variables));
            }

            if (!Strings.isNullOrEmpty(forkEnvironment.getJavaHome())) {
                javaHome = VariableSubstitutor.filterAndUpdate(forkEnvironment.getJavaHome(), variables);
            }
        }

        List<String> prefixes = javaPrefixCommandExtractor.extractJavaPrefixCommandToCommandListFromScriptResult(forkEnvironmentScriptResult);

        List<String> javaCommand = new ArrayList<>(prefixes.size() + 3 + jvmArguments.size() + 2);
        javaCommand.addAll(prefixes);
        javaCommand.add(javaHome + javaHomePostfixJavaExecutable);

        javaCommand.add("-cp");
        javaCommand.add(classpath.toString());
        javaCommand.addAll(jvmArguments);
        javaCommand.add(ExecuteForkedTaskInsideNewJvm.class.getName());
        javaCommand.add(serializedContextAbsolutePath);

        return javaCommand;
    }

    private StringBuilder getStandardClassPathEntries(Map<String, Serializable> variables) throws IOException {
        StringBuilder classpathEntries = new StringBuilder();
        String schedulerHome;
        schedulerHome = System.getProperty(CentralPAPropertyRepository.PA_HOME.getName());
        if (schedulerHome == null) {
            schedulerHome = (String) variables.get("PA_SCHEDULER_HOME");
        }
        if (schedulerHome != null) {
            File paHome = new File(schedulerHome).getCanonicalFile();
            File distLib = new File(paHome, "dist/lib").getCanonicalFile();
            if (distLib.exists()) {
                File addons = new File(paHome, "addons").getCanonicalFile();
                classpathEntries.append(distLib);
                classpathEntries.append(File.pathSeparatorChar);
                classpathEntries.append(new File(distLib, "*"));
                classpathEntries.append(File.pathSeparatorChar);
                classpathEntries.append(addons);
                classpathEntries.append(File.pathSeparatorChar);
                classpathEntries.append(new File(addons, "*"));
            } else {
                return getClassPathEntriesUsingJavaClassPath();
            }
        } else {
            return getClassPathEntriesUsingJavaClassPath();
        }

        return classpathEntries;

    }

    private StringBuilder getClassPathEntriesUsingJavaClassPath() {
        StringBuilder classpathEntries = new StringBuilder();
        classpathEntries.append(System.getProperty("java.class.path", ""));
        return classpathEntries;
    }
}
