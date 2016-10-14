package org.ow2.proactive.scheduler.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.job.JobIdImpl;


public class JobLoggerTest {

    @Test
    public void testGetJobLogFilename() {
        JobId id = new JobIdImpl(1123, "readableName");
        assertThat(JobLogger.getJobLogFilename(id), is("1123/1123"));
    }
}
