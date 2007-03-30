package org.objectweb.proactive.extra.infrastructuremanager.core;

import java.util.ArrayList;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.infrastructuremanager.dataresource.IMNode;


public class IMActivityNode implements Runnable {
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.IM_ACTIVITY_NODES);
    private boolean actif;
    private long wait;
    private IMCore imCore;

    public IMActivityNode(IMCore imCore) {
        this.imCore = imCore;
        this.actif = true;
        this.wait = 30000;
    }

    public void stop() {
        this.actif = false;
    }

    public void run() {
        while (actif) {
            if (logger.isInfoEnabled()) {
                logger.info("search down nodes");
            }
            ArrayList<IMNode> imNodes = this.imCore.getListAllNodes();
            for (IMNode imNode : imNodes) {
                String nodeURL;
                Node node;
                try {
                    nodeURL = imNode.getNodeInformation().getURL();
                    node = NodeFactory.getNode(nodeURL);
                } catch (NodeException e) {
                    this.imCore.nodeIsDown(imNode);
                }
            }

            try {
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
