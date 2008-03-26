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
package org.objectweb.proactive.examples.fibonacci;

import java.io.Serializable;
import java.math.BigInteger;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;


/**
 * @author The ProActive Team
 *
 */
public class Cons1 implements Serializable, InitActive, RunActive {
    private Add add;
    private Cons2 cons2;
    private BigInteger fibN;

    //Empty no arg constructor
    public Cons1() {
    }

    /**
     * @param add The add to set.
     */
    public void setAdd(Add add) {
        this.add = add;
    }

    /**
     * @param cons2 The cons2 to set.
     */
    public void setCons2(Cons2 cons2) {
        this.cons2 = cons2;
    }

    /**
     * @param fibN The fibN to set.
     */
    public void setFibN(BigInteger fibN) {
        this.fibN = fibN;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        Service service = new Service(body);
        service.blockingServeOldest("setAdd");
        service.blockingServeOldest("setCons2");
    }

    public void runActivity(Body body) {
        Service service = new Service(body);
        add.setFibN_1(BigInteger.ONE); // starting with 1 
        cons2.setFibN_1(BigInteger.ONE); // starting with 1

        while (body.isActive()) {
            service.blockingServeOldest("setFibN");
            add.setFibN_1(fibN);
            cons2.setFibN_1(fibN);
        }
    }
}
