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

/**
 * @author Alexandre di Costanzo
 *
 */
public class BenchFuncCall05 extends FunctionCall {
    public BenchFuncCall05() {
    }

    public BenchFuncCall05(NodeImpl node) {
        super(node, "Functions Call  --> String f(String)",
            "Mesure the time of a call Function who return String with 1 String argument.");
    }

    public long action() throws Exception {
        
        
        BenchFuncCall05 activeObject = (BenchFuncCall05) getActiveObject();
        String s = "toto";
        this.timer.start(); for(int i = 0 ; i < FunctionCall.MAX_CALL ; i++) {
        activeObject.f(s);
        } this.timer.stop();
        return this.timer.getCumulatedTime();
    }

    public String f(String s) throws Exception {
        s.toString();
        if (logger.isDebugEnabled()) {
            logger.debug(InetAddress.getLocalHost().getHostName());
        }
        return "toto";
    }
}
