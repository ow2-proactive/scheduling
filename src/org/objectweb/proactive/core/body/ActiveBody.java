/*
* ################################################################
*
* ProActive: The Java(TM) library for Parallel, Distributed,
*            Concurrent computing with Security and Mobility
*
* Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
* Contact: proactive-support@inria.fr
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
* USA
*
*  Initial developer(s):               The ProActive Team
*                        http://www.inria.fr/oasis/ProActive/contacts.html
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
import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.core.body.migration.MigratableBody;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;


public class ActiveBody extends MigratableBody implements Runnable,
    java.io.Serializable {
    protected static Logger logger = Logger.getLogger(ActiveBody.class.getName());

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
     * Build the body object, then fires its service thread
     */
    public ActiveBody(ConstructorCall c, String nodeURL, Active activity,
        MetaObjectFactory factory, String jobID)
        throws java.lang.reflect.InvocationTargetException, 
            ConstructorCallExecutionFailedException {
        // Creates the reified object
        super(c.execute(), nodeURL, factory, jobID);

        // InitActive
        Object reifiedObject = localBodyStrategy.getReifiedObject();
        if ((activity != null) && activity instanceof InitActive) {
            initActive = (InitActive) activity;
        } else if (reifiedObject instanceof InitActive) {
            initActive = (InitActive) reifiedObject;
        }

        // RunActive
        if ((activity != null) && activity instanceof RunActive) {
            runActive = (RunActive) activity;
        } else if (reifiedObject instanceof RunActive) {
            runActive = (RunActive) reifiedObject;
        } else {
            runActive = new FIFORunActive();
        }

        // EndActive
        if ((activity != null) && activity instanceof EndActive) {
            endActive = (EndActive) activity;
        } else if (reifiedObject instanceof EndActive) {
            endActive = (EndActive) reifiedObject;
        } else {
            endActive = null;
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
        activityStarted();
        // execute the initialization if needed. Only once
        if (initActive != null) {
            initActive.initActivity(this);
            initActive = null; // we won't do it again
        }

        // run the activity of the body
        try {
            runActive.runActivity(this);
            // the body terminate its activity
            if (isActive()) {
                // serve remaining requests if non dead
                while (!(localBodyStrategy.getRequestQueue().isEmpty())) {
                    serve(localBodyStrategy.getRequestQueue().removeOldest());
                }
            }
        } catch (Exception e) {
            
            logger.error("Exception occured in runActivity method of body " +
                    toString() + ". Now terminating the body");
           
            e.printStackTrace();
            terminate();
        } finally {
            if (isActive()) {
                activityStopped();
            }

            // execute the end of activity if not after migration
            if ((!hasJustMigrated) && (endActive != null)) {
                endActive.endActivity(this);
            }
        }
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //

    /**
     * Creates the active thread and start it using this runnable body.
     */
    protected void startBody() {
        if (logger.isDebugEnabled()) {
            logger.debug("Starting Body");
        }
        Thread t = new Thread(this,
                shortClassName(getName()) + " on " + getNodeURL());
        t.start();
    }

    /**
     * Signals that the activity of this body, managed by the active thread has just stopped.
     */
    protected void activityStopped() {
        super.activityStopped();
        runActive = null;
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
        startBody();
    }

    //
    // -- INNER CLASSES -----------------------------------------------
    //
    private class FIFORunActive implements RunActive, java.io.Serializable {
        public void runActivity(Body body) {
            while (isActive()) {
                serve(localBodyStrategy.getRequestQueue().blockingRemoveOldest());
            }
        }
    }
}
