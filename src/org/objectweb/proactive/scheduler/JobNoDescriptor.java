package org.objectweb.proactive.scheduler;

import java.util.Vector;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.Job;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.node.Node;


/**
 * This is the class that supports the jobs that have no XML
 * descriptor API. They use directly the Scheduler API to
 * get nodes.
 * @author cjarjouh
 *
 */
public class JobNoDescriptor implements java.io.Serializable, Job, RunActive {
    private GenericJob job;
    private RessourceManager ressourceManager;
    private boolean flag = false;

    public JobNoDescriptor() {
    }

    public JobNoDescriptor(RessourceManager ressourceManager,
        Integer ressourceNb, Integer estimatedTime) {
        job = new GenericJob();
        job.setRessourceNb(ressourceNb.intValue());
        job.setEstimatedTime(estimatedTime.intValue());
        this.ressourceManager = ressourceManager;
    }

    public String getJobID() {
        return this.job.getJobID();
    }

    /**
     * This method is used to detect the node reservation event and to
     * help fetch those reserved nodes.
     * @return a vector of all the reserved nodes.
     */
    public Vector getNodes() {
        this.flag = true;
        while (!this.ressourceManager.checkReservation(this.getJobID())
                                         .booleanValue())
            ;
        int ressourceNb = this.job.getRessourceNb();
        Node[] nodes = this.ressourceManager.getNodes(this.getJobID(),
                ressourceNb);
        Vector result = new Vector();

        for (int i = 0; i < nodes.length; ++i)
            result.add(nodes[i]);

        return result;
    }

    public GenericJob getJobDescription() {
        return this.job;
    }

    /**
     * The runActivity is reimplemented to stop the service and to destroy the
     * active object after the getNodes method is called.
     */
    public void runActivity(Body body) {
        Service service = new Service(body);
        while (!this.flag) {
            service.waitForRequest();
            service.serveOldest();
        }
    }
}
