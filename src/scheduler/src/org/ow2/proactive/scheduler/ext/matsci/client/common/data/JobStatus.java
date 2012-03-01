package org.ow2.proactive.scheduler.ext.matsci.client.common.data;

/**
 * JobStatus a clone of the scheduler JobStatus used here to avoid class downloading
 *
 * @author The ProActive Team
 */
public enum JobStatus implements java.io.Serializable {
    /**
     * The job is waiting to be scheduled.
     */
    PENDING("Pending"),
    /**
     * The job is running. Actually at least one of its task has been scheduled.
     */
    RUNNING("Running"),
    /**
     * The job has been launched but no task are currently running.
     */
    STALLED("Stalled"),
    /**
     * The job is finished. Every tasks are finished.
     */
    FINISHED("Finished"),
    /**
     * The job is paused waiting for user to resume it.
     */
    PAUSED("Paused"),
    /**
     * The job has been canceled due to user exception and order.
     * This status runs when a user exception occurs in a task
     * and when the user has asked to cancel On exception.
     */
    CANCELED("Canceled"),
    /**
     * The job has failed. One or more tasks have failed (due to resources failure).
     * There is no more executionOnFailure left for a task.
     */
    FAILED("Failed"),
    /**
     * The job has been killed by a user..
     * Nothing can be done anymore on this job expect read execution informations
     * such as output, time, etc...
     */
    KILLED("Killed");

    /** The textual definition of the status */
    private String definition;

    /**
     * Default constructor.
     * @param def the textual definition of the status.
     */
    JobStatus(String def) {
        definition = def;
    }

    /**
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return definition;
    }

    public static JobStatus getJobStatus(String status) {
        for (JobStatus s : JobStatus.values()) {
            if (s.toString().equals(status)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Wrong status : " + status);
    }

}
