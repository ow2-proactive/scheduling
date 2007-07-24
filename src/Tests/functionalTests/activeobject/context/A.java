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
package functionalTests.activeobject.context;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.Context;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;


public class A implements java.io.Serializable {
    private UniqueID myID;

    public boolean init() {
        this.myID = ProActive.getBodyOnThis().getID();
        ProActive.setImmediateService("immediateService");
        return (this.myID != null);
    }

    public BooleanWrapper standardService(UniqueID caller) {
        Context current = ProActive.getContext();
        System.out.println("" + current);
        Request r = current.getCurrentRequest();
        return new BooleanWrapper((r != null) &&
            (current.getBody().getID().equals(myID)) &&
            (r.getSourceBodyID().equals(caller)) &&
            (r.getMethodName().equals("standardService")));
    }

    public BooleanWrapper immediateService(UniqueID caller) {
        Context current = ProActive.getContext();
        System.out.println("" + current);
        Request r = current.getCurrentRequest();
        return new BooleanWrapper((r != null) &&
            (current.getBody().getID().equals(myID)) &&
            (r.getSourceBodyID().equals(caller)) &&
            (r.getMethodName().equals("immediateService")));
    }

    public BooleanWrapper test(A a) {
        return new BooleanWrapper((a.standardService(this.myID).booleanValue()) &&
            (a.immediateService(this.myID)).booleanValue());
    }
}
