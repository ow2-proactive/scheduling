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
package benchmark.functionscall.stringreturn;

import benchmark.functionscall.FunctionCall;

import org.objectweb.proactive.core.node.NodeImpl;

import java.net.InetAddress;


public class BenchFuncCall07 extends FunctionCall {
    public BenchFuncCall07() {
    }

    public BenchFuncCall07(NodeImpl node) {
        super(node, "Functions Call  --> String f(String, String, String)",
            "Mesure the time of a call Function who return String with 3 String arguments.");
    }

    public long action() throws Exception {
        long start;
        long end;
        BenchFuncCall07 activeObject = (BenchFuncCall07) getActiveObject();
        String s = "toto";
        String t = "tutu";
        String u = "titi";
        start = System.currentTimeMillis();
        activeObject.f(s, t, u);
        end = System.currentTimeMillis();
        return end - start;
    }

    public String f(String s, String t, String u) throws Exception {
        s.toString();
        t.toString();
        u.toString();
        if (logger.isDebugEnabled()) {
            logger.debug(InetAddress.getLocalHost().getHostName());
        }
        return "toto";
    }
}
