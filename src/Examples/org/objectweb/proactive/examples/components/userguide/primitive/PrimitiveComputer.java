//@snippet-start primitive_computer
package org.objectweb.proactive.examples.components.userguide.primitive;

import java.io.Serializable;


public class PrimitiveComputer implements ComputeItf, Serializable {
    public PrimitiveComputer() {
    }

    public int compute(int a) {
        int result = a * 2;
        System.err.println(" PrimitiveComputer-->compute(" + a + "): " + result);
        return result;
    }

    public void doNothing() {
        System.err.println(" PrimitiveComputer-->doNothing()");
    }
}
//@snippet-end primitive_computer