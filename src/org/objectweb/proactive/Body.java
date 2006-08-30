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
package org.objectweb.proactive;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.LocalBodyStrategy;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.message.MessageEventProducer;
import org.objectweb.proactive.core.component.representative.ItfID;
import org.objectweb.proactive.ext.security.PolicyServer;


/**
 * <P>
 * An object implementing this interface is an implementation of the non fonctionnal part
 * of an ActiveObject. This representation is local to the ActiveObject. By contrast there
 * is a remote representation of Body that can be accessed by distant object.
 * </P><P>
 * <code>UniversalBody</code> defines the remote accessible part of the body while
 * <code>LocalBody</code> defines the local accessible part of the body.
 * </P><P>
 * The body of an ActiveObject provides needed services such as a the ability to sent and
 * receive request and reply.
 * </P><P>
 * The interface also defines how the activity methods of an active object sees its Body.
 * </P><P>
 * A body has 2 associated states :
 * <ul>
 * <li>alive : the body is alive as long as it is processing request and reply</li>
 * <li>active : the body is active as long as it has an associated thread running
 *              to serve the requests by calling methods on the active object.</li>
 * </ul>
 * </P><P>
 * Note that a thread can be alive but not active, such as a forwarder that just
 * forward request to another peer.
 * </P>
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 */
public interface Body extends LocalBodyStrategy, UniversalBody,
    MessageEventProducer {

    /**
     * Returns whether the body is alive or not.
     * The body is alive as long as it is processing request and reply
     * @return whether the body is alive or not.
     */
    public boolean isAlive();

    /**
     * Returns whether the body is active or not.
     * The body is active as long as it has an associated thread running
     * to serve the requests by calling methods on the active object.
     * @return whether the body is active or not.
     */
    public boolean isActive();

    /**
     * blocks all incoming communications. After this call, the body cannot
     * receive any request or reply.
     */
    public void blockCommunication();

    /**
     * Signals the body to accept all incoming communications. This call undo
     * a previous call to blockCommunication.
     */
    public void acceptCommunication();

    /**
     * Allows the calling thread to enter in the ThreadStore of this body.
     */
    public void enterInThreadStore();

    /**
     * Allows the calling thread to exit from the ThreadStore of this body.
     */
    public void exitFromThreadStore();

    /**
     * Tries to find a local version of the body of id uniqueID. If a local version
     * is found it is returned. If not, tries to find the body of id uniqueID in the
     * known body of this body. If a body is found it is returned, else null is returned.
     * @param uniqueID the id of the body to lookup
     * @return the last known version of the body of id uniqueID or null if not known
     */
    public UniversalBody checkNewLocation(UniqueID uniqueID);

    /**
     * Returns the body that is the target of this shortcut for this component interface
     * @param functionalItfID the id of the interface on which the shortcut is available
     * @return the body that is the target of this shortcut for this interface
     */
    public UniversalBody getShortcutTargetBody(
        ItfID functionalItfID);

    /**
     * set the policy server of the active object
     * @param server the policy server
     */
    public void setPolicyServer(PolicyServer server);

    /**
     * Set the nodeURL of this body
     * @param newNodeURL the new URL of the node
     */
    public void updateNodeURL(String newNodeURL);
}
