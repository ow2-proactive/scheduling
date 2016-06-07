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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests.scripts.clean;

import functionaltests.utils.SchedulerFunctionalTestWithRestart;
import functionaltests.utils.SchedulerStartForFunctionalTest;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;


/**
 * This test checks that variable bindings are available and correctly set in cleaning scripts
 * If test succeed, you will see something like this in the console :
 * Contenu: firstTask -- Task success: true -- value test: initialValue
 *          secondTask -- Task success: true -- value test: newValue
 *          errorTask -- Task success: false -- value test: newNewValue
 *          errorTask -- Task success: false -- value test: newNewValue
 *
 * This is also a test for the PA_TASK_SUCCESS variable which indicates if a task has ended
 * with errors or not
 */
public class TestJobCleaningScriptVariables extends SchedulerFunctionalTestWithRestart {

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();


    private static URL jobDescriptor = TestJobCleaningScriptVariables.class
            .getResource("/functionaltests/descriptors/Job_cleaning_variables.xml");

    @Test
    public void testJobSelScriptVariables() throws Throwable {

        HashMap<String,String> variables = new HashMap<>();
        File ok = tmpDir.newFile("ok");

        variables.put("path",ok.toString());
        variables.put("test","initialValue");

        JobId jobId = schedulerHelper.submitJob(new File(jobDescriptor.toURI())
                .getAbsolutePath(),variables);

        schedulerHelper.waitForEventJobFinished(jobId);

        while(schedulerHelper.getResourceManager().getState().getFreeNodesNumber()!= SchedulerStartForFunctionalTest.RM_NODE_NUMBER);

        String content;
        content = FileUtils.readFileToString(ok);
        System.out.println("Contenu: "+content);
    }
}