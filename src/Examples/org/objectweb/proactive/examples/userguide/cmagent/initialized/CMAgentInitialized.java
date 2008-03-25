
package org.objectweb.proactive.examples.userguide.cmagent.initialized;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.util.wrapper.LongWrapper;
import org.objectweb.proactive.examples.userguide.cmagent.simple.CMAgent;


public class CMAgentInitialized extends CMAgent implements InitActive, RunActive, EndActive {
    private long lastRequestDuration;
    private long startTime;
    private long requestsServed = 0;

    public void initActivity(Body body) {
        System.out.println("### Started Active object " + body.getMBean().getName() + " on " +
            body.getMBean().getNodeUrl());
        //get start time
        startTime = System.currentTimeMillis();
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
            //increment the number of requests served
            requestsServed++;
        }
    }

    public void endActivity(Body body) {
        long runningTime = System.currentTimeMillis() - startTime;
        System.out.println("### You have killed the active object. The final" + " resting place is on " +
            body.getNodeURL() + "\n### It has faithfully served " + requestsServed + " requests " +
            "and has been an upstanding active object for " + runningTime + " ms ");
    }

    public LongWrapper getLastRequestServeTime() {
        // user wrappers for primitive types
        // so the calls are asynchronous
        return new LongWrapper(lastRequestDuration);
    }

}
