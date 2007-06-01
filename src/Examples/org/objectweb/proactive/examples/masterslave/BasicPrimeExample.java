package org.objectweb.proactive.examples.masterslave;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.objectweb.proactive.extra.masterslave.ProActiveMaster;
import org.objectweb.proactive.extra.masterslave.TaskException;
import org.objectweb.proactive.extra.masterslave.interfaces.SlaveMemory;
import org.objectweb.proactive.extra.masterslave.interfaces.Task;


/**
 * This simple test class is an example on how to use the Master/Slave API
 * The tasks wait for a period between 15 and 20 seconds (by ex)
 * The main program displays statistics about the speedup due to parallelization
 * @author fviale
 *
 */
public class BasicPrimeExample extends AbstractExample {
    public static int number_of_intervals;
    public static long prime_to_find;

    /**
     * Displays result of this test
     * @param results results of the test
     * @param startTime starting time of the test
     * @param endTime ending time of the test
     * @param nbSlaves number of slaves used during the test
     */
    public static void displayResult(Collection<Boolean> results,
        long startTime, long endTime, int nbSlaves) {
        // Post processing, calculates the statistics
        boolean prime = true;

        for (Boolean result : results) {
            prime = prime && result;
        }

        long effective_time = (int) (endTime - startTime);
        //      Displaying the result
        System.out.println("" + prime_to_find +
            (prime ? " is prime." : " is not prime."));

        System.out.println("Calculation time (ms): " + effective_time);
    }

    /**
     * @param args
     * @throws TaskException
     * @throws MalformedURLException
     */
    public static void main(String[] args)
        throws TaskException, MalformedURLException {
        NativeExample instance = new NativeExample();
        //   Getting command line parameters
        instance.init(args, 2, " prime_to_find number_of_intervals");

        //      Creating the Master
        ProActiveMaster<FindPrimeTask, Boolean> master = new ProActiveMaster<FindPrimeTask, Boolean>(instance.descriptor_url,
                instance.vn_name);

        // Creating the tasks to be solved
        List<FindPrimeTask> tasks = new ArrayList<FindPrimeTask>();
        long square_root_of_candidate = (long) Math.ceil(Math.sqrt(
                    prime_to_find));
        for (int i = 0; i < number_of_intervals; i++) {
            tasks.add(new FindPrimeTask(prime_to_find,
                    square_root_of_candidate / (number_of_intervals / i),
                    square_root_of_candidate / (number_of_intervals / (i + 1))));
        }
        long startTime = System.currentTimeMillis();
        // Submitting the tasks
        master.solve(tasks, false);

        // Collecting the results
        Collection<Boolean> results = master.waitAllResults();
        long endTime = System.currentTimeMillis();

        // Displaying result
        displayResult(results, startTime, endTime, master.slavepoolSize());

        // Terminating the Master
        master.terminate(true);

        System.exit(0);
    }

    @Override
    protected void init_specialized(String[] args) {
        prime_to_find = Long.parseLong(args[2]);
        number_of_intervals = Integer.parseInt(args[3]);
    }

    public static class FindPrimeTask implements Task<Boolean> {
        private long begin;
        private long end;
        private long prime;

        public FindPrimeTask(long prime, long begin, long end) {
            this.begin = begin;
            this.end = end;
            this.prime = prime;
        }

        /* (non-Javadoc)
         * @see org.objectweb.proactive.extra.masterslave.interfaces.Task#run(org.objectweb.proactive.extra.masterslave.interfaces.SlaveMemory)
         */
        public Boolean run(SlaveMemory memory) {
            for (long divider = begin; divider < end; divider++) {
                if ((prime % divider) == 0) {
                    return new Boolean(false);
                }
            }
            return new Boolean(true);
        }
    }
}
