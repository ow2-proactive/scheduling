package org.objectweb.proactive.examples.userguide.cmagent.migration;

import java.io.Serializable;

import org.objectweb.proactive.api.PAMobileAgent;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.examples.userguide.cmagent.initialized.CMAgentInitialized;


public class CMAgentMigrator extends CMAgentInitialized implements Serializable {
    public void migrateTo(Node whereTo) {
        try {
            //TODO 1. Migrate the active object to the Node received as parameter
            //should be the last call in this method
            //instructions after a call to PAMobileAgent.migrateTo are NOT executed 
            PAMobileAgent.migrateTo(whereTo);
        } catch (ProActiveException moveExcep) {
            System.err.println(moveExcep.getMessage());
        }
    }
}
