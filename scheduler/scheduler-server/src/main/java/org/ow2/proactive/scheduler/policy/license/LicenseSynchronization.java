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
package org.ow2.proactive.scheduler.policy.license;

import static jdbm.RecordManagerFactory.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;

import jdbm.PrimaryHashMap;
import jdbm.RecordManager;


public class LicenseSynchronization {

    private static final Logger logger = Logger.getLogger(LicenseSynchronization.class);

    /**
     * JDBM database
     */
    private static File storeFile = null;

    private static File storeFileDirectory = null;

    private static final String STORE_FILE_NAME = "LICENSE_SCHEDULING_POLICY_STORE";

    /**
     * JDBM database tables
     */
    private static final String JOBS_LICENSES = "jobsLicenses";

    private static final String TASKS_LICENSES = "tasksLicenses";

    private static final String REMAINING_TOKENS = "remainingTokens";

    /**
     * JDBM maps
     */
    private static PrimaryHashMap<String, LinkedBlockingQueue<String>> persistedJobsLicenses = null;

    private static PrimaryHashMap<String, LinkedBlockingQueue<String>> persistedTasksLicenses = null;

    private static PrimaryHashMap<String, Integer> remainingTokens = null;

    private static RecordManager recordManager = null;

    LicenseSynchronization() {
        initDBFile();
        initDBMaps(true);
    }

    private void initDBFile() {
        storeFileDirectory = new File(PASchedulerProperties.getAbsolutePath(PASchedulerProperties.LICENSE_SCHEDULING_POLICY_DATABASE.getValueAsString()));
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
            persistedJobsLicenses = recordManager.hashMap(JOBS_LICENSES);
            persistedTasksLicenses = recordManager.hashMap(TASKS_LICENSES);
            remainingTokens = recordManager.hashMap(REMAINING_TOKENS);

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

    void addSoftware(String software, int nbTokens) {
        persistedJobsLicenses.put(software, new LinkedBlockingQueue<>(nbTokens));
        persistedTasksLicenses.put(software, new LinkedBlockingQueue<>(nbTokens));
        remainingTokens.put(software, nbTokens);
    }

    void persist() {
        try {
            recordManager.commit();
        } catch (IOException e) {
            logger.error(e);
        }
    }

    LinkedBlockingQueue<String> getPersistedJobsLicense(String license) {
        return persistedJobsLicenses.get(license);
    }

    boolean containsJobId(String license, String jobId) {
        return persistedJobsLicenses.get(license).stream().anyMatch(jid -> jid.equals(jobId));
    }

    void addJobToLicense(String license, String jobId) {
        persistedJobsLicenses.get(license).add(jobId);
        remainingTokens.put(license, remainingTokens.get(license) - 1);
    }

    void markJobLicenseChange(String license) {
        // Record Manager mark as dirty (uncommited) entries which have be modified via a put call
        // Thus, such operation as persistedJobsLicenses.get(license).dosomething() will not be committed
        // by the following trick, we mark the entry as dirty and commit
        persistedJobsLicenses.put(license, persistedJobsLicenses.get(license));
    }

    LinkedBlockingQueue<String> getPersistedTasksLicense(String license) {
        return persistedTasksLicenses.get(license);
    }

    void addTaskToLicense(String license, String taskId) {
        persistedTasksLicenses.get(license).add(taskId);
        remainingTokens.put(license, remainingTokens.get(license) - 1);
    }

    void markTaskLicenseChange(String license) {
        // Record Manager mark as dirty (uncommited) entries which have be modified via a put call
        // Thus, such operation as persistedTasksLicenses.get(license).dosomething() will not be committed
        // by the following trick, we mark the entry as dirty and commit
        persistedTasksLicenses.put(license, persistedTasksLicenses.get(license));
    }

    int getRemainingTokens(String license) {
        if (!remainingTokens.containsKey(license)) {
            logger.error("License " + license + " does not exist in the remainingTokens map, whereas it should be.");
        }
        return remainingTokens.get(license);
    }

    boolean containsLicense(String license) {
        return remainingTokens.containsKey(license);
    }

    void setNbTokens(String license, int currentNbTokens) {
        remainingTokens.put(license, currentNbTokens);
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

}
