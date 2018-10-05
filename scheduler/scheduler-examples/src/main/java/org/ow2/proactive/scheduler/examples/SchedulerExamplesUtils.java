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
package org.ow2.proactive.scheduler.examples;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.transfer.Transfer;
import com.amazonaws.services.s3.transfer.TransferProgress;


/**
 * SelectionUtils provides static methods for scheduler examples.
 *
 * @author ActiveEon Team
 * @since 17/05/2018
 */
public class SchedulerExamplesUtils {

    private static final Logger logger = Logger.getLogger(SchedulerExamplesUtils.class);

    private SchedulerExamplesUtils() {

    }

    /**
     * waits for the transfer to complete, catching any exceptions that occur.
      * @param xfer
     */
    static void waitForCompletion(Transfer xfer) {
        try {
            xfer.waitForCompletion();
        } catch (AmazonServiceException e) {
            logger.error("Amazon service error: " + e.getMessage());
            System.exit(1);
        } catch (AmazonClientException e) {
            logger.error("Amazon client error: " + e.getMessage());
            System.exit(1);
        } catch (InterruptedException e) {
            logger.error("Transfer interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
            System.exit(1);
        }
    }

    /**
     * Prints progress while waiting for the transfer to finish.
     * @param xfer
     */
    static void showTransferProgress(Transfer xfer) {
        // print the transfer's human-readable description
        logger.info(xfer.getDescription());
        // print an empty progress bar...
        printProgressBar(0.0);
        // update the progress bar while the xfer is ongoing.
        do {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            // Note: so_far and total aren't used, they're just for
            // documentation purposes.
            TransferProgress progress = xfer.getProgress();
            double pct = progress.getPercentTransferred();
            eraseProgressBar();
            printProgressBar(pct);
        } while (!xfer.isDone());
        // print the final state of the transfer.
        Transfer.TransferState xferState = xfer.getState();
        logger.info(": " + xferState);
    }

    /**
     * prints a simple text progressbar: [#####     ]
     * @param pct
     */
    static void printProgressBar(double pct) {
        // if bar_size changes, then change erase_bar (in eraseProgressBar) to
        // match.
        final int bar_size = 40;
        final String empty_bar = "                                        ";
        final String filled_bar = "########################################";
        int amtFull = (int) (bar_size * (pct / 100.0));
        final String logMsg = String.format("  [%s%s]",
                                            filled_bar.substring(0, amtFull),
                                            empty_bar.substring(0, bar_size - amtFull));
        logger.info(logMsg);
    }

    /**
     * erases the progress bar.
     */
    static void eraseProgressBar() {
        // erase_bar is bar_size (from printProgressBar) + 4 chars.
        final String erase_bar = "\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b";
        logger.info(erase_bar);
    }

    /**
     * List recursively the contents of a directory
     * @param dir
     * @param filesRelativePathName
     * @return List of Files' names of the given directory
     * @throws IOException
     */
    static List<String> listDirectoryContents(File dir, List<String> filesRelativePathName) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) {
            throw new IOException("Download failed. The resource in the given S3 URL does not exist or is not accessible");
        }
        for (File file : files) {
            if (file.isDirectory()) {
                listDirectoryContents(file, filesRelativePathName);
            } else {
                filesRelativePathName.add(file.getCanonicalPath());
            }
        }
        return filesRelativePathName;
    }

    /**
     * Creates a directory if it does not exist
     * @param file
     */
    static void createDirIfNotExists(File file) {
        // If the path already exists, print a warning.
        if (!file.exists()) {
            try {
                file.mkdir();
                logger.info("The " + file.getName() + " directory is created");
            } catch (Exception e) {
                logger.error("Couldn't create destination directory! " + file.getName());
                System.exit(1);
            }
        } else {
            logger.info("The given local path " + file.getName() + " already exists");
        }
    }

    /**
     * check whether or not the given file path is a path to a directory terminated by /
     * @param filePath
     * @return
     */
    protected static boolean isDirectoryPath(String filePath) {

        return filePath.endsWith(File.separator);
    }
}
