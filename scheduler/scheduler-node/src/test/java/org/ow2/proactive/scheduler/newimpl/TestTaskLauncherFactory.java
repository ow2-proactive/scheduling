package org.ow2.proactive.scheduler.newimpl;

import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.ow2.proactive.scheduler.common.task.Decrypter;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.newimpl.data.TaskDataspaces;
import org.ow2.proactive.scheduler.task.TaskResultImpl;

import java.io.File;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.Semaphore;

public class TestTaskLauncherFactory extends TaskLauncherFactory {
    private Semaphore taskRunning;

    public TestTaskLauncherFactory() {
    }


    public TestTaskLauncherFactory(Semaphore taskRunning) {
        this.taskRunning = taskRunning;
    }

    @Override
    public TaskDataspaces createTaskDataspaces(TaskId taskId, NamingService namingService) {
        return new TaskFileDataspaces();
    }

    @Override
    public TaskExecutor createTaskExecutor(final File workingDir, final Decrypter decrypter) {
        return new TaskExecutor() {
            @Override
            public TaskResultImpl execute(TaskContext container, PrintStream output, PrintStream error) {
                if(taskRunning != null){
                    taskRunning.release();
                }
                return new ForkerTaskExecutor(workingDir, decrypter).execute(container, output, error);
            }
        };
    }

    public static class TaskFileDataspaces implements TaskDataspaces {

        @Override
        public File getScratchFolder() {
            return new File(".");
        }

        @Override
        public String getScratchURI() {
            return null;
        }

        @Override
        public String getInputURI() {
            return null;
        }

        @Override
        public String getOutputURI() {
            return null;
        }

        @Override
        public String getUserURI() {
            return null;
        }

        @Override
        public String getGlobalURI() {
            return null;
        }

        @Override
        public void copyInputDataToScratch(List<InputSelector> inputFiles) throws FileSystemException {

        }

        @Override
        public void copyScratchDataToOutput(List<OutputSelector> outputFiles) throws FileSystemException {

        }
    }
}
