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
package functionaltests.dataspaces;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileFilter;
import java.net.URI;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.OnTaskError;
import org.ow2.proactive.scheduler.common.task.ScriptTask;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;

import functionaltests.utils.SchedulerFunctionalTest;
import functionaltests.utils.SchedulerFunctionalTestWithCustomConfigAndRestart;
import functionaltests.utils.SchedulerTHelper;


/**
 * This test is executing a set tasks which produces multiple files and another set of tasks which consume these files and produce new ones
 *
 * The test is successful if all files could be processed and there are no missing outputs
 *
 * The test starts a clean scheduler in non-fork mode (for memory consumption reasons) with many nodes running in the same JVM
 */
public class TestDataspaceConcurrentTransfer extends SchedulerFunctionalTestWithCustomConfigAndRestart {

    static final int NB_NODES = 50;

    static final int NB_TASKS = NB_NODES;

    static final String JOB_NAME = "TestDataspaceConcurrentTransfer";

    static final String TASK_NAME_CREATE = "ConcurrentCreate_";

    static final String TASK_NAME_PROCESS = "ConcurrentProcessFile_";

    static final String FILE_EXT_IN = ".in";

    static final String FILE_EXT_OUT = ".out";

    static final String FILE_NAME = "TASKFILE_";

    static final String FOLDER_IN_NAME = "FOLDER_IN_";

    static final String FOLDER_OUT_NAME = "FOLDER_OUT_";

    File globalSpaceFile;

    @BeforeClass
    public static void startDedicatedScheduler() throws Exception {
        // we start a scheduler with an empty RM and add multiple nodes
        schedulerHelper = new SchedulerTHelper(true,
                                               true,
                                               new File(SchedulerFunctionalTest.class.getResource("/functionaltests/config/scheduler-nonforkedscripttasks.ini")
                                                                                     .toURI()).getAbsolutePath());

        schedulerHelper.createNodeSource(JOB_NAME, NB_NODES);
    }

    @Before
    public void setUp() throws Exception {
        Scheduler sched = schedulerHelper.getSchedulerInterface();
        String globalURI = sched.getGlobalSpaceURIs().get(0);
        assertTrue(globalURI.startsWith("file:"));
        log("Global Space URI is " + globalURI);
        globalSpaceFile = new File(new URI(globalURI));

        FileUtils.cleanDirectory(globalSpaceFile);
    }

    @Test
    public void multiple_tasks_transferring() throws Throwable {
        Job job = createJobWithFileTransfers();

        JobId id = schedulerHelper.testJobSubmission(job);
        assertFalse("The job execution must not fail, check the node source log file : Node-local-" + JOB_NAME + ".log",
                    schedulerHelper.getJobResult(id).hadException());

        File[] inputDirectories = globalSpaceFile.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory() && pathname.getName().startsWith(FOLDER_IN_NAME);
            }
        });

        File[] outputDirectories = globalSpaceFile.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory() && pathname.getName().startsWith(FOLDER_OUT_NAME);
            }
        });

        Assert.assertEquals(NB_TASKS, inputDirectories.length);
        Assert.assertEquals(NB_TASKS, outputDirectories.length);

        HashMap<String, File> directoriesMap = new HashMap<>();

        for (File dir : inputDirectories) {
            directoriesMap.put(dir.getName(), dir);
        }

        for (File dir : outputDirectories) {
            directoriesMap.put(dir.getName(), dir);
        }

        for (int i = 0; i < NB_TASKS; i++) {
            File inputDir = directoriesMap.get(FOLDER_IN_NAME + i);
            assertNotNull(inputDir);
            File inputFile = new File(inputDir, FILE_NAME + i + FILE_EXT_IN);
            Assert.assertTrue(inputFile.exists());

            File outputDir = directoriesMap.get(FOLDER_OUT_NAME + i);
            assertNotNull(outputDir);
            File outputFile = new File(outputDir, FILE_NAME + i + FILE_EXT_OUT);
            Assert.assertTrue(outputFile.exists());
        }
    }

    public Job createJobWithFileTransfers() throws UserException, InvalidScriptException {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(JOB_NAME);
        job.setOnTaskError(OnTaskError.CONTINUE_JOB_EXECUTION);
        for (int i = 0; i < NB_TASKS; i++) {
            ScriptTask st1 = new ScriptTask();
            st1.setName(TASK_NAME_CREATE + i);
            st1.setScript(new TaskScript(new SimpleScript("org.apache.commons.io.FileUtils.touch(new File(localspace, \"" +
                                                          FOLDER_IN_NAME + i + "/" + FILE_NAME + i + FILE_EXT_IN +
                                                          "\"));", "groovy")));
            st1.addOutputFiles("**/*" + FILE_EXT_IN, OutputAccessMode.TransferToGlobalSpace);
            job.addTask(st1);

            ScriptTask st2 = new ScriptTask();
            st2.setName(TASK_NAME_PROCESS + i);
            st2.setScript(new TaskScript(new SimpleScript("org.apache.commons.io.FileUtils.copyFile(new File(localspace, \"" +
                                                          FOLDER_IN_NAME + i + "/" + FILE_NAME + i + FILE_EXT_IN +
                                                          "\"), new File(localspace, \"" + FOLDER_OUT_NAME + i + "/" +
                                                          FILE_NAME + i + FILE_EXT_OUT + "\"));", "groovy")));
            st2.addInputFiles(FOLDER_IN_NAME + i + "/" + FILE_NAME + i + FILE_EXT_IN,
                              InputAccessMode.TransferFromGlobalSpace);
            st2.addOutputFiles("**/*" + FILE_EXT_OUT, OutputAccessMode.TransferToGlobalSpace);
            st2.addDependence(st1);
            job.addTask(st2);
        }
        return job;
    }

}
