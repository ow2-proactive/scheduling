/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */

package org.objectweb.proactive.scheduler.policy;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.scheduler.Agent;
import org.objectweb.proactive.scheduler.DeployedTask;
import org.objectweb.proactive.scheduler.GenericJob;
import org.objectweb.proactive.scheduler.JobConstants;
import org.objectweb.proactive.scheduler.Queue;
import org.objectweb.proactive.scheduler.QueueFullException;
import org.objectweb.proactive.scheduler.RessourceManager;
import org.objectweb.proactive.scheduler.SchedulerConstants;


/**
 * This is an abstract class that contains all the essential tools for the job
 * managers to run. Mainly, the insertion and deployement of jobs, and an abstract
 * comparer that should be redefined in the specific policies.
 *
 * @author cjarjouh
 *
 */
public abstract class AbstractPolicy implements Serializable, RunActive,
    JobConstants, SchedulerConstants {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.JOB_MANAGER);
    protected RessourceManager ressourceManager;
    protected Queue queue;
    protected HashMap deployedTasks;

    public AbstractPolicy() {
    }

    /**
     * This is the main constructor for the creation of the job manager object.
     * @param ressourceManager the ressourceManager object responsible for
     *                 the management and the acquisition of the ressources.
     */
    public AbstractPolicy(RessourceManager ressourceManager) {
        if (ressourceManager != null) {
            deployedTasks = new HashMap();
            queue = new Queue();
            this.ressourceManager = ressourceManager;
            logger.debug("job manager created");
        }
    }

    /**
     * This method has been redefined to reimplement the way this active object
     * works and serves its methods.
     */
    public void runActivity(Body body) {
        Service service = new Service(body);

        while (true) {
            this.execute();

            if (service.hasRequestToServe()) {
                service.blockingServeOldest();
            }

            this.checkRunningTasks();
        }
    }

    /**
     * Check the list of the running jobs to find out if there is one that's
     * finished so that we can free the allocated ressources.
     *
     */
    public void checkRunningTasks() {
        if (!this.deployedTasks.isEmpty()) {
            Set runningProcessIds = this.deployedTasks.keySet();
            Iterator iterator = runningProcessIds.iterator();

            while (iterator.hasNext()) {
                String processId = (String) iterator.next();
                DeployedTask deployedTask = (DeployedTask) this.deployedTasks.get(processId);

                if (!(deployedTask.isAlive()).booleanValue()) {
                    this.deployedTasks.remove(processId);

                    GenericJob job = deployedTask.getTaskDescription();
                    int ressourceNb = job.getRessourceNb() - 1;
                    System.out.println("Task '" + job.getClassName() +
                        "' finished ...");
                    System.out.println("Freeing " + ressourceNb +
                        ((ressourceNb > 1) ? " ressources ..." : " ressource ..."));

                    ressourceManager.freeNodes(job.getJobID(), true);
                    job.setJobStatus("finished");
                    logger.debug("job finished " + job.getJobID());
                    // if we want we can always keep track of the finished jobs ...
                    break;
                }
            }
        }
    }

    /**
     * Insert job's descriptions in the queue.
     * @param job: the descriptions of the job
     * @return false if the queue is full and true otherwise.
     */
    public BooleanWrapper sub(GenericJob job) {
        try {
            job.setJobStatus("queued");
            queue.put(job);

            logger.debug("job added to the queue");

            return new BooleanWrapper(true);
        } catch (QueueFullException e) {
            logger.error("Couldn't add job to the queue. Queue full ...");
            return new BooleanWrapper(false);
        }
    }

    /**
     * Deletes the job from the queue and stops it if it has already been launched.
     * @param id: the id of the job
     * @return true if the operation completed succesfully, false orherwise.
     */
    public BooleanWrapper del(String jobId) {
        try {
            if (queue.remove(jobId) == null) {
                this.deployedTasks.remove(jobId);
                this.ressourceManager.freeNodes(jobId, false);
            }
            logger.debug("job deleted");
            return new BooleanWrapper(true);
        } catch (Exception e) {
            logger.error("couldn't delete job");
            return new BooleanWrapper(false);
        }
    }

    /**
     * Gives description of all the jobs that are curently running in forms of a
     * Vector if no specific id is specified, else, it gives the description of
     * the specified job if it exists.
     * @return an ArrayList containing the description of all the running jobs.
     */
    public Vector stat(String jobId) {
        Vector vector = new Vector();
        GenericJob jobDescription;
        if (jobId != null) {
            jobDescription = queue.get(jobId);
            if (jobDescription == null) {
                DeployedTask deployedTask = (DeployedTask) this.deployedTasks.get(jobId);
                if (deployedTask != null) {
                    jobDescription = deployedTask.getTaskDescription();
                }
            }

            if (jobDescription != null) {
                vector.add(jobDescription);
            }
        } else {
            vector.addAll(this.queue.values());

            Collection c = this.deployedTasks.values();
            Iterator iterator = c.iterator();

            while (iterator.hasNext()) {
                DeployedTask deployedTask = (DeployedTask) iterator.next();
                vector.add(deployedTask.getTaskDescription());
            }
        }
        logger.debug("job status evoqued");
        return vector;
    }

    /**
     * This is an abstract comparer method to be redefined by the specifique policy ...
     * Returns true if job1 is to be served before job2 according to the policy.
     * @param job1
     * @param job2
     * @return true if job1 is to be served before job2.
     */
    abstract public boolean isToBeServed(GenericJob job1, GenericJob job2);

    /**
     * returns the job that should run next (according to the implemented policy).
     * @return the job that should run next (according to the implemented policy).
     */
    public String nextTask() {
        // TODO Auto-generated method stub
        String jobId = null;
        GenericJob job = null;

        // TODO Auto-generated method stub
        String tmpId;
        GenericJob tmpTask;

        Set set = queue.keySet();
        Iterator iterator = set.iterator();

        // initialize jobId and job wwith the first job in the queue
        if (iterator.hasNext()) {
            jobId = (String) iterator.next();
            job = (GenericJob) queue.get(jobId);
        }

        // try to find the appropriate job to be served
        while (iterator.hasNext()) {
            tmpId = (String) iterator.next();

            tmpTask = (GenericJob) queue.get(tmpId);

            if (isToBeServed(tmpTask, job)) {
                jobId = tmpId;
                job = tmpTask;
            }
        }
        logger.debug("next task evoked");
        return jobId;
    }

    /**
     * returns the list of the deployed jobs.
     * @return the list of the deployed jobs.
     */
    public HashMap getDeployedTasks() {
        return this.deployedTasks;
    }

    /**
     * This method is used to execute a job if the required ressources are
     * available.
     *
     */
    public void execute() {
        int ressourceNb;
        int minRessourceNb;

        if (!queue.isEmpty()) {
            String jobId = nextTask();
            GenericJob job = queue.get(jobId);
            ressourceNb = job.getRessourceNb();
            minRessourceNb = job.getMinNbOfNodes();

            if (!ressourceManager.isAvailable(ressourceNb).booleanValue()) {
                if (!ressourceManager.isAvailable(minRessourceNb).booleanValue()) {
                    return;
                } else {
                    ressourceNb = minRessourceNb;
                }
            }

            // execute job ....
            // the job has to signal the ressources available after finishing it's processing ...
            queue.remove(jobId);

            try {
                Node node = ressourceManager.reserveNodes(jobId, ressourceNb);

                Agent agent = null;
                String jobName = job.getClassName();

                // Here we may have one of 2 cases:
                //  1- the job we're running is already running meaning
                //     that it's a job without an XML deployment descriptor
                //     and is asking in real time programming for nodes
                //     from the scheduler service
                //  2- the job isn't created yet and we need to deploy it
                if (jobName != null) {
                    // here we shall have to test that the job is non local:
                    String xmlPath = job.getXMLFullPath();

                    /*                        if (jobName != null) {
                                                    xmlPath = "/user/cjarjouh/home/";
                                                    File localSource = new File(job.getXMLFullPath());
                                                    File remoteDest = new File(xmlPath);
                                                    FileVector filePushed =FileTransfer.pushFile(node,localSource, remoteDest);
                                                    filePushed.waitForAll();  //wait for push to finish
                                            }
                     */
                    HashMap systemProperties = new HashMap();
                    systemProperties.put(XML_DESC, xmlPath);
                    systemProperties.put(JOB_ID, jobId);

                    agent = (Agent) ProActive.newActive(Agent.class.getName(),
                            new Object[] { systemProperties }, node);

                    // we need here to check if the node is local or remote to
                    // find out if we have to do some file transfering
                    // and we also have to set the system property file ...
                    // we also need to put a try catch here to find if the job is in
                    // the class path and maybe we shall need some file transfert:
                    /*
                     *                                         // we shall have to get the node of the scheduler first then do a transfert
                                        File remoteSource = new File("/remote/source/path/file");
                                            File localDest = new File("/local/destination/path/file");
                                            FileVector filePulled = FileTransfer.pullFile(examplenode[0], remoteSource, localDest);
                                            File  file = filePulled.getFile(0); //wait for pull to finish
                    */
                    ProActive.newMain(jobName, job.getMainParameters(), node);

                    System.out.println("Starting '" + jobName + "' job ...");
                }

                System.out.println("Allocation of " + ressourceNb +
                    ((ressourceNb > 1) ? " ressources ..." : " ressource ..."));
                job.setJobStatus("deployed");
                this.deployedTasks.put(jobId, new DeployedTask(job, agent));
                logger.debug("job deployed successfully");
            } catch (Exception e) {
                logger.error("error executing job");
            }
        }
    }

    /**
     * Does some cleaning before ending the job manager.
     *
     */
    public void end() {
    }

    /**
     * Returns the queue that stores the waiting jobs.
     * @return the queue that stores the waiting jobs.
     */
    public Queue getQueue() {
        return this.queue;
    }
}
