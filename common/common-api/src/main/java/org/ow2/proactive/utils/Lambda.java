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
package org.ow2.proactive.utils;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;


// Sonar does not like that I wrap checked exception into unchecked.
// But it is exactly the purpose of some of these functions,
// thus we suppress this warnings.
@SuppressWarnings("squid:S00112")
public class Lambda {

    private Lambda() {
    }

    /**
     * Execute a runnable with lock acquire/release
     * @param lock the lock to acquire
     * @param runnable code to execute
     */
    public static void withLock(Lock lock, Runnable runnable) {
        lock.lock();
        try {
            runnable.run();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Execute a callable with lock acquire/release
     * @param lock the lock to acquire
     * @param callable code to execute
     * @param <T> return type
     * @return the callable result
     */
    public static <T> T withLock(Lock lock, Callable<T> callable) {
        lock.lock();
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Execute a callable with lock acquire/release, handling InterruptedException
     * @param lock the lock to acquire interruptibly
     * @param callable code to execute
     * @param interruptedException exception which will be used to wrap the InterruptedException (null to use a RuntimeException)
     * @param <T> return type
     * @param <E> wrapper exception type
     * @return the callable result
     * @throws E when the lock is interrupted
     */
    public static <T, E extends Exception> T withLockInterruptible(Lock lock, Callable<T> callable,
            E interruptedException) throws E {

        try {
            lock.lockInterruptibly();
            return callable.call();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (interruptedException != null) {
                interruptedException.initCause(e);
                throw interruptedException;
            }
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Execute a callable with lock acquire/release, handling one checked exception
     * @param lock the lock to acquire
     * @param callable code to execute
     * @param exception1Class the class of the checked exception which will be handled
     * @param <T> return type
     * @param <E1> type of checked exception
     * @return the callable result
     * @throws E1 when this exception occurs in the callable
     */
    public static <T, E1 extends Exception> T withLockException1(Lock lock, Callable<T> callable,
            Class<E1> exception1Class) throws E1 {
        lock.lock();
        try {
            return callable.call();
        } catch (Exception e) {
            if (exception1Class.isInstance(e)) {
                throw (E1) e;
            }
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Execute a callable with lock acquire/release, handling two checked exceptions
     * @param lock the lock to acquire
     * @param callable code to execute
     * @param exception1Class the class of the first checked exception which will be handled
     * @param exception2Class the class of the second checked exception which will be handled
     * @param <T> return type
     * @param <E1> type of the first checked exception
     * @param <E2> type of the second checked exception
     * @return the callable result
     * @throws E1 when the first exception type occurs in the callable
     * @throws E2 when the second exception type occurs in the callable
     */
    public static <T, E1 extends Exception, E2 extends Exception> T withLockException2(Lock lock, Callable<T> callable,
            Class<E1> exception1Class, Class<E2> exception2Class) throws E1, E2 {
        lock.lock();
        try {
            return callable.call();
        } catch (Exception e) {
            if (exception1Class.isInstance(e)) {
                throw (E1) e;
            }
            if (exception2Class.isInstance(e)) {
                throw (E2) e;
            }
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * repeats func function limit number of times
     */
    public static final BiConsumer<Integer, RunnableThatThrows> repeater = (limit, func) -> {
        for (int i = 0; i < limit; ++i) {
            try {
                func.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    };

    public static <T, R> Function<T, R> silent(FunctionThatThrows<T, R> functionThatThrows) {
        return arg -> {
            try {
                return functionThatThrows.apply(arg);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static <T> Callable<T> silent(CallableThatThrows<T> functionThatThrows) {
        return () -> {
            try {
                return functionThatThrows.apply();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static Runnable silent(RunnableThatThrows functionThatThrows) {
        return () -> {
            try {
                functionThatThrows.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static <T> void forEachWithIndex(Iterable<T> iterable, IteratorWithIndex<T> iteratorWithIndex) {

        Iterator<T> iterator = iterable.iterator();

        int index = 0;

        while (iterator.hasNext()) {
            T nextT = iterator.next();
            iteratorWithIndex.forEachWithIndex(nextT, index);
            index++;
        }
    }

    public static <A, B, C> Map<A, C> mapValues(Map<A, B> map, Function<B, C> mapper) {
        return map.entrySet()
                  .stream()
                  .map(p -> new AbstractMap.SimpleEntry<>(p.getKey(), mapper.apply(p.getValue())))
                  .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    public static <A, B, C> Map<B, C> mapKeys(Map<A, C> map, Function<A, B> mapper) {
        return map.entrySet()
                  .stream()
                  .map(p -> new AbstractMap.SimpleEntry<>(mapper.apply(p.getKey()), p.getValue()))
                  .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    public interface IteratorWithIndex<T> {
        void forEachWithIndex(T itemT, int index);
    }

    @FunctionalInterface
    public interface RunnableThatThrows {
        void run() throws Exception;
    }

    @FunctionalInterface
    public interface RunnableThatThrowsException<T extends Throwable> {
        void run() throws T;
    }

    @FunctionalInterface
    public interface RunnableThatThrows3Exceptions<A extends Throwable, B extends Throwable, C extends Throwable> {
        void run() throws A, B, C;
    }

    @FunctionalInterface
    public interface CallableThatThrows<T> {
        T apply() throws Exception;

    }

    @FunctionalInterface
    public interface FunctionThatThrows<T, R> {
        R apply(T var1) throws Exception;

    }
}
