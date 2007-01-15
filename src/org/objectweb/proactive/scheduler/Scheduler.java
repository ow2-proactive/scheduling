package org.objectweb.proactive.scheduler;

import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.StringMutableWrapper;
import org.objectweb.proactive.scheduler.policy.*;


/**
 * This is the class of the scheduler daemon.
 * @author cjarjouh
 *
 */
public class Scheduler implements java.io.Serializable, SchedulerConstants {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.SCHEDULER);
    static private Scheduler scheduler; // singleton
    private AbstractPolicy policy;
    private RessourceManager ressourceManager;
    private HashMap tmpJobs;

    public Scheduler() {
    }

    /**
     * Scheduler constructor that instanciate an active object used to manage jobs
     * knowing the policy class name and creates an active object ressource manager.
     * @param policyClass, the class name of the policy
     */
    public Scheduler(String policyClass) {
        try {
            String nodeURL = "//localhost:" +
                System.getProperty("proactive.rmi.port") + "/" +
                SCHEDULER_NODE_NAME;
            this.tmpJobs = new HashMap();
            this.ressourceManager = (RessourceManager) ProActive.newActive(RessourceManager.class.getName(),
                    new Object[] { new BooleanWrapper(true) }, nodeURL);

            Object[] constructorParameters = new Object[1];
            constructorParameters[0] = this.ressourceManager;
            this.policy = (AbstractPolicy) ProActive.newActive(policyClass,
                    constructorParameters, nodeURL);

            Scheduler.scheduler = this;
            logger.debug("Scheduler created ...");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void init() {
    }

    /**
     * Insert a job in the queue of the scheduler.
     * @param job, the descriptions of the job to be inserted
     * @return true if the queue is not full and false for the other way around.
     */
    public BooleanWrapper sub(GenericJob job) {
        logger.debug("sub method evoked ....");

        return this.policy.sub(job);
    }

    /**
     * Deletes the job from the queue and stops it if it has already been launched.
     * @param id: the id of the job
     * @return true if the operation completed succesfully, false orherwise.
     */
    public BooleanWrapper del(String jobId) {
        logger.debug("del method evoked ....");

        return this.policy.del(jobId);
    }

    /**
     * Gives description of all the jobs that are curently running in forms of a
     * Vector if no specific id is specified, else, it gives the description of
     * the specified job if it exists.
     * @return an ArrayList containing the description of all the running jobs.
     */
    public Vector stat(String jobId) {
        logger.debug("stat method evoked ....");
        return this.policy.stat(jobId);
    }

    /**
     * Provides the information about the nodes (state, job running, ...)
     * @return information about the nodes in forms of an ArrayList and null if
     *         the node is not defined
     */
    public Vector nodes(String nodeURL) {
        logger.debug("nodes method evoked ....");
        return this.ressourceManager.nodes(nodeURL);
    }

    /**
     * This method is used to create a unique scheduler object on the machine.
     * If the scheduler isn't already created, it creates a new instance.
     * @param policyName the name of the policy used by the scheduler
     * @return the scheduler deployed on the machine
     * @throws Exception when having difficulty with the creation of the scheduler.
     */
    static public void createScheduler(String policyName) {
        // tests if the service already exists
        String nodeURL = System.getProperty(SCHEDULER_URL) + "/" +
            SCHEDULER_NODE_NAME;
        Scheduler.scheduler = Scheduler.connectTo(nodeURL);

        if (Scheduler.scheduler == null) {
            //    		JVMProcessImpl rsh = new JVMProcessImpl(new StandardOutputMessageLogger());
            //        	rsh.setClassname("org.objectweb.proactive.core.node.StartNode");
            //        	rsh.setParameters(nodeURL);
            //        	rsh.startProcess();       	        	

            //        	Thread.sleep(3000);

            //String nodeURL = "//localhost/" + SCHEDULER_NODE_NAME;
            ProActiveRuntime paRuntime;

            try {
                paRuntime = RuntimeFactory.getProtocolSpecificRuntime(System.getProperty(
                            "proactive.communication.protocol") + ":");
                nodeURL = paRuntime.createLocalNode(SCHEDULER_NODE_NAME, false,
                        null, paRuntime.getVMInformation().getName(),
                        paRuntime.getJobID());

                Object[] constructorParameters = { policyName };
                scheduler = (Scheduler) ProActive.newActive(Scheduler.class.getName(),
                        constructorParameters, nodeURL);

                logger.debug("created object scheduler");
                //scheduler.init();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                logger.error("error creating object scheduler");
            }
        }
    }

    /**
     * Starts the scheduler.
     *
     */
    public static void start(String policyName) {
        try {
            Scheduler.createScheduler(policyName);
            logger.debug("service started successfully");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.error("error starting service");
        }
    }

    /**
     * Method invoqued when we want to shutdown the scheduler. We can free
     * the ressources here for example ...
     *
     */
    public void end() {
        logger.debug("shutting down scheduler service");
        policy.end();
    }

    /**
     * Gives intormation about the scheduler. (Version ...)
     *
     */
    public StringMutableWrapper info() {
        return new StringMutableWrapper("Scheduler beta");
    }

    /**
     * Redefines the finalize method to do some cleaning.
     */
    protected void finalize() {
        this.end();
        Scheduler.scheduler = null;
    }

    /**
     * This method launches the parsing of the XML file to extract the description
     * of the job submitted prior to its submission to the queue.
     * @return the jobId of the newly created object
     */
    public StringMutableWrapper fetchJobDescription(String xmlDescriptorUrl) {
        try {
            // it is here that we shall enter the modifications if the submission of the 
            // xml file is remote...
            GenericJob tmp = new GenericJob();
            String xmlFileName = xmlDescriptorUrl.substring(xmlDescriptorUrl.lastIndexOf(
                        '/') + 1);
            String jobId = tmp.getJobID();
            tmp.setXMLDescriptorName(xmlFileName);
            tmp.setXMLFullPath(xmlDescriptorUrl);
            this.tmpJobs.put(jobId, tmp);

            ProActiveJobHandler h = new ProActiveJobHandler(this, jobId,
                    xmlDescriptorUrl);
            String uri = xmlDescriptorUrl;
            org.objectweb.proactive.core.xml.io.StreamReader sr = new org.objectweb.proactive.core.xml.io.StreamReader(new org.xml.sax.InputSource(
                        uri), h);
            sr.read();

            logger.debug("starting the parsing of the newly added XML file");

            return new StringMutableWrapper(jobId);
        } catch (Exception e) {
            // log it for futur reference ...
            logger.error("error parsing the file");
            return null;
        }
    }

    /**
     * connects to the scheduler node and fetchs the scheduler daemon using
     * the submitted url
     * @param schedulerURL the url of the scheduler node
     * @return the scheduler object
     */
    public static Scheduler connectTo(String schedulerURL) {
        try {
            Node node = NodeFactory.getNode(schedulerURL);

            Object[] ao = node.getActiveObjects(Scheduler.class.getName());

            if (ao.length == 1) {
                logger.debug("scheduler object fetched");
                return ((Scheduler) ao[0]);
            } else {
                logger.debug("no scheduler object created");
                return null;
            }
        } catch (Exception e) {
            logger.error("error connecting the scheduler service:" + e);
            return null;
        }
    }

    public static void main(String[] args) throws Exception {
        String policyName = "org.objectweb.proactive.scheduler.policy.FIFOPolicy";
        Scheduler.start(policyName);
        Thread.sleep(3000);
    }

    /**
     * This method is used while programming .. You can use it to reserve
     * submit your demand for ressources...
     */
    public Vector getNodes(int ressourceNb, int estimatedTime) {
        Object[] constructorParameters = new Object[3];
        constructorParameters[0] = this.ressourceManager;
        constructorParameters[1] = new Integer(ressourceNb);
        constructorParameters[2] = new Integer(estimatedTime);
        try {
            JobNoDescriptor job = (JobNoDescriptor) ProActive.newActive(JobNoDescriptor.class.getName(),
                    constructorParameters);
            GenericJob jobDescription = job.getJobDescription();
            this.sub(jobDescription);
            logger.debug("new object created in the queue");
            return job.getNodes();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.error("error submitting the command to the scheduler");

            return null;
        }
    }

    // ------------------
    // methods used for parsing .....

    /**
     * returns an array of the reserved nodes of the object with the specified jobId
     */
    public Node[] getReservedNodes(String jobID, int askedNodes) {
        logger.debug("returning reserved nodes");
        return this.ressourceManager.getNodes(jobID, askedNodes);
    }

    /**
     * commits the job's description and submits it to the waiting queue
     * @param jobID the ID of the job
     */
    public void commit(String jobID) {
        if (this.tmpJobs.containsKey(jobID)) {
            System.out.println("committing the job's description");

            GenericJob job = (GenericJob) this.tmpJobs.remove(jobID);
            this.sub(job);
            logger.debug("commiting the job's description");
        } else {
            logger.debug("no object to commit");
        }
    }

    /**
     * Gets the temporary created generic job object to change it's attribute's content.
     * It is important to note that this method is only used while parsing.
     * @param jobID the job id of the temporary job description object.
     * @return the temporary object or null if the object oesn't exist.
     */
    public GenericJob getTmpJob(String jobID) {
        if (this.tmpJobs.containsKey(jobID)) {
            return (GenericJob) this.tmpJobs.get(jobID);
        } else {
            return null;
        }
    }
}
