package org.objectweb.proactive.extra.infrastructuremanager.test.util;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class TestLogger {
    // voir la classe org.objectweb.proactive.core.util.log.Loggers 
    /*-----------------------------------------------------------------------
    //InfrastructureManager loggers
    static final public String IM                      = "IM";
    static final public String IMTEST                = IM+".IMTest";
     *-----------------------------------------------------------------------
     */

    // et le fichier im-log4j  
    public static void main(String[] args) {
        Logger logger = ProActiveLogger.getLogger(Loggers.IM);
        Logger loggerTest = ProActiveLogger.getLogger(Loggers.IM_TEST);

        System.out.println("Logger name : " + logger.getName());
        System.out.println("Logger test name : " + loggerTest.getName());

        if (logger.isInfoEnabled()) {
            logger.info("Info mode actif");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Debug mode actif");
        }

        if (loggerTest.isInfoEnabled()) {
            loggerTest.info("Test Info mode actif");
        }
        if (loggerTest.isDebugEnabled()) {
            loggerTest.debug("Test Debug mode actif");
        }

        try {
        } catch (Exception e) {
            logger.fatal("Fatal mode actif");
            logger.fatal("Fatal mode actif", e);
        }
    }
}
