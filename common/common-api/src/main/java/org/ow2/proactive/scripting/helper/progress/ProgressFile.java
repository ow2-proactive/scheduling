/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scripting.helper.progress;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility methods that can be invoked from scripts to ease reading and writing
 * of progress values from or to a file.
 *
 * @author The ProActive Team
 */
public final class ProgressFile {

    /**
     * Returns the progress value read from the specified {@code
     * progressFilePath} as an int.
     *
     * @param progressFilePath the absolute path to the progress file to read
     *                         from.
     * @return the progress value read or {@code -1} in case of error.
     */
    public static int getProgress(String progressFilePath) {
        return getProgress(Paths.get(progressFilePath));
    }

    /**
     * Returns the progress value read from the specified {@code progressFile}
     * as an int.
     *
     * @param progressFile the progress file to read from.
     * @return the progress value read, {@code 0} if the specified {@code
     * progressFile} does not exist or is empty, {@code -1} in case of error.
     */
    public static int getProgress(Path progressFile) {
        if (!Files.exists(progressFile)) {
            return 0;
        }

        try {
            String line =
                    com.google.common.io.Files.readFirstLine(
                            progressFile.toFile(), Charset.defaultCharset());

            if (line == null) {
                // file content was empty
                return 0;
            }

            try {
                // try to parse double to allow int + double
                int value = (int) Double.parseDouble(line);

                if (!isValidProgressValue(value)) {
                    logError("Invalid progress value: " + value);
                    return -1;
                }

                return value;
            } catch (NumberFormatException e) {
                logError("Progress value is a not a numeric value: " + line);
                return -1;
            }
        } catch (IOException e) {
            logError("Error while reading the first line of " + progressFile);
            return -1;
        }
    }

    /**
     * Returns a boolean that indicates whether the specified value is a valid
     * progress value.
     *
     * @param value the progress value to check.
     * @return {@code true} if the specified value is between {@code 0} and
     * {@code 100} included, {@code false} otherwise.
     */
    public static boolean isValidProgressValue(int value) {
        return value >= 0 && value <= 100;
    }

    /**
     * Write the specified {@code value} to the given {@code progressFilePath}
     * if the value is between 0 and 100 included. Otherwise, nothing is
     * performed.
     *
     * @param progressFilePath the absolute path to the file to write the
     *                         progress value in.
     * @param value            the progress value, expected to be between 0 and
     *                         100 included.
     * @return {@code false} if the value is out of range or an IO exception
     * occurred, {@code true} otherwise.
     */
    public static boolean setProgress(String progressFilePath, int value) {
        return setProgress(Paths.get(progressFilePath), value);
    }

    /**
     * Write the specified {@code value} to the given {@code progressFilePath}
     * if the value is between 0 and 100 included. Otherwise, nothing is
     * performed.
     *
     * @param progressFile the path to the file to write the progress value in.
     * @param value        the progress value, expected to be between 0 and 100
     *                     included.
     * @return {@code false} if the value is out of range or an IO exception
     * occurred, {@code true} otherwise.
     */
    public static boolean setProgress(Path progressFile, int value) {
        if (!isValidProgressValue(value)) {
            return false;
        }

        return setProgress(progressFile, Integer.toString(value));
    }

    /**
     * Write the specified {@code value} to the given {@code progressFilePath}.
     * No check is performed regarding the type of the value. This method is
     * mainly here for test purposes.
     *
     * @param progressFile the path to the file to write the progress value in.
     * @param value        the progress value
     * @return {@code false} if an IO exception occurred, {@code true}
     * otherwise.
     */
    public static boolean setProgress(Path progressFile, String value) {
        try (OutputStream outputStream =
                     Files.newOutputStream(progressFile)) {
            outputStream.write(value.getBytes(Charset.defaultCharset()));

            return true;
        } catch (IOException e) {
            logError("Writing progress to file '"
                    + progressFile + "' with value '" + value + "' failed");

            return false;
        }
    }

    private static void logError(String msg) {
        System.err.println(msg);
    }

}
