package org.objectweb.proactive.core.util.log;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.spi.LoggingEvent;

public class HostNameConsoleAppender extends ConsoleAppender {

    public void append(LoggingEvent event) {
        super.append(event);
    }
    

}
