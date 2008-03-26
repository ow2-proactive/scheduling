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
package org.objectweb.proactive.core.body.ft.protocols.cic.infos;

import java.util.Hashtable;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.ft.message.MessageInfo;
import org.objectweb.proactive.core.body.ft.protocols.FTManagerFactory;
import org.objectweb.proactive.core.util.MutableLong;


/**
 * @author The ProActive Team
 * @since 2.2
 */
public class MessageInfoCIC implements MessageInfo {

    /**
     *
     */

    // checkpointing protocol
    public char checkpointIndex;
    public char historyIndex;
    public char incarnation;
    public char lastRecovery;
    public char isOrphanFor;
    public boolean fromHalfBody;

    // output commit protocol
    public long positionInHistory;
    public Hashtable<UniqueID, MutableLong> vectorClock;

    /**
     * @see org.objectweb.proactive.core.body.ft.message.MessageInfo#getProtocolType()
     */
    public int getProtocolType() {
        return FTManagerFactory.PROTO_CIC_ID;
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.message.MessageInfo#isFromHalfBody()
     */
    public boolean isFromHalfBody() {
        return this.fromHalfBody;
    }
}
