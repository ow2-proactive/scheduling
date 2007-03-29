package org.objectweb.proactive.extra.scheduler.exception;

import org.objectweb.proactive.core.exceptions.NonFunctionalException;
import org.objectweb.proactive.core.exceptions.manager.NFEListener;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import static org.objectweb.proactive.extra.scheduler.Scheduler.logger;
public class NFEHandler implements NFEListener, java.io.Serializable {
    private String source;

    public NFEHandler() {
    }

    public NFEHandler(String NFESource) {
        source = NFESource;
    }

    public boolean handleNFE(NonFunctionalException e) {
        //TODO: An optimal solution would be to handle the exception for eg if it is due to the fact that the user cant be reached from the user api, the result might need to be sent back tot the core or cached in the uaerapi
        logger.info("##" + source + "had an  NFE");
        if (logger.isDebugEnabled()) {
            logger.debug(
                "follows is a print out of the stack trace, warning, hte exception is caught , this is just a printout" +
                ProActiveLogger.getStackTraceAsString(e));
        }
        return true;
    }
}
