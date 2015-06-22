package org.ow2.proactive.scheduler.task;

import org.apache.commons.io.FileUtils;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.task.data.TaskDataspaces;
import org.ow2.proactive.scheduler.task.executors.ForkedTaskExecutor;
import org.ow2.proactive.scheduler.task.executors.TaskExecutor;
import org.ow2.proactive.scheduler.task.utils.Decrypter;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

public class TestTaskLauncherFactory extends ProActiveForkedTaskLauncherFactory {
    private Semaphore taskRunning;
    private File dataspacesRootFolder;

    private TaskFileDataspaces dataSpaces;

    public TestTaskLauncherFactory() {
        this(new File(System.getProperty("java.io.tmpdir")));
    }

    public TestTaskLauncherFactory(Semaphore taskRunning) {
        this();
        this.taskRunning = taskRunning;
    }

    public TestTaskLauncherFactory(File dataspacesRootFolder) {
        this.dataspacesRootFolder = dataspacesRootFolder;
        this.dataSpaces = new TaskFileDataspaces(dataspacesRootFolder);
    }

    @Override
    public TaskDataspaces createTaskDataspaces(TaskId taskId, NamingService namingService) {
        return dataSpaces;
    }

    public TaskFileDataspaces getDataSpaces() {
        return dataSpaces;
    }

    @Override
    public TaskExecutor createTaskExecutor(final File workingDir, final Decrypter decrypter) {
        return new TaskExecutor() {
            @Override
            public TaskResultImpl execute(TaskContext container, PrintStream output, PrintStream error) {
                if (taskRunning != null) {
                    taskRunning.release();
                }
                return new ForkedTaskExecutor(workingDir, decrypter).execute(container, output, error);
            }
        };
    }

    public static class TaskFileDataspaces implements TaskDataspaces {

        private final File scratchFolder;
        private final File userspaceFolder;
        private final File globalspaceFolder;
        private final File inputspaceFolder;
        private final File outputspaceFolder;

        public TaskFileDataspaces() {
            this(new File("."));
        }

        public TaskFileDataspaces(File rootFolder) {
            scratchFolder = new File(rootFolder, "scratch");
            userspaceFolder = new File(rootFolder, "userspace");
            globalspaceFolder = new File(rootFolder, "globalspace");
            inputspaceFolder = new File(rootFolder, "inputspace");
            outputspaceFolder = new File(rootFolder, "outputspace");
            try {
                FileUtils.forceMkdir(scratchFolder);
                FileUtils.forceMkdir(userspaceFolder);
                FileUtils.forceMkdir(globalspaceFolder);
                FileUtils.forceMkdir(inputspaceFolder);
                FileUtils.forceMkdir(outputspaceFolder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
                    switch(inputFile.getMode()){
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
    }
}
