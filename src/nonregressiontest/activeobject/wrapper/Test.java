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

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.DoubleWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.core.util.wrapper.LongWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;

import testsuite.test.FunctionalTest;
import org.objectweb.proactive.core.util.wrapper.FloatWrapper;


/**
 * Test primitive wrapper for asynchronous call.
 * @author Alexandre di Costanzo
 *
 * Created on Jul 28, 2005
 */
public class Test extends FunctionalTest {
    private A ao;
    private BooleanWrapper bool;
    private DoubleWrapper dble;
    private IntWrapper integer;
    private LongWrapper longNumber;
    private StringWrapper string;
    private FloatWrapper flt;

    public Test() {
        super("Non Reifiable Object Wrappers",
            "Test if a futre is created for primitive wrappers");
    }

    public void initTest() throws Exception {
        this.ao = (A) ProActive.newActive(A.class.getName(), null);
    }

    /**
     * @see testsuite.test.FunctionalTest#postConditions()
     */
    public boolean preConditions() throws Exception {
        return this.ao != null;
    }

    public void action() throws Exception {
        this.bool = this.ao.testBooleanWrapper();
        this.dble = this.ao.testDoubleWrapper();
        this.integer = this.ao.testIntWrapper();
        this.longNumber = this.ao.testLongWrapper();
        this.string = this.ao.testStringWrapper();
        this.flt = this.ao.testFloatWrapper();
    }

    /**
     * @see testsuite.test.FunctionalTest#preConditions()
     */
    public boolean postConditions() throws Exception {
        return ProActive.isAwaited(this.bool) &&
        ProActive.isAwaited(this.dble) && ProActive.isAwaited(this.integer) &&
        ProActive.isAwaited(this.longNumber) &&
        ProActive.isAwaited(this.string) &&
        ProActive.isAwaited(this.flt);
    }

    public void endTest() throws Exception {
        this.ao = null;
    }
}
