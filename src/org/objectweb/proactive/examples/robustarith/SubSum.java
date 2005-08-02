/*
 * Created on Jun 7, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.objectweb.proactive.examples.robustarith;

import java.io.Serializable;

import java.math.BigInteger;


/**
 * @author gchazara
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SubSum implements Serializable {
    private String name;

    public SubSum() {
    }

    public SubSum(String name) {
        this.name = name;
    }

    public Ratio eval(Formula formula, int begin, int end)
        throws OverflowException {
        Ratio r = new Ratio(BigInteger.ZERO, BigInteger.ONE);

        while (begin <= end) {
            Ratio term = formula.eval(begin);
            r.add(term);
            System.out.println(name + ": (" + begin + ")");
            begin++;
        }

        return r;
    }
}
