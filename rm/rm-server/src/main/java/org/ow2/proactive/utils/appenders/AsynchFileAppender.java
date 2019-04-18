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

import java.util.List;
import java.util.Objects;
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

    final BlockingQueue<ApplicableEvent> queue = new LinkedBlockingQueue<>(LOG4J_ASYNC_APPENDER_BUFFER_SIZE.getValueAsInt());

    private final Set<ApplicableEvent> keys = new ConcurrentHashSet<>();

    public AsynchFileAppender() {
        super();

        Thread logEventsProcessor = new Thread(this::logEventProcessor, "FileAppenderThread");
        logEventsProcessor.setDaemon(true);
        logEventsProcessor.start();
    }

    // non blocking
    public void append(String cacheKey, LoggingEvent event) {
        synchronized (queue) {
            try {
                queue.put(new ApplicableEvent(cacheKey, event));
            } catch (InterruptedException e) {
                LOGGER.debug("Append interrupted: " + e);
            }
        }
    }

    @Override
    public void close() {
        flush();
    }

    // blocking
    public final void flush() {
        Optional<String> opKey = extractKey();
        if (opKey.isPresent()) {
            synchronized (queue) {
                Optional<ApplicableEvent> opApplicableEvent = lastEventByKey(opKey.get());
                if (opApplicableEvent.isPresent()) {
                    ApplicableEvent lastEvent = opApplicableEvent.get();
                    keys.add(lastEvent);
                    while (keys.contains(lastEvent)) {
                        try {
                            queue.wait(1000);
                        } catch (InterruptedException e) {
                            LOGGER.warn("Wait method was interrupted.", e);
                        }
                    }
                }

            }
        }
    }

    protected Optional<String> extractKey() {
        return Optional.ofNullable((String) MDC.get(FILE_NAME));
    }

    private Optional<ApplicableEvent> lastEventByKey(String key) {
        List<ApplicableEvent> collect = queue.stream()
                                             .filter(event -> event.getKey().equals(key))
                                             .collect(Collectors.toList());
        if (!collect.isEmpty()) {
            return Optional.of(collect.get(collect.size() - 1));
        } else {
            return Optional.empty();
        }
    }

    public void logEventProcessor() {
        while (true) {
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
            synchronized (queue) {
                queue.take();
                if (keys.contains(this)) {
                    keys.remove(this);
                    queue.notifyAll();
                }
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            ApplicableEvent that = (ApplicableEvent) o;
            return Objects.equals(key, that.key) && Objects.equals(event, that.event);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, event);
        }

        public String getKey() {
            return key;
        }

        public LoggingEvent getEvent() {
            return event;
        }
    }

}
