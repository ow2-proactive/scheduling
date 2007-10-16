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

import java.math.BigInteger;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;


/**
 * @author manu
 *
 */
public class Cons2 implements InitActive, RunActive {
    private Add add;
    private BigInteger fibN_1;

    //	Empty no arg constructor
    public Cons2() {
    }

    public void setFibN_1(BigInteger fibN_1) {
        this.fibN_1 = fibN_1;
    }

    /**
     * @param add The add to set.
     */
    public void setAdd(Add add) {
        this.add = add;
    }

    public void initActivity(Body body) {
        Service service = new Service(body);
        service.blockingServeOldest("setAdd");
    }

    public void runActivity(Body body) {
        int k = 0;
        Service service = new Service(body);
        add.setFibN_2(BigInteger.ZERO); // starting with 0
        k++;
        while (body.isActive()) {
            service.blockingServeOldest("setFibN_1");
            add.setFibN_2(fibN_1);
            System.out.println("Fib(" + k + ") = " + fibN_1);
            k++;
        }
    }
}
