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

import java.io.File;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.AsyncAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;


/**
 * 
 * An appender that redirects logging events to different files
 * depending on "filename" property in log4j context.
 * 
 * Is used to put server logs for tasks and jobs into files with 
 * different names.
 *
 */
public class FileAppender extends WriterAppender {

    public static final String FILE_NAME = "filename";

    private String maxFileSize;

    protected static ConcurrentHashMap<String, RollingFileAppender> appenderCache = new ConcurrentHashMap<>();

    protected static LinkedBlockingQueue<QueuedLoggingEvent> loggingQueue = new LinkedBlockingQueue<>(PAResourceManagerProperties.LOG4J_ASYNC_APPENDER_BUFFER_SIZE.getValueAsInt());

    private static Thread logEventsProcessor;

    static {
        logEventsProcessor = new Thread(new LogEventsProcessor(), "FileAppenderThread");
        logEventsProcessor.setDaemon(true);
    }

    protected String filesLocation;

    public FileAppender() {
        setLayout(new PatternLayout("[%d{ISO8601} %-5p] %m%n"));
        fetchLayoutFromRootLogger();
    }

    @Override
    public void append(LoggingEvent event) {
        Object value = MDC.get(FILE_NAME);
        if (value != null) {
            append(value.toString(), event);
        }
    }

    public void append(String cacheKey, LoggingEvent event) {
        if (PAResourceManagerProperties.LOG4J_ASYNC_APPENDER_ENABLED.getValueAsBoolean()) {
            if (PAResourceManagerProperties.LOG4J_ASYNC_APPENDER_CACHE_ENABLED.getValueAsBoolean()) {
                appenderCache.computeIfAbsent(cacheKey, k -> createAppender(k));
            }
            try {
                loggingQueue.put(new QueuedLoggingEvent(cacheKey, event));
            } catch (InterruptedException e) {
                Logger.getRootLogger().warn("Interrupted while logging on " + cacheKey);
            }
        } else {
            RollingFileAppender appender = createAppender(cacheKey);
            appender.append(event);
            appender.close();
        }
    }

    private RollingFileAppender createAppender(String cacheKey) {
        RollingFileAppender appender;
        String fileName = cacheKey;
        if (filesLocation != null) {
            fileName = filesLocation + File.separator + fileName;
        }
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                FileUtils.forceMkdirParent(file);
                FileUtils.touch(file);
            }

            appender = new RollingFileAppender(getLayout(), fileName, true);
            appender.setMaxBackupIndex(1);
            if (maxFileSize != null) {
                appender.setMaxFileSize(maxFileSize);
            }
        } catch (Exception e) {
            Logger.getRootLogger()
                  .error("Error when creating logger : " + cacheKey + " logging will be disabled for this context", e);
            appender = new RollingFileAppender();

        }
        return appender;
    }

    private boolean fetchLayoutFromRootLogger() {
        // trying to get a layout from log4j configuration
        Enumeration<?> en = Logger.getRootLogger().getAllAppenders();
        if (en != null && en.hasMoreElements()) {
            Appender app = (Appender) en.nextElement();
            if (app != null && app.getLayout() != null) {
                if (app instanceof AsyncAppender) {
                    Enumeration<?> attachedAppenders = ((AsyncAppender) app).getAllAppenders();
                    if (attachedAppenders != null && attachedAppenders.hasMoreElements()) {
                        Appender attachedApp = (Appender) attachedAppenders.nextElement();
                        return setLayoutUsingAppender(attachedApp);
                    }
                } else {
                    return setLayoutUsingAppender(app);
                }
            }
        }
        return false;
    }

    private boolean setLayoutUsingAppender(Appender attachedApp) {
        if (attachedApp.getLayout() != null) {
            Logger.getRootLogger().trace("Retrieved layout from log4j configuration");
            setLayout(attachedApp.getLayout());
            return true;
        }
        return false;
    }

    @Override
    public void close() {
        Object fileName = MDC.get(FILE_NAME);
        if (fileName != null) {
            loggingQueue.stream()
                        .filter(event -> event.getKey().equals(fileName))
                        .forEach(cachedEvent -> cachedEvent.apply());
            loggingQueue.removeIf(event -> event.getKey().equals(fileName));

            if (PAResourceManagerProperties.LOG4J_ASYNC_APPENDER_CACHE_ENABLED.getValueAsBoolean()) {
                RollingFileAppender cachedAppender = appenderCache.remove(fileName);
                if (cachedAppender != null) {
                    cachedAppender.close();
                }
            }
        }
    }

    @Override
    public boolean requiresLayout() {
        return true;
    }

    public String getFilesLocation() {
        return filesLocation;
    }

    public void setFilesLocation(String filesLocation) {
        this.filesLocation = filesLocation;
    }

    public String getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(String maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public static boolean doesCacheContain(String key) {
        return appenderCache.containsKey(key);
    }

    private RollingFileAppender getAppender(String key) {
        RollingFileAppender appender;

        if (PAResourceManagerProperties.LOG4J_ASYNC_APPENDER_CACHE_ENABLED.getValueAsBoolean()) {
            appender = appenderCache.get(key);
        } else {
            appender = createAppender(key);
        }
        return appender;
    }

    /**
     * An event waiting to be processed
     */
    private class QueuedLoggingEvent {

        private String cacheKey;

        private LoggingEvent event;

        public QueuedLoggingEvent(String cacheKey, LoggingEvent event) {
            this.cacheKey = cacheKey;
            this.event = event;
        }

        public String getKey() {
            return cacheKey;
        }

        public LoggingEvent getEvent() {
            return event;
        }

        public synchronized void apply() {
            RollingFileAppender appender = getAppender(cacheKey);

            if (appender != null && event != null) {
                appender.append(event);
            }
            event = null;

            if (!PAResourceManagerProperties.LOG4J_ASYNC_APPENDER_CACHE_ENABLED.getValueAsBoolean()) {
                appender.close();
            }
        }
    }

    /**
     * Singleton thread processing all logging events
     */
    private static class LogEventsProcessor implements Runnable {

        @Override
        public void run() {
            try {
                while (Thread.currentThread().isAlive()) {
                    QueuedLoggingEvent queuedEvent = loggingQueue.take();
                    queuedEvent.apply();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
