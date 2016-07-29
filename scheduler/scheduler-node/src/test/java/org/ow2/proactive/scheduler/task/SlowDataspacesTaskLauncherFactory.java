package org.ow2.proactive.scheduler.task;

import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.task.data.TaskDataspaces;
import org.ow2.proactive.scheduler.task.executors.InProcessTaskExecutor;
import org.ow2.proactive.scheduler.task.executors.TaskExecutor;

import java.io.File;
import java.util.List;
import java.util.concurrent.Semaphore;

public class SlowDataspacesTaskLauncherFactory extends ProActiveForkedTaskLauncherFactory {
        private Semaphore taskRunning;

    public SlowDataspacesTaskLauncherFactory() {
    }

    public SlowDataspacesTaskLauncherFactory(Semaphore taskRunning) {
            this.taskRunning = taskRunning;
        }

        @Override
        public TaskDataspaces createTaskDataspaces(TaskId taskId, NamingService namingService, boolean isRunAsUser) {
            return new SlowDataspaces(taskRunning);
        }

        @Override
        public TaskExecutor createTaskExecutor(File workingDir) {
            return new InProcessTaskExecutor();
        }


    private static class SlowDataspaces implements TaskDataspaces {

        private Semaphore taskRunning;

        public SlowDataspaces(Semaphore taskRunning) {
            this.taskRunning = taskRunning;
        }

        @Override
        public File getScratchFolder() {
            return new File(".");
        }

        @Override
        public String getScratchURI() {
            return null;
        }

        @Override
        public String getCacheURI() {
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
            if(taskRunning!=null) {
                taskRunning.release();
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void copyScratchDataToOutput(List<OutputSelector> outputFiles) throws FileSystemException {

        }

        @Override
        public void close() {

        }
    }
}
