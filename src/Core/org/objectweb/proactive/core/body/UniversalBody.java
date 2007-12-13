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

import java.io.IOException;
import java.io.Serializable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Job;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.ft.internalmsg.FTMessage;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.component.request.Shortcut;
import org.objectweb.proactive.core.gc.GCMessage;
import org.objectweb.proactive.core.gc.GCResponse;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.security.SecurityEntity;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * An object implementing this interface provides the minimum service a body offers
 * remotely or locally. This interface is the generic version that is used remotely
 * and locally. A body accessed from the same JVM offers all services of this interface,
 * plus the services defined in the Body interface.
 *
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 * @see org.objectweb.proactive.Body
 * @see org.objectweb.proactive.core.body.rmi.RmiBodyAdapter
 */
public interface UniversalBody extends Job, Serializable, SecurityEntity {
    public static Logger bodyLogger = ProActiveLogger.getLogger(Loggers.BODY);
    public static Logger sendReplyExceptionsLogger = ProActiveLogger.getLogger(Loggers.EXCEPTIONS_SEND_REPLY);

    /**
     * Receives a request for later processing. The call to this method is non blocking
     * unless the body cannot temporary receive the request.
     * @param request the request to process
     * @exception java.io.IOException if the request cannot be accepted
     * @return value for fault-tolerance protocol
     */
    public int receiveRequest(Request request) throws java.io.IOException, RenegotiateSessionException;

    /**
     * Receives a reply in response to a former request.
     * @param r the reply received
     * @exception java.io.IOException if the reply cannot be accepted
     * @return value for fault-tolerance procotol
     */
    public int receiveReply(Reply r) throws java.io.IOException;

    /**
     * Returns the url of the node this body is associated to
     * The url of the node can change if the active object migrates
     * @return the url of the node this body is associated to
     */
    public String getNodeURL();

    /**
     * Returns the UniqueID of this body
     * This identifier is unique accross all JVMs
     * @return the UniqueID of this body
     */
    public UniqueID getID();

    /**
     * Signals to this body that the body identified by id is now to a new
     * remote location. The body given in parameter is a new stub pointing
     * to this new location. This call is a way for a body to signal to his
     * peer that it has migrated to a new location
     * @param id the id of the body
     * @param body the stub to the new location
     * @exception java.io.IOException if a pb occurs during this method call
     */
    public void updateLocation(UniqueID id, UniversalBody body) throws java.io.IOException;

    /**
     * similar to the {@link UniversalBody#updateLocation(org.objectweb.proactive.core.UniqueID, UniversalBody)} method,
     * it allows direct communication to the target of a functional call, accross membranes of composite components.
     * @param shortcut the shortcut to create
     * @exception java.io.IOException if a pb occurs during this method call
     */
    public void createShortcut(Shortcut shortcut) throws java.io.IOException;

    /**
     * Returns the remote friendly version of this body
     * @return the remote friendly version of this body
     */
    public UniversalBody getRemoteAdapter();

    /**
     * Returns the name of the class of the reified object
     * @return the name of the class of the reified object
     */
    public String getReifiedClassName();

    /**
     * Enables automatic continuation mechanism for this body
     * @exception java.io.IOException if a pb occurs during this method call
     */
    public void enableAC() throws java.io.IOException;

    /**
     * Disables automatic continuation mechanism for this body
     * @exception java.io.IOException if a pb occurs during this method call
     */
    public void disableAC() throws java.io.IOException;

    // FAULT TOLERANCE

    /**
     * For sending a non fonctional message to the FTManager linked to this object.
     * @param ev the message to send
     * @return depends on the message meaning
     * @exception java.io.IOException if a pb occurs during this method call
     */
    public Object receiveFTMessage(FTMessage ev) throws IOException;

    /**
     * The DGC broadcasting method, called every GarbageCollector.TTB between
     * referenced active objects. The GC{Message,Response} may actually be
     * composed of many GCSimple{Message,Response}.
     *
     * @param toSend the message
     * @return its associated response
     * @throws IOException if a pb occurs during this method call
     */
    public GCResponse receiveGCMessage(GCMessage toSend) throws IOException;

    /**
     * Inform the DGC that an active object is pinned somewhere so cannot
     * be garbage collected until being unregistered.
     * @param registered true for a registration, false for an unregistration
     * @throws IOException if a pb occurs during this method call
     */
    public void setRegistered(boolean registered) throws IOException;

    public void register(String url) throws IOException, UnknownProtocolException;
}
