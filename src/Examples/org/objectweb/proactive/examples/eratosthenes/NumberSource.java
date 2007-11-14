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
package org.objectweb.proactive.examples.eratosthenes;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/** This class sends increasing numbers to the first PrimeNumber for testing.
 * NumberSource is migratable. pause() serves to pause sending of numbers
 * but allows the containers to finish their waiting requests.
 * slowDown() makes the source sleep a little.
 * @author Jonathan Streit
 */
public class NumberSource implements java.io.Serializable, RunActive, Slowable {
    static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);
    private ActivePrimeContainer first;
    private boolean pause;
    private boolean sleep;
    private long currentNumber;

    /**
     * Constructor for NumberSource.
     */
    public NumberSource() {
        pause = true;
        currentNumber = 5;
    }

    public void setFirst(ActivePrimeContainer first) {
        this.first = first;
    }

    /** makes the source sleep a little to reduce charge on the first container. */
    public void sleep(boolean sleep) {
        this.sleep = sleep;
    }

    /** Sends increasing numbers to the first PrimeNumber for testing. */
    public void runActivity(Body b) {
        Service service = new Service(b);
        while (b.isActive()) {
            while (service.hasRequestToServe())
                service.serveOldest(); // serve requests (sleep)
            if (pause || sleep) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
            } else if (first != null) {
                logger.info("        Testing numbers from " + currentNumber +
                    " to " + (currentNumber + 98));
                for (int i = 0; i < 100; i++) { // start several requests at a time in order to increase speed
                    first.tryModulo(currentNumber);
                    currentNumber += 2;
                }
            }
        }
    }

    /** Pause temporarily */
    public void pause(boolean p) {
        pause = p;
    }
}
