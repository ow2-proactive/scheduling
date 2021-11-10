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
package org.ow2.proactive.scheduler.signal;

import java.io.File;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobVariable;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.synchronization.AOSynchronization;
import org.ow2.proactive.scheduler.synchronization.InvalidChannelException;
import org.ow2.proactive.scheduler.synchronization.SynchronizationInternal;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.tests.ProActiveTestClean;

import com.jayway.awaitility.Awaitility;


public class SignalApiTest extends ProActiveTestClean {

    private static final String USER = "user";

    private static final JobId JOB_ID = new JobIdImpl(0, "JobWithSignals");

    private static final TaskId TASK_ID = TaskIdImpl.createTaskId(JOB_ID, "Task", 0);

    private static final String SIGNALS_CHANNEL = PASchedulerProperties.SCHEDULER_SIGNALS_CHANNEL.getValueAsString();

    private static SynchronizationInternal synchronizationInternal;

    private static SignalApi signalApi;

    public static final String READY_PREFIX = "ready_";

    @ClassRule
    public static TemporaryFolder folder = new TemporaryFolder();

    private static File tempFolder;

    private static ExecutorService executor;

    @BeforeClass
    public static void classInit()
            throws IOException, ActiveObjectCreationException, NodeException, AlreadyBoundException {
        CentralPAPropertyRepository.PA_CLASSLOADING_USEHTTP.setValue(false);
        if (System.getProperty("log4j.configuration") == null) {
            // While logger is not configured and it not set with sys properties, use Console logger
            Logger.getRootLogger().getLoggerRepository().resetConfiguration();
            BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%m%n")));
            Logger.getRootLogger().setLevel(Level.DEBUG);
        }
        Logger.getLogger(SignalApiImpl.class).setLevel(Level.TRACE);

        tempFolder = folder.newFolder("signal");

        Node localNode = ProActiveRuntimeImpl.getProActiveRuntime().createLocalNode("signal-node-0",
                                                                                    true,
                                                                                    "signal-v-node-0");

        synchronizationInternal = PAActiveObject.newActive(AOSynchronization.class,
                                                           new Object[] { tempFolder.getAbsolutePath() },
                                                           localNode);
        synchronizationInternal.createChannelIfAbsent(USER, TASK_ID, SIGNALS_CHANNEL, true);
        signalApi = new SignalApiImpl(USER, TASK_ID, synchronizationInternal);
        executor = Executors.newFixedThreadPool(2);
    }

    @Before
    public void clearJobSignals() throws SignalApiException {
        signalApi.clearJobSignals();
    }

    @AfterClass
    public static void cleanUp() {
        executor.shutdownNow();
        PAActiveObject.terminateActiveObject(synchronizationInternal, true);
        tempFolder.delete();
    }

    @Test
    public void testReadyForSignal() throws SignalApiException, InvalidChannelException {
        String signalName = "test_signal_1";
        signalApi.readyForSignal(signalName);

        Assert.assertTrue(((Signal) synchronizationInternal.get(USER,
                                                                TASK_ID,
                                                                SIGNALS_CHANNEL + JOB_ID.value(),
                                                                READY_PREFIX + signalName)).getName()
                                                                                           .equals(signalName));
        Assert.assertNull((synchronizationInternal.get(USER, TASK_ID, SIGNALS_CHANNEL + JOB_ID.value(), signalName)));
    }

    @Test
    public void testReadyForSignalWithVariables() throws SignalApiException, InvalidChannelException {
        String signalName = "test_signal_1";
        JobVariable jobVariable1 = new JobVariable("name1", "value1");
        JobVariable jobVariable2 = new JobVariable("name2", "value2");
        List<JobVariable> jobVariables = new ArrayList<>();
        jobVariables.add(jobVariable1);
        jobVariables.add(jobVariable2);
        signalApi.readyForSignal(signalName, jobVariables);

        Assert.assertTrue(((Signal) synchronizationInternal.get(USER,
                                                                TASK_ID,
                                                                SIGNALS_CHANNEL + JOB_ID.value(),
                                                                READY_PREFIX + signalName)).getName()
                                                                                           .equals(signalName));
        Assert.assertNotNull((synchronizationInternal.get(USER,
                                                          TASK_ID,
                                                          SIGNALS_CHANNEL + JOB_ID.value(),
                                                          READY_PREFIX + signalName)));
        Assert.assertEquals(jobVariables, ((Signal) synchronizationInternal.get(USER,
                                                                                TASK_ID,
                                                                                SIGNALS_CHANNEL + JOB_ID.value(),
                                                                                READY_PREFIX + signalName)).getInputVariables());
    }

    @Test(expected = SignalApiException.class)
    public void testReadyForSignalWithParamsException() throws SignalApiException {
        String signal = " ";

        // Send ready for an empty signal then assert a SignalAPIException is thrown
        signalApi.readyForSignal(signal, new ArrayList<>());
    }

    @Test(expected = SignalApiException.class)
    public void testReadyForSignalWithException() throws SignalApiException {
        String signal = " ";

        // Send ready for an empty signal then assert a SignalAPIException is thrown
        signalApi.readyForSignal(signal);
    }

    @Test
    public void testIsReceived() throws SignalApiException {
        String signal = "test_signal_2";
        signalApi.sendSignal(signal);
        Assert.assertTrue(signalApi.isReceived(signal));
    }

    @Test
    public void testCheckForSignals() throws SignalApiException {
        Set<String> signalsToBeChecked = new HashSet<String>() {
            {
                add("test_signal_3_1");
                add("test_signal_3_2");
            }
        };
        signalApi.sendManySignals(signalsToBeChecked);
        Signal receivedSignal = signalApi.checkForSignals(signalsToBeChecked);
        Assert.assertTrue((receivedSignal.getName().equals("test_signal_3_1")) ||
                          receivedSignal.getName().equals("test_signal_3_2"));
    }

    @Test
    public void testSendSignal() throws InvalidChannelException, SignalApiException {
        String signalName = "test_signal_4";

        // Send the signal then check that its associated ready signal is removed
        signalApi.sendSignal(signalName);

        Assert.assertNotNull(synchronizationInternal.get(USER, TASK_ID, SIGNALS_CHANNEL + JOB_ID.value(), signalName));
    }

    @Test
    public void testSendSignalWithParams() throws InvalidChannelException, SignalApiException {
        String signalName = "test_signal_5";
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("param1", "value1");
        parameters.put("param2", "value2");

        // Send the signal then check that its associated ready signal is removed
        signalApi.sendSignal(signalName, parameters);

        Assert.assertNotNull((synchronizationInternal.get(USER,
                                                          TASK_ID,
                                                          SIGNALS_CHANNEL + JOB_ID.value(),
                                                          signalName)));

        Assert.assertEquals(parameters, ((Signal) synchronizationInternal.get(USER,
                                                                              TASK_ID,
                                                                              SIGNALS_CHANNEL + JOB_ID.value(),
                                                                              signalName)).getOutputValues());

        Assert.assertNotNull(synchronizationInternal.get(USER, TASK_ID, SIGNALS_CHANNEL + JOB_ID.value(), signalName));
    }

    @Test(expected = SignalApiException.class)
    public void testSendSignalWithParamsException() throws SignalApiException {
        String signal = " ";

        // Send an empty signal then assert a SignalAPIException is thrown
        signalApi.sendSignal(signal, new HashMap<>());
    }

    @Test(expected = SignalApiException.class)
    public void testSendSignalWithException() throws SignalApiException {
        String signal = " ";

        // Send an empty signal then assert a SignalAPIException is thrown
        signalApi.sendSignal(signal);
    }

    @Test
    public void testSendManySignals() throws InvalidChannelException, SignalApiException {
        String signalName1 = "test_signal_5_1";
        String signalName2 = "test_signal_5_2";
        Set<String> signalsToBeSent = new HashSet<String>() {
            {
                add(signalName1);
                add(signalName2);
            }
        };

        // Send signals twice and check that each signal is added only once, and the signals are received
        signalApi.sendManySignals(signalsToBeSent);
        signalApi.sendManySignals(signalsToBeSent);

        Signal signal1 = (Signal) synchronizationInternal.get(USER,
                                                              TASK_ID,
                                                              SIGNALS_CHANNEL + JOB_ID.value(),
                                                              signalName1);
        Signal signal2 = (Signal) synchronizationInternal.get(USER,
                                                              TASK_ID,
                                                              SIGNALS_CHANNEL + JOB_ID.value(),
                                                              signalName2);
        Assert.assertNotNull(signal1);
        Assert.assertNotNull(signal2);
        Assert.assertTrue(signalApi.getJobSignals().size() == 2);
    }

    @Test
    public void testSendManySignalsWithParams() throws InvalidChannelException, SignalApiException {
        String signalName1 = "test_signal_5_1";
        String signalName2 = "test_signal_5_2";
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("param1", "value1");
        parameters.put("param2", "value2");
        Map<String, Map<String, String>> signalsWithParams = new LinkedHashMap<>();
        signalsWithParams.put(signalName1, parameters);
        signalsWithParams.put(signalName2, parameters);

        // Send signals twice and check that each signal is added only once, and the signals are received
        signalApi.sendManySignals(signalsWithParams);
        signalApi.sendManySignals(signalsWithParams);

        Signal signal1 = (Signal) synchronizationInternal.get(USER,
                                                              TASK_ID,
                                                              SIGNALS_CHANNEL + JOB_ID.value(),
                                                              signalName1);
        Signal signal2 = (Signal) synchronizationInternal.get(USER,
                                                              TASK_ID,
                                                              SIGNALS_CHANNEL + JOB_ID.value(),
                                                              signalName2);
        Assert.assertNotNull(signal1);
        Assert.assertEquals(parameters, signal1.getOutputValues());
        Assert.assertNotNull(signal2);
        Assert.assertEquals(parameters, signal2.getOutputValues());
        Assert.assertTrue(signalApi.getJobSignals().size() == 2);
    }

    @Test(expected = SignalApiException.class)
    public void testSendManySignalsWithParamsException() throws SignalApiException {
        Map<String, Map<String, String>> signalsWithParams = new LinkedHashMap<>();
        signalsWithParams.put("", new LinkedHashMap<>());
        signalsWithParams.put("", new LinkedHashMap<>());

        // Send empty signals then assert a SignalAPIException is thrown
        signalApi.sendManySignals(signalsWithParams);
    }

    @Test
    public void testRemoveManySignalsWithSignals() throws InvalidChannelException, SignalApiException {
        String signalName1 = "test_signal_7_3";
        String signalName2 = "test_signal_7_4";
        Set<String> signalsToBeRemoved = new HashSet<>();
        signalsToBeRemoved.add(signalName1);
        signalsToBeRemoved.add(signalName2);
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("param1", "value1");
        parameters.put("param2", "value2");
        Map<String, Map<String, String>> signalsWithParams = new LinkedHashMap<>();
        signalsWithParams.put(signalName1, parameters);
        signalsWithParams.put(signalName2, parameters);

        signalApi.sendManySignals(signalsWithParams);
        signalApi.removeManySignals(signalsToBeRemoved);

        Assert.assertNull(synchronizationInternal.get(USER,
                                                      TASK_ID,
                                                      SIGNALS_CHANNEL + JOB_ID.value(),
                                                      signalsToBeRemoved.toArray()[0].toString()));
        Assert.assertNull(synchronizationInternal.get(USER,
                                                      TASK_ID,
                                                      SIGNALS_CHANNEL + JOB_ID.value(),
                                                      signalsToBeRemoved.toArray()[1].toString()));
    }

    @Test(expected = SignalApiException.class)
    public void testSendManySignalsWithException() throws SignalApiException {
        Set<String> signalsToBeSent = new HashSet<String>() {
            {
                add("");
            }
        };

        // Send empty signals then assert a SignalAPIException is thrown
        signalApi.sendManySignals(signalsToBeSent);
    }

    @Test
    public void testRemoveSignal() throws InvalidChannelException, SignalApiException {
        String signalName = "test_signal_6";
        signalApi.sendSignal(signalName);
        signalApi.removeSignal(signalName);
        Assert.assertNull((synchronizationInternal.get(USER, TASK_ID, SIGNALS_CHANNEL + JOB_ID.value(), signalName)));
    }

    @Test
    public void testRemoveManySignals() throws InvalidChannelException, SignalApiException {
        Set<String> signalsToBeRemoved = new HashSet<String>() {
            {
                add("test_signal_7_1");
                add("test_signal_7_2");
            }
        };
        signalApi.sendManySignals(signalsToBeRemoved);
        signalApi.removeManySignals(signalsToBeRemoved);

        Assert.assertNull(synchronizationInternal.get(USER,
                                                      TASK_ID,
                                                      SIGNALS_CHANNEL + JOB_ID.value(),
                                                      signalsToBeRemoved.toArray()[0].toString()));
        Assert.assertNull(synchronizationInternal.get(USER,
                                                      TASK_ID,
                                                      SIGNALS_CHANNEL + JOB_ID.value(),
                                                      signalsToBeRemoved.toArray()[1].toString()));
    }

    @Test
    public void testGetJobSignals() throws SignalApiException {
        String signalName = "test_signal_8";
        signalApi.sendSignal(signalName);
        Assert.assertFalse(signalApi.getJobSignals().isEmpty());
    }

    @Test
    public void testClearJobSignals() throws InvalidChannelException, SignalApiException {
        signalApi.clearJobSignals();
        Assert.assertFalse(synchronizationInternal.containsKey(USER, TASK_ID, SIGNALS_CHANNEL, JOB_ID.value()));
    }

    @Test
    public void testWaitFor() throws SignalApiException {
        String signal = "test_signal_9";
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("param1", "value1");
        parameters.put("param2", "value2");
        long durationInMillis = 1000;

        //Define a thread that waits for the signal reception
        Runnable waitForSignalThread = () -> {
            try {
                Map<String, String> outputValues = signalApi.waitFor(signal);
                Assert.assertEquals(parameters, outputValues);
            } catch (Exception e) {
            }
        };

        executor.submit(sendSignalRunnable(signal, parameters, durationInMillis));
        Awaitility.await().atMost(3 * durationInMillis, TimeUnit.MILLISECONDS).until(waitForSignalThread);
        Assert.assertTrue(signalApi.isReceived(signal));
    }

    @Test
    public void testWaitForAny() throws SignalApiException {
        String signal_1 = "test_signal_10_1";
        String signal_2 = "test_signal_10_2";
        Set<String> signals = new HashSet<String>() {
            {
                add(signal_1);
                add(signal_2);
            }
        };
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("param1", "value1");
        parameters.put("param2", "value2");
        long durationInMillis_1 = 100;
        long durationInMillis_2 = 400;

        //Define a thread that waits for the signal reception
        Runnable waitForAnySignalThread = () -> {
            try {
                signalApi.waitForAny(signals);
            } catch (Exception e) {
            }
        };

        executor.submit(sendSignalRunnable(signal_1, parameters, durationInMillis_1));
        executor.submit(sendSignalRunnable(signal_2, parameters, durationInMillis_2));
        Awaitility.await().atMost(4 * durationInMillis_1, TimeUnit.MILLISECONDS).until(waitForAnySignalThread);
        Assert.assertTrue(signalApi.isReceived(signal_1));
        Assert.assertFalse(signalApi.isReceived(signal_2));
    }

    @Test
    public void testWaitForAll() throws SignalApiException {
        String signal_1 = "test_signal_11_1";
        String signal_2 = "test_signal_11_2";
        Set<String> signalNames = new HashSet<String>() {
            {
                add(signal_1);
                add(signal_2);
            }
        };
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("param1", "value1");
        parameters.put("param2", "value2");
        long durationInMillis = 0;

        //Define a thread that waits for signals' reception
        Runnable waitForAllSignalThread = () -> {
            try {
                Map<String, Map<String, String>> signals = signalApi.waitForAll(signalNames);
                Assert.assertEquals(signalNames, signals.keySet());
                signals.entrySet()
                       .stream()
                       .parallel()
                       .forEach(signalPair -> Assert.assertEquals(parameters, signalPair.getValue()));
            } catch (Exception e) {
            }
        };

        executor.submit(sendSignalRunnable(signal_1, parameters, durationInMillis));
        executor.submit(sendSignalRunnable(signal_2, parameters, durationInMillis));

        Awaitility.await().atMost(3000, TimeUnit.MILLISECONDS).until(waitForAllSignalThread);

        Assert.assertTrue(signalApi.isReceived(signal_1) && signalApi.isReceived(signal_2));
    }

    private Runnable sendSignalRunnable(String signal, Map<String, String> parameters, long duration) {
        Runnable waitForSignalThread = () -> {
            try {
                TimeUnit.MILLISECONDS.sleep(duration);
                signalApi.sendSignal(signal, parameters);
            } catch (SignalApiException | InterruptedException e) {
                e.getMessage();
            }
        };
        return waitForSignalThread;
    }
}
