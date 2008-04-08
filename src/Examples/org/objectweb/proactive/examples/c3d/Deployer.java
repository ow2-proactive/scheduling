package org.objectweb.proactive.examples.c3d;

import java.io.File;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.GCMApplication;
import org.objectweb.proactive.extensions.gcmdeployment.core.GCMVirtualNode;


public class Deployer {
    protected static final Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

    GCMApplication gcmad;
    GCMVirtualNode renderer;
    GCMVirtualNode dispatcher;

    public Deployer() {
        // No args constructor
    }

    public Deployer(File applicationDescriptor) {
        try {
            ProActiveConfiguration.load();
            gcmad = PAGCMDeployment.loadApplicationDescriptor(applicationDescriptor);
            gcmad.startDeployment();
            renderer = gcmad.getVirtualNode("Renderer");
            dispatcher = gcmad.getVirtualNode("Dispatcher");
        } catch (ProActiveException e) {
            logger.error("Cannot load GCM Application Descriptor: " + applicationDescriptor, e);
        }
    }

    public Node[] getRendererNodes() {
        if (renderer == null)
            return null;

        logger.info("Waiting Renderer virtual node to be ready");
        renderer.waitReady();
        return renderer.getCurrentNodes().toArray(new Node[0]);
    }

    public Node getDispatcherNode() {
        if (dispatcher == null)
            return null;

        logger.info("Waiting Dispatcher virtual node to be ready");
        dispatcher.waitReady();
        return dispatcher.getANode();
    }

    public void shutdown() {
        gcmad.kill();
    }

    public void abortOnError(Exception e) {
        logger.error("Abort on errror", e);
        shutdown();
    }
}
