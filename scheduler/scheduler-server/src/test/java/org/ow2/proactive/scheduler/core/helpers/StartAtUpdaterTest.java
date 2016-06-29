package org.ow2.proactive.scheduler.core.helpers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.descriptor.JobDescriptorImpl;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.policy.ExtendedSchedulerPolicy;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.util.policy.ISO8601DateUtil;
import org.python.google.common.collect.Maps;

import com.google.common.collect.Lists;


public class StartAtUpdaterTest {

    private StartAtUpdater startAtUpdater;

    @Mock
    private InternalJob internalJob;

    @Mock
    private InternalTask internalTask;

    @Mock
    private JobDescriptorImpl jobDescriptor;

    @Mock
    private SchedulerDBManager dbManager;

    @Before
    public void init() {
        this.startAtUpdater = new StartAtUpdater();
        MockitoAnnotations.initMocks(this);

        when(internalJob.getJobDescriptor()).thenReturn(jobDescriptor);
    }

    @Test
    public void testAlreadyExistingAndSameStartAt() {

        String startAt = "2014-01-01T00:00:00+01:00";

        ArrayList<InternalTask> internalTasks = Lists.newArrayList();
        internalTasks.add(internalTask);

        when(internalJob.getITasks()).thenReturn(internalTasks);
        Map<String, String> genericInformation = Maps.newHashMap();

        genericInformation.put(ExtendedSchedulerPolicy.GENERIC_INFORMATION_KEY_START_AT, startAt);
        when(internalJob.getGenericInformations(true)).thenReturn(genericInformation);

        assertThat(startAtUpdater.updateStartAt(internalJob, startAt, dbManager), is(false));

        verify(dbManager, times(0)).updateTaskSchedulingTime(internalJob,
                ISO8601DateUtil.toDate(startAt).getTime());
    }

    @Test
    public void testNotExistingStartAt() {

        String startAt = "2014-01-01T00:00:00+01:00";

        ArrayList<InternalTask> internalTasks = Lists.newArrayList();
        internalTasks.add(internalTask);

        when(internalJob.getITasks()).thenReturn(internalTasks);
        Map<String, String> genericInformation = Maps.newHashMap();
        when(internalJob.getGenericInformations(true)).thenReturn(genericInformation);

        assertThat(startAtUpdater.updateStartAt(internalJob, startAt, dbManager), is(true));

        verify(dbManager, times(1)).updateTaskSchedulingTime(internalJob,
                ISO8601DateUtil.toDate(startAt).getTime());
    }

    @Test
    public void testExistingAndDifferentStartAt() {

        String startAt = "2035-01-01T00:00:00+01:00";

        String startAtUpdate = "2015-01-01T00:00:00+01:00";

        ArrayList<InternalTask> internalTasks = Lists.newArrayList();
        internalTasks.add(internalTask);

        when(internalJob.getITasks()).thenReturn(internalTasks);
        Map<String, String> genericInformation = Maps.newHashMap();
        genericInformation.put(ExtendedSchedulerPolicy.GENERIC_INFORMATION_KEY_START_AT, startAt);
        when(internalJob.getGenericInformations(true)).thenReturn(genericInformation);

        assertThat(startAtUpdater.updateStartAt(internalJob, startAtUpdate, dbManager), is(true));

        verify(dbManager, times(1)).updateTaskSchedulingTime(internalJob,
                ISO8601DateUtil.toDate(startAtUpdate).getTime());
    }

    @Test
    public void testExistingAndDifferentStartAtButNoTasks() {

        String startAt = "2035-01-01T00:00:00+01:00";

        String startAtUpdate = "2015-01-01T00:00:00+01:00";

        ArrayList<InternalTask> internalTasks = Lists.newArrayList();
        when(internalJob.getITasks()).thenReturn(internalTasks);
        Map<String, String> genericInformation = Maps.newHashMap();
        genericInformation.put(ExtendedSchedulerPolicy.GENERIC_INFORMATION_KEY_START_AT, startAt);
        when(internalJob.getGenericInformations(true)).thenReturn(genericInformation);

        assertThat(startAtUpdater.updateStartAt(internalJob, startAtUpdate, dbManager), is(false));

        verify(dbManager, times(0)).updateTaskSchedulingTime(internalJob,
                ISO8601DateUtil.toDate(startAtUpdate).getTime());
    }

}
