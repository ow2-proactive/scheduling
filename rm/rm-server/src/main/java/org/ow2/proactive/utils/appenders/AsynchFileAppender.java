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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.*;
import org.apache.log4j.spi.LoggingEvent;


public class AsynchFileAppender extends FileAppender {

    private static final Logger LOGGER = Logger.getLogger(AsynchFileAppender.class);

    final BlockingQueue<ApplicableEvent> queue = new LinkedBlockingQueue<>(LOG4J_ASYNC_APPENDER_BUFFER_SIZE.getValueAsInt());

    public AsynchFileAppender() {
        super();

        Thread logEventsProcessor = new Thread(this::logEventProcessor, "logEventsProcessor");
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
    final void flush() {
        extractKey().ifPresent(key -> {
            while (queueHasEventByKey(key) && !Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(LOG4J_ASYNC_APPENDER_FLUSH_TIMOUT.getValueAsInt());
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
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
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

        public String getKey() {
            return key;
        }

        public LoggingEvent getEvent() {
            return event;
        }
    }

}
