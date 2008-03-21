//@snippet-start primes_distributedmw_task
package org.objectweb.proactive.examples.userguide.primes.distributedmw;

import org.objectweb.proactive.extensions.masterworker.interfaces.Task;
import org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory;


/**
 * Task to find if any number in a specified interval divides the given
 * candidate
 * 
 * @author ActiveEon Team
 * 
 */
public class FindPrimeTask implements Task<Boolean> {
    private static final long serialVersionUID = -6474502532363614259L;

    private long begin;
    private long end;
    private long taskCandidate;

    /*******************************************/
    /* 1. Write the constructor for this task */
    /*******************************************/
    public FindPrimeTask(long taskCandidate, long begin, long end) {
        this.begin = begin;
        this.end = end;
        this.taskCandidate = taskCandidate;
    }

    /*******************************************/

    /*******************************************************/
    /* 2. Fill the code that checks if the taskCandidate */
    /* is prime. Note that no wrappers are needed ! */
    /*******************************************************/
    public Boolean run(WorkerMemory memory) {
        try {
            Thread.sleep(300);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (long divider = begin; divider < end; divider++) {
            if ((taskCandidate % divider) == 0) {
                return new Boolean(false);
            }
        }
        return new Boolean(true);
    }
    /*******************************************************/

}
//@snippet-end primes_distributedmw_task