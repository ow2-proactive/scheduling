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
package org.ow2.proactive.scheduler.task;

import static com.google.common.io.Files.*;
import static org.apache.commons.io.FileUtils.deleteQuietly;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.task.utils.ForkerUtils;


/**
 * ProgressFileReader is in charge of:
 * - creating a progress file in the specified working directory
 * - detecting changes on a progress file efficiently
 * - reading new value and saving it in memory
 * - exposing the last read value
 * <p>
 * Instances of this class are NOT thread-safe.
 *
 * @author The ProActive Team
 */
public class ProgressFileReaderPoller {

    private static final Long DEFAULT_POLLER_PERIOD = 1000L;

    private static final Logger logger = Logger.getLogger(ProgressFileReaderPoller.class);

    private static final String PROGRESS_FILE_DIR = ".tasks-progress";

    private Path progressFileDir;

    private Path progressFile;

    private volatile int progress;

    private Set<Listener> observers;

    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public ProgressFileReaderPoller() {
        // mainly for test purposes
        observers = new HashSet<>(0);
    }

    public boolean start(File workingDir, TaskId taskId) {
        String progressFileName = "job-" + taskId.getJobId().value() + "-task-" + taskId.value() + "-" +
                                  UUID.randomUUID() + ".progress";

        return start(workingDir, progressFileName);
    }

    boolean start(File workingDir, String filename) {
        try {
            createProgressFileIfNeeded(workingDir, filename);

            String propertyValue = System.getProperty(TaskLauncher.PROGRESS_FILE_READER_PERIOD,
                                                      DEFAULT_POLLER_PERIOD.toString());
            Long periodInMs;
            try {
                periodInMs = Long.parseLong(propertyValue);
            } catch (NumberFormatException e) {
                logger.warn(String.format("Incorect property %s value: %s, using default value: %s.",
                                          TaskLauncher.PROGRESS_FILE_READER_PERIOD,
                                          propertyValue,
                                          DEFAULT_POLLER_PERIOD.toString()));
                periodInMs = DEFAULT_POLLER_PERIOD;
            }

            scheduledExecutorService.scheduleAtFixedRate(this::readValue, 0, periodInMs, TimeUnit.MILLISECONDS);

            progress = 0;

            return true;
        } catch (IOException e) {
            logger.warn("Error while creating progress file. Progress will not be reported.", e);
            return false;
        }
    }

    private void createProgressFileIfNeeded(File workingDir, String progressFileName) throws IOException {
        progressFileDir = workingDir.toPath().resolve(PROGRESS_FILE_DIR);

        Files.createDirectories(progressFileDir);
        ForkerUtils.setSharedExecutablePermissions(progressFileDir.toFile());
        progressFile = progressFileDir.resolve(progressFileName);
        if (!Files.exists(progressFile)) {
            Files.createFile(progressFile);
        }
        ForkerUtils.setSharedPermissions(progressFile.toFile());

        logger.debug("Progress file '" + progressFile + "' created");
    }

    public int getProgress() {
        return progress;
    }

    Path getProgressFile() {
        return progressFile;
    }

    public void register(Listener listener) {
        this.observers.add(listener);
    }

    void unregister(Listener listener) {
        this.observers.remove(listener);
    }

    public void stop() {
        logger.trace(String.format("Stopping %s...", ProgressFileReaderPoller.class.getSimpleName()));
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdownNow();
        }
        removeProgressFileDir();
    }

    private void removeProgressFileDir() {
        if (progressFileDir != null && Files.exists(progressFileDir)) {
            deleteQuietly(progressFileDir.toFile());
        }
    }

    public interface Listener {

        void onProgressUpdate(int newValue);

    }

    public void readValue() {
        try {
            String line = readFirstLine(progressFile.toFile(), Charset.defaultCharset());

            if (line != null) {
                try {
                    // try to parse double to allow int + double
                    int value = (int) Double.parseDouble(line);

                    if (value >= 0 && value <= 100) {
                        progress = value;

                        for (Listener observer : observers) {
                            observer.onProgressUpdate(progress);
                        }

                        if (logger.isDebugEnabled()) {
                            logger.debug("New progress value read: " + value);
                        }
                    } else {
                        logger.warn("Invalid progress value: " + value);
                    }
                } catch (NumberFormatException e) {
                    logger.warn("Progress value is a not a numeric value: " + line);
                }
            }
        } catch (IOException e) {
            logger.warn("Error while reading the first line of " + progressFile);
        }
    }

}
