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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.*;
import org.ow2.proactive.utils.ThreadPoolRouter;


public abstract class AsynchFileAppender extends FileAppender {

    private static final Logger LOGGER = Logger.getLogger(AsynchFileAppender.class);

    static BlockingQueue<ApplicableEvent> loggingQueue = new LinkedBlockingQueue<>(LOG4J_ASYNC_APPENDER_BUFFER_SIZE.getValueAsInt());

    private static ThreadPoolRouter pool = new ThreadPoolRouter(LOG4J_ASYNC_APPENDER_POOL_SIZE.getValueAsInt());

    static {
        Thread logEventsProcessor = new Thread(AsynchFileAppender::logEventsProcessor, "FileAppenderThread");
        logEventsProcessor.setDaemon(true);
        logEventsProcessor.start();
    }

    @Override
    public void close() {
        Object fileName = MDC.get(FILE_NAME);
        if (fileName != null) {
            while (loggingQueue.stream().anyMatch(event -> event.getKey().equals(fileName))) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    LOGGER.warn("Close method was interrupted.", e);
                }
            }
        }
        super.close();
    }

    /**
     * An event waiting to be processed
     */
    public interface ApplicableEvent {

        String getKey();

        void apply();
    }

    private static void logEventsProcessor() {

        while (Thread.currentThread().isAlive()) {
            try {
                ApplicableEvent queuedEvent = loggingQueue.take();
                pool.route(queuedEvent.getKey(), queuedEvent::apply);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                Logger.getRootLogger().warn("Error while logging", e);
            }
        }

    }

}
