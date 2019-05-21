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

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.spi.LoggingEvent;


public class AsynchChachedFileAppender extends AsynchFileAppender {

    private static final Logger LOGGER = Logger.getLogger(AsynchChachedFileAppender.class);

    private ConcurrentHashMap<String, RollingFileAppender> appenderCache = new ConcurrentHashMap<>();

    @Override
    public void append(String cacheKey, LoggingEvent event) {
        try {
            isAppending.lock();
            appenderCache.computeIfAbsent(cacheKey, this::createAppender);
            queue.put(new ApplicableEvent(cacheKey, event));
        } catch (InterruptedException e) {
            LOGGER.warn("Queue put is interrupted.");
        } finally {
            isAppending.unlock();
        }
    }

    @Override
    public void close() {
        try {
            isClosing.lock();
            super.flush();
            Optional<String> opKey = extractKey();
            if (opKey.isPresent()) {
                RollingFileAppender appender = appenderCache.remove(opKey.get());
                if (appender != null) {
                    appender.close();
                }
            }
        } finally {
            isClosing.unlock();
        }
    }

    class ApplicableEvent extends AsynchFileAppender.ApplicableEvent {

        ApplicableEvent(String key, LoggingEvent event) {
            super(key, event);
        }

        @Override
        protected void apply() {
            RollingFileAppender appender = appenderCache.get(key);
            if (appender != null) {
                appender.append(event);
            }
        }

    }

    public boolean doesCacheContain(String fileName) {
        return appenderCache.containsKey(fileName);
    }

    public int numberOfAppenders() {
        return appenderCache.size();
    }

}
