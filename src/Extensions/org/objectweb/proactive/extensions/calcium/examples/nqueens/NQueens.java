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
package org.objectweb.proactive.extensions.calcium.examples.nqueens;

import java.io.Serializable;

import org.objectweb.proactive.extensions.calcium.Calcium;
import org.objectweb.proactive.extensions.calcium.Stream;
import org.objectweb.proactive.extensions.calcium.environment.EnvironmentFactory;
import org.objectweb.proactive.extensions.calcium.environment.multithreaded.MultiThreadedEnvironment;
import org.objectweb.proactive.extensions.calcium.examples.nqueens.bt1.DivideBT1;
import org.objectweb.proactive.extensions.calcium.examples.nqueens.bt1.SolveBT1;
import org.objectweb.proactive.extensions.calcium.examples.nqueens.bt2.DivideBT2;
import org.objectweb.proactive.extensions.calcium.examples.nqueens.bt2.SolveBT2;
import org.objectweb.proactive.extensions.calcium.exceptions.MuscleException;
import org.objectweb.proactive.extensions.calcium.futures.Future;
import org.objectweb.proactive.extensions.calcium.monitor.Monitor;
import org.objectweb.proactive.extensions.calcium.monitor.SimpleLogMonitor;
import org.objectweb.proactive.extensions.calcium.skeletons.DaC;
import org.objectweb.proactive.extensions.calcium.skeletons.Fork;
import org.objectweb.proactive.extensions.calcium.skeletons.Seq;
import org.objectweb.proactive.extensions.calcium.skeletons.Skeleton;


public class NQueens implements Serializable {
    public Skeleton<Board, Result> root;

    public static void main(String[] args) throws Exception {
        NQueens nq = new NQueens();

        if (args.length != 5) {
            nq.solve(16, 14, 10,
                NQueens.class.getResource("LocalDescriptor.xml").getPath(),
                "local");
        } else {
            nq.solve(Integer.parseInt(args[0]), Integer.parseInt(args[1]),
                Integer.parseInt(args[2]), args[3], args[4]);
        }
    }

    @SuppressWarnings("unchecked")
    public NQueens() {
        Skeleton<Board, Result> BT1 = new DaC<Board, Result>(new DivideBT1(),
                new DivideCondition(), new Seq<Board, Result>(new SolveBT1()),
                new ConquerBoard());

        Skeleton<Board, Result> BT2 = new DaC<Board, Result>(new DivideBT2(),
                new DivideCondition(), new Seq<Board, Result>(new SolveBT2()),
                new ConquerBoard());

        root = new Fork<Board, Result>(new ConquerBoard(), BT1, BT2);
    }

    public void solve(int boardSize, int solvableSize, int times,
        String descriptor, String virtualNode) throws Exception {
        EnvironmentFactory manager = new MultiThreadedEnvironment(2);

        //EnvironmentFactory manager = new ProActiveEnvironment(descriptor, virtualNode);
        Calcium calcium = new Calcium(manager);
        Monitor monitor = new SimpleLogMonitor(calcium, 1);
        monitor.start();
        calcium.boot();

        Stream<Board, Result> stream = calcium.newStream(root);

        for (int i = 0; i < times; i++) {
            stream.submit(new Board(boardSize, solvableSize));
        }

        try {
            for (; times > 0; times--) {
                Future<Result> future = stream.retrieve();
                Result res = future.get();
                System.out.println(res);
                System.out.println(future.getStats());
            }
        } catch (MuscleException e) {
            e.printStackTrace();
        }
        calcium.shutdown();
        monitor.stop();
    }
}
