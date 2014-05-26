package org.ow2.proactive.scheduler.core;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.remoteobject.RemoteObjectAdapter;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.util.JobLogger;
import org.ow2.proactive.scheduler.util.classloading.TaskClassServer;


public class SchedulerClassServers {

    private static final Logger logger = Logger.getLogger(SchedulingService.class);

    private static final JobLogger jlogger = JobLogger.getInstance();

    private final ConcurrentHashMap<JobId, TaskClassServer> classServers;

    private final ConcurrentHashMap<JobId, RemoteObjectExposer<TaskClassServer>> remoteClassServers;

    SchedulerClassServers() {
        this.classServers = new ConcurrentHashMap<JobId, TaskClassServer>();
        this.remoteClassServers = new ConcurrentHashMap<JobId, RemoteObjectExposer<TaskClassServer>>();
    }

    /**
     * Return the task classserver for the job jid.<br>
     * return null if the classServer is undefine for the given jobId.
     *
     * @param jid the job id
     * @return the task classserver for the job jid
     */
    TaskClassServer getTaskClassServer(JobId jid) {
        return this.classServers.get(jid);
    }

    /**
     * Create a taskclassserver for this job if a jobclasspath is set
     */
    void createTaskClassServer(InternalJob job, SchedulerSpacesSupport spacesSupport) {
        String[] classpath = job.getEnvironment().getJobClasspath();

        if (classpath == null || classpath.length == 0) {
            return; // Do nothing if no job classpath
        }

        JobId jid = job.getId();
        if (getTaskClassServer(jid) != null) {
            throw new IllegalStateException("job " + jid + " classServer already exists");
        }

        try {
            jlogger.info(jid, "creating the remote task classServer");
            TaskClassServer localReference = new TaskClassServer(jid, spacesSupport.getGlobalSpace(),
                spacesSupport.getUserSpace(job.getOwner()));
            RemoteObjectExposer<TaskClassServer> remoteExposer = new RemoteObjectExposer<TaskClassServer>(
                TaskClassServer.class.getName(), localReference);
            URI uri = RemoteObjectHelper.generateUrl(jid.toString());
            RemoteRemoteObject rro = remoteExposer.createRemoteObject(uri);
            // must activate through local ref to avoid copy of the classpath content !
            jlogger.info(jid, "activating local reference");
            localReference.activate(job.getEnvironment(), job.getGlobalSpace(), job.getUserSpace());
            // store references
            classServers.put(jid, (TaskClassServer) new RemoteObjectAdapter(rro).getObjectProxy());
            remoteClassServers.put(jid, remoteExposer);// stored to be unregistered later
        } catch (Exception e) {
            logger.error("", e);
            throw new IllegalStateException("Unable to create class server for job " + jid, e);
        }
    }

    /**
     * Remove the taskClassServer for the job jid. Delete the classpath
     * associated in SchedulerCore.tmpJarFilesDir.
     *
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
                jlogger.error(jid, "unable to unregister remote taskClassServer", e);
                logger.error("", e);
            }
        }
        return (tcs != null);
    }
}
