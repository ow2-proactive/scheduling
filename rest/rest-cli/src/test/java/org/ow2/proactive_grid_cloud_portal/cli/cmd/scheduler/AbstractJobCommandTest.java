package org.ow2.proactive_grid_cloud_portal.cli.cmd.scheduler;

import org.ow2.proactive_grid_cloud_portal.scheduler.exception.UnknownJobRestException;

/**
 * Created by Sandrine on 17/09/2015.
 */
public abstract class AbstractJobCommandTest extends AbstractSchedulerCommandTest {

    protected String jobId = "1";

    protected String unknownJobId = "2";

    protected UnknownJobRestException exceptionUnknownJob = new UnknownJobRestException("Job 2 does not exists");
}
