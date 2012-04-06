/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.tests.performance.jmeter;

import org.apache.jmeter.JMeter;
import org.apache.log.Logger;
import org.objectweb.proactive.core.runtime.RuntimeFactory;


/**
 * Test JavaSamplers work with ProActive which internally starts some non-daemon
 * threads and JMeter process can finish when test execution completes because
 * of this threads. This class is used as workaround for this ProActive issue:
 * special JavaSampler executed by the setUp ThreadGroup from its 'teardownTest'
 * method starts JVMKillerThread which after some delay calls System.exit (delay
 * is needed because JMeter has do some work after 'teardownTest' is called).
 * This hack with JVMKillerThread isn't reliable but I didn't find another
 * simple solution.
 * 
 * @author ProActive team
 * 
 */
public class JVMKillerThread extends Thread {

    private final long killDelay;

    private final Logger logger;

    public JVMKillerThread(long killDelay, Logger logger) {
        this.killDelay = killDelay;
        this.logger = logger;
    }

    public void run() {
        try {
            logger.info(String.format("JVMKillerThread: sleeping for %d millis", killDelay));
            Thread.sleep(killDelay);
            logger.info("JVMKillerThread: killing JVM");
            try {
                RuntimeFactory.getDefaultRuntime().killRT(false);
            } finally {
                System.exit(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void startKillerThreadIfNonGUIMode(long killDelay, Logger logger) {
        if (JMeter.isNonGUI()) {
            new JVMKillerThread(killDelay, logger).start();
        }
    }
}
