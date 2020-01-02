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
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Synchronization provides a persistent <i>key/value store</i> API, similar to the {@link java.util.Map Map} interface.
 *
 * The Synchronization service is automatically started with the ProActive Server.
 *
 * <p>The key/value store is organized in <i>channels</i>. Each channel corresponds to a private key/value {@link java.util.HashMap HashMap}.
 * Synchronization provides an API to create or delete channel and decide if the channel should be persisted on disk (thus available on scheduler restart) or kept in memory only.</p>
 *
 * <p>Each map operation is performed on a given <i>channel</i>, with the channel name provided as parameter.</p>
 *
 * <p>The Synchronization API provides also additional map operations such as {@link #conditionalCompute conditionalCompute} to perform conditional operations based on the result of a {@link java.util.function.BiPredicate}, or wait operations such as {@link #waitUntil(String, String, String)} to block the caller until a given predicate is met.</p>
 *
 * <p>These <i>wait operations</i> allow the API to be used for synchronization and not only as a key/value store. The <i>Synchronization API</i> is notably used to synchronize parallel ProActive Tasks.</p>
 *
 * <p>Each map operation can be performed <b>remotely</b>, for example from <i>ProActive Workflow Tasks</i> executed inside <i>remote ProActive Nodes</i>.</p>
 *
 * <p>Each operation is as well <b>synchronous</b> and <b>atomic</b>, remote operations are received by the service and handled through a request queue.</p>
 *
 * <p>Java 8 Map operations, based on lambdas, are allowed, but lambdas must be provided as String values containing <a href="http://groovy-lang.org/closures.html">Groovy Closures</a> expressions which match the corresponding java lambda.</p>
 *
 * <p>For example, to write a groovy closure which match a Java {@link java.util.function.BiFunction}:</p>
 *
 * <pre> {@code
 * "{ k, x -> x + 1 }"
 * }</pre>
 *
 * @author ActiveEon Team
 * @since 27/03/2018
 * @see java.util.Map
 * @see <a href="http://groovy-lang.org/closures.html">Groovy Closures</a>
 */
@PublicAPI
public interface Synchronization extends Serializable {

    // Channel Operations

    /**
     * Create a channel, overwrite any already existing channel with the same name
     *
     * @param name new channel name
     * @param isPersistent controls whether the channel should have a database backup (in case of crash or restart) or is kept in memory only
     * @return true if the channel was created and no previous channel with the same name already existed
     * @throws IOException if an error occurred while persisting the channel on disk
     */
    boolean createChannel(String name, boolean isPersistent) throws IOException;

    /**
     * Create a channel, if no channel with the same name already exists
     *
     * @param name new or existing channel name
     * @param isPersistent controls whether the channel should have a database backup (in case of crash or restart) or is kept in memory only
     * @return true if a new channel was created and no previous channel with the same name already existed
     * @throws IOException if an error occurred while persisting the channel on disk
     */
    boolean createChannelIfAbsent(String name, boolean isPersistent) throws IOException;

    /**
     * Delete a channel
     *
     * @param name existing channel name
     * @return true if a channel existed with the given name and was deleted by this operation
     * @throws IOException if an error occurred while persisting the channel on disk
     */
    boolean deleteChannel(String name) throws IOException;

    /**
     * Check for channel existence
     * @param name channel name
     * @return true if the channel exists
     */
    boolean channelExists(String name);

    // Standard map operations

    /**
     * Returns the number of key-value mappings in this channel map.  If the
     * channel map contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     *
     * @param channel channel identifier
     *
     * @return the number of key-value mappings in this channel map
     *
     * @throws InvalidChannelException if the channel does not exist
     */
    int size(String channel) throws InvalidChannelException;

    /**
     * Returns <tt>true</tt> if this channel map contains no key-value mappings.
     *
     * @param channel channel identifier
     *
     * @return <tt>true</tt> if this channel map contains no key-value mappings
     *
     * @throws InvalidChannelException if the channel does not exist
     */
    boolean isEmpty(String channel) throws InvalidChannelException;

    /**
     * Returns <tt>true</tt> if this channel map contains a mapping for the specified
     * key.  More formally, returns <tt>true</tt> if and only if
     * this channel map contains a mapping for a key <tt>k</tt> such that
     * <tt>(key==null ? k==null : key.equals(k))</tt>.  (There can be
     * at most one such mapping.)
     *
     * @param channel channel identifier
     * @param key key whose presence in this channel map is to be tested
     * @return <tt>true</tt> if this channel map contains a mapping for the specified
     *         key
     * @throws ClassCastException if the key is of an inappropriate type for
     *         this channel map
     * @throws InvalidChannelException if the channel does not exist
     */
    boolean containsKey(String channel, String key) throws InvalidChannelException;

    /**
     * Returns <tt>true</tt> if this channel map maps one or more keys to the
     * specified value.  More formally, returns <tt>true</tt> if and only if
     * this channel map contains at least one mapping to a value <tt>v</tt> such that
     * <tt>(value==null ? v==null : value.equals(v))</tt>.  This operation
     * will probably require time linear in the channel map size for most
     * implementations of the <tt>Map</tt> interface.
     *
     * @param channel channel identifier
     * @param value value whose presence in this channel map is to be tested
     * @return <tt>true</tt> if this channel map maps one or more keys to the
     *         specified value
     * @throws ClassCastException if the value is of an inappropriate type for
     *         this channel map
     * (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws InvalidChannelException if the channel does not exist
     */
    boolean containsValue(String channel, Serializable value) throws InvalidChannelException;

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this channel map contains no mapping for the key.
     *
     * <p>More formally, if this channel map contains a mapping from a key
     * {@code k} to a value {@code v} such that {@code (key==null ? k==null :
     * key.equals(k))}, then this method returns {@code v}; otherwise
     * it returns {@code null}.  (There can be at most one such mapping.)
     *
     * <p>As this channel map permits null values, a return value of
     * {@code null} does not <i>necessarily</i> indicate that the channel map
     * contains no mapping for the key; it's also possible that the channel map
     * explicitly maps the key to {@code null}.  The {@link #containsKey
     * containsKey} operation may be used to distinguish these two cases.
     *
     * @param channel channel identifier
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or
     *         {@code null} if this channel map contains no mapping for the key
     * @throws ClassCastException if the key is of an inappropriate type for
     *         this channel map
     * (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws InvalidChannelException if the channel does not exist
     */
    Serializable get(String channel, String key) throws InvalidChannelException;

    /**
     * Associates the specified value with the specified key in this channel map
     * (optional operation).  If the channel map previously contained a mapping for
     * the key, the old value is replaced by the specified value.  (A channel map
     * <tt>m</tt> is said to contain a mapping for a key <tt>k</tt> if and only
     * if {@link #containsKey(String, String)}  m.containsKey(k)} would return
     * <tt>true</tt>.)
     *
     * @param channel channel identifier
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     *         (A <tt>null</tt> return can also indicate that the channel map
     *         previously associated <tt>null</tt> with <tt>key</tt>,
     *         as this implementation supports <tt>null</tt> values.)
     * @throws UnsupportedOperationException if the <tt>put</tt> operation
     *         is not supported by this channel map
     * @throws ClassCastException if the class of the specified key or value
     *         prevents it from being stored in this channel map
     * @throws IllegalArgumentException if some property of the specified key
     *         or value prevents it from being stored in this channel map
     * @throws InvalidChannelException if the channel does not exist
     * @throws IOException if an error occurred when persisting the modified channel on disk
     */
    Serializable put(String channel, String key, Serializable value) throws InvalidChannelException, IOException;

    /**
     * Removes the mapping for a key from this channel map if it is present
     * (optional operation).   More formally, if this channel map contains a mapping
     * from key <tt>k</tt> to value <tt>v</tt> such that
     * <code>(key==null ?  k==null : key.equals(k))</code>, that mapping
     * is removed.  (The channel map can contain at most one such mapping.)
     *
     * <p>Returns the value to which this channel map previously associated the key,
     * or <tt>null</tt> if the channel map contained no mapping for the key.
     *
     * <p>As this channel map permits null values, a return value of
     * <tt>null</tt> does not <i>necessarily</i> indicate that the channel map
     * contained no mapping for the key; it's also possible that the channel map
     * explicitly mapped the key to <tt>null</tt>.
     *
     * <p>The channel map will not contain a mapping for the specified key once the
     * call returns.
     *
     * @param channel channel identifier
     * @param key key whose mapping is to be removed from the channel map
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     * @throws UnsupportedOperationException if the <tt>remove</tt> operation
     *         is not supported by this channel map
     * @throws ClassCastException if the key is of an inappropriate type for
     *         this channel map
     * (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws InvalidChannelException if the channel does not exist
     * @throws IOException if an error occurred when persisting the modified channel on disk
     */
    Serializable remove(String channel, String key) throws InvalidChannelException, IOException;

    /**
     * Copies all of the mappings from the specified map to this channel map
     * (optional operation).  The effect of this call is equivalent to that
     * of calling {@link #put(String, String, Serializable)} put(k, v)} on this channel map once
     * for each mapping from key <tt>k</tt> to value <tt>v</tt> in the
     * specified map.  The behavior of this operation is undefined if the
     * specified map is modified while the operation is in progress.
     *
     * @param channel channel identifier
     * @param m mappings to be stored in this channel map
     * @throws UnsupportedOperationException if the <tt>putAll</tt> operation
     *         is not supported by this channel map
     * @throws ClassCastException if the class of a key or value in the
     *         specified channel map prevents it from being stored in this channel map
     * @throws NullPointerException if the specified map is null
     * @throws IllegalArgumentException if some property of a key or value in
     *         the specified channel map prevents it from being stored in this channel map
     * @throws InvalidChannelException if the channel does not exist
     * @throws IOException if an error occurred when persisting the modified channel on disk         
     */
    void putAll(String channel, Map<? extends String, ? extends Serializable> m)
            throws InvalidChannelException, IOException;

    /**
     * Removes all of the mappings from this channel map (optional operation).
     * The channel map will be empty after this call returns.
     *
     * @param channel channel identifier
     * @throws UnsupportedOperationException if the <tt>clear</tt> operation
     *         is not supported by this channel map
     * @throws InvalidChannelException if the channel does not exist
     * @throws IOException if an error occurred when persisting the modified channel on disk         
     */
    void clear(String channel) throws InvalidChannelException, IOException;

    /**
     * Returns a {@link Set} view of the keys contained in this channel map.
     * As this operation is performed remotely, the returned set is a remote copy of the server-side channel content.
     * Any operation on the reported set will not be reflected on the server and can be used only for informative purpose.
     *
     * @param channel channel identifier
     * @return a set view of the keys contained in this map
     * @throws InvalidChannelException if the channel does not exist
     */
    Set<String> keySet(String channel) throws InvalidChannelException;

    /**
     * Returns a {@link Collection} view of the values contained in this channel map.
     * As this operation is performed remotely, the returned collection is a remote copy of the server-side channel content.
     * Any operation on the reported set will not be reflected on the server and can be used only for informative purpose.
     *
     * @param channel channel identifier
     * @return a collection view of the values contained in this map
     * @throws InvalidChannelException if the channel does not exist
     */
    Collection<Serializable> values(String channel) throws InvalidChannelException;

    /**
     * Returns a {@link Set} view of the mappings contained in this map.
     * As this operation is performed remotely, the returned collection is a remote copy of the server-side channel content.
     * Any operation on the reported set will not be reflected on the server and can be used only for informative purpose.
     *
     * @param channel channel identifier
     * @return a set view of the mappings contained in this map
     * @throws InvalidChannelException if the channel does not exist
     */
    Set<Map.Entry<String, Serializable>> entrySet(String channel) throws InvalidChannelException;

    /**
     * Returns the value to which the specified key is mapped, or
     * {@code defaultValue} if this channel map contains no mapping for the key.
     *
     * This implementation guarantees that the operation is performed atomically on the map.
     *
     * @param channel channel identifier
     * @param key the key whose associated value is to be returned
     * @param defaultValue the default mapping of the key
     * @return the value to which the specified key is mapped, or
     * {@code defaultValue} if this channel map contains no mapping for the key
     * @throws ClassCastException if the key is of an inappropriate type for
     * this channel map
     * (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws InvalidChannelException if the channel does not exist
     * @since 1.8
     */
    Serializable getOrDefault(String channel, String key, Serializable defaultValue) throws InvalidChannelException;

    /**
     * Performs the given action for each entry in this channel map until all entries
     * have been processed or the action throws an exception.   Unless
     * otherwise specified by the implementing class, actions are performed in
     * the order of entry set iteration (if an iteration order is specified.)
     * Exceptions thrown by the action are relayed to the caller as ClosureEvaluationException.
     *
     * The action is passed as a String containing a groovy closure definition which matches the {@link java.util.function.BiConsumer} specification.
     * 
     * The first parameter receives the key, and the second parameter receives the binding associated with this key.
     *
     * For example:
     * <pre> {@code
     * "{ k, x -> println(k + \":\" + x) }"
     * }</pre>
     *
     * This implementation guarantees that the operation is performed atomically on the channel map.
     *
     * @param channel channel identifier
     * @param action The action to be performed for each entry
     * @throws NullPointerException if the specified action is null
     * @throws InvalidChannelException if the channel does not exist
     * @throws CompilationException if an error occurred during the compilation of the specified action
     * @throws ClosureEvaluationException if an error occurred during the evaluation of the specified action
     * @since 1.8
     * @see java.util.Map#forEach(BiConsumer)
     * @see java.util.function.BiConsumer
     * @see <a href="http://groovy-lang.org/closures.html">Groovy Closures</a>
     */
    void forEach(String channel, String action) throws InvalidChannelException, CompilationException;

    /**
     * Replaces each entry's value with the result of invoking the given
     * function on that entry until all entries have been processed or the
     * function throws an exception.  Exceptions thrown by the function are
     * relayed to the caller as ClosureEvaluationException.
     *
     * The mapping function is passed as a String containing a groovy closure definition which matches the {@link java.util.function.BiFunction} specification.
     *
     * The first parameter receives the key, and the second parameter receives the binding associated with this key.
     *
     * For example:
     * <pre> {@code
     * "{ k, x -> x = x + 1 }"
     * }</pre>
     *
     * This implementation guarantees that the operation is performed atomically on the channel map.
     *
     * @param channel channel identifier
     * @param function the function to apply to each entry
     * @throws NullPointerException if the specified function is null
     * @throws InvalidChannelException if the channel does not exist
     * @throws CompilationException if an error occurred during the compilation of the specified action
     * @throws ClosureEvaluationException if an error occurred during the evaluation of the specified action
     * @throws IOException if an error occurred when persisting the modified channel on disk
     * @since 1.8
     * @see java.util.Map#replaceAll(BiFunction)
     * @see java.util.function.BiFunction
     * @see <a href="http://groovy-lang.org/closures.html">Groovy Closures</a>
     */
    void replaceAll(String channel, String function) throws InvalidChannelException, CompilationException, IOException;

    /**
     * If the specified key is not already associated with a value (or is mapped
     * to {@code null}) associates it with the given value and returns
     * {@code null}, else returns the current value.
     *
     * The default implementation is equivalent to, for this {@code
     * map}:
     *
     * <pre> {@code
     * V v = map.get(key);
     * if (v == null)
     *     v = map.put(key, value);
     *
     * return v;
     * }</pre>
     *
     * <p>This implementation guarantees that the operation is performed atomically on the channel map.
     *
     * @param channel channel identifier
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or
     *         {@code null} if there was no mapping for the key.
     *         (A {@code null} return can also indicate that the map
     *         previously associated {@code null} with the key,
     *         as this implementation supports null values.)
     * @throws UnsupportedOperationException if the {@code put} operation
     *         is not supported by this channel map
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws ClassCastException if the key or value is of an inappropriate
     *         type for this channel map
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws IllegalArgumentException if some property of the specified key
     *         or value prevents it from being stored in this channel map
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws InvalidChannelException if the channel does not exist
     * @throws IOException if an error occurred when persisting the modified channel on disk
     * @since 1.8
     * @see java.util.Map#putIfAbsent(Object, Object)
     */
    Serializable putIfAbsent(String channel, String key, Serializable value)
            throws InvalidChannelException, IOException;

    /**
     * Removes the entry for the specified key only if it is currently
     * mapped to the specified value.
     *
     * The default implementation is equivalent to, for this {@code map}:
     *
     * <pre> {@code
     * if (map.containsKey(key) && Objects.equals(map.get(key), value)) {
     *     map.remove(key);
     *     return true;
     * } else
     *     return false;
     * }</pre>
     *
     * <p>This implementation guarantees that the operation is performed atomically on the channel map.
     *
     * @param channel channel identifier
     * @param key key with which the specified value is associated
     * @param value value expected to be associated with the specified key
     * @return {@code true} if the value was removed
     * @throws UnsupportedOperationException if the {@code remove} operation
     *         is not supported by this channel map
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws ClassCastException if the key or value is of an inappropriate
     *         type for this channel map
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws InvalidChannelException if the channel does not exist
     * @throws IOException if an error occurred when persisting the modified channel on disk
     * @since 1.8
     * @see java.util.Map#remove(Object, Object)
     */
    boolean remove(String channel, String key, Serializable value) throws InvalidChannelException, IOException;

    /**
     * Replaces the entry for the specified key only if currently
     * mapped to the specified value.
     *
     * The default implementation is equivalent to, for this {@code map}:
     *
     * <pre> {@code
     * if (map.containsKey(key) && Objects.equals(map.get(key), value)) {
     *     map.put(key, newValue);
     *     return true;
     * } else
     *     return false;
     * }</pre>
     *
     * <p>This implementation guarantees that the operation is performed atomically on the channel map.
     *
     * @param channel channel identifier
     * @param key key with which the specified value is associated
     * @param oldValue value expected to be associated with the specified key
     * @param newValue value to be associated with the specified key
     * @return {@code true} if the value was replaced
     * @throws UnsupportedOperationException if the {@code put} operation
     *         is not supported by this channel map
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws ClassCastException if the class of a specified key or value
     *         prevents it from being stored in this channel map
     * @throws IllegalArgumentException if some property of a specified key
     *         or value prevents it from being stored in this channel map
     * @throws InvalidChannelException if the channel does not exist
     * @throws IOException if an error occurred when persisting the modified channel on disk
     * @since 1.8
     * @see java.util.Map#replace(Object, Object, Object)
     */
    boolean replace(String channel, String key, Serializable oldValue, Serializable newValue)
            throws InvalidChannelException, IOException;

    /**
     * Replaces the entry for the specified key only if it is
     * currently mapped to some value.
     *
     * The default implementation is equivalent to, for this {@code map}:
     *
     * <pre> {@code
     * if (map.containsKey(key)) {
     *     return map.put(key, value);
     * } else
     *     return null;
     * }</pre>
     *
     * <p>This implementation guarantees that the operation is performed atomically on the channel map.
     *
     * @param channel channel identifier
     * @param key key with which the specified value is associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or
     *         {@code null} if there was no mapping for the key.
     *         (A {@code null} return can also indicate that the channel map
     *         previously associated {@code null} with the key,
     *         as this implementation supports null values.)
     * @throws UnsupportedOperationException if the {@code put} operation
     *         is not supported by this channel map
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws ClassCastException if the class of the specified key or value
     *         prevents it from being stored in this channel map
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws IllegalArgumentException if some property of the specified key
     *         or value prevents it from being stored in this channel map
     * @throws InvalidChannelException if the channel does not exist
     * @throws IOException if an error occurred when persisting the modified channel on disk
     * @since 1.8
     * @see java.util.Map#replace(Object, Object)
     */
    Serializable replace(String channel, String key, Serializable value) throws InvalidChannelException, IOException;

    /**
     * If the specified key is not already associated with a value (or is mapped
     * to {@code null}), attempts to compute its value using the given mapping
     * function and enters it into this map unless {@code null}.
     *
     * The function is passed as a String containing a groovy closure definition which matches the {@link java.util.function.Function} specification.
     *
     * The parameter received contains the key.
     *
     * For example:
     * <pre> {@code
     * "{ k -> new HashSet() }"
     * }</pre>
     *
     * <p>If the function returns {@code null} no mapping is recorded. If
     * the function itself throws an (unchecked) exception, the
     * exception is rethrown as a ClosureEvaluationException, and no mapping is recorded.
     *
     * The most common usage is to construct a new object serving as an initial
     * mapped value or memoized result, as in:
     *
     * <pre> {@code
     * store.computeIfAbsent(channel, key, "{k -> new Value(k)}");
     * }</pre>
     *
     * <p>Or to implement a multi-value map, {@code Map<K,Collection<V>>},
     * supporting multiple values per key:
     *
     *
     * The default implementation is equivalent to the following steps for this
     * {@code map}, then returning the current value or {@code null} if now
     * absent:
     *
     * <pre> {@code
     * if (map.get(key) == null) {
     *     V newValue = mappingFunction.apply(key);
     *     if (newValue != null)
     *         map.put(key, newValue);
     * }
     * }</pre>
     *
     * <p>This implementation guarantees that the operation is performed atomically on the channel map.
     *
     * @param channel channel identifier
     * @param key key with which the specified value is to be associated
     * @param mappingFunction the function to compute a value
     * @return the current (existing or computed) value associated with
     *         the specified key, or null if the computed value is null
     * @throws CompilationException if an error occurred during the compilation of the specified action
     * @throws ClosureEvaluationException if an error occurred during the evaluation of the specified action
     * @throws InvalidChannelException if the channel does not exist
     * @throws NullPointerException if the mappingFunction is null
     * @throws IOException if an error occurred when persisting the modified channel on disk
     * @since 1.8
     * @see java.util.Map#computeIfAbsent(Object, Function)
     * @see java.util.function.Function
     * @see <a href="http://groovy-lang.org/closures.html">Groovy Closures</a>
     */
    Serializable computeIfAbsent(String channel, String key, String mappingFunction)
            throws InvalidChannelException, CompilationException, IOException;

    /**
     * If the value for the specified key is present and non-null, attempts to
     * compute a new mapping given the key and its current mapped value.
     *
     * <p>If the function returns {@code null}, the mapping is removed.  If the
     * function itself throws an (unchecked) exception, the exception is
     * rethrown, and the current mapping is left unchanged.
     *
     * The function is passed as a String containing a groovy closure definition which matches the {@link java.util.function.BiFunction} specification.
     *
     * The first parameter receives the key, and the second parameter receives the binding associated with this key.
     *
     * For example:
     * <pre> {@code
     * "{ k, x -> x = x + 1 }"
     * }</pre>
     *
     * The default implementation is equivalent to performing the following
     * steps for this {@code map}, then returning the current value or
     * {@code null} if now absent:
     *
     * <pre> {@code
     * if (map.get(key) != null) {
     *     V oldValue = map.get(key);
     *     V newValue = remappingFunction.apply(key, oldValue);
     *     if (newValue != null)
     *         map.put(key, newValue);
     *     else
     *         map.remove(key);
     * }
     * }</pre>
     *
     * <p>This implementation guarantees that the operation is performed atomically on the channel map.
     *
     * @param channel channel identifier
     * @param key key with which the specified value is to be associated
     * @param remappingFunction the function to compute a value
     * @return the new value associated with the specified key, or null if none
     * @throws NullPointerException if the remappingFunction is null
     * @throws CompilationException if an error occurred during the compilation of the specified action
     * @throws ClosureEvaluationException if an error occurred during the evaluation of the specified action
     * @throws InvalidChannelException if the channel does not exist
     * @throws IOException if an error occurred when persisting the modified channel on disk
     * @since 1.8
     * @see java.util.Map#computeIfPresent(Object, BiFunction)
     * @see java.util.function.BiFunction
     * @see <a href="http://groovy-lang.org/closures.html">Groovy Closures</a>
     */
    Serializable computeIfPresent(String channel, String key, String remappingFunction)
            throws InvalidChannelException, CompilationException, IOException;

    /**
     * Attempts to compute a mapping for the specified key and its current
     * mapped value (or {@code null} if there is no current mapping).
     *
     * The function is passed as a String containing a groovy closure definition which matches the {@link java.util.function.BiFunction} specification.
     * The first parameter receives the key, and the second parameter receives the binding associated with this key.
     *
     * For example, to either create or append a {@code String} msg to a value
     * mapping:
     *
     * <pre> {@code
     * store.compute(channel, key, "{ k, v -> (v == null) ? \"" + msg + "\" : v.concat(\"" + msg + "\")}")}</pre>
     * (Method {@link #merge merge()} is often simpler to use for such purposes.)
     *
     * <p>If the function returns {@code null}, the mapping is removed (or
     * remains absent if initially absent).  If the function itself throws an
     * (unchecked) exception, the exception is rethrown as a ClosureEvaluationException, and the current mapping
     * is left unchanged.
     *
     * The default implementation is equivalent to performing the following
     * steps for this {@code map}, then returning the current value or
     * {@code null} if absent:
     *
     * <pre> {@code
     * V oldValue = map.get(key);
     * V newValue = remappingFunction.apply(key, oldValue);
     * if (oldValue != null ) {
     *    if (newValue != null)
     *       map.put(key, newValue);
     *    else
     *       map.remove(key);
     * } else {
     *    if (newValue != null)
     *       map.put(key, newValue);
     *    else
     *       return null;
     * }
     * }</pre>
     *
     * <p>This implementation guarantees that the operation is performed atomically on the channel map.
     *
     * @param channel channel identifier
     * @param key key with which the specified value is to be associated
     * @param remappingFunction the function to compute a value
     * @return the new value associated with the specified key, or null if none
     * @throws NullPointerException if the remappingFunction is null
     * @throws CompilationException if an error occurred during the compilation of the specified action
     * @throws ClosureEvaluationException if an error occurred during the evaluation of the specified action
     * @throws InvalidChannelException if the channel does not exist
     * @throws IOException if an error occurred when persisting the modified channel on disk
     * @since 1.8
     * @see java.util.Map#compute(Object, BiFunction)
     * @see java.util.function.BiFunction
     * @see <a href="http://groovy-lang.org/closures.html">Groovy Closures</a>
     */
    Serializable compute(String channel, String key, String remappingFunction)
            throws InvalidChannelException, CompilationException, IOException;

    /**
     * If the specified key is not already associated with a value or is
     * associated with null, associates it with the given non-null value.
     * Otherwise, replaces the associated value with the results of the given
     * remapping function, or removes if the result is {@code null}. This
     * method may be of use when combining multiple mapped values for a key.
     *
     * The function is passed as a String containing a groovy closure definition which matches the {@link java.util.function.BiFunction} specification.
     * The first parameter receives the key, and the second parameter receives the binding associated with this key.
     *
     * For example, to either create or append a {@code String msg} to a
     * value mapping:
     *
     * <pre> {@code
     * store.merge(channel, key, msg, "{k, x -> x.concat(\"" + msg + "\")}")
     * }</pre>
     *
     * <p>If the function returns {@code null} the mapping is removed.  If the
     * function itself throws an (unchecked) exception, the exception is
     * rethrown, and the current mapping is left unchanged.
     *
     * The default implementation is equivalent to performing the following
     * steps for this {@code map}, then returning the current value or
     * {@code null} if absent:
     *
     * <pre> {@code
     * V oldValue = map.get(key);
     * V newValue = (oldValue == null) ? value :
     *              remappingFunction.apply(oldValue, value);
     * if (newValue == null)
     *     map.remove(key);
     * else
     *     map.put(key, newValue);
     * }</pre>
     *
     * <p>This implementation guarantees that the operation is performed atomically on the channel map.
     *
     * @param channel channel identifier
     * @param key key with which the resulting value is to be associated
     * @param value the non-null value to be merged with the existing value
     *        associated with the key or, if no existing value or a null value
     *        is associated with the key, to be associated with the key
     * @param remappingFunction the function to recompute a value if present
     * @return the new value associated with the specified key, or null if no
     *         value is associated with the key
     * @throws NullPointerException if the remappingFunction is null
     * @throws CompilationException if an error occurred during the compilation of the specified action
     * @throws ClosureEvaluationException if an error occurred during the evaluation of the specified action
     * @throws InvalidChannelException if the channel does not exist
     * @throws IOException if an error occurred when persisting the modified channel on disk
     * @since 1.8
     * @see java.util.Map#merge(Object, Object, BiFunction)
     * @see java.util.function.BiFunction
     * @see <a href="http://groovy-lang.org/closures.html">Groovy Closures</a>
     */
    Serializable merge(String channel, String key, Serializable value, String remappingFunction)
            throws InvalidChannelException, CompilationException, IOException;

    // Additional operations

    /**
     * Conditional execution on a key based on a predicate result and remapping function.
     *
     * Both predicate and function are passed as String containing a groovy closure definition which matches the {@link java.util.function.BiPredicate} and {@link java.util.function.BiFunction} specifications.
     * The first parameter receives the key, and the second parameter receives the binding associated with this key.
     *
     * For example, to append a {@code String} msg to a value only if the value is already defined
     * mapping:
     *
     * <pre> {@code
     * store.conditionalCompute(channel, key, "{ k, v -> v != null}", "{k, v -> v.concat(\"" + msg + "\")}")}</pre>
     *
     * <p>If the function returns {@code null}, the mapping is removed (or
     * remains absent if initially absent).  If the predicate or function itself throw an
     * (unchecked) exception, the exception is rethrown as a ClosureEvaluationException, and the current mapping
     * is left unchanged.
     *
     * The default implementation is equivalent to performing the following
     * steps for this {@code map}, then returning {@link PredicateActionResult} object containing the current value or
     * {@code null} if absent:
     *
     * <pre> {@code
     * V oldValue = map.get(key);
     * if (predicate.test(key, oldValue)) {
     *  map.compute(key, thenRemappingFunction);
     * }
     * }</pre>
     *
     * <p>This implementation guarantees that the operation is performed atomically on the channel map.
     *
     * @param channel channel identifier
     * @param key key with which the specified value is to be associated
     * @param predicate closure used for conditional decision
     * @param thenRemappingFunction the function to compute a value if the predicate is true
     * @return a {@link PredicateActionResult} object which contains the result of the predicate evaluation and either (true) the new value associated with the key after applying thenRemappingFunction, or (false) the unmodified value associated with the key.
     * @throws NullPointerException if the predicate or remappingFunction is null
     * @throws CompilationException if an error occurred during the compilation of the specified action
     * @throws ClosureEvaluationException if an error occurred during the evaluation of the specified action
     * @throws InvalidChannelException if the channel does not exist
     * @throws IOException if an error occurred when persisting the modified channel on disk
     * @since 1.8
     * @see java.util.Map#compute(Object, BiFunction)
     * @see java.util.function.BiPredicate
     * @see java.util.function.BiFunction
     * @see <a href="http://groovy-lang.org/closures.html">Groovy Closures</a>
     * @see org.ow2.proactive.scheduler.synchronization.PredicateActionResult
     */
    PredicateActionResult conditionalCompute(String channel, String key, String predicate, String thenRemappingFunction)
            throws InvalidChannelException, CompilationException, IOException;

    /**
     * Conditional execution on a key based on a predicate result and two remapping functions.
     *
     * Both predicate and functions are passed as String containing a groovy closure definition which matches the {@link java.util.function.BiPredicate} and {@link java.util.function.BiFunction} specifications.
     * The first parameter receives the key, and the second parameter receives the binding associated with this key.
     *
     * For example, to append a {@code String} msg to a value if the value is already defined, and otherwise set msg as the new mapping
     * mapping:
     *
     * <pre> {@code
     * store.conditionalCompute(channel, key, "{ k, v -> v != null}", "{k, v -> v.concat(\"" + msg + "\")}", "{k, v -> \"" + msg + "\""}")}</pre>
     *
     * <p>If either function returns {@code null}, the mapping is removed (or
     * remains absent if initially absent).  If the predicate or function itself throw an
     * (unchecked) exception, the exception is rethrown as a ClosureEvaluationException, and the current mapping
     * is left unchanged.
     *
     * The default implementation is equivalent to performing the following
     * steps for this {@code map}, then returning a PredicateActionResult object containing the current value or
     * {@code null} if absent:
     *
     * <pre> {@code
     * V oldValue = map.get(key);
     * if (predicate.test(key, oldValue)) {
     *  map.compute(key, thenRemappingFunction);
     * } else {
     *  map.compute(key, elseRemappingFunction);
     * }
     * }</pre>
     *
     * <p>This implementation guarantees that the operation is performed atomically on the channel map.
     *
     * @param channel channel identifier
     * @param key key with which the specified value is to be associated
     * @param predicate closure used for conditional decision
     * @param thenRemappingFunction the function to compute a value if the predicate is true
     * @param elseRemappingFunction the function to compute a value if the predicate is false
     * @return a {@link PredicateActionResult} object which contains the result of the predicate evaluation and either (true) the new value associated with the key after applying thenRemappingFunction, or (false) the new value associated with the key after applying elseRemappingFunction.
     * @throws NullPointerException if the predicate or any remappingFunction is null
     * @throws CompilationException if an error occurred during the compilation of the specified action
     * @throws ClosureEvaluationException if an error occurred during the evaluation of the specified action
     * @throws InvalidChannelException if the channel does not exist
     * @throws IOException if an error occurred when persisting the modified channel on disk
     * @since 1.8
     * @see java.util.Map#compute(Object, BiFunction)
     * @see java.util.function.BiPredicate
     * @see java.util.function.BiFunction
     * @see <a href="http://groovy-lang.org/closures.html">Groovy Closures</a>
     * @see org.ow2.proactive.scheduler.synchronization.PredicateActionResult
     */
    PredicateActionResult conditionalCompute(String channel, String key, String predicate, String thenRemappingFunction,
            String elseRemappingFunction) throws InvalidChannelException, CompilationException, IOException;

    /**
     * Replaces each entry's value with the result of invoking the given function on that entry if the given predicate is satisfied.
     * The operation is performed until all entries have been processed or either the predicate or
     * function throws an exception.  Exceptions thrown by the function are
     * relayed to the caller as ClosureEvaluationException.
     *
     * Both predicate and function are passed as String containing a groovy closure definition which matches the {@link java.util.function.BiPredicate} and {@link java.util.function.BiFunction} specifications.
     * The first parameter receives the key, and the second parameter receives the binding associated with this key.
     *
     * For example, to increment a counter only on positive values
     *
     * <pre> {@code
     * store.conditionalCompute(channel, key, "{ k, v -> v >= 0}", "{k, v -> v + 1}")
     * }</pre>
     *
     * <p>If the function returns {@code null}, the mapping is removed (or
     * remains absent if initially absent).  If the predicate or function itself throw an
     * (unchecked) exception, the exception is rethrown as a ClosureEvaluationException, and the current mapping
     * is left unchanged.
     *
     * The default implementation is equivalent to performing the following
     * steps for this {@code map}, then returning {@link PredicateActionResult} object containing the current value or
     * {@code null} if absent:
     *
     * <pre> {@code
     * for (Map.Entry<String, V> entry : map.entries()) {
     *
     *  if (predicate.test(entry.getKey(), entry.getValue())) {
     *      map.compute(entry.getKey(), thenRemappingFunction);
     *  }
     * }
     * }</pre>
     *
     * <p>This implementation guarantees that the operation is performed atomically on the channel map.
     *
     * @param channel channel identifier
     * @param predicate closure used for conditional decision
     * @param thenRemappingFunction the function to compute a value if the predicate is true
     * @throws NullPointerException if the predicate or remappingFunction is null
     * @throws CompilationException if an error occurred during the compilation of the specified action
     * @throws ClosureEvaluationException if an error occurred during the evaluation of the specified action
     * @throws InvalidChannelException if the channel does not exist
     * @throws IOException if an error occurred when persisting the modified channel on disk
     * @since 1.8
     * @see java.util.Map#compute(Object, BiFunction)
     * @see java.util.function.BiPredicate
     * @see java.util.function.BiFunction
     * @see <a href="http://groovy-lang.org/closures.html">Groovy Closures</a>
     */
    void conditionalReplaceAll(String channel, String predicate, String thenRemappingFunction)
            throws InvalidChannelException, CompilationException, IOException;

    /**
     * Replaces each entry's value with the result of invoking the given function on that entry if the given predicate is satisfied.
     * The operation is performed until all entries have been processed or either the predicate or
     * function throws an exception.  Exceptions thrown by the function are
     * relayed to the caller as ClosureEvaluationException.
     *
     * Both predicate and function are passed as String containing a groovy closure definition which matches the {@link java.util.function.BiPredicate} and {@link java.util.function.BiFunction} specifications.
     * The first parameter receives the key, and the second parameter receives the binding associated with this key.
     *
     * For example, to increment a counter only on positive values
     *
     * <pre> {@code
     * store.conditionalCompute(channel, key, "{ k, v -> v >= 0}", "{k, v -> v + 1}")
     * }</pre>
     *
     * <p>If the function returns {@code null}, the mapping is removed (or
     * remains absent if initially absent).  If the predicate or function itself throw an
     * (unchecked) exception, the exception is rethrown as a ClosureEvaluationException, and the current mapping
     * is left unchanged.
     *
     * The default implementation is equivalent to performing the following
     * steps for this {@code map}, then returning PredicateActionResult object containing the current value or
     * {@code null} if absent:
     *
     * <pre> {@code
     * for (Map.Entry<String, V> entry : map.entries()) {
     *
     *  if (predicate.test(entry.getKey(), entry.getValue())) {
     *      map.compute(entry.getKey(), thenRemappingFunction);
     *  } else {
     *      map.compute(entry.getKey(), elseRemappingFunction);
     *  }
     * }
     * }</pre>
     *
     * <p>This implementation guarantees that the operation is performed atomically on the channel map.
     *
     * @param channel channel identifier
     * @param predicate closure used for conditional decision
     * @param thenRemappingFunction the function to compute a value if the predicate is true
     * @param elseRemappingFunction the function to compute a value if the predicate is false
     * @throws NullPointerException if the predicate or remappingFunction is null
     * @throws CompilationException if an error occurred during the compilation of the specified action
     * @throws ClosureEvaluationException if an error occurred during the evaluation of the specified action
     * @throws InvalidChannelException if the channel does not exist
     * @throws IOException if an error occurred when persisting the modified channel on disk
     * @since 1.8
     * @see java.util.Map#compute(Object, BiFunction)
     * @see java.util.function.BiPredicate
     * @see java.util.function.BiFunction
     * @see <a href="http://groovy-lang.org/closures.html">Groovy Closures</a>
     */
    void conditionalReplaceAll(String channel, String predicate, String thenRemappingFunction,
            String elseRemappingFunction) throws InvalidChannelException, CompilationException, IOException;

    /**
     * Blocking call waiting for a predicate to be met.
     *
     * The predicate is passed as String containing a groovy closure definition which matches the {@link java.util.function.BiPredicate} specification.
     *
     * For example, to wait until a mapping is defined:
     *
     * <pre> {@code
     * store.waitUntil(channel, key, "{ k, v -> v != null}")}</pre>
     *
     * The default implementation is equivalent to performing the following
     * steps for this {@code map}, then returning the result of the predicate, which will always be true;
     *
     * <pre> {@code
     * V oldValue = map.get(key);
     * while (!predicate.test(key, oldValue)) {
     *  oldValue = map.get(key);
     * }
     * }</pre>
     *
     * <p>This implementation guarantees that the operation is performed atomically on the channel map, which means that as soon as another operation modifies the underlying structure to meet the predicate,
     * the waitUntil will be unblocked. Subsequent operations based on this condition wait may still be interleaved by other operations performed on the map. This is why it is generally preferable to use {@link #waitUntilThen waitUntilThen()}  to guaranty both predicate and action.
     *
     * @param channel channel identifier
     * @param key key with which the specified value is to be associated
     * @param predicate closure used for conditional decision
     * @throws NullPointerException if the predicate is null
     * @throws CompilationException if an error occurred during the compilation of the specified actions
     * @throws ClosureEvaluationException if an error occurred during the evaluation of the specified actions
     * @throws InvalidChannelException if the channel does not exist
     * @since 1.8
     * @see java.util.Map#compute(Object, BiFunction)
     * @see java.util.function.BiPredicate
     * @see <a href="http://groovy-lang.org/closures.html">Groovy Closures</a>
     *
     */
    boolean waitUntil(String channel, String key, String predicate)
            throws InvalidChannelException, CompilationException;

    /**
     * Blocking call waiting for a predicate to be met, with a timeout.
     *
     * The predicate is passed as String containing a groovy closure definition which matches the {@link java.util.function.BiPredicate} specification.
     *
     * For example, to wait for at most one minute until a mapping is defined:
     *
     * <pre> {@code
     * store.waitUntil(channel, key, "{ k, v -> v != null}", 60000)}</pre>
     *
     * <p>This implementation guarantees that the operation is performed atomically on the channel map, which means that as soon as another operation modifies the underlying structure to meet the predicate,
     * the waitUntil will be unblocked. Subsequent operations based on this condition wait may still be interleaved by other operations performed on the map. This is why it is generally preferable to use {@link #waitUntilThen waitUntilThen()}  to guaranty both predicate and action.
     *
     * @param channel channel identifier
     * @param key key with which the specified value is to be associated
     * @param predicate closure used for conditional decision
     * @param timeout maximum time in milliseconds to wait
     * @throws NullPointerException if the predicate is null
     * @throws CompilationException if an error occurred during the compilation of the specified actions
     * @throws ClosureEvaluationException if an error occurred during the evaluation of the specified actions
     * @throws InvalidChannelException if the channel does not exist
     * @throws TimeoutException if the predicate is not met before the timeout is reached
     * @since 1.8
     * @see java.util.Map#compute(Object, BiFunction)
     * @see java.util.function.BiPredicate
     * @see <a href="http://groovy-lang.org/closures.html">Groovy Closures</a>
     *
     */
    boolean waitUntil(String channel, String key, String predicate, long timeout)
            throws InvalidChannelException, CompilationException, TimeoutException;

    /**
     * Blocking call waiting for a predicate to be met before executing a remapping function.
     *
     * Attempts to compute a mapping for the specified key and its current mapped value (or {@code null} if there is no current mapping), if the given predicate is met.
     * If the predicate is not yet met, block the operation until it is satisfied
     *
     * Both predicate and function are passed as String containing a groovy closure definition which matches the {@link java.util.function.BiPredicate} and {@link java.util.function.BiFunction} specifications.
     * The first parameter receives the key, and the second parameter receives the binding associated with this key.
     *
     * For example, to append a {@code String} msg to a value when the value will be defined
     * mapping:
     *
     * <pre> {@code
     * store.waitUntilThen(channel, key, "{ k, v -> v != null}", "{k, v -> v.concat(\"" + msg + "\")}")}</pre>
     *
     * <p>If the function returns {@code null}, the mapping is removed (or
     * remains absent if initially absent).  If the predicate or function itself throw an
     * (unchecked) exception, the exception is rethrown as a ClosureEvaluationException, and the current mapping
     * is left unchanged.
     *
     * The default implementation is equivalent to performing the following
     * steps for this {@code map}, then returning the current value or
     * {@code null} if absent:
     *
     * <pre> {@code
     * V oldValue = map.get(key);
     * while (!predicate.test(key, oldValue)) {
     *  oldValue = map.get(key)
     * }
     * map.compute(key, thenRemappingFunction);
     * }</pre>
     *
     * <p>This implementation guarantees that the operation is performed atomically on the channel map, which means that as soon as another operation modifies the underlying structure to meet the predicate,
     * the remappingFunction will be executed at once and atomically, without letting another operation to interfere.
     *
     * @param channel channel identifier
     * @param key key with which the specified value is to be associated
     * @param predicate closure used for conditional decision
     * @param thenRemappingFunction the function to compute a value
     * @return a {@link PredicateActionResult} object which contains the result of the predicate evaluation and the new value associated with the key after applying thenRemappingFunction
     * @throws NullPointerException if the predicate or remappingFunction is null
     * @throws CompilationException if an error occurred during the compilation of the specified action
     * @throws ClosureEvaluationException if an error occurred during the evaluation of the specified action
     * @throws InvalidChannelException if the channel does not exist
     * @throws IOException if an error occurred when persisting the modified channel on disk
     * @since 1.8
     * @see java.util.Map#compute(Object, BiFunction)
     * @see java.util.function.BiPredicate
     * @see java.util.function.BiFunction
     * @see <a href="http://groovy-lang.org/closures.html">Groovy Closures</a>
     */
    PredicateActionResult waitUntilThen(String channel, String key, String predicate, String thenRemappingFunction)
            throws InvalidChannelException, CompilationException, IOException;

    /**
     * Blocking call waiting with timeout for a predicate to be met before executing a remapping function.
     *
     * Attempts to compute a mapping for the specified key and its current mapped value (or {@code null} if there is no current mapping), if the given predicate is met.
     * If the predicate is not yet met, block the operation until it is satisfied or until the specified timeout is reached.
     *
     * Both predicate and function are passed as String containing a groovy closure definition which matches the {@link java.util.function.BiPredicate} and {@link java.util.function.BiFunction} specifications.
     * The first parameter receives the key, and the second parameter receives the binding associated with this key.
     *
     * For example, to append a {@code String} msg to a value when the value will be defined
     * mapping:
     *
     * <pre> {@code
     * store.waitUntilThen(channel, key, "{ k, v -> v != null}", "{k, v -> v.concat(\"" + msg + "\")}")}</pre>
     *
     * <p>If the function returns {@code null}, the mapping is removed (or
     * remains absent if initially absent).  If the predicate or function itself throw an
     * (unchecked) exception, the exception is rethrown as a ClosureEvaluationException, and the current mapping
     * is left unchanged.
     *
     * <p>This implementation guarantees that the operation is performed atomically on the channel map, which means that as soon as another operation modifies the underlying structure to meet the predicate,
     * the remappingFunction will be executed at once and atomically, without letting another operation to interfere.
     *
     * @param channel channel identifier
     * @param key key with which the specified value is to be associated
     * @param predicate closure used for conditional decision
     * @param timeout maximum time in milliseconds to wait
     * @param thenRemappingFunction the function to compute a value
     * @return a {@link PredicateActionResult} object which contains the result of the predicate evaluation and the new value associated with the key after applying thenRemappingFunction
     * @throws NullPointerException if the predicate or remappingFunction is null
     * @throws CompilationException if an error occurred during the compilation of the specified action
     * @throws ClosureEvaluationException if an error occurred during the evaluation of the specified action
     * @throws InvalidChannelException if the channel does not exist
     * @throws IOException if an error occurred when persisting the modified channel on disk
     * @throws TimeoutException if the predicate is not met before the timeout is reached
     * @since 1.8
     * @see java.util.Map#compute(Object, BiFunction)
     * @see java.util.function.BiPredicate
     * @see java.util.function.BiFunction
     * @see <a href="http://groovy-lang.org/closures.html">Groovy Closures</a>
     */
    PredicateActionResult waitUntilThen(String channel, String key, String predicate, long timeout,
            String thenRemappingFunction)
            throws InvalidChannelException, CompilationException, IOException, TimeoutException;

    void freeze() throws IOException;

    void resume() throws IOException;
}
