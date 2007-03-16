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
package org.objectweb.proactive.ic2d.spy;

import org.objectweb.proactive.core.UniqueID;


public class BodySpyEvent extends SpyEvent implements java.io.Serializable {

    /** whether the body has an activity done with a active thread */
    private boolean isActive;

    /** whether the body has been killed. */
    private boolean isAlive;

    public BodySpyEvent(UniqueID bodyID) {
        this(bodyID, false, true);
    }

    public BodySpyEvent(UniqueID bodyID, boolean isActive) {
        this(bodyID, isActive, true);
    }

    public BodySpyEvent(UniqueID bodyID, boolean isActive, boolean isAlive) {
        this(BODY_EVENT_TYPE, bodyID, isActive, isAlive);
    }

    protected BodySpyEvent(int eventType, UniqueID bodyID, boolean isActive,
        boolean isAlive) {
        super(eventType, bodyID);
        this.isActive = isActive;
        this.isAlive = isAlive;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isAlive() {
        return isAlive;
    }
}
