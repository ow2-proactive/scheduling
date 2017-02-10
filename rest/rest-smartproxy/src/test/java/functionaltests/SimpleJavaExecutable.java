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
package functionaltests;

import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;

import org.apache.commons.io.FileUtils;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;


/**
 * A simple java executable used by {@link RestSmartProxyTest}
 * For all files in the localspace (non recursive into folders) creates an
 * output file with the same content and the .out extension.
 *
 * @author The ProActive Team
 */
public class SimpleJavaExecutable extends JavaExecutable {

    @Override
    public Serializable execute(TaskResult... results) throws Throwable {
        File localSpaceFolder = new File(".");
        System.out.println("Using localspace folder " + localSpaceFolder.getAbsolutePath());
        System.out.println(localSpaceFolder.listFiles());
        File[] inputFiles = localSpaceFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(RestSmartProxyTest.INPUT_FILE_EXT);
            }
        });

        for (File inputFile : inputFiles) {
            String outputFileName = inputFile.getName()
                                             .replace("input", "output")
                                             .replace(RestSmartProxyTest.INPUT_FILE_EXT,
                                                      RestSmartProxyTest.OUTPUT_FILE_EXT);
            File outputFile = new File(outputFileName);
            FileUtils.copyFile(inputFile, outputFile);
            System.out.println("Written file " + outputFile.getAbsolutePath());
        }
        return "OK";
    }

}
