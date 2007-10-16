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
package org.objectweb.proactive.core.body.reply;

import java.io.IOException;

import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.future.FutureResult;
import org.objectweb.proactive.core.body.message.Message;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;


public interface Reply extends Message {
    public FutureResult getResult();

    /**
     * Sends this reply to the body destination
     * @param destinationBody the body destination of this reply
     * @return value used by fault-tolerance mechanism.
     * @exception java.io.IOException if the reply fails to be sent
     */
    public int send(UniversalBody destinationBody) throws IOException;

    // SECURITY
    public boolean isCiphered();

    public long getSessionId();

    public boolean decrypt(ProActiveSecurityManager psm)
        throws RenegotiateSessionException;

    // AUTOMATIC CONTINUATION
    public boolean isAutomaticContinuation();
}
