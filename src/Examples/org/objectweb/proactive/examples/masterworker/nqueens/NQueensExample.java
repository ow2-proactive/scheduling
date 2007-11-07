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
package org.objectweb.proactive.examples.masterworker.nqueens;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.Vector;

import org.apache.commons.cli.HelpFormatter;
import org.objectweb.proactive.examples.masterworker.AbstractExample;
import org.objectweb.proactive.examples.masterworker.nqueens.query.Query;
import org.objectweb.proactive.examples.masterworker.nqueens.query.QueryExtern;
import org.objectweb.proactive.examples.masterworker.nqueens.query.QueryGenerator;
import org.objectweb.proactive.examples.masterworker.util.Pair;
import org.objectweb.proactive.extra.masterworker.ProActiveMaster;
import org.objectweb.proactive.extra.masterworker.TaskAlreadySubmittedException;
import org.objectweb.proactive.extra.masterworker.TaskException;
import org.objectweb.proactive.extra.masterworker.interfaces.Task;


/**
 * This examples calculates the Nqueen
 * @author fviale
 *
 */
public class NQueensExample extends AbstractExample {
    private static final int DEFAULT_BOARD_SIZE = 18;
    private static final int DEFAULT_ALGORITHM_DEPTH = 1;
    public static int nqueen_board_size;
    public static int nqueen_algorithm_depth;
    private ProActiveMaster<QueryExtern, Pair<Long, Long>> master;

    @SuppressWarnings("unchecked")
    public static void main(String[] args)
        throws MalformedURLException, TaskAlreadySubmittedException {
        NQueensExample instance = new NQueensExample();

        //   Getting command line parameters and creating the master (see AbstractExample)
        instance.init(args);

        instance.master.addResources(instance.descriptor_url, instance.vn_name);

        System.out.println("Launching NQUEENS solutions finder for n = " +
            nqueen_board_size + " with a depth of " + nqueen_algorithm_depth);

        long sumResults = 0;
        long sumTime = 0;
        long begin = System.currentTimeMillis();

        // Generating the queries for the NQueens
        Vector<Query> unresolvedqueries = QueryGenerator.generateQueries(nqueen_board_size,
                nqueen_algorithm_depth);

        // Splitting Queries
        Vector<QueryExtern> toSolve = new Vector<QueryExtern>();
        while (!unresolvedqueries.isEmpty()) {
            Query query = unresolvedqueries.remove(0);
            Vector<Query> splitted = QueryGenerator.splitAQuery(query);
            if (!splitted.isEmpty()) {
                for (Query splitquery : splitted) {
                    toSolve.add(new QueryExtern(splitquery));
                }
            } else {
                toSolve.add(new QueryExtern(query));
            }
        }
        instance.master.solve(toSolve);

        // Print results on the fly
        while (!instance.master.isEmpty()) {
            try {
                Pair<Long, Long> res = instance.master.waitOneResult();
                sumResults += res.getFirst();
                sumTime += res.getSecond();
                System.out.println("Current nb of results : " + sumResults);
            } catch (TaskException e) {
                // Exception in the algorithm
                e.printStackTrace();
            }
        }

        // Calculation finished, printing summary and total number of solutions
        long end = System.currentTimeMillis();
        int nbslaves = instance.master.slavepoolSize();

        System.out.println("Total number of configurations found for n = " +
            nqueen_board_size + " and with " + nbslaves + " workers : " +
            sumResults);
        System.out.println("Time needed with " + nbslaves + " workers : " +
            ((end - begin) / 3600000) +
            String.format("h %1$tMm %1$tSs %1$tLms", end - begin));
        System.out.println("Total workers calculation time : " +
            (sumTime / 3600000) +
            String.format("h %1$tMm %1$tSs %1$tLms", sumTime));

        System.exit(0);
    }

    @Override
    protected void after_init() {
        String board_sizeString = cmd.getOptionValue("s");
        if (board_sizeString == null) {
            nqueen_board_size = DEFAULT_BOARD_SIZE;
        } else {
            nqueen_board_size = Integer.parseInt(board_sizeString);
        }

        String algodepthString = cmd.getOptionValue("D");
        if (algodepthString == null) {
            nqueen_algorithm_depth = DEFAULT_ALGORITHM_DEPTH;
        } else {
            nqueen_algorithm_depth = Integer.parseInt(algodepthString);
        }
    }

    @Override
    protected void before_init() {
        command_options.addOption("s", true, "nqueen board size");
        command_options.addOption("D", true, "nqueen algorithm depth");
        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("NQueensExample", command_options);
    }

    @Override
    protected ProActiveMaster<?extends Task<?extends Serializable>, ?extends Serializable> creation() {
        master = new ProActiveMaster<QueryExtern, Pair<Long, Long>>();
        return (ProActiveMaster<?extends Task<?extends Serializable>, ?extends Serializable>) master;
    }
}
