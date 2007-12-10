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

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAException;


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
            test = (ExceptionTest) PAActiveObject.newActive(ExceptionTest.class.getName(),
                    null);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        PAException.tryWithCatch(DangerousException.class);
        try {
            System.out.println("Appel");
            ExceptionTest et = test.dangerousMethod();

            //            et.toString();
            System.out.println("Fin de l'appel");
            PAException.endTryWithCatch();
        } catch (DangerousException de) {
            System.out.println("Backtrace de l'exception :");
            de.printStackTrace(System.out);
        } finally {
            PAException.removeTryWithCatch();
        }
        System.out.println("fini");
    }
}
