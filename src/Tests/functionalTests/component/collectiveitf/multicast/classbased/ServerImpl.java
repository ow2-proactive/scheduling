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
package functionalTests.component.collectiveitf.multicast.classbased;

import java.util.List;

import functionalTests.component.collectiveitf.multicast.Identifiable;
import functionalTests.component.collectiveitf.multicast.WrappedInteger;


public class ServerImpl implements BroadcastServerItf, OneToOneServerItf, Identifiable {
    int id = 0;

    /*
     * @see functionalTests.component.collectiveitf.multicast.Identifiable#getID()
     */
    public String getID() {
        return new Integer(id).toString();
    }

    /*
     * @see functionalTests.component.collectiveitf.multicast.Identifiable#setID(java.lang.String)
     */
    public void setID(String id) {
        this.id = new Integer(id);
    }

    /*
     * @see functionalTests.component.collectiveitf.multicast.classbased.BroadcastServerItf#dispatch(java.util.List)
     */
    public WrappedInteger dispatch(List<WrappedInteger> l) {
        functionalTests.component.collectiveitf.multicast.ServerImpl s = new functionalTests.component.collectiveitf.multicast.ServerImpl();
        s.setID(getID());
        return s.testBroadcast_Param(l);
    }

    /*
     * @see functionalTests.component.collectiveitf.multicast.classbased.OneToOneServerItf#dispatch(functionalTests.component.collectiveitf.multicast.WrappedInteger)
     */
    public WrappedInteger dispatch(WrappedInteger i) {
        functionalTests.component.collectiveitf.multicast.ServerImpl s = new functionalTests.component.collectiveitf.multicast.ServerImpl();
        s.setID(getID());
        return s.testOneToOne_Method(i);
    }
}
