package org.ow2.proactive.scheduler.common.util.logforwarder;

import org.apache.log4j.Appender;
import org.apache.log4j.AsyncAppender;
import org.apache.log4j.spi.LoggingEvent;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class LoggingEventProcessor {
    private ConcurrentHashMap<String, AsyncAppender> appenders = new ConcurrentHashMap<String, AsyncAppender>();

    public void addAppender(String loggerName, Appender appender) {
        AsyncAppender sink = appenders.get(loggerName);
        if (sink == null) {
            sink = new AsyncAppender(); // new thread is created here
            appenders.put(loggerName, sink);
        }
        sink.addAppender(appender);
    }

    public void removeAllAppenders(String loggerName) {
        AsyncAppender appender = appenders.remove(loggerName);
        if (appender != null) appender.close();
    }

    public void removeAllAppenders() {
        for(String loggerName: appenders.keySet()) {
            removeAllAppenders(loggerName);
        }
    }

    public void processEvent(LoggingEvent event) {
        String loggerName = event.getLoggerName();
        AsyncAppender sink = appenders.get(loggerName);
        if (sink != null) {
            sink.append(event);
        }
    }

}
