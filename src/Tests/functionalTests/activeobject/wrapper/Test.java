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

import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProFuture;
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

import functionalTests.FunctionalTest;
import static junit.framework.Assert.assertTrue;

/**
 * Test if a futre is created for primitive wrappers
 * Test primitive wrapper for asynchronous call.
 * @author Alexandre di Costanzo
 *
 * Created on Jul 28, 2005
 */
public class Test extends FunctionalTest {
    private static final long serialVersionUID = -659037699635472597L;
    private A ao;
    private BooleanMutableWrapper boolMutable;
    private DoubleMutableWrapper dbleMutable;
    private IntMutableWrapper integerMutable;
    private LongMutableWrapper longNumberMutable;
    private StringMutableWrapper stringMutable;
    private FloatMutableWrapper fltMutable;
    private BooleanWrapper bool;
    private DoubleWrapper dble;
    private IntWrapper integer;
    private LongWrapper longNumber;
    private StringWrapper string;
    private FloatWrapper flt;

    @Before
    public void initTest() throws Exception {
        this.ao = (A) ProActiveObject.newActive(A.class.getName(), null);
    }

    @org.junit.Test
    public void action() throws Exception {
        assertTrue(ao != null);

        this.boolMutable = this.ao.testBooleanMutableWrapper();
        this.dbleMutable = this.ao.testDoubleMutableWrapper();
        this.integerMutable = this.ao.testIntMutableWrapper();
        this.longNumberMutable = this.ao.testLongMutableWrapper();
        this.stringMutable = this.ao.testStringMutableWrapper();
        this.fltMutable = this.ao.testFloatMutableWrapper();

        this.bool = this.ao.testBooleanWrapper();
        this.dble = this.ao.testDoubleWrapper();
        this.integer = this.ao.testIntWrapper();
        this.longNumber = this.ao.testLongWrapper();
        this.string = this.ao.testStringWrapper();
        this.flt = this.ao.testFloatWrapper();

        assertTrue(ProFuture.isAwaited(this.boolMutable));
        assertTrue(ProFuture.isAwaited(this.dbleMutable));
        assertTrue(ProFuture.isAwaited(this.integerMutable));
        assertTrue(ProFuture.isAwaited(this.longNumberMutable));
        assertTrue(ProFuture.isAwaited(this.stringMutable));
        assertTrue(ProFuture.isAwaited(this.fltMutable));
        assertTrue(ProFuture.isAwaited(this.bool));
        assertTrue(ProFuture.isAwaited(this.dble));
        assertTrue(ProFuture.isAwaited(this.integer));
        assertTrue(ProFuture.isAwaited(this.longNumber));
        assertTrue(ProFuture.isAwaited(this.string));
        assertTrue(ProFuture.isAwaited(this.flt));
    }

    @After
    public void endTest() throws Exception {
        this.ao.terminate();
        this.ao = null;
    }
}
