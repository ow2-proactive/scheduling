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
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.examples.robustarith;

import java.io.Serializable;

import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProException;


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
            test = (ExceptionTest) ProActiveObject.newActive(ExceptionTest.class.getName(),
                    null);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        ProException.tryWithCatch(DangerousException.class);
        try {
            System.out.println("Appel");
            ExceptionTest et = test.dangerousMethod();

            //            et.toString();
            System.out.println("Fin de l'appel");
            ProException.endTryWithCatch();
        } catch (DangerousException de) {
            System.out.println("Backtrace de l'exception :");
            de.printStackTrace(System.out);
        } finally {
            ProException.removeTryWithCatch();
        }
        System.out.println("fini");
    }
}
