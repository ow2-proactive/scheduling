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
package org.objectweb.proactive.examples.robustarith;

import java.io.File;
import java.math.BigInteger;
import java.util.Set;

import org.objectweb.proactive.api.PAException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extra.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplication;
import org.objectweb.proactive.extra.gcmdeployment.core.GCMVirtualNode;


/**
 * @author gchazara
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Main {
    public static void main(String[] args) {
        Formula f = new Formula() {
            private final Ratio ONE_64 = new Ratio(BigInteger.ONE, new BigInteger("64"));

            private BigInteger i2b(int i) {
                return new BigInteger("" + i);
            }

            private Ratio ratio(int x, int a, int b, int c) {
                int denum = (b * x) + c;
                return new Ratio(i2b(a), i2b(denum));
            }

            public Ratio eval(int x) throws OverflowException {
                boolean even = (x & 1) == 0;
                BigInteger firstNum = even ? BigInteger.ONE : Int.MINUS_ONE;
                Ratio first = new Ratio(firstNum, Int.pow2(10 * x));
                Ratio r = new Ratio(BigInteger.ZERO, BigInteger.ONE);
                r.add(ratio(x, -32, 4, 1));
                r.add(ratio(x, -1, 4, 3));
                r.add(ratio(x, 256, 10, 1));
                r.add(ratio(x, -64, 10, 3));
                r.add(ratio(x, -4, 10, 5));
                r.add(ratio(x, -4, 10, 7));
                r.add(ratio(x, 1, 10, 9));
                r.mul(first);
                r.mul(ONE_64);

                return r;
            }
        };

        //ProActive.tryWithCatch(java.io.IOException.class);
        PAException.tryWithCatch(Exception.class);
        try {
            String path = (args.length == 0) ? "descriptors/Matrix.xml" : args[0];
            GCMApplication pad = PAGCMDeployment.loadApplicationDescriptor(new File(path));
            GCMVirtualNode dispatcher = pad.getVirtualNode("matrixNode");
            dispatcher.waitReady();
            Set<Node> nodes = dispatcher.getCurrentNodes();
            Sum s = new Sum(nodes);
            Ratio r = s.eval(f, 0, 40);
            System.out.println(r);
            PAException.endTryWithCatch();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            PAException.removeTryWithCatch();
        }
        System.exit(0);
    }
}
