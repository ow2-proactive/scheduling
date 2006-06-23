/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.event.BodyEventListener;
import org.objectweb.proactive.core.event.BodyEventProducerImpl;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * <p>
 * This class store all active bodies known in the current JVM. The class is a singleton
 * in a given JVM. It also associates each active thread with its matching body.
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 * @see Body
 * @see UniqueID
 *
 */
public class LocalBodyStore {
    //
    // -- STATIC MEMBERS -----------------------------------------------
    //
    static Logger logger = ProActiveLogger.getLogger(Loggers.BODY);
    private static LocalBodyStore instance = new LocalBodyStore();

    //
    // -- PRIVATE MEMBERS -----------------------------------------------
    //

    /**
     * This table maps all known active Bodies in this JVM with their UniqueID
     * From one UniqueID it is possible to get the corresponding body if
     * it belongs to this JVM
     */
    private BodyMap localBodyMap = new BodyMap();

    /**
     * This table maps all known HalfBodies in this JVM with their UniqueID
     * From one UniqueID it is possible to get the corresponding halfbody if
     * it belongs to this JVM
     */
    private BodyMap localHalfBodyMap = new BodyMap();

    /**
     * This table maps all known Forwarder's in this JVM with their UniqueID
     * From one UniqueID it is possible to get the corresponding forwarder if
     * it belongs to this JVM. Used by the HTTP ProActive protocol, so that
     * when a Body migrates, it is still possible to talk to the object using
     * its BodyID, that here now points to the Forwarder instead of the original
     * Body.
     */
    private BodyMap localForwarderMap = new BodyMap();

    /**
     * Static object that manages the registration of listeners and the sending of
     * events
     */
    private BodyEventProducerImpl bodyEventProducer = new BodyEventProducerImpl();
    private ThreadLocal bodyPerThread = new ThreadLocal();
    private MetaObjectFactory halfBodyMetaObjectFactory = null;

    /** Should the JVM be killed when it has no more active bodies? */
    private boolean exitOnEmpty = false;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * Creates a new AbstractBody.
     * Used for serialization.
     */
    private LocalBodyStore() {
    }

    //
    // -- STATIC METHODS -----------------------------------------------
    //
    public static LocalBodyStore getInstance() {
        return instance;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    public synchronized MetaObjectFactory getHalfBodyMetaObjectFactory() {
        if (this.halfBodyMetaObjectFactory == null) {
            halfBodyMetaObjectFactory = ProActiveMetaObjectFactory.newInstance();
        }
        return halfBodyMetaObjectFactory;
    }

    public synchronized void setHalfBodyMetaObjectFactory(
        MetaObjectFactory factory) {
        halfBodyMetaObjectFactory = factory;
    }

    /**
     * Returns the body associated with the thread calling the method. If no body is associated with the
     * calling thread, an HalfBody is created to manage the futures.
     * @return the body associated to the active object whose active thread is calling this method.
     */
    public Body getCurrentThreadBody() {
        AbstractBody body = (AbstractBody) bodyPerThread.get();
        if (body == null) {
            // If we cannot find the body from the current thread we assume that the current thread
            // is not the one from an active object. Therefore in this case we create an HalfBody
            // that handle the futures
            body = HalfBody.getHalfBody(this.getHalfBodyMetaObjectFactory());
            bodyPerThread.set(body);
            registerHalfBody(body);
        }

        return body;
    }

    /**
     * Associates the body with the thread calling the method.
     * @param body the body to associate to the active thread that calls this method.
     */
    public void setCurrentThreadBody(Body body) {
        bodyPerThread.set(body);
    }

    /**
     * Returns the body belonging to this JVM whose ID is the one specified.
     * Returns null if a body with such an id is not found in this jvm
     * @param bodyID the ID to look for
     * @return the body with matching id or null
     */
    public Body getLocalBody(UniqueID bodyID) {
        return (Body) localBodyMap.getBody(bodyID);
    }

    /**
     * Returns the halfbody belonging to this JVM whose ID is the one specified.
     * Returns null if a halfbody with such an id is not found in this jvm
     * @param bodyID the ID to look for
     * @return the halfbody with matching id or null
     */
    public Body getLocalHalfBody(UniqueID bodyID) {
        return (Body) localHalfBodyMap.getBody(bodyID);
    }

    /**
     * Returns the forwarder belonging to this JVM whose ID is the one specified.
     * Returns null if a forwarder with such an id is not found in this jvm
     * @param bodyID the ID to look for
     * @return the halfbody with matching id or null
     */
    public Body getForwarder(UniqueID bodyID) {
        return (Body) localForwarderMap.getBody(bodyID);
    }

    /**
     * Returns all local Bodies in a new BodyMap
     * @return all local Bodies in a new BodyMap
     */
    public BodyMap getLocalBodies() {
        return (BodyMap) localBodyMap.clone();
    }

    /**
     * Returns all local HalfBodies in a new BodyMap
     * @return all local HalfBodies in a new BodyMap
     */
    public BodyMap getLocalHalfBodies() {
        return (BodyMap) localHalfBodyMap.clone();
    }

    /**
     * Adds a listener of body events. The listener is notified every time a body
     * (active or not) is registered or unregistered in this JVM.
     * @param listener the listener of body events to add
     */
    public void addBodyEventListener(BodyEventListener listener) {
        bodyEventProducer.addBodyEventListener(listener);
    }

    /**
     * Removes a listener of body events.
     * @param listener the listener of body events to remove
     */
    public void removeBodyEventListener(BodyEventListener listener) {
        bodyEventProducer.removeBodyEventListener(listener);
    }

    private void checkExitOnEmpty() {
    	if (this.exitOnEmpty && this.localBodyMap.size() == 0) {
    		System.exit(0);
    	}
    }
    
    /**
     * After this call, when the JVM has no more active objects
     * it will be killed.
     *
     */
    public void enableExitOnEmpty() {
    	this.exitOnEmpty = true;
    	checkExitOnEmpty();
    }

    //
    // -- FRIENDLY METHODS -----------------------------------------------
    //
    void registerBody(AbstractBody body) {
        if (localBodyMap.getBody(body.getID()) != null) {
            logger.warn("Body already registered in the body map");
        }
        localBodyMap.putBody(body.bodyID, body);
        bodyEventProducer.fireBodyCreated(body);
    }

    void unregisterBody(AbstractBody body) {
        localBodyMap.removeBody(body.bodyID);
        bodyEventProducer.fireBodyRemoved(body);
        checkExitOnEmpty();
    }

    void registerHalfBody(AbstractBody body) {
        localHalfBodyMap.putBody(body.bodyID, body);

        //bodyEventProducer.fireBodyCreated(body);
    }

    void unregisterHalfBody(AbstractBody body) {
        localHalfBodyMap.removeBody(body.bodyID);
        //bodyEventProducer.fireBodyRemoved(body);
    }

    public void registerForwarder(AbstractBody body) {
        if (localForwarderMap.getBody(body.bodyID) != null) {
            logger.debug("Forwarder already registered in the body map");
            localForwarderMap.removeBody(body.bodyID);
        }
        localForwarderMap.putBody(body.bodyID, body);
    }

    public void unregisterForwarder(AbstractBody body) {
        localForwarderMap.removeBody(body.bodyID);
    }
}
