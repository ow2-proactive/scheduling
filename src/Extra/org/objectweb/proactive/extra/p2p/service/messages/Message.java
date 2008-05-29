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
package org.objectweb.proactive.extra.p2p.service.messages;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.p2p.service.P2PService;
import org.objectweb.proactive.extra.p2p.service.util.UniversalUniqueID;


public abstract class Message implements Serializable {
    protected static final Logger logger = ProActiveLogger.getLogger(Loggers.P2P_MESSAGE);
    protected int TTL;
    protected UniversalUniqueID uuid;
    protected P2PService sender;

    public Message() {
    }

    public Message(int ttl) {
        this.TTL = ttl;
    }

    public Message(int ttl, UniversalUniqueID id, P2PService sender) {
        this.TTL = ttl;
        this.uuid = id;
        this.sender = sender;
    }

    public int getTTL() {
        return TTL;
    }

    public void setTTL(int ttl) {
        TTL = ttl;
    }

    public UniversalUniqueID getUuid() {
        return uuid;
    }

    public void setUuid(UniversalUniqueID uuid) {
        this.uuid = uuid;
    }

    public P2PService getSender() {
        return sender;
    }

    public void setSender(P2PService s) {
        this.sender = s;
    }

    /**
     * Execute the message on the given local target
     * @param target
     */
    public abstract void execute(P2PService target);

    /**
     * Transmits the message to the next peer
     * @param acq
     */
    public abstract void transmit(P2PService acq);

    public boolean shouldExecute() {
        return true;
    }

    public boolean shouldTransmit() {
        return true;
    }
}
