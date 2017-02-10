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
import static org.hamcrest.core.Is.isA;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.security.KeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.rest.ds.IDataSpaceClient;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.proactive.scheduler.task.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.client.DataSpaceNodeClient;
import org.ow2.proactive.scheduler.task.client.SchedulerNodeClient;
import org.ow2.proactive.scheduler.task.containers.ScriptExecutableContainer;
import org.ow2.proactive.scheduler.task.context.NodeDataSpacesURIs;
import org.ow2.proactive.scheduler.task.context.TaskContext;
import org.ow2.proactive.scheduler.task.utils.Decrypter;
import org.ow2.proactive.scheduler.task.utils.VariablesMap;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.ScriptHandler;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;


public class ForkedTaskVariablesManagerTest {
    private String jobNameValue = "TestJobName";

    private String taskNameValue = "TestTaskName";

    private long taskIdValue = 20L;

    private long jobIdValue = 12L;

    private String testVariable1Key = "TestVariable1";

    private String testVariable1Value = "valueForTest1";

    private String testCred1Key = "TestCred1";

    private String testCred1Value = "valueForTestCred1";

    private String testUser = "User";

    private String testPass = "Pass";

    private String thirdPartyCred1Key = "ThirdpartyCred1";

    private String thirdPartyCred1Value = "ThirdPArtCred1Value";

    private String testSetString = "ThisIsASetString";

    @Test
    public void testAddBindingsToScriptHandlerContainsVariables()
            throws InvalidScriptException, NodeException, NoSuchFieldException, IllegalAccessException {
        ScriptHandler scriptHandler = new ScriptHandler();

        Map<String, Serializable> variables = new HashMap<>();
        variables.put(testVariable1Key, testVariable1Value);

        VariablesMap variablesMap = new VariablesMap();
        variablesMap.setInheritedMap(variables);

        validateThatScriptHandlerBindingsContain(scriptHandler,
                                                 createTaskContext(null),
                                                 variablesMap,
                                                 new HashMap<String, String>(),
                                                 new HashMap<String, String>(),
                                                 SchedulerConstants.VARIABLES_BINDING_NAME,
                                                 variablesMap);

    }

    @Test
    public void testAddBindingsToScriptHandlerContainsResultMetadata()
            throws InvalidScriptException, NodeException, NoSuchFieldException, IllegalAccessException {
        ScriptHandler scriptHandler = new ScriptHandler();

        Map<String, String> resultMetadata = new HashMap<>();

        validateThatScriptHandlerBindingsContain(scriptHandler,
                                                 createTaskContext(null),
                                                 new VariablesMap(),
                                                 new HashMap<String, String>(),
                                                 resultMetadata,
                                                 SchedulerConstants.RESULT_METADATA_VARIABLE,
                                                 resultMetadata);

    }

    @Test
    public void testAddBindingsToScriptHandlerContainsPreviousTaskResults()
            throws InvalidScriptException, NodeException, NoSuchFieldException, IllegalAccessException {
        // Create task result array
        TaskResultImpl taskResult = new TaskResultImpl(TaskIdImpl.createTaskId(new JobIdImpl(jobIdValue, jobNameValue),
                                                                               taskNameValue,
                                                                               taskIdValue),
                                                       new Exception("Exception"));
        TaskResult[] taskResultArray = { taskResult };

        // Create TaskContext with task result array
        TaskContext taskContext = createTaskContext(taskResultArray);

        // Expect taskResultArray to be inside the map
        validateThatScriptHandlerBindingsContain(new ScriptHandler(),
                                                 taskContext,
                                                 new VariablesMap(),
                                                 new HashMap<String, String>(),
                                                 new HashMap<String, String>(),
                                                 SchedulerConstants.RESULTS_VARIABLE,
                                                 taskResultArray);

    }

    @Test
    public void testAddBindingsToScriptHandlerContainsCredentials()
            throws InvalidScriptException, NodeException, NoSuchFieldException, IllegalAccessException {
        Map<String, String> thirdPartyCredentials = new HashMap<>();
        thirdPartyCredentials.put(testCred1Key, testCred1Value);

        // Expect taskResultArray to be inside the map
        validateThatScriptHandlerBindingsContain(new ScriptHandler(),
                                                 createTaskContext(null),
                                                 new VariablesMap(),
                                                 thirdPartyCredentials,
                                                 new HashMap<String, String>(),
                                                 SchedulerConstants.CREDENTIALS_VARIABLE,
                                                 thirdPartyCredentials);

    }

    @Test
    public void testDEFAULT()
            throws InvalidScriptException, NodeException, NoSuchFieldException, IllegalAccessException {

        // Expect taskResultArray to be inside the map
        validateThatScriptHandlerBindingsContain(new ScriptHandler(),
                                                 createTaskContext(null),
                                                 new VariablesMap(),
                                                 new HashMap<String, String>(),
                                                 new HashMap<String, String>(),
                                                 "",
                                                 null);

    }

    @Test
    public void testAddBindingsToScriptHandlerContainsForkEnvironment()
            throws InvalidScriptException, NodeException, NoSuchFieldException, IllegalAccessException {
        TaskContext taskContext = createTaskContext(null);
        ForkEnvironment forkEnvironment = new ForkEnvironment();
        taskContext.getInitializer().setForkEnvironment(forkEnvironment);

        // Expect taskResultArray to be inside the map
        validateThatScriptHandlerBindingsContain(new ScriptHandler(),
                                                 taskContext,
                                                 new VariablesMap(),
                                                 new HashMap<String, String>(),
                                                 new HashMap<String, String>(),
                                                 SchedulerConstants.FORK_ENVIRONMENT_BINDING_NAME,
                                                 forkEnvironment);

    }

    @Test
    public void testAddBindingsToScriptHandlerContainsSchedulerNodeClientVariable() throws InvalidScriptException,
            NodeException, NoSuchFieldException, IllegalAccessException, KeyException, NoSuchAlgorithmException {
        ScriptExecutableContainer scriptContainer = createScriptContainer();
        TaskLauncherInitializer taskLauncherInitializer = new TaskLauncherInitializer();
        taskLauncherInitializer.setForkEnvironment(new ForkEnvironment());
        taskLauncherInitializer.setSchedulerRestUrl("http://localhost:8080/rest");

        Decrypter decrypter = createCredentials(testUser, testPass);

        TaskContext taskContext = new TaskContext(scriptContainer,
                                                  taskLauncherInitializer,
                                                  null,
                                                  new NodeDataSpacesURIs(null, null, null, null, null, null),
                                                  null,
                                                  null,
                                                  decrypter);

        // variable should belong to the expected class
        validateThatScriptHandlerBindingsInstanceOf(new ScriptHandler(),
                                                    taskContext,
                                                    new VariablesMap(),
                                                    new HashMap<String, String>(),
                                                    new HashMap<String, String>(),
                                                    SchedulerConstants.SCHEDULER_CLIENT_BINDING_NAME,
                                                    SchedulerNodeClient.class);

    }

    @Test
    public void testAddBindingsToScriptHandlerContainsUserAndGlobalSpaceApiVariable() throws InvalidScriptException,
            NodeException, NoSuchFieldException, IllegalAccessException, KeyException, NoSuchAlgorithmException {
        ScriptExecutableContainer scriptContainer = createScriptContainer();
        TaskLauncherInitializer taskLauncherInitializer = new TaskLauncherInitializer();
        taskLauncherInitializer.setForkEnvironment(new ForkEnvironment());
        taskLauncherInitializer.setSchedulerRestUrl("http://localhost:8080/rest");

        Decrypter decrypter = createCredentials(testUser, testPass);

        TaskContext taskContext = new TaskContext(scriptContainer,
                                                  taskLauncherInitializer,
                                                  null,
                                                  new NodeDataSpacesURIs(null, null, null, null, null, null),
                                                  null,
                                                  null,
                                                  decrypter);

        // variable should belong to the expected class
        validateThatScriptHandlerBindingsInstanceOf(new ScriptHandler(),
                                                    taskContext,
                                                    new VariablesMap(),
                                                    new HashMap<String, String>(),
                                                    new HashMap<String, String>(),
                                                    SchedulerConstants.DS_USER_API_BINDING_NAME,
                                                    DataSpaceNodeClient.class);

        // variable should belong to the expected class
        validateThatScriptHandlerBindingsInstanceOf(new ScriptHandler(),
                                                    taskContext,
                                                    new VariablesMap(),
                                                    new HashMap<String, String>(),
                                                    new HashMap<String, String>(),
                                                    SchedulerConstants.DS_GLOBAL_API_BINDING_NAME,
                                                    DataSpaceNodeClient.class);
    }

    @Test
    public void testAddBindingsToScriptHandlerContainsScratchURI()
            throws InvalidScriptException, NodeException, NoSuchFieldException, IllegalAccessException {
        ScriptExecutableContainer scriptContainer = createScriptContainer();
        TaskLauncherInitializer taskLauncherInitializer = new TaskLauncherInitializer();
        taskLauncherInitializer.setForkEnvironment(new ForkEnvironment());

        TaskContext taskContext = new TaskContext(scriptContainer,
                                                  taskLauncherInitializer,
                                                  null,
                                                  new NodeDataSpacesURIs(testSetString, null, null, null, null, null),
                                                  null,
                                                  null);

        // Expect taskResultArray to be inside the map
        validateThatScriptHandlerBindingsContain(new ScriptHandler(),
                                                 taskContext,
                                                 new VariablesMap(),
                                                 new HashMap<String, String>(),
                                                 new HashMap<String, String>(),
                                                 SchedulerConstants.DS_SCRATCH_BINDING_NAME,
                                                 testSetString);
    }

    @Test
    public void testAddBindingsToScriptHandlerContainsCacheURI()
            throws InvalidScriptException, NodeException, NoSuchFieldException, IllegalAccessException {
        ScriptExecutableContainer scriptContainer = createScriptContainer();
        TaskLauncherInitializer taskLauncherInitializer = new TaskLauncherInitializer();
        taskLauncherInitializer.setForkEnvironment(new ForkEnvironment());

        TaskContext taskContext = new TaskContext(scriptContainer,
                                                  taskLauncherInitializer,
                                                  null,
                                                  new NodeDataSpacesURIs(null, testSetString, null, null, null, null),
                                                  null,
                                                  null);

        // Expect taskResultArray to be inside the map
        validateThatScriptHandlerBindingsContain(new ScriptHandler(),
                                                 taskContext,
                                                 new VariablesMap(),
                                                 new HashMap<String, String>(),
                                                 new HashMap<String, String>(),
                                                 SchedulerConstants.DS_CACHE_BINDING_NAME,
                                                 testSetString);
    }

    @Test
    public void testAddBindingsToScriptHandlerContainsInputURI()
            throws InvalidScriptException, NodeException, NoSuchFieldException, IllegalAccessException {
        ScriptExecutableContainer scriptContainer = createScriptContainer();
        TaskLauncherInitializer taskLauncherInitializer = new TaskLauncherInitializer();
        taskLauncherInitializer.setForkEnvironment(new ForkEnvironment());

        TaskContext taskContext = new TaskContext(scriptContainer,
                                                  taskLauncherInitializer,
                                                  null,
                                                  new NodeDataSpacesURIs(null, null, testSetString, null, null, null),
                                                  null,
                                                  null);

        // Expect taskResultArray to be inside the map
        validateThatScriptHandlerBindingsContain(new ScriptHandler(),
                                                 taskContext,
                                                 new VariablesMap(),
                                                 new HashMap<String, String>(),
                                                 new HashMap<String, String>(),
                                                 SchedulerConstants.DS_INPUT_BINDING_NAME,
                                                 testSetString);
    }

    @Test
    public void testAddBindingsToScriptHandlerContainsOutputURI()
            throws InvalidScriptException, NodeException, NoSuchFieldException, IllegalAccessException {
        ScriptExecutableContainer scriptContainer = createScriptContainer();
        TaskLauncherInitializer taskLauncherInitializer = new TaskLauncherInitializer();
        taskLauncherInitializer.setForkEnvironment(new ForkEnvironment());

        TaskContext taskContext = new TaskContext(scriptContainer,
                                                  taskLauncherInitializer,
                                                  null,
                                                  new NodeDataSpacesURIs(null, null, null, testSetString, null, null),
                                                  null,
                                                  null);

        // Expect taskResultArray to be inside the map
        validateThatScriptHandlerBindingsContain(new ScriptHandler(),
                                                 taskContext,
                                                 new VariablesMap(),
                                                 new HashMap<String, String>(),
                                                 new HashMap<String, String>(),
                                                 SchedulerConstants.DS_OUTPUT_BINDING_NAME,
                                                 testSetString);
    }

    @Test
    public void testAddBindingsToScriptHandlerContainsUserURI()
            throws InvalidScriptException, NodeException, NoSuchFieldException, IllegalAccessException {
        ScriptExecutableContainer scriptContainer = createScriptContainer();
        TaskLauncherInitializer taskLauncherInitializer = new TaskLauncherInitializer();
        taskLauncherInitializer.setForkEnvironment(new ForkEnvironment());

        TaskContext taskContext = new TaskContext(scriptContainer,
                                                  taskLauncherInitializer,
                                                  null,
                                                  new NodeDataSpacesURIs(null, null, null, null, testSetString, null),
                                                  null,
                                                  null);

        // Expect taskResultArray to be inside the map
        validateThatScriptHandlerBindingsContain(new ScriptHandler(),
                                                 taskContext,
                                                 new VariablesMap(),
                                                 new HashMap<String, String>(),
                                                 new HashMap<String, String>(),
                                                 SchedulerConstants.DS_USER_BINDING_NAME,
                                                 testSetString);
    }

    @Test
    public void testAddBindingsToScriptHandlerContainsGlobalURI()
            throws InvalidScriptException, NodeException, NoSuchFieldException, IllegalAccessException {
        ScriptExecutableContainer scriptContainer = createScriptContainer();
        TaskLauncherInitializer taskLauncherInitializer = new TaskLauncherInitializer();
        taskLauncherInitializer.setForkEnvironment(new ForkEnvironment());

        TaskContext taskContext = new TaskContext(scriptContainer,
                                                  taskLauncherInitializer,
                                                  null,
                                                  new NodeDataSpacesURIs(null, null, null, null, null, testSetString),
                                                  null,
                                                  null);

        // Expect taskResultArray to be inside the map
        validateThatScriptHandlerBindingsContain(new ScriptHandler(),
                                                 taskContext,
                                                 new VariablesMap(),
                                                 new HashMap<String, String>(),
                                                 new HashMap<String, String>(),
                                                 SchedulerConstants.DS_GLOBAL_BINDING_NAME,
                                                 testSetString);
    }

    @Test
    public void testExtractThirdPartyCredentials() throws Exception {
        ScriptExecutableContainer scriptContainer = createScriptContainer();
        TaskLauncherInitializer taskLauncherInitializer = new TaskLauncherInitializer();
        taskLauncherInitializer.setForkEnvironment(new ForkEnvironment());

        Decrypter decrypter = createCredentials(testUser, testPass);

        TaskContext taskContext = new TaskContext(scriptContainer,
                                                  taskLauncherInitializer,
                                                  null,
                                                  new NodeDataSpacesURIs(null, null, null, null, null, null),
                                                  null,
                                                  null,
                                                  decrypter);

        ForkedTaskVariablesManager forkedTaskVariablesManager = new ForkedTaskVariablesManager();
        Map<String, String> creds = forkedTaskVariablesManager.extractThirdPartyCredentials(taskContext);

        assertThat(creds.get(thirdPartyCred1Key), is(thirdPartyCred1Value));
    }

    @Test
    public void testScriptParametersAreReplaced() throws InvalidScriptException {
        ForkedTaskVariablesManager forkedTaskVariablesManager = new ForkedTaskVariablesManager();

        // Create a variable $[something] inside a python script
        Serializable[] parameters = new Serializable[] { "$" + testVariable1Key };
        Script script = new SimpleScript("print 'hello'", "python", parameters);

        // Create a hash map with key as varialbe name and value as variable value.
        Map<String, Serializable> variables = new HashMap<>();
        variables.put(testVariable1Key, testVariable1Value);

        VariablesMap variablesMap = new VariablesMap();
        variablesMap.setInheritedMap(variables);

        // Replace that variable inside the script parameters with its value in the hash map
        forkedTaskVariablesManager.replaceScriptParameters(script,
                                                           new HashMap<String, String>(),
                                                           variablesMap,
                                                           System.out);

        assertThat((String) parameters[0], is(testVariable1Value));
    }

    @Test
    public void testScriptCredentialsAreReplaced() throws InvalidScriptException {
        ForkedTaskVariablesManager forkedTaskVariablesManager = new ForkedTaskVariablesManager();

        // Add $credential_[something] variable to new python script
        Serializable[] parameters = new Serializable[] { "$" + ForkedTaskVariablesManager.CREDENTIALS_KEY_PREFIX +
                                                         testVariable1Key };
        Script script = new SimpleScript("print 'hello'", "python", parameters);

        // Create credentials
        Map<String, String> credentials = new HashMap<>();
        credentials.put(testVariable1Key, testVariable1Value);

        // Replace the credentials inside the script parameters
        forkedTaskVariablesManager.replaceScriptParameters(script, credentials, new VariablesMap(), System.out);

        assertThat((String) parameters[0], is(testVariable1Value));
    }

    @Test(expected = Exception.class)
    public void testExtractThirdPartyCredentialsThrowsExceptionIfTaskLauncherInitializerIsNull() throws Exception {
        ScriptExecutableContainer scriptContainer = createScriptContainer();

        TaskContext taskContext = new TaskContext(scriptContainer,
                                                  null,
                                                  null,
                                                  new NodeDataSpacesURIs(null, null, null, null, null, null),
                                                  null,
                                                  null);

        ForkedTaskVariablesManager forkedTaskVariablesManager = new ForkedTaskVariablesManager();
        forkedTaskVariablesManager.extractThirdPartyCredentials(taskContext);
    }

    private <T> void validateThatScriptHandlerBindingsContain(ScriptHandler scriptHandler, TaskContext taskContext,
            VariablesMap variables, Map<String, String> credentials, Map<String, String> resultMetadata, String key,
            T entry) throws NoSuchFieldException, IllegalAccessException, InvalidScriptException, NodeException {
        Map<String, Object> scriptHandlerBindings = initializeForkedTaskVariableManager(scriptHandler,
                                                                                        taskContext,
                                                                                        variables,
                                                                                        credentials,
                                                                                        resultMetadata);

        // Check if element exists
        assertThat((T) scriptHandlerBindings.get(key), is(entry));
    }

    private <T> void validateThatScriptHandlerBindingsInstanceOf(ScriptHandler scriptHandler, TaskContext taskContext,
            VariablesMap variables, Map<String, String> credentials, Map<String, String> resultMetadata, String key,
            Class clazz) throws NoSuchFieldException, IllegalAccessException, InvalidScriptException, NodeException {
        Map<String, Object> scriptHandlerBindings = initializeForkedTaskVariableManager(scriptHandler,
                                                                                        taskContext,
                                                                                        variables,
                                                                                        credentials,
                                                                                        resultMetadata);

        // Check if element exists
        assertThat((T) scriptHandlerBindings.get(key), isA(clazz));
    }

    private Map<String, Object> initializeForkedTaskVariableManager(ScriptHandler scriptHandler,
            TaskContext taskContext, VariablesMap variables, Map<String, String> credentials,
            Map<String, String> resultMetadata) throws IllegalAccessException, NoSuchFieldException {
        // Create class
        ForkedTaskVariablesManager forkedTaskVariablesManager = new ForkedTaskVariablesManager();

        // Replace additionalBindings to hold a reference to it
        Map<String, Object> scriptHandlerBindings = new HashMap<>();
        setPrivateField(ScriptHandler.class.getDeclaredField("additionalBindings"),
                        scriptHandler,
                        scriptHandlerBindings);

        SchedulerNodeClient schedulerNodeClient = forkedTaskVariablesManager.createSchedulerNodeClient(taskContext);

        // Execute method which adds bindings
        forkedTaskVariablesManager.addBindingsToScriptHandler(scriptHandler,
                                                              taskContext,
                                                              variables,
                                                              credentials,
                                                              schedulerNodeClient,
                                                              forkedTaskVariablesManager.createDataSpaceNodeClient(taskContext,
                                                                                                                   schedulerNodeClient,
                                                                                                                   IDataSpaceClient.Dataspace.USER),
                                                              forkedTaskVariablesManager.createDataSpaceNodeClient(taskContext,
                                                                                                                   schedulerNodeClient,
                                                                                                                   IDataSpaceClient.Dataspace.GLOBAL),
                                                              resultMetadata);
        return scriptHandlerBindings;
    }

    private TaskContext createTaskContext(TaskResult[] previousTasksResults)
            throws InvalidScriptException, NodeException {
        ScriptExecutableContainer scriptContainer = createScriptContainer();
        TaskLauncherInitializer taskLauncherInitializer = new TaskLauncherInitializer();
        taskLauncherInitializer.setForkEnvironment(new ForkEnvironment());

        TaskContext taskContext = new TaskContext(scriptContainer,
                                                  taskLauncherInitializer,
                                                  previousTasksResults,
                                                  new NodeDataSpacesURIs(null, null, null, null, null, null),
                                                  null,
                                                  null);
        return taskContext;
    }

    private ScriptExecutableContainer createScriptContainer() throws InvalidScriptException {
        return new ScriptExecutableContainer(new TaskScript(new SimpleScript("print('hello'); result='hello'",
                                                                             "javascript")));
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

    private Decrypter createCredentials(String username, String password)
            throws NoSuchAlgorithmException, KeyException {
        Map<String, String> thirdPartyCreds = new HashMap<>();
        thirdPartyCreds.put(thirdPartyCred1Key, thirdPartyCred1Value);
        CredData credData = new CredData(username, password, thirdPartyCreds);
        KeyPairGenerator keyGen;
        keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(512, new SecureRandom());
        KeyPair keyPair = keyGen.generateKeyPair();
        Decrypter decrypter = new Decrypter(keyPair.getPrivate());
        Credentials credentials = Credentials.createCredentials(credData, keyPair.getPublic());
        decrypter.setCredentials(credentials);
        return decrypter;
    }

}
