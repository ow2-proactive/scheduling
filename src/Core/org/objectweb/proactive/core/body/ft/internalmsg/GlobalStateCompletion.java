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
package org.objectweb.proactive.core.body.ft.internalmsg;

import org.objectweb.proactive.core.body.ft.protocols.FTManager;
import org.objectweb.proactive.core.body.ft.protocols.cic.managers.FTManagerCIC;


/**
 * This event indicates to the receiver that a global state has been completed.
 * @author cdelbe
 * @since ProActive 2.2
 */
public class GlobalStateCompletion implements FTMessage {

    /**
         *
         */
    private static final long serialVersionUID = -3950343920191825755L;
    private int index;

    /**
     * Create a non-fonctional message.
     * @param index the index of the completed global state
     */
    public GlobalStateCompletion(int index) {
        this.index = index;
    }

    public int getIndex() {
        return this.index;
    }

    public Object handleFTMessage(FTManager ftm) {
        return ((FTManagerCIC) ftm).handlingGSCEEvent(this);
    }
}
