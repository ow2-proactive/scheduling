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
package org.objectweb.proactive.extra.gcmdeployment.tests.functional.capacity;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.extra.gcmdeployment.core.StartRuntime;

import functionalTests.FunctionalTest;


public class TestDefaultCapacity extends FunctionalTest {
    @Test
    public void testCapacityAutoDetection() {
        new Thread() {
            @Override
            public void run() {
                StartRuntime.main(new String[] {});
            }
        }.start();

        /* Be sure that the StartRuntime thread has been scheduled
         * Otherwise getCapacity will return -1 due to a race condition
         */
        Thread.yield();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ProActiveRuntimeImpl part = ProActiveRuntimeImpl.getProActiveRuntime();

        long cap = part.getVMInformation().getCapacity();
        long nproc = Runtime.getRuntime().availableProcessors();
        Assert.assertEquals(nproc, cap);
        Assert.assertEquals(nproc, part.getLocalNodes().size());
    }
}
