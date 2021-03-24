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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.mop.MethodCallExecutionFailedException;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.utils.StackTraceUtil;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.util.TaskLogger;

import groovy.lang.GroovyShell;
import jdbm.PrimaryHashMap;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;


/**
 * Implementation of the Synchronization service
 *
 * This implementation uses an Active Object to allow remote interaction and enforce atomicity
 *
 * All requests are <b>synchronous</b> (this is done by returning primitive objects or throwing checked exception)
 *
 * Internally, HashMap are used to implement Channels. Channels are persisted thanks to <a href="https://code.google.com/archive/p/jdbm2/">JDBM2</a>
 *
 * A custom {@link RunActive#runActivity(Body) runActivity} is implemented to handle wait Methods.
 * @author ActiveEon Team
 * @since 26/03/2018
 */
@ActiveObject
public class AOSynchronization implements RunActive, InitActive, EndActive, SynchronizationInternal {

    private static final Logger logger = Logger.getLogger(AOSynchronization.class);

    private static final String RESULT_IS = ", result is: ";

    private static final String QUOTE = "'";

    private static final String PREVIOUS_VALUE_WAS = ", previous value was ";

    private static final String EXCEPTION_WHEN_EVALUATING_CLOSURE = "Exception when evaluating closure : ";

    private static final String ON_KEY = " on key ";

    private static final String WITH_VALUE = ", with value ";

    /** Path to jdbm database main file */
    private File statusFile;

    /** Location of the jdbm database */
    private File statusFileDirectory;

    /** Name of the JDBM Database file used by this service */
    private static final String DEFAULT_STORE_SESSION_NAME = "SchedulerStore";

    /** Schema used inside the JDBM database */
    private static final String STATUS_RECORD_NAME = "STORE";

    private static long WAIT_STEP = 500L;

    /** HashMap storing the in-memory channels */
    private ConcurrentHashMap<String, Channel> inMemoryChannels;

    /** JDBM map storing the persistent channels */
    private PrimaryHashMap<String, Channel> persistedChannels;

    private Map<String, Channel> synchronizedPersistedChannels;

    /** Queue used to memorize and handle wait requests to the active object */
    private Queue<TimedOutRequest> waitUntilQueue = new ArrayDeque<>();

    /** Groovy related configuration */

    private GroovyShell shell;

    private RecordManager recordManager;

    private volatile boolean isStarted = false;

    @java.lang.SuppressWarnings("unused")
    public AOSynchronization() {
        // ProActive empty no arg constructor
    }

    @java.lang.SuppressWarnings("unused")
    public AOSynchronization(String statusFileDirectoryPath) {
        initializeGroovyCompiler();
        initializeStatusFile(statusFileDirectoryPath);
        inMemoryChannels = new ConcurrentHashMap<>();

    }

    private void initializeGroovyCompiler() {
        CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        compilerConfiguration.setTargetBytecode(CompilerConfiguration.JDK8);
        shell = new GroovyShell(this.getClass().getClassLoader(), compilerConfiguration);
    }

    private void initializeStatusFile(String statusFileDirectoryPath) {
        this.statusFileDirectory = new File(statusFileDirectoryPath);
        if (!statusFileDirectory.exists()) {
            boolean result = statusFileDirectory.mkdirs();
            if (!result) {
                throw new IllegalArgumentException("Could not create directory hierarchy for path " +
                                                   statusFileDirectory);
            }
        } else if (!statusFileDirectory.isDirectory()) {
            throw new IllegalArgumentException("Provided directory path exists and is not a directory " +
                                               statusFileDirectory);
        }
        this.statusFile = new File(statusFileDirectory, DEFAULT_STORE_SESSION_NAME);
    }

    /**
     * Initialize the Record Manager.
     *
     * Load the persisted channel from disk at configured location.
     *
     * Delete the persisted database in case a serious error occurred when loading the database.
     * @param firstAttempt first attempt at loading the persisted channels
     * @throws IllegalStateException when an error occurred during database loading, even after cleaning it
     */
    private void init(boolean firstAttempt) {
        close();
        try {
            logger.info("Loading Record Manager from file : " + statusFile);
            recordManager = RecordManagerFactory.createRecordManager(statusFile.getCanonicalPath());
            persistedChannels = recordManager.hashMap(STATUS_RECORD_NAME);
            // While printing the content of persistedChannels, InvalidClassException or jdbm Error can be triggered
            logger.info("Content of persisted store : " + persistedChannels);

            recordManager.commit();
            synchronizedPersistedChannels = Collections.synchronizedMap(persistedChannels);
            if (!firstAttempt) {
                logger.info("Loading of job database successful after clean.");
            }
        } catch (Throwable e) {
            if (firstAttempt) {
                logger.error("Error occurred when loading channels database " + statusFile.getAbsolutePath() +
                             ", now cleaning it and retrying.", e);
                cleanDataBase();
                init(false);
            } else {
                close();
                throw new IllegalStateException("Error when loading database (even after cleaning it): " +
                                                statusFile.getAbsolutePath(), e);
            }
        }
        isStarted = true;
    }

    private void waitUntilStarted() {
        while (!isStarted) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @ImmediateService
    public boolean isStarted() {
        return isStarted;
    }

    private void cleanDataBase() {
        close();
        logger.info("Cleaning database");

        // delete all db files
        File[] dbChannelFiles = statusFileDirectory.listFiles((dir,
                name) -> name.startsWith(DEFAULT_STORE_SESSION_NAME));
        if (dbChannelFiles != null) {
            for (File file : dbChannelFiles) {
                try {
                    logger.info("Deleting " + file);
                    boolean result = file.delete();
                    if (!result) {
                        logger.error("Could not delete file " + file +
                                     ". Synchronization service might not work properly. Please remove the file manually.");
                    }
                } catch (Exception e) {
                    logger.info("Error while deleting file during database cleanup", e);
                }
            }
        }
    }

    public boolean close() {
        inMemoryChannels.clear();
        if (recordManager != null) {
            try {
                logger.info("Closing Record Manager");
                recordManager.close();
                recordManager = null;
            } catch (IOException e) {
                logger.warn("Error when closing Record Manager", e);
            }
        }
        return true;
    }

    private Channel getChannel(String name) throws InvalidChannelException {
        if (inMemoryChannels.containsKey(name)) {
            return inMemoryChannels.get(name);
        } else if (synchronizedPersistedChannels.containsKey(name)) {
            return synchronizedPersistedChannels.get(name);
        }
        throw new InvalidChannelException("Channel " + name + " does not exist");
    }

    @Override
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    public boolean createChannel(String originator, TaskId taskid, String name, boolean isPersistent)
            throws IOException {
        try {
            boolean alreadyExistingChannel = deleteChannel(originator, taskid, name);
            Channel newChannel = new Channel();
            if (isPersistent) {
                synchronizedPersistedChannels.put(name, newChannel);
                logWithContextAndPersist(taskid,
                                         null,
                                         "Created new persistent channel " + QUOTE + name + QUOTE,
                                         Level.INFO);
                recordManager.commit();
            } else {
                inMemoryChannels.put(name, newChannel);
                logWithContextAndPersist(taskid,
                                         null,
                                         "Created new memory channel " + QUOTE + name + QUOTE,
                                         Level.INFO);
            }
            return !alreadyExistingChannel;
        } catch (InvalidChannelException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean deleteChannel(String originator, TaskId taskid, String name) throws IOException {
        try {
            if (synchronizedPersistedChannels.containsKey(name)) {
                synchronizedPersistedChannels.remove(name);
                logWithContextAndPersist(taskid,
                                         null,
                                         "Deleted persistent channel " + QUOTE + name + QUOTE,
                                         Level.INFO);
                recordManager.commit();
                return true;
            } else if (inMemoryChannels.containsKey(name)) {
                inMemoryChannels.remove(name);
                logWithContextAndPersist(taskid, null, "Deleted memory channel " + QUOTE + name + QUOTE, Level.INFO);
                return true;
            } else {
                return false;
            }
        } catch (InvalidChannelException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    @ImmediateService
    public boolean channelExists(String originator, TaskId taskid, String name) {
        waitUntilStarted();
        return inMemoryChannels.containsKey(name) || synchronizedPersistedChannels.containsKey(name);
    }

    @Override
    public boolean createChannelIfAbsent(String originator, TaskId taskid, String name, boolean isPersistent)
            throws IOException {
        if (inMemoryChannels.containsKey(name) || synchronizedPersistedChannels.containsKey(name)) {
            return false;
        } else {
            return createChannel(originator, taskid, name, isPersistent);
        }
    }

    /**
     * Commits pending operations on the given channel
     * @param channel channel name
     * @throws IOException if an error occurs when persisting channel
     */
    private void commitIfNeeded(String channel) throws IOException {
        if (synchronizedPersistedChannels.containsKey(channel)) {
            // Record Manager mark as dirty (uncommited) entries which have be modified via a put call
            // Thus, such operation as persistedChannels.get(channel).dosomething() will not be committed
            // by the following trick, we mark the entry as dirty and commit
            synchronizedPersistedChannels.put(channel, synchronizedPersistedChannels.get(channel));
            recordManager.commit();
        }
    }

    @Override
    @ImmediateService
    public boolean containsKey(String originator, TaskId taskid, String channel, String key)
            throws InvalidChannelException {
        waitUntilStarted();
        return getChannel(channel).containsKey(key);
    }

    @Override
    @ImmediateService
    public int size(String originator, TaskId taskid, String channel) throws InvalidChannelException {
        waitUntilStarted();
        return getChannel(channel).size();
    }

    @Override
    @ImmediateService
    public boolean isEmpty(String originator, TaskId taskid, String channel) throws InvalidChannelException {
        waitUntilStarted();
        return getChannel(channel).isEmpty();
    }

    @Override
    @ImmediateService
    public boolean containsValue(String originator, TaskId taskid, String channel, Serializable value)
            throws InvalidChannelException {
        waitUntilStarted();
        return getChannel(channel).containsValue(value);
    }

    @Override
    @ImmediateService
    public Serializable get(String originator, TaskId taskid, String channel, String key)
            throws InvalidChannelException {
        waitUntilStarted();
        return getChannel(channel).get(key);
    }

    @Override
    public Serializable put(String originator, TaskId taskid, String channel, String key, Serializable value)
            throws InvalidChannelException, IOException {
        Serializable answer = getChannel(channel).put(key, value);
        logWithContextAndPersist(taskid,
                                 channel,
                                 "Put " + value + ON_KEY + QUOTE + key + QUOTE + PREVIOUS_VALUE_WAS + answer + "",
                                 Level.DEBUG);
        return answer;
    }

    @Override
    public Serializable remove(String originator, TaskId taskid, String channel, String key)
            throws InvalidChannelException, IOException {
        Serializable answer = getChannel(channel).remove(key);
        logWithContextAndPersist(taskid,
                                 channel,
                                 "Removed key " + QUOTE + key + QUOTE + PREVIOUS_VALUE_WAS + answer,
                                 Level.DEBUG);
        return answer;
    }

    @Override
    public void putAll(String originator, TaskId taskid, String channel,
            Map<? extends String, ? extends Serializable> m) throws InvalidChannelException, IOException {
        getChannel(channel).putAll(m);
        logWithContextAndPersist(taskid, channel, "PutAll called, with new entries: " + m, Level.DEBUG);
    }

    @Override
    public void clear(String originator, TaskId taskid, String channel) throws InvalidChannelException, IOException {
        getChannel(channel).clear();
        logWithContextAndPersist(taskid, channel, "Cleared channel", Level.DEBUG);
    }

    @Override
    @ImmediateService
    public Set<String> keySet(String originator, TaskId taskid, String channel) throws InvalidChannelException {
        waitUntilStarted();
        return new HashSet(getChannel(channel).keySet());
    }

    @Override
    @ImmediateService
    public Collection<Serializable> values(String originator, TaskId taskid, String channel)
            throws InvalidChannelException {
        waitUntilStarted();
        return getChannel(channel).values();
    }

    @Override
    @ImmediateService
    public Set<Map.Entry<String, Serializable>> entrySet(String originator, TaskId taskid, String channel)
            throws InvalidChannelException {
        waitUntilStarted();
        return getChannel(channel).entrySet();
    }

    @Override
    @ImmediateService
    public Serializable getOrDefault(String originator, TaskId taskid, String channel, String key,
            Serializable defaultValue) throws InvalidChannelException {
        waitUntilStarted();
        return getChannel(channel).getOrDefault(key, defaultValue);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void forEach(String originator, TaskId taskid, String channel, String action)
            throws CompilationException, ClosureEvaluationException, InvalidChannelException {
        try {
            Channel chosenChannel = getChannel(channel);
            chosenChannel.forEach(evaluateClosure(action, BiConsumer.class));
            logWithContextAndPersist(taskid, channel, "Run forEach, new content is: " + chosenChannel, Level.DEBUG);
        } catch (InvalidChannelException | CompilationException e) {
            throw e;
        } catch (Exception e) {
            throw new ClosureEvaluationException(EXCEPTION_WHEN_EVALUATING_CLOSURE + StackTraceUtil.getStackTrace(e));
        }

    }

    @Override
    @SuppressWarnings("unchecked")
    public void replaceAll(String originator, TaskId taskid, String channel, String function)
            throws InvalidChannelException, CompilationException, IOException {
        try {
            Channel chosenChannel = getChannel(channel);
            chosenChannel.replaceAll(evaluateClosure(function, BiFunction.class));
            logWithContextAndPersist(taskid, channel, "Run replaceAll, new content is: " + chosenChannel, Level.DEBUG);
        } catch (InvalidChannelException | CompilationException | IOException e) {
            throw e;
        } catch (Exception e) {
            throw new ClosureEvaluationException(EXCEPTION_WHEN_EVALUATING_CLOSURE + StackTraceUtil.getStackTrace(e));
        }
    }

    @Override
    public Serializable putIfAbsent(String originator, TaskId taskid, String channel, String key, Serializable value)
            throws InvalidChannelException, IOException {
        Serializable answer = getChannel(channel).putIfAbsent(key, value);
        logWithContextAndPersist(taskid,
                                 channel,
                                 "Put " + value + ON_KEY + QUOTE + key + QUOTE + PREVIOUS_VALUE_WAS + answer,
                                 Level.DEBUG);
        return answer;
    }

    @Override
    public boolean remove(String originator, TaskId taskid, String channel, String key, Serializable value)
            throws InvalidChannelException, IOException {
        boolean answer = getChannel(channel).remove(key, value);
        logWithContextAndPersist(taskid,
                                 channel,
                                 "Tried to remove key " + QUOTE + key + QUOTE + " with expected value " + value +
                                          ", success=" + answer,
                                 Level.DEBUG);
        return answer;
    }

    @Override
    public boolean replace(String originator, TaskId taskid, String channel, String key, Serializable oldValue,
            Serializable newValue) throws InvalidChannelException, IOException {
        boolean answer = getChannel(channel).replace(key, oldValue, newValue);
        logWithContextAndPersist(taskid,
                                 channel,
                                 "Tried to replace key " + QUOTE + key + QUOTE + " with expected old value " +
                                          oldValue + " and new value " + newValue + ", success=" + answer,
                                 Level.DEBUG);
        return answer;

    }

    @Override
    public Serializable replace(String originator, TaskId taskid, String channel, String key, Serializable value)
            throws InvalidChannelException, IOException {
        Serializable answer = getChannel(channel).replace(key, value);
        logWithContextAndPersist(taskid,
                                 channel,
                                 "Replaced key " + QUOTE + key + QUOTE + WITH_VALUE + value + ", old value was " +
                                          answer,
                                 Level.DEBUG);
        return answer;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Serializable computeIfAbsent(String originator, TaskId taskid, String channel, String key,
            String mappingFunction) throws InvalidChannelException, CompilationException, IOException {
        Serializable answer;
        try {
            Channel chosenChannel = getChannel(channel);
            answer = chosenChannel.computeIfAbsent(key,
                                                   (Function<? super String, ? extends Serializable>) evaluateClosure(mappingFunction,
                                                                                                                      Function.class));
            logWithContextAndPersist(taskid,
                                     channel,
                                     "Run computeIfAbsent" + ON_KEY + QUOTE + key + QUOTE + RESULT_IS + answer,
                                     Level.DEBUG);
        } catch (InvalidChannelException | CompilationException | IOException e) {
            throw e;
        } catch (Exception e) {
            throw new ClosureEvaluationException(EXCEPTION_WHEN_EVALUATING_CLOSURE + StackTraceUtil.getStackTrace(e));
        }
        return answer;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Serializable computeIfPresent(String originator, TaskId taskid, String channel, String key,
            String remappingFunction) throws InvalidChannelException, CompilationException, IOException {
        Serializable answer;
        try {
            Channel chosenChannel = getChannel(channel);
            answer = chosenChannel.computeIfPresent(key,
                                                    (BiFunction<? super String, ? super Serializable, ? extends Serializable>) evaluateClosure(remappingFunction,
                                                                                                                                               BiFunction.class));
            logWithContextAndPersist(taskid,
                                     channel,
                                     "Run computeIfPresent" + ON_KEY + QUOTE + key + QUOTE + RESULT_IS + answer,
                                     Level.DEBUG);
        } catch (InvalidChannelException | CompilationException | IOException e) {
            throw e;
        } catch (Exception e) {
            throw new ClosureEvaluationException(EXCEPTION_WHEN_EVALUATING_CLOSURE + StackTraceUtil.getStackTrace(e));
        }
        return answer;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Serializable compute(String originator, TaskId taskid, String channel, String key, String remappingFunction)
            throws InvalidChannelException, CompilationException, IOException {
        Serializable answer;
        try {
            answer = getChannel(channel).compute(key,
                                                 (BiFunction<? super String, ? super Serializable, ? extends Serializable>) evaluateClosure(remappingFunction,
                                                                                                                                            BiFunction.class));
            logWithContextAndPersist(taskid,
                                     channel,
                                     "Run compute" + ON_KEY + QUOTE + key + QUOTE + RESULT_IS + answer,
                                     Level.DEBUG);
        } catch (InvalidChannelException | CompilationException | IOException e) {
            throw e;
        } catch (Exception e) {
            throw new ClosureEvaluationException(EXCEPTION_WHEN_EVALUATING_CLOSURE + StackTraceUtil.getStackTrace(e));
        }

        return answer;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Serializable merge(String originator, TaskId taskid, String channel, String key, Serializable value,
            String remappingFunction) throws InvalidChannelException, CompilationException, IOException {
        Serializable answer;
        try {
            answer = getChannel(channel).merge(key,
                                               value,
                                               (BiFunction<? super Serializable, ? super Serializable, ? extends Serializable>) evaluateClosure(remappingFunction,
                                                                                                                                                BiFunction.class));
            logWithContextAndPersist(taskid,
                                     channel,
                                     "Run merge" + ON_KEY + QUOTE + key + QUOTE + WITH_VALUE + value + RESULT_IS +
                                              answer,
                                     Level.DEBUG);
        } catch (InvalidChannelException | CompilationException | IOException e) {
            throw e;
        } catch (Exception e) {
            throw new ClosureEvaluationException(EXCEPTION_WHEN_EVALUATING_CLOSURE + StackTraceUtil.getStackTrace(e));
        }
        return answer;
    }

    @Override
    @SuppressWarnings("unchecked")
    public PredicateActionResult conditionalCompute(String originator, TaskId taskid, String channel, String key,
            String predicate, String thenRemappingFunction)
            throws InvalidChannelException, CompilationException, IOException {
        PredicateActionResult answer;
        try {
            Channel chosenChannel = getChannel(channel);
            Serializable value = chosenChannel.get(key);

            if (evaluateClosure(predicate, BiPredicate.class).test(key, value)) {
                answer = new PredicateActionResult(true,
                                                   chosenChannel.compute(key,
                                                                         (BiFunction<? super String, ? super Serializable, ? extends Serializable>) evaluateClosure(thenRemappingFunction,
                                                                                                                                                                    BiFunction.class)));
            } else {
                answer = new PredicateActionResult(false, value);
            }
            logWithContextAndPersist(taskid,
                                     channel,
                                     "Run conditionalCompute" + ON_KEY + QUOTE + key + QUOTE + RESULT_IS + answer,
                                     Level.DEBUG);

        } catch (InvalidChannelException | CompilationException | IOException e) {
            throw e;
        } catch (Exception e) {
            throw new ClosureEvaluationException(EXCEPTION_WHEN_EVALUATING_CLOSURE + StackTraceUtil.getStackTrace(e));
        }
        return answer;
    }

    @Override
    @SuppressWarnings("unchecked")
    public PredicateActionResult conditionalCompute(String originator, TaskId taskid, String channel, String key,
            String predicate, String thenRemappingFunction, String elseRemappingFunction)
            throws InvalidChannelException, CompilationException, IOException {
        PredicateActionResult answer;
        try {
            Channel chosenChannel = getChannel(channel);
            Serializable value = chosenChannel.get(key);
            if (evaluateClosure(predicate, BiPredicate.class).test(key, value)) {
                answer = new PredicateActionResult(true,
                                                   chosenChannel.compute(key,
                                                                         (BiFunction<? super String, ? super Serializable, ? extends Serializable>) evaluateClosure(thenRemappingFunction,
                                                                                                                                                                    BiFunction.class)));
            } else {
                answer = new PredicateActionResult(false,
                                                   chosenChannel.compute(key,
                                                                         (BiFunction<? super String, ? super Serializable, ? extends Serializable>) evaluateClosure(elseRemappingFunction,
                                                                                                                                                                    BiFunction.class)));
            }
            logWithContextAndPersist(taskid,
                                     channel,
                                     "Run conditionalCompute" + ON_KEY + QUOTE + key + QUOTE + RESULT_IS + answer,
                                     Level.DEBUG);
        } catch (InvalidChannelException | CompilationException | IOException e) {
            throw e;
        } catch (Exception e) {
            throw new ClosureEvaluationException(EXCEPTION_WHEN_EVALUATING_CLOSURE + StackTraceUtil.getStackTrace(e));
        }
        return answer;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void conditionalReplaceAll(String originator, TaskId taskid, String channel, String predicate,
            String thenRemappingFunction) throws InvalidChannelException, CompilationException, IOException {
        String currentKey;
        try {
            Channel chosenChannel = getChannel(channel);
            for (Map.Entry<String, Serializable> entry : chosenChannel.entrySet()) {
                currentKey = entry.getKey();
                if (evaluateClosure(predicate, BiPredicate.class).test(currentKey, entry.getValue())) {

                    chosenChannel.compute(currentKey, evaluateClosure(thenRemappingFunction, BiFunction.class));
                }
            }
            logWithContextAndPersist(taskid,
                                     channel,
                                     "Run conditionalReplaceAll, new content is: " + chosenChannel,
                                     Level.DEBUG);

        } catch (InvalidChannelException | CompilationException | IOException e) {
            throw e;
        } catch (Exception e) {
            throw new ClosureEvaluationException(EXCEPTION_WHEN_EVALUATING_CLOSURE + StackTraceUtil.getStackTrace(e));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void conditionalReplaceAll(String originator, TaskId taskid, String channel, String predicate,
            String thenRemappingFunction, String elseRemappingFunction)
            throws InvalidChannelException, CompilationException, IOException {
        String currentKey;
        try {
            Channel chosenChannel = getChannel(channel);
            for (Map.Entry<String, Serializable> entry : chosenChannel.entrySet()) {
                currentKey = entry.getKey();
                if (evaluateClosure(predicate, BiPredicate.class).test(currentKey, entry.getValue())) {

                    chosenChannel.compute(currentKey,
                                          (BiFunction<? super String, ? super Serializable, ? extends Serializable>) evaluateClosure(thenRemappingFunction,
                                                                                                                                     BiFunction.class));
                } else {
                    chosenChannel.compute(currentKey,
                                          (BiFunction<? super String, ? super Serializable, ? extends Serializable>) evaluateClosure(elseRemappingFunction,
                                                                                                                                     BiFunction.class));

                }
            }

            logWithContextAndPersist(taskid,
                                     channel,
                                     "Run conditionalReplaceAll, new content is: " + chosenChannel,
                                     Level.DEBUG);

        } catch (InvalidChannelException | CompilationException | IOException e) {
            throw e;
        } catch (Exception e) {
            throw new ClosureEvaluationException(EXCEPTION_WHEN_EVALUATING_CLOSURE + StackTraceUtil.getStackTrace(e));
        }
    }

    /**
     * Execute a predicate and return its answer
     */
    private boolean executeWaitPredicate(String originator, TaskId taskid, String channel, String key, String predicate)
            throws InvalidChannelException, CompilationException {
        Channel chosenChannel = getChannel(channel);
        Serializable value = chosenChannel.get(key);
        try {
            return evaluateClosure(predicate, BiPredicate.class).test(key, value);
        } catch (CompilationException e) {
            throw e;
        } catch (Exception e) {
            throw new ClosureEvaluationException(EXCEPTION_WHEN_EVALUATING_CLOSURE + StackTraceUtil.getStackTrace(e));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    @ImmediateService
    public boolean waitUntil(String originator, TaskId taskid, String channel, String key, String predicate)
            throws InvalidChannelException, CompilationException {
        /**
         * waitUntil is run on its dedicated thread (ImmediateService), it runs its predicate periodically until success
         */
        waitUntilStarted();
        Channel chosenChannel = getChannel(channel);
        Serializable value = chosenChannel.get(key);
        try {
            while (!evaluateClosure(predicate, BiPredicate.class).test(key, value)) {
                Thread.sleep(WAIT_STEP);
            }
            return true;
        } catch (CompilationException e) {
            throw e;
        } catch (Exception e) {
            throw new ClosureEvaluationException(EXCEPTION_WHEN_EVALUATING_CLOSURE + StackTraceUtil.getStackTrace(e));
        }
    }

    @Override
    @ImmediateService
    public boolean waitUntil(String originator, TaskId taskid, String channel, String key, String predicate,
            long timeout) throws InvalidChannelException, CompilationException, TimeoutException {
        /**
         * waitUntil with timeout is run on its dedicated thread (ImmediateService).
         * it runs its predicate periodically until success or timeout
         */
        waitUntilStarted();
        Channel chosenChannel = getChannel(channel);
        Serializable value = chosenChannel.get(key);
        long startTime = System.currentTimeMillis();
        try {
            while (!evaluateClosure(predicate, BiPredicate.class).test(key, value)) {
                if (System.currentTimeMillis() - startTime > timeout) {
                    throw new TimeoutException("Timeout of " + timeout + " ms expired while waiting for predicate");
                }
                Thread.sleep(WAIT_STEP);
            }
            return true;
        } catch (CompilationException e) {
            throw e;
        } catch (TimeoutException e) {
            throw e;
        } catch (Exception e) {
            throw new ClosureEvaluationException(EXCEPTION_WHEN_EVALUATING_CLOSURE + StackTraceUtil.getStackTrace(e));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public PredicateActionResult waitUntilThen(String originator, TaskId taskid, String channel, String key,
            String predicate, String thenRemappingFunction)
            throws InvalidChannelException, CompilationException, IOException {
        /**
         * as waitUntilThen can operate a change on the channel, it is handled with the ActiveObject request queue (see runActivity)
         * This it is handled differently from waitUntil
         */
        PredicateActionResult answer;
        try {
            Channel chosenChannel = getChannel(channel);
            Serializable value = chosenChannel.get(key);
            if (evaluateClosure(predicate, BiPredicate.class).test(key, value)) {
                answer = new PredicateActionResult(true,
                                                   chosenChannel.compute(key,
                                                                         (BiFunction<? super String, ? super Serializable, ? extends Serializable>) evaluateClosure(thenRemappingFunction,
                                                                                                                                                                    BiFunction.class)));
            } else {
                throw new IllegalStateException("Predicate " + predicate + " is not met");
            }

            logWithContextAndPersist(taskid,
                                     channel,
                                     "Run waitUntil" + ON_KEY + QUOTE + key + QUOTE + RESULT_IS + answer,
                                     Level.DEBUG);
        } catch (InvalidChannelException | CompilationException | IOException e) {
            throw e;
        } catch (Exception e) {
            throw new ClosureEvaluationException(EXCEPTION_WHEN_EVALUATING_CLOSURE + StackTraceUtil.getStackTrace(e));
        }
        return answer;
    }

    @Override
    public PredicateActionResult waitUntilThen(String originator, TaskId taskid, String channel, String key,
            String predicate, long timeout, String thenRemappingFunction)
            throws InvalidChannelException, CompilationException, IOException, TimeoutException {
        /**
         * as waitUntilThen can operate a change on the channel, it is handled with the ActiveObject request queue (see runActivity)
         * This it is handled differently from waitUntil
         * timeout is ignored inside the method implementation (it is handled by the runActivity method)
         */
        return waitUntilThen(originator, taskid, channel, key, predicate, thenRemappingFunction);
    }

    private void logWithContextAndPersist(TaskId taskId, String channel, String message, Level level)
            throws IOException, InvalidChannelException {
        logWithContext(taskId, channel, message, null, level);
        try {
            commitIfNeeded(channel);
        } catch (IOException e) {
            logWithContext(taskId, channel, "Error when persisting channel content", e, Level.ERROR);
            throw e;
        }
    }

    @SuppressWarnings("squid:S3776")
    private void logWithContext(TaskId taskId, String channel, String message, Throwable exception, Level level)
            throws InvalidChannelException {
        String channelHeader = "[" + Synchronization.class.getSimpleName() + "]" +
                               (channel != null ? "[" + Channel.class.getSimpleName() + "=" + channel + "] " : "");
        switch (level.toInt()) {
            case Level.TRACE_INT:
                if (logger.isTraceEnabled()) {
                    TaskLogger.getInstance().info(taskId, channelHeader + message);
                }
                break;
            case Level.DEBUG_INT:
                if (logger.isDebugEnabled()) {
                    TaskLogger.getInstance().info(taskId, channelHeader + message);
                }
                break;
            case Level.INFO_INT:
                if (logger.isInfoEnabled()) {
                    TaskLogger.getInstance().info(taskId, channelHeader + message);
                }
                break;
            case Level.WARN_INT:
                if (exception != null) {
                    TaskLogger.getInstance().warn(taskId, channelHeader + message, exception);
                } else {
                    TaskLogger.getInstance().warn(taskId, channelHeader + message);
                }
                break;
            case Level.ERROR_INT:
                if (exception != null) {
                    TaskLogger.getInstance().error(taskId, channelHeader + message, exception);
                } else {
                    TaskLogger.getInstance().error(taskId, channelHeader + message);
                }
                break;
            default:
                // no action
        }
        if (logger.isTraceEnabled() && channel != null) {
            TaskLogger.getInstance().info(taskId, channelHeader + "New channel content : " + getChannel(channel));
        }
    }

    @Override
    public void runActivity(Body body) {
        Service service = new Service(body);
        while (body.isActive()) {
            try {
                List<NewRequestWithWaitTime> requestsWithWaitTime = waitForNewRequests(service);
                for (NewRequestWithWaitTime requestWithWaitTime : requestsWithWaitTime) {
                    Request request = requestWithWaitTime.getNewRequest();
                    if (request != null && request.getMethodName().equals("freeze")) {
                        service.serve(request);
                        service.blockingServeOldest("resume");
                    } else if (request != null && request.getMethodName().startsWith("waitUntilThen") &&
                               !testWaitFunction(service, request)) {
                        // If the predicate is not met, delay the wait method execution
                        TimedOutRequest timedOutRequest = new TimedOutRequest(request,
                                                                              extractWaitRequestTimeoutParameter(request));
                        logger.trace("New pending wait request : " + timedOutRequest);
                        waitUntilQueue.add(new TimedOutRequest(request, extractWaitRequestTimeoutParameter(request)));
                    } else if (request != null) {
                        service.serve(request);
                    }
                }
                unblockWaitMethods(service, computeMaxTimeSpentWaiting(requestsWithWaitTime));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private long computeMaxTimeSpentWaiting(List<NewRequestWithWaitTime> requestsWithWaitTime) {
        long maxWait = 0;
        for (NewRequestWithWaitTime requestWithWaitTime : requestsWithWaitTime) {
            if (requestWithWaitTime.waitTime > maxWait) {
                maxWait = requestWithWaitTime.waitTime;
            }
        }
        return maxWait;
    }

    @Override
    public void freeze() throws IOException {
        logger.info("Closing Record Manager");
        recordManager.close();
        recordManager = null;
        isStarted = false;
    }

    @Override
    public void resume() throws IOException {
        logger.info("Loading Record Manager from file : " + statusFile);
        recordManager = RecordManagerFactory.createRecordManager(statusFile.getCanonicalPath());
        persistedChannels = recordManager.hashMap(STATUS_RECORD_NAME);
        recordManager.commit();
        synchronizedPersistedChannels = Collections.synchronizedMap(persistedChannels);
        isStarted = true;
    }

    /**
     * Wait for a new request in the request queue, until a request is found or until a timeout based on the pending wait request queue
     * @param service active object service
     * @return composite object containing the request and the waited time
     * @throws InterruptedException if the thread is interrupted while waiting for request
     */
    private List<NewRequestWithWaitTime> waitForNewRequests(Service service) throws InterruptedException {
        // we cannot wait more then min(pending_wait_requests_timeouts)
        long maximumTimeToWaitForNewRequests = computeMinimumTimeout();
        List<NewRequestWithWaitTime> newRequests = new ArrayList<>();

        logger.trace("Waiting for new requests with timeout = " + maximumTimeToWaitForNewRequests + " ms");
        long waitStart = System.currentTimeMillis();
        do {
            Request request = service.blockingRemoveOldest(maximumTimeToWaitForNewRequests);
            long timeSpentWaiting = System.currentTimeMillis() - waitStart;
            logger.trace("Time spent waiting for new request: " + timeSpentWaiting + " ms");
            newRequests.add(new NewRequestWithWaitTime(request, timeSpentWaiting));
        } while (service.hasRequestToServe());
        return newRequests;
    }

    @java.lang.SuppressWarnings({ "squid:S1481", "squid:S1854", "unused", "unchecked" })
    private long extractWaitRequestTimeoutParameter(Request request) {
        // Extract all common parameters to waitUntil waitUntilThen
        int paramIndex = 0;
        // The following code is mainly for clarity
        String originator = (String) request.getParameter(paramIndex++);
        TaskId taskId = (TaskId) request.getParameter(paramIndex++);
        String channel = (String) request.getParameter(paramIndex++);
        String key = (String) request.getParameter(paramIndex++);
        String predicate = (String) request.getParameter(paramIndex++);
        // Next parameter may or may not be a timeout
        if (request.getMethodCall().getNumberOfParameter() <= paramIndex ||
            !(request.getParameter(paramIndex) instanceof Long)) {
            return Long.MAX_VALUE;
        } else {
            return (long) request.getParameter(paramIndex);
        }
    }

    private long computeMinimumTimeout() {
        long minimumTimeout = Long.MAX_VALUE;
        for (TimedOutRequest request : waitUntilQueue) {
            if (request.getRemainingTimeout() < minimumTimeout) {
                minimumTimeout = request.getRemainingTimeout();
            }
        }
        return minimumTimeout;
    }

    /**
     * Unblock all pending wait methods if predicates are met
     * @param service active object service
     */
    private void unblockWaitMethods(Service service, long timeSpentWaiting) {
        for (Iterator<TimedOutRequest> iterator = waitUntilQueue.iterator(); iterator.hasNext();) {
            TimedOutRequest waitRequest = iterator.next();
            waitRequest.substractTime(timeSpentWaiting);
            logger.trace("Updated timeouted request: " + waitRequest);
            if (testWaitFunction(service, waitRequest.getRequest())) {
                // predicate is met, serve the request
                service.serve(waitRequest.getRequest());
                iterator.remove();
            } else if (waitRequest.getRemainingTimeout() <= 0) {
                // predicate is not met and timeout expired, serve the request with a timeout exception
                logger.trace("Timeout observed: " + waitRequest.getRequest().getMethodName());
                service.serveWithException(waitRequest.getRequest(),
                                           new TimeoutException("Timeout of " + waitRequest.getConfiguredTimeout() +
                                                                " ms expired while waiting for predicate"));
                iterator.remove();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T evaluateClosure(String closureDefinition, Class<T> type) throws CompilationException {
        try {
            return (T) shell.evaluate(closureDefinition + " as " + type.getCanonicalName());
        } catch (CompilationFailedException e) {
            // CompilationFailedException contains instances which are not serializable
            throw new CompilationException(StackTraceUtil.getStackTrace(e));
        }
    }

    /**
     * Execute the waitUntil function corresponding to the provided request
     *
     * If an error occurs, serve the request to unblock the caller with the exception attached
     *
     * @param service active object service
     * @param request request to process
     * @return true if the result of the waitUntil call is true or if an exception occurred
     */
    private boolean testWaitFunction(Service service, Request request) {
        boolean dontNeedWaiting;
        try {

            dontNeedWaiting = executeWaitPredicateFunction(request);
        } catch (Exception e) {
            service.serveWithException(request, e);
            // in case an exception occurs, don't delay the request, execute it immediately to notify the client
            dontNeedWaiting = true;
        }
        return dontNeedWaiting;

    }

    /**
     * Extract from the request the parameters corresponding to the waitUntil method call, then execute waitUntil with these parameters
     * @param request request used to extract parameters
     * @return the result of the waitUntil method call
     */
    private boolean executeWaitPredicateFunction(Request request) throws InvalidChannelException, CompilationException {
        // extract the parameters which match the waitUntil call
        int paramIndex = 0;
        String originator = (String) request.getParameter(paramIndex++);
        TaskId taskId = (TaskId) request.getParameter(paramIndex++);
        String channel = (String) request.getParameter(paramIndex++);
        String key = (String) request.getParameter(paramIndex++);
        String predicate = (String) request.getParameter(paramIndex);
        return this.executeWaitPredicate(originator, taskId, channel, key, predicate);
    }

    @Override
    public void initActivity(Body body) {
        logger.info("Starting Synchronization service");
        init(true);
        logger.info("Synchronization service is started");
    }

    @Override
    public void endActivity(Body body) {
        close();
    }

    public static class Channel extends ConcurrentHashMap<String, Serializable> {
        private static final long serialVersionUID = 1L;
    }

    private static final class NewRequestWithWaitTime {
        private Request newRequest;

        private long waitTime;

        NewRequestWithWaitTime(Request newRequest, long waitTime) {
            this.newRequest = newRequest;
            this.waitTime = waitTime;
        }

        Request getNewRequest() {
            return newRequest;
        }

        long getWaitTime() {
            return waitTime;
        }
    }

    private static final class TimedOutRequest {
        private Request request;

        private long configuredTimeout;

        private long remainingTimeout;

        TimedOutRequest(Request request, long configuredTimeout) {
            this.request = request;
            this.configuredTimeout = configuredTimeout;
            this.remainingTimeout = configuredTimeout;
        }

        void substractTime(long timespent) {
            this.remainingTimeout -= timespent;
        }

        Request getRequest() {
            return request;
        }

        long getConfiguredTimeout() {
            return configuredTimeout;
        }

        long getRemainingTimeout() {
            return remainingTimeout;
        }

        @Override
        public String toString() {
            return "TimedOutRequest{" + "request=" + request + ", configuredTimeout=" + configuredTimeout +
                   ", remainingTimeout=" + remainingTimeout + '}';
        }
    }
}
