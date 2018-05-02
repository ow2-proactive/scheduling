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
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.ow2.proactive.scheduler.common.task.TaskId;


/**
 * @author ActiveEon Team
 * @since 27/03/2018
 */
public interface SynchronizationInternal extends Serializable {

    // Channel Operations

    boolean createChannel(String originator, TaskId taskid, String name, boolean isPersistent) throws IOException;

    boolean createChannelIfAbsent(String originator, TaskId taskid, String name, boolean isPersistent)
            throws IOException;

    boolean deleteChannel(String originator, TaskId taskid, String name) throws IOException;

    boolean channelExists(String originator, TaskId taskid, String name);

    // Standard map operations

    int size(String originator, TaskId taskid, String channel) throws InvalidChannelException;

    boolean isEmpty(String originator, TaskId taskid, String channel) throws InvalidChannelException;

    boolean containsKey(String originator, TaskId taskid, String channel, String key) throws InvalidChannelException;

    boolean containsValue(String originator, TaskId taskid, String channel, Serializable value)
            throws InvalidChannelException;

    Serializable get(String originator, TaskId taskid, String channel, String key) throws InvalidChannelException;

    Serializable put(String originator, TaskId taskid, String channel, String key, Serializable value)
            throws InvalidChannelException, IOException;

    Serializable remove(String originator, TaskId taskid, String channel, String key)
            throws InvalidChannelException, IOException;

    void putAll(String originator, TaskId taskid, String channel, Map<? extends String, ? extends Serializable> m)
            throws InvalidChannelException, IOException;

    void clear(String originator, TaskId taskid, String channel) throws InvalidChannelException, IOException;

    Set<String> keySet(String originator, TaskId taskid, String channel) throws InvalidChannelException;

    Collection<Serializable> values(String originator, TaskId taskid, String channel) throws InvalidChannelException;

    Set<Map.Entry<String, Serializable>> entrySet(String originator, TaskId taskid, String channel)
            throws InvalidChannelException;

    Serializable getOrDefault(String originator, TaskId taskid, String channel, String key, Serializable defaultValue)
            throws InvalidChannelException;

    void forEach(String originator, TaskId taskid, String channel, String action)
            throws InvalidChannelException, CompilationException;

    void replaceAll(String originator, TaskId taskid, String channel, String function)
            throws InvalidChannelException, CompilationException, IOException;

    Serializable putIfAbsent(String originator, TaskId taskid, String channel, String key, Serializable value)
            throws InvalidChannelException, IOException;

    boolean remove(String originator, TaskId taskid, String channel, String key, Serializable value)
            throws InvalidChannelException, IOException;

    boolean replace(String originator, TaskId taskid, String channel, String key, Serializable oldValue,
            Serializable newValue) throws InvalidChannelException, IOException;

    Serializable replace(String originator, TaskId taskid, String channel, String key, Serializable value)
            throws InvalidChannelException, IOException;

    Serializable computeIfAbsent(String originator, TaskId taskid, String channel, String key, String mappingFunction)
            throws InvalidChannelException, CompilationException, IOException;

    Serializable computeIfPresent(String originator, TaskId taskid, String channel, String key,
            String remappingFunction) throws InvalidChannelException, CompilationException, IOException;

    Serializable compute(String originator, TaskId taskid, String channel, String key, String remappingFunction)
            throws InvalidChannelException, CompilationException, IOException;

    Serializable merge(String originator, TaskId taskid, String channel, String key, Serializable value,
            String remappingFunction) throws InvalidChannelException, CompilationException, IOException;

    // Additional operations

    PredicateActionResult conditionalCompute(String originator, TaskId taskid, String channel, String key,
            String predicate, String thenRemappingFunction)
            throws InvalidChannelException, CompilationException, IOException;

    PredicateActionResult conditionalCompute(String originator, TaskId taskid, String channel, String key,
            String predicate, String thenRemappingFunction, String elseRemappingFunction)
            throws InvalidChannelException, CompilationException, IOException;

    void conditionalReplaceAll(String originator, TaskId taskid, String channel, String predicate,
            String thenRemappingFunction) throws InvalidChannelException, CompilationException, IOException;

    void conditionalReplaceAll(String originator, TaskId taskid, String channel, String predicate,
            String thenRemappingFunction, String elseRemappingFunction)
            throws InvalidChannelException, CompilationException, IOException;

    boolean waitUntil(String originator, TaskId taskid, String channel, String key, String predicate)
            throws InvalidChannelException, CompilationException;

    boolean waitUntil(String originator, TaskId taskid, String channel, String key, String predicate, long timeout)
            throws InvalidChannelException, CompilationException, TimeoutException;

    PredicateActionResult waitUntilThen(String originator, TaskId taskid, String channel, String key, String predicate,
            String thenRemappingFunction) throws InvalidChannelException, CompilationException, IOException;

    PredicateActionResult waitUntilThen(String originator, TaskId taskid, String channel, String key, String predicate,
            long timeout, String thenRemappingFunction)
            throws InvalidChannelException, CompilationException, IOException, TimeoutException;
}
