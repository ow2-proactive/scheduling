//@snippet-start primitive_master
package org.objectweb.proactive.examples.components.userguide.primitive;

import java.io.Serializable;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;


public class PrimitiveMaster implements Runnable, Serializable, BindingController {
    private static final String COMPUTER_CLIENT_ITF = "compute-itf";
    private ComputeItf computer;

    public PrimitiveMaster() {
    }

    public void run() {
        computer.doNothing();
        int result = computer.compute(5);
        System.out.println(" PrimitiveMaster-->run(): " + "Result of computation whith 5 is: " + result); //display 10
    }

    //BINDING CONTROLLER implementation
    public void bindFc(String myClientItf, Object serverItf) throws NoSuchInterfaceException,
            IllegalBindingException, IllegalLifeCycleException {
        if (myClientItf.equals(COMPUTER_CLIENT_ITF)) {
            computer = (ComputeItf) serverItf;
        }
    }

    public String[] listFc() {
        return new String[] { COMPUTER_CLIENT_ITF };
    }

    public Object lookupFc(String itf) throws NoSuchInterfaceException {
        if (itf.equals(COMPUTER_CLIENT_ITF)) {
            return computer;
        }
        return null;
    }

    public void unbindFc(String itf) throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        if (itf.equals(COMPUTER_CLIENT_ITF)) {
            computer = null;
        }
    }
}
//@snippet-end primitive_master
