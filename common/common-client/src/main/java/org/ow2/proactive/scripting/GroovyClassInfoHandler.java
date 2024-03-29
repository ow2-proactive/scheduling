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
package org.ow2.proactive.scripting;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.codehaus.groovy.reflection.ClassInfo;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.objectweb.proactive.utils.NamedThreadFactory;

import jdk.nashorn.api.scripting.NashornScriptEngine;


/**
 * This class handles the cleanup of groovy classes in the system class loader
 *
 * Recurrent executions of groovy scripts provokes a memory leak if groovy generated classes are not cleaned from the system class loader
 * The difficulty is that, on a ProActive Node, multiple executions of groovy scripts can happen in parallel from multiple sources (selection scripts, clean script, fork env scripts, non-forked tasks)
 *
 * Additionally, only a System.gc() ensure proper removal of groovy classes from the JVM. If System.gc() is not called,
 * the same classes will be detected again and marked again for cleanup. When a lot of groovy scripts are run, this can dramatically decrease
 * performances as 100K+ groovy classes may be detected over and over. Furthermore, while the long cleanup is performed, it will block any groovy script execution (due to some internal lock mechanism in groovy).
 *
 * Accordingly, the cleaning mechanism must:
 *  1) Take a snapshot of groovy generated classes when no script is currently running. Also, take a snapshot of entries created by groovy in the system class loader private field parallelLockMap (this is achieved via reflection).
 *  2) In a dedicated background thread, perform cleanup of these classes and lock keys periodically and call System.gc() immediately.
 *
 *  To achieve this, the classes use locks and atomic objects to synchronize and share objects with the background cleanup.
 *
 *  The background cleanup is scheduled via a java-property configurable period in seconds (defaults to 10 seconds).
 *
 *  Caveats :
 *   - the cleanup only triggers when no script is running (a script running eternally on the node will prevent any cleanup from occurring)
 *   - the call to System.gc() is mandatory after each cleanup, but a call to System.gc() is not without implications.
 *   Calling it on a ProActive Node frequently will not have a sensible impact, but calling it frequently on the server may decrease its performance.
 *   Accordingly, the clean period must be set differently on the server than the nodes.
 */
public class GroovyClassInfoHandler {

    static final Logger logger = Logger.getLogger(GroovyClassInfoHandler.class);

    // how many groovy scripts are currently running
    private static AtomicInteger runningTasksCount = new AtomicInteger(0);

    // when was the last cleanup operation performed (epoch ms time)
    private static AtomicLong lastGroovyClassCleanup = new AtomicLong(0);

    // dedicated thread performing the cleanup
    private static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1,
                                                                                        new NamedThreadFactory("GroovyClassInfoHandler",
                                                                                                               true));

    // this prefix is used by all groovy classes generated from a groovy script
    private static final String GROOVY_SCRIPT_CLASS_PREFIX = "Script";

    // cleanup period in seconds
    private static int GROOVY_CLASS_CLEANER_PERIOD;

    // configurable cleanup period java property name
    // To disable the mechanism, this property can be set with a value <= 0)
    private static final String GROOVY_CLASS_CLEANER_PERIOD_PROP_NAME = "groovy.class.cleaner.period";

    private static final String META_CLASS_BEGIN = "groovy.runtime.metaclass.Script";

    private static final String META_CLASS_END = "MetaClass";

    private static final String CUSTOMIZER_REGEXP = "Script[0-9]+Customizer";

    private static final String BEAN_INFO_REGEXP = "Script[0-9]+BeanInfo";

    public static final String PARALLEL_LOCK_MAP_FIELD_NAME = "parallelLockMap";

    // shared object between
    private static AtomicReference<List<ClassInfo>> classInfoListReference = new AtomicReference<>();

    private static AtomicReference<Set<String>> parallelLockKeysInAppClassLoaderReference = new AtomicReference<>();

    private static AtomicReference<Set<String>> parallelLockKeysInExtClassLoaderReference = new AtomicReference<>();

    private static boolean ENABLED = false;

    private static ReentrantLock lock = new ReentrantLock();

    static {
        try {
            GROOVY_CLASS_CLEANER_PERIOD = Integer.parseInt(System.getProperty(GROOVY_CLASS_CLEANER_PERIOD_PROP_NAME,
                                                                              "10"));
        } catch (Exception e) {
            GROOVY_CLASS_CLEANER_PERIOD = 10;
        }
        if (GROOVY_CLASS_CLEANER_PERIOD > 0) {
            ENABLED = true;
            executor.scheduleAtFixedRate(() -> {
                lastGroovyClassCleanup.set(System.currentTimeMillis());
                cleanClassInfo();
            }, GROOVY_CLASS_CLEANER_PERIOD, GROOVY_CLASS_CLEANER_PERIOD, TimeUnit.SECONDS);
        }
    }

    // increase task count, this must be called before a groovy script is run
    public static void increaseGroovyScriptCount() {
        if (!ENABLED) {
            return;
        }
        try {
            lock.lockInterruptibly();
            runningTasksCount.incrementAndGet();
        } catch (InterruptedException e) {
            logger.warn("GroovyClassInfoHandler increaseTaskCount interrupted");
        } finally {
            lock.unlock();
        }
    }

    // decrease task count, this must be called after a groovy script is run
    // If the running task count becomes 0, a snapshot of groovy classes that start with "Script" is made
    public static void decreaseGroovyScriptCount() {
        if (!ENABLED) {
            return;
        }
        try {
            lock.lockInterruptibly();
            int count = runningTasksCount.decrementAndGet();
            if (count == 0 && isLastCleanupNotDoneRecently()) {
                lastGroovyClassCleanup.set(System.currentTimeMillis());
                List<ClassInfo> classInfoList = getAllClassInfo();
                classInfoListReference.compareAndSet(null,
                                                     classInfoList.stream()
                                                                  .filter(ci -> checkClassInfoName(ci))
                                                                  .collect(Collectors.toList()));
                logger.debug("All classes count: " + classInfoList.size());
                Set<String> groovySystemKeys = readParallelLockKeys(ClassLoader.getSystemClassLoader());
                parallelLockKeysInAppClassLoaderReference.compareAndSet(null, groovySystemKeys);
                Set<String> groovyExtKeys = readParallelLockKeys(getExtClassLoader());
                parallelLockKeysInExtClassLoaderReference.compareAndSet(null, groovyExtKeys);

                logger.debug("Groovy keys count: " + groovySystemKeys.size() + groovyExtKeys.size());
            }
        } catch (InterruptedException e) {
            logger.warn("GroovyClassInfoHandler increaseTaskCount interrupted");
        } catch (Throwable e) {
            logger.error("Error when analysing groovy classes", e);
        } finally {
            lock.unlock();
        }
    }

    private static ClassLoader getExtClassLoader() {
        // NashornScriptEngine is one of the rare classes loaded by the extension class loader.
        return NashornScriptEngine.class.getClassLoader();
    }

    private static List<ClassInfo> getAllClassInfo() {
        try {
            Collection<ClassInfo> classInfo = ClassInfo.getAllClassInfo();
            if (classInfo != null) {
                return new ArrayList<>(classInfo);
            } else {
                return new ArrayList<>();
            }
        } catch (Throwable e) {
            logger.error("Error when reading class info", e);
            return new ArrayList<>();
        }
    }

    private static boolean checkClassInfoName(ClassInfo classInfo) {
        try {
            return classInfo.getTheClass().getName().startsWith(GROOVY_SCRIPT_CLASS_PREFIX);
        } catch (Throwable e) {
            logger.error("Error when reading class info", e);
            return false;
        }
    }

    private static ConcurrentHashMap<String, Object> getParallelLockField(ClassLoader classLoader) {
        try {
            Field parallelLockMapField = ClassLoader.class.getDeclaredField(PARALLEL_LOCK_MAP_FIELD_NAME);
            parallelLockMapField.setAccessible(true);
            ConcurrentHashMap<String, Object> parallelLockMap = (ConcurrentHashMap<String, Object>) parallelLockMapField.get(classLoader);
            return parallelLockMap;
        } catch (Throwable e) {
            logger.error("Error when reading parallel lock field", e);
            return new ConcurrentHashMap<>();
        }
    }

    private static Set<String> readParallelLockKeys(ClassLoader classLoader) {
        try {
            ConcurrentHashMap<String, Object> parallelLockMap = getParallelLockField(classLoader);
            Set<String> currentLockKeys = new HashSet<>(parallelLockMap.keySet());
            Set<String> groovyKeys = currentLockKeys.stream()
                                                    .filter(key -> key != null && isGroovyKey(key))
                                                    .collect(Collectors.toSet());
            return groovyKeys;
        } catch (Throwable e) {
            logger.error("Error when analysing class loader", e);
            return new HashSet<>();
        }
    }

    private static boolean isGroovyKey(String key) {
        return (key.startsWith(META_CLASS_BEGIN) && key.endsWith(META_CLASS_END)) || key.matches(CUSTOMIZER_REGEXP) ||
               key.matches(BEAN_INFO_REGEXP);
    }

    private static int removeGroovyKeys(Set<String> keys, ClassLoader classLoader) {
        try {
            ConcurrentHashMap<String, Object> parallelLockMap = getParallelLockField(classLoader);
            for (String key : keys) {
                parallelLockMap.remove(key);
            }
            return parallelLockMap.size();
        } catch (Exception e) {
            logger.error("Error when removing parallel keys", e);
            return 0;
        }
    }

    private static boolean isLastCleanupNotDoneRecently() {
        return System.currentTimeMillis() - lastGroovyClassCleanup.get() > (GROOVY_CLASS_CLEANER_PERIOD - 1) * 1000L;
    }

    // Remove from Groovy cache all classes that were marked and call System.gc()
    private static void cleanClassInfo() {
        List<ClassInfo> classInfoList = classInfoListReference.getAndSet(null);
        if (classInfoList != null) {
            try {
                int classesRemoved = 0;
                long start = System.currentTimeMillis();
                for (ClassInfo ci : classInfoList) {
                    Class clazz = ci.getTheClass();
                    if (logger.isTraceEnabled()) {
                        logger.trace("Removing class " + clazz.getName());
                    }
                    try {
                        InvokerHelper.removeClass(clazz);
                        classesRemoved++;
                    } catch (Throwable e) {
                        logger.warn("Error when cleaning groovy classes", e);
                    }
                }
                if (classesRemoved > 0) {
                    // this is very important, without calling it, the same classes will be detected again
                    // note that System.gc() will likely be running asynchronously in a separate thread.
                    System.gc();
                    int remainingKeys = 0;
                    int removedKeys = 0;
                    // removal of parallel lock keys is performed after System.gc() (in case these keys are used by the garbage collection).
                    Set<String> keysToRemove = parallelLockKeysInAppClassLoaderReference.getAndSet(null);
                    if (keysToRemove != null) {
                        removedKeys += keysToRemove.size();
                        remainingKeys += removeGroovyKeys(keysToRemove, ClassLoader.getSystemClassLoader());
                    }
                    keysToRemove = parallelLockKeysInExtClassLoaderReference.getAndSet(null);
                    if (keysToRemove != null) {
                        removedKeys += keysToRemove.size();
                        remainingKeys += removeGroovyKeys(keysToRemove, getExtClassLoader());
                    }

                    logger.info(String.format("Removed %d groovy classes info and %d groovy keys (with %d remaining) in %d ms.",
                                              classesRemoved,
                                              removedKeys,
                                              remainingKeys,
                                              (System.currentTimeMillis() - start)));
                }
            } catch (Throwable e) {
                logger.error("Error when cleaning groovy classes", e);
            }
        }
    }
}
