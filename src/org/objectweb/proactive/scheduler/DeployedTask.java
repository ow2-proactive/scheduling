package org.objectweb.proactive.scheduler;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;


/**
 * Class that contains the description of the job and the reference to an
 * agent that keeps track of the job evolution. This agent helps setting
 * the main JVM system properties as well a simple method to make sure
 * that this node stays alive.
 * @author cjarjouh
 *
 */
public class DeployedTask {
    private GenericJob jobDescription;
    private Agent agent;

    public DeployedTask(GenericJob jobDescription, Agent agent) {
        this.setAgent(agent);
        this.setTaskDescription(jobDescription);

        // TODO Auto-generated constructor stub
    }

    public void setTaskDescription(GenericJob jobDescription) {
        this.jobDescription = jobDescription;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public GenericJob getTaskDescription() {
        return this.jobDescription;
    }

    /**
     * this method is a sort of pinging method. It gives the status
     * of the main node (alive or dead)
     * @return true if the main node is alive, false otherwise
     */
    public BooleanWrapper isAlive() {
        if (agent == null) {
            return new BooleanWrapper(true);
        }

        try {
            this.agent.ping();

            return new BooleanWrapper(true);
        } catch (Exception e) {
            return new BooleanWrapper(false);
        }
    }
}
