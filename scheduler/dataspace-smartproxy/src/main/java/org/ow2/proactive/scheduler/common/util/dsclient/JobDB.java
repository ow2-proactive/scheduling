/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.util.dsclient;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOError;
import java.io.IOException;
import java.io.InvalidClassException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jdbm.PrimaryHashMap;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.Selectors;
import org.apache.log4j.Logger;
import org.objectweb.proactive.extensions.dataspaces.vfs.VFSFactory;
import org.ow2.proactive.scheduler.common.SchedulerConstants;


/**
 * JobDB
 *
 * @author The ProActive Team
 */
public class JobDB {

    public static final Logger logger = Logger.getLogger(JobDB.class);

    protected static final String TMPDIR = System.getProperty("java.io.tmpdir");

    /**
     * Default name of the file used to persist the list of jobs
     */
    protected static final String DEFAULT_STATUS_FILENAME = "SmartProxy";

    protected static String sessionName = DEFAULT_STATUS_FILENAME;

    /**
     * file which persists the list of {@link AwaitedJob}
     */
    public static File statusFile = new File(TMPDIR, sessionName);

    /**
     * Name of the jobs backup table being recorded
     */
    public static final String STATUS_RECORD_NAME = "AWAITED_JOBS";

    /**
     * Object handling the AwaitedJobsFile connection
     */
    protected RecordManager recMan;

    /**
     * A map of jobs that have been launched and which results are awaited each
     * time a new job is sent to the scheduler for computation, it will be added
     * to this map, as an entry of (JobId, AwaitedJob), where JobId is given as
     * a string. When the output data related to this job has been transfered,
     * the corresponding awaited job will be removed from this map. This map is
     * persisted in the status file
     */
    protected PrimaryHashMap<String, AwaitedJob> awaitedJobs;

    // TODO: is the FileSystemManager threadSafe ? Do we need to create one
    // instance per thread ?
    // See https://issues.apache.org/jira/browse/VFS-98
    /**
     * The VFS {@link org.apache.commons.vfs2.FileSystemManager} used for file transfer
     */
    transient protected FileSystemManager fsManager = null;

    {
        try {
            fsManager = VFSFactory.createDefaultFileSystemManager();
        } catch (FileSystemException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.error("Could not create Default FileSystem Manager", e);
        }
    }

    public JobDB() {

    }

    public void close() {
        if (recMan != null) {
            try {
                recMan.close();
                recMan = null;
            } catch (IOException e) {

            }
        }
    }

    /**
     * Sets the name of this recording session. The name must be an unique word composed of alphanumerical charecter
     * The file used to persist awaited jobs will be named accordingly. If no session name is provided, a generic default name will be used.
     *
     * @param name alphanumerical word
     */
    public void setSessionName(String name) {
        if (awaitedJobs != null) {
            throw new IllegalStateException(
                "Session already started, try calling setSessionName before calling init");
        }
        if (name != null && !name.matches("\\w+")) {
            throw new IllegalArgumentException("Session Name must be an alphanumerical word.");
        }
        if (name == null) {
            sessionName = DEFAULT_STATUS_FILENAME;
        } else {
            sessionName = name;
        }
        statusFile = new File(TMPDIR, sessionName);
    }

    /**
     * load the awaited jobs from the status file
     * if a InvalidClassException occur, we clean the database
     */
    public void loadJobs() {
        if (recMan != null) {
            try {
                recMan.close();
            } catch (Exception e) {
            }
        }
        try {
            recMan = RecordManagerFactory.createRecordManager(statusFile.getCanonicalPath());
            awaitedJobs = recMan.hashMap(STATUS_RECORD_NAME);
            // This empty loop triggers InvalidClassException in case of serial version uid problems
            for (Map.Entry<String, AwaitedJob> job : awaitedJobs.entrySet()) {

            }
            recMan.commit();
        } catch (IOError e) {
            // we track invalid class exceptions
            if (e.getCause() instanceof InvalidClassException) {
                try {
                    recMan.close();
                } catch (IOException e1) {

                }
                recMan = null;
                cleanDataBase();
                loadJobs();
            } else {
                throw e;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void cleanDataBase() {
        if (recMan != null) {
            throw new IllegalStateException("Connection to a DB is established, cannot clean it");
        }
        // delete all db files
        File[] dbJobFiles = new File(TMPDIR).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.startsWith(sessionName)) {
                    return true;
                }
                return false;
            }
        });
        for (File f : dbJobFiles) {
            try {
                logger.info("Deleting " + f);
                f.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This call "reset" the current proxy by removing all knowledge of awaited jobs
     */
    public void discardAllJobs() {
        awaitedJobs.clear();
        logger.info("Proxy's database has been reseted.");
        try {
            recMan.commit();
        } catch (IOException e) {
            logger.error("Exception occured while closing connection to status file:", e);
        }
    }

    /**
     * Removes the given job of the awaited job list (should rarely be used)
     */
    public void discardJob(String jobID) {
        if (awaitedJobs.containsKey(jobID)) {
            awaitedJobs.remove(jobID);
            try {
                recMan.commit();
            } catch (IOException e) {
                logger.error("Exception occured while closing connection to status file:", e);
            }
        } else {
            logger.warn("Job " + jobID + " is not handled by the proxy.");
        }
    }

    protected Set<String> getAwaitedJobsIds() {
        return awaitedJobs.keySet();
    }

    protected AwaitedJob getAwaitedJob(String id) {
        return awaitedJobs.get(id);
    }

    protected boolean isAwaitedJob(String id) {
        if (awaitedJobs.get(id) != null)
            return true;
        else
            return false;
    }

    protected void putAwaitedJob(String id, AwaitedJob aj) {
        if (!aj.getJobId().equals(id)) {
            throw new IllegalArgumentException("given id " + id + " is different from job id : " +
                aj.getJobId());
        }
        this.awaitedJobs.put(id, aj);
        try {
            this.recMan.commit();
        } catch (IOException e) {
            logger.error("Could not save status file after adding job on awaited jobs list " + aj.getJobId(),
                    e);
        }
    }

    public FileObject resolveFile(String url) throws FileSystemException {
        return fsManager.resolveFile(url);
    }

    /**
     * Removes from the proxy knowledge all info related with the given job.
     * This will also delete every folder created by the job in the shared input and output spaces
     *
     * @param id jobID
     */
    protected void removeAwaitedJob(String id) {

        AwaitedJob aj = awaitedJobs.get(id);
        if (aj == null) {
            logger.warn("Job " + id + " not in the awaited list");
            return;
        }
        logger.debug("Removing knowledge of job " + id);

        String pullUrl = aj.getPullURL();

        String pushUrl = aj.getPushURL();

        FileObject remotePullFolder = null;
        FileObject remotePushFolder = null;

        try {
            remotePullFolder = resolveFile(pullUrl);
            remotePushFolder = resolveFile(pushUrl);
        } catch (Exception e) {
            logger.error("Could not remove data for job " + id, e);
            return;
        }
        if (aj.isIsolateTaskOutputs()) {
            try {
                remotePullFolder = remotePullFolder.getParent();
            } catch (FileSystemException e) {
                logger.error("Could not get the parent of folder " + remotePullFolder, e);
            }
        }

        Set<FileObject> foldersToDelete = new HashSet<FileObject>();
        try {
            foldersToDelete.add(remotePullFolder.getParent());
            if (!remotePullFolder.getParent().equals(remotePushFolder.getParent()))
                foldersToDelete.add(remotePushFolder.getParent());
        } catch (FileSystemException e) {
            logger.warn("Data in folders " + pullUrl + " and " + pushUrl +
                " cannot be deleted due to an unexpected error ", e);
            e.printStackTrace();
        }

        String url = "NOT YET DEFINED";
        for (FileObject fo : foldersToDelete) {
            try {
                url = fo.getURL().toString();

                if (!logger.isTraceEnabled()) {
                    logger.debug("Deleting directory " + url);
                    fo.delete(Selectors.SELECT_ALL);
                    fo.delete();
                }
            } catch (FileSystemException e) {
                logger.warn("Could not delete temporary files at location " + url + " .");
            }
        }

        this.awaitedJobs.remove(id);

        try {
            this.recMan.commit();
        } catch (IOException e) {
            logger.error("Could not save status file after removing job " + id, e);
        }
    }

    /**
     * Sets the given task to transferring status. This is to avoid duplicate transfers in case of duplicate events
     *
     * @param id    jobID
     * @param tname task name
     * @param transferring
     */
    protected void setTaskTransferring(String id, String tname, boolean transferring) {
        AwaitedJob aj = awaitedJobs.get(id);
        if (aj == null) {
            logger.warn("Job " + id + " not in the awaited list");
            return;
        }
        AwaitedTask at = aj.getAwaitedTask(tname);
        if (at == null) {
            logger.warn("Task " + tname + " from Job " + id + " not in the awaited list");
            return;
        }
        at.setTransferring(transferring);
        awaitedJobs.put(id, aj);
        try {
            this.recMan.commit();
        } catch (IOException e) {
            logger.error("Could not save status file after setting transferring mode to task Task " + tname +
                " from Job" + id, e);
        }
    }

    /**
     * Removes from the proxy knowledge all info related with the given task.
     * If all tasks of a job have been removed this way, the job itself will be removed.
     *
     * @param id    jobID
     * @param tname task name
     */
    protected void removeAwaitedTask(String id, String tname) {
        AwaitedJob aj = awaitedJobs.get(id);
        if (aj == null) {
            logger.warn("Job " + id + " not in the awaited list");
            return;
        }
        AwaitedTask at = aj.getAwaitedTask(tname);
        if (at == null) {
            logger.warn("Task " + tname + " from Job " + id + " not in the awaited list");
            return;
        }
        logger.debug("Removing knowledge of task " + tname + " from job " + id);
        if (aj.isIsolateTaskOutputs() && at.getTaskId() != null) {
            // If the output data as been isolated in a dedicated folder we can delete it.

            String pullUrl = aj.getPullURL();
            pullUrl = pullUrl.replace(SchedulerConstants.TASKID_DIR_DEFAULT_NAME,
                    SchedulerConstants.TASKID_DIR_DEFAULT_NAME + "/" + at.getTaskId());

            FileObject remotePullFolder = null;

            try {
                remotePullFolder = resolveFile(pullUrl);
                String url = remotePullFolder.getURL().toString();
                logger.debug("Deleting directory " + remotePullFolder);
                remotePullFolder.delete(Selectors.SELECT_ALL);
                remotePullFolder.delete();
            } catch (Exception e) {
                logger.warn("Could not remove data for task " + tname + " of job " + id, e);
            }

        }

        aj.removeAwaitedTask(tname);
        if (aj.getAwaitedTasks().isEmpty()) {
            removeAwaitedJob(id);
            return;
        } else {
            awaitedJobs.put(id, aj); // this is done to ensure persistence of the operation
        }

        try {
            this.recMan.commit();
        } catch (IOException e) {
            logger
                    .error("Could not save status file after removing task Task " + tname + " from Job" + id,
                            e);
        }

    }

}
