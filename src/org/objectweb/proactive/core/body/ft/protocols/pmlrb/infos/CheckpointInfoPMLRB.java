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
package org.objectweb.proactive.core.body.ft.protocols.pmlrb.infos;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.core.body.ft.checkpointing.CheckpointInfo;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;


/**
 * Checkpoint additional informations for the PMLRB protocol. Contains message logs
 * used to recover the activity state
 * @author cdelbe
 */
public class CheckpointInfoPMLRB implements CheckpointInfo {
    // message logs
    private List requestLog;
    private List replyLog;

    // pending request
    private Request pending;

    /**
     * Checkpoint infos constructor.
     * @param pendingRequest the request that must be served after the recovery.
     */
    public CheckpointInfoPMLRB(Request pendingRequest) {
        this.pending = pendingRequest;
        this.replyLog = new ArrayList();
        this.requestLog = new ArrayList();
    }

    // GETTERs - SETTERs
    public Request getPendingRequest() {
        return this.pending;
    }

    public void setRequestLog(List requests) {
        this.requestLog = requests;
    }

    public void setReplyLog(List replies) {
        this.replyLog = replies;
    }

    public List getRequestLog() {
        return this.requestLog;
    }

    public List getReplyLog() {
        return this.replyLog;
    }

    public void addRequest(Request r) {
        this.requestLog.add(r);
    }

    public void addReply(Reply r) {
        this.replyLog.add(r);
    }
}
