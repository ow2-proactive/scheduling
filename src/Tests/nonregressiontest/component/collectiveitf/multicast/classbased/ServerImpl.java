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
package nonregressiontest.component.collectiveitf.multicast.classbased;

import java.util.List;

import nonregressiontest.component.collectiveitf.multicast.Identifiable;
import nonregressiontest.component.collectiveitf.multicast.WrappedInteger;


public class ServerImpl implements BroadcastServerItf, OneToOneServerItf,
    Identifiable {
    int id = 0;

    /*
     * @see nonregressiontest.component.collectiveitf.multicast.Identifiable#getID()
     */
    public String getID() {
        return new Integer(id).toString();
    }

    /*
     * @see nonregressiontest.component.collectiveitf.multicast.Identifiable#setID(java.lang.String)
     */
    public void setID(String id) {
        this.id = new Integer(id);
    }

    /*
     * @see nonregressiontest.component.collectiveitf.multicast.classbased.BroadcastServerItf#dispatch(java.util.List)
     */
    public WrappedInteger dispatch(List<WrappedInteger> l) {
        nonregressiontest.component.collectiveitf.multicast.ServerImpl s = new nonregressiontest.component.collectiveitf.multicast.ServerImpl();
        s.setID(getID());
        return s.testBroadcast_Param(l);
    }

    /*
     * @see nonregressiontest.component.collectiveitf.multicast.classbased.OneToOneServerItf#dispatch(nonregressiontest.component.collectiveitf.multicast.WrappedInteger)
     */
    public WrappedInteger dispatch(WrappedInteger i) {
        nonregressiontest.component.collectiveitf.multicast.ServerImpl s = new nonregressiontest.component.collectiveitf.multicast.ServerImpl();
        s.setID(getID());
        return s.testOneToOne_Method(i);
    }
}
