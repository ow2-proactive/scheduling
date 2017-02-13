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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import org.apache.commons.io.FileUtils;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.task.context.TaskContext;
import org.ow2.proactive.scheduler.task.data.TaskDataspaces;
import org.ow2.proactive.scheduler.task.executors.ForkedTaskExecutor;
import org.ow2.proactive.scheduler.task.executors.TaskExecutor;


public class TestTaskLauncherFactory extends ProActiveForkedTaskLauncherFactory {
    private Semaphore taskRunning;

    private TaskFileDataspaces dataSpaces;

    public TestTaskLauncherFactory() {
        this(new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString()));
    }

    public TestTaskLauncherFactory(Semaphore taskRunning) {
        this();
        this.taskRunning = taskRunning;
    }

    public TestTaskLauncherFactory(File dataspacesRootFolder) {
        this.dataSpaces = new TaskFileDataspaces(dataspacesRootFolder);
    }

    @Override
    public TaskDataspaces createTaskDataspaces(TaskId taskId, NamingService namingService, boolean isRunAsUser) {
        return dataSpaces;
    }

    public TaskFileDataspaces getDataSpaces() {
        return dataSpaces;
    }

    @Override
    public TaskExecutor createTaskExecutor(final File workingDir) {
        return new TaskExecutor() {
            @Override
            public TaskResultImpl execute(TaskContext container, PrintStream output, PrintStream error) {
                if (taskRunning != null) {
                    taskRunning.release();
                }
                return new ForkedTaskExecutor(workingDir).execute(container, output, error);
            }
        };
    }

    public static class TaskFileDataspaces implements TaskDataspaces {

        private final File scratchFolder;

        private final File cacheFolder;

        private final File userspaceFolder;

        private final File globalspaceFolder;

        private final File inputspaceFolder;

        private final File outputspaceFolder;

        public TaskFileDataspaces() {
            this(new File("."));
        }

        public TaskFileDataspaces(File rootFolder) {
            scratchFolder = createDataspaceFolder(rootFolder, "scratch");
            cacheFolder = createDataspaceFolder(rootFolder, "cache");
            userspaceFolder = createDataspaceFolder(rootFolder, "userspace");
            globalspaceFolder = createDataspaceFolder(rootFolder, "globalspace");
            inputspaceFolder = createDataspaceFolder(rootFolder, "inputspace");
            outputspaceFolder = createDataspaceFolder(rootFolder, "outputspace");
        }

        private File createDataspaceFolder(File rootFolder, String dsName) {
            File dsFolder = new File(rootFolder, dsName);
            dsFolder.deleteOnExit();
            try {
                FileUtils.forceMkdir(dsFolder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return dsFolder;
        }

        // implement a fake one with files
        @Override
        public File getScratchFolder() {
            return scratchFolder;
        }

        @Override
        public String getScratchURI() {
            return scratchFolder.getAbsolutePath();
        }

        @Override
        public String getCacheURI() {
            return cacheFolder.getAbsolutePath();
        }

        @Override
        public String getInputURI() {
            return inputspaceFolder.getAbsolutePath();
        }

        @Override
        public String getOutputURI() {
            return outputspaceFolder.getAbsolutePath();
        }

        @Override
        public String getUserURI() {
            return userspaceFolder.getAbsolutePath();
        }

        @Override
        public String getGlobalURI() {
            return globalspaceFolder.getAbsolutePath();
        }

        @Override
        public void copyInputDataToScratch(List<InputSelector> inputFiles) throws FileSystemException {
            for (InputSelector inputFile : inputFiles) {
                Set<String> includes = inputFile.getInputFiles().getIncludes();
                for (String include : includes) {
                    switch (inputFile.getMode()) {
                        case TransferFromInputSpace:
                            copyFile(new File(inputspaceFolder, include), new File(scratchFolder, include));
                            break;
                        case TransferFromOutputSpace:
                            copyFile(new File(outputspaceFolder, include), new File(scratchFolder, include));
                            break;
                        case TransferFromUserSpace:
                            copyFile(new File(userspaceFolder, include), new File(scratchFolder, include));
                            break;
                        case TransferFromGlobalSpace:
                            copyFile(new File(globalspaceFolder, include), new File(scratchFolder, include));
                            break;
                    }
                }
            }
        }

        @Override
        public void copyScratchDataToOutput(List<OutputSelector> outputFiles) throws FileSystemException {
            for (OutputSelector outputFile : outputFiles) {
                Set<String> includes = outputFile.getOutputFiles().getIncludes();
                for (String include : includes) {
                    switch (outputFile.getMode()) {
                        case TransferToUserSpace:
                            copyFile(new File(scratchFolder, include), new File(userspaceFolder, include));
                            break;
                        case TransferToGlobalSpace:
                            copyFile(new File(scratchFolder, include), new File(globalspaceFolder, include));
                            break;
                        case TransferToOutputSpace:
                            copyFile(new File(scratchFolder, include), new File(outputspaceFolder, include));
                            break;
                    }
                }
            }
        }

        private void copyFile(File source, File destination) throws FileSystemException {
            try {
                FileUtils.copyFile(source, destination);
            } catch (IOException e) {
                throw new FileSystemException(e);
            }
        }

        @Override
        public void close() {
            FileUtils.deleteQuietly(getScratchFolder());
        }
    }
}
