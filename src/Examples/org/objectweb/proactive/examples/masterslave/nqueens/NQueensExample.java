package org.objectweb.proactive.examples.masterslave.nqueens;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Vector;

import org.objectweb.proactive.examples.masterslave.nqueens.query.Query;
import org.objectweb.proactive.examples.masterslave.nqueens.query.QueryGenerator;
import org.objectweb.proactive.extra.masterslave.ProActiveMaster;
import org.objectweb.proactive.extra.masterslave.TaskException;


/**
 * This examples calculates the Nqueen
 * @author fviale
 *
 */
public class NQueensExample {
    public static final String DEFAULT_DESCRIPTOR = "./RSHListbyHost_Example.xml";
    public static final String DEFAULT_VN_NAME = "matrixNode";
    public static final int DEFAULT_NQUEEN_BOARD = 20;
    public static final int DEFAULT_NQUEEN_ALG_DEPTH = 3;
    public static String descriptor_path;
    public static String vn_name;
    public static int nqueen_board_size;
    public static int nqueen_algorithm_depth;

    /**
     * Initializing the example with command line arguments
     * @param args
     */
    public static void init(String[] args) {
        if (args.length == 0) {
            descriptor_path = DEFAULT_DESCRIPTOR;
            vn_name = DEFAULT_VN_NAME;
            nqueen_board_size = DEFAULT_NQUEEN_BOARD;
            nqueen_algorithm_depth = DEFAULT_NQUEEN_ALG_DEPTH;
        } else if (args.length == 4) {
            descriptor_path = args[0];
            vn_name = args[1];
            nqueen_board_size = Integer.parseInt(args[2]);
            nqueen_algorithm_depth = Integer.parseInt(args[3]);
        } else {
            System.out.println(
                "Usage: <java_command> [descriptor_path virtual_node_name nqueen_board_size nqueen_algorithm_depth]");
        }
    }

    public static void main(String[] args) throws MalformedURLException {
        // Getting command line parameters
        init(args);

        // Creating the Master
        ProActiveMaster master = new ProActiveMaster(new URL(descriptor_path),
                vn_name);

        // Generating the queries for the NQueens
        Vector<Query> queries = QueryGenerator.generateQueries(nqueen_board_size,
                nqueen_algorithm_depth);
        System.out.println("Launching NQUEENS solutions finder for n = " +
            nqueen_board_size + " with a depth of " + nqueen_algorithm_depth);
        master.solveAll(queries);
        try {
            long sumResults = 0;
            long begin = System.currentTimeMillis();

            // Waiting for the results
            Collection<Long> results = master.waitAllResults();
            for (long res : results) {
                sumResults += res;
            }
            long end = System.currentTimeMillis();

            System.out.println("Total number of configurations found in " +
                String.format("%1$tT", end - begin) + " for n = " +
                nqueen_board_size + " : " + sumResults);
        } catch (TaskException e) {
            // Exception in the algorithm
            e.printStackTrace();
        }
    }
}
