package org.ow2.proactive.wrapper;

import org.apache.log4j.*;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;

/**
 * Created by root on 05/04/17.
 */
public class BootstrapLogger {

    public static Logger logger;

    // While logger is not configured and it not set with sys properties, use Console logger
    static {
        if (System.getProperty(CentralPAPropertyRepository.LOG4J.getName()) == null) {
            logger = Logger.getRootLogger();
            logger.getLoggerRepository().resetConfiguration();
            BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%m%n")));
            logger.setLevel(Level.INFO);
        }
    }
}
