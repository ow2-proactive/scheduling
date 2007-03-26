package org.objectweb.proactive.extra.infrastructuremanager.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class IMDeploymentFactory {
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.IM_DEPLOYMENT_FACTORY);

    // Attributes
    private static ExecutorService executor = Executors.newCachedThreadPool();

    /**
     *
     * @param imCore
     * @param pad
     * @return
     * @throws ProActiveException
     */
    public static void deployAllVirtualNodes(IMCore imCore, String padName,
        ProActiveDescriptor pad) {
        if (logger.isInfoEnabled()) {
            logger.info("deployAllVirtualNodes");
        }
        IMDeploy d = new IMDeploy(imCore, padName, pad);
        executor.execute(d);
    }

    public static void deployVirtualNode(IMCore imCore, String padName,
        ProActiveDescriptor pad, String vnName) {
        if (logger.isInfoEnabled()) {
            logger.info("deployVirtualNode : " + vnName);
        }
        IMDeploy d = new IMDeploy(imCore, padName, pad, new String[] { vnName });
        executor.execute(d);
    }

    public static void deployVirtualNodes(IMCore imCore, String padName,
        ProActiveDescriptor pad, String[] vnNames) {
        if (logger.isInfoEnabled()) {
            String concatVnNames = "";
            for (String vnName : vnNames) {
                concatVnNames += (vnName + " ");
            }
            logger.info("deployVirtualNodes : " + concatVnNames);
        }
        IMDeploy d = new IMDeploy(imCore, padName, pad, vnNames);
        executor.execute(d);
    }
}
