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
package org.objectweb.proactive.core.body.ft.internalmsg;

import org.objectweb.proactive.core.body.ft.protocols.FTManager;
import org.objectweb.proactive.core.body.ft.protocols.cic.managers.FTManagerCIC;


/**
 * This class defines a message send to all processes (by the server) when one
 * of the process of the group is sending a message to the outside world (i.e. an
 * external element or a process belonging to another group)
 * @author cdelbe
 * @since 3.0
 */
public class OutputCommit implements FTMessage {

    /**
         *
         */
    private static final long serialVersionUID = 8277241393322154618L;
    private long lastIndexToRetreive;
    private long firstIndexToRetreive;

    /**
     * Create an output commit message.
     * @param firstIndex first index of history that must be commited
     * @param lastIndex last index of history that must be commited
     */
    public OutputCommit(long firstIndex, long lastIndex) {
        this.firstIndexToRetreive = firstIndex;
        this.lastIndexToRetreive = lastIndex;
    }

    public Object handleFTMessage(FTManager ftm) {
        return ((FTManagerCIC) ftm).handlingOCEvent(this);
    }

    public long getLastIndexToRetreive() {
        return this.lastIndexToRetreive;
    }

    public long getFirstIndexToRetreive() {
        return this.firstIndexToRetreive;
    }
}
