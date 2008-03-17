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
package functionalTests.hpc.collectiveoperation;

import static junit.framework.Assert.assertTrue;

import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.api.PASPMD;
import org.objectweb.proactive.core.node.Node;

import functionalTests.FunctionalTestDefaultNodes;
import functionalTests.GCMDeploymentReady;


@GCMDeploymentReady
public class TestPow2 extends FunctionalTestDefaultNodes {
    private A spmdgroup;
    private int groupSize;

    public TestPow2() {
        super(DeploymentType._2x2);
    }

    @org.junit.Before
    public void action() throws Exception {
        int nbNodes = 4;
        Object[][] params = new Object[nbNodes][];
        Node[] nodes = new Node[nbNodes];
        for (int i = 0; i < nbNodes; i++) {
            params[i] = new Object[] {};
            nodes[i] = super.getANode();
        }

        // Let's create a four nodes SPMD group
        spmdgroup = (A) PASPMD.newSPMDGroup(A.class.getName(), params, nodes);
        groupSize = PAGroup.getGroup(spmdgroup).size();

        spmdgroup.init();

        spmdgroup.max();
        spmdgroup.min();
        spmdgroup.sum();
        spmdgroup.maxArray();
        spmdgroup.minArray();
        spmdgroup.sumArray();
    }

    @org.junit.Test
    public void testMax() {
        double goodValue = groupSize; // Each worker has rank+1 => max = maxRank + 1 = groupSize
        for (int i = 0; i < groupSize; i++) {
            double a = ((A) PAGroup.get(spmdgroup, i)).getMax();
            assertTrue(a == goodValue);
        }
    }

    @org.junit.Test
    public void testMin() {
        double goodValue = 1; // Each worker has rank+1 => min = minRank + 1 = 1
        for (int i = 0; i < groupSize; i++) {
            double a = ((A) PAGroup.get(spmdgroup, i)).getMin();
            assertTrue(a == goodValue);
        }
    }

    @org.junit.Test
    public void testSum() {
        double goodValue = 0;
        for (int i = 1; i <= groupSize; i++) {
            goodValue += i; // Each worker sum up rank + 1
        }
        for (int i = 0; i < groupSize; i++) {
            double a = ((A) PAGroup.get(spmdgroup, i)).getSum();
            assertTrue(a == goodValue);
        }
    }

    @org.junit.Test
    public void testMaxArray() {
        // Each worker has { rank , 1/(rank+0.1) , rank*10 }
        double[] goodArray = new double[] { groupSize - 1, 10.0 /* 1/0.1=10 */, 10 * (groupSize - 1) };
        for (int i = 1; i < groupSize; i++) {
            double[] a = ((A) PAGroup.get(spmdgroup, i)).getMaxArray();
            assertTrue(java.util.Arrays.equals(a, goodArray));
        }
    }

    @org.junit.Test
    public void testMinArray() {
        // Each worker has { rank , 1/(rank+0.1) , rank*10 }
        double[] goodArray = new double[] { 0, 1.0 / (groupSize - 0.9), 0 };
        for (int i = 1; i < groupSize; i++) {
            double[] a = ((A) PAGroup.get(spmdgroup, i)).getMinArray();
            assertTrue(java.util.Arrays.equals(a, goodArray));
        }
    }

    @org.junit.Test
    public void testSumArray() {
        // Each worker has { rank , 1/(rank+0.1) , rank*10 }
        double v1 = 0.0;
        double v2 = 0.0;
        double v3 = 0.0;
        for (int i = 0; i < groupSize; i++) {
            v1 += i;
            v2 += (1.0 / (i + 0.1));
            v3 += 10.0 * i;
        }
        double[] goodArray = new double[] { v1, v2, v3 };
        for (int i = 1; i < groupSize; i++) {
            double[] a = ((A) PAGroup.get(spmdgroup, i)).getSumArray();
            assertTrue(java.util.Arrays.equals(a, goodArray));
        }
    }
}
