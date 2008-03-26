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
package org.objectweb.proactive.loadbalancing;

import java.util.Random;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActiveInternalObject;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.loadbalancing.metrics.Metric;


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
 * @author The ProActive Team
 *
 */
public class LoadMonitor implements Runnable, ProActiveInternalObject {
    static Logger logger = ProActiveLogger.getLogger(Loggers.LOAD_BALANCING);

    //protected double load = 0;
    protected LoadBalancer lb;
    protected Metric metric;

    public LoadMonitor(LoadBalancer lb, Metric metric) {
        this.lb = lb;
        this.metric = metric;
    }

    public synchronized void killMePlease() {
        Thread.currentThread().interrupt();
    }

    public void run() {
        Random r = new Random();
        do {
            this.metric.takeDecision(lb);
            try {
                double sl;
                sl = LoadBalancingConstants.UPDATE_TIME * r.nextDouble();
                /*
                if (load > 0) {
                    if (load < 1) sl = sl * (1 - load);
                    sl = sl + LoadBalancingConstants.MIGRATION_TIME;
                }
                 */
                Thread.sleep(Math.round(sl));
            } catch (InterruptedException interruptedexception) {
            }
        } while (true);
    }
}
