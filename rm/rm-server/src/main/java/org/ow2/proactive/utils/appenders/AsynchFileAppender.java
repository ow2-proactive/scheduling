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
package org.ow2.proactive.utils.appenders;

import static org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties.LOG4J_ASYNC_APPENDER_BUFFER_SIZE;
import static org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties.LOG4J_ASYNC_APPENDER_FLUSH_TIMOUT;

import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.*;
import org.apache.log4j.spi.LoggingEvent;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;


public class AsynchFileAppender extends FileAppender implements RemovalListener<String, RollingFileAppender> {

    private static final Logger LOGGER = Logger.getLogger(AsynchFileAppender.class);

    final BlockingQueue<ApplicableEvent> queue = new ArrayBlockingQueue<>(LOG4J_ASYNC_APPENDER_BUFFER_SIZE.getValueAsInt(),
                                                                          true);

    protected ReentrantReadWriteLock preventConcurrentAppendClose = new ReentrantReadWriteLock();

    protected ReentrantReadWriteLock.ReadLock isAppending = preventConcurrentAppendClose.readLock();

    protected ReentrantReadWriteLock.WriteLock isClosing = preventConcurrentAppendClose.writeLock();

    private Cache<String, RollingFileAppender> appenderCache;

    public AsynchFileAppender() {
        super();

        appenderCache = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).removalListener(this).build();

        Thread logEventsProcessor = new Thread(this::logEventProcessor, "logEventsProcessor");
        logEventsProcessor.setDaemon(true);
        logEventsProcessor.start();
    }

    // non blocking
    public void append(String cacheKey, LoggingEvent event) {
        try {
            isAppending.lock();
            queue.put(new ApplicableEvent(cacheKey, event));
        } catch (InterruptedException e) {
            LOGGER.warn("Interrupted append on " + cacheKey);
            Thread.currentThread().interrupt();
        } finally {
            isAppending.unlock();
        }
    }

    @Override
    public void close() {
        try {
            isClosing.lock();
            flush();
            extractKey().ifPresent(key -> {
                RollingFileAppender appender = appenderCache.getIfPresent(key);
                if (appender != null) {
                    appenderCache.invalidate(key);
                    appender.close();
                }
            });
        } finally {
            isClosing.unlock();
        }
    }

    // blocking
    public final void flush() {
        extractKey().ifPresent(key -> {
            while (queueHasEventByKey(key) && !Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(LOG4J_ASYNC_APPENDER_FLUSH_TIMOUT.getValueAsLong());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    Optional<String> extractKey() {
        return Optional.ofNullable((String) MDC.get(FILE_NAME));
    }

    private boolean queueHasEventByKey(String key) {
        return queue.stream().anyMatch(event -> event.getKey().equals(key));
    }

    private void logEventProcessor() {
        while (Thread.currentThread().isAlive()) {
            try {
                ApplicableEvent queuedEvent = queue.peek();
                if (queuedEvent != null) {
                    queuedEvent.apply();
                    queue.take();
                } else {
                    // Workaround to avoid CPU overhead
                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void onRemoval(RemovalNotification<String, RollingFileAppender> notification) {
        RollingFileAppender appender = notification.getValue();
        appender.close();
    }

    class ApplicableEvent {

        protected String key;

        protected LoggingEvent event;

        ApplicableEvent(String key, LoggingEvent event) {
            this.key = key;
            this.event = event;
        }

        protected void apply() {
            RollingFileAppender appender = appenderCache.getIfPresent(key);
            if (appender == null) {
                appender = createAppender(key);
                if (appender != null) {
                    appenderCache.put(key, appender);
                }
            }
            if (appender != null && event != null) {
                appender.append(event);
            }
        }

        public String getKey() {
            return key;
        }

        public LoggingEvent getEvent() {
            return event;
        }
    }

}
