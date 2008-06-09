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
package org.objectweb.proactive.extensions.calcium.examples.findprimes;

import java.io.Serializable;
import java.util.Vector;

import org.objectweb.proactive.extensions.calcium.Calcium;
import org.objectweb.proactive.extensions.calcium.Stream;
import org.objectweb.proactive.extensions.calcium.environment.EnvironmentFactory;
import org.objectweb.proactive.extensions.calcium.environment.multithreaded.MultiThreadedEnvironment;
import org.objectweb.proactive.extensions.calcium.exceptions.MuscleException;
import org.objectweb.proactive.extensions.calcium.exceptions.PanicException;
import org.objectweb.proactive.extensions.calcium.futures.CalFuture;
import org.objectweb.proactive.extensions.calcium.skeletons.DaC;
import org.objectweb.proactive.extensions.calcium.skeletons.Skeleton;
import org.objectweb.proactive.extensions.calcium.statistics.StatsGlobal;


public class FindPrimes implements Serializable {
    public Skeleton<Interval, Primes> root;

    public static void main(String[] args) throws InterruptedException, PanicException {
        FindPrimes st = new FindPrimes();
        st.solve();
    }

    public FindPrimes() {
        root = new DaC<Interval, Primes>(new IntervalDivide(), new IntervalDivideCondition(),
            new SearchInterval(), new JoinPrimes());
    }

    public void solve() throws InterruptedException, PanicException {
        String descriptor = FindPrimes.class.getResource("LocalDescriptor.xml").getPath();

        EnvironmentFactory environment = new MultiThreadedEnvironment(1);

        //new MonoThreadedManager();
        //new MultiThreadedManager(5);
        //new ProActiveManager(descriptor, "local");
        Calcium calcium = new Calcium(environment);

        Stream<Interval, Primes> stream = calcium.newStream(root);

        Vector<CalFuture<Primes>> futures = new Vector<CalFuture<Primes>>(3);
        futures.add(stream.input(new Interval(1, 6400, 300)));
        futures.add(stream.input(new Interval(1, 100, 20)));
        futures.add(stream.input(new Interval(1, 640, 64)));

        calcium.boot();

        try {
            for (CalFuture<Primes> future : futures) {
                Primes res = future.get();
                for (Integer i : res.primes) {
                    System.out.print(i + " ");
                }
                System.out.println();
                System.out.println(future.getStats());
            }
        } catch (MuscleException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        StatsGlobal stats = calcium.getStatsGlobal();
        System.out.println(stats);
        calcium.shutdown();
    }
}
