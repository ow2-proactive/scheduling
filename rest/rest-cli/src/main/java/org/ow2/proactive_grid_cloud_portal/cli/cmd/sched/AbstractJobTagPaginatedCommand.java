package org.ow2.proactive_grid_cloud_portal.cli.cmd.sched;

public abstract class AbstractJobTagPaginatedCommand extends AbstractJobTagCommand {

    protected final int MAX_PAGE_SIZE = 50;
    protected int offset = 0;
    protected int limit = MAX_PAGE_SIZE;

    public AbstractJobTagPaginatedCommand(String jobId) {
        super(jobId);
    }

    public AbstractJobTagPaginatedCommand(String jobId, String tag) {
        super(jobId, tag);
    }

    public AbstractJobTagPaginatedCommand(String jobId, String tag, int offset, int limit) {
        super(jobId, tag);
        this.offset = offset;
        this.limit = limit;
    }

    public AbstractJobTagPaginatedCommand(String jobId, int offset, int limit) {
        super(jobId);
        this.offset = offset;
        this.limit = limit;
    }
}
