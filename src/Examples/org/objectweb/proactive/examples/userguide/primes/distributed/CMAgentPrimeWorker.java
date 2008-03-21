//@snippet-start primes_distributed_worker
package org.objectweb.proactive.examples.userguide.primes.distributed;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.examples.userguide.cmagent.simple.CMAgent;


/**
 * @author ActiveEon Team 
 */
public class CMAgentPrimeWorker extends CMAgent {

    /**
     * Tests a primality of a specified number in a specified range.
     * 
     * @param candidate
     *            the candidate number to check
     * @param begin
     *            starts check from this value
     * @param end
     *            checks until this value
     * @return <code>true</code> if is prime; <code>false</code> otherwise
     */
    public BooleanWrapper isPrime(final long candidate, final long begin, final long end) {
        /*******************************************************/
        /* 4. Return a reifiable wrapper for the Boolean type  */
        /*    for asynchronous calls.                          */
        /*******************************************************/
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (long divider = begin; divider < end; divider++) {
            if ((candidate % divider) == 0) {
                return new BooleanWrapper(false);
            }
        }
        return new BooleanWrapper(true);
    }

}
//@snippet-end primes_distributed_worker