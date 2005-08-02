/*
 * Created on Jun 6, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.objectweb.proactive.examples.robustarith;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;

import java.io.Serializable;

import java.math.BigInteger;


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
            sums[i] = (SubSum) ProActive.newActive(SubSum.class.getName(),
                    new Object[] { "SubSum" + i }, nodes[i]);
        }
    }
}
