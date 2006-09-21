package org.objectweb.proactive.scheduler.policy;

import java.util.Vector;

import org.objectweb.proactive.scheduler.GenericJob;
import org.objectweb.proactive.scheduler.RessourceManager;


/**
 * This is the mixed policy class that takes, while instanciating, the policy names and
 * creates a vector of the policies for the only purpose of using its comparor
 * @author cjarjouh
 *
 */
public class MixedPolicy extends AbstractPolicy {
    private Vector policies;

    public MixedPolicy() {
        // TODO Auto-generated constructor stub
    }

    /**
     * Here we create a mixed policy job manager by submitting the ressource manager
     * object and a vector of the policies that shall form this policy.
     * @param ressourceManager the reference to the ressource manager active object
     *                 responsible of the ressourc allocation
     * @param policyNames a vector of the policies names that form this policy
     */
    public MixedPolicy(RessourceManager ressourceManager, Vector policyNames) {
        super(ressourceManager);
        this.policies = new Vector();
        for (int i = 0; i < policyNames.size(); ++i) {
            String policyName = (String) policyNames.get(i);
            try {
                policies.add(Class.forName(policyName).newInstance());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns true if job1 is to be served before job2 according to the policy.
     * @param job1
     * @param job2
     * @return true if job1 is to be served before job2.
     */
    public boolean isToBeServed(GenericJob job1, GenericJob job2) {
        // TODO Auto-generated method stub
        for (int i = 0; i < this.policies.size(); ++i) {
            AbstractPolicy policy = (AbstractPolicy) this.policies.get(i);
            if (policy.isToBeServed(job2, job1)) {
                return false;
            }
        }

        return true;
    }
}
