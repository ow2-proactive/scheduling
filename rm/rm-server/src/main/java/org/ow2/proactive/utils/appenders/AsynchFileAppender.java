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
import static org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties.LOG4J_ASYNC_APPENDER_POOL_SIZE;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import org.apache.log4j.*;
import org.apache.log4j.spi.LoggingEvent;
import org.eclipse.jetty.util.ConcurrentHashSet;


public class AsynchFileAppender extends FileAppender {

    private static final Logger LOGGER = Logger.getLogger(AsynchFileAppender.class);

    protected static final ArrayList<BlockingQueue<ApplicableEvent>> queues;

    private static final ArrayList<Thread> pool;

    private static final Set<ApplicableEvent> keys = new ConcurrentHashSet<>();

    static {
        queues = new ArrayList<>();
        for (int i = 0; i < LOG4J_ASYNC_APPENDER_POOL_SIZE.getValueAsInt(); ++i) {
            queues.add(new LinkedBlockingQueue<>(LOG4J_ASYNC_APPENDER_BUFFER_SIZE.getValueAsInt()));
        }

        pool = new ArrayList<>();
        for (int i = 0; i < LOG4J_ASYNC_APPENDER_POOL_SIZE.getValueAsInt(); ++i) {
            Thread logEventsProcessor = new Thread(new EventProcessor(queues.get(i)), "FileAppenderThread-" + i);
            logEventsProcessor.setDaemon(true);
            logEventsProcessor.start();
            pool.add(logEventsProcessor);
        }

    }

    // non blocking
    public void append(String cacheKey, LoggingEvent event) {
        int indexOfQueue = getIndexOfQueue(cacheKey);
        try {
            synchronized (queues) {
                queues.get(indexOfQueue).put(new ApplicableEvent(cacheKey, event));
            }
        } catch (InterruptedException e) {
            LOGGER.warn("Queue put is interrupted.");
        }
    }

    protected int getIndexOfQueue(String cacheKey) {
        return cacheKey.hashCode() % queues.size();
    }

    protected Optional<String> extractKey() {
        return Optional.ofNullable((String) MDC.get(FILE_NAME));
    }

    // blocking
    final public void flush() {
        Optional<String> opKey = extractKey();
        if (opKey.isPresent()) {
            synchronized (queues) {
                Optional<ApplicableEvent> opApplicableEvent = lastEventByKey(opKey.get());
                if (opApplicableEvent.isPresent()) {
                    ApplicableEvent lastEvent = opApplicableEvent.get();
                    keys.add(lastEvent);
                    synchronized (lastEvent) {
                        while (keys.contains(lastEvent)) {
                            try {
                                lastEvent.wait();
                            } catch (InterruptedException e) {
                                LOGGER.warn("Wait method was interrupted.", e);
                            }
                        }
                    }
                }

            }
        }
    }

    private Optional<ApplicableEvent> lastEventByKey(String key) {
        List<ApplicableEvent> collect = queues.get(getIndexOfQueue(key))
                                              .stream()
                                              .filter(event -> event.getKey().equals(key))
                                              .collect(Collectors.toList());
        if (!collect.isEmpty()) {
            return Optional.of(collect.get(collect.size() - 1));
        } else {
            return Optional.empty();
        }
    }

    class ApplicableEvent {

        protected String key;

        protected LoggingEvent event;

        ApplicableEvent(String key, LoggingEvent event) {
            this.key = key;
            this.event = event;
        }

        protected void apply() {
            RollingFileAppender appender = createAppender(key);
            if (appender != null && event != null) {
                appender.append(event);
                appender.close();
            }
        }

        final void applyAndPossiblyClose() throws InterruptedException {
            apply();
            synchronized (queues) {
                queues.get(getIndexOfQueue(getKey())).take();
                if (keys.contains(this)) {
                    synchronized (this) {
                        keys.remove(this);
                        this.notifyAll();
                    }
                }
            }
        }

        public String getKey() {
            return key;
        }

        public LoggingEvent getEvent() {
            return event;
        }
    }

    private static class EventProcessor implements Runnable {

        private BlockingQueue<ApplicableEvent> queue;

        EventProcessor(BlockingQueue<ApplicableEvent> queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            while (true) {
                synchronized (queues) {
                    try {
                        ApplicableEvent queuedEvent = queue.peek();
                        if (queuedEvent != null) {
                            queuedEvent.applyAndPossiblyClose();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
