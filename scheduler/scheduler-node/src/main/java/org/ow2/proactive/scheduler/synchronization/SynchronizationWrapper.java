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

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.mop.MOP;
import org.ow2.proactive.scheduler.common.task.TaskId;


/**
 * SynchronizationWrapper acts as a proxy object to the Synchronization service (Internal) implementation
 *
 * Basically, this wrapper forwards to the service context information such as the current task id or the user (originator).
 *
 * This information allows the Synchronization service to match an operation with the correct TaskLogger
 *
 * Originator is not currently used on the server side, in the future, it could be used to implement channel access restrictions
 *
 * @author ActiveEon Team
 * @since 11/04/2018
 */
public class SynchronizationWrapper implements Synchronization {

    private String originator;

    private TaskId taskId;

    private transient SynchronizationInternal internalAPI;

    private String[] internalAPIUrls;

    private static Logger logger = Logger.getLogger(SynchronizationWrapper.class);

    public SynchronizationWrapper(String originator, TaskId taskId, SynchronizationInternal internalAPI) {
        this.originator = originator;
        this.taskId = taskId;
        this.internalAPI = internalAPI;
        try {
            if (internalAPI != null && MOP.isReifiedObject(internalAPI)) {
                internalAPIUrls = PAActiveObject.getUrls(internalAPI);
            }
        } catch (Exception e) {
            logger.warn("Could not initialize the SynchronizationWrapper for taskId " + taskId, e);
        }
    }

    private synchronized void initInternalAPI() {
        if (this.internalAPI == null && this.internalAPIUrls != null) {
            for (String lookupURL : this.internalAPIUrls) {
                try {
                    this.internalAPI = PAActiveObject.lookupActive(SynchronizationInternal.class, lookupURL);
                    return;
                } catch (Exception e) {
                    logger.warn("Could not lookup Synchronization service at " + lookupURL, e);
                }
            }
            throw new IllegalStateException("Could not locate the Synchronization service at " +
                                            Arrays.toString(internalAPIUrls));
        } else if (this.internalAPI == null && this.internalAPIUrls == null) {
            throw new IllegalStateException("Synchronization api was not initialized properly (see Scheduler logs for details)");
        }
    }

    @Override
    public boolean createChannel(String name, boolean isPersistent) throws IOException {
        initInternalAPI();
        return internalAPI.createChannel(originator, taskId, name, isPersistent);
    }

    @Override
    public boolean createChannelIfAbsent(String name, boolean isPersistent) throws IOException {
        initInternalAPI();
        return internalAPI.createChannelIfAbsent(originator, taskId, name, isPersistent);
    }

    @Override
    public boolean deleteChannel(String name) throws IOException {
        initInternalAPI();
        return internalAPI.deleteChannel(originator, taskId, name);
    }

    @Override
    public boolean channelExists(String name) {
        initInternalAPI();
        return internalAPI.channelExists(originator, taskId, name);
    }

    @Override
    public int size(String channel) throws InvalidChannelException {
        initInternalAPI();
        return internalAPI.size(originator, taskId, channel);
    }

    @Override
    public boolean isEmpty(String channel) throws InvalidChannelException {
        initInternalAPI();
        return internalAPI.isEmpty(originator, taskId, channel);
    }

    @Override
    public boolean containsKey(String channel, String key) throws InvalidChannelException {
        initInternalAPI();
        return internalAPI.containsKey(originator, taskId, channel, key);
    }

    @Override
    public boolean containsValue(String channel, Serializable value) throws InvalidChannelException {
        initInternalAPI();
        return internalAPI.containsValue(originator, taskId, channel, value);
    }

    @Override
    public Serializable get(String channel, String key) throws InvalidChannelException {
        initInternalAPI();
        return internalAPI.get(originator, taskId, channel, key);
    }

    @Override
    public Serializable put(String channel, String key, Serializable value)
            throws InvalidChannelException, IOException {
        initInternalAPI();
        return internalAPI.put(originator, taskId, channel, key, value);
    }

    @Override
    public Serializable remove(String channel, String key) throws InvalidChannelException, IOException {
        initInternalAPI();
        return internalAPI.remove(originator, taskId, channel, key);
    }

    @Override
    public void putAll(String channel, Map<? extends String, ? extends Serializable> m)
            throws InvalidChannelException, IOException {
        initInternalAPI();
        internalAPI.putAll(originator, taskId, channel, m);
    }

    @Override
    public void clear(String channel) throws InvalidChannelException, IOException {
        initInternalAPI();
        internalAPI.clear(originator, taskId, channel);
    }

    @Override
    public Set<String> keySet(String channel) throws InvalidChannelException {
        initInternalAPI();
        return internalAPI.keySet(originator, taskId, channel);
    }

    @Override
    public Collection<Serializable> values(String channel) throws InvalidChannelException {
        initInternalAPI();
        return internalAPI.values(originator, taskId, channel);
    }

    @Override
    public Set<Map.Entry<String, Serializable>> entrySet(String channel) throws InvalidChannelException {
        initInternalAPI();
        return internalAPI.entrySet(originator, taskId, channel);
    }

    @Override
    public Serializable getOrDefault(String channel, String key, Serializable defaultValue)
            throws InvalidChannelException {
        initInternalAPI();
        return internalAPI.getOrDefault(originator, taskId, channel, key, defaultValue);
    }

    @Override
    public void forEach(String channel, String action) throws InvalidChannelException, CompilationException {
        initInternalAPI();
        internalAPI.forEach(originator, taskId, channel, action);
    }

    @Override
    public void replaceAll(String channel, String function)
            throws InvalidChannelException, CompilationException, IOException {
        initInternalAPI();
        internalAPI.replaceAll(originator, taskId, channel, function);
    }

    @Override
    public Serializable putIfAbsent(String channel, String key, Serializable value)
            throws InvalidChannelException, IOException {
        initInternalAPI();
        return internalAPI.putIfAbsent(originator, taskId, channel, key, value);
    }

    @Override
    public boolean remove(String channel, String key, Serializable value) throws InvalidChannelException, IOException {
        initInternalAPI();
        return internalAPI.remove(originator, taskId, channel, key, value);
    }

    @Override
    public boolean replace(String channel, String key, Serializable oldValue, Serializable newValue)
            throws InvalidChannelException, IOException {
        initInternalAPI();
        return internalAPI.replace(originator, taskId, channel, key, oldValue, newValue);
    }

    @Override
    public Serializable replace(String channel, String key, Serializable value)
            throws InvalidChannelException, IOException {
        initInternalAPI();
        return internalAPI.replace(originator, taskId, channel, key, value);
    }

    @Override
    public Serializable computeIfAbsent(String channel, String key, String mappingFunction)
            throws InvalidChannelException, CompilationException, IOException {
        initInternalAPI();
        return internalAPI.computeIfAbsent(originator, taskId, channel, key, mappingFunction);
    }

    @Override
    public Serializable computeIfPresent(String channel, String key, String remappingFunction)
            throws InvalidChannelException, CompilationException, IOException {
        initInternalAPI();
        return internalAPI.computeIfPresent(originator, taskId, channel, key, remappingFunction);
    }

    @Override
    public Serializable compute(String channel, String key, String remappingFunction)
            throws InvalidChannelException, CompilationException, IOException {
        initInternalAPI();
        return internalAPI.compute(originator, taskId, channel, key, remappingFunction);
    }

    @Override
    public Serializable merge(String channel, String key, Serializable value, String remappingFunction)
            throws InvalidChannelException, CompilationException, IOException {
        initInternalAPI();
        return internalAPI.merge(originator, taskId, channel, key, value, remappingFunction);
    }

    @Override
    public PredicateActionResult conditionalCompute(String channel, String key, String predicate,
            String thenRemappingFunction) throws InvalidChannelException, CompilationException, IOException {
        initInternalAPI();
        return internalAPI.conditionalCompute(originator, taskId, channel, key, predicate, thenRemappingFunction);
    }

    @Override
    public PredicateActionResult conditionalCompute(String channel, String key, String predicate,
            String thenRemappingFunction, String elseRemappingFunction)
            throws InvalidChannelException, CompilationException, IOException {
        initInternalAPI();
        return internalAPI.conditionalCompute(originator,
                                              taskId,
                                              channel,
                                              key,
                                              predicate,
                                              thenRemappingFunction,
                                              elseRemappingFunction);
    }

    @Override
    public void conditionalReplaceAll(String channel, String predicate, String thenRemappingFunction)
            throws InvalidChannelException, CompilationException, IOException {
        initInternalAPI();
        internalAPI.conditionalReplaceAll(originator, taskId, channel, predicate, thenRemappingFunction);
    }

    @Override
    public void conditionalReplaceAll(String channel, String predicate, String thenRemappingFunction,
            String elseRemappingFunction) throws InvalidChannelException, CompilationException, IOException {
        initInternalAPI();
        internalAPI.conditionalReplaceAll(originator,
                                          taskId,
                                          channel,
                                          predicate,
                                          thenRemappingFunction,
                                          elseRemappingFunction);
    }

    @Override
    public boolean waitUntil(String channel, String key, String predicate)
            throws InvalidChannelException, CompilationException {
        initInternalAPI();
        return internalAPI.waitUntil(originator, taskId, channel, key, predicate);
    }

    @Override
    public boolean waitUntil(String channel, String key, String predicate, long timeout)
            throws InvalidChannelException, CompilationException, TimeoutException {
        initInternalAPI();
        return internalAPI.waitUntil(originator, taskId, channel, key, predicate, timeout);
    }

    @Override
    public PredicateActionResult waitUntilThen(String channel, String key, String predicate,
            String thenRemappingFunction) throws InvalidChannelException, CompilationException, IOException {
        initInternalAPI();
        return internalAPI.waitUntilThen(originator, taskId, channel, key, predicate, thenRemappingFunction);
    }

    @Override
    public PredicateActionResult waitUntilThen(String channel, String key, String predicate, long timeout,
            String thenRemappingFunction)
            throws InvalidChannelException, CompilationException, IOException, TimeoutException {
        initInternalAPI();
        return internalAPI.waitUntilThen(originator, taskId, channel, key, predicate, timeout, thenRemappingFunction);
    }
}
