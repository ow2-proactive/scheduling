package org.objectweb.proactive.extra.infrastructuremanager.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class IMDeploymentFactory {
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.IM_DEPLOYMENT_FACTORY);

    // Attributes
    private static ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Deploy all virtual node of the proactive descriptor <I>pad</I>
     * @param imCore
     * @param padName : the name of the proactive descriptor
     * @param pad     : the procative descriptor
     */
    public static void deployAllVirtualNodes(IMCore imCore, String padName,
        ProActiveDescriptor pad) {
        if (logger.isInfoEnabled()) {
            logger.info("deployAllVirtualNodes");
        }
        IMDeploy d = new IMDeploy(imCore, padName, pad);
        executor.execute(d);
    }

    /**
     * Deploy only the virtual node <I>vnName</I>
     * @param imCore
     * @param padName : the name of the proactive descriptor
     * @param pad     : the procative descriptor
     * @param vnName  : the name of the virtual node to deploy
     */
    public static void deployVirtualNode(IMCore imCore, String padName,
        ProActiveDescriptor pad, String vnName) {
        if (logger.isInfoEnabled()) {
            logger.info("deployVirtualNode : " + vnName);
        }
        deployVirtualNodes(imCore, padName, pad, new String[] { vnName });
    }

    /**
     * Deploy only the virtual nodes <I>vnNames</I>
     * @param imCore
     * @param padName : the name of the proactive descriptor
     * @param pad     : the procative descriptor
     * @param vnNames : the name of the virtual nodes to deploy
     */
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
