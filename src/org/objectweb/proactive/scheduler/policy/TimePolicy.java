package org.objectweb.proactive.scheduler.policy;

import org.objectweb.proactive.scheduler.*;


/**
 * Time policy is a policy where the quickest job is served first.
 *
 * @author cjarjouh
 *
 */
public class TimePolicy extends AbstractPolicy {
    public TimePolicy() {
        super();

        // TODO Auto-generated constructor stub
    }

    public TimePolicy(RessourceManager ressourceManager) {
        super(ressourceManager);
    }

    /**
     * Returns true if job1 is to be served before job2 according to the policy.
     * @param job1
     * @param job2
     * @return true if job1 is to be served before job2.
     */
    public boolean isToBeServed(GenericJob job1, GenericJob job2) {
        // TODO Auto-generated method stub
        if (job1.getEstimatedTime() < job2.getEstimatedTime()) {
            return true;
        } else {
            return false;
        }
    }
}
