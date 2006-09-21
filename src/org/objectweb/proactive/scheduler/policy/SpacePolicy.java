package org.objectweb.proactive.scheduler.policy;

import org.objectweb.proactive.scheduler.*;


/**
 * Space policy is a policy where the jobs that need the minimum of ressources
 * are served first. If more than one job need the same amount of ressources,
 * the oldest job in the queue is served first.
 *
 * @author cjarjouh
 *
 */
public class SpacePolicy extends AbstractPolicy {
    public SpacePolicy() {
        super();

        // TODO Auto-generated constructor stub
    }

    public SpacePolicy(RessourceManager ressourceManager) {
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
        int ressourceNb1 = job1.getRessourceNb();
        int ressourceNb2 = job2.getRessourceNb();

        if ((ressourceNb1 < ressourceNb2) ||
                ((ressourceNb1 == ressourceNb2) &&
                (job1.getSubmitDate().before(job2.getSubmitDate())))) {
            return true;
        } else {
            return false;
        }
    }
}
