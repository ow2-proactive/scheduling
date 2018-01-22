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

import java.io.*;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;
import org.ow2.proactive.scheduler.common.task.flow.FlowBlock;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;
import functionaltests.utils.SchedulerFunctionalTestWithRestart;
import functionaltests.workflow.JobWorkflowDataspace;
import functionaltests.workflow.TWorkflowJobs;


/**
 * Ensures Dataspace features work properly when combined with control flow operations
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public class TestWorkflowDataspace extends SchedulerFunctionalTestNoRestart {

    @Test
    public void testWorkflowDataspace() throws Throwable {
        testJavaTask();
    }

    private void testJavaTask() throws Throwable {
        String tmpdir = System.getProperty("java.io.tmpdir");
        File inputSpace = new File(tmpdir + File.separator + "inputSpace." + System.currentTimeMillis());
        File outputSpace = new File(tmpdir + File.separator + "outputSpace." + System.currentTimeMillis());

        inputSpace.mkdir();
        outputSpace.mkdir();

        for (int it = 0; it < 3; it++) {
            for (int dup = 0; dup < 3; dup++) {
                File f = new File(inputSpace.getCanonicalPath() + File.separator + it + "_" + dup + ".in");
                f.createNewFile();
                BufferedWriter out = new BufferedWriter(new FileWriter(f));
                out.write("it " + it + " dup " + dup + "\n");
                out.close();
            }
        }

        TaskFlowJob job = new TaskFlowJob();
        job.setName(TestWorkflowDataspace.class.getSimpleName());
        job.setInputSpace(inputSpace.toURI().toURL().toString());
        job.setOutputSpace(outputSpace.toURI().toURL().toString());

        ForkEnvironment forkEnvironment = new ForkEnvironment();

        JavaTask t = new JavaTask();
        t.setName("T");
        t.setExecutableClassName("org.ow2.proactive.scheduler.examples.EmptyTask");
        t.setFlowScript(FlowScript.createReplicateFlowScript("runs = 3;"));
        t.setFlowBlock(FlowBlock.START);
        t.setForkEnvironment(forkEnvironment);
        job.addTask(t);

        JavaTask t1 = new JavaTask();
        t1.setName("T1");
        t1.setForkEnvironment(forkEnvironment);
        t1.setExecutableClassName(JobWorkflowDataspace.class.getCanonicalName());
        t1.setForkEnvironment(new ForkEnvironment());
        t1.addDependence(t);
        t1.addInputFiles("$PA_TASK_ITERATION_$PA_TASK_REPLICATION.in", InputAccessMode.TransferFromInputSpace);
        t1.addOutputFiles("$PA_TASK_ITERATION_$PA_TASK_REPLICATION.out", OutputAccessMode.TransferToOutputSpace);
        job.addTask(t1);

        JavaTask t2 = new JavaTask();
        t2.setName("T2");
        t2.setForkEnvironment(forkEnvironment);
        t2.setExecutableClassName("org.ow2.proactive.scheduler.examples.EmptyTask");
        t2.addDependence(t1);
        t2.setFlowScript(FlowScript.createLoopFlowScript(//
                                                         "if (variables.get('PA_TASK_ITERATION') < 2) {" + //
                                                         "loop = true;" + //
                                                         "} else {" + //
                                                         "loop = false;" + //
                                                         "}", "T"));
        t2.setFlowBlock(FlowBlock.END);
        job.addTask(t2);

        JobId id = TWorkflowJobs.testJobSubmission(schedulerHelper, job, null);
        Assert.assertFalse(schedulerHelper.getJobResult(id).hadException());

        for (int it = 0; it < 3; it++) {
            for (int dup = 0; dup < 3; dup++) {
                File f = new File(outputSpace.getCanonicalPath() + File.separator + it + "_" + dup + ".out");
                Assert.assertTrue("Missing output file " + f.getName(), f.exists());

                BufferedReader in = new BufferedReader(new FileReader(f));
                String line = in.readLine();
                Assert.assertTrue("Wrong content for " + f.getCanonicalPath(), line.equals("it " + it + " dup " + dup));
            }
        }
    }

}
