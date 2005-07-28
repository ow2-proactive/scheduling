/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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
package nonregressiontest.activeobject.wrapper;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.DoubleWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.core.util.wrapper.LongWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;

import java.io.Serializable;


public class A implements Serializable {
    public A() {
    }

    public BooleanWrapper testBooleanWrapper() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        return new BooleanWrapper(false);
    }

    public DoubleWrapper testDoubleWrapper() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        return new DoubleWrapper(0);
    }

    public IntWrapper testIntWrapper() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        return new IntWrapper(0);
    }

    public LongWrapper testLongWrapper() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        return new LongWrapper(0);
    }

    public StringWrapper testStringWrapper() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        return new StringWrapper("Alexandre dC is a famous coder :)");
    }
}
