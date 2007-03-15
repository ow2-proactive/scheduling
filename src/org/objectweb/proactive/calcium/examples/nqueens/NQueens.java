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
package org.objectweb.proactive.calcium.examples.nqueens;

import java.io.Serializable;
import java.util.Vector;

import org.objectweb.proactive.calcium.Calcium;
import org.objectweb.proactive.calcium.MultiThreadedManager;
import org.objectweb.proactive.calcium.ResourceManager;
import org.objectweb.proactive.calcium.Stream;
import org.objectweb.proactive.calcium.examples.nqueens.bt1.DivideBT1;
import org.objectweb.proactive.calcium.examples.nqueens.bt1.SolveBT1;
import org.objectweb.proactive.calcium.examples.nqueens.bt2.DivideBT2;
import org.objectweb.proactive.calcium.examples.nqueens.bt2.SolveBT2;
import org.objectweb.proactive.calcium.exceptions.MuscleException;
import org.objectweb.proactive.calcium.exceptions.PanicException;
import org.objectweb.proactive.calcium.futures.Future;
import org.objectweb.proactive.calcium.monitor.Monitor;
import org.objectweb.proactive.calcium.monitor.SimpleLogMonitor;
import org.objectweb.proactive.calcium.skeletons.DaC;
import org.objectweb.proactive.calcium.skeletons.Fork;
import org.objectweb.proactive.calcium.skeletons.Seq;
import org.objectweb.proactive.calcium.skeletons.Skeleton;


public class NQueens implements Serializable {
    public Skeleton<Board, Result> root;

    public static void main(String[] args)
        throws InterruptedException, PanicException {
        NQueens nq = new NQueens();
        if (args.length != 5) {
            nq.start(16, 15, 10,
                NQueens.class.getResource("LocalDescriptor.xml").getPath(),
                "local");
        } else {
            nq.start(Integer.parseInt(args[0]), Integer.parseInt(args[1]),
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

    public void start(int boardSize, int solvableSize, int times,
        String descriptor, String virtualNode)
        throws InterruptedException, PanicException {
        ResourceManager manager = new MultiThreadedManager(10);

        //new MonoThreadedManager();
        //new ProActiveThreadedManager(descriptor, virtualNode);
        //new ProActiveManager(descriptor, virtualNode);
        Calcium calcium = new Calcium(manager);
        Monitor monitor = new SimpleLogMonitor(calcium, 5);
        monitor.start();
        calcium.boot();

        Stream<Board, Result> stream = calcium.newStream(root);
        Vector<Future<Result>> futures = new Vector<Future<Result>>(times);

        for (int i = 0; i < times; i++) {
            Future<Result> f = stream.input(new Board(boardSize, solvableSize));
            futures.add(f);
        }

        try {
            for (Future<Result> future : futures) {
                Result res = future.get();
                long total = 0;
                for (int i = 0; i < res.solutions.length; i++) {
                    System.out.print(res.solutions[i] + "|");
                    total += res.solutions[i];
                }
                System.out.println();
                System.out.println("Total=" + total);
                System.out.println(future.getStats());
            }
        } catch (MuscleException e) {
            e.printStackTrace();
        }
        calcium.shutdown();
        monitor.stop();
    }
}
