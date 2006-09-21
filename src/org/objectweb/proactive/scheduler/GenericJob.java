package org.objectweb.proactive.scheduler;

import java.io.Serializable;
import java.util.Date;
import java.util.Vector;

import org.objectweb.proactive.Job;
import org.objectweb.proactive.core.UniqueID;


/**
 * This class includes the definition and description of the jobs ...
 *  - "classname" of the class to be run
 *  - "priority" of the job
 *  - "userId" is the id  of the user who posted the job.
 *  - "submitDate" gives the date when the job was submitted.
 *  - "mainParameters" contains the parameters that should be submitted to the job
 *    when starting it.
 *  - "ressourceNb" indicates the number of processors needed to execute this job.
 *  - "estimatedTime" gives an estimate of the time to finish the job.
 *  - "xmlName" gives a mean to store the path to the deployment descriptor if present.
 *  - "startDate" is the date when the job should start execution
 *  - "jobId" is the id of the job
 *  - "jvmParameters" is the JVM system properties of the main node
 *  - "jobStatus" is the status of the job. It can take one of the following values:
 *    queued, deployed, finished
 *  - "classPath" offers the class paths of the main node
 *  - "minRessourceNb" is an optional field that, if set, indicates the minimum required
 *    ressource to enable the job to run
 * @author cjarjouh
 *
 */
public class GenericJob implements Serializable, Job {
    private String className;
    private int priority;
    private int userId;
    private Date submitDate;
    private Vector mainParameters;

    //    private Node [] nodes;
    private int ressourceNb;

    //    private Object[] constructorParameters;
    private int estimatedTime;
    private String xmlName;
    private String xmlFullPath;

    //    private Date startDate;
    private UniqueID jobId;
    private String jvmParameters;
    private String jobStatus;
    private String[] classPath;
    private int minRessourceNb = 0;

    //  private Date elapsedTime;    
    public GenericJob() {
        //       this(null, 0, 0, 0, 1, null, null, new Vector(), new Date());
        this.setClassName(null);
        this.setPriority(0);
        this.setUserId(0);
        this.setSubmitDate(new Date());
        this.setMainParameters(new Vector());
        this.setRessourceNb(1);
        this.setEstimatedTime(0);
        this.jobId = new UniqueID();
        this.classPath = null;
    }

    /*
        public GenericJob(String className, int userId, int estimatedTime) {
            this(className, userId, estimatedTime, 0, 1, null, null, null,
                new Date());
        }

        public GenericJob(String className, int userId, int estimatedTime,
            int priority) {
            this(className, userId, estimatedTime, priority, 1, null, null, null,
                new Date());
        }

        public GenericJob(String className, int userId, int estimatedTime,
            int priority, int ressourceNb) {
            this(className, userId, estimatedTime, priority, ressourceNb, null,
                null, null, new Date());
        }

        public GenericJob(String className, int userId, int estimatedTime,
            int priority, int ressourceNb, Object[] constructorParameters) {
            this(className, userId, estimatedTime, priority, ressourceNb,
                constructorParameters, null, null, new Date());
        }

        public GenericJob(String className, int userId, int estimatedTime,
            int priority, int ressourceNb, Object[] constructorParameters,
            Node [] nodes) {
            this(className, userId, estimatedTime, priority, ressourceNb,
                constructorParameters, nodes, null, new Date());
        }

        public GenericJob(String className, int userId, int estimatedTime,
            int priority, int ressourceNb, Object[] constructorParameters,
            Node [] nodes, Vector mainParameters) {
            this(className, userId, estimatedTime, priority, ressourceNb,
                constructorParameters, nodes, mainParameters, new Date());
        }

        public GenericJob(String className, int userId, int estimatedTime,
            int priority, int ressourceNb, Object[] constructorParameters,
            Node [] nodes, Vector mainParameters, Date submitDate) {
            this.setClassName(className);
            this.setPriority(priority);
            this.setUserId(userId);
            this.setSubmitDate(submitDate);
            this.setMainParameters(mainParameters);
            this.setNodes(nodes);
            this.setRessourceNb(ressourceNb);
            this.setConstructorParameters(constructorParameters);
            this.setEstimatedTime(estimatedTime);

        }
    */
    public void setClassName(String className) {
        this.className = className;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setSubmitDate() {
        this.submitDate = new Date();
    }

    public void setSubmitDate(Date submitDate) {
        this.submitDate = submitDate;
    }

    public void setMainParameters(Vector mainParameters) {
        this.mainParameters = mainParameters;
    }

    /**
     * appends the mainParameter to the curently existing list of mainParameters
     * @param mainParameter
     */
    public void addMainParameter(String mainParameter) {
        this.mainParameters.add(mainParameter);
    }

    /*
        public void setNodes(Node [] nodes) {
            this.nodes = nodes;
        }
    */
    public void setRessourceNb(int ressourceNb) {
        this.ressourceNb = ressourceNb;
    }

    /*
        public void setConstructorParameters(Object[] constructorParameters) {
            this.constructorParameters = constructorParameters;
        }
    */
    public void setEstimatedTime(int estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public String getClassName() {
        return this.className;
    }

    public int getPriority() {
        return this.priority;
    }

    public int getUserId() {
        return this.userId;
    }

    public Date getSubmitDate() {
        return this.submitDate;
    }

    public String[] getMainParameters() {
        String[] mainParameters = new String[this.mainParameters.size()];

        for (int i = 0; i < mainParameters.length; ++i) {
            mainParameters[i] = (String) this.mainParameters.elementAt(i);
        }

        return mainParameters;
    }

    /*
        public Node [] getNodes() {
            return this.nodes;
        }
    */
    public int getRessourceNb() {
        return this.ressourceNb;
    }

    /*
        public Object[] getConstructorParameters() {
            return this.constructorParameters;
        }
    */
    public int getEstimatedTime() {
        return this.estimatedTime;
    }

    public String getJobID() {
        return this.jobId.toString();
    }

    public void setXMLDescriptorName(String XMLDescriptorName) {
        this.xmlName = XMLDescriptorName;
    }

    public String getXMLDescriptorName() {
        return this.xmlName;
    }

    public void setXMLFullPath(String XMLFullPath) {
        this.xmlFullPath = XMLFullPath;
    }

    public String getXMLFullPath() {
        return this.xmlFullPath;
    }

    public void setJVMParameters(String jvmParameters) {
        this.jvmParameters = jvmParameters;
    }

    public String getJVMParameters() {
        return this.jvmParameters;
    }

    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }

    public String getJobStatus() {
        return this.jobStatus;
    }

    public String[] getClassPath() {
        return this.classPath;
    }

    public void setClassPath(String[] classPath) {
        this.classPath = classPath;
    }

    public void setMinNbOfNodes(int nbOfNodes) {
        this.minRessourceNb = nbOfNodes;
    }

    public int getMinNbOfNodes() {
        return this.minRessourceNb;
    }
}
