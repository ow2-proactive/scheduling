/*
 *  *
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 *  * $$PROACTIVE_INITIAL_DEV$$
 */

package org.ow2.proactive.scheduler.job;

import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.task.flow.FlowAction;
import org.ow2.proactive.scheduler.common.task.flow.FlowBlock;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scheduler.core.SchedulerStateUpdate;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.ExecuterInformation;
import org.ow2.proactive.scheduler.task.internal.InternalScriptTask;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.util.TaskLogger;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.tests.ProActiveTest;

import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;


public class TagTest extends ProActiveTest{

    /* mocks */
    protected TaskResultImpl resultMock = mock(TaskResultImpl.class);
    protected SchedulerStateUpdate schedulerStateUpdateMock = mock(SchedulerStateUpdate.class);
    protected ExecuterInformation executerInformationMock = mock(ExecuterInformation.class);


    protected InternalTaskFlowJob job = null;



    @Before
    public void setUp() {
        job = new InternalTaskFlowJob();
    }



    private InternalScriptTask createTask(String name, InternalTask[] dependences, FlowBlock block, String matchingBlock)
             {
        InternalScriptTask result = new InternalScriptTask();
        result.setName(name);

        if(dependences != null && dependences.length > 0){
            for(InternalTask dep: dependences){
                result.addDependence(dep);
            }
        }

        if(block != null){
            result.setFlowBlock(block);
            result.setMatchingBlock(matchingBlock);
        }

        job.addTask(result);
        return result;
    }



    private InternalScriptTask createLoopTask(String name, String scriptContent, InternalTask[] dependences, String targetName, boolean block)
            throws InvalidScriptException {
        FlowBlock fb = null;
        if(block){
            fb = FlowBlock.END;
        }
        InternalScriptTask result = createTask(name, dependences, fb, targetName);
        FlowScript loop = FlowScript.createLoopFlowScript(scriptContent, targetName);
        result.setFlowScript(loop);

        return result;
    }


    private InternalScriptTask createReplicateTask(String name, InternalTask[] dependences, FlowBlock block, String matchingBlock, int nbRuns)
            throws InvalidScriptException {
        InternalScriptTask result = createTask(name, dependences, block, matchingBlock);
        FlowScript replicate = FlowScript.createReplicateFlowScript("runs = " + nbRuns + ";");
        result.setFlowScript(replicate);
        return result;
    }



    private void execute(InternalTask task){
        task.setExecuterInformation(executerInformationMock);
        job.startTask(task);
        FlowScript script = task.getFlowScript();
        FlowAction action = null;
        if(script != null) {
            action = task.getFlowScript().execute().getResult();
        }
        job.terminateTask(false, task.getId(), schedulerStateUpdateMock, action, resultMock);
        System.out.println("executed " + task.getName() + " -> " + getTaskNameList(true));
    }


    private void execute(String [] tasks) throws UnknownTaskException {
        for(String currentTaskName: tasks){
            InternalTask currentTask = job.getTask(currentTaskName);
            execute(currentTask);
        }
    }


    private String getTaskNameList(boolean showTags){
        StringBuffer buf = new StringBuffer();
        ArrayList<InternalTask> tasks = job.getITasks();
        if(tasks.size() > 0){
            buf.append(tasks.get(0).getName());
            for(int i = 1; i < tasks.size(); i++){
                InternalTask currentTask = tasks.get(i);
                buf.append(", ");
                buf.append(currentTask.getName());
                if(showTags){
                    if(currentTask.getTag() != null){
                        buf.append("(");
                        buf.append(currentTask.getTag());
                        buf.append(")");
                    }
                }
            }
        }
        return buf.toString();
    }


    private void assertTags(String expectedTag, String[] taskNames) throws UnknownTaskException {
        for(String currentTaskName: taskNames){
            assertEquals(expectedTag, job.getTask(currentTaskName).getTag());
        }
    }

    private void assertTagsStartsWith(String expectedTag, String[] taskNames) throws UnknownTaskException {
        for(String currentTaskName: taskNames){
            assertTrue(job.getTask(currentTaskName).getTag().startsWith(expectedTag));
        }
    }

    @Test
    public void testReplicationLoopSelfTag() throws Exception {
        InternalScriptTask task1 = createLoopTask("T1", "loop = true;", null, "T1", false);
        execute(task1);

        InternalTask task2 = job.getTask("T1#1");
        execute(task2);

        assertEquals("LOOP-T1-1", job.getTask("T1#1").getTag());
        assertEquals("LOOP-T1-2", job.getTask("T1#2").getTag());
    }

    @Test
    public void testReplicationLoopTag() throws Exception {
        InternalTask task1 = createTask("T1", null, FlowBlock.START, "T3");
        InternalTask task2 = createTask("T2", new InternalTask[]{task1}, null, null);
        InternalTask task3 = createLoopTask("T3", "loop = true;", new InternalTask[]{task2}, "T1", true);

        execute(new String[]{"T1", "T2", "T3", "T1#1", "T2#1", "T3#1"});

        assertTags("LOOP-T3-1", new String[]{"T1#1", "T2#1", "T3#1"});
        assertTags("LOOP-T3-2", new String[]{"T1#2", "T2#2", "T3#2"});
    }


    @Test
    public void testReplicationLoopSelfCronTag() throws Exception {
        InternalScriptTask task1 = createLoopTask("T1", "loop = '* * * * *'", null, "T1", false);
        execute(new String[]{"T1", "T1#1"});

        assertTagsStartsWith("LOOP-T1-", new String[]{"T1#1", "T1#2"});
    }


    @Test
    public void testReplicationReplicateTag() throws Exception {
        InternalTask task1 = createReplicateTask("T1", null, null, null, 3);
        InternalTask task2 = createTask("T2", new InternalTask[]{task1}, null, null);
        InternalTask task3 = createTask("T3", new InternalTask[]{task2}, null, null);

        execute(task1);

        assertNull(job.getTask("T2").getTag());
        assertEquals("REPLICATE-T1-1", job.getTask("T2*1").getTag());
        assertEquals("REPLICATE-T1-2", job.getTask("T2*2").getTag());
    }


    @Test
    public void testReplicationReplicateBlockTag() throws Exception {
        InternalTask task1 = createReplicateTask("T1", null, null, null, 3);
        InternalTask task2 = createTask("T2", new InternalTask[]{task1}, FlowBlock.START, "T4");
        InternalTask task3 = createTask("T3", new InternalTask[]{task2}, null, null);
        InternalTask task4 = createTask("T4", new InternalTask[]{task3}, FlowBlock.END, "T2");

        execute(task1);

        assertNull(job.getTask("T2").getTag());
        assertNull(job.getTask("T3").getTag());
        assertNull(job.getTask("T4").getTag());

        assertTags("REPLICATE-T1-1", new String[]{"T2*1", "T3*1", "T4*1"});
        assertTags("REPLICATE-T1-2", new String[]{"T2*2", "T3*2", "T4*2"});
    }


    @Test
    public void testReplicationReplicateCombinedTag() throws Exception {
        InternalTask T = createReplicateTask("T", null, FlowBlock.START, "T2", 3);
        InternalTask T1 = createTask("T1", new InternalTask[]{T}, FlowBlock.START, "T5");
        InternalTask T3 = createTask("T3", new InternalTask[]{T1}, null, null);
        InternalTask T4 = createTask("T4", new InternalTask[]{T1}, null, null);
        InternalTask T5 = createTask("T5", new InternalTask[]{T3, T4}, FlowBlock.END, "T1");
        InternalTask T2 = createLoopTask("T2", "loop = true;", new InternalTask[]{T5}, "T", true);

        execute(new String[]{"T", "T1", "T3", "T4", "T5",
                "T1*1", "T3*1", "T4*1", "T5*1",
                "T1*2", "T3*2", "T4*2", "T5*2",
                "T2", "T#1", "T1#1", "T3#1", "T4#1", "T5#1",
                "T1#1*1", "T3#1*1", "T4#1*1", "T5#1*1",
                "T1#1*2", "T3#1*2", "T4#1*2", "T5#1*2"});

        assertTags("REPLICATE-T-1", new String[]{"T1*1", "T3*1", "T4*1", "T5*1"});
        assertTags("REPLICATE-T-2", new String[]{"T1*2", "T3*2", "T4*2", "T5*2"});
        assertTags("LOOP-T2-1", new String[]{"T#1", "T1#1", "T3#1", "T4#1", "T5#1"});
        assertTags("REPLICATE-T#1-1", new String[]{"T1#1*1", "T3#1*1", "T4#1*1", "T5#1*1"});
        assertTags("REPLICATE-T#1-2", new String[]{"T1#1*2", "T3#1*2", "T4#1*2", "T5#1*2"});
    }


    @Test
    public void testReplicationReplicateCombined2Tag() throws Exception {
        InternalTask T1 = createReplicateTask("T1", null, null, null, 3);
        InternalTask T2 = createTask("T2", new InternalTask[]{T1}, FlowBlock.START, "T4");
        InternalTask T3 = createTask("T3", new InternalTask[]{T2}, null, null);
        InternalTask T4 = createLoopTask("T4", "loop = true;", new InternalTask[]{T3}, "T2", true);

        execute(new String[]{"T1", "T2", "T3", "T4",
                "T2#1", "T3#1", "T4#1",
                "T2*1", "T3*1", "T4*1",
                "T2#1*1", "T3#1*1", "T4#1*1",
                "T2*2", "T3*2", "T4*2",
                "T2#1*2", "T3#1*2", "T4#1*2"
        });

        assertTags("LOOP-T4-1", new String[]{"T2#1", "T3#1", "T4#1"});
        assertTags("LOOP-T4-2", new String[]{"T2#2", "T3#2", "T4#2"});
        assertTags("REPLICATE-T1-1", new String[]{"T2*1", "T3*1", "T4*1"});
        assertTags("LOOP-T4*1-1", new String[]{"T2#1*1", "T3#1*1", "T4#1*1"});
        assertTags("LOOP-T4*2-1", new String[]{"T2#1*2", "T3#1*2", "T4#1*2"});
        assertTags("REPLICATE-T1-2", new String[]{"T2*2", "T3*2", "T4*2"});
        assertTags("LOOP-T4*1-2", new String[]{"T2#2*1", "T3#2*1", "T4#2*1"});
        assertTags("LOOP-T4*2-2", new String[]{"T2#2*2", "T3#2*2", "T4#2*2"});
    }


    @Test
    public void testReplicationReplicateDoubleTag() throws Exception {
        InternalTask T1 = createReplicateTask("T1", null, null, null, 3);
        InternalTask T2 = createReplicateTask("T2", new InternalTask[]{T1}, FlowBlock.START, "T6", 3);
        InternalTask T3 = createTask("T3", new InternalTask[]{T2}, FlowBlock.START, "T5");
        InternalTask T4 = createTask("T4", new InternalTask[]{T3}, null, null);
        InternalTask T5 = createTask("T5", new InternalTask[]{T4}, FlowBlock.END, "T3");
        InternalTask T6 = createTask("T6", new InternalTask[]{T5}, FlowBlock.END, "T2");

        execute(new String[]{"T1", "T2*1", "T2*2"});

        assertTags("REPLICATE-T1-1", new String[]{"T2*1", "T3*1", "T4*1", "T5*1", "T6*1"});
        assertTags("REPLICATE-T1-2", new String[]{"T2*2", "T3*2", "T4*2", "T5*2", "T6*2"});
        assertTags("REPLICATE-T2*1-3", new String[]{"T3*3", "T4*3", "T5*3"});
        assertTags("REPLICATE-T2*1-4", new String[]{"T3*4", "T4*4", "T5*4"});
        assertTags("REPLICATE-T2*2-5", new String[]{"T3*5", "T4*5", "T5*5"});
        assertTags("REPLICATE-T2*2-6", new String[]{"T3*6", "T4*6", "T5*6"});
    }


    /*@Test
    public void testReplicationLoopDoubleTag() throws Exception {
        InternalTask T1 = createTask("T1", null, FlowBlock.START, "T5");
        InternalTask T2 = createTask("T2", new InternalTask[]{T1}, FlowBlock.START, "T4");
        InternalTask T3 = createTask("T3", new InternalTask[]{T2}, null, null);
        InternalTask T4 = createLoopTask("T4", "loop = true;", new InternalTask[]{T3}, "T2", true);
        InternalTask T5 = createLoopTask("T5", "loop = true;", new InternalTask[]{T4}, "T1", true);

        job.getJobDescriptor().getEligibleTasks();


        execute(new String[]{"T5"});

        assertTags("REPLICATE-T1-1", new String[]{"T2*1", "T3*1", "T4*1", "T5*1", "T6*1"});
        assertTags("REPLICATE-T1-2", new String[]{"T2*2", "T3*2", "T4*2", "T5*2", "T6*2"});
        assertTags("REPLICATE-T2*1-3", new String[]{"T3*3", "T4*3", "T5*3"});
        assertTags("REPLICATE-T2*1-4", new String[]{"T3*4", "T4*4", "T5*4"});
        assertTags("REPLICATE-T2*2-5", new String[]{"T3*5", "T4*5", "T5*5"});
        assertTags("REPLICATE-T2*2-6", new String[]{"T3*6", "T4*6", "T5*6"});
    }*/


    @Test
    public void testTaskLogger() throws InvalidScriptException, UnknownTaskException {
        InternalScriptTask task1 = createLoopTask("T1", "loop = true;", null, "T1", false);
        TaskLogger.getInstance().info(job.getTask("T1").getId(), "a message");
    }

    @Test
    public void testTaskLoggerWithTag() throws InvalidScriptException, UnknownTaskException {
        InternalScriptTask task1 = createLoopTask("T1", "loop = true;", null, "T1", false);
        task1.setTag("aTag");
        TaskLogger.getInstance().info(job.getTask("T1").getId(), "a message");
    }
}
