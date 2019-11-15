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
package org.ow2.proactive.scheduler.task.executors.forked.env;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.ow2.proactive.scheduler.common.task.TaskProgress;


/**
 * @author ActiveEon Team
 * @since 24/10/2019
 */
public class TaskProgressImpl implements TaskProgress {

    File progressFile;

    transient PrintStream output;

    transient PrintStream error;

    public TaskProgressImpl(String progressFilePath, PrintStream output, PrintStream error) {
        if (progressFilePath != null) {
            progressFile = new File(progressFilePath);
        }

        this.output = output;
        this.error = error;
    }

    @Override
    public int get() {
        try {
            return Integer.parseInt(FileUtils.readFileToString(progressFile, StandardCharsets.UTF_8));
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public void set(int progress) {
        if (progress < 0 || progress > 100) {
            throw new IllegalArgumentException("Invalid progress value (must be in range [0..100]): " + progress);
        }
        try {
            FileUtils.writeStringToFile(progressFile, String.valueOf(progress), StandardCharsets.UTF_8);
        } catch (IOException e) {
            if (error != null) {
                error.println("Error when writing progress to progress file.");
                e.printStackTrace(error);
            }

        }
    }
}
