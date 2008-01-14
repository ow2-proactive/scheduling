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
package org.objectweb.proactive.benchmarks.timit.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.benchmarks.timit.util.basic.BasicTimer;
import org.objectweb.proactive.benchmarks.timit.util.basic.TimItBasicReductor;
import org.objectweb.proactive.benchmarks.timit.util.service.TimItTechnicalService;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.MetaObjectFactory;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.profiling.TimerProvidable;
import org.objectweb.proactive.core.util.profiling.TimerWarehouse;


/**
 * This class represents a container of timers, an instance of this class is
 * attached to a body. Timers follows a predefined hierarchy :<p>
 * <ul>
 *
 *   <li><p> Total timer is decomposed into Serve and WaitForRequest timers.
 *   </p></li>
 *
 *   <li><p> Serve timer is decomposed into a list of MethodTimers.
 *   </p></li>
 *
 *   <li><p> MethodTimer corresponds to the name of a public, non-static and non-final
 *                    method that can potentially be served as a request.
 *                    Note that equals, hashCode and toString methods are ignored.
 *           MethodTimer is decomposed into UserComputation, SendRequest, SendReply, GroupOneWayCall, GroupAsyncCall timers.
 *   </p></li>
 *
 *   <li><p> SendRequest timer is decomposed into LocalCopy, BeforeSerialization, Serialization and AfterSerialization timers.
 *   </p></li>
 *
 * </ul>
 *
 * <p> The instanciation of this class should be done when a body is created.
 * If the <code>TimItTechnicalService</code> was set to reduce results; once the
 * Total timer has been stopped ie the body has been terminated a list of timers
 * is sent to the reductor.
 *
 * <p> To add an internal timer in ProActive core, you must be sure
 * of its validity, then :
 * <ul>
 *
 *   <li><p> Declare its index in the <code>TimerWarehouse</code> class
 *   </p></li>
 *
 *   <li><p> Declare its name in this class like other timer names
 *   </p></li>
 *
 *   <li><p> Instanciate it in the constructor of this class and add it to the internal list of timers to allow snapshots
 *   </p></li>
 *
 * </ul>
 *
 * <p> To add a user level timer, call <code>attachTimer()</code> method. It will
 * return an instance of a timer attached to the Serve timer.
 *
 * @author vbodnart
 */
@PublicAPI
public class CoreTimersContainer implements TimerProvidable {
    public static final String TOTAL_TIMER_NAME = "Total";
    public static final String SERVE_TIMER_NAME = "Serve";
    public static final String WAIT_FOR_REQUEST_TIMER_NAME = "WaitForRequest";
    public static final String USER_COMPUTATION_TIMER_NAME = "UserComputation";
    public static final String SEND_REQUEST_TIMER_NAME = "SendRequest";
    public static final String SEND_REPLY_TIMER_NAME = "SendReply";
    public static final String LOCAL_COPY_TIMER_NAME = "LocalCopy";
    public static final String BEFORE_SERIALIZATION_TIMER_NAME = "BeforeSerialization";
    public static final String SERIALIZATION_TIMER_NAME = "Serialization";
    public static final String AFTER_SERIALIZATION_TIMER_NAME = "AfterSerialization";
    public static final String WAIT_BY_NECESSITY_TIMER_NAME = "WaitByNecessity";
    public static final String GROUP_ONE_WAY_CALL_TIMER_NAME = "GroupOneWayCall";
    public static final String GROUP_ASYNC_CALL_TIMER_NAME = "GroupAsyncCall";

    /**
     * A stub to the reductor used to send the map of timers to the JVM that
     * deployed the active object
     */
    private TimItBasicReductor timitReductor;

    /**
     * A list of timers that will be used to send all timers to the reductor
     * object
     */
    private ArrayList<BasicTimer> timersList;

    /** A unique id that identifies this TimerProvidable object */
    private UniqueID currentID;

    /** If the property is different of all, */
    private String[] askedTimersNames;

    /** Used to disable the timer for example in case of a group call */
    private boolean isDisabled = false;

    /** This variable is used if the timit property value is all */
    private boolean activateAll = false;

    /** Other information to send to the reductor */
    private String otherInformation;

    /** Map used to resolve a timer index from its name */
    private Map<Method, Integer> methodsTimersMap;

    /** Current method timer index */
    private int currentMethodTimerIndex = 0;

    /**
     * Creates a new instance of SimpleTimersContainer.
     *
     * @param uniqueID
     *            The UniqueID of the body to wich this container is attached
     * @param reductor
     *            The stub of the reductor object
     * @param propertyValue
     *            The timers to activate
     * @param otherInformation
     *            Some information to send to the reductor
     */
    private CoreTimersContainer(final UniqueID uniqueID, final Object reifiedObject, final Object reductor,
            final String propertyValue, final String otherInformation) {
        this.currentID = uniqueID;
        this.timitReductor = (TimItBasicReductor) reductor;
        this.otherInformation = otherInformation;

        // Check if all timers need to be activated
        this.activateAll = "all".compareToIgnoreCase(propertyValue) == 0;

        // Provide the array of timer names that the user want to activate
        if (!this.activateAll) {
            this.askedTimersNames = propertyValue.split(",");
            Arrays.sort(this.askedTimersNames);
        }

        this.methodsTimersMap = new HashMap<Method, Integer>();
        this.timersList = new ArrayList<BasicTimer>();

        // First create the total, serve and wfr timers  that are not filtered
        final BasicTimer total = new BasicTimer(CoreTimersContainer.TOTAL_TIMER_NAME, null);
        final BasicTimer serve = new BasicTimer(CoreTimersContainer.SERVE_TIMER_NAME, total);
        final BasicTimer wfr = new BasicTimer(CoreTimersContainer.WAIT_FOR_REQUEST_TIMER_NAME, total);
        this.timersList.add(total);
        this.timersList.add(serve);
        this.timersList.add(wfr);

        // Analyse the reified object class in order to create a timer per public method        
        final Class<?> reifiedObjectClass = reifiedObject.getClass();
        final Method[] methods = reifiedObjectClass.getMethods();
        for (final Method m : methods) {
            // Modifier.isPublic(mod) && !Modifier.isStatic(mod) && !Modifier.isFinal(mod)
            if ((m.getModifiers() == 1) && !"equals".equals(m.getName()) && !"hashCode".equals(m.getName()) &&
                !"toString".equals(m.getName())) {
                String timerName = m.getName() + "(";
                Class<?>[] paramClasses = m.getParameterTypes();
                int paramLength = paramClasses.length;
                for (int i = 0; i < paramLength; i++) {
                    timerName += (paramClasses[i].getSimpleName() + ((i < (paramLength - 1)) ? ", " : ""));
                }
                timerName += ")";

                // Create a method timer							
                final BasicTimer methodTimer = new BasicTimer(timerName, serve);
                // Add the 
                this.timersList.add(methodTimer);
                // Put it into the map in order to have an acces to its index 
                this.methodsTimersMap.put(m, this.timersList.size() - 1);
                // Attach children for this method timer
                this.createMethodTimerChildren(methodTimer);
            }
        }

        // Register this class as a TimerProvidable
        TimerWarehouse.addTimerProvidable(this);
    }

    /**
     * Creates a container only if it is necessary ie the timitActivation property was specified
     * through the technical service.
     *
     * @param uniqueID
     *            The id of the body
     * @param factory
     *            The MOP factory
     * @param nodeURL The URL of the node
     *
     * @return An instance of CoreTimersContainer
     */
    public final static CoreTimersContainer create(final UniqueID uniqueID, final Object reifiedObject,
            final MetaObjectFactory factory, final String nodeURL) {
        final String timitActivationPropertyValue = CoreTimersContainer.checkNodeProperty(nodeURL);

        // If there is no activated timers no need to create the container
        if ((timitActivationPropertyValue == null) || "".equals(timitActivationPropertyValue)) {
            return null;
        }
        return new CoreTimersContainer(uniqueID, reifiedObject, factory.getTimItReductor(),
            timitActivationPropertyValue, "");
    }

    /**
     * Checks the general property to know if some timer names are specified
     *
     * @param nodeURL
     *            The URL of the node containing these properties
     * @return The value of the property
     */
    public final static String checkNodeProperty(final String nodeURL) {
        // Check the activated timers from the node properties
        String result = null;
        try {
            final Node currentNode = NodeFactory.getNode(nodeURL);
            result = currentNode.getProperty("timitActivation");
            if ((result != null) && !"".equals(result)) {
                TimItTechnicalService.setGenerateOutputFile(currentNode.getProperty("generateOutputFile"));
                TimItTechnicalService.setPrintOutput(currentNode.getProperty("printOutput"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Starts the serve timer then starts the associated method timer given by the methodName.
     * @param method The name of the timer associated to this method
     */
    public final void startServeTimer(final Method method) {
        // First start serve timer
        this.timersList.get(TimerWarehouse.SERVE).start();
        // Then start the method timer
        final Integer i = this.methodsTimersMap.get(method);
        if (i != null) {
            this.currentMethodTimerIndex = i;
            final BasicTimer methodTimer = this.timersList.get(i);
            methodTimer.start();
        }
    }

    /**
     * Stops the current started method timer and then stops the serve timer.
     */
    public final void stopServeTimer() {
        // First stop the method timer
        if (this.currentMethodTimerIndex != 0) {
            BasicTimer methodTimer = this.timersList.get(this.currentMethodTimerIndex);
            methodTimer.stop();
            this.currentMethodTimerIndex = 0;
        }
        // Then stop serve timer
        this.timersList.get(TimerWarehouse.SERVE).stop();
    }

    // //////////////////////////////////
    // TimerProvidable implementation  //
    // //////////////////////////////////

    /**
     * @see org.objectweb.proactive.core.util.profiling.TimerProvidable#startTimer(int)
     */
    public final void startTimer(final int timerId) {
        if (this.isDisabled) {
            return;
        }
        final BasicTimer timer = this.getTimer(timerId);
        if (timer != null) {
            timer.start();
        }
    }

    /**
     * @see org.objectweb.proactive.core.util.profiling.TimerProvidable#stopTimer(int)
     */
    public final void stopTimer(final int timerId) {
        if (this.isDisabled) {
            return;
        }
        final BasicTimer timer = this.getTimer(timerId);
        if (timer != null) {
            timer.stop();
        }
    }

    /**
     * @see org.objectweb.proactive.core.util.profiling.TimerProvidable#getTimerProvidableID()
     */
    public final UniqueID getTimerProvidableID() {
        return this.currentID;
    }

    /**
     * Invoked by TimItReductor to inform Timed object that he is the
     * finalization reductor
     *
     * @param red
     *            The reference on the reductor
     */
    public final void setTimerReduction(final TimItBasicReductor red) {
        this.timitReductor = red;
    }

    /**
     * Sends results to the reductor object.
     * The timers must be stopped.
     * @param className The class name of the reified object
     * @param shortUniqueID The short id version of the body
     */
    public final void sendResults(final String className, final String shortUniqueID) {
        // Unregister this from the TimerWarehouse
        TimerWarehouse.timerProvidableStore.remove(this.currentID);

        // Update all user computation timers
        CoreTimersContainer.updateAllUserComputationTimers(this.timersList.toArray(new BasicTimer[] {}));

        // A list of BasicTimer is made
        // During the copy filter all timers with 0 total time        
        final ArrayList<BasicTimer> result = new ArrayList<BasicTimer>();
        for (final BasicTimer t : this.timersList) {
            if (t.getTotalTime() != 0L) {
                result.add(t);
            }
        }

        // If the reductor reference is not provided the results will be printed
        // directly on the current jvm therefore a the reductor is a simple object
        if (this.timitReductor == null) {
            this.timitReductor = new TimItBasicReductor();
            this.timitReductor.receiveTimersDirectMode(className, shortUniqueID, result, otherInformation);
        } else {
            this.timitReductor.receiveTimers(className, shortUniqueID, result, otherInformation);
        }
        this.timitReductor = null;
        this.timersList.clear();
        this.methodsTimersMap.clear();
    }

    /**
     * Returns a the list of non-stopped timers.
     * DO NOT FORGET TO RETURN A TIME STAMP IN CASE OF A REMOTE SNAPSHOT !
     * DO NOT FORGET TO UPDATE USER COMPUTATION TIME !
     * @param timersNames A filter of timers names.
     */
    public final BasicTimer[] getSnapshot() {
        return this.timersList.toArray(new BasicTimer[] {});
    }

    /**
     * Returns an instance of a timer from a given id.<p>
     * For example if you want to get the Serve timer :
     * <p><blockquote><pre>
     *   BasicTimer serveTimer = TimerWarehouse.getTimerProvidable().getTimer(TimerWarehouse.SERVE);
     * </pre></blockquote><p>
     *
     * @param timerId
     *            The id of the wanted timer
     * @return An instance of a timer
     */
    public final BasicTimer getTimer(final int timerId) {
        return this.timersList.get(this.currentMethodTimerIndex + timerId);
    }

    /**
     * Direct access to the Total timer
     * @return The Total timer
     */
    public final BasicTimer getTotalTimer() {
        return this.timersList.get(0);
    }

    /**
     * Direct access to the Serve timer
     * @return The Serve timer
     */
    public final BasicTimer getServeTimer() {
        return this.timersList.get(1);
    }

    /**
     * Direct access to the WaitForRequest timer
     * @return The WaitForRequest timer
     */
    public final BasicTimer getWaitForRequestTimer() {
        return this.timersList.get(2);
    }

    /**
     * Stops automatically all timers, use this method before terminate a body.
     */
    public final void stopAll() {
        this.stopAll(this.timersList);
    }

    /**
     * Stops automatically all timers, use this method before terminate a body.
     */
    public final void stopAll(final Collection<BasicTimer> timersCollection) {
        for (final BasicTimer current : timersCollection) {
            BasicTimer startedSonOfCurrent = getStartedSonOf(current, timersCollection);
            BasicTimer lastStarted = startedSonOfCurrent;

            // Get the leaf for the current timer
            while (startedSonOfCurrent != null) {
                lastStarted = startedSonOfCurrent;
                startedSonOfCurrent = getStartedSonOf(startedSonOfCurrent, timersCollection);
            }

            // If lastStarted is null the current has no started sons
            // We can stop the current
            if (lastStarted == null) {
                if (current.isStarted()) {
                    current.stop();
                }
            } else {
                // lastStarted is not null so we
                // found the leaf and we can stop it				
                lastStarted.stop();
            }
        }

        // Stop all roots only when children are stopped
        for (final BasicTimer current : timersCollection) {
            // If parent is null then
            if ((current.getParent() == null) && current.isStarted()) {
                current.stop();
            }
        }
    }

    /**
     * Starts the timer identified by its id, and enables a check on send
     * request timer and its direct sons.
     *
     * @param timerId
     *            The id of the timer to stop
     */
    public final void startXAndDisable(final int timerId) {
        this.startTimer(timerId);
        this.isDisabled = true;
    }

    /**
     * Stops the timer identified by its id, and removes a check on send request
     * timer and its direct sons.
     *
     * @param timerId
     *            The id of the timer to stop
     */
    public final void enableAndStopX(final int timerId) {
        this.isDisabled = false;
        this.stopTimer(timerId);
    }

    /**
     * Updates all user computation timers.
     * Such as userTime = methodTime - sysTime, where sysTime = sum(SendRequest,SendReply,WaitByNecessity,GroupOneWayCall,GroupAsyncCall)
     * Warning ! The timers in the timers array must be stopped.
     * @param timersList The array of timers
     */
    public static final void updateAllUserComputationTimers(BasicTimer[] timersArray) {
        BasicTimer currentTimer;
        for (int i = 0; i < timersArray.length; i++) {
            currentTimer = timersArray[i];
            if (currentTimer.getName().equals(CoreTimersContainer.USER_COMPUTATION_TIMER_NAME)) {
                // Get parent index (ie the index of the method timer)
                final int methodTimerIndex = i - 1;
                long sysTotalTime = 0L;
                // Add send request total time
                sysTotalTime += timersArray[methodTimerIndex + TimerWarehouse.SEND_REQUEST].getTotalTime();
                // Add send reply total time
                sysTotalTime += timersArray[methodTimerIndex + TimerWarehouse.SEND_REPLY].getTotalTime();
                // Add wait by necessity total time
                sysTotalTime += timersArray[methodTimerIndex + TimerWarehouse.WAIT_BY_NECESSITY]
                        .getTotalTime();
                // Add group one way call total time
                sysTotalTime += timersArray[methodTimerIndex + TimerWarehouse.GROUP_ONE_WAY_CALL]
                        .getTotalTime();
                // Add group async call total time
                sysTotalTime += timersArray[methodTimerIndex + TimerWarehouse.GROUP_ASYNC_CALL]
                        .getTotalTime();
                // Update current user total time 
                // Such as userTime = methodTotalTime - sysTime, where sysTime = sum(SendRequest,SendReply,WaitByNecessity,GroupOneWayCall,GroupAsyncCall)
                currentTimer.setTotal(currentTimer.getParent().getTotalTime() - sysTotalTime);
                // The number of invocations of this timer is equal to its parent invocations
                currentTimer.setStartStopCoupleCount(currentTimer.getParent().getStartStopCoupleCount());
            }
        }
    }

    //////////////////////
    // Private methods  //
    //////////////////////

    /**
     * Associates a timer with its id and puts it in a map containing all
     * timers. If a parent timer is not activated the child timer will not be
     * activated.
     */
    private final void createMethodTimerChildren(final BasicTimer methodTimer) {
        this.createOnDemand(CoreTimersContainer.USER_COMPUTATION_TIMER_NAME, methodTimer);
        final BasicTimer sendRequest = this.createOnDemand(CoreTimersContainer.SEND_REQUEST_TIMER_NAME,
                methodTimer);
        this.createOnDemand(CoreTimersContainer.SEND_REPLY_TIMER_NAME, methodTimer);
        this.createOnDemand(CoreTimersContainer.LOCAL_COPY_TIMER_NAME, sendRequest);
        this.createOnDemand(CoreTimersContainer.BEFORE_SERIALIZATION_TIMER_NAME, sendRequest);
        this.createOnDemand(CoreTimersContainer.SERIALIZATION_TIMER_NAME, sendRequest);
        this.createOnDemand(CoreTimersContainer.AFTER_SERIALIZATION_TIMER_NAME, sendRequest);
        this.createOnDemand(CoreTimersContainer.WAIT_BY_NECESSITY_TIMER_NAME, methodTimer);
        this.createOnDemand(CoreTimersContainer.GROUP_ONE_WAY_CALL_TIMER_NAME, methodTimer);
        this.createOnDemand(CoreTimersContainer.GROUP_ASYNC_CALL_TIMER_NAME, methodTimer);
    }

    /**
     * Returns an instance of a created timer. The created timer is added
     * in the map of timers
     *
     * @param timerName The name of the timer
     * @param parent The reference on the parent of the timer
     * @return An isntance of BasicTimer
     */
    private final BasicTimer createOnDemand(final String timerName, final BasicTimer parent) {
        BasicTimer timer = null;

        // Check if the current timer must be activated
        if (activateAll || ((parent != null) && (Arrays.binarySearch(askedTimersNames, timerName) >= 0))) {
            // Create the timer
            timer = new BasicTimer(timerName, parent);
        }
        // Add it to the list           
        this.timersList.add(timer);
        return timer;
    }

    /**
     * Returns the first started son of the specified timer.
     *
     * @param t The parent timer
     * @return The first started son of t
     */
    private final static BasicTimer getStartedSonOf(final BasicTimer t,
            final Collection<BasicTimer> timersCollection) {
        for (final BasicTimer current : timersCollection) {
            if ((current.getParent() != null) && current.getParent().equals(t) && current.isStarted()) {
                return current;
            }
        }
        return null;
    }

    /**
     * A setter for the additional information
     *
     * @param otherInformation Some extra information
     */
    public final void setOtherInformation(final String otherInformation) {
        this.otherInformation = otherInformation;
    }

    /**
     * This method checks that the provided name is unique and
     * creates a new <code>BasicTimer</code>, attaches it to the SERVE timer
     * then return a reference on this new timer.
     * User level timer is always attached as a son of SERVE timer.
     *
     * @param timerName The name of the timer
     * @return A new instance of a timer attached to the SERVE timer
     */
    public final BasicTimer attachTimer(final String timerName) {
        // First check if the timerName is already used
        for (final BasicTimer t : this.timersList) {
            if (t.getName().equals(timerName)) {
                throw new RuntimeException("This timer name is already used : " + timerName);
            }
        }

        // Get the instance of SERVE timer
        final BasicTimer serveTimer = this.timersList.get(TimerWarehouse.SERVE);
        if (serveTimer == null) {
            return null;
        }

        // Create the users timer
        final BasicTimer userTimer = new BasicTimer(timerName, serveTimer);
        this.timersList.add(userTimer);
        userTimer.setUserLevel(true);
        return userTimer;
    }
}
