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

import java.io.Serializable;
import java.math.BigInteger;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;


/**
 * @author gchazara
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Sum implements Serializable {
    private SubSum[] sums;

    public Sum() {
    }

    public Ratio eval(Formula formula, int begin, int end)
        throws OverflowException {
        Ratio[] ratios = new Ratio[sums.length];
        int d = (end - begin) / ratios.length;

        for (int i = 0; i < (ratios.length - 1); i++) {
            ratios[i] = sums[i].eval(formula, begin, (begin + d) - 1);
            begin += d;
        }

        ratios[ratios.length - 1] = sums[ratios.length - 1].eval(formula,
                begin, end);

        Ratio r = new Ratio(BigInteger.ZERO, BigInteger.ONE);
        for (int i = 0; i < ratios.length; i++) {
            r.add(ratios[i]);
        }

        return r;
    }

    public Sum(Node[] nodes)
        throws ActiveObjectCreationException, NodeException {
        sums = new SubSum[nodes.length];
        for (int i = 0; i < sums.length; i++) {
            sums[i] = (SubSum) ProActiveObject.newActive(SubSum.class.getName(),
                    new Object[] { "SubSum" + i }, nodes[i]);
        }
    }
}
