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
package org.objectweb.proactive.loadbalancing;

import java.util.Random;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.loadbalancing.LoadBalancingConstants;


/**
 * This is the main class for Load Monitors, particular implementations
 * of monitors should inherite from it.  Like all monitors, it will create a thread which
 * calculates the load, sending it to the load balancer, and sleeping for a a while.
 *
 * To minimize reaction time against overloading, this implementation will reduce the
 * sleeping time while the load is increasing, supposing load index between 0 and 1.
 *
 * This class provides the  monitor behaviour, the load calculus has to be provided.
 *
 * @author Javier.Bustos@sophia.inria.fr
 *
 */
public class LoadMonitor implements Runnable {
    static Logger logger = ProActiveLogger.getLogger(Loggers.LOAD_BALANCING);
    protected double load = 0;
    protected LoadBalancer lb;

    protected synchronized void calculateLoad() {
    }
    ;
    public void run() {
        Random r = new Random();
        int i = 0;
        do {
            calculateLoad();
            lb.register(load);
            try {
                double sl;
                sl = LoadBalancingConstants.UPDATE_TIME * r.nextDouble();
                if (load > 0) {
                    sl = sl * (1 - load);
                    sl = sl + LoadBalancingConstants.MIGRATION_TIME;
                }
                Thread.sleep(Math.round(sl));
            } catch (InterruptedException interruptedexception) {
            }
        } while (true);
    }
}
