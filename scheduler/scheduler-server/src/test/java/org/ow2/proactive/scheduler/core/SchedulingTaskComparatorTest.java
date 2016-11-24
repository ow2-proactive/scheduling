package org.ow2.proactive.scheduler.core;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scripting.SelectionScript;
import org.python.google.common.collect.ImmutableList;
import org.python.google.common.collect.ImmutableMap;

public class SchedulingTaskComparatorTest {

    InternalTask task1;
    InternalTask task2;
    InternalJob job1;
    InternalJob job2;
    Node node1;
    Node node2;
    Node node3;

    @Before
    public void init() throws Exception {

        task1 = Mockito.mock(InternalTask.class);
        task2 = Mockito.mock(InternalTask.class);
        job1 = Mockito.mock(InternalJob.class);
        Mockito.when(job1.getOwner()).thenReturn("admin");
        Mockito.when(job1.getPriority()).thenReturn(JobPriority.NORMAL);
        job2 = Mockito.mock(InternalJob.class);
        Mockito.when(job2.getOwner()).thenReturn("admin");
        Mockito.when(job2.getPriority()).thenReturn(JobPriority.NORMAL);
        node1 = Mockito.mock(Node.class);
        node2 = Mockito.mock(Node.class);
        node3 = Mockito.mock(Node.class);
    }

    @Test
    public void basicTestEquals() throws Exception {
        Assert.assertTrue((new SchedulingTaskComparator(task1, job1)).equals(new SchedulingTaskComparator(task2, job2)));
    }


    @Test
    public void testSelectionScripts() throws Exception {
        Mockito.when(task1.getSelectionScripts()).thenReturn(ImmutableList.of(new SelectionScript("selected = true", "javascript")));
        Mockito.when(task2.getSelectionScripts()).thenReturn(ImmutableList.of(new SelectionScript("selected = true", "javascript")));
        Assert.assertTrue((new SchedulingTaskComparator(task1, job1)).equals(new SchedulingTaskComparator(task2, job2)));

        Mockito.when(task1.getSelectionScripts()).thenReturn(ImmutableList.of(new SelectionScript("selected = true", "javascript")));
        Mockito.when(task2.getSelectionScripts()).thenReturn(ImmutableList.of(new SelectionScript("selected = false", "javascript")));
        Assert.assertFalse((new SchedulingTaskComparator(task1, job1)).equals(new SchedulingTaskComparator(task2, job2)));
    }

    @Test
    public void testSelectionScriptsUseVariables() throws Exception {
        Mockito.when(task1.getSelectionScripts()).thenReturn(ImmutableList.of(new SelectionScript("variables.get(\"PA_JOB\");selected = true", "javascript")));
        Mockito.when(task2.getSelectionScripts()).thenReturn(ImmutableList.of(new SelectionScript("selected = true", "javascript")));
        Assert.assertFalse((new SchedulingTaskComparator(task1, job1)).equals(new SchedulingTaskComparator(task2, job2)));

        Mockito.when(task1.getSelectionScripts()).thenReturn(ImmutableList.of(new SelectionScript("selected = true", "javascript")));
        Mockito.when(task2.getSelectionScripts()).thenReturn(ImmutableList.of(new SelectionScript("variables.get(\"PA_JOB\");selected = false", "javascript")));
        Assert.assertFalse((new SchedulingTaskComparator(task1, job1)).equals(new SchedulingTaskComparator(task2, job2)));
    }

    @Test
    public void testJobOwnerDiffers() throws Exception {
        Mockito.when(job2.getOwner()).thenReturn("notadmin");
        Assert.assertFalse((new SchedulingTaskComparator(task1, job1)).equals(new SchedulingTaskComparator(task2, job2)));
    }

    @Test
    public void testPriorityDiffers() throws Exception {
        Mockito.when(job2.getPriority()).thenReturn(JobPriority.HIGH);
        Assert.assertFalse((new SchedulingTaskComparator(task1, job1)).equals(new SchedulingTaskComparator(task2, job2)));
    }

    @Test
    public void testAnyParallel() throws Exception {
        Mockito.when(task1.isParallel()).thenReturn(true);
        Assert.assertFalse((new SchedulingTaskComparator(task1, job1)).equals(new SchedulingTaskComparator(task2, job2)));
        Mockito.when(task2.isParallel()).thenReturn(true);
        Assert.assertFalse((new SchedulingTaskComparator(task1, job1)).equals(new SchedulingTaskComparator(task2, job2)));
        Mockito.when(task1.isParallel()).thenReturn(false);
        Assert.assertFalse((new SchedulingTaskComparator(task1, job1)).equals(new SchedulingTaskComparator(task2, job2)));
    }

    @Test
    public void testAnyRequiresNodeWithToken() throws Exception {
        Mockito.when(task1.getRuntimeGenericInformation()).thenReturn(ImmutableMap.of(SchedulerConstants.NODE_ACCESS_TOKEN, "token"));
        Assert.assertFalse((new SchedulingTaskComparator(task1, job1)).equals(new SchedulingTaskComparator(task2, job2)));
        Mockito.when(task2.getRuntimeGenericInformation()).thenReturn(null);
        Mockito.when(task2.getRuntimeGenericInformation()).thenReturn(ImmutableMap.of(SchedulerConstants.NODE_ACCESS_TOKEN, "token"));
        Assert.assertFalse((new SchedulingTaskComparator(task1, job1)).equals(new SchedulingTaskComparator(task2, job2)));
    }

}