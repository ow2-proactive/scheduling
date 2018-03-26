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

import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.function.BiConsumer;
import java.util.function.Function;


// Sonar does not like that I wrap checked exception into unchecked.
// But it is exactly the purpose of some of these functions,
// thus we suppress this warnings.
@SuppressWarnings("squid:S00112")
public class Lambda {

    private Lambda() {
    }

    public static void withLock(Lock lock, Runnable runnable) {
        lock.lock();
        try {
            runnable.run();
        } finally {
            lock.unlock();
        }
    }

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

    @FunctionalInterface
    public interface RunnableThatThrows {
        void run() throws Exception;

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
