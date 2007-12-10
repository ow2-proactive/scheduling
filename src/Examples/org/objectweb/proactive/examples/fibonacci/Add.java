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

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;


/**
 * @author manu
 *
 */
public class Add implements Serializable, InitActive, RunActive {
    private Cons1 cons1;
    private BigInteger fibN_1;
    private BigInteger fibN_2;

    //Empty noArg constructor
    public Add() {
    }

    public void initActivity(Body body) {
        Service service = new Service(body);
        service.blockingServeOldest("setCons1");
    }

    /**
     * @param cons1 The cons1 to set.
     */
    public void setCons1(Cons1 cons1) {
        this.cons1 = cons1;
    }

    /**
     * @param fibN_1 The fibN_1 to set.
     */
    public void setFibN_1(BigInteger fibN_1) {
        this.fibN_1 = fibN_1;
    }

    /**
     * @param fibN_2 The fibN_2 to set.
     */
    public void setFibN_2(BigInteger fibN_2) {
        this.fibN_2 = fibN_2;
    }

    public void runActivity(Body body) {
        Service service = new Service(body);
        while (body.isActive()) {
            service.blockingServeOldest("setFibN_1");
            service.blockingServeOldest("setFibN_2");
            cons1.setFibN(fibN_1.add(fibN_2));
        }
    }

    public static void main(String[] args) {
        try {
            Add add = (Add) PAActiveObject.newActive(Add.class.getName(), null);
            Cons1 cons1 = (Cons1) PAActiveObject.newActive(Cons1.class.getName(),
                    null);
            Cons2 cons2 = (Cons2) PAActiveObject.newActive(Cons2.class.getName(),
                    null);
            add.setCons1(cons1);
            cons1.setAdd(add);
            cons1.setCons2(cons2);
            cons2.setAdd(add);
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
    }
}
