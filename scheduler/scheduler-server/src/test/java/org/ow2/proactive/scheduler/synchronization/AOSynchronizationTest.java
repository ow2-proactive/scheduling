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
package org.ow2.proactive.scheduler.synchronization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.TaskIdImpl;
import org.ow2.tests.ProActiveTestClean;


/**
 * @author ActiveEon Team
 * @since 05/04/2018
 */
public class AOSynchronizationTest extends ProActiveTestClean {

    private static final String CHANNEL1 = "Channel1";

    private static final String CHANNEL2 = "Channel2";

    private static final String USER = "user";

    private static final TaskId TASK_ID = TaskIdImpl.createTaskId(new JobIdImpl(0, "Job"), "Task", 0);

    public static final String PREDICATE_WITH_RUNTIME_ERROR = "{k, x -> 1 / 0}";

    public static final String PREDICATE_WITH_COMPILATION_ERROR = "{k, x -> ?}";

    public static final String BIFUNCTION_WITH_COMPILATION_ERROR = "{k, x -> a +-= 1}";

    public static final String BIFUNCTION_WITH_RUNTIME_ERROR = "{k, x -> 1/0}";

    public static final String FUNCTION_WITH_RUNTIME_ERROR = "{k -> 1/0}";

    public static final String FUNCTION_WITH_COMPILATION_ERROR = "{k -> a +-= 1}";

    public static final String CONSUMER_WITH_COMPILATION_ERROR = "{x -> a +-= 1}";

    public static final String CONSUMER_WITH_RUNTIME_ERROR = "{x -> 1/0}";

    public static final String BIFUNCTION_INCREMENT_ONE = "{k, x -> x + 1}";

    public static final String PREDICATE_GT_ZERO = "{k, x -> x > 0}";

    public static final String PREDICATE_LT_ZERO = "{k, x -> x < 0}";

    public static final String BIFUNCTION_DECREMENT_ONE = "{k, x -> x - 1}";

    public static final String PREDICATE_GTE_ZERO = "{k, x -> x >= 0}";

    public static final String PREDICATE_EQ_ZERO = "{k, x -> x == 0}";

    public static final String FUNCTION_INCREMENT_ONE = "{k -> x + 1}";

    public static final String FUNCTION_RETURN_SEVEN = "{k -> 7}";

    private AOSynchronization synchronizationInternal;

    private Synchronization synchronization;

    private ExecutorService executor;

    @ClassRule
    public static TemporaryFolder folder = new TemporaryFolder();

    private File tempFolder;

    @BeforeClass
    public static void classInit() {
        CentralPAPropertyRepository.PA_CLASSLOADING_USEHTTP.setValue(false);
        if (System.getProperty("log4j.configuration") == null) {
            // While logger is not configured and it not set with sys properties, use Console logger
            Logger.getRootLogger().getLoggerRepository().resetConfiguration();
            BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%m%n")));
            Logger.getRootLogger().setLevel(Level.INFO);
        }
        Logger.getLogger(AOSynchronization.class).setLevel(Level.TRACE);
    }

    @Before
    public void init() throws IOException, ActiveObjectCreationException, NodeException {
        tempFolder = folder.newFolder();
        initSynchronizationAPI(tempFolder);

        executor = Executors.newFixedThreadPool(2);
    }

    private void initSynchronizationAPI(File tempFolder) throws ActiveObjectCreationException, NodeException {
        synchronizationInternal = PAActiveObject.newActive(AOSynchronization.class,
                                                           new Object[] { tempFolder.getAbsolutePath() });
        synchronization = new SynchronizationWrapper(USER, TASK_ID, synchronizationInternal);
    }

    @After
    public void cleanUp() {
        executor.shutdownNow();
        PAActiveObject.terminateActiveObject(synchronizationInternal, true);
    }

    private void initChannel(String channel) throws IOException, InvalidChannelException {
        synchronization.createChannelIfAbsent(channel, false);
        Map<String, Integer> map = new HashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        synchronization.putAll(channel, map);
    }

    private void initChannel() throws IOException, InvalidChannelException {
        initChannel(CHANNEL1);
    }

    @Test
    public void testChannelCreationDeletion() throws IOException, InvalidChannelException {
        synchronization.createChannel(CHANNEL1, false);
        Assert.assertEquals(0, synchronization.size(CHANNEL1));
        synchronization.deleteChannel(CHANNEL1);
        try {
            synchronization.size(CHANNEL1);
            fail("Channel should have been deleted");
        } catch (InvalidChannelException e) {
            // ok
        }
    }

    @Test
    public void testCreateChannelIfAbsent() throws IOException, InvalidChannelException {
        synchronization.createChannelIfAbsent(CHANNEL1, false);
        Assert.assertEquals(0, synchronization.size(CHANNEL1));
        Assert.assertNull(synchronization.put(CHANNEL1, "key1", "value1"));
        synchronization.createChannelIfAbsent(CHANNEL1, false);
        Assert.assertEquals("value1", synchronization.get(CHANNEL1, "key1"));
    }

    @Test
    public void testPersistentChannel()
            throws IOException, InvalidChannelException, ActiveObjectCreationException, NodeException {
        synchronization.createChannelIfAbsent(CHANNEL2, true);
        initChannel(CHANNEL2);
        // Synchronously close the database, then kill the active object
        // This is to prevent the database close to be delayed by the asynchronous endActivity execution
        synchronizationInternal.close();
        PAActiveObject.terminateActiveObject(synchronizationInternal, true);

        // Recreate the synchronization object (reloads the databse)
        initSynchronizationAPI(tempFolder);
        Assert.assertEquals(1, (int) synchronization.get(CHANNEL2, "a"));
        Assert.assertEquals(2, (int) synchronization.get(CHANNEL2, "b"));
    }

    @Test
    public void testForEach() throws IOException, InvalidChannelException, CompilationException {
        initChannel();
        try {
            synchronization.forEach(CHANNEL1, CONSUMER_WITH_COMPILATION_ERROR);
            fail("CompilationException should be thrown");
        } catch (CompilationException e) {
            // expected
        }

        try {
            synchronization.forEach(CHANNEL1, CONSUMER_WITH_RUNTIME_ERROR);
            fail("ClosureEvaluationException should be thrown");
        } catch (ClosureEvaluationException e) {
            // expected
        }
    }

    @Test
    public void testReplaceAll() throws IOException, InvalidChannelException, CompilationException {
        initChannel();
        try {
            synchronization.replaceAll(CHANNEL1, BIFUNCTION_WITH_COMPILATION_ERROR);
            fail("CompilationException should be thrown");
        } catch (CompilationException e) {
            // expected
        }

        try {
            synchronization.replaceAll(CHANNEL1, BIFUNCTION_WITH_RUNTIME_ERROR);
            fail("ClosureEvaluationException should be thrown");
        } catch (ClosureEvaluationException e) {
            // expected
        }

        synchronization.replaceAll(CHANNEL1, BIFUNCTION_INCREMENT_ONE);
        Assert.assertEquals(2, (int) synchronization.get(CHANNEL1, "a"));
        Assert.assertEquals(3, (int) synchronization.get(CHANNEL1, "b"));
    }

    @Test
    public void testComputeIfAbsent() throws IOException, InvalidChannelException, CompilationException {
        initChannel();
        try {
            synchronization.computeIfAbsent(CHANNEL1, "c", FUNCTION_WITH_COMPILATION_ERROR);
            fail("CompilationException should be thrown");
        } catch (CompilationException e) {
            // expected
        }

        try {
            synchronization.computeIfAbsent(CHANNEL1, "c", FUNCTION_WITH_RUNTIME_ERROR);
            fail("ClosureEvaluationException should be thrown");
        } catch (ClosureEvaluationException e) {
            // expected
        }

        synchronization.computeIfAbsent(CHANNEL1, "a", FUNCTION_INCREMENT_ONE);
        Assert.assertEquals(1, (int) synchronization.get(CHANNEL1, "a"));
        synchronization.computeIfAbsent(CHANNEL1, "c", FUNCTION_RETURN_SEVEN);
        Assert.assertEquals(7, (int) synchronization.get(CHANNEL1, "c"));
    }

    @Test
    public void testComputeIfPresent() throws IOException, InvalidChannelException, CompilationException {
        initChannel();
        try {
            synchronization.computeIfPresent(CHANNEL1, "a", BIFUNCTION_WITH_COMPILATION_ERROR);
            fail("CompilationException should be thrown");
        } catch (CompilationException e) {
            // expected
        }

        try {
            synchronization.computeIfPresent(CHANNEL1, "a", BIFUNCTION_WITH_RUNTIME_ERROR);
            fail("ClosureEvaluationException should be thrown");
        } catch (ClosureEvaluationException e) {
            // expected
        }

        synchronization.computeIfPresent(CHANNEL1, "c", BIFUNCTION_INCREMENT_ONE);
        Assert.assertNull(synchronization.get(CHANNEL1, "c"));
        synchronization.computeIfPresent(CHANNEL1, "a", BIFUNCTION_INCREMENT_ONE);
        Assert.assertEquals(2, (int) synchronization.get(CHANNEL1, "a"));
    }

    @Test
    public void testCompute() throws IOException, InvalidChannelException, CompilationException {
        initChannel();
        try {
            synchronization.compute(CHANNEL1, "a", BIFUNCTION_WITH_COMPILATION_ERROR);
            fail("CompilationException should be thrown");
        } catch (CompilationException e) {
            // expected
        }

        try {
            synchronization.compute(CHANNEL1, "a", BIFUNCTION_WITH_RUNTIME_ERROR);
            fail("ClosureEvaluationException should be thrown");
        } catch (ClosureEvaluationException e) {
            // expected
        }

        try {
            synchronization.compute(CHANNEL1, "c", BIFUNCTION_INCREMENT_ONE);
            fail("ClosureEvaluationException should be thrown (x will be null)");
        } catch (ClosureEvaluationException e) {
            // expected
        }
        Assert.assertEquals(2, (int) synchronization.compute(CHANNEL1, "a", BIFUNCTION_INCREMENT_ONE));
        Assert.assertEquals(2, (int) synchronization.get(CHANNEL1, "a"));
    }

    @Test
    public void testConditionalCompute() throws IOException, InvalidChannelException, CompilationException {
        initChannel();
        try {
            synchronization.conditionalCompute(CHANNEL1,
                                               "a",
                                               PREDICATE_WITH_COMPILATION_ERROR,
                                               BIFUNCTION_WITH_COMPILATION_ERROR);
            fail("CompilationException should be thrown");
        } catch (CompilationException e) {
            // expected
        }

        try {
            synchronization.conditionalCompute(CHANNEL1,
                                               "a",
                                               PREDICATE_WITH_RUNTIME_ERROR,
                                               BIFUNCTION_WITH_COMPILATION_ERROR);
            fail("ClosureEvaluationException should be thrown");
        } catch (ClosureEvaluationException e) {
            // expected
        }

        // the following should do a <- (a + 1) - 1
        Assert.assertEquals(new PredicateActionResult(true, 2),
                            synchronization.conditionalCompute(CHANNEL1,
                                                               "a",
                                                               PREDICATE_GT_ZERO,
                                                               BIFUNCTION_INCREMENT_ONE));
        Assert.assertEquals(new PredicateActionResult(false, 1), synchronization.conditionalCompute(CHANNEL1,
                                                                                                    "a",
                                                                                                    PREDICATE_LT_ZERO,
                                                                                                    BIFUNCTION_INCREMENT_ONE,
                                                                                                    BIFUNCTION_DECREMENT_ONE));
        Assert.assertEquals(1, (int) synchronization.get(CHANNEL1, "a"));
    }

    @Test
    public void testConditionalReplaceAll() throws IOException, InvalidChannelException, CompilationException {
        synchronization.createChannelIfAbsent(CHANNEL1, false);
        Map<String, Integer> map = new HashMap<>();
        map.put("a", -1);
        map.put("b", 1);
        map.put("c", -2);
        map.put("d", 2);
        synchronization.putAll(CHANNEL1, map);
        try {
            synchronization.conditionalReplaceAll(CHANNEL1,
                                                  PREDICATE_WITH_COMPILATION_ERROR,
                                                  PREDICATE_WITH_COMPILATION_ERROR,
                                                  BIFUNCTION_WITH_COMPILATION_ERROR);
            fail("CompilationException should be thrown");
        } catch (CompilationException e) {
            // expected
        }

        try {
            synchronization.conditionalReplaceAll(CHANNEL1,
                                                  PREDICATE_WITH_RUNTIME_ERROR,
                                                  BIFUNCTION_WITH_COMPILATION_ERROR);
            fail("ClosureEvaluationException should be thrown");
        } catch (ClosureEvaluationException e) {
            // expected
        }

        // the following increments one on all positive entries, decrement one on all negative
        synchronization.conditionalReplaceAll(CHANNEL1,
                                              PREDICATE_GTE_ZERO,
                                              BIFUNCTION_INCREMENT_ONE,
                                              BIFUNCTION_DECREMENT_ONE);
        Assert.assertEquals(-2, (int) synchronization.get(CHANNEL1, "a"));
        Assert.assertEquals(2, (int) synchronization.get(CHANNEL1, "b"));
        Assert.assertEquals(-3, (int) synchronization.get(CHANNEL1, "c"));
        Assert.assertEquals(3, (int) synchronization.get(CHANNEL1, "d"));
    }

    @Test
    public void testWaitUntil() throws IOException, InvalidChannelException, CompilationException {
        initChannel();
        try {
            synchronization.waitUntil(CHANNEL1, "a", PREDICATE_WITH_COMPILATION_ERROR);
            fail("CompilationException should be thrown");
        } catch (CompilationException e) {
            // expected
        }

        try {
            synchronization.waitUntil(CHANNEL1, "a", PREDICATE_WITH_RUNTIME_ERROR);
            fail("ClosureEvaluationException should be thrown");
        } catch (ClosureEvaluationException e) {
            // expected
        }

        Runnable decrementRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    synchronization.compute(CHANNEL1, "a", BIFUNCTION_DECREMENT_ONE);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };

        // this is a producer/consumer pattern
        // the producer will decrement the counter, consumer will wait until the counter reach zero
        executor.submit(decrementRunnable);
        Assert.assertTrue(synchronization.waitUntil(CHANNEL1, "a", PREDICATE_EQ_ZERO));
        Assert.assertEquals(0, (int) synchronization.get(CHANNEL1, "a"));
    }

    @Test
    public void testWaitUntilWithTimeout() throws IOException, InvalidChannelException, CompilationException {
        initChannel();

        Runnable decrementRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    synchronization.compute(CHANNEL1, "a", BIFUNCTION_DECREMENT_ONE);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };

        // this is a producer/consumer pattern
        // the producer will decrement the counter, consumer will wait until the counter reach zero
        // a timeout exception should be thrown by the wait method
        executor.submit(decrementRunnable);
        try {
            synchronization.waitUntil(CHANNEL1, "a", PREDICATE_EQ_ZERO, 500);
            fail("TimeoutException should be thrown");
        } catch (TimeoutException e) {
            // expected
        }
    }

    @Test
    public void testWaitUntilThen() throws IOException, InvalidChannelException, CompilationException {
        initChannel();
        try {
            synchronization.waitUntilThen(CHANNEL1,
                                          "a",
                                          PREDICATE_WITH_COMPILATION_ERROR,
                                          BIFUNCTION_WITH_COMPILATION_ERROR);
            fail("CompilationException should be thrown");
        } catch (CompilationException e) {
            // expected
        }

        try {
            synchronization.waitUntilThen(CHANNEL1,
                                          "a",
                                          PREDICATE_WITH_RUNTIME_ERROR,
                                          BIFUNCTION_WITH_COMPILATION_ERROR);
            fail("ClosureEvaluationException should be thrown");
        } catch (ClosureEvaluationException e) {
            // expected
        }

        Runnable decrementRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    synchronization.waitUntilThen(CHANNEL1, "a", PREDICATE_GT_ZERO, BIFUNCTION_DECREMENT_ONE);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };

        // this is a producer/consumer pattern
        // each producer will decrement the counter, consumers will wait until the counter reach zero and increment it
        executor.submit(decrementRunnable);
        executor.submit(decrementRunnable);
        synchronization.waitUntilThen(CHANNEL1, "a", PREDICATE_EQ_ZERO, BIFUNCTION_INCREMENT_ONE);
        synchronization.waitUntilThen(CHANNEL1, "a", PREDICATE_EQ_ZERO, BIFUNCTION_INCREMENT_ONE);
        Assert.assertEquals(1, (int) synchronization.get(CHANNEL1, "a"));
    }

}
