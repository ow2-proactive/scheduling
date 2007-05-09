package org.objectweb.proactive.benchmarks.timit.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.proactive.benchmarks.timit.util.basic.BasicTimer;
import org.objectweb.proactive.benchmarks.timit.util.basic.TimItBasicReductor;
import org.objectweb.proactive.benchmarks.timit.util.service.TimItTechnicalService;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.MetaObjectFactory;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.profiling.TimerProvidable;
import org.objectweb.proactive.core.util.profiling.TimerWarehouse;


/**
 * This class represents a container of timers, an instance of this class is
 * attached to a body. Timers follows a predefined hierarchy.
 *
 * The instanciation of this class is done when a body is created. Once the
 * total timer has been stopped ie the body has been terminated a map of timers
 * is sent to the reductor. To add a timer just add one in the createTimers()
 * method.
 *
 * @author vbodnart
 */
public class CoreTimersContainer implements TimerProvidable {

    /** Since the timer container is attahced to a body we use its logger */
    private static Logger logger = ProActiveLogger.getLogger(Loggers.BODY);

    /** A static variable to switch to debug mode */
    public static final boolean DEBUG = false;

    /**
     * A stub to the reductor used to send the map of timers to the JVM that
     * deployed the active object
     */
    private TimItBasicReductor timitReductor;

    /**
     * A map of timers that will be used to send all timers to the reductor
     * object
     */
    private Map<Byte, BasicTimer> timersMap;

    /** A unique id that identifies this TimerProvidable object */
    private UniqueID currentID;

    /** If the property is different of all, */
    private String[] askedTimersNames;

    /** Enable the checking of send request and sons*/
    private boolean checkSendRequestAndSons;

    /** This variable is used if the timit property value is all */
    private boolean activateAll = false;

    /** A String used as a buffer to the output in case of a DEBUG mode */
    private String tempOutput = null;

    /** Other information to send to the reductor */
    private String otherInformation;

    /**
     * Constructs a container only if the timitActivation property was specified
     * through the technical service.
     * @param uniqueID The id of the body
     * @param factory The MOP factory
     * @param timitActivationPropertyValue The timers property
     * @param otherInformation Some information to send to the reductor
     * @return An instance of CoreTimersContainer
     */
    public static final CoreTimersContainer contructOnDemand(
        final UniqueID uniqueID, final MetaObjectFactory factory,
        final String timitActivationPropertyValue, final String otherInformation) {
        // If there is no activated timers no need to create the container
        if ((timitActivationPropertyValue == null) ||
                "".equals(timitActivationPropertyValue)) {
            if (CoreTimersContainer.DEBUG) {
                // The property is not specified
                logger.debug(
                    "TimIt [CoreTimersContainer.CoreTimersContainer()] : 'proactive.timit.activation' property is not specified");
            }
            return null;
        }

        // Free the remote reference on the reductor if needed 
        Object r = factory.getTimItReductor();
        if (r != null) {
            factory.setTimItReductor(null);
        }
        return new CoreTimersContainer(uniqueID, r,
            timitActivationPropertyValue, otherInformation);
    }

    /**
     * Verifies that the reified object is the reductor.
     * @param reifiedObject The currently reified object
     * @return True if the reifiedObject is the reductor, false otherwise
     */
    public static final boolean checkReifiedObject(final Object reifiedObject) {
        // First thing to check is the class of the reified object
        // in order to avoid the creation of a container for the reductor active object
        return (reifiedObject.getClass().equals(TimItBasicReductor.class));
    }

    /**
     * Checks the general property to know if some timer names are specified
     * @param nodeURL The URL of the node containing these properties
     * @return The value of the property
     */
    public static final String checkNodeProperty(final String nodeURL) {
        // Check the activated timers from the node properties
        String result = null;
        try {
            Node currentNode = NodeFactory.getNode(nodeURL);
            result = currentNode.getProperty("timitActivation");
            TimItTechnicalService.setGenerateOutputFile(currentNode.getProperty(
                    "generateOutputFile"));
            TimItTechnicalService.setPrintOutput(currentNode.getProperty(
                    "printOutput"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Creates a new instance of SimpleTimersContainer.
     * @param uniqueID
     *            The UniqueID of the body to wich this container is attached
     * @param reductor
     *            The stub of the reductor object
     * @param propertyValue The timers to activate
     * @param otherInformation Some information to send to the reductor
     */
    public CoreTimersContainer(final UniqueID uniqueID, final Object reductor,
        final String propertyValue, final String otherInformation) {
        this.timitReductor = (TimItBasicReductor) reductor;
        this.currentID = uniqueID;
        this.timersMap = new HashMap<Byte, BasicTimer>(15);
        this.otherInformation = otherInformation;
        // DEBUG
        if (CoreTimersContainer.DEBUG) {
            this.tempOutput = "";
        }

        // Check if all timers need to be activated
        activateAll = "all".compareToIgnoreCase(propertyValue) == 0;

        // Provide the array of timer names that the user want to activate
        this.askedTimersNames = propertyValue.split(",");
        Arrays.sort(this.askedTimersNames);

        // Create timers
        this.createTimers();

        // The user may want to activate all timers
        // Register the benchmark class as a TimerProvidable
        TimerWarehouse.addTimerProvidable(this);

        if (CoreTimersContainer.DEBUG) {
            logger.debug( //System.out.println(
                "** TimIt [CoreTimersContainer] : activated timers = " +
                propertyValue);
        }
    }

    // //////////////////////////////////
    // TimerProvidable implementation //
    // //////////////////////////////////	

    /**
     * (non-Javadoc)
     *
     * @see org.objectweb.proactive.core.util.profiling.TimerProvidable#startTimer(byte)
     */
    public final void startTimer(final byte timerId, final String infos) {
        if (this.checkSendRequestAndSons && isSendRequestOrSons(timerId)) {
            return;
        }
        BasicTimer t = getTimer(timerId);
        if (CoreTimersContainer.DEBUG) {
            String s = "TimIt [CoreTimersContainer.stopTimer()] : Asking for starting " +
                t.getName() + ((infos == null) ? "" : (" infos : " + infos)) +
                " currentID : " + this.currentID.shortString() +
                " currentThread " + Thread.currentThread();
            tempOutput += (s + "\n");
        }
        if (t != null) {
            t.start();
        }
    }

    /**
     * @see org.objectweb.proactive.core.util.profiling.TimerProvidable#stopTimer(byte)
     */
    public final void stopTimer(final byte timerId, String infos) {
        if (this.checkSendRequestAndSons && isSendRequestOrSons(timerId)) {
            return;
        }
        BasicTimer t = getTimer(timerId);
        if (CoreTimersContainer.DEBUG) {
            String s = "TimIt [CoreTimersContainer.stopTimer()] : Asking for stopping " +
                t.getName() + ((infos == null) ? "" : (" infos : " + infos)) +
                " currentID : " + this.currentID.shortString() +
                " currentThread " + Thread.currentThread();
            tempOutput += (s + "\n");
        }
        if (t != null) {
            t.stop();
        }
    }

    /**
     *
     * @see org.objectweb.proactive.core.util.profiling.TimerProvidable#setTimerValue(byte,
     *      long)
     */
    public final void setTimerValue(final byte timerId, final long value) {
        getTimer(timerId).setTotal(value);
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
     */
    public final void setTimerReduction(final TimItBasicReductor red) {
        this.timitReductor = red;
    }

    public void dump() {
        System.out.println("CoreTimersContainer.dump() ----> " +
            this.tempOutput);
    }

    /**
     * Sends results to the reductor object.
     */
    public final void sendResults(final String className,
        final String shortUniqueID) {
        // Unregister this from the TimerWarehouse
        TimerWarehouse.timerProvidableStore.remove(this.currentID);

        if (CoreTimersContainer.DEBUG) {
            logger.debug(tempOutput);
        }

        // Since the Bug ID : 4501848
        // Objects returned by keySet(), entrySet() and values() are not
        // Serializable
        // A list of BasicTimer is made
        // During the copy filter all timers differents 0
        final Iterator<BasicTimer> it = this.timersMap.values().iterator();
        final ArrayList<BasicTimer> a = new ArrayList<BasicTimer>(this.timersMap.values()
                                                                                .size());
        while (it.hasNext()) {
            BasicTimer t = it.next();
            if (t.getTotalTime() != 0L) {
                a.add(t);
            }
        }

        // If the reductor reference is not provided the results will be printed directly
        // on the current jvm therefore a the reductor is a simple
        if (this.timitReductor == null) {
            this.timitReductor = new TimItBasicReductor();
            this.timitReductor.receiveTimersDirectMode(className,
                shortUniqueID, a, otherInformation);
        } else {
            this.timitReductor.receiveTimers(className, shortUniqueID, a,
                otherInformation);
        }
        this.timitReductor = null;
    }

    /**
     * Returns an instance of a timer from a given id.
     * @param timerId The id of the wanted timer
     * @return An instance of a timer
     */
    public final BasicTimer getTimer(final byte timerId) {
        final BasicTimer result = this.timersMap.get(timerId);
        if (CoreTimersContainer.DEBUG) {
            if (result == null) {
                throw new RuntimeException(
                    "** TimIt [CoreTimersContainer] : Something is wrong ! Unknown timer id " +
                    timerId);
            }
        }
        return result;
    }

    /**
     * Stops all timers, use this method before terminate a body.
     */
    public final void stopAll() {
        BasicTimer lcopy = getTimer(TimerWarehouse.LOCAL_COPY);
        BasicTimer befs = getTimer(TimerWarehouse.BEFORE_SERIALIZATION);
        BasicTimer ser = getTimer(TimerWarehouse.SERIALIZATION);
        BasicTimer afser = getTimer(TimerWarehouse.AFTER_SERIALIZATION);
        BasicTimer srep = getTimer(TimerWarehouse.SEND_REPLY);
        BasicTimer sreq = getTimer(TimerWarehouse.SEND_REQUEST);
        BasicTimer g1way = getTimer(TimerWarehouse.GROUP_ONE_WAY_CALL);
        BasicTimer gasync = getTimer(TimerWarehouse.GROUP_ASYNC_CALL);
        BasicTimer wfr = getTimer(TimerWarehouse.WAIT_FOR_REQUEST);
        BasicTimer serve = getTimer(TimerWarehouse.SERVE);
        BasicTimer total = getTimer(TimerWarehouse.TOTAL);
        if (lcopy.isStarted()) {
            this.stopTimer(TimerWarehouse.LOCAL_COPY, null);
        }
        if (befs.isStarted()) {
            this.stopTimer(TimerWarehouse.BEFORE_SERIALIZATION, null);
        }
        if (ser.isStarted()) {
            this.stopTimer(TimerWarehouse.SERIALIZATION, null);
        }
        if (afser.isStarted()) {
            this.stopTimer(TimerWarehouse.AFTER_SERIALIZATION, null);
        }
        if (srep.isStarted()) {
            this.stopTimer(TimerWarehouse.SEND_REPLY, null);
        }
        if (sreq.isStarted()) {
            this.stopTimer(TimerWarehouse.SEND_REQUEST, null);
        }
        if (g1way.isStarted()) {
            this.stopTimer(TimerWarehouse.GROUP_ONE_WAY_CALL, null);
        }
        if (gasync.isStarted()) {
            this.stopTimer(TimerWarehouse.GROUP_ASYNC_CALL, null);
        }

        // Check if the wait for request 
        if (wfr.isStarted()) {
            this.stopTimer(TimerWarehouse.WAIT_FOR_REQUEST, null);
        }

        // Check if the Serve timer is started ie if the terminate is done during a service
        if (serve.isStarted()) {
            this.stopTimer(TimerWarehouse.SERVE, null);
        }

        // then we can stop the total
        if (total.isStarted()) {
            this.stopTimer(TimerWarehouse.TOTAL, null);
        }
    }

    /**
     * Starts the timer identified by its id, and enables a check on
     * send request timer and its direct sons.
     * @param timerId The id of the timer to stop
     */
    public final void startXAndSkipSendRequest(final byte timerId) {
        this.startTimer(timerId, null);
        this.checkSendRequestAndSons = true;
    }

    /**
     * Stops the timer identified by its id, and removes a check on
     * send request timer and its direct sons.
     * @param timerId The id of the timer to stop
     */
    public final void stopXAndUnskipSendRequest(final byte timerId) {
        this.stopTimer(timerId, null);
        this.checkSendRequestAndSons = false;
    }

    // ///////////////////
    // Private methods //
    // ///////////////////

    /**
     * Associates a timer with its id and puts it in a map containing all
     * timers. If a parent timer is not activated the child timer will not be
     * activated.
     */
    private final void createTimers() {
        // The total has no parent
        BasicTimer total = null;
        if (activateAll ||
                (Arrays.binarySearch(askedTimersNames, "Total") >= 0)) {
            // Create the timer
            total = new BasicTimer("Total", null);
            // Add it to the list
            this.timersMap.put(TimerWarehouse.TOTAL, total);
        }
        // Children
        this.createOnDemand(TimerWarehouse.DEPLOYEMENT, "Deployement", total);
        final BasicTimer serve = this.createOnDemand(TimerWarehouse.SERVE,
                "Serve", total);
        final BasicTimer sendRequest = this.createOnDemand(TimerWarehouse.SEND_REQUEST,
                "SendRequest", serve);
        this.createOnDemand(TimerWarehouse.LOCAL_COPY, "LocalCopy", sendRequest);
        this.createOnDemand(TimerWarehouse.BEFORE_SERIALIZATION,
            "BeforeSerialization", sendRequest);
        this.createOnDemand(TimerWarehouse.SERIALIZATION, "Serialization",
            sendRequest);
        this.createOnDemand(TimerWarehouse.AFTER_SERIALIZATION,
            "AfterSerialization", sendRequest);
        this.createOnDemand(TimerWarehouse.WAIT_BY_NECESSITY,
            "WaitByNecessity", serve);
        this.createOnDemand(TimerWarehouse.GROUP_ONE_WAY_CALL,
            "GroupOneWayCall", serve);
        this.createOnDemand(TimerWarehouse.GROUP_ASYNC_CALL, "GroupAsyncCall",
            serve);
        this.createOnDemand(TimerWarehouse.SEND_REPLY, "SendReply", serve);
        this.createOnDemand(TimerWarehouse.WAIT_FOR_REQUEST, "WaitForRequest",
            total);
    }

    private final BasicTimer createOnDemand(final byte id,
        final String timerName, final BasicTimer parent) {
        BasicTimer timer = null;

        // Check if the current timer must be activated
        if (activateAll ||
                ((parent != null) &&
                (Arrays.binarySearch(askedTimersNames, timerName) >= 0))) {
            // Create the timer
            timer = new BasicTimer(timerName, parent);
            // Add it to the list
            this.timersMap.put(id, timer);
        }
        return timer;
    }

    private final boolean isSendRequestOrSons(final byte timerId) {
        return ((timerId == TimerWarehouse.SEND_REQUEST) ||
        (timerId == TimerWarehouse.LOCAL_COPY) ||
        (timerId == TimerWarehouse.BEFORE_SERIALIZATION) ||
        (timerId == TimerWarehouse.SERIALIZATION) ||
        (timerId == TimerWarehouse.AFTER_SERIALIZATION));
    }
}
