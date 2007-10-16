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
package org.objectweb.proactive.p2p.v2.monitoring;

import java.io.Serializable;


public class AcquaintanceInfo implements Serializable {
    protected String sender;
    protected String[] acq;
    protected int noa;
    protected int currentNoa;
    protected String[] awaitedReplies;

    public AcquaintanceInfo() {
    }

    public AcquaintanceInfo(String sender, String[] acq) {
        this.sender = sender;
        this.acq = acq;
    }

    public AcquaintanceInfo(String sender, String[] acq, int noa, int currentNoa) {
        this.sender = sender;
        this.acq = acq;
        this.noa = noa;
        this.currentNoa = currentNoa;
    }

    public AcquaintanceInfo(String sender, String[] acq, int noa,
        int currentNoa, String[] awaited) {
        this(sender, acq, noa, currentNoa);
        this.awaitedReplies = awaited;
    }

    public String[] getAcq() {
        return acq;
    }

    public String getSender() {
        return sender;
    }

    public int getNoa() {
        return noa;
    }

    public int getCurrentNoa() {
        return this.currentNoa;
    }

    public String[] getAwaitedReplies() {
        return awaitedReplies;
    }
}
