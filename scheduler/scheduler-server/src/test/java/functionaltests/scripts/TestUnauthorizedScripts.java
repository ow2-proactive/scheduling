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
package functionaltests.scripts;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.ScriptTask;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;

import functionaltests.utils.SchedulerFunctionalTestWithCustomConfigAndRestart;
import functionaltests.utils.SchedulerTHelper;


/**
 * This class tests the authorized script functionality.
 * When enabled, only authorized fork or clean script can be executed.
 */
public class TestUnauthorizedScripts extends SchedulerFunctionalTestWithCustomConfigAndRestart {

    @ClassRule
    public static TemporaryFolder folder = new TemporaryFolder();

    private static File scriptsFolder;

    private static String authorizedForkScriptContent;

    private static String authorizedCleanScriptContent;

    private static String authorizedSelectionScriptContent;

    private static String unauthorizedForkScriptContent;

    private static String unauthorizedCleanScriptContent;

    private static String unauthorizedSelectionScriptContent;

    static URL originalSchedulerConfigFile = TestUnauthorizedScripts.class.getResource("/functionaltests/scripts/schedulerPropertiesUnauthorizedScripts.ini");

    static URL originalRMConfigFile = TestUnauthorizedScripts.class.getResource("/functionaltests/scripts/rmPropertiesUnauthorizedScripts.ini");

    @BeforeClass
    public static void before() throws Throwable {
        // initialize the scheduler & rm with a custom authorized script folder

        scriptsFolder = folder.newFolder("scripts");
        File schedulerConfigFile = generateConfigFile(originalSchedulerConfigFile.toURI(), "schedulerConfig");
        File rmConfigFile = generateConfigFile(originalRMConfigFile.toURI(), "rmConfig");

        // create authorized and unauthorized scripts
        String tempFolderPathEscaped = folder.getRoot().getAbsolutePath().replace("\\", "\\\\");
        authorizedForkScriptContent = "new File('" + tempFolderPathEscaped + "','fork_auth.out').write('ok')";
        authorizedCleanScriptContent = "new File('" + tempFolderPathEscaped + "','clean_auth.out').write('ok')";
        authorizedSelectionScriptContent = "new File('" + tempFolderPathEscaped +
                                           "','selection_auth.out').write('ok'); selected=true";

        File forkScript = new File(scriptsFolder, "forkScript");
        FileUtils.write(forkScript, authorizedForkScriptContent, Charset.defaultCharset(), false);
        File cleanScript = new File(scriptsFolder, "cleanScript");
        FileUtils.write(cleanScript, authorizedCleanScriptContent, Charset.defaultCharset(), false);
        File selectionScript = new File(scriptsFolder, "selectionScript");
        FileUtils.write(selectionScript, authorizedSelectionScriptContent, Charset.defaultCharset(), false);

        unauthorizedForkScriptContent = "new File('" + tempFolderPathEscaped + "','fork.out').write('ko')";
        unauthorizedCleanScriptContent = "new File('" + tempFolderPathEscaped + "','clean.out').write('ko')";
        unauthorizedSelectionScriptContent = "new File('" + tempFolderPathEscaped +
                                             "','selection.out').write('ko'); selected=true";

        // start the configured scheduler
        schedulerHelper = new SchedulerTHelper(true,
                                               schedulerConfigFile.getAbsolutePath(),
                                               rmConfigFile.getAbsolutePath(),
                                               null);
    }

    private static File generateConfigFile(URI originalConfigFile, String configFileName) throws IOException {
        File schedulerPropertiesfile = new File(originalConfigFile);
        String schedulerConfFileContent = FileUtils.readFileToString(schedulerPropertiesfile, Charset.defaultCharset());

        schedulerConfFileContent = schedulerConfFileContent.replace("%PATH_TO_SCRIPT_DIR%",
                                                                    scriptsFolder.getAbsolutePath().replace("\\",
                                                                                                            "\\\\"));
        File newConfigFile = folder.newFile(configFileName);
        FileUtils.write(newConfigFile, schedulerConfFileContent, Charset.defaultCharset(), false);
        return newConfigFile;
    }

    @Test
    public void testAuthorizedForkAndCleanScripts() throws Exception {
        Job job = createJob(authorizedForkScriptContent, authorizedCleanScriptContent);
        schedulerHelper.testJobSubmission(job);
        Thread.sleep(1000);
        File forkOut = new File(folder.getRoot().getAbsolutePath(), "fork_auth.out");
        Assert.assertTrue("File created by the authorized fork env script should exist", forkOut.exists());
        File cleanOut = new File(folder.getRoot().getAbsolutePath(), "clean_auth.out");
        Assert.assertTrue("File created by the authorized clean env script should exist", cleanOut.exists());
    }

    @Test
    public void testAuthorizedSelectionScripts() throws Exception {
        Job job = createJobSelection(authorizedSelectionScriptContent);
        schedulerHelper.testJobSubmission(job);
        File selectionOut = new File(folder.getRoot().getAbsolutePath(), "selection_auth.out");
        Assert.assertTrue("File created by the selection script should exist", selectionOut.exists());

    }

    @Test
    public void testUnAuthorizedSelectionScripts() throws Exception {
        Job job = createJobSelection(unauthorizedSelectionScriptContent);
        JobId id = schedulerHelper.submitJob(job);
        Thread.sleep(10000);
        File selectionOut = new File(folder.getRoot().getAbsolutePath(), "selection.out");
        Assert.assertFalse("File created by the unauthorized selection script should NOT exist", selectionOut.exists());

        Assert.assertEquals(JobStatus.PENDING, schedulerHelper.getSchedulerInterface().getJobState(id).getStatus());

        schedulerHelper.getSchedulerInterface().killJob(id);
    }

    @Test
    public void testUnAuthorizedForkAndCleanScripts() throws Exception {
        Job job = createJob(unauthorizedForkScriptContent, unauthorizedCleanScriptContent);

        schedulerHelper.testJobSubmission(job, true, false);

        Thread.sleep(1000);
        File forkOut = new File(folder.getRoot().getAbsolutePath(), "fork.out");
        Assert.assertFalse("File created by the unauthorized fork env script should NOT exist", forkOut.exists());
        File cleanOut = new File(folder.getRoot().getAbsolutePath(), "clean.out");
        Assert.assertFalse("File created by the unauthorized clean env script should NOT exist", cleanOut.exists());
    }

    public Job createJob(String forkScriptContent, String cleanScriptContent)
            throws InvalidScriptException, UserException {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_forkAndClean");
        ScriptTask taskWithFork = new ScriptTask();
        taskWithFork.setScript(new TaskScript(new SimpleScript("println 'Hello'", "groovy")));
        ForkEnvironment forkEnvironment = new ForkEnvironment();
        forkEnvironment.setEnvScript(new SimpleScript(forkScriptContent, "groovy"));
        taskWithFork.setForkEnvironment(forkEnvironment);

        ScriptTask taskWithClean = new ScriptTask();
        taskWithClean.setScript(new TaskScript(new SimpleScript("println 'Hello'", "groovy")));
        taskWithClean.setCleaningScript(new SimpleScript(cleanScriptContent, "groovy"));
        job.addTask(taskWithFork);
        job.addTask(taskWithClean);
        return job;
    }

    public Job createJobSelection(String selectionScriptContent) throws InvalidScriptException, UserException {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_selection");
        ScriptTask taskWithSelection = new ScriptTask();
        taskWithSelection.setScript(new TaskScript(new SimpleScript("println 'Hello'", "groovy")));
        taskWithSelection.addSelectionScript(new SelectionScript(new SimpleScript(selectionScriptContent, "groovy"),
                                                                 true));

        job.addTask(taskWithSelection);
        return job;
    }
}
