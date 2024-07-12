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
package org.ow2.proactive.scheduler.policy.limit;

import static jdbm.RecordManagerFactory.createRecordManager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;

import jdbm.PrimaryHashMap;
import jdbm.RecordManager;


public class NodeUsageLimitSynchronization {

    private static final Logger logger = Logger.getLogger(NodeUsageLimitSynchronization.class);

    /**
     * JDBM database
     */
    private static File storeFile = null;

    private static File storeFileDirectory = null;

    private static final String STORE_FILE_NAME = "NODE_USAGE_SCHEDULING_POLICY_STORE";

    /**
     * JDBM database tables
     */
    private static final String JOBS_TOKENS = "jobsMaxTokens";

    private static final String ANCESTOR_JOBS = "ancestorJobs";

    private static final String TASKS_TOKENS = "tasksTokenUsage";

    private static final String JOBS_TOKENS_USAGE = "jobsTokensUsage";

    /**
     * JDBM maps
     */
    // top-level jobs handled, and for each job the maximum number of Nodes configured
    // < <jobid1, 2> <jobid2, 3> <jobid3, 1>> ... >
    private static PrimaryHashMap<String, Integer> persistedJobsMaxTokens = null;

    // For each top-level job handled, the number of Nodes currently used
    private static PrimaryHashMap<String, Integer> persistedJobsTokensUsage = null;

    // for each job, determine the ancestor job
    // < <jobid2, jobid1> <jobid3, jobid1>>>
    private static PrimaryHashMap<String, String> childrenJobs = null;

    // number of nodes used by each running task
    // < <taskid0, 1> <taskid1, 2> ... >
    private static PrimaryHashMap<String, Integer> persistedTasksTokens = null;

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private ReentrantReadWriteLock.ReadLock readLock = lock.readLock();

    private ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    private Set<String> pendingTopLevelJobToRemove = new HashSet<>();

    private static RecordManager recordManager = null;

    NodeUsageLimitSynchronization() {
        initDBFile();
        initDBMaps(true);
    }

    private void initDBFile() {
        storeFileDirectory = new File(PASchedulerProperties.getAbsolutePath(PASchedulerProperties.NODE_USAGE_SCHEDULING_POLICY_DATABASE.getValueAsString()));
        if (!storeFileDirectory.exists()) {
            boolean result = storeFileDirectory.mkdirs();
            if (!result) {
                throw new IllegalArgumentException("Could not create directory hierarchy for path " +
                                                   storeFileDirectory);
            }
        } else if (!storeFileDirectory.isDirectory()) {
            throw new IllegalArgumentException("Provided directory path exists and is not a directory " +
                                               storeFileDirectory);
        }
        storeFile = new File(storeFileDirectory, STORE_FILE_NAME);
    }

    private void initDBMaps(boolean firstAttempt) {
        close();
        try {
            recordManager = createRecordManager(storeFile.getCanonicalPath());
            persistedJobsMaxTokens = recordManager.hashMap(JOBS_TOKENS);
            childrenJobs = recordManager.hashMap(ANCESTOR_JOBS);
            persistedTasksTokens = recordManager.hashMap(TASKS_TOKENS);
            persistedJobsTokensUsage = recordManager.hashMap(JOBS_TOKENS_USAGE);

            recordManager.commit();
            if (!firstAttempt) {
                logger.info("Loading of job database successful after clean.");
            }

        } catch (Exception e) {
            if (firstAttempt) {
                logger.error("Error occurred when loading the database " + storeFile.getAbsolutePath() +
                             ", now cleaning it and retrying.", e);
                cleanDataBase();
                initDBMaps(false);
            } else {
                close();
                throw new IllegalStateException("Error when loading database (even after cleaning it): " +
                                                storeFile.getAbsolutePath(), e);
            }
        }
    }

    // Add a new job that will be handled, with a maximum number of nodes configured
    void addJob(String jobId, int maxNodes) {
        try {
            writeLock.lock();
            if (!persistedJobsMaxTokens.containsKey(jobId)) {

                persistedJobsMaxTokens.put(jobId, maxNodes);
                persistedJobsTokensUsage.put(jobId, 0);
                persist();

            }
        } finally {
            writeLock.unlock();
        }
    }

    // Possibly add a child job using the couple <childJobId, ancestorJobId>
    // ancestorJob is not necessarily the parent job, it is the top-level job being handled
    void addChildJob(String jobId, String parentJobId) {
        try {
            writeLock.lock();
            if (!childrenJobs.containsKey(jobId)) {
                if (childrenJobs.containsKey(parentJobId)) {
                    // if the parent job is already referenced, use the ancestor stored in its association
                    childrenJobs.put(jobId, childrenJobs.get(parentJobId));
                    persist();
                } else if (persistedJobsMaxTokens.containsKey(parentJobId)) {
                    // if the parent job is not referenced, check if it's a top-level job handled
                    childrenJobs.put(jobId, parentJobId);
                    persist();
                }
                // ignore job if its parent is not handled
            }
        } finally {
            writeLock.unlock();
        }
    }

    void addRunningTask(String jobId, String taskId, int nbNodes) {
        String ancestorJobId = getAncestorJob(jobId);
        try {
            writeLock.lock();
            if (persistedJobsMaxTokens.containsKey(ancestorJobId)) {
                logger.trace("New task running " + taskId + " with " + nbNodes + " nodes");
                // we only record the task if it's being handled
                persistedTasksTokens.put(taskId, nbNodes);
                persist();
                updateAncestorJobTokenUsage(jobId);
            }
        } finally {
            writeLock.unlock();
        }
    }

    private void updateAncestorJobTokenUsage(String jobId) {
        String ancestorJobId = getAncestorJob(jobId);
        try {
            writeLock.lock();
            if (persistedJobsMaxTokens.containsKey(ancestorJobId)) {
                int ancestorJobTokenCount = countJobTokenUsage(ancestorJobId);
                logger.trace("New usage count for job " + ancestorJobId + ":" + ancestorJobTokenCount);
                persistedJobsTokensUsage.put(ancestorJobId, ancestorJobTokenCount);
                persist();
            }
        } finally {
            writeLock.unlock();
        }
    }

    Set<String> findJobTree(String ancestorJobId) {
        Set<String> allJobs = new HashSet<>();
        allJobs.add((ancestorJobId));
        allJobs.addAll(childrenJobs.entrySet()
                                   .stream()
                                   .filter(e -> ancestorJobId.equals(e.getValue()))
                                   .map(e -> e.getKey())
                                   .collect(Collectors.toSet()));
        return allJobs;
    }

    Set<String> findAllJobsHandled() {
        try {
            readLock.lock();
            Set<String> allJobs = new HashSet<>();
            for (String jobId : persistedJobsMaxTokens.keySet()) {
                allJobs.addAll(findJobTree(jobId));
            }
            return allJobs;
        } finally {
            readLock.unlock();
        }
    }

    Set<String> findAllTasksHandled() {
        try {
            readLock.lock();
            return new HashSet<>(persistedTasksTokens.keySet());
        } finally {
            readLock.unlock();
        }
    }

    int countJobTokenUsage(String jobId) {
        int total = 0;
        for (String subJobId : findJobTree(jobId)) {
            total += persistedTasksTokens.entrySet()
                                         .stream()
                                         .filter(e -> e.getKey().startsWith(subJobId + "t"))
                                         .map(e -> e.getValue())
                                         .reduce(Integer::sum)
                                         .orElse(0);
        }
        return total;
    }

    String getAncestorJob(String jobId) {
        String answer = childrenJobs.get(jobId);
        return answer != null ? answer : jobId;
    }

    synchronized void taskNotRunning(String jobId, String taskId) {
        logger.trace("taskNotRunning : " + jobId + " " + taskId);
        try {
            writeLock.lock();
            persistedTasksTokens.remove(taskId);
            updateAncestorJobTokenUsage(jobId);
        } finally {
            writeLock.unlock();
        }
    }

    synchronized void jobTerminated(String jobId) {
        logger.trace("Job terminated : " + jobId);
        try {
            writeLock.lock();
            if (persistedJobsMaxTokens.containsKey(jobId)) {
                Set<String> allJobs = findJobTree(jobId);
                if (allJobs.size() == 1) {
                    persistedJobsMaxTokens.remove(jobId);
                    persistedJobsTokensUsage.remove(jobId);
                } else {
                    logger.trace("Job has children still running : " + allJobs);
                    // if there are children jobs still running we delay the removal
                    pendingTopLevelJobToRemove.add(jobId);
                }
            } else {
                String ancestorJob = getAncestorJob(jobId);
                childrenJobs.remove(jobId);
                Set<String> tasksMatchingJobId = persistedTasksTokens.keySet()
                                                                     .stream()
                                                                     .filter(e -> e.startsWith(jobId + "t"))
                                                                     .collect(Collectors.toSet());
                for (String taskId : tasksMatchingJobId) {
                    persistedTasksTokens.remove(taskId);
                }
                if (pendingTopLevelJobToRemove.contains(ancestorJob)) {
                    Set<String> allJobs = findJobTree(ancestorJob);
                    if (allJobs.size() == 1) {
                        logger.trace("Ancestor job has no more children running " + ancestorJob);
                        // if there are no more children jobs still running we execute the removal of the ancestor
                        persistedJobsMaxTokens.remove(ancestorJob);
                        persistedJobsTokensUsage.remove(ancestorJob);
                        pendingTopLevelJobToRemove.remove(ancestorJob);
                    } else {
                        logger.trace("Ancestor job has children still running : " + allJobs);
                    }
                }
            }
            updateAncestorJobTokenUsage(jobId);
        } finally {
            writeLock.unlock();
        }
    }

    public NodeUsageTokens getNodeUsageTokens() {
        NodeUsageTokens answer = new NodeUsageTokens();
        try {
            readLock.lock();
            for (String jobId : persistedJobsMaxTokens.keySet()) {
                answer.setTokens(jobId, persistedJobsMaxTokens.get(jobId) - persistedJobsTokensUsage.get(jobId));
            }
            for (Map.Entry<String, String> entry : childrenJobs.entrySet()) {
                answer.addChildJob(entry.getKey(), entry.getValue());
            }
        } finally {
            readLock.unlock();
        }
        return answer;
    }

    void persist() {
        try {
            recordManager.commit();
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public void close() {
        if (recordManager != null) {
            try {
                logger.info("Closing Record Manager");
                recordManager.close();
                recordManager = null;
            } catch (IOException e) {
                logger.warn("Error when closing Record Manager", e);
            }
        }
    }

    private void cleanDataBase() {
        close();
        logger.info("Cleaning database");

        // delete all db files
        File[] dbChannelFiles = storeFileDirectory.listFiles((dir, name) -> name.startsWith(STORE_FILE_NAME));
        if (dbChannelFiles != null) {
            for (File file : dbChannelFiles) {
                try {
                    logger.info("Deleting " + file);
                    boolean result = file.delete();
                    if (!result) {
                        logger.error("Could not delete file " + file +
                                     ". Synchronization service might not work properly. Please remove the file manually.");
                    }
                } catch (Exception e) {
                    logger.info("Error while deleting file during database cleanup", e);
                }
            }
        }
    }

    // A structure used to filter eligible tasks according to node token current capacity
    public static class NodeUsageTokens {

        private Map<String, Integer> tokens = new HashMap<>();

        private Map<String, String> childJobs = new HashMap<>();

        private NodeUsageTokens() {

        }

        private void addChildJob(String jobId, String parentJobId) {
            if (childJobs.containsKey(parentJobId)) {
                childJobs.put(jobId, childJobs.get(parentJobId));
            } else if (tokens.containsKey(parentJobId)) {
                childJobs.put(jobId, parentJobId);
            }
        }

        private void setTokens(String jobId, int nbTokens) {
            tokens.put(jobId, nbTokens);
        }

        public boolean acquireTokens(String jobId, int nbTokens) {
            String ancestorJobId = childJobs.containsKey(jobId) ? childJobs.get(jobId) : jobId;
            if (tokens.containsKey(ancestorJobId)) {
                int availableTokens = tokens.get(ancestorJobId);
                if (nbTokens <= availableTokens) {
                    tokens.put(ancestorJobId, availableTokens - nbTokens);
                    return true;
                }
                return false;
            } else {
                return true;
            }
        }

        public String toString() {
            return tokens.toString();
        }

    }

}
