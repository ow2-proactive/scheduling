package org.objectweb.proactive.examples.nbody.common;

import java.io.File;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.gcmdeployment.API;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationDescriptor;
import org.objectweb.proactive.extra.gcmdeployment.core.GCMVirtualNode;


public class Deployer {
    protected static final Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

    GCMApplicationDescriptor gcmad;
    GCMVirtualNode workers;

    public Deployer() {
        // No args constructor
    }

    public Deployer(File applicationDescriptor) {
        try {
            gcmad = API.getGCMApplicationDescriptor(applicationDescriptor);
            gcmad.startDeployment();
            workers = gcmad.getVirtualNode("Workers");
        } catch (ProActiveException e) {
            logger.error("Cannot load GCM Application Descriptor: " + applicationDescriptor, e);
        }
    }

    public Node[] getWorkerNodes() {
        if (workers == null)
            return null;

        logger.info("Waiting Workers virtual node becomes ready");
        workers.waitReady();
        return workers.getCurrentNodes().toArray(new Node[0]);
    }

    public void shutdown() {
        gcmad.kill();
    }

    public void abortOnError(Exception e) {
        logger.error("Abort on errror", e);
        shutdown();
    }
}
