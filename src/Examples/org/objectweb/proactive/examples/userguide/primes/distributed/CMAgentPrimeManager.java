//@snippet-start primes_distributed_manager
package org.objectweb.proactive.examples.userguide.primes.distributed;

import java.util.Vector;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;


/**
 * @author ActiveEon Team
 */
public class CMAgentPrimeManager {
    /**
     * A vector of references on workers
     */
    private Vector<CMAgentPrimeWorker> workers = new Vector<CMAgentPrimeWorker>();
    /**
     * Default interval size
     */
    public static final int INTERVAL_SIZE = 10000;

    /**
     * Empty no-arg constructor needed by ProActive
     */
    public CMAgentPrimeManager() {
    }

    /**
     * Tests a primality of a specified number. Synchronous !
     * 
     * @param number
     *            The number to test
     * @return <code>true</code> if is prime; <code>false</code> otherwise
     */
    public boolean isPrime(long number) {
        // We don't need to check numbers greater than the square-root of the
        // candidate in this algorithm
        long squareRootOfCandidate = (long) Math.ceil(Math.sqrt(number));

        // Begin from 2 the first known prime number
        long begin = 2;

        // The number of intervals       
        long nbOfIntervals = (long) Math.ceil(squareRootOfCandidate / INTERVAL_SIZE);

        // Until the end of the first interval
        long end = INTERVAL_SIZE;

        // The vector of futures
        final Vector<BooleanWrapper> answers = new Vector<BooleanWrapper>();

        // Non blocking (asynchronous method call) 
        for (int i = 0; i <= nbOfIntervals; i++) {

            // Use round robin selection of worker
            int workerIndex = i % workers.size();
            CMAgentPrimeWorker worker = workers.get(workerIndex);

            // Send asynchronous method call to the worker
            BooleanWrapper res = worker.isPrime(number, begin, end);

            // Adds the future to the vector
            answers.add(res);

            // Update the begin and the end of the interval
            begin = end + 1;
            end += INTERVAL_SIZE;
        }
        // Once all requests was sent
        boolean prime = true;

        // Loop until a worker returns false or vector is empty (all results have been checked)
        while (!answers.isEmpty() && prime) {

            // Will block until a new response is available
            int intervalNumber = PAFuture.waitForAny(answers);

            // Check the answer
            prime = answers.get(intervalNumber).booleanValue();

            // Remove the actualized future			
            answers.remove(intervalNumber);

        }
        return prime;
    }

    /**
     * Adds a worker to the local vector
     * 
     * @param worker
     *            The worker to add to the vector
     */
    public void addWorker(CMAgentPrimeWorker worker) {
        this.workers.add(worker);
    }
}
// @snippet-end primes_distributed_manager
