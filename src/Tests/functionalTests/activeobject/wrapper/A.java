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
package functionalTests.activeobject.wrapper;

import java.io.Serializable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.util.wrapper.BooleanMutableWrapper;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.DoubleMutableWrapper;
import org.objectweb.proactive.core.util.wrapper.DoubleWrapper;
import org.objectweb.proactive.core.util.wrapper.FloatMutableWrapper;
import org.objectweb.proactive.core.util.wrapper.FloatWrapper;
import org.objectweb.proactive.core.util.wrapper.IntMutableWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.core.util.wrapper.LongMutableWrapper;
import org.objectweb.proactive.core.util.wrapper.LongWrapper;
import org.objectweb.proactive.core.util.wrapper.StringMutableWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;


public class A implements RunActive, Serializable {

    /**
         *
         */
    private static final long serialVersionUID = -681586215164175864L;

    public A() {
    }

    public BooleanMutableWrapper testBooleanMutableWrapper() {
        return new BooleanMutableWrapper(false);
    }

    public DoubleMutableWrapper testDoubleMutableWrapper() {
        return new DoubleMutableWrapper(0);
    }

    public IntMutableWrapper testIntMutableWrapper() {
        return new IntMutableWrapper(0);
    }

    public LongMutableWrapper testLongMutableWrapper() {
        return new LongMutableWrapper(0);
    }

    public StringMutableWrapper testStringMutableWrapper() {
        return new StringMutableWrapper(
            "Alexandre dC is a famous coder <-- do you mean that ? really ?");
    }

    public FloatMutableWrapper testFloatMutableWrapper() {
        return new FloatMutableWrapper(0);
    }

    public BooleanWrapper testBooleanWrapper() {
        return new BooleanWrapper(false);
    }

    public DoubleWrapper testDoubleWrapper() {
        return new DoubleWrapper(0);
    }

    public IntWrapper testIntWrapper() {
        return new IntWrapper(0);
    }

    public LongWrapper testLongWrapper() {
        return new LongWrapper(0);
    }

    public StringWrapper testStringWrapper() {
        return new StringWrapper(
            "Alexandre dC is a famous coder <-- do you mean that ? really ?");
    }

    public FloatWrapper testFloatWrapper() {
        return new FloatWrapper(0);
    }

    public void terminate() {
        ProActiveObject.terminateActiveObject(true);
    }

    public void runActivity(Body body) {
        Service service = new Service(body);
        while (body.isActive()) {
            service.blockingServeOldest("terminate");
            return;
        }
    }
}
