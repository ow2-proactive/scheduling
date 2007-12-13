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
package org.objectweb.proactive.core.security;

import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;


/**
 * A message implementing this interface can be handled by the security
 * mechanism.
 *
 * This interface <b>must</b> be implemented by all messages exchanged
 * between security entities. It includes Request, Reply, BodyRequest, etc.
 *
 *
 */
public interface Securizable {

    /**
     * @return return true if the message is ciphered
     */
    public boolean isCiphered();

    /**
     * @return return session Id identifying the security
     * session
     */
    public long getSessionId();

    /**
     * @param psm ProActiveSecurityManager
     * @return true if decrypt method succeeds
     * @throws RenegotiateSessionException This exception is thrown when a session
     * corresponding to the sessionId is not found. The error must be returned to the sender
     * in order to renegociate a new session and re-send the message
     */
    public boolean decrypt(ProActiveSecurityManager psm) throws RenegotiateSessionException;

    /**
     * @param psm the proactiveSecurityManager of the entity
     * @return true the message can be emitted.
     * @throws RenegotiateSessionException
     */
    public boolean crypt(ProActiveSecurityManager psm, SecurityEntity destinationBody)
            throws RenegotiateSessionException;
}
