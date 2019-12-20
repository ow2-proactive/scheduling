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
package org.ow2.proactive.scheduler.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.ow2.proactive.core.properties.PASharedProperties;
import org.ow2.proactive.scheduler.common.util.ZipUtils;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


public class SchedulerBackupRunner implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(SchedulerBackupRunner.class);

    private static final String PREFIX = "ProActiveSchedulerBackup_";

    private final List<String> targets;

    private final String destination;

    private final int windowSize;

    private final SchedulingService scheduler;

    public SchedulerBackupRunner(SchedulingService scheduler) {
        this.scheduler = scheduler;
        targets = Arrays.asList(PASharedProperties.SERVER_BACKUP_TARGETS.getValueAsString().split(","));
        destination = PASharedProperties.SERVER_BACKUP_DESTINATION.getValueAsString();
        windowSize = PASharedProperties.SERVER_BACKUP_WINDOWS.getValueAsInt();
    }

    @Override
    public void run() {
        try {
            scheduler.freeze();

            while (!noOtherTaskIsRunning()) {
                Thread.sleep(10000);
            }
            removeOldBackups();
            performBackup();
        } catch (IOException | InterruptedException e) {
            LOGGER.error("", e);
        } finally {
            scheduler.resume();
        }
    }

    private boolean noOtherTaskIsRunning() {
        return scheduler.getJobs().getRunningTasks().isEmpty();
    }

    private void performBackup() throws IOException {
        String backupFileName = PREFIX + DateTime.now().toString("yyyy-MM-dd'T'HH-mm") + ".zip";
        File destinationFolder = new File(destination);

        if (!destinationFolder.isAbsolute()) {
            destinationFolder = new File(PASchedulerProperties.SCHEDULER_HOME.getValueAsString(), destination);
        }

        if (!destinationFolder.exists()) {
            destinationFolder.mkdirs();
        }

        if (destinationFolder.exists() && destinationFolder.isDirectory() && destinationFolder.canWrite()) {
            File backupFile = new File(destinationFolder, backupFileName);
            LOGGER.info("Performing backup to " + backupFile);
            String[] foldersToZip = targets.stream()
                                           .map(target -> (new File(PASchedulerProperties.SCHEDULER_HOME.getValueAsString(),
                                                                    target)).getAbsolutePath())
                                           .toArray(String[]::new);
            ZipUtils.zip(foldersToZip, backupFile, null);
        } else {
            LOGGER.error("Cannot save a backup because backup destination folder does not exist: " + destination);
        }
    }

    private void removeOldBackups() {
        File fodlerWhereBackups = new File(destination);
        File[] files = fodlerWhereBackups.listFiles();
        if (files != null) {
            Stream.of(files)
                  .filter(File::isFile)
                  .filter(file -> file.getName().startsWith(PREFIX))
                  .sorted(Comparator.comparingLong(file -> getCreationTime((File) file)).reversed())
                  .skip(windowSize - 1)
                  .forEach(file -> {
                      LOGGER.info("Removing old backup: " + file.getName());
                      file.delete();
                  });
        }
    }

    private long getCreationTime(File file) {
        try {
            BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            FileTime fileTime = attr.creationTime();
            return fileTime.toMillis();
        } catch (IOException e) {
            LOGGER.error("", e);
            // handle exception
            return 0l;
        }
    }
}
