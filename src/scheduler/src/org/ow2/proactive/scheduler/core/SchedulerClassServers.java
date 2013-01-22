package org.ow2.proactive.scheduler.core;

import java.net.URI;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.remoteobject.RemoteObjectAdapter;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;
import org.ow2.proactive.scheduler.common.exception.ClassServerException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.core.db.JobClasspathContent;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.util.JobLogger;
import org.ow2.proactive.scheduler.util.classloading.TaskClassServer;


final class SchedulerClassServers {

    private static final Logger logger = Logger.getLogger(SchedulerCore.class);

    private static final JobLogger jlogger = JobLogger.getInstance();

    private final Hashtable<JobId, TaskClassServer> classServers;

    private final Hashtable<JobId, RemoteObjectExposer<TaskClassServer>> remoteClassServers;

    private final SchedulerDBManager dbManager;

    SchedulerClassServers(SchedulerDBManager dbManager) {
        this.classServers = new Hashtable<JobId, TaskClassServer>();
        this.remoteClassServers = new Hashtable<JobId, RemoteObjectExposer<TaskClassServer>>();
        this.dbManager = dbManager;
    }

    /**
     * Return the task classserver for the job jid.<br>
     * return null if the classServer is undefine for the given jobId.
     * 
     * @param jid the job id 
     * @return the task classserver for the job jid
     */
    TaskClassServer getTaskClassServer(JobId jid) {
        return classServers.get(jid);
    }

    /**
     * Create a new taskClassServer for the job jid
     * @param jid the job id
     * @param userClasspathJarFile the contents of the classpath as a serialized jar file
     * @param deflateJar if true, the jar file is deflated in the tmpJarFilesDir
     * @throws ClassServerException if something goes wrong during task class server creation
     */
    private void addTaskClassServer(JobId jid, byte[] userClasspathJarFile, boolean deflateJar)
            throws ClassServerException {
        if (getTaskClassServer(jid) != null) {
            throw new ClassServerException("job " + jid + " classServer already exists");
        }
        try {
            // create remote task classserver 
            jlogger.info(jid, "creating the remote task classServer");
            TaskClassServer localReference = new TaskClassServer(jid);
            RemoteObjectExposer<TaskClassServer> remoteExposer = new RemoteObjectExposer<TaskClassServer>(
                TaskClassServer.class.getName(), localReference);
            URI uri = RemoteObjectHelper.generateUrl(jid.toString());
            RemoteRemoteObject rro = remoteExposer.createRemoteObject(uri);
            // must activate through local ref to avoid copy of the classpath content !
            jlogger.info(jid, "activating local reference");
            localReference.activate(userClasspathJarFile, deflateJar);
            // store references
            classServers.put(jid, (TaskClassServer) new RemoteObjectAdapter(rro).getObjectProxy());
            remoteClassServers.put(jid, remoteExposer);// stored to be unregistered later
        } catch (Exception e) {
            logger.error("", e);
            throw new ClassServerException("Unable to create class server for job " + jid + " because " +
                e.getMessage());
        }
    }

    /**
     * Create a taskclassserver for this job if a jobclasspath is set
     */
    void createTaskClassServer(InternalJob job) {
        // restart classserver if needed
        try {
            String[] classpath = job.getEnvironment().getJobClasspath();
            if (classpath != null && classpath.length > 0) {
                JobClasspathContent cp = dbManager.loadJobClasspathContent(job.getEnvironment()
                        .getJobClasspathCRC());
                if (cp == null) {
                    throw new ClassServerException("No classpath content is available for job " +
                        job.getJobInfo().getJobId());
                }
                addTaskClassServer(job.getId(), cp.getClasspathContent(), cp.isContainsJarFiles());
            }
        } catch (ClassServerException e) {
            throw new IllegalStateException("Cannot create TaskClassServer for job " +
                job.getJobInfo().getJobId(), e);
        }
    }

    /**
     * Remove the taskClassServer for the job jid.
     * Delete the classpath associated in SchedulerCore.tmpJarFilesDir.
     * @return true if a taskClassServer has been removed, false otherwise.
     */
    boolean removeTaskClassServer(JobId jid) {
        jlogger.info(jid, "removing TaskClassServer");
        // desactivate tcs
        TaskClassServer tcs = classServers.remove(jid);
        if (tcs != null) {
            tcs.desactivate();
        }
        // unexport remote object
        RemoteObjectExposer<TaskClassServer> roe = remoteClassServers.remove(jid);
        if (roe != null) {
            try {
                jlogger.info(jid, "unregistering remote TaskClassServer");
                roe.unregisterAll();
            } catch (ProActiveException e) {
                jlogger.error(jid, "unable to unregister remote taskClassServer because : " + e.getMessage());
                logger.error("", e);
            }
        }
        return (tcs != null);
    }

}
