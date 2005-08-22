package org.objectweb.proactive.examples.robustarith;

import java.io.Serializable;

import org.objectweb.proactive.ProActive;


class DangerousException extends Exception {
    DangerousException(String str) {
        super(str);
    }
}


public class ExceptionTest implements Serializable {

    /* Empty constructor for ProActive */
    public ExceptionTest() {
    }

    public ExceptionTest dangerousMethod() throws DangerousException {
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new DangerousException("Very dangerous");
    }

    public static void main(String[] args) {
        ExceptionTest test = null;
        try {
            test = (ExceptionTest) ProActive.newActive(ExceptionTest.class.getName(),
                    null);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        ProActive.tryWithCatch(DangerousException.class);
        try {
            System.out.println("Appel");
            ExceptionTest et = test.dangerousMethod();

            //            et.toString();
            System.out.println("Fin de l'appel");
            ProActive.endTryWithCatch();
        } catch (DangerousException de) {
            System.out.println("Backtrace de l'exception :");
            de.printStackTrace(System.out);
        } finally {
            ProActive.removeTryWithCatch();
        }
        System.out.println("fini");
    }
}
