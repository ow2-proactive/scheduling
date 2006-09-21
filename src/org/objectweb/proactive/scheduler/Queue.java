package org.objectweb.proactive.scheduler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;


/**
 * This is the class definition of the queue that shall be used to store the
 * jobs to be started in a HashMap.
 * @author cjarjouh
 *
 */
public class Queue implements java.io.Serializable {
    private HashMap queue;
    private int queueSize;

    public Queue() {
        queue = new HashMap();
        queueSize = 100;
    }

    /**
     * Returns the number of the waiting jobs.
     * @return the number of the waiting jobs.
     */
    public int size() {
        return this.queue.size();
    }

    /**
     * Search for a job by job Id.
     * @param Id is the id of the job.
     * @return true if the job exists, null otherwise.
     */
    public boolean containsId(String jobId) {
        return this.queue.containsKey(jobId);
    }

    /**
     * Inserts the job to the queue and give it an Id.
     * @param job contains the description of the job to be deployed.
     * @throws QueueFullException whenever the queue is full.
     */
    public void put(GenericJob job) throws QueueFullException {
        //    	System.out.println(".......................................");
        if (this.size() == queueSize) {
            throw new QueueFullException();
        }

        this.queue.put(job.getJobID(), job);
    }

    /**
     * Gives a list of the IDs of the waiting jobs.
     * @return a set of all the keys
     */
    public Set keySet() {
        return queue.keySet();
    }

    /**
     * returns the job associated with the job Id.
     * @param Id is the Id of the job needed
     * @return the description of the job associated with the job Id.
     */
    public GenericJob get(String jobId) {
        return (GenericJob) this.queue.get(jobId);
    }

    /**
     * removes and returns the job associated with the job Id from the queue.
     * @param Id is the Id of the job needed
     * @return the description of the job associated with the job Id.
     */
    public GenericJob remove(String jobId) {
        return (GenericJob) this.queue.remove(jobId);
    }

    /**
     * returns true if the queue is empty, false otherwise.
     * @return true if the queue is empty, false otherwise.
     */
    public boolean isEmpty() {
        return this.queue.isEmpty();
    }

    /**
     * returns a collection of the genericJob description
     * @return a collection of the genericJob description
     */
    public Collection values() {
        return this.queue.values();
    }
}
