package org.ow2.proactive_grid_cloud_portal.cli.cmd.sched;

public abstract class AbstractJobTagPaginatedCommand extends AbstractJobTagCommand {

    protected final int MAX_PAGE_SIZE = 50;
    protected int offset = 0;
    protected int limit = 0;

    public AbstractJobTagPaginatedCommand(String jobId) {
        super(jobId);
    }

    public AbstractJobTagPaginatedCommand(String jobId, String tag) {
        super(jobId, tag);
    }

    public AbstractJobTagPaginatedCommand(String jobId, String tag, String offset, String limit) {
        super(jobId, tag);
        this.offset = Integer.valueOf(offset);
        this.limit = Integer.valueOf(limit);
    }

    public AbstractJobTagPaginatedCommand(String jobId, String offset, String limit) {
        super(jobId);
        this.offset = Integer.valueOf(offset);
        this.limit = Integer.valueOf(limit);
    }
}
