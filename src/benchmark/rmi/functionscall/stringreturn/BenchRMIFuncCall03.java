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
package benchmark.rmi.functionscall.stringreturn;

import benchmark.rmi.functionscall.RMIFunctionCall;

import org.objectweb.proactive.core.node.NodeImpl;

import java.net.InetAddress;


public class BenchRMIFuncCall03 extends RMIFunctionCall {
    public BenchRMIFuncCall03() {
    }

    public BenchRMIFuncCall03(NodeImpl node) {
        super(node, "RMI Functions Call  --> String f(int, int)",
            "Mesure the time of a call Function who return String with 2 int arguments.");
    }

    public long action() throws Exception {
        long start;
        long end;
        BenchRMIFuncCall03 activeObject = (BenchRMIFuncCall03) getRmiObject();
        start = System.currentTimeMillis();
        activeObject.f(1, 2);
        end = System.currentTimeMillis();
        return end - start;
    }

    public String f(int i, int j) throws Exception {
        i++;
        j++;
        if (logger.isDebugEnabled()) {
            logger.debug(InetAddress.getLocalHost().getHostName());
        }
        return "toto";
    }
}
