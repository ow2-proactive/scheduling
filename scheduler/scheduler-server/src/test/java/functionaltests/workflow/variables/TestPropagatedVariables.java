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
package functionaltests.workflow.variables;

import java.util.HashMap;

import org.junit.Test;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.NativeTask;

import functionaltests.executables.PropagateVariablesExec;
import functionaltests.utils.SchedulerFunctionalTestNoRestart;


public class TestPropagatedVariables extends SchedulerFunctionalTestNoRestart {

    @Test
    public void testPropagatedVariables() throws Throwable {
        schedulerHelper.testJobSubmission(createTaskFlowJob());
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
            taskD.setCommandLine("/bin/bash",
                                 "-c",
                                 "echo $variables_Task_A_Var; test \"$variables_Task_A_Var\" == \"Task_A_Val\"");
            taskD.addDependence(taskC);
            flowJob.addTask(taskD);
        }

        return flowJob;
    }

}
