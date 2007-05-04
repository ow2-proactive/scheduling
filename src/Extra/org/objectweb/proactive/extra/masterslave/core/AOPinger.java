package org.objectweb.proactive.extra.masterslave.core;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.exceptions.NonFunctionalException;
import org.objectweb.proactive.core.exceptions.manager.NFEListener;
import org.objectweb.proactive.core.exceptions.proxy.FailedGroupRendezVousException;
import org.objectweb.proactive.core.group.ExceptionInGroup;
import org.objectweb.proactive.core.group.ExceptionListException;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.Slave;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.SlaveDeadListener;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.SlaveWatcher;


/**
 * The Pinger Active Object is responsible for watching slaves'activity. <br/>
 * It reports slaves failure to the Master
 * @author fviale
 *
 */
public class AOPinger implements SlaveWatcher, RunActive, InitActive,
    Serializable {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.MASTERSLAVE_SLAVES);

    // stub
    AOPinger stubOnThis;

    // terminated state
    boolean terminated;

    // pinging period
    long pingPeriod;

    // Slaves to be watched
    Slave slaveGroupStub;
    private Group slaveGroup;
    SlaveDeadListener listener;

    /**
     * ProActive empty constructor
     */
    public AOPinger() {
    }

    /**
     * Creates a pinger with the given listenerr
     * @param listener
     */
    public AOPinger(SlaveDeadListener listener) {
        this.listener = listener;
        terminated = false;
        pingPeriod = Long.parseLong(System.getProperty(
                    "proactive.masterslave.pingperiod"));
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.SlavePinger#addSlaveToPing(org.objectweb.proactive.extra.masterslave.interfaces.internal.Slave)
     */
    public void addSlaveToWatch(Slave slave) {
        slaveGroup.add(slave);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        try {
            slaveGroupStub = (Slave) ProActiveGroup.newGroup(AOSlave.class.getName());
            slaveGroup = ProActiveGroup.getGroup(slaveGroupStub);
            stubOnThis = (AOPinger) ProActive.getStubOnThis();

            ProActive.addNFEListenerOnGroup(slaveGroupStub,
                new DetectMissingGroup());
        } catch (ClassNotReifiableException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.SlavePinger#removeSlaveToPing(org.objectweb.proactive.extra.masterslave.interfaces.internal.Slave)
     */
    public void removeSlaveToWatch(Slave slave) {
        slaveGroup.remove(slave);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.RunActive#runActivity(org.objectweb.proactive.Body)
     */
    public void runActivity(Body body) {
        Service service = new Service(body);
        while (!terminated) {
            // we serve everything
            while (service.hasRequestToServe()) {
                service.serveOldest();
            }
            slaveGroupStub.heartBeat();
            try {
                Thread.sleep(pingPeriod);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            body.terminate();
        } catch (IOException e) {
        }
    }

    /**
     * @param slave
     */
    public void slaveMissing(Slave slave) {
        logger.debug("A slave is missing...reporting back to the Master");
        if (slaveGroup.contains(slave)) {
            listener.isDead(slave);
            slaveGroup.remove(slave);
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.internal.SlaveWatcher#terminate()
     */
    public BooleanWrapper terminate() {
        this.terminated = true;
        logger.debug("Pinger terminated...");
        return new BooleanWrapper(true);
    }

    /**
     * Handles Non Functional Exceptions(NFE) detection
     * @author fviale
     */
    public class DetectMissingGroup implements NFEListener {

        /* (non-Javadoc)
         * @see org.objectweb.proactive.core.exceptions.manager.NFEListener#handleNFE(org.objectweb.proactive.core.exceptions.NonFunctionalException)
         */
        public boolean handleNFE(NonFunctionalException nfe) {
            Iterator exceptions;
            ExceptionListException exceptionList;

            try {
                FailedGroupRendezVousException fgrve = (FailedGroupRendezVousException) nfe;
                exceptionList = (ExceptionListException) fgrve.getCause();
            } catch (ClassCastException cce) {
                return false;
            }

            synchronized (exceptionList) {
                exceptions = exceptionList.iterator();

                while (exceptions.hasNext()) {
                    ExceptionInGroup eig = (ExceptionInGroup) exceptions.next();
                    stubOnThis.slaveMissing((Slave) eig.getObject());
                }
            }

            return true;
        }
    }
}
