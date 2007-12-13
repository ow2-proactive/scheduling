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
package org.objectweb.proactive.core.body.ft.protocols.pmlrb.infos;

import org.objectweb.proactive.core.body.ft.exception.NotImplementedException;
import org.objectweb.proactive.core.body.ft.message.MessageInfo;
import org.objectweb.proactive.core.body.ft.protocols.FTManagerFactory;


/**
 * Informations piggybacked on messages with the PMLRB protocol.
 * @author cdelbe
 * @since 3.0
 */
public class MessageInfoPMLRB implements MessageInfo {

    /**
     *
     */
    public long sentSequenceNumber;

    /**
     * @see org.objectweb.proactive.core.body.ft.message.MessageInfo#getProtocolType()
     */
    public int getProtocolType() {
        return FTManagerFactory.PROTO_PML_ID;
    }

    /**
     * @see org.objectweb.proactive.core.body.ft.message.MessageInfo#isFromHalfBody()
     */
    public boolean isFromHalfBody() {
        throw new NotImplementedException();
    }
}
