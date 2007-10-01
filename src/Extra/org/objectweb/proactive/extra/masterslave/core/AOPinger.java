package org.objectweb.proactive.extra.masterslave.core;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProGroup;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.Slave;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.SlaveDeadListener;
import org.objectweb.proactive.extra.masterslave.interfaces.internal.SlaveWatcher;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * The Pinger Active Object is responsible for watching slaves'activity. <br>
 * It reports slaves failure to the Master<br>
 * @author fviale
 *
 */
public class AOPinger implements SlaveWatcher, RunActive, InitActive,
    Serializable {

    /**
         *
         */
    private static final long serialVersionUID = -7489033564540496244L;

    /**
    * pinger log4j logger
    */
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.MASTERSLAVE_SLAVES);

    /**
     * Stub on the active object
     */
    protected AOPinger stubOnThis;

    /**
     * is this active object terminated
     */
    protected boolean terminated;

    /**
     * interval when slaves are sent a ping message
     */
    protected long pingPeriod;

    /**
     * Who will be notified when slaves are dead (in general : the master)
     */
    protected SlaveDeadListener listener;

    /**
     * Stub to slave group
     */
    protected Slave slaveGroupStub;

    /**
     * Slave group
     */
    protected Group<Slave> slaveGroup;

    /**
     * for internal use
     */
    private Thread localThread;

    /**
     * ProActive empty constructor
     */
    public AOPinger() {
    }

    /**
     * Creates a pinger with the given listener
     * @param listener object which will be notified when a slave is dead
     */
    public AOPinger(final SlaveDeadListener listener) {
        this.listener = listener;
        terminated = false;
        pingPeriod = Long.parseLong(PAProperties.PA_MASTERSLAVE_PINGPERIOD.getValue());
    }

    /**
     * {@inheritDoc}
     */
    public void addSlaveToWatch(final Slave slave) {
        slaveGroup.add(slave);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public void initActivity(final Body body) {
        try {
            slaveGroupStub = (Slave) ProGroup.newGroup(AOSlave.class.getName());
            slaveGroup = ProGroup.getGroup(slaveGroupStub);
            stubOnThis = (AOPinger) ProActiveObject.getStubOnThis();
            body.setImmediateService("terminate");
        } catch (ClassNotReifiableException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeSlaveToWatch(final Slave slave) {
        slaveGroup.remove(slave);
    }

    /**
     * {@inheritDoc}
     */
    public void runActivity(final Body body) {
        localThread = Thread.currentThread();
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
                // do not print message, pinger is terminating
            }
        }

        body.terminate();
    }

    /**
     * {@inheritDoc}
     */
    public void setPingPeriod(long periodMillis) {
        this.pingPeriod = periodMillis;
    }

    /**
     * Reports that a slave is missing
     * @param slave the missing slave
     */
    public void slaveMissing(final Slave slave) {
        synchronized (slaveGroup) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                    "A slave is missing...reporting back to the Master");
            }

            if (slaveGroup.contains(slave)) {
                listener.isDead(slave);
                slaveGroup.remove(slave);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public BooleanWrapper terminate() {
        this.terminated = true;
        localThread.interrupt();

        if (logger.isDebugEnabled()) {
            logger.debug("Pinger terminated...");
        }

        return new BooleanWrapper(true);
    }

    /*
     * TODO: handle exceptions with stubOnThis.slaveMissing((Slave) eig.getObject()); for each
     * member in the ExceptionListException
     */
}
