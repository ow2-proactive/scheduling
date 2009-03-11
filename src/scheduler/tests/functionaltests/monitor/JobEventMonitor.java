package functionaltests.monitor;

import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;


/**
 * Defines An EventMonitor for a Job event.
 * This class is used for two purposes :
 * - representing an event thrown by scheduler (memorization of an event)
 * - representing an Monitor for an event awaited and not yet occurred
 * 	(monitor object for an awaited event).
 *
 * Remark :
 * when an event concerning a job occurs, it can have 2 types of associated objects,
 * Job for 'Job submitted event', and JobEvent for other. That why getJob() method
 * is relevant only if this monitor has been constructed for a job submitted event,
 * and getJobEvent() method is relevant for other jobs event.
 *
 * @author ProActive team
 *
 */
public class JobEventMonitor extends EventMonitor {

    /**
     * JobId defining Job related
     */
    private JobId jobId;

    /**
     * JobInfo object to return to Threads that
     * call waitForEventTask**()
     */
    private JobInfo jobInfo;

    /**
     * JobEvent object to return to Threads that
     * call waitForEventJobsubmitted()
     */
    private JobState jobState;

    /**
     * Constructor, to use this Object as waiting monitor.
     * (JobEvent field is not yet defined).
     * @param evt event to wait for
     * @param id
     */
    public JobEventMonitor(SchedulerEvent evt, JobId id) {
        super(evt);
        this.jobId = id;
    }

    /**
     * Constructor, to call for using this object as memorization of an event.
     * @param evt event type
     * @param info JobInfo object associated to the occurred event.
     */
    public JobEventMonitor(SchedulerEvent evt, JobInfo info) {
        super(evt);
        jobId = info.getJobId();
        jobInfo = info;
    }

    /**
     * Constructor, to use this object as memorization of an event.
     * This constructor is used to memorize a job submitted event,
     * because associated object for this event is a Job,
     * (not a JobInfo)
     * @param evt event type
     * @param jState state object associated to event.
     */
    public JobEventMonitor(SchedulerEvent evt, JobState jState) {
        super(evt);
        jobId = jState.getId();
        this.jobState = jState;
    }

    /**
     * Get JobId object corresponding to this EventMonitor
     * @return JobId object.
     */
    public JobId getJobId() {
        return this.jobId;
    }

    /**
     * @return JobInfo object associated to this EventMonitor, if
     * event has occurred. returns null if Event hasn't yet occurred or
     * SchedulerEvent of this monitor is SchedulerEvent.JOB_SUBMITTED.
     */
    public JobInfo getJobInfo() {
        return jobInfo;
    }

    /**
     * Set associated JobInfo object for this monitor
     * (typically when event occurred)
     * @param info to associate with this monitor
     */
    public void setJobInfo(JobInfo info) {
        this.jobInfo = info;
    }

    /**
     * @return Job object of this event,
     * if SchedulerEvent of this monitor is SchedulerEvent.JOB_SUBMITTED and
     * event occurred.
     */
    public JobState getJobState() {
        return jobState;
    }

    /**
     * Set associated Job to this monitor.
     * Only used when SchedulerEvent of this monitor is SchedulerEvent.JOB_SUBMITTED.
     * and event has occurred.
     * @param jState state object associated to event.
     */
    public void setJobState(JobState jState) {
        this.jobState = jState;
    }

    public boolean equals(Object o) {
        if (super.equals(o)) {
            return (((JobEventMonitor) o).getJobId().equals(jobId));
        }
        return false;
    }
}
