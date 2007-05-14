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
package functionalTests.component.conform;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import functionalTests.ComponentTest;


/**
 * @author cdalmass
 *
 */
public class TestConformWrapper extends ComponentTest {

    /**
         *
         */
    private static final long serialVersionUID = 7978027750915442971L;
    static Result r;
    private boolean success = false;

    public TestConformWrapper() {
        super("Fractal conform tests", "Fractal conform tests");
    }

    /* (non-Javadoc)
     * @see testsuite.test.FunctionalTest#action()
     */
    @Test
    public void action() throws Exception {
        JUnitCore ju = new org.junit.runner.JUnitCore();
        Class[] testsClass = new Class[] { TestTypeFactory.class //, 
                                                                 //TestContentController.class
             };

        for (Class currentTestClass : testsClass) {
            r = ju.run(currentTestClass);
            if (r.wasSuccessful()) {
                success = true;
            } else {
                System.out.println("Run : " + currentTestClass.getSimpleName());
                System.out.println("There are " + r.getFailureCount() +
                    " failure(s) :");
                List<Failure> failures = r.getFailures();
                for (Failure failure : failures) {
                    System.out.println("Test " + failure.getTestHeader() +
                        " failed because of " + failure.getMessage());
                    //System.err.println("Description : " + failure.getDescription());
                    System.out.println("Trace : " + failure.getTrace());
                }
                System.out.println(r.getRunCount() + " test run in " +
                    r.getRunTime() + "ms; with " + r.getFailureCount() +
                    " failure(s).");
            }
        }

        Assert.assertTrue(success);
    }
}
