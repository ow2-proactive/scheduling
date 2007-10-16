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


/**
 * This class is the default implementation of the Body interface.
 * An implementation of the Body interface, which lets the reified object
 * explicitely manage the queue of pending requests through its live() routine.
 *
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 * @see org.objectweb.proactive.Body
 * @see AbstractBody
 * @see org.objectweb.proactive.core.body.migration.MigratableBody
 *
 */
import org.apache.log4j.Logger;
import org.objectweb.proactive.Active;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.body.request.BlockingRequestQueue;
import org.objectweb.proactive.core.component.body.ComponentActivity;
import org.objectweb.proactive.core.component.body.ComponentBodyImpl;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class ActiveBody extends ComponentBodyImpl implements Runnable,
    java.io.Serializable {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.BODY);

    //
    // -- STATIC MEMBERS -----------------------------------------------
    //
    //
    // -- PROTECTED MEMBERS -----------------------------------------------
    //
    //
    // -- PRIVATE MEMBERS -----------------------------------------------
    //
    private transient InitActive initActive; // used only once when active object is started first time
    private RunActive runActive;
    private EndActive endActive;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * Doesn't build anything, just for having one no-arg constructor
     */
    public ActiveBody() {
    }

    /**
     * Builds the body object, then fires its service thread
     */
    public ActiveBody(ConstructorCall c, String nodeURL, Active activity,
        MetaObjectFactory factory, String jobID)
        throws java.lang.reflect.InvocationTargetException,
            ConstructorCallExecutionFailedException,
            ActiveObjectCreationException {
        // Creates the reified object
        super(c.execute(), nodeURL, activity, factory, jobID);

        Object reifiedObject = this.localBodyStrategy.getReifiedObject();

        // when building a component, encapsulate the functional activity
        // TODO_M read some flag before doing this?
        if (getProActiveComponentImpl() != null) {
            activity = new ComponentActivity(activity, reifiedObject);
        }

        // InitActive
        if ((activity != null) && activity instanceof InitActive) {
            this.initActive = (InitActive) activity;
        } else if (reifiedObject instanceof InitActive) {
            this.initActive = (InitActive) reifiedObject;
        }

        // RunActive
        if ((activity != null) && activity instanceof RunActive) {
            this.runActive = (RunActive) activity;
        } else if (reifiedObject instanceof RunActive) {
            this.runActive = (RunActive) reifiedObject;
        } else {
            this.runActive = new FIFORunActive();
        }

        // EndActive
        if ((activity != null) && activity instanceof EndActive) {
            this.endActive = (EndActive) activity;
        } else if (reifiedObject instanceof EndActive) {
            this.endActive = (EndActive) reifiedObject;
        } else {
            this.endActive = null;
        }

        startBody();
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //
    // -- implements Runnable -----------------------------------------------
    //

    /**
     * The method executed by the active thread that will eventually launch the live
     * method of the active object of the default live method of this body.
     */
    public void run() {
        // Notify the rmi thread that the body has been registered into
        // the localBodyStore.
        synchronized (this) {
            activityStarted();
            this.notify();
        }

        // execute the initialization if needed. Only once
        if (this.initActive != null) {
            this.initActive.initActivity(this);
            this.initActive = null; // we won't do it again
        }

        // run the activity of the body
        try {

            /* We may race with a termination request in immediate service */
            RunActive thisRunActive = this.runActive;
            if (thisRunActive != null) {
                thisRunActive.runActivity(this);
            }

            // the body terminate its activity
            if (isActive()) {
                // serve remaining requests if non dead
                for (;;) {
                    BlockingRequestQueue queue;
                    try {
                        /* We may race with a termination request in immediate service */
                        queue = this.localBodyStrategy.getRequestQueue();
                        if (queue.isEmpty()) {
                            break;
                        }
                    } catch (ProActiveRuntimeException pre) {
                        break;
                    }
                    serve(queue.removeOldest());
                }
            }
        } catch (Exception e) {
            logger.error("Exception occured in runActivity method of body " +
                toString() + ". Now terminating the body");

            e.printStackTrace();
            terminate();
        } finally {
            // execute the end of activity if not after migration
            if ((!this.hasJustMigrated) && (this.endActive != null)) {
                this.endActive.endActivity(this);
            }

            if (isActive()) {
                activityStopped(!this.getFuturePool().remainingAC());
            }
        }
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    /**
     * Creates the active thread and start it using this runnable body.
     */
    public void startBody() {
        if (logger.isDebugEnabled()) {
            logger.debug("Starting Body");
        }

        // get the incarnation number if ft is enable
        String inc = (this.ftmanager != null) ? ("" + this.ftmanager) : ("");
        Thread t = new Thread(this,
                shortClassName(getName()) + " on " + getNodeURL() + inc);

        // Wait for the registration of this Body inside the LocalBodyStore
        // to avoid a race condition (t not yet scheduled and getActiveObjects() called)
        synchronized (this) {
            t.start();
            try {
                this.wait();
            } catch (InterruptedException e) {
                logger.warn(e.getMessage(), e);
            }
        }
    }

    /**
     * Signals that the activity of this body, managed by the active thread has just stopped.
     * @param completeACs if true, and if there are remaining AC in the futurepool, the AC thread
     * is not killed now; it will be killed after the sending of the last remaining AC.
     */
    @Override
    protected void activityStopped(boolean completeACs) {
        super.activityStopped(completeACs);
        this.runActive = null;
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private static String shortClassName(String fqn) {
        int n = fqn.lastIndexOf('.');
        if ((n == -1) || (n == (fqn.length() - 1))) {
            return fqn;
        }
        return fqn.substring(n + 1);
    }

    private void writeObject(java.io.ObjectOutputStream out)
        throws java.io.IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("out = " + out);
        }
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in)
        throws java.io.IOException, ClassNotFoundException {
        if (logger.isDebugEnabled()) {
            logger.debug("in = " + in);
        }
        in.defaultReadObject();
        // FAULT-TOLERANCE: if this is a recovering checkpoint,
        // activity will be started in ProActiveRuntimeImpl.receiveCheckpoint()
        if (this.ftmanager != null) {
            if (!this.ftmanager.isACheckpoint()) {
                startBody();
            }
        } else {
            startBody();
        }
    }

    //
    // -- INNER CLASSES -----------------------------------------------
    //
    private class FIFORunActive implements RunActive, java.io.Serializable {
        public void runActivity(Body body) {
            new Service(body).fifoServing();
        }
    }
}
