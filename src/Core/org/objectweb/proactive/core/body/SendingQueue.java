/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.body;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.objectweb.proactive.core.body.proxy.AbstractProxy;
import org.objectweb.proactive.core.body.proxy.RequestToSend;
import org.objectweb.proactive.core.body.proxy.SendingQueueProxy;
import org.objectweb.proactive.core.body.proxy.UniversalBodyProxy;
import org.objectweb.proactive.core.mop.StubObject;


/**
 * This class handles the FOS requests which are ready to be sent. There is one instance of this
 * class per body.
 */
public class SendingQueue {

    private static final String ANY_PARAMETERS = "any-parameters";

    /* Contains a set of SendingQueueProxy according to their AbstractProxy they are attached to */
    private Map</* AbstractProxy hashCode */Integer, SendingQueueProxy> sqProxies;

    /* Contains the next requests to send (FIFO). Polled by the SendingThreadPool */
    private BlockingQueue<RequestToSend> rtsQueue;

    /* The SendingThreadPool which handles the sending of those RequestToSend */
    private SendingThreadPool sendingThreadPool;

    //
    // ------ CONSTRUCTORS ------------------------------------------
    //

    public SendingQueue() {
        this.sqProxies = new HashMap<Integer, SendingQueueProxy>();
        this.rtsQueue = new LinkedBlockingQueue<RequestToSend>();
    }

    //
    // ------ PUBLIC METHODS ----------------------------------------
    //

    /**
     * This method is invoked before sending a request in a FOS way. It permits to start the
     * threadPool only if it is needed.
     */
    public void wakeUpThreadPool() {
        if (sendingThreadPool == null) {
            sendingThreadPool = new SendingThreadPool(this);

            // Check if there is pending requests to send
            Iterator<SendingQueueProxy> it = sqProxies.values().iterator();
            while (it.hasNext()) {
                RequestToSend rts = it.next().getLastSent();
                if (rts != null) {
                    try {
                        put(rts);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Stops the {@link SendingThreadPool}
     */
    public void stop() {
        if (sendingThreadPool != null) {
            sendingThreadPool.stop();
        }
    }

    // -- MANAGING THE QUEUES --

    /**
     * Put a {@link RequestToSend} in the main queue to send it ASAP. This method is invoked by the
     * {@link SendingQueueProxy}. There is never two {@link RequestToSend} coming from the same
     * {@link SendingQueueProxy} at the same time in this queue. Otherwise big troubles regarding
     * FIFO Point-To-Point ordering should occur.
     */
    public void put(RequestToSend rts) throws InterruptedException {
        this.sendingThreadPool.wakeUp();
        rtsQueue.put(rts);
    }

    /**
     * Invoked by a {@link SendingThreadPool}'s thread to retrieve the next {@link RequestToSend}.
     * 
     * @param timeout
     * @param timeUnit
     * @return
     * @throws InterruptedException
     */
    public RequestToSend poll(long timeout, TimeUnit timeUnit) throws InterruptedException {
        return rtsQueue.poll(timeout, timeUnit);
    }

    /**
     * Invoked before each standard sending to avoid Causal Ordering disruption, or before a
     * migration to avoid un-useful serialization of {@link RequestToSend}
     * 
     * @throws InterruptedException
     */
    public void waitForAllSendingQueueEmpty() throws InterruptedException {

        // Sequentially waits on all the sendingQueueProxies
        Iterator<SendingQueueProxy> it = sqProxies.values().iterator();

        while (it.hasNext()) {
            it.next().waitForEmpty();
        }
    }

    /**
     * Retrieve the {@link SendingQueueProxy} attached to a given {@link UniversalBodyProxy}
     * 
     * @param destProxy
     * @return
     */
    public SendingQueueProxy getSendingQueueProxyFor(AbstractProxy destProxy) {
        SendingQueueProxy sqProxy = sqProxies.get(destProxy.hashCode());

        if (sqProxy == null) {
            sqProxy = new SendingQueueProxy(this);
            sqProxies.put(destProxy.hashCode(), sqProxy);
        }

        return sqProxy;
    }

    public SendingQueueProxy getSendingQueueProxyFor(Object activeObject) {
        AbstractProxy destProxy = (AbstractProxy) ((StubObject) activeObject).getProxy();

        return getSendingQueueProxyFor(destProxy);
    }

    // -- MANAGING FORGET ON SEND METHOD DECLARATIONS --

    /**
     * Declares 'methodName' as 'ForgetOnSend'
     */
    public void addFosRequest(Object obj, String methodName) {
        this.getSendingQueueProxyFor(obj).addFosRequest(obj, methodName, ANY_PARAMETERS);
    }

    /**
     * Declares 'methodName' as not 'ForgetOnSend'
     */
    public void removeFosRequest(Object obj, String methodName) {
        this.getSendingQueueProxyFor(obj).removeFosRequest(methodName);
    }

    /**
     * Check if methodName has been declared as 'ForgetOnSend' or not
     */
    public boolean isFosRequest(Object obj, String methodName) {
        return this.getSendingQueueProxyFor(obj).isFosRequest(methodName);
    }

    //
    // ------ DEBUGGING METHODS -----------------------------------------
    //
    //    private static java.io.BufferedWriter bw = null;
    //
    //    public static void msg(String msg) {
    //        try {
    //            if (bw == null) {
    //                bw = new java.io.BufferedWriter(new java.io.FileWriter("myLog", true));
    //                bw.write("\n\nStart\n");
    //            }
    //            bw.write(msg + "\n");
    //            bw.flush();
    //        } catch (java.io.IOException e) {
    //        }
    //    }
}
