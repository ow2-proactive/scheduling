/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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
package org.objectweb.proactive.core.group.spmd;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.group.MethodCallControlForGroup;
import org.objectweb.proactive.core.mop.MethodCallExecutionFailedException;

import java.lang.reflect.InvocationTargetException;


/**
 * This class represents a call of strong synchronization between the member of a SPMD Group.
 * @author Laurent Baduel
 */
public class MethodCallBarrier extends MethodCallControlForGroup {

    /** The unique ID of the barrier */
    private String IDName;

    /** the SPMDGroupManager in wich the barrier call have to operate (on the calle side) */
    private ProActiveSPMDGroupManager spmdManager = null;

    /**
     * Constructor
     * @param idname - the id name of the barrier
     */
    public MethodCallBarrier(String idname) {
        this.IDName = idname;
    }

    /**
     * Returns the name of the call
     * @return the String "MethodCallBarrier"
     */
    public String getName() {
        return "MethodCallBarrier";
    }

    /**
     * Returns the ID name of the barrier
     * @return the ID name of the barrier
     */
    public String getIDName() {
        return this.IDName;
    }

    /**
     * Execution of a barrier call is to block the service of request if the method is sent by the object itself.
     * @param target this object is not used.
     * @return null
     */
    public Object execute(Object target)
        throws InvocationTargetException, MethodCallExecutionFailedException {
        this.spmdManager = ((AbstractBody) ProActive.getBodyOnThis()).getProActiveSPMDGroupManager();
        BarrierState bs = (BarrierState) this.spmdManager.getBarrierStateFor(this.getIDName());

        // bs == null  =>  state not found  =>  first barrier encountered for ID name
        if (bs == null) {
            // System.out.println("First barrier \"" + this.getIDName() + "\" encountered !");
            // build and add infos about new barrier
            bs = new BarrierState();
            this.spmdManager.addToCurrentBarriers(this.getIDName(), bs);
        }

        // if there is others waiting calls, decrement
        if ((bs.getAwaitedCalls() - (bs.getReceivedCalls() + 1)) != 0) {
            bs.incrementReceivedCalls();
        }
        // calls == 0  =>  this is the last awaited call to this barrier 
        else {
            this.spmdManager.remove(this.getIDName());
        }
        return null;
    }

    /**
     *  Set the SPMDGroupManager in wich the barrier call have to operate
     * @param spmdManager the ProActiveSPMDGroupManager in wich the barrier call have to operate
     */
    public void setSPMDManager(ProActiveSPMDGroupManager spmdManager) {
        this.spmdManager = spmdManager;
    }
}
