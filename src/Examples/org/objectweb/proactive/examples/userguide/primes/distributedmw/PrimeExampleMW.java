//@snippet-start primes_distributedmw_example
package org.objectweb.proactive.examples.userguide.primes.distributedmw;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.extensions.masterworker.ProActiveMaster;
import org.objectweb.proactive.extensions.masterworker.interfaces.Task;
import org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory;


/**
 * 
 * @author ActiveEon Team
 * 
 */
public class PrimeExampleMW {
    /**
     * Default number of intervals
     */
    public static final int NUMBER_OF_INTERVALS = 15;

    public static void main(String[] args) {
        // The default value for the candidate to test (is prime)
        long candidate = 4398042316799l;
        // Parse the number from args if there is some
        if (args.length > 1) {
            try {
                candidate = Long.parseLong(args[1]);
            } catch (NumberFormatException numberException) {
                System.err.println("Usage: PrimeExampleMW <candidate>");
                System.err.println(numberException.getMessage());
            }
        }
        try {
            // Create the Master
            ProActiveMaster<FindPrimeTask, Boolean> master = new ProActiveMaster<FindPrimeTask, Boolean>();
            // Deploy resources
            for (VirtualNode vNode : deploy(args[0])) {
                master.addResources(vNode);
            }
            // Create and submit the tasks
            master.solve(createTasks(candidate));
            // Collect results
            List<Boolean> results = master.waitAllResults();
            // Test the primality
            boolean isPrime = true;
            for (Boolean result : results) {
                isPrime = isPrime && result;
            }
            // Display the result
            System.out.println("\n" + candidate + (isPrime ? " is prime." : " is not prime.") + "\n");
            // Terminate the master and free all resources
            master.terminate(true);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    /**
     * Creates the prime computation tasks to be solved
     * 
     * @return A list of prime computation tasks
     */
    public static List<FindPrimeTask> createTasks(long number) {
        List<FindPrimeTask> tasks = new ArrayList<FindPrimeTask>();

        // We don't need to check numbers greater than the square-root of the
        // candidate in this algorithm
        double squareRootOfCandidate = (long) Math.ceil(Math.sqrt(number));

        // Begin from 2 the first known prime number
        long begin = 2;

        // The size of the interval [begin ... end]
        long intervalSize = (long) Math.ceil(squareRootOfCandidate / NUMBER_OF_INTERVALS);

        // Until the end of the range
        long end = (long) intervalSize;

        for (int i = 0; i < NUMBER_OF_INTERVALS; i++) {

            // Adds the future to the vector
            tasks.add(new FindPrimeTask(number, begin, end));

            // Update the begin of the interval
            begin = end + 1;
            end += intervalSize;
        }

        return tasks;
    }

    private static VirtualNode[] deploy(String descriptor) {
        ProActiveDescriptor pad;
        try {
            pad = PADeployment.getProactiveDescriptor(descriptor);
            // active all Virtual Nodes
            pad.activateMappings();
            // get the first Node available in the first Virtual Node
            // specified in the descriptor file
            return pad.getVirtualNodes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Task to find if any number in a specified interval divides the given
     * candidate
     * 
     * @author ActiveEon Team
     * 
     */
    public static class FindPrimeTask implements Task<Boolean> {
        private static final long serialVersionUID = -6474502532363614259L;

        private long begin;
        private long end;
        private long taskCandidate;

        public FindPrimeTask(long taskCandidate, long begin, long end) {
            this.begin = begin;
            this.end = end;
            this.taskCandidate = taskCandidate;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.objectweb.proactive.extensions.masterworker.interfaces.Task#run(org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory)
         */
        public Boolean run(WorkerMemory memory) {
            for (long divider = begin; divider < end; divider++) {
                if ((taskCandidate % divider) == 0) {
                    return new Boolean(false);
                }
            }
            return new Boolean(true);
        }
    }
}
//@snippet-end primes_distributedmw_example