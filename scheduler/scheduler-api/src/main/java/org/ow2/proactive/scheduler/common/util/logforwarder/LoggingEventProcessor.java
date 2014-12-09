package org.ow2.proactive.scheduler.common.util.logforwarder;

import org.apache.log4j.Appender;
import org.apache.log4j.Category;
import org.apache.log4j.Hierarchy;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.RootLogger;


public class LoggingEventProcessor {
    Hierarchy h = new NoWarningHierarchy();

    public void addAppender(String loggerName, Appender appender) {
        h.getLogger(loggerName).addAppender(appender);
    }

    public void removeAllAppenders(String loggerName) {
        h.getLogger(loggerName).removeAllAppenders();
    }

    public void removeAllAppenders() {
        h.getRootLogger().removeAllAppenders();
    }

    public void processEvent(LoggingEvent event) {
        h.getLogger(event.getLoggerName()).callAppenders(event);
    }

    private static class NoWarningHierarchy extends Hierarchy {
        public NoWarningHierarchy() {
            super(new RootLogger(Level.ALL));
        }

        @Override
        public void emitNoAppenderWarning(Category cat) {

        }
    }
}
