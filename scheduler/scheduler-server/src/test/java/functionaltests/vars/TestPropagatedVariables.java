/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package functionaltests.vars;

import java.util.HashMap;

import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.junit.Test;

import functionaltests.SchedulerConsecutive;
import functionaltests.SchedulerTHelper;
import functionaltests.executables.PropagateVariablesExec;


public class TestPropagatedVariables extends SchedulerConsecutive {

    @Test
    public void run() throws Throwable {
        SchedulerTHelper.testJobSubmissionAndVerifyAllResults(createTaskFlowJob(),
                "TestPropagatedVariables.TestFlowJob");
    }

    private TaskFlowJob createTaskFlowJob() throws UserException {
        TaskFlowJob flowJob = new TaskFlowJob();
        JavaTask taskA = new JavaTask();
        taskA.setName("Task_A");
        HashMap<String, String> setA = new HashMap<>();
        setA.put("Task_A_Var", "Task_A_Val");
        taskA.addArgument("set", setA);
        taskA.setExecutableClassName(PropagateVariablesExec.class.getName());
        flowJob.addTask(taskA);

        JavaTask taskB = new JavaTask();
        taskB.setName("Task_B");
        HashMap<String, String> setB = new HashMap<>();
        setB.put("Task_B_Var", "Task_B_Val");
        taskB.addArgument("set", setB);
        taskB.setExecutableClassName(PropagateVariablesExec.class.getName());
        flowJob.addTask(taskB);

        JavaTask taskC = new JavaTask();
        taskC.setName("Task_C");
        taskC.addDependence(taskA);
        taskC.addDependence(taskB);
        HashMap<String, String> checkC = new HashMap<>();
        checkC.put("Task_A_Var", "Task_A_Val");
        checkC.put("Task_B_Var", "Task_B_Val");
        taskC.addArgument("check", checkC);
        taskC.setExecutableClassName(PropagateVariablesExec.class.getName());
        flowJob.addTask(taskC);

        if (OperatingSystem.unix == OperatingSystem.getOperatingSystem()) {
            NativeTask taskD = new NativeTask();
            taskD.setName("TaskD");
            taskD
                    .setCommandLine(
                            "/bin/bash",
                            "-c",
                            "echo $variables_Task_A_Var; test \"$variables_Task_A_Var\" == \"Task_A_Val\"");
            taskD.addDependence(taskC);
            flowJob.addTask(taskD);
        }

        return flowJob;
    }

}
