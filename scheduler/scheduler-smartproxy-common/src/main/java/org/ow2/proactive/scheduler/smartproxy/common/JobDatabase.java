/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.smartproxy.common;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import jdbm.PrimaryHashMap;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;


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

    ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();

    ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();

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
        try {
            writeLock.lock();

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
        } finally {
            writeLock.unlock();
        }
    }

    public void commit() throws IOException {
        try {
            writeLock.lock();

            recMan.commit();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * This call "reset" the current proxy by removing all knowledge of awaited jobs
     */
    public void discardAllJobs() {
        try {
            writeLock.lock();
            awaitedJobs.clear();
            log.info("Proxy's database has been reseted.");
            try {
                recMan.commit();
            } catch (IOException e) {
                log.error("Exception occured while closing connection to status file:", e);
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Removes the given job of the awaited job list (should rarely be used)
     */
    public void discardJob(String jobID) {
        try {
            writeLock.lock();
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
        } finally {
            writeLock.unlock();
        }
    }

    public Set<String> getAwaitedJobsIds() {
        try {
            readLock.lock();
            return new LinkedHashSet<>(awaitedJobs.keySet());
        } finally {
            readLock.unlock();
        }
    }

    public AwaitedJob getAwaitedJob(String id) {
        try {
            readLock.lock();
            return awaitedJobs.get(id);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * load the awaited jobs from the status file
     * if a InvalidClassException occur, we clean the database
     */
    public void loadJobs() {
        this.loadJobs(true);
    }

    // The following warning is disabled because in this case we must catch any throwable raised by data file parsing.
    @SuppressWarnings("squid:3AS1181")
    private void loadJobs(boolean firstAttempt) {
        try {
            writeLock.lock();
            closeRecordManager();
            try {
                recMan = RecordManagerFactory.createRecordManager(statusFile.getCanonicalPath());
                awaitedJobs = recMan.hashMap(STATUS_RECORD_NAME);
                // This empty loop triggers InvalidClassException in case of serial version uid problems
                for (Map.Entry<String, AwaitedJob> job : awaitedJobs.entrySet())
                    ;
                recMan.commit();
                if (!firstAttempt) {
                    log.info("Loading of job database successful after clean.");
                }
            } catch (Throwable e) {
                if (firstAttempt) {
                    log.error("Error occurred when loading job database " + statusFile.getAbsolutePath() +
                              ", now cleaning it and retrying.", e);
                    closeRecordManager();
                    recMan = null;
                    cleanDataBase();
                    loadJobs(false);
                } else {
                    closeRecordManager();
                    throw new IllegalStateException("Error when loading database (even after cleaning it): " +
                                                    statusFile.getAbsolutePath(), e);
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    private void closeRecordManager() {
        if (recMan != null) {
            try {
                recMan.close();
            } catch (IOException e1) {
                log.trace("Error when closing record manager", e1);
            }
        }
    }

    public void putAwaitedJob(String id, AwaitedJob awaitedJob) {
        try {
            writeLock.lock();
            if (!awaitedJob.getJobId().equals(id)) {
                throw new IllegalArgumentException("given id " + id + " is different from job id : " +
                                                   awaitedJob.getJobId());
            }

            this.awaitedJobs.put(id, awaitedJob);

            try {
                this.recMan.commit();
            } catch (IOException e) {
                log.error("Could not save status file after adding job on awaited jobs list " + awaitedJob.getJobId(),
                          e);
            }
        } finally {
            writeLock.unlock();
        }
    }

    public AwaitedJob removeAwaitedJob(String id) {
        try {
            writeLock.lock();
            return this.awaitedJobs.remove(id);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Sets the name of this recording session. The name must be an unique word composed of alphanumerical charecter
     * The file used to persist awaited jobs will be named accordingly. If no session name is provided, a generic default name will be used.
     *
     * @param name alphanumerical word
     */
    public void setSessionName(String name) {
        try {
            writeLock.lock();
            if (awaitedJobs != null) {
                throw new IllegalStateException("Session already started, try calling setSessionName before calling init");
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
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Sets the given task to transferring status. This is to avoid duplicate transfers in case of duplicate events
     *
     * @param id    jobID
     * @param taskName task name
     * @param transferring
     */
    public void setTaskTransferring(String id, String taskName, boolean transferring) {
        try {
            writeLock.lock();
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
        } finally {
            writeLock.unlock();
        }
    }

    public void close() {
        try {
            writeLock.lock();
            if (recMan != null) {
                try {
                    recMan.close();
                    recMan = null;
                } catch (IOException e) {

                }
            }
        } finally {
            writeLock.unlock();
        }
    }

}
