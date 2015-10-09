/**
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
package org.ow2.proactive.scheduler.smartproxy.common;

import jdbm.PrimaryHashMap;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Map;
import java.util.Set;

/**
 * Database in charge to persist references to awaited jobs in order to prevent data loss.
 *
 * @author The ProActive Team
 */
public class JobDatabase {

    private static final Logger log = Logger.getLogger(JobDatabase.class);

    protected static final String TMPDIR = System.getProperty("java.io.tmpdir");

    /**
     * Default name of the file used to persist the list of jobs
     */
    protected static final String DEFAULT_STATUS_FILENAME = "SmartProxy";

    protected static String sessionName = DEFAULT_STATUS_FILENAME;

    /**
     * File which persists the list of {@link AwaitedJob}
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
     * a string. When the output data related to this job has been transferred,
     * the corresponding awaited job will be removed from this map. This map is
     * persisted in the status file
     */
    protected PrimaryHashMap<String, AwaitedJob> awaitedJobs;

    public void cleanDataBase() {
        if (recMan != null) {
            throw new IllegalStateException("Connection to a DB is established, cannot clean it");
        }

        log.info("Cleaning database");

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
        for (File file : dbJobFiles) {
            try {
                log.info("Deleting " + file);
                file.delete();
            } catch (Exception e) {
                log.info("Error while deleting file during database cleanup", e);
            }
        }
    }

    public void commit() throws IOException {
        recMan.commit();
    }

    /**
     * This call "reset" the current proxy by removing all knowledge of awaited jobs
     */
    public void discardAllJobs() {
        awaitedJobs.clear();
        log.info("Proxy's database has been reseted.");
        try {
            recMan.commit();
        } catch (IOException e) {
            log.error("Exception occured while closing connection to status file:", e);
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
                log.error("Exception occured while closing connection to status file:", e);
            }
        } else {
            log.warn("Job " + jobID + " is not handled by the proxy.");
        }
    }

    public Set<String> getAwaitedJobsIds() {
        return awaitedJobs.keySet();
    }

    public AwaitedJob getAwaitedJob(String id) {
        return awaitedJobs.get(id);
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
            for (Map.Entry<String, AwaitedJob> job : awaitedJobs.entrySet());
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

    public void putAwaitedJob(String id, AwaitedJob awaitedJob) {
        if (!awaitedJob.getJobId().equals(id)) {
            throw new IllegalArgumentException(
                    "given id " + id + " is different from job id : " + awaitedJob.getJobId());
        }

        this.awaitedJobs.put(id, awaitedJob);

        try {
            this.recMan.commit();
        } catch (IOException e) {
            log.error(
                    "Could not save status file after adding job on awaited jobs list "
                            + awaitedJob.getJobId(), e);
        }
    }

    public AwaitedJob removeAwaitedJob(String id) {
        return this.awaitedJobs.remove(id);
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
     * Sets the given task to transferring status. This is to avoid duplicate transfers in case of duplicate events
     *
     * @param id    jobID
     * @param taskName task name
     * @param transferring
     */
    public void setTaskTransferring(String id, String taskName, boolean transferring) {
        AwaitedJob aj = awaitedJobs.get(id);
        if (aj == null) {
            log.warn("Job " + id + " not in the awaited list");
            return;
        }

        AwaitedTask at = aj.getAwaitedTask(taskName);

        if (at == null) {
            log.warn("Task " + taskName + " from Job " + id + " not in the awaited list");
            return;
        }

        at.setTransferring(transferring);
        awaitedJobs.put(id, aj);

        try {
            this.recMan.commit();
        } catch (IOException e) {
            log.error("Could not save status file after setting transferring mode to task Task " + taskName +
                    " from Job" + id, e);
        }
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

}
