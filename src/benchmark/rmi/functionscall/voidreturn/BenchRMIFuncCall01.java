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
package benchmark.rmi.functionscall.voidreturn;

import benchmark.rmi.functionscall.RMIFunctionCall;

import org.objectweb.proactive.core.node.NodeImpl;

import java.net.InetAddress;


public class BenchRMIFuncCall01 extends RMIFunctionCall {
    public BenchRMIFuncCall01() {
    }

    public BenchRMIFuncCall01(NodeImpl node) {
        super(node, "RMI Functions Call --> void f()",
            "Mesure the time of a call Function who return void with no argument.");
    }

    public long action() throws Exception {
        long start;
        long end;
        BenchRMIFuncCall01 activeObject = (BenchRMIFuncCall01) getRmiObject();
        start = System.currentTimeMillis();
        activeObject.f();
        end = System.currentTimeMillis();
        return end - start;
    }

    public void f() throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug(InetAddress.getLocalHost().getHostName());
        }
    }
}
