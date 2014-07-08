package org.ow2.proactive.scheduler.common.util.logforwarder;

import org.apache.log4j.Appender;
import org.apache.log4j.AsyncAppender;
import org.apache.log4j.spi.LoggingEvent;

import java.util.concurrent.ConcurrentHashMap;


public class LoggingEventProcessor {
    private ConcurrentHashMap<String, AsyncAppender> appenders = new ConcurrentHashMap<String, AsyncAppender>();

    public void addAppender(String loggerName, Appender appender) {
        appenders.putIfAbsent(loggerName, new AsyncAppender());
        AsyncAppender sink = appenders.get(loggerName);
        if (sink != null) {
            sink.addAppender(appender);
        }
    }

    public void removeAppender(String loggerName, Appender appender) {
        AsyncAppender sink = appenders.get(loggerName);
        if (sink != null) {
            sink.removeAppender(appender);
        }
    }

    public void removeAllAppenders(String loggerName) {
        appenders.remove(loggerName);
    }

    public void processEvent(LoggingEvent event) {
        String loggerName = event.getLoggerName();
        AsyncAppender sink = appenders.get(loggerName);
        if (sink != null) {
            sink.append(event);
        }
    }

}
