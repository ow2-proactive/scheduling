/*
 * Created on Jun 6, 2005
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
public class Ratio implements Serializable {
    private BigInteger num;
    private BigInteger denum;

    public Ratio() {
    }

    private static BigInteger lcm(BigInteger u, BigInteger v)
        throws OverflowException {
        return Int.mul(u, v).divide(u.gcd(v));
    }

    public void add(Ratio r) throws OverflowException {
        BigInteger l = lcm(denum, r.getDenum());

        BigInteger f = Int.div(l, denum);
        num = Int.mul(num, f);
        denum = l;

        f = Int.div(l, r.getDenum());
        num = Int.add(num, Int.mul(r.getNum(), f));
    }

    public void mul(Ratio r) throws OverflowException {
        num = Int.mul(num, r.getNum());
        denum = Int.mul(denum, r.getDenum());
    }

    public Ratio(BigInteger num, BigInteger denum) {
        this.num = num;
        this.denum = denum;
    }

    public String toString() {
        return num + " / " + denum;
    }

    public BigInteger getNum() {
        return num;
    }

    public BigInteger getDenum() {
        return denum;
    }
}
