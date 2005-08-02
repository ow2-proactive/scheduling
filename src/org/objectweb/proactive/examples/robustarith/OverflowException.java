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
public class OverflowException extends Exception {
    private String op;
    private BigInteger a;
    private BigInteger b;
    
    public OverflowException(String op, BigInteger a, BigInteger b) {
        this.op = op;
        this.a = a;
        this.b = b;
    }
    
    public String toString() {
        return "Overflow in " + a + " " + op + " " + b;
    }
}
