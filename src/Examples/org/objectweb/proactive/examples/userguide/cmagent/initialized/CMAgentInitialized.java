//@snippet-start cma_init_full
package org.objectweb.proactive.examples.userguide.cmagent.initialized;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.examples.userguide.cmagent.simple.CMAgent;


public class CMAgentInitialized extends CMAgent implements InitActive, EndActive {

    public void initActivity(Body body) {
        System.out.println("--> Started Active object " + body.getMBean().getName() + " on " +
            body.getMBean().getNodeUrl());
    }

    public void endActivity(Body body) {
        System.out.println("Active object stopped.");
    }

}
//@snippet-end cma_init_full