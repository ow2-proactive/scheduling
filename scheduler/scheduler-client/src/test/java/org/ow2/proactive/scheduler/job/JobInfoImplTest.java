package org.ow2.proactive.scheduler.job;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class JobInfoImplTest {

    @Test
    public void testGetFinishedTimeReturnsMinusOneByDefault() throws Exception {
        JobInfoImpl job = new JobInfoImpl();
        assertThat(job.getFinishedTime()).isEqualTo(-1);
    }

    @Test
    public void testGetInErrorTimeReturnsMinusOneByDefault() throws Exception {
        JobInfoImpl job = new JobInfoImpl();
        assertThat(job.getInErrorTime()).isEqualTo(-1);
    }

    @Test
    public void testGetRemovedTimeReturnsMinusOneByDefault() throws Exception {
        JobInfoImpl job = new JobInfoImpl();
        assertThat(job.getRemovedTime()).isEqualTo(-1);
    }

    @Test
    public void testGetStartTimeReturnsMinusOneByDefault() throws Exception {
        JobInfoImpl job = new JobInfoImpl();
        assertThat(job.getStartTime()).isEqualTo(-1);
    }

    @Test
    public void testGetSubmittedTimeReturnsMinusOneByDefault() throws Exception {
        JobInfoImpl job = new JobInfoImpl();
        assertThat(job.getSubmittedTime()).isEqualTo(-1);
    }

}