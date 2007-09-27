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
package performanceTests.simple;

import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;


/**
 *
 * @author vbodnart
 */
public class Callee implements java.io.Serializable {
    public static final int WAIT_TIME = 10;
    public static final boolean WAIT_ENABLED = false;

    public Callee() {
    }

    public void voidAsyncCall(int[] array) {
        if (WAIT_ENABLED) {
            Callee.waitSomeTime();
        }
    }

    public boolean syncCall() {
        if (WAIT_ENABLED) {
            Callee.waitSomeTime();
        }
        return true;
    }

    public IntWrapper futureAsyncCall(int[] array) {
        if (WAIT_ENABLED) {
            Callee.waitSomeTime();
        }
        return new IntWrapper(0);
    }

    public int[] futureAsyncCall2(int[] array) {
        if (WAIT_ENABLED) {
            Callee.waitSomeTime();
        }
        return array;
    }

    private static final void waitSomeTime() {
        try {
            Thread.sleep(WAIT_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Since an instance of this class could be active this method should be
     * called to terminate its activity.
     */
    public boolean kill() {
        ProActiveObject.terminateActiveObject(true);
        return true;
    }
}
