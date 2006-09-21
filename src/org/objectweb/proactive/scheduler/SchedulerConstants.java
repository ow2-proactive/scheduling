package org.objectweb.proactive.scheduler;

public interface SchedulerConstants {

    /**
    * <code>SCHEDULER_NODE_NAME</code>: name of the scheduler node where the service is deployed.
    */
    public static final String SCHEDULER_NODE_NAME = "SchedulerNode";

    // -------------------------------------------------------------------------
    // Java system properties names
    // -------------------------------------------------------------------------

    /** policy class name of the scheduler */
    public static final String POLICY_NAME = "proactive.scheduler.policy";

    /** scheduler url (protocol://host:port) */
    public static final String SCHEDULER_URL = "proactive.scheduler.url";

    /** jvm parameters */
    public static final String JVM_PARAMETERS = "proactive.scheduler.jvmParameters";

    //    /** xml path where the xml files are being deployed */
    //    public static final String XML_PATH = "proactive.scheduler.xmlPath";

    /** the job ID of the job to be run locally */
    public static final String JOB_ID = "proactive.scheduler.genericJob.jobId";

    /** the complete path of the XML Deployement Descriptor */
    public static final String XML_PATH = "XMLDescriptorFile";
}
