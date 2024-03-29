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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.pamr.PAMRConfig;
import org.objectweb.proactive.extensions.processbuilder.CoreBindingDescriptor;
import org.objectweb.proactive.extensions.processbuilder.OSProcessBuilder;
import org.objectweb.proactive.extensions.processbuilder.OSUser;
import org.objectweb.proactive.extensions.processbuilder.PAOSProcessBuilderFactory;
import org.objectweb.proactive.extensions.processbuilder.exception.CoreBindingException;
import org.objectweb.proactive.extensions.processbuilder.exception.FatalProcessBuilderException;
import org.objectweb.proactive.extensions.processbuilder.exception.OSUserException;
import org.ow2.proactive.scheduler.common.job.JobVariable;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.task.containers.ScriptExecutableContainer;
import org.ow2.proactive.scheduler.task.context.NodeDataSpacesURIs;
import org.ow2.proactive.scheduler.task.context.NodeInfo;
import org.ow2.proactive.scheduler.task.context.TaskContext;
import org.ow2.proactive.scheduler.task.executors.forked.env.command.JavaPrefixCommandExtractor;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;
import org.ow2.tests.ProActiveTestClean;


public class ForkedJvmTaskExecutionCommandCreatorTest extends ProActiveTestClean {
    private String jobNameValue = "TestJobName";

    private String jobOwnerValue = "TestOwner";

    private String taskNameValue = "TestTaskName";

    private long taskIdValue = 20L;

    private long jobIdValue = 12L;

    private int iterationIndexValue = 40;

    private int taskReplicationValue = 20;

    private String testVariable1Key = "TestVariable1";

    private String testVariable1Value = "valueForTest1";

    private String[] testPreJaveCommandString = new String[] { "My", "Command" };

    private String[] testPreJaveCommandStringList = new String[] { "His", "Commands" };

    private String[] forkEnvJvmArguments = new String[] { "Arg1", "Arg2" };

    private String serializedContextAbsolutePath = "/some/absolute/path.file";

    private String additionalClasspath = "additionalClasspath";

    private String forkenvironmentJavaHome = "java/home/from/fork/env";

    @Test
    public void testAllNullReturnsAnEmptyList() throws Exception {
        ForkedJvmTaskExecutionCommandCreator forkedJvmTaskExecutionCommandCreator = new ForkedJvmTaskExecutionCommandCreator();
        List<String> containsJavaHome = forkedJvmTaskExecutionCommandCreator.createForkedJvmTaskExecutionCommand(null,
                                                                                                                 null,
                                                                                                                 null,
                                                                                                                 (new PAOSProcessBuilderFactory(".")).getBuilder());
        assertThat(containsJavaHome.size(), is(0));
    }

    @Test
    public void testExecCommandUsesJavaHomeFromSystemProperties() throws Exception {
        javaCommandContainsOrNot(Arrays.asList(new String[] { System.getProperty("java.home") }),
                                 createForkEnvironment(),
                                 true,
                                 false);
    }

    @Test
    public void testExecCommandUsesClassPathSystemProperties() throws Exception {
        // before test (clear 'proactive.home' system property)
        String proactiveHomeProperty = CentralPAPropertyRepository.PA_HOME.getName();
        String proactiveHomeValue = System.getProperty(proactiveHomeProperty);
        if (proactiveHomeValue != null) {
            System.clearProperty(proactiveHomeProperty);
        }

        // test
        javaCommandContainsOrNot(Arrays.asList(new String[] { "-cp", System.getProperty("java.class.path") }),
                                 createForkEnvironment(),
                                 true,
                                 false);

        // after test (reset 'proactive.home')
        System.setProperty(proactiveHomeProperty, proactiveHomeValue);
    }

    @Test
    public void testExecCommandContainsJavaCommandPrefix() throws Exception {
        javaCommandContainsOrNot(Arrays.asList(testPreJaveCommandString), createForkEnvironment(), true, true);
    }

    @Test
    public void testExecCommandContainsJavaCommandPrefixList() throws Exception {
        ForkEnvironment forkEnv = createForkEnvironment();
        for (String arg : testPreJaveCommandStringList) {
            forkEnv.addPreJavaCommand(arg);
        }
        javaCommandContainsOrNot(Arrays.asList(testPreJaveCommandStringList), forkEnv, true, false);

        // when the prefix string is added, it overrides the pre command list
        javaCommandContainsOrNot(Arrays.asList(testPreJaveCommandString), forkEnv, true, true);
        javaCommandContainsOrNot(Arrays.asList(testPreJaveCommandStringList), forkEnv, false, true);

    }

    @Test
    public void testExecCommandContainsJavaArgumentsExtractedFromForkEnvironment() throws Exception {
        javaCommandContainsOrNot(Arrays.asList(forkEnvJvmArguments), createForkEnvironment(), true, false);
    }

    @Test
    public void testExecCommandContainsAbsolutePathOfSerializedContext() throws Exception {
        javaCommandContainsOrNot(Arrays.asList(serializedContextAbsolutePath), createForkEnvironment(), true, false);
    }

    @Test
    public void testExecCommandContainsAdditionalClasspathSavedInForkEnvironment() throws Exception {
        javaCommandContainsOrNot(Arrays.asList(additionalClasspath), createForkEnvironment(), true, false);
    }

    @Test
    public void testExecCommandOverwritesJavaHomeFromForkEnvironment() throws Exception {
        ForkEnvironment forkEnvironment = createForkEnvironment();
        forkEnvironment.setJavaHome(forkenvironmentJavaHome);
        javaCommandContainsOrNot(Arrays.asList(forkenvironmentJavaHome), forkEnvironment, true, false);
    }

    @Test
    public void testExecCommandForwardsProActiveProperties() throws Exception {
        PAMRConfig.PA_NET_ROUTER_PORT.setValue(33648);
        ForkEnvironment forkEnvironment = createForkEnvironment();
        javaCommandContainsOrNot(Arrays.asList(PAMRConfig.PA_NET_ROUTER_PORT.getCmdLine() + 33648),
                                 forkEnvironment,
                                 true,
                                 false);
    }

    @Test
    public void testExecCommandProActivePropertiesCanBeOverriden() throws Exception {
        PAMRConfig.PA_NET_ROUTER_PORT.setValue(33648);
        ForkEnvironment forkEnvironment = createForkEnvironment();
        forkEnvironment.addJVMArgument(PAMRConfig.PA_NET_ROUTER_PORT.getCmdLine() + 33649);
        javaCommandContainsOrNot(Arrays.asList(PAMRConfig.PA_NET_ROUTER_PORT.getCmdLine() + 33649),
                                 forkEnvironment,
                                 true,
                                 false);
        javaCommandContainsOrNot(Arrays.asList(PAMRConfig.PA_NET_ROUTER_PORT.getCmdLine() + 33648),
                                 forkEnvironment,
                                 false,
                                 false);
    }

    private void javaCommandContainsOrNot(List<String> stringsContained, ForkEnvironment forkEnvironment,
            boolean contains, boolean addJavaPrefix) throws Exception {
        ForkedJvmTaskExecutionCommandCreator forkedJvmTaskExecutionCommandCreator = new ForkedJvmTaskExecutionCommandCreator();
        if (addJavaPrefix) {
            replaceJavaPrefixCommandCreatorWithMock(forkedJvmTaskExecutionCommandCreator);
        }

        TaskContext taskContext = createTaskContext();
        taskContext.getInitializer().setForkEnvironment(forkEnvironment);

        List<String> containsJavaHome = forkedJvmTaskExecutionCommandCreator.createForkedJvmTaskExecutionCommand(taskContext,
                                                                                                                 null,
                                                                                                                 serializedContextAbsolutePath,
                                                                                                                 (new PAOSProcessBuilderFactory(".")).getBuilder());

        for (String insideJavaCommand : stringsContained) {
            if (contains) {
                assertThatListHasAtLeastOneStringWhichContains(containsJavaHome, insideJavaCommand);
            } else {
                assertThatListHasNoStringWhichContains(containsJavaHome, insideJavaCommand);
            }
        }
    }

    /**
     * Replaces the java prefix command creator service with a mock
     *
     * @param forkedJvmTaskExecutionCommandCreator Instance which will have the service replaced.
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     */
    private void replaceJavaPrefixCommandCreatorWithMock(
            ForkedJvmTaskExecutionCommandCreator forkedJvmTaskExecutionCommandCreator)
            throws IllegalAccessException, NoSuchFieldException {
        JavaPrefixCommandExtractor javaPrefixCommandExtractor = mock(JavaPrefixCommandExtractor.class);
        given(javaPrefixCommandExtractor.extractJavaPrefixCommandToCommandListFromScriptResult(any(ScriptResult.class))).willReturn(Arrays.asList(testPreJaveCommandString));

        setPrivateField(ForkedJvmTaskExecutionCommandCreator.class.getDeclaredField("javaPrefixCommandExtractor"),
                        forkedJvmTaskExecutionCommandCreator,
                        javaPrefixCommandExtractor);
    }

    /**
     * Assert that a list if strings has at least one string which contains as a substring the matchig string.
     *
     * @param list     List of strings.
     * @param matching String to look for in the list of strings.
     */
    private void assertThatListHasAtLeastOneStringWhichContains(List<String> list, String matching) {
        boolean hasOneStringContaining = false;
        for (String string : list) {
            if (string.contains(matching)) {
                hasOneStringContaining = true;
            }
        }
        assertThat("List did not contain string with: " + matching + ".\n But was: " + list,
                   hasOneStringContaining,
                   is(true));
    }

    /**
     * Assert that a list if strings has no string which contains as a substring the matching string.
     *
     * @param list     List of strings.
     * @param shouldNotMatch String to look for in the list of strings.
     */
    private void assertThatListHasNoStringWhichContains(List<String> list, String shouldNotMatch) {
        boolean hasOneStringContaining = false;
        for (String string : list) {
            if (string.contains(shouldNotMatch)) {
                hasOneStringContaining = true;
            }
        }
        assertThat("List should not contain string with: " + shouldNotMatch + ".\n But was: " + list,
                   hasOneStringContaining,
                   is(false));
    }

    /**
     * Sets a private field.
     *
     * @param privateField The private field to set.
     * @param target       Instance of class, in which to set the field.
     * @param value        Value to set the field to.
     */
    private void setPrivateField(Field privateField, Object target, Object value) throws IllegalAccessException {
        privateField.setAccessible(true);
        privateField.set(target, value);
        privateField.setAccessible(false);
    }

    private TaskContext createTaskContext() throws InvalidScriptException, NodeException {
        ScriptExecutableContainer scriptContainer = new ScriptExecutableContainer(new TaskScript(new SimpleScript("print('hello'); result='hello'",
                                                                                                                  "javascript")));
        TaskLauncherInitializer taskLauncherInitializer = getTaskLauncherInitializerWithWorkflowVariableAndForkEnvironment();

        TaskContext taskContext = new TaskContext(scriptContainer,
                                                  taskLauncherInitializer,
                                                  null,
                                                  new NodeDataSpacesURIs(null, null, null, null, null, null),
                                                  null,
                                                  new NodeInfo(null, null, null, null));
        return taskContext;
    }

    private TaskLauncherInitializer createTaskLauncherInitializer() {
        TaskLauncherInitializer taskLauncherInitializer = new TaskLauncherInitializer();
        TaskId taskId = createTaskId();

        taskLauncherInitializer.setTaskId(taskId);
        taskLauncherInitializer.setIterationIndex(iterationIndexValue);
        taskLauncherInitializer.setJobOwner(jobOwnerValue);
        taskLauncherInitializer.setReplicationIndex(taskReplicationValue);
        return taskLauncherInitializer;
    }

    private TaskId createTaskId() {
        return TaskIdImpl.createTaskId(new JobIdImpl(jobIdValue, jobNameValue), taskNameValue, taskIdValue);
    }

    private TaskLauncherInitializer getTaskLauncherInitializerWithWorkflowVariableAndForkEnvironment() {
        // Create and setup task launcher initializer
        TaskLauncherInitializer taskLauncherInitializer = createTaskLauncherInitializer();
        Map<String, JobVariable> variablesToPut = new HashMap<>();
        variablesToPut.put(testVariable1Key, new JobVariable(testVariable1Key, testVariable1Value));
        taskLauncherInitializer.setJobVariables(variablesToPut);
        return taskLauncherInitializer;
    }

    private ForkEnvironment createForkEnvironment() {
        ForkEnvironment forkEnv = new ForkEnvironment();
        forkEnv.addJVMArgument(forkEnvJvmArguments[0]);
        forkEnv.addJVMArgument(forkEnvJvmArguments[1]);
        forkEnv.addAdditionalClasspath(additionalClasspath);
        return forkEnv;
    }
}
