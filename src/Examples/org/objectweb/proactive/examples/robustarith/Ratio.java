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

    private static BigInteger lcm(BigInteger u, BigInteger v) throws OverflowException {
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

    @Override
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
