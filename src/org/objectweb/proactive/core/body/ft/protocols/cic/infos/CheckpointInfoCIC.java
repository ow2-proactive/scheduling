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
package org.objectweb.proactive.core.body.ft.protocols.cic.infos;

import java.util.Iterator;
import java.util.Vector;

import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;


/**
 * This class contains several informations about a checkpoint.
 * in the context of the CIC protocol.
 * @author cdelbe
 * @since ProActive 2.2
 */
public class CheckpointInfoCIC
    implements org.objectweb.proactive.core.body.ft.checkpointing.CheckpointInfo {

    /** The index of the linked checkpoint */
    public int checkpointIndex;

    /** The logged requests, i.e. requests that have to be resend on recovery from the linked checkpoint */
    public Vector requestToResend;

    /** The logged replies, i.e. replies that have to be resend on recovery from the linked checkpoint */
    public Vector replyToResend;

    /** The pending request when the checkpoint has occured, This request must be first served on recovery from the linked checkpoint */
    public Request pendingRequest;

    /** The history of the linked checkpoint, i.e. a list of awaited request that have to be append to the request queue on recovery from the linked checkpoint */
    public Vector history;

    /** The reception index of the latest received request when the checkpoint has occured */
    public long lastRcvdRequestIndex; //set by the owner

    /** The index of the last element of the latest commited history */
    public long lastCommitedIndex; //set by the server

    /**
     * "Pretty" printing
     */
    public String toString() {
        StringBuffer r = new StringBuffer();
        r.append("---------------------------------------------\n");
        r.append("CkptIndex      : " + this.checkpointIndex + "\n");
        Iterator itrep = replyToResend.iterator();
        r.append("Logged replies :\n");
        while (itrep.hasNext()) {
            r.append("   >" + ((Reply) (itrep.next())).getResult() + "\n");
        }
        r.append("---------------------------------------------\n");
        return r.toString();
    }
}
