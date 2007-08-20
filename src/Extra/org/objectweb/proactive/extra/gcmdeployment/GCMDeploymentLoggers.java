package org.objectweb.proactive.extra.gcmdeployment;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class GCMDeploymentLoggers {
    static final public String GCM_DEPLOYMENT = Loggers.DEPLOYMENT + ".GCMD";
    static final public String GCM_APPLICATION = Loggers.DEPLOYMENT + ".GCMA";
    static final public Logger GCMD_LOGGER = ProActiveLogger.getLogger(GCM_DEPLOYMENT);
    static final public Logger GCMA_LOGGER = ProActiveLogger.getLogger(GCM_APPLICATION);
}
