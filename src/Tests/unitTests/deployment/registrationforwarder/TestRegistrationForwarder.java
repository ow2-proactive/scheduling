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
package unitTests.deployment.registrationforwarder;

import org.junit.Test;
import org.objectweb.proactive.core.jmx.notification.GCMRuntimeRegistrationNotificationData;
import org.objectweb.proactive.core.runtime.RegistrationForwarder;

import junit.framework.Assert;
import static unitTests.UnitTests.logger;
public class TestRegistrationForwarder {
    final static long timeout = 500;

    @Test
    public void test() throws InterruptedException {
        // Do nothing test
        RegistrationForwarderExt to = new RegistrationForwarderExt(3, timeout);
        Assert.assertEquals(0, to.flushed);
        Thread.sleep(timeout * 2);
        Assert.assertEquals(0, to.flushed);
    }

    @Test
    public void test2() throws InterruptedException {
        // Check the message is flushed only after the timeout
        RegistrationForwarderExt to = new RegistrationForwarderExt(3, timeout);
        GCMRuntimeRegistrationNotificationData m = new GCMRuntimeRegistrationNotificationData("1",
                1, 1, null);
        to.addMessage(m);
        Assert.assertEquals(0, to.flushed);
        Thread.sleep(timeout * 2);
        Assert.assertEquals(1, to.flushed);
    }

    @Test
    public void test3() throws InterruptedException {
        // Check the message is flushed only after the timeout
        RegistrationForwarderExt to = new RegistrationForwarderExt(3, timeout);
        GCMRuntimeRegistrationNotificationData m;
        m = new GCMRuntimeRegistrationNotificationData("1", 1, 1, null);
        to.addMessage(m);
        Assert.assertEquals(0, to.flushed);
        m = new GCMRuntimeRegistrationNotificationData("2", 2, 2, null);
        to.addMessage(m);
        Assert.assertEquals(0, to.flushed);
        Thread.sleep(timeout * 2);
        Assert.assertEquals(1, to.flushed);
    }

    @Test
    public void test4() throws InterruptedException {
        RegistrationForwarderExt to = new RegistrationForwarderExt(3, 500);
        GCMRuntimeRegistrationNotificationData m;
        m = new GCMRuntimeRegistrationNotificationData("1", 1, 1, null);
        to.addMessage(m);
        Assert.assertEquals(0, to.flushed);
        m = new GCMRuntimeRegistrationNotificationData("2", 2, 2, null);
        to.addMessage(m);
        Assert.assertEquals(0, to.flushed);
        m = new GCMRuntimeRegistrationNotificationData("3", 3, 3, null);
        to.addMessage(m);
        Assert.assertEquals(1, to.flushed);
        m = new GCMRuntimeRegistrationNotificationData("4", 4, 4, null);
        to.addMessage(m);
        Assert.assertEquals(1, to.flushed);
        Thread.sleep(timeout * 2);
        Assert.assertEquals(2, to.flushed);
    }

    @Test
    public void test5() throws InterruptedException {
        logger.debug("test5");
        RegistrationForwarderExt to = new RegistrationForwarderExt(3, 500);
        GCMRuntimeRegistrationNotificationData m;
        for (int i = 1; i < 101; i++) {
            m = new GCMRuntimeRegistrationNotificationData(Integer.toString(i),
                    i, i, null);
            to.addMessage(m);
            Assert.assertEquals(i / 3, to.flushed);
        }
        Assert.assertEquals(101 / 3, to.flushed);
        Thread.sleep(timeout * 2);
        Assert.assertEquals((101 / 3) + 1, to.flushed);
    }

    private class RegistrationForwarderExt extends RegistrationForwarder {
        int flushed;

        public RegistrationForwarderExt(int threshlod, long maxWait) {
            super(threshlod, maxWait);
            flushed = 0;
        }

        @Override
        public void flush() {
            logger.debug("Flushing");
            flushed++;
        }
    }
}
