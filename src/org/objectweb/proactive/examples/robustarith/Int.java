/*
 * Created on Jun 6, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.objectweb.proactive.examples.robustarith;

import java.math.BigInteger;

/**
 * @author gchazara
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Int {
    public static final BigInteger MINUS_ONE = BigInteger.ONE.negate();
    private static final int MAGNITUDE = 6553600;
    private static final BigInteger MAX = BigInteger.ONE.shiftLeft(MAGNITUDE);
    private static final BigInteger MIN = MAX.negate();
    
    public static BigInteger pow2(int e) throws OverflowException {
        BigInteger val = BigInteger.ONE.shiftLeft(e);
        if (val.compareTo(MAX) > 0) {
            throw new OverflowException("pow", new BigInteger("2"), new BigInteger("" + e));
        }
        
        return val;
    }
    
    public static BigInteger add(BigInteger a, BigInteger b) throws OverflowException {
        if (a.signum() > 0 && b.signum() > 0) {
            if (MAX.subtract(a).compareTo(b) < 0) {
                throw new OverflowException("add", a, b);
            }
        } else if (a.signum() < 0 && b.signum() < 0) {
            if (a.subtract(MIN).compareTo(b.negate()) < 0) {
                throw new OverflowException("add", a, b);
            }
        }
        
        return a.add(b);
    }
    
    public static BigInteger sub(BigInteger a, BigInteger b) throws OverflowException {
        try {
            return add(a, b.negate());
        } catch (OverflowException oe) {
            throw new OverflowException("sub", a, b);
        }
    }
    
    public static BigInteger mul(BigInteger a, BigInteger b) throws OverflowException {
        BigInteger m = a.multiply(b);
        if (m.compareTo(MAX) > 0) {
            throw new OverflowException("mul", a, b);
        }
        
        return m;
    }
    
    public static BigInteger div(BigInteger a, BigInteger b) throws OverflowException {
        if (b.signum() == 0) {
            throw new OverflowException("div", a, b);
        }
        
        return a.divide(b);
    }
}
