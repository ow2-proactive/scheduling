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
package org.objectweb.proactive.extensions.calcium.examples.blast;

import java.io.File;

import org.objectweb.proactive.extensions.calcium.Calcium;
import org.objectweb.proactive.extensions.calcium.Stream;
import org.objectweb.proactive.extensions.calcium.environment.EnvironmentFactory;
import org.objectweb.proactive.extensions.calcium.environment.multithreaded.MultiThreadedEnvironment;
import org.objectweb.proactive.extensions.calcium.examples.nqueens.NQueens;
import org.objectweb.proactive.extensions.calcium.exceptions.MuscleException;
import org.objectweb.proactive.extensions.calcium.exceptions.PanicException;
import org.objectweb.proactive.extensions.calcium.futures.Future;
import org.objectweb.proactive.extensions.calcium.skeletons.DaC;
import org.objectweb.proactive.extensions.calcium.skeletons.Pipe;
import org.objectweb.proactive.extensions.calcium.skeletons.Seq;
import org.objectweb.proactive.extensions.calcium.skeletons.Skeleton;
import org.objectweb.proactive.extensions.calcium.statistics.StatsGlobal;


public class Blast {
    Skeleton<BlastParams, File> root;

    public Blast() {
        /* Format the query and database files */
        Pipe<BlastParams, BlastParams> formatFork = new Pipe<BlastParams, BlastParams>(new ExecuteFormatDB(),
                new ExecuteFormatQuery());

        /* Blast a database
         * 2.1 Format the database
         * 2.2 Blast the database */
        Pipe<BlastParams, File> blastPipe = new Pipe<BlastParams, File>(formatFork,
                new Seq<BlastParams, File>(new ExecuteBlast()));

        /* 1 Divide the database
         * 2 Blast the database with the query
         * 3 Conquer the query results  */
        root = new DaC<BlastParams, File>(new DivideDB(),
                new DivideDBCondition(), blastPipe, new ConquerResults());
    }

    public static void main(String[] args) throws Exception {
        BlastParams param = new BlastParams(new File("/home/mleyton/query.nt"),
                new File("/home/mleyton/db.nt"), true, 100 * 1024);

        Blast blast = new Blast();
        blast.solve(param);
    }

    private void solve(BlastParams parameters)
        throws InterruptedException, PanicException {
        String descriptor = NQueens.class.getResource("LocalDescriptor.xml")
                                         .getPath();

        //descriptor="/home/mleyton/workspace/ProActive/descriptors/examples/SSH_SGE_Example.xml";
        //new ProActiveEnvironment(descriptor, "local");
        EnvironmentFactory envfactory = new MultiThreadedEnvironment(1);

        Calcium calcium = new Calcium(envfactory);
        Stream<BlastParams, File> stream = calcium.newStream(root);
        Future<File> future = stream.input(parameters);
        calcium.boot();

        try {
            File res = future.get();
            System.out.println("Result in:" + res + " " + res.length() +
                " [bytes]");
            System.out.println(future.getStats());
        } catch (MuscleException e) {
            e.printStackTrace();
        }

        StatsGlobal stats = calcium.getStatsGlobal();
        System.out.println(stats);
        calcium.shutdown();
    }
}
