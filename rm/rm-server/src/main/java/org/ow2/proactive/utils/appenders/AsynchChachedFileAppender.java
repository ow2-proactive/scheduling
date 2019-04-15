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

import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.*;
import org.apache.log4j.spi.LoggingEvent;


public class AsynchChachedFileAppender extends AsynchFileAppender {

    private static ConcurrentHashMap<String, RollingFileAppender> appenderCache = new ConcurrentHashMap<>();

    public void append(String cacheKey, LoggingEvent event) {
        appenderCache.computeIfAbsent(cacheKey, this::createAppender);
        try {
            loggingQueue.put(new QueuedLoggingEvent(cacheKey, event, false));
        } catch (InterruptedException e) {
            Logger.getRootLogger().warn("Interrupted while logging on " + cacheKey);
        }
    }

    public boolean doesCacheContain(String fileName) {
        return appenderCache.containsKey(fileName);
    }

    @Override
    public void close() {
        Object fileName = MDC.get(FILE_NAME);
        if (fileName != null) {
            try {
                loggingQueue.put(new QueuedLoggingEvent((String) fileName, null, true));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            super.close();
        }
    }

    private void closeAppender(String key) {
        RollingFileAppender cachedAppender = appenderCache.remove(key);
        if (cachedAppender != null) {
            cachedAppender.close();
        }
    }

    protected class QueuedLoggingEvent implements ApplicableEvent {

        private String key;

        private LoggingEvent event;

        private boolean isClosing;

        QueuedLoggingEvent(String cacheKey, LoggingEvent event, boolean isClosing) {
            this.key = cacheKey;
            this.event = event;
            this.isClosing = isClosing;
        }

        @Override
        public String getKey() {
            return key;
        }

        public synchronized void apply() {
            RollingFileAppender appender = appenderCache.get(key);

            if (isClosing) {
                closeAppender(key);
            } else {

                if (appender != null && event != null) {
                    appender.append(event);
                }

            }
        }
    }
}
