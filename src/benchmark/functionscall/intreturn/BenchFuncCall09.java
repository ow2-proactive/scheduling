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
package benchmark.functionscall.intreturn;

import java.net.InetAddress;

import org.objectweb.proactive.core.node.NodeImpl;

import benchmark.functionscall.FunctionCall;
import benchmark.util.ReifiableObject;

/**
 * @author Alexandre di Costanzo
 *
 */
public class BenchFuncCall09 extends FunctionCall {
    public BenchFuncCall09() {
    }

    public BenchFuncCall09(NodeImpl node) {
        super(node,
            "Functions Call  --> int f(ReifiableObject, ReifiableObject)",
            "Mesure the time of a call Function who return int with 2 ReifiableObject arguments.");
    }

    public long action() throws Exception {
        
        
        BenchFuncCall09 activeObject = (BenchFuncCall09) getActiveObject();
        ReifiableObject o = new ReifiableObject();
        ReifiableObject p = new ReifiableObject();
        this.timer.start(); for(int i = 0 ; i < FunctionCall.MAX_CALL ; i++) {
        activeObject.f(o, p);
        } this.timer.stop();
        return this.timer.getCumulatedTime();
    }

    public int f(ReifiableObject o, ReifiableObject p)
        throws Exception {
        o.toString();
        p.toString();
        if (logger.isDebugEnabled()) {
            logger.debug(InetAddress.getLocalHost().getHostName());
        }
        return 1;
    }
}
