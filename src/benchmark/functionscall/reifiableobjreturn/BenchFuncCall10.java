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
package benchmark.functionscall.reifiableobjreturn;

import java.net.InetAddress;

import org.objectweb.proactive.core.node.NodeImpl;

import benchmark.functionscall.FunctionCall;
import benchmark.util.ReifiableObject;


public class BenchFuncCall10 extends FunctionCall {
    public BenchFuncCall10() {
    }

    public BenchFuncCall10(NodeImpl node) {
        super(node,
            "Functions Call  --> ReifiableObject f(ReifiableObject, ReifiableObject, ReifiableObject)",
            "Mesure the time of a call Function who return ReifiableObject with 3 ReifiableObject arguments.");
    }

    public long action() throws Exception {
        long start;
        long end;
        BenchFuncCall10 activeObject = (BenchFuncCall10) getActiveObject();
        ReifiableObject o = new ReifiableObject();
        ReifiableObject p = new ReifiableObject();
        ReifiableObject q = new ReifiableObject();
        start = System.currentTimeMillis();
        activeObject.f(o, p, q);
        end = System.currentTimeMillis();
        return end - start;
    }

    public ReifiableObject f(ReifiableObject o, ReifiableObject p,
        ReifiableObject q) throws Exception {
        o.toString();
        p.toString();
        q.toString();
        if (logger.isDebugEnabled()) {
            logger.debug(InetAddress.getLocalHost().getHostName());
        }
        return new ReifiableObject();
    }
}
