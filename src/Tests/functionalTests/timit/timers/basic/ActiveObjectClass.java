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
package functionalTests.timit.timers.basic;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.benchmarks.timit.util.CoreTimersContainer;
import org.objectweb.proactive.benchmarks.timit.util.basic.BasicTimer;
import org.objectweb.proactive.core.util.profiling.TimerWarehouse;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;


/**
 *
 * @author vbodnart
 */
public class ActiveObjectClass implements java.io.Serializable {
    private static final int customWaitTimeInMillis = 200;
    private ActiveObjectClass remoteReference;
    private ActiveObjectClass localReference;
    private String name;

    public ActiveObjectClass() {
    }

    public ActiveObjectClass(String name) {
        this.name = name;
    }

    public ActiveObjectClass(ActiveObjectClass remoteReference, ActiveObjectClass localReference, String name) {
        this.remoteReference = remoteReference;
        this.localReference = localReference;
        this.name = name;
    }

    public String getShortId() {
        return PAActiveObject.getBodyOnThis().getID().shortString();
    }

    public boolean checkRemoteAndLocalReference() {
        return (this.remoteReference != null) && (this.localReference != null);
    }

    /**
     * Checks if the total timer is started.
     * @return null if test passed
     */
    public String checkIfTotalIsStarted() {
        CoreTimersContainer c = (CoreTimersContainer) TimerWarehouse.getTimerProvidable(PAActiveObject
                .getBodyOnThis().getID());

        // Check if the timer container is null
        if (c == null) {
            return "There is no TimerProvidable registered for this body id.";
        }

        // Get the the total timer
        BasicTimer totalTimer = c.getTotalTimer();
        if (!totalTimer.isStarted()) {
            return "Problem with the Total timer it's not started.";
        }
        return null;
    }

    /**
     * Checks WaitForRequest timer.
     * @return null if test passed
     */
    public String checkIfWfrIsStopped() {
        CoreTimersContainer c = (CoreTimersContainer) TimerWarehouse.getTimerProvidable(PAActiveObject
                .getBodyOnThis().getID());

        // Check if the timer container is null
        if (c == null) {
            return "There is no TimerProvidable registered for this body id.";
        }

        // Get the the WaitForWRequest timer
        BasicTimer wfrTimer = c.getWaitForRequestTimer();

        // If the wait for request timer is started during a request test is failed
        if (wfrTimer.isStarted()) {
            return "Problem with the WaitForRequest timer it's still started during the service of a request.";
        }
        return null;
    }

    /**
     * Since it's not possible to know if the serve timer is stopped correctly
     * we just check if it's well started.
     * @return null if test passed
     */
    public String checkIfServeIsStarted() {
        CoreTimersContainer c = (CoreTimersContainer) TimerWarehouse.getTimerProvidable(PAActiveObject
                .getBodyOnThis().getID());

        // Check if the timer container is null
        if (c == null) {
            return "There is no TimerProvidable registered for this body id.";
        }

        // Get the the serve timer
        BasicTimer serveTimer = c.getServeTimer();
        if (!serveTimer.isStarted()) {
            return "Problem with the Serve timer it's not started during the service of a request.";
        }
        return null;
    }

    /**
     * Test of timers during a remote call
     * This method must be called once during the test
     * @return null if test passed
     */
    public String performSyncCallOnRemote() {
        CoreTimersContainer c = (CoreTimersContainer) TimerWarehouse.getTimerProvidable(PAActiveObject
                .getBodyOnThis().getID());

        // Check if the timer container is null
        if (c == null) {
            return "There is no TimerProvidable registered for this body id.";
        }

        // Get the the send request timer
        BasicTimer sendRequestTimer = c.getTimer(TimerWarehouse.SEND_REQUEST);
        BasicTimer beforeSerTimer = c.getTimer(TimerWarehouse.BEFORE_SERIALIZATION);
        BasicTimer serTimer = c.getTimer(TimerWarehouse.SERIALIZATION);
        BasicTimer afterSerTimer = c.getTimer(TimerWarehouse.AFTER_SERIALIZATION);
        // reset all these timers
        sendRequestTimer.reset();
        beforeSerTimer.reset();
        serTimer.reset();
        afterSerTimer.reset();
        // Perform the request and measure it
        ///////////////////////////////////////////////////
        long realSendRequestStartTime = System.nanoTime();
        this.remoteReference.syncCall();
        long realSendRequestTime = (System.nanoTime() - realSendRequestStartTime);

        ///////////////////////////////////////////////////
        // Verify that each timer was stopped
        if (sendRequestTimer.isStarted()) {
            return "Problem with the SendRequest timer, it wasn't stopped properly.";
        }
        if (beforeSerTimer.isStarted()) {
            return "Problem with the BeforeSerialization timer, it wasn't stopped properly.";
        }
        if (serTimer.isStarted()) {
            return "Problem with the Serialization timer, it wasn't stopped properly.";
        }
        if (afterSerTimer.isStarted()) {
            return "Problem with the AfterSerialization timer, it wasn't stopped properly.";
        }

        // Get timers total time     	
        long timedSendRequestTime = sendRequestTimer.getTotalTime();
        long timedBeforeSerTime = beforeSerTimer.getTotalTime();
        long timedSerTime = serTimer.getTotalTime();
        long timedAfterSerTime = afterSerTimer.getTotalTime();

        // Check for the SendRequest timer correctness    	
        if ((timedSendRequestTime <= 0) || (timedSendRequestTime > realSendRequestTime)) {
            return "Problem with the SendRequest timer, its value is incorrect.";
        }

        // Check for the BeforeSer timer correctness
        if ((timedBeforeSerTime <= 0) || (timedBeforeSerTime > realSendRequestTime) ||
            (timedBeforeSerTime > timedSendRequestTime)) {
            return "Problem with the BeforeSerialization timer, its value is incorrect.";
        }

        // Check for the Serialization timer correctness    	
        if ((timedSerTime <= 0) || (timedSerTime > realSendRequestTime) ||
            (timedSerTime > timedSendRequestTime)) {
            return "Problem with the Serialiaztion timer, its value is incorrect.";
        }

        // Check for the AfterSer timer correctness
        if ((timedAfterSerTime <= 0) || (timedAfterSerTime > realSendRequestTime) ||
            (timedAfterSerTime > timedSendRequestTime)) {
            return "Problem with the AfterSerialization timer, its value is incorrect.";
        }

        // If here the test is ok
        return null;
    }

    /**
     * Test of timers during a call on a reference of a ao tha is on the same node
     * This method must be called once during the test
     * @return null if test passed
     */
    public String performSyncCallOnLocal() {
        CoreTimersContainer c = (CoreTimersContainer) TimerWarehouse.getTimerProvidable(PAActiveObject
                .getBodyOnThis().getID());

        // Check if the timer container is null
        if (c == null) {
            return "There is no TimerProvidable registered for this body id.";
        }

        // Get the the send request timer
        BasicTimer sendRequestTimer = c.getTimer(TimerWarehouse.SEND_REQUEST);
        BasicTimer localCopyTimer = c.getTimer(TimerWarehouse.LOCAL_COPY);
        // reset all these timers (total time)
        sendRequestTimer.reset();
        localCopyTimer.reset();
        // Perform the request and measure it
        ///////////////////////////////////////////////////
        long realSendRequestStartTime = System.nanoTime();
        this.localReference.syncCall();
        long realSendRequestTime = (System.nanoTime() - realSendRequestStartTime);

        ///////////////////////////////////////////////////
        // Verify that each timer was stopped
        if (sendRequestTimer.isStarted()) {
            return "Problem with the SendRequest timer, it wasn't stopped properly.";
        }
        if (localCopyTimer.isStarted()) {
            return "Problem with the LocalCopy timer, it wasn't stopped properly.";
        }

        // Get timers total time 
        long timedSendRequestTime = sendRequestTimer.getTotalTime();
        long timedLocalCopyTime = localCopyTimer.getTotalTime();

        // Check the SendRequest timer correctness 
        if ((timedSendRequestTime <= 0) || (timedSendRequestTime > realSendRequestTime)) {
            return "Problem with the SendRequest timer, its value is incorrect.";
        }

        // Check the LocalCopy timer correctness
        if ((timedLocalCopyTime <= 0) || (timedLocalCopyTime > realSendRequestTime) ||
            (timedLocalCopyTime > timedSendRequestTime)) {
            return "Problem with the LocalCopy timer, its value is incorrect.";
        }

        // If here the test is ok
        return null;
    }

    public int syncCall() {
        try {
            Thread.sleep(customWaitTimeInMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Checks SendRequest, WaitByNecessity
     * @return null if test passed
     */
    public String performAsyncCallWithWbnOnLocal() {
        CoreTimersContainer c = (CoreTimersContainer) TimerWarehouse.getTimerProvidable(PAActiveObject
                .getBodyOnThis().getID());

        // Check if the timer container is null
        if (c == null) {
            return "There is no TimerProvidable registered for this body id.";
        }

        // Get the the send request timer
        BasicTimer sendRequestTimer = c.getTimer(TimerWarehouse.SEND_REQUEST);
        BasicTimer wbnTimer = c.getTimer(TimerWarehouse.WAIT_BY_NECESSITY);
        // reset all these timers
        sendRequestTimer.reset();
        wbnTimer.reset();

        // Perform the request and measure it
        ///////////////////////////////////////////////////
        long realSendRequestStartTime = System.nanoTime();
        IntWrapper wrapper = this.localReference.asyncCall();
        long realSendRequestTime = (System.nanoTime() - realSendRequestStartTime);

        // This call is blocking until the futur becomes available
        // we need to measure the wbn time that will be approx equal customWaitTimeInMillis
        long realWbnStartTime = System.nanoTime();
        wrapper.intValue();
        long realWbnTime = (System.nanoTime() - realWbnStartTime);

        ///////////////////////////////////////////////////
        // Verify that each timer was stopped
        if (sendRequestTimer.isStarted()) {
            return "Problem with the SendRequest timer, it wasn't stopped properly.";
        }
        if (wbnTimer.isStarted()) {
            return "Problem with the WaitByNecessity timer, it wasn't stopped properly.";
        }

        // Get timers total time 
        long timedSendRequestTime = sendRequestTimer.getTotalTime();
        long timedWbnTime = wbnTimer.getTotalTime();

        // Check the SendRequest timer correctness 
        if ((timedSendRequestTime <= 0) || (timedSendRequestTime > realSendRequestTime)) {
            return "Problem with the SendRequest timer, its value is incorrect.";
        }

        // Check the WaitByNecessity timer correctness
        if ((timedWbnTime <= 0) || (timedWbnTime > realWbnTime)) {
            return "Problem with the WaitByNecessity timer, its value is incorrect.";
        }
        return null;
    }

    public IntWrapper asyncCall() {
        try {
            Thread.sleep(customWaitTimeInMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new IntWrapper(0);
    }

    /**
     * Since an instance of this class could be active this method
     * should be called to terminate its activity.
     */
    public void terminate() {
        this.localReference = null;
        this.remoteReference = null;
        Body b = PAActiveObject.getBodyOnThis();
        //b.getFuturePool().disableAC();
        PAActiveObject.terminateActiveObject(true);
    }

    /**
     * @return The name of this object
     */
    @Override
    public String toString() {
        return name;
    }
}
