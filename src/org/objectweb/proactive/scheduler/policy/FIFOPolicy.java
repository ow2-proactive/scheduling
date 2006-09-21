package org.objectweb.proactive.scheduler.policy;

import org.objectweb.proactive.scheduler.*;


/**
 * FIFO Policy is a policy where the jobs are served by the order of their
 * submission to the scheduler. There are no priority, the oldest job in
 * the queue is served first.
 *
 * @author cjarjouh
 *
 */
public class FIFOPolicy extends AbstractPolicy {
    public FIFOPolicy() {
        // TODO Auto-generated constructor stub
    }

    public FIFOPolicy(RessourceManager ressourceManager) {
        super(ressourceManager);
    }

    /**
     * Returns true if job1 is to be served before job2 according to the policy.
     * @param job1
     * @param job2
     * @return true if job1 is to be served before job2.
     */
    public boolean isToBeServed(GenericJob job1, GenericJob job2) {
        if (job1.getSubmitDate().before(job2.getSubmitDate())) {
            return true;
        } else {
            return false;
        }
    }
}
