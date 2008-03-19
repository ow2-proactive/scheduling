//@snippet-start cma_init_full
package org.objectweb.proactive.examples.userguide.cmagent.initialized;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.util.wrapper.LongWrapper;
import org.objectweb.proactive.examples.doctor.Cure;
import org.objectweb.proactive.examples.userguide.cmagent.simple.CMAgent;


public class CMAgentInitialized extends CMAgent implements InitActive, RunActive, EndActive {
    private long lastRequestDuration;

    public void initActivity(Body body) {
        System.out.println("### Started Active object " + body.getMBean().getName() + " on " +
            body.getMBean().getNodeUrl());
    }

    public void runActivity(Body body) {
        Service service = new Service(body);
        long currentRequestDuration = 0;
        while (body.isActive()) {
            service.waitForRequest(); // block until a request is received 
            currentRequestDuration = System.currentTimeMillis();
            service.serveOldest(); //server the requests in a FIFO manner
            //calculate the total duration
            currentRequestDuration = System.currentTimeMillis() - currentRequestDuration;
            // an intermediary variable is used so 
            // when calling getLastRequestServeTime() 
            // we get the first value before the last request
            // i.e when calling getLastRequestServeTime
            // the lastRequestDuration is update with the 
            // value of the getLastRequestServeTime call 
            // AFTER the previous calculated value has been returned
            lastRequestDuration = currentRequestDuration;
        }
    }

    public void endActivity(Body body) {
        System.out.println("### Active object stopped.");
    }

    public LongWrapper getLastRequestServeTime() {
        // user wrappers for primitive types
        // so the calls are asynchronous
        return new LongWrapper(lastRequestDuration);
    }

}
//@snippet-end cma_init_full